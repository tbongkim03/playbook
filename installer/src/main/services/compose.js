'use strict'

const fs = require('node:fs')
const fsp = require('node:fs/promises')
const path = require('node:path')

const { runCapture, runStream } = require('./exec')

/**
 * 8단계 — 배포 페이로드 전개 + docker compose pull / up.
 *
 * 마법사 .exe 에는 docker-compose.prod.yml, db/(Dockerfile·init·data·conf·migration),
 * monitoring/ 이 동봉되어 있다(extraResources). 이를 사용자가 고른 설치 경로로 복사한 뒤
 * 그 경로에서 docker compose 를 실행한다.
 */

const COMPOSE_FILE = 'docker-compose.prod.yml'

/** 개발 모드에서는 저장소 루트, 패키징 후에는 resources/payload */
function payloadRoot(app) {
  const packaged = path.join(process.resourcesPath || '', 'payload')
  if (process.resourcesPath && fs.existsSync(path.join(packaged, COMPOSE_FILE))) return packaged

  // 개발 실행: installer/payload → 없으면 저장소 루트를 직접 본다
  const devPayload = path.resolve(__dirname, '../../../payload')
  if (fs.existsSync(path.join(devPayload, COMPOSE_FILE))) return devPayload

  const repoRoot = path.resolve(__dirname, '../../../..')
  if (fs.existsSync(path.join(repoRoot, COMPOSE_FILE))) return repoRoot

  return null
}

/**
 * 페이로드를 설치 경로로 복사한다.
 * 기존 .env.prod 는 절대 덮어쓰지 않는다 (재설치 시 시크릿 유실 방지).
 */
async function stagePayload(app, installDir, onLog = () => {}) {
  const src = payloadRoot(app)
  if (!src) {
    return {
      ok: false,
      message:
        '배포 파일(docker-compose.prod.yml)을 찾을 수 없습니다. 설치 패키지가 손상되었을 수 있습니다. 재설치하세요.'
    }
  }
  onLog(`배포 파일 원본: ${src}`)
  await fsp.mkdir(installDir, { recursive: true })

  const copied = []
  const entries = await fsp.readdir(src, { withFileTypes: true })
  for (const e of entries) {
    if (e.name === 'PAYLOAD.json') continue
    // 저장소 루트를 직접 쓰는 개발 모드에서 불필요한 디렉터리를 걸러낸다
    if (['node_modules', '.git', 'installer', 'front', 'back', 'docs', '_workspace'].includes(e.name)) {
      if (!(e.name === 'front' && !e.isDirectory())) continue
    }
    const from = path.join(src, e.name)
    const to = path.join(installDir, e.name)
    await fsp.cp(from, to, {
      recursive: true,
      force: true,
      filter: (f) => path.basename(f) !== '.env.prod' // 기존 시크릿 보존
    })
    copied.push(e.name)
    onLog(`복사: ${e.name}`)
  }

  return { ok: true, installDir, copied, source: src }
}

/**
 * alloy(모니터링 사이드카)는 compose 의 `profiles: [monitoring]` 뒤에 있다.
 * 프로파일을 켜지 않으면 존재하지 않는 서비스로 취급되므로, 모니터링을 쓰는 캠퍼스에서는
 * --profile monitoring 을 반드시 붙여야 한다.
 */
function composeArgs(installDir, rest, profiles = []) {
  const profileFlags = []
  for (const p of profiles) profileFlags.push('--profile', p)
  return ['compose', ...profileFlags, '-f', path.join(installDir, COMPOSE_FILE), ...rest]
}

function profilesFor(monitoringEnabled) {
  return monitoringEnabled ? ['monitoring'] : []
}

async function validateCompose(installDir, profiles = []) {
  const r = await runCapture('docker', composeArgs(installDir, ['config', '--quiet'], profiles), {
    cwd: installDir,
    timeout: 60000
  })
  return { ok: r.ok, message: r.ok ? 'compose 파일 유효' : (r.stderr || r.stdout || '').trim() }
}

/**
 * 이미지 pull — 로그 실시간 스트리밍.
 * db 서비스는 image 가 아니라 build 로 정의돼 있으므로 --ignore-buildable 로 건너뛴다.
 * (compose v2.22 미만에는 이 플래그가 없어 실패하므로 플래그 없이 한 번 더 시도한다)
 */
async function pull(installDir, { onLog, secretValues = [], profiles = [] }) {
  onLog(`$ docker compose -f ${COMPOSE_FILE} pull --ignore-buildable`)
  let r = await runStream('docker', composeArgs(installDir, ['pull', '--ignore-buildable'], profiles), {
    cwd: installDir,
    onLog,
    secretValues,
    timeout: 30 * 60 * 1000
  })
  if (!r.ok && /unknown flag|unknown shorthand/i.test(r.stderr || '')) {
    onLog('이 Docker Compose 버전은 --ignore-buildable 을 지원하지 않습니다. 플래그 없이 다시 시도합니다.')
    onLog(`$ docker compose -f ${COMPOSE_FILE} pull`)
    r = await runStream('docker', composeArgs(installDir, ['pull'], profiles), {
      cwd: installDir,
      onLog,
      secretValues,
      timeout: 30 * 60 * 1000
    })
  }
  if (!r.ok) {
    onLog('')
    onLog('이미지 내려받기에 실패했습니다.')
    onLog('· GHCR(ghcr.io) 접근이 캠퍼스 방화벽에 막혀 있는지 확인하세요.')
    onLog('· 비공개 이미지라면 `docker login ghcr.io` 로 먼저 로그인해야 합니다.')
  }
  return r
}

/**
 * 컨테이너 기동.
 *
 * ★ 기동 순서가 중요하다.
 *   운영 백엔드는 spring.jpa.hibernate.ddl-auto=validate 로 동작하므로,
 *   마이그레이션이 적용되지 않은 스키마에서는 **기동 자체가 실패**한다.
 *   따라서 마법사는 반드시
 *      ① db 컨테이너만 기동  ② 마이그레이션 적용  ③ 나머지 서비스 기동
 *   순서로 진행한다. services 를 주면 그 서비스만 올린다.
 *
 * db 서비스는 image 가 아니라 build 로 정의돼 있어 --build 를 함께 준다.
 */
async function up(installDir, { onLog, secretValues = [], services = [], profiles = [] } = {}) {
  const target = services.length ? services : []
  onLog(`$ docker compose -f ${COMPOSE_FILE} up -d --build ${target.join(' ')}`.trim())
  const r = await runStream('docker', composeArgs(installDir, ['up', '-d', '--build', ...target], profiles), {
    cwd: installDir,
    onLog,
    secretValues,
    timeout: 30 * 60 * 1000
  })
  return r
}

/**
 * db 컨테이너가 접속을 받을 수 있을 때까지 기다린다.
 * compose 의 healthcheck 상태를 우선 보고, 값이 없으면 mysqladmin ping 으로 확인한다.
 */
async function waitForDb(installDir, { onLog, timeoutMs = 5 * 60 * 1000 } = {}) {
  const started = Date.now()
  let lastReport = ''
  while (Date.now() - started < timeoutMs) {
    const inspect = await runCapture(
      'docker',
      ['inspect', '--format', '{{.State.Status}}|{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}', 'db'],
      { cwd: installDir, timeout: 20000 }
    )
    if (inspect.ok) {
      const [status, health] = inspect.stdout.trim().split('|')
      const report = `db 컨테이너: ${status}${health && health !== 'none' ? ` / health=${health}` : ''}`
      if (report !== lastReport) {
        onLog && onLog(report)
        lastReport = report
      }
      if (status === 'running' && (health === 'healthy' || health === 'none')) {
        if (health === 'healthy') return { ok: true, via: 'healthcheck' }
        // healthcheck 가 없는 이미지: ping 으로 직접 확인
        const ping = await runCapture('docker', ['exec', 'db', 'mysqladmin', 'ping', '--silent'], {
          cwd: installDir,
          timeout: 20000
        })
        if (ping.ok) return { ok: true, via: 'ping' }
      }
      if (status === 'exited' || status === 'dead') {
        return { ok: false, message: `db 컨테이너가 종료되었습니다(status=${status}). 로그를 확인하세요.` }
      }
    }
    await new Promise((r) => setTimeout(r, 3000))
  }
  return { ok: false, message: 'db 컨테이너가 준비되지 않았습니다(5분 초과). docker compose logs db 로 원인을 확인하세요.' }
}

async function ps(installDir, profiles = []) {
  const r = await runCapture(
    'docker',
    composeArgs(installDir, ['ps', '--format', '{{.Service}}\t{{.Name}}\t{{.State}}\t{{.Status}}'], profiles),
    { cwd: installDir, timeout: 30000 }
  )
  if (!r.ok) return { ok: false, message: (r.stderr || '').trim(), services: [] }
  const services = r.stdout
    .split('\n')
    .filter(Boolean)
    .map((l) => {
      const [service, name, state, status] = l.split('\t')
      return { service, name, state, status }
    })
  return { ok: true, services }
}

async function logs(installDir, service, tail = 120, profiles = []) {
  const r = await runCapture('docker', composeArgs(installDir, ['logs', '--tail', String(tail), service], profiles), {
    cwd: installDir,
    timeout: 30000
  })
  return { ok: r.ok, text: r.stdout || r.stderr }
}

module.exports = {
  stagePayload,
  payloadRoot,
  validateCompose,
  pull,
  up,
  waitForDb,
  profilesFor,
  ps,
  logs,
  COMPOSE_FILE,
  composeArgs
}
