'use strict'

const fsp = require('node:fs/promises')
const os = require('node:os')
const path = require('node:path')

const { request } = require('../util/http')
const { ps } = require('./compose')
const { buildBackupText } = require('./envfile')

/**
 * 10단계 — 헬스체크 · 바로가기 · 설정 백업.
 *
 * 헬스체크 주의사항 (application-prod.properties 확인 결과)
 *   management.server.port=8081 로 Actuator 가 **메인 포트와 분리**돼 있고,
 *   docker-compose.prod.yml 은 8081 을 호스트에 매핑하지 않는다.
 *   → 호스트에서 http://localhost/api/actuator/health 는 동작하지 않는다.
 *   그래서 헬스 판정은
 *     ① 프론트(nginx) 200            ← 주 판정
 *     ② /api/* 응답이 502/504 가 아님 ← 백엔드 도달 여부
 *     ③ 컨테이너 안에서 actuator 직접 조회 (docker exec) ← 있으면 보강
 *   순서로 본다.
 */

const FRONT_URL = 'http://localhost/'
const API_PROBE_URL = 'http://localhost/api/actuator/health'
const API_FALLBACK_URL = 'http://localhost/api/'

async function checkFront() {
  try {
    const res = await request(FRONT_URL, { timeout: 8000 })
    if (res.status === 200) {
      return { id: 'front', label: '프론트엔드 (http://localhost)', status: 'ok', detail: 'HTTP 200' }
    }
    // nginx 가 auth_request 로 /api/ip-gate 판정을 받는 구성이라,
    // 접속 허용 IP 규칙에 걸리면 화면 자체가 401/403 으로 막힌다.
    if (res.status === 401 || res.status === 403) {
      return {
        id: 'front',
        label: '프론트엔드 (http://localhost)',
        status: 'fail',
        detail: `접속 허용 IP 규칙에 의해 차단되었습니다 (HTTP ${res.status}).`,
        hint:
          '6단계에서 등록한 규칙에 이 PC 가 포함되지 않았습니다. 설치 경로의 back/.env.prod 에서 ' +
          'IP_ALLOWLIST_BYPASS 에 이 PC 의 대역을 넣고 컨테이너를 재시작하거나, IP_ALLOWLIST_ENABLED=false 로 ' +
          '일시적으로 차단을 끈 뒤 관리자 화면에서 규칙을 고치세요.'
      }
    }
    return {
      id: 'front',
      label: '프론트엔드 (http://localhost)',
      status: 'warn',
      detail: `HTTP ${res.status}`
    }
  } catch (e) {
    return {
      id: 'front',
      label: '프론트엔드 (http://localhost)',
      status: 'fail',
      detail: '응답 없음',
      hint: 'front-prod 컨테이너가 기동 중인지 확인하세요. (docker compose ps)'
    }
  }
}

async function checkBackend() {
  // 1) actuator 가 메인 포트에 열려 있는 구성이면 여기서 바로 200 이 온다
  try {
    const res = await request(API_PROBE_URL, { timeout: 8000 })
    if (res.status === 200 && res.json && res.json.status) {
      return {
        id: 'back',
        label: '백엔드 API',
        status: res.json.status === 'UP' ? 'ok' : 'warn',
        detail: `/api/actuator/health → ${res.json.status}`
      }
    }
    if (res.status === 502 || res.status === 504) {
      return {
        id: 'back',
        label: '백엔드 API',
        status: 'fail',
        detail: `nginx 가 백엔드에 닿지 못했습니다 (HTTP ${res.status})`,
        hint: 'back-prod 컨테이너 로그를 확인하세요. DB 접속 실패로 기동이 멈춰 있는 경우가 가장 흔합니다.'
      }
    }
    // 404 등: nginx→back 프록시는 살아 있다는 뜻
    return {
      id: 'back',
      label: '백엔드 API',
      status: 'ok',
      detail:
        `백엔드 도달 확인 (HTTP ${res.status}). Actuator 는 management.server.port=8081 로 분리돼 있어 ` +
        '호스트에서 직접 조회되지 않는 것이 정상입니다.'
    }
  } catch {
    // 2) 프록시 자체가 안 떴을 수 있으니 루트로 한 번 더
    try {
      const res2 = await request(API_FALLBACK_URL, { timeout: 8000 })
      return {
        id: 'back',
        label: '백엔드 API',
        status: res2.status === 502 || res2.status === 504 ? 'fail' : 'ok',
        detail: `HTTP ${res2.status}`
      }
    } catch {
      return {
        id: 'back',
        label: '백엔드 API',
        status: 'fail',
        detail: '응답 없음',
        hint: '컨테이너 기동에 1~3분이 걸릴 수 있습니다. 잠시 후 [다시 검사] 를 눌러보세요.'
      }
    }
  }
}

async function checkContainers(installDir) {
  const r = await ps(installDir)
  if (!r.ok) {
    return {
      id: 'containers',
      label: '컨테이너 상태',
      status: 'warn',
      detail: r.message || 'docker compose ps 실패'
    }
  }
  const bad = r.services.filter((s) => !/running|up/i.test(s.state || s.status || ''))
  return {
    id: 'containers',
    label: '컨테이너 상태',
    status: bad.length === 0 && r.services.length > 0 ? 'ok' : bad.length ? 'fail' : 'warn',
    detail: r.services.map((s) => `${s.service}: ${s.status || s.state}`).join(' / ') || '실행 중인 서비스 없음',
    services: r.services
  }
}

async function healthCheck(installDir) {
  const checks = []
  checks.push(await checkContainers(installDir))
  checks.push(await checkFront())
  checks.push(await checkBackend())
  const failed = checks.filter((c) => c.status === 'fail')
  return {
    checkedAt: new Date().toISOString(),
    checks,
    healthy: failed.length === 0,
    url: FRONT_URL
  }
}

/**
 * 바탕화면 바로가기.
 * 인터넷 바로가기(.url)를 쓴다 — 추가 의존성 없이 Windows/기타 OS 모두에서 만들 수 있고,
 * 기본 브라우저로 열린다.
 */
async function createDesktopShortcut({ url = FRONT_URL, name = 'Playbook 도서관리' } = {}) {
  const desktop = path.join(os.homedir(), 'Desktop')
  let target = desktop
  try {
    await fsp.access(desktop)
  } catch {
    // 한국어 Windows 는 보통 Desktop 이지만, OneDrive 리디렉션 등을 대비
    const alt = path.join(os.homedir(), 'OneDrive', 'Desktop')
    try {
      await fsp.access(alt)
      target = alt
    } catch {
      return { ok: false, message: '바탕화면 폴더를 찾지 못했습니다. 브라우저 즐겨찾기에 직접 추가해 주세요.' }
    }
  }

  const filePath = path.join(target, `${name}.url`)
  const content = ['[InternetShortcut]', `URL=${url}`, 'IconIndex=0', ''].join('\r\n')
  try {
    await fsp.writeFile(filePath, content, 'utf8')
    return { ok: true, path: filePath }
  } catch (e) {
    return { ok: false, message: `바로가기 생성 실패: ${e.message}` }
  }
}

/** 설정 백업 파일 (시크릿 평문 — 사용자가 명시적으로 요청할 때만 호출) */
async function saveBackup(state, targetDir) {
  const stamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19)
  const dir = targetDir || path.join(os.homedir(), 'Desktop')
  let outDir = dir
  try {
    await fsp.access(outDir)
  } catch {
    outDir = state.installDir || os.homedir()
    await fsp.mkdir(outDir, { recursive: true })
  }
  const filePath = path.join(outDir, `playbook-설치정보-${stamp}.txt`)
  await fsp.writeFile(filePath, buildBackupText(state), { encoding: 'utf8', mode: 0o600 })
  try {
    await fsp.chmod(filePath, 0o600)
  } catch {
    /* Windows 무시 */
  }
  return { ok: true, path: filePath }
}

module.exports = { healthCheck, createDesktopShortcut, saveBackup, FRONT_URL }
