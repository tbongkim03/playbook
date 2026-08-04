'use strict'

const net = require('node:net')
const fs = require('node:fs')
const fsp = require('node:fs/promises')
const path = require('node:path')

const { runCapture, isWin } = require('./exec')

/**
 * 1단계 — 설치 환경 점검.
 * 각 항목은 { id, label, status: 'ok'|'warn'|'fail'|'skip', detail, hint, downloadUrl } 로 통일한다.
 */

const DOCKER_DESKTOP_URL = 'https://www.docker.com/products/docker-desktop/'
const WSL2_DOC_URL = 'https://learn.microsoft.com/ko-kr/windows/wsl/install'

const REQUIRED_PORTS = [80, 8080]
const MIN_FREE_GB = 20

async function checkDockerInstalled() {
  const r = await runCapture('docker', ['--version'], { timeout: 20000 })
  if (r.spawnError || !r.ok) {
    return {
      id: 'docker-installed',
      label: 'Docker Desktop 설치',
      status: 'fail',
      detail: 'docker 명령을 찾을 수 없습니다.',
      hint: 'Docker Desktop 을 설치한 뒤 PC 를 재부팅하고 [다시 검사] 를 누르세요.',
      downloadUrl: DOCKER_DESKTOP_URL
    }
  }
  return {
    id: 'docker-installed',
    label: 'Docker Desktop 설치',
    status: 'ok',
    detail: r.stdout.trim()
  }
}

async function checkDockerRunning() {
  const r = await runCapture('docker', ['info', '--format', '{{.ServerVersion}}'], { timeout: 45000 })
  if (r.spawnError) {
    return {
      id: 'docker-running',
      label: 'Docker 엔진 기동',
      status: 'fail',
      detail: 'docker 명령 실행 불가',
      hint: 'Docker Desktop 설치를 먼저 완료하세요.',
      downloadUrl: DOCKER_DESKTOP_URL
    }
  }
  if (!r.ok) {
    return {
      id: 'docker-running',
      label: 'Docker 엔진 기동',
      status: 'fail',
      detail: (r.stderr || '').split('\n')[0] || '엔진에 연결할 수 없습니다.',
      hint: '작업 표시줄에서 Docker Desktop 을 실행하고, 고래 아이콘이 "Running" 이 될 때까지 기다린 뒤 [다시 검사] 를 누르세요.'
    }
  }
  return {
    id: 'docker-running',
    label: 'Docker 엔진 기동',
    status: 'ok',
    detail: `서버 버전 ${r.stdout.trim()}`
  }
}

async function checkComposeV2() {
  const r = await runCapture('docker', ['compose', 'version', '--short'], { timeout: 30000 })
  if (!r.ok) {
    return {
      id: 'compose',
      label: 'Docker Compose v2',
      status: 'fail',
      detail: 'docker compose 플러그인을 찾을 수 없습니다.',
      hint: 'Docker Desktop 최신 버전으로 업데이트하세요. (구형 docker-compose 단독 실행 파일은 지원하지 않습니다)',
      downloadUrl: DOCKER_DESKTOP_URL
    }
  }
  return { id: 'compose', label: 'Docker Compose v2', status: 'ok', detail: `v${r.stdout.trim()}` }
}

async function checkWsl2() {
  if (!isWin) {
    return {
      id: 'wsl2',
      label: 'WSL2',
      status: 'skip',
      detail: 'Windows 가 아니므로 건너뜁니다. (개발 환경 확인용 실행)'
    }
  }
  // wsl.exe 는 기본적으로 UTF-16LE 로 출력한다
  const r = await runCapture('wsl.exe', ['--status'], { timeout: 30000 })
  const raw = `${r.stdout}${r.stderr}`
  const text = raw.includes('\u0000') ? Buffer.from(raw, 'binary').toString('utf16le') : raw
  if (r.spawnError) {
    return {
      id: 'wsl2',
      label: 'WSL2',
      status: 'fail',
      detail: 'wsl 명령을 찾을 수 없습니다.',
      hint: '관리자 PowerShell 에서 `wsl --install` 실행 후 재부팅하세요.',
      downloadUrl: WSL2_DOC_URL
    }
  }
  const listed = await runCapture('wsl.exe', ['-l', '-v'], { timeout: 30000 })
  const listRaw = `${listed.stdout}${listed.stderr}`
  const listText = listRaw.includes('\u0000') ? Buffer.from(listRaw, 'binary').toString('utf16le') : listRaw
  const hasV2 = /\s2\s*$/m.test(listText) || /기본 버전:\s*2/.test(text) || /Default Version:\s*2/i.test(text)
  return {
    id: 'wsl2',
    label: 'WSL2',
    status: hasV2 ? 'ok' : 'warn',
    detail: hasV2 ? 'WSL2 배포판이 확인되었습니다.' : 'WSL2 배포판을 확인하지 못했습니다.',
    hint: hasV2
      ? undefined
      : 'Docker Desktop 이 Hyper-V 백엔드로 동작 중일 수 있습니다. 설정 > General > "Use WSL 2 based engine" 을 권장합니다.',
    downloadUrl: hasV2 ? undefined : WSL2_DOC_URL
  }
}

function probePort(port) {
  return new Promise((resolve) => {
    const server = net.createServer()
    server.once('error', (err) => {
      resolve({ free: false, code: err.code })
    })
    server.once('listening', () => {
      server.close(() => resolve({ free: true }))
    })
    // 0.0.0.0 으로 바인딩해야 docker 가 쓰려는 것과 같은 조건이 된다
    server.listen(port, '0.0.0.0')
  })
}

/** 포트를 이미 우리 컨테이너가 쓰고 있는 경우는 '충돌'이 아니라 '재설치'다 */
async function whoHoldsPort() {
  const r = await runCapture('docker', ['ps', '--format', '{{.Names}}\t{{.Ports}}'], { timeout: 20000 })
  if (!r.ok) return []
  return r.stdout
    .split('\n')
    .filter(Boolean)
    .map((l) => {
      const [name, ports] = l.split('\t')
      return { name, ports: ports || '' }
    })
}

async function checkPorts() {
  const holders = await whoHoldsPort()
  const results = []
  for (const port of REQUIRED_PORTS) {
    const p = await probePort(port)
    if (p.free) {
      results.push({
        id: `port-${port}`,
        label: `포트 ${port} 사용 가능`,
        status: 'ok',
        detail: '비어 있습니다.'
      })
      continue
    }
    const holder = holders.find((h) => h.ports.includes(`:${port}->`))
    if (holder) {
      results.push({
        id: `port-${port}`,
        label: `포트 ${port} 사용 가능`,
        status: 'warn',
        detail: `이미 Playbook 컨테이너(${holder.name})가 사용 중입니다.`,
        hint: '재설치·업데이트라면 그대로 진행해도 됩니다. 8단계에서 컨테이너가 교체됩니다.'
      })
      continue
    }
    results.push({
      id: `port-${port}`,
      label: `포트 ${port} 사용 가능`,
      status: 'fail',
      detail: `다른 프로그램이 포트 ${port} 을(를) 점유하고 있습니다. (${p.code || 'EADDRINUSE'})`,
      hint:
        port === 80
          ? 'IIS·Apache·Skype 등이 흔한 원인입니다. 관리자 명령 프롬프트에서 `netstat -ano | findstr :80` 으로 PID 를 확인해 종료하세요.'
          : '다른 개발 서버가 8080 을 쓰고 있는지 확인하세요. `netstat -ano | findstr :8080`'
    })
  }
  return results
}

async function checkDisk(targetDir) {
  const probe = targetDir && targetDir.length ? path.parse(path.resolve(targetDir)).root : path.parse(process.cwd()).root
  try {
    if (typeof fsp.statfs !== 'function') {
      return {
        id: 'disk',
        label: `디스크 여유 공간 (${MIN_FREE_GB}GB 이상)`,
        status: 'warn',
        detail: '이 Node 런타임에서는 디스크 용량을 확인할 수 없습니다.',
        hint: '탐색기에서 설치 드라이브의 여유 공간을 직접 확인하세요.'
      }
    }
    const st = await fsp.statfs(probe)
    const freeGb = (st.bsize * st.bavail) / 1024 ** 3
    return {
      id: 'disk',
      label: `디스크 여유 공간 (${MIN_FREE_GB}GB 이상)`,
      status: freeGb >= MIN_FREE_GB ? 'ok' : 'fail',
      detail: `${probe} 여유 ${freeGb.toFixed(1)}GB`,
      hint:
        freeGb >= MIN_FREE_GB
          ? undefined
          : `Docker 이미지(백엔드·프론트·MariaDB·Redis)와 DB 데이터에 최소 ${MIN_FREE_GB}GB 가 필요합니다.`
    }
  } catch (e) {
    return {
      id: 'disk',
      label: `디스크 여유 공간 (${MIN_FREE_GB}GB 이상)`,
      status: 'warn',
      detail: `확인 실패: ${e.message}`
    }
  }
}

function checkInstallDir(targetDir) {
  if (!targetDir) {
    return { id: 'install-dir', label: '설치 경로', status: 'fail', detail: '설치 경로가 지정되지 않았습니다.' }
  }
  const abs = path.resolve(targetDir)
  let probe = abs
  while (!fs.existsSync(probe) && path.dirname(probe) !== probe) probe = path.dirname(probe)
  try {
    fs.accessSync(probe, fs.constants.W_OK)
    return {
      id: 'install-dir',
      label: '설치 경로 쓰기 권한',
      status: 'ok',
      detail: abs + (fs.existsSync(abs) ? ' (이미 존재 — 기존 설치 갱신)' : ' (새로 생성)')
    }
  } catch {
    return {
      id: 'install-dir',
      label: '설치 경로 쓰기 권한',
      status: 'fail',
      detail: `${probe} 에 쓸 수 없습니다.`,
      hint: '관리자 권한이 필요 없는 경로(예: C:\\Playbook)를 고르거나, 마법사를 관리자 권한으로 실행하세요.'
    }
  }
}

async function runAll(installDir) {
  const checks = []
  const dockerInstalled = await checkDockerInstalled()
  checks.push(dockerInstalled)

  if (dockerInstalled.status === 'ok') {
    checks.push(await checkDockerRunning())
    checks.push(await checkComposeV2())
  } else {
    checks.push({
      id: 'docker-running',
      label: 'Docker 엔진 기동',
      status: 'skip',
      detail: 'Docker 미설치로 건너뜀'
    })
    checks.push({ id: 'compose', label: 'Docker Compose v2', status: 'skip', detail: 'Docker 미설치로 건너뜀' })
  }

  checks.push(await checkWsl2())
  checks.push(...(await checkPorts()))
  checks.push(await checkDisk(installDir))
  checks.push(checkInstallDir(installDir))

  const blocking = checks.filter((c) => c.status === 'fail')
  return {
    checkedAt: new Date().toISOString(),
    platform: process.platform,
    checks,
    canProceed: blocking.length === 0,
    blockingCount: blocking.length,
    links: { docker: DOCKER_DESKTOP_URL, wsl2: WSL2_DOC_URL }
  }
}

module.exports = { runAll, DOCKER_DESKTOP_URL, WSL2_DOC_URL }
