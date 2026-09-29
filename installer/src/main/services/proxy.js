'use strict'

const fs = require('node:fs')
const fsp = require('node:fs/promises')
const os = require('node:os')
const path = require('node:path')

const { runCapture } = require('./exec')
const { request } = require('../util/http')
const composeSvc = require('./compose')
const network = require('./network')

/**
 * Windows 네이티브 접속 프록시 (Caddy).
 *
 * <p>왜 필요한가</p>
 * Windows Docker Desktop 은 게시 포트로 들어온 접속의 원래 IP 를 보존하지 않는다.
 * front(nginx) 가 게시 포트 80 을 직접 받으면 모든 접속이 Docker 게이트웨이(172.31.240.x)에서
 * 온 것으로 보여 IP 허용목록이 "전부 허용" 아니면 "전부 차단" 이 된다.
 * 그래서 Caddy 가 Windows 에서 직접 80 을 받아 실제 IP 를 본 뒤, localhost 전용으로 게시한
 * front(127.0.0.1:18080) 로 넘긴다.
 *
 * <p>헤더 계약 (front/nginx-prod.conf)</p>
 *   X-Campus-Client-IP   : Caddy 가 직접 본 접속 IP ({remote_host}). 클라이언트가 보낸 값은 덮어쓴다
 *   X-Campus-Proxy-Token : 설치마다 생성한 토큰. front 는 이 값이 일치할 때만 위 헤더를 믿는다
 * 클라이언트가 보낸 X-Forwarded-For 등은 버린다.
 *
 * <p>운영 원칙</p>
 *   · Caddy 버전은 설치 프로그램에 동봉한 검증 버전만 쓴다 (자동 갱신 없음, prepare-payload 가 체크섬 고정)
 *   · admin API 끔. 설정을 바꾸면 서비스를 재시작한다
 *   · 서비스 계정은 LocalService. 프록시 폴더 읽기 + 로그 폴더 쓰기만 준다
 *   · 방화벽은 TCP 80 을 caddy.exe 에만, Private·Domain 프로필에만 연다
 *   · Caddy 가 멈추면 외부 접속은 실패한다 — 18080 은 localhost 전용이라 밖으로 새지 않는다
 */

const SERVICE_NAME = 'PlaybookProxy'
const SERVICE_DISPLAY = 'Playbook 접속 프록시'
const FIREWALL_RULE = 'Playbook 웹 (TCP 80)'
const FRONT_BIND = '127.0.0.1'
const FRONT_PORT = 18080
const DOCKER_NETWORK_PREFIX = '172.31.240.' // docker-compose.prod.yml prod-network

const isWindows = () => process.platform === 'win32'

function proxyDir(installDir) {
  return path.join(installDir, 'proxy')
}

/** 동봉된 caddy.exe (설치 프로그램 payload/proxy) */
function bundledCaddy(app) {
  const root = composeSvc.payloadRoot(app)
  if (!root) return null
  const p = path.join(root, 'proxy', 'caddy.exe')
  return fs.existsSync(p) ? p : null
}

function buildCaddyfile(token, logDir) {
  if (!/^[A-Za-z0-9_-]{32,}$/.test(token || '')) throw new Error('프록시 토큰이 올바르지 않습니다.')
  const logFile = path.join(logDir, 'access.log').replace(/\\/g, '/')
  return [
    '# Playbook 접속 프록시 — 설치 마법사가 생성했습니다. 직접 고치지 말고 마법사에서 다시 설치하세요.',
    '{',
    '\tadmin off',
    '\tauto_https off',
    '\tpersist_config off',
    '}',
    '',
    ':80 {',
    '\tlog {',
    `\t\toutput file "${logFile}" {`,
    '\t\t\troll_size 10MiB',
    '\t\t\troll_keep 5',
    '\t\t}',
    '\t}',
    `\treverse_proxy ${FRONT_BIND}:${FRONT_PORT} {`,
    '\t\t# 클라이언트가 보낸 전달 헤더는 쓰지 않는다',
    '\t\theader_up -X-Forwarded-For',
    '\t\theader_up -X-Real-IP',
    '\t\theader_up -Forwarded',
    '\t\t# Caddy 가 직접 본 접속 IP 만 넘긴다 (클라이언트가 같은 이름으로 보낸 값은 덮어쓴다)',
    '\t\theader_up X-Campus-Client-IP {remote_host}',
    `\t\theader_up X-Campus-Proxy-Token "${token}"`,
    '\t}',
    '}',
    ''
  ].join('\r\n')
}

/** compose .env 에 넣을 값 — front 를 localhost 전용으로 게시하고 토큰을 넘긴다 */
function composeEnvEntries(token) {
  return [
    { key: 'FRONT_BIND', value: FRONT_BIND },
    { key: 'FRONT_PORT', value: String(FRONT_PORT) },
    { key: 'NATIVE_PROXY_TOKEN', value: token }
  ]
}

/** KEY=VALUE 줄을 바꾸거나 없으면 붙인다 */
function setEnvLines(text, entries) {
  let out = text || ''
  for (const { key, value } of entries) {
    const line = `${key}=${value}`
    const re = new RegExp(`^#?\\s*${key}=.*$`, 'm')
    if (re.test(out)) out = out.replace(re, line)
    else out = `${out.replace(/\s*$/, '')}\n${line}\n`
  }
  return out
}

/** PowerShell 작은따옴표 문자열 */
const psq = (s) => `'${String(s).replace(/'/g, "''")}'`

/**
 * 관리자 권한으로 실행할 설치 스크립트.
 * 결과는 resultFile 에 OK / ERROR: ... 로 남긴다 (권한 상승된 프로세스의 출력은 받을 수 없다).
 */
function buildInstallScript({ caddySrc, caddyfileSrc, dir, resultFile }) {
  return `
$ErrorActionPreference = 'Stop'
$result = ${psq(resultFile)}
try {
  $svc = ${psq(SERVICE_NAME)}
  $dir = ${psq(dir)}
  $logs = Join-Path $dir 'logs'
  $exe = Join-Path $dir 'caddy.exe'
  $cfg = Join-Path $dir 'Caddyfile'

  $existing = Get-Service -Name $svc -ErrorAction SilentlyContinue
  if ($existing) {
    if ($existing.Status -ne 'Stopped') { Stop-Service -Name $svc -Force; (Get-Service $svc).WaitForStatus('Stopped', '00:00:20') }
  }

  New-Item -ItemType Directory -Force -Path $dir, $logs | Out-Null
  Copy-Item -LiteralPath ${psq(caddySrc)} -Destination $exe -Force
  Copy-Item -LiteralPath ${psq(caddyfileSrc)} -Destination $cfg -Force

  # 권한: 상속을 끊고 관리자·SYSTEM 전체, LocalService 는 읽기·실행만. 로그 폴더만 LocalService 수정 허용.
  # (일반 사용자가 Caddyfile 을 바꿔 토큰·전달 대상을 조작하지 못하게 한다)
  & icacls $dir /inheritance:r /grant:r '*S-1-5-32-544:(OI)(CI)F' '*S-1-5-18:(OI)(CI)F' '*S-1-5-19:(OI)(CI)RX' | Out-Null
  if ($LASTEXITCODE -ne 0) { throw "icacls 실패 ($LASTEXITCODE)" }
  & icacls $logs /grant '*S-1-5-19:(OI)(CI)M' | Out-Null
  if ($LASTEXITCODE -ne 0) { throw "icacls(logs) 실패 ($LASTEXITCODE)" }

  $bin = '"' + $exe + '" run --config "' + $cfg + '" --adapter caddyfile'
  if (-not $existing) {
    & sc.exe create $svc binPath= $bin start= auto obj= 'NT AUTHORITY\\LocalService' DisplayName= ${psq(SERVICE_DISPLAY)} | Out-Null
  } else {
    & sc.exe config $svc binPath= $bin start= auto obj= 'NT AUTHORITY\\LocalService' DisplayName= ${psq(SERVICE_DISPLAY)} | Out-Null
  }
  if ($LASTEXITCODE -ne 0) { throw "서비스 등록 실패 ($LASTEXITCODE)" }
  & sc.exe description $svc '외부 접속(80)을 받아 실제 접속 IP 를 붙여 Playbook 으로 넘깁니다.' | Out-Null
  & sc.exe failure $svc reset= 86400 actions= restart/5000/restart/5000/restart/30000 | Out-Null

  Get-NetFirewallRule -DisplayName ${psq(FIREWALL_RULE)} -ErrorAction SilentlyContinue | Remove-NetFirewallRule
  New-NetFirewallRule -DisplayName ${psq(FIREWALL_RULE)} -Direction Inbound -Action Allow -Protocol TCP -LocalPort 80 -Program $exe -Profile Private,Domain | Out-Null

  Start-Service -Name $svc
  (Get-Service $svc).WaitForStatus('Running', '00:00:20')
  Set-Content -LiteralPath $result -Value 'OK' -Encoding UTF8
} catch {
  Set-Content -LiteralPath $result -Value ('ERROR: ' + $_.Exception.Message) -Encoding UTF8
  exit 1
}
`
}

/** 스크립트를 UAC 권한 상승으로 실행하고 끝날 때까지 기다린다 */
async function runElevated(scriptPath, resultFile) {
  const launcher =
    `try { $p = Start-Process -FilePath 'powershell.exe' -Verb RunAs -Wait -PassThru -WindowStyle Hidden ` +
    `-ArgumentList @('-NoProfile','-ExecutionPolicy','Bypass','-File',${psq(scriptPath)}); exit $p.ExitCode } ` +
    `catch { Write-Output 'CANCELED'; exit 1223 }`
  const r = await runCapture('powershell.exe', ['-NoProfile', '-Command', launcher], { timeout: 5 * 60 * 1000 })
  let result = ''
  try {
    result = (await fsp.readFile(resultFile, 'utf8')).replace(/^﻿/, '').trim()
  } catch {
    /* 스크립트가 시작되지 못함 */
  }
  if (/CANCELED/.test(r.stdout || '') || r.code === 1223) {
    return { ok: false, message: '관리자 권한 요청(UAC)이 취소되었습니다. 다시 시도해 [예] 를 눌러 주세요.' }
  }
  if (result === 'OK') return { ok: true }
  return { ok: false, message: result.replace(/^ERROR:\s*/, '') || (r.stderr || '').trim() || `종료 코드 ${r.code}` }
}

async function serviceStatus() {
  if (!isWindows()) return { supported: false }
  const r = await runCapture('sc.exe', ['query', SERVICE_NAME], { timeout: 15000 })
  if (!r.ok) return { supported: true, installed: false }
  const m = /STATE\s*:\s*\d+\s+(\w+)/.exec(r.stdout || '')
  return { supported: true, installed: true, state: m ? m[1] : 'UNKNOWN', running: !!m && m[1] === 'RUNNING' }
}

/**
 * 프록시 설치(재설치). 순서:
 *   ① compose .env 에 FRONT_BIND/FRONT_PORT/토큰 → front 재생성 (이제 127.0.0.1:18080)
 *   ② Caddyfile 생성 → 관리자 권한으로 서비스 등록·방화벽·시작
 */
async function install(app, installDir, token, { onLog = () => {}, secretValues = [], profiles = [] } = {}) {
  if (!isWindows()) return { ok: false, message: '네이티브 프록시는 Windows 설치본에서만 씁니다.' }
  const caddySrc = bundledCaddy(app)
  if (!caddySrc) return { ok: false, message: '설치 프로그램에 caddy.exe 가 없습니다. 설치 파일이 손상되었을 수 있습니다.' }

  onLog('① front 를 localhost 전용(127.0.0.1:18080)으로 다시 게시합니다…')
  const envPath = path.join(installDir, '.env')
  const envText = fs.existsSync(envPath) ? await fsp.readFile(envPath, 'utf8') : ''
  await fsp.writeFile(envPath, setEnvLines(envText, composeEnvEntries(token)), 'utf8')
  const up = await composeSvc.up(installDir, { onLog, secretValues, services: ['front'], profiles })
  if (!up.ok) return { ok: false, message: 'front 컨테이너 재생성 실패. 위 로그를 확인하세요.' }

  onLog('② 프록시 서비스를 설치합니다 — 관리자 권한 확인 창이 뜨면 [예] 를 누르세요.')
  const work = await fsp.mkdtemp(path.join(os.tmpdir(), 'playbook-proxy-'))
  try {
    const caddyfileSrc = path.join(work, 'Caddyfile')
    const scriptPath = path.join(work, 'install.ps1')
    const resultFile = path.join(work, 'result.txt')
    const dir = proxyDir(installDir)
    await fsp.writeFile(caddyfileSrc, buildCaddyfile(token, path.join(dir, 'logs')), 'utf8')
    // PowerShell 5 는 BOM 없는 UTF-8 스크립트의 한글을 깨뜨린다
    await fsp.writeFile(scriptPath, '﻿' + buildInstallScript({ caddySrc, caddyfileSrc, dir, resultFile }), 'utf8')
    const r = await runElevated(scriptPath, resultFile)
    if (!r.ok) return { ok: false, message: `프록시 서비스 설치 실패: ${r.message}` }
  } finally {
    await fsp.rm(work, { recursive: true, force: true })
  }
  onLog(`✔ ${SERVICE_DISPLAY} 서비스 실행 중 (방화벽: TCP 80, 개인·도메인 네트워크)`)
  return { ok: true }
}

// ── 검증 ─────────────────────────────────────────────────────────────────────

async function login(base, master) {
  const res = await fetch(`${base}/admin/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ seqCampus: 0, idAdmin: master.id, pwAdmin: master.pw }),
    signal: AbortSignal.timeout(15000)
  })
  if (!res.ok) return null
  const raw = res.headers.getSetCookie ? res.headers.getSetCookie() : []
  return raw.map((c) => c.split(';')[0]).join('; ') || null
}

async function seenIp(host, master) {
  const base = `http://${host}/api`
  try {
    const cookie = await login(base, master)
    if (!cookie) return { ok: false, message: `${host} 로 로그인하지 못했습니다` }
    const r = await request(`${base}/allowed-ips/my-ip`, { headers: { Cookie: cookie }, timeout: 15000 })
    if (r.status === 403) return { ok: false, blocked: true, message: `${host} 접속이 IP 허용목록에 막혔습니다` }
    const ip = r.json && r.json.data && r.json.data.clientIp
    return ip ? { ok: true, ip } : { ok: false, message: `my-ip 응답을 해석하지 못했습니다 (HTTP ${r.status})` }
  } catch (e) {
    return { ok: false, message: `${host} 에 접속하지 못했습니다: ${e.message}` }
  }
}

const isLoopback = (ip) => /^127\./.test(ip) || ip === '::1' || ip === '0:0:0:0:0:0:0:1'

/**
 * 서버가 실제 접속 IP 를 보는지 확인한다.
 *   localhost 로 접속 → 루프백으로 보여야 한다
 *   이 PC 의 LAN IP 로 접속 → 그 LAN IP 로 보여야 한다
 * 둘 다 Docker 대역으로 보이면 프록시를 거치지 않거나 front 이미지가 구버전이다.
 */
async function verify(master) {
  const checks = []
  const local = await seenIp('localhost', master)
  checks.push({
    id: 'loopback',
    label: 'localhost 접속',
    status: local.ok ? (isLoopback(local.ip) ? 'ok' : 'fail') : 'fail',
    detail: local.ok ? `서버가 본 IP: ${local.ip}` : local.message
  })

  const primary = network.detect().primary
  if (!primary) {
    checks.push({ id: 'lan', label: 'LAN IP 접속', status: 'warn', detail: '이 PC 의 LAN IP 를 찾지 못했습니다.' })
  } else {
    const lan = await seenIp(primary.address, master)
    checks.push({
      id: 'lan',
      label: `LAN IP 접속 (${primary.address})`,
      status: lan.ok ? (lan.ip === primary.address ? 'ok' : 'fail') : lan.blocked ? 'warn' : 'fail',
      detail: lan.ok ? `서버가 본 IP: ${lan.ip}` : lan.message
    })
  }

  const docker = checks.find((c) => /서버가 본 IP: 172\.31\.240\./.test(c.detail || ''))
  return {
    ok: checks.every((c) => c.status !== 'fail'),
    checks,
    hint: docker
      ? `서버가 Docker 내부 주소(${DOCKER_NETWORK_PREFIX}x)로 보고 있습니다. 프록시를 거치지 않았거나 front 이미지가 구버전입니다. [업데이트] 로 최신 버전을 받은 뒤 다시 설치하세요.`
      : null
  }
}

module.exports = {
  SERVICE_NAME,
  FRONT_PORT,
  isWindows,
  bundledCaddy,
  buildCaddyfile,
  buildInstallScript,
  composeEnvEntries,
  setEnvLines,
  serviceStatus,
  install,
  verify
}
