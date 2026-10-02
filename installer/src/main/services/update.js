'use strict'

const fs = require('node:fs')
const fsp = require('node:fs/promises')
const path = require('node:path')
const { spawn } = require('node:child_process')

const { request, sleep } = require('../util/http')
const { runStream } = require('./exec')
const composeSvc = require('./compose')
const migrationSvc = require('./migration')
const finishSvc = require('./finish')

/**
 * 설치 후 업데이트 — 새 버전 이미지로 교체한다.
 *
 * <p>왜 GitHub 가 밀어 넣지 않고 여기서 당겨 오는가</p>
 * 캠퍼스 서버는 내부망이라 GitHub Actions 가 SSH 로 들어올 수 없다. 그래서 캠퍼스 PC 가
 * 공개 GHCR 에서 이미지를, 공개 저장소에서 배포 파일을 직접 받아 온다. 인증 정보가 없다.
 *
 * <p>버전의 정본은 GHCR 이미지 태그다</p>
 * `v1.2.3` git 태그를 push 하면 cd.yml 이 `1.2.3` 태그로 back·front 이미지를 올린다.
 * git 태그만 있고 이미지 빌드가 아직 안 끝난 버전을 고르면 pull 에서 깨지므로,
 * **back·front 둘 다 이미지가 있는 버전**만 업데이트 대상으로 본다.
 *
 * <p>순서 — 되돌릴 수 없는 단계를 최대한 뒤로 미룬다</p>
 *   ① DB 백업  ② 이미지 pull  ③ 배포 파일 내려받기(임시 폴더)
 *   ④ 현재 파일 백업 → 새 파일 반영  ⑤ 새 마이그레이션 적용
 *   ⑥ .env 이미지 태그 교체 → 재기동  ⑦ 헬스체크 — 실패하면 이전 태그로 롤백
 * ①~③ 에서 실패하면 설치본은 전혀 바뀌지 않는다. ⑤ 는 DB 를 바꾸므로 자동 복원하지 않고
 * ① 의 백업 경로를 알려 준다 (운영 DB 를 기계가 덮어쓰는 일은 하지 않는다).
 */

const REPO = 'tbongkim03/playbook'
const IMAGES = {
  back: 'tbongkim03/playbook-back',
  front: 'tbongkim03/playbook-front'
}

/** 업데이트 때 저장소에서 새로 받아 오는 배포 파일. db/init·data 는 최초 설치용이라 제외한다. */
const SYNC_FILES = ['docker-compose.prod.yml']
const SYNC_DIRS = ['db/migration/', 'monitoring/']

/** 롤백 시 되돌리는 파일. db/migration 은 되돌리지 않는다 — 이미 DB 에 적용됐다. */
const RESTORE_ON_ROLLBACK = ['.env', 'docker-compose.prod.yml', 'monitoring']

const HEALTH_TIMEOUT_MS = 4 * 60 * 1000
const HEALTH_INTERVAL_MS = 10 * 1000

let running = false

// ── 버전 ─────────────────────────────────────────────────────────────────────

const VERSION_RE = /^(\d+)\.(\d+)\.(\d+)(?:-([0-9A-Za-z.-]+))?$/

function parseVersion(tag) {
  const m = VERSION_RE.exec(String(tag || '').trim().replace(/^v/, ''))
  if (!m) return null
  // 이 저장소 태그는 `rc7` 처럼 점 없이 붙는다. 문자·숫자 경계에서도 나눠야 rc10 > rc9 가 된다.
  const pre = m[4] ? m[4].split('.').flatMap((x) => x.match(/\d+|\D+/g) || []) : []
  return { nums: [Number(m[1]), Number(m[2]), Number(m[3])], pre }
}

/** semver 비교. 정식 > 프리릴리스(rc), rc 끼리는 조각 단위(숫자는 숫자로) 비교 */
function compareVersions(a, b) {
  const va = parseVersion(a)
  const vb = parseVersion(b)
  if (!va || !vb) throw new Error(`버전 형식이 아닙니다: ${a} / ${b}`)
  for (let i = 0; i < 3; i++) {
    if (va.nums[i] !== vb.nums[i]) return va.nums[i] - vb.nums[i]
  }
  if (!va.pre.length || !vb.pre.length) return vb.pre.length - va.pre.length
  const n = Math.max(va.pre.length, vb.pre.length)
  for (let i = 0; i < n; i++) {
    const x = va.pre[i]
    const y = vb.pre[i]
    if (x === undefined) return -1
    if (y === undefined) return 1
    const nx = /^\d+$/.test(x)
    const ny = /^\d+$/.test(y)
    if (nx && ny && Number(x) !== Number(y)) return Number(x) - Number(y)
    if (nx !== ny) return nx ? -1 : 1
    if (x !== y) return x < y ? -1 : 1
  }
  return 0
}

/** back·front 양쪽에 있는 버전 태그 중 가장 높은 것 */
function pickLatest(backTags, frontTags) {
  const front = new Set(frontTags)
  const both = backTags.filter((t) => front.has(t) && parseVersion(t))
  both.sort(compareVersions)
  return { latest: both.length ? both[both.length - 1] : null, versions: both }
}

/** 설치 경로 .env 의 BACK_IMAGE 태그. `latest`·미지정이면 null */
function readCurrentTag(envText) {
  const m = /^BACK_IMAGE=.*:([^:\s]+)\s*$/m.exec(envText || '')
  if (!m) return null
  return parseVersion(m[1]) ? m[1] : null
}

/** .env 의 BACK_IMAGE/FRONT_IMAGE 를 새 태그로 바꾼다. 줄이 없으면 끝에 붙인다. */
function withImageTag(envText, tag) {
  let text = envText || ''
  for (const [key, repo] of [
    ['BACK_IMAGE', IMAGES.back],
    ['FRONT_IMAGE', IMAGES.front]
  ]) {
    const line = `${key}=ghcr.io/${repo}:${tag}`
    const re = new RegExp(`^#?\\s*${key}=.*$`, 'm')
    if (re.test(text)) text = text.replace(re, line)
    else text = `${text.replace(/\s*$/, '')}\n${line}\n`
  }
  return text
}

// ── 원격 조회 ────────────────────────────────────────────────────────────────

async function ghcrTags(repo) {
  const tokenRes = await request(`https://ghcr.io/token?scope=repository:${repo}:pull`, { timeout: 15000 })
  const token = tokenRes.json && tokenRes.json.token
  if (!token) throw new Error(`GHCR 토큰을 받지 못했습니다 (HTTP ${tokenRes.status})`)
  const res = await request(`https://ghcr.io/v2/${repo}/tags/list?n=1000`, {
    headers: { Authorization: `Bearer ${token}` },
    timeout: 20000
  })
  if (res.status === 401 || res.status === 403 || res.status === 404) {
    throw new Error(
      `이미지 ${repo} 를 조회할 수 없습니다 (HTTP ${res.status}). GHCR 패키지가 공개(Public)인지 확인하세요.`
    )
  }
  if (!res.ok || !res.json) throw new Error(`이미지 태그 조회 실패 (HTTP ${res.status})`)
  return res.json.tags || []
}

async function githubApi(p) {
  const res = await request(`https://api.github.com/repos/${REPO}${p}`, {
    headers: { Accept: 'application/vnd.github+json', 'User-Agent': 'playbook-installer' },
    timeout: 20000
  })
  if (res.status === 403 && /rate limit/i.test(res.text || '')) {
    throw new Error('GitHub 조회 한도(시간당 60회)를 넘었습니다. 1시간 뒤 다시 시도하세요.')
  }
  if (!res.ok || !res.json) throw new Error(`GitHub 조회 실패 ${p} (HTTP ${res.status})`)
  return res.json
}

function rawUrl(tag, relPath) {
  const encoded = relPath.split('/').map(encodeURIComponent).join('/')
  return `https://raw.githubusercontent.com/${REPO}/v${tag}/${encoded}`
}

async function fetchRaw(tag, relPath) {
  const res = await fetch(rawUrl(tag, relPath), { signal: AbortSignal.timeout(30000) })
  if (!res.ok) throw new Error(`${relPath} 내려받기 실패 (HTTP ${res.status})`)
  return Buffer.from(await res.arrayBuffer())
}

/** 경로 조작 방지 — 동기화 대상 접두사 안의 정상 경로만 받는다 */
function isSyncPath(p) {
  if (!p || p.includes('..') || p.includes('\\') || p.startsWith('/')) return false
  return SYNC_FILES.includes(p) || SYNC_DIRS.some((d) => p.startsWith(d))
}

async function listSyncFiles(tag) {
  const tree = await githubApi(`/git/trees/v${encodeURIComponent(tag)}?recursive=1`)
  if (tree.truncated) throw new Error('저장소 파일 목록이 너무 커서 잘렸습니다. 관리자에게 문의하세요.')
  return (tree.tree || []).filter((e) => e.type === 'blob' && isSyncPath(e.path)).map((e) => e.path)
}

/**
 * 새 버전 백엔드가 요구하는데 back/.env.prod 에 없는 환경변수.
 * 기본값 없는 ${VAR} 가 비어 있으면 백엔드가 기동하지 못한다. 이 경우는 마법사 자체를
 * 새 버전으로 받아 설정을 다시 만들어야 한다 (이 화면은 새 키 값을 알 수 없다).
 */
async function missingEnvKeys(tag, installDir, composeText) {
  const required = new Set()
  for (const f of ['application.properties', 'application-prod.properties']) {
    let text
    try {
      text = (await fetchRaw(tag, `back/src/main/resources/${f}`)).toString('utf8')
    } catch {
      continue
    }
    for (const line of text.split(/\r?\n/)) {
      if (/^\s*#/.test(line)) continue
      for (const m of line.matchAll(/\$\{([A-Z0-9_]+)\}/g)) required.add(m[1])
    }
  }
  let envText = ''
  try {
    envText = await fsp.readFile(path.join(installDir, 'back', '.env.prod'), 'utf8')
  } catch {
    /* 없으면 전부 누락으로 본다 */
  }
  const have = new Set([...envText.matchAll(/^([A-Z0-9_]+)=/gm)].map((m) => m[1]))
  return [...required].filter((k) => !have.has(k) && !new RegExp(`\\b${k}\\b`).test(composeText || '')).sort()
}

// ── 확인 ─────────────────────────────────────────────────────────────────────

async function check(installDir) {
  if (!installDir || !fs.existsSync(path.join(installDir, composeSvc.COMPOSE_FILE))) {
    return { ok: false, message: '설치 경로에서 docker-compose.prod.yml 을 찾지 못했습니다. 설치를 먼저 완료하세요.' }
  }
  let envText = ''
  try {
    envText = await fsp.readFile(path.join(installDir, '.env'), 'utf8')
  } catch {
    /* 태그 미지정 설치본 */
  }
  const current = readCurrentTag(envText)

  const [backTags, frontTags] = await Promise.all([ghcrTags(IMAGES.back), ghcrTags(IMAGES.front)])
  const { latest, versions } = pickLatest(backTags, frontTags)
  if (!latest) {
    return { ok: false, message: '배포된 버전 이미지가 없습니다. v1.2.3 형식의 태그로 이미지를 먼저 빌드하세요.' }
  }

  const upToDate = !!current && compareVersions(latest, current) <= 0
  const result = {
    ok: true,
    checkedAt: new Date().toISOString(),
    current,
    latest,
    versions: versions.slice(-10).reverse(),
    upToDate,
    commits: [],
    newMigrations: [],
    missingEnv: []
  }
  if (upToDate) return result

  const files = await listSyncFiles(latest)
  const localMig = new Set(
    fs.existsSync(path.join(installDir, 'db', 'migration'))
      ? await fsp.readdir(path.join(installDir, 'db', 'migration'))
      : []
  )
  const newMigNames = files
    .filter((f) => f.startsWith('db/migration/') && f.toLowerCase().endsWith('.sql'))
    .map((f) => f.slice('db/migration/'.length))
    .filter((n) => !n.includes('/') && !localMig.has(n))
    .sort()

  for (const name of newMigNames) {
    const sql = (await fetchRaw(latest, `db/migration/${name}`)).toString('utf8')
    const parsed = migrationSvc.parseFile(name, sql)
    result.newMigrations.push({
      file: name,
      tables: parsed.groups.map((g) => g.label),
      statementCount: parsed.groups.reduce((a, g) => a + g.statementCount, 0),
      excluded: parsed.excluded
    })
  }

  const composeText = (await fetchRaw(latest, composeSvc.COMPOSE_FILE)).toString('utf8')
  result.missingEnv = await missingEnvKeys(latest, installDir, composeText)

  if (current) {
    try {
      const cmp = await githubApi(`/compare/v${encodeURIComponent(current)}...v${encodeURIComponent(latest)}`)
      result.commits = (cmp.commits || [])
        .map((c) => String(c.commit && c.commit.message).split('\n')[0])
        .reverse()
        .slice(0, 40)
    } catch {
      // 변경 내역은 참고용이다 — 못 받아도 업데이트는 가능하다
    }
  }
  return result
}

// ── 실행 ─────────────────────────────────────────────────────────────────────

/** mysqldump 를 파일로 바로 흘린다 (문자열로 모으면 멀티바이트가 청크 경계에서 깨진다) */
function dumpDatabase(installDir, dbEnv, rootPassword, outFile) {
  return new Promise((resolve) => {
    const out = fs.createWriteStream(outFile, { mode: 0o600 })
    const child = spawn(
      'docker',
      [
        'exec', '-e', `MYSQL_PWD=${rootPassword}`, 'db',
        'mysqldump', '-uroot', '--single-transaction', '--routines', '--triggers',
        '--default-character-set=utf8mb4', '--databases', dbEnv.database
      ],
      { cwd: installDir, windowsHide: true, shell: false }
    )
    let err = ''
    child.stdout.pipe(out)
    child.stderr.on('data', (d) => (err = (err + d.toString()).slice(-2000)))
    child.on('error', (e) => resolve({ ok: false, message: e.message }))
    child.on('close', (code) => {
      out.end(() => resolve(code === 0 ? { ok: true } : { ok: false, message: err.trim() || `종료 코드 ${code}` }))
    })
  })
}

async function readRootPassword(installDir) {
  const text = await fsp.readFile(path.join(installDir, 'db', '.env.prod'), 'utf8')
  const m = /^MYSQL_ROOT_PASSWORD=(.*)$/m.exec(text)
  return m ? m[1].trim() : ''
}

async function waitHealthy(installDir, onLog) {
  const started = Date.now()
  let last = null
  while (Date.now() - started < HEALTH_TIMEOUT_MS) {
    await sleep(HEALTH_INTERVAL_MS)
    last = await finishSvc.healthCheck(installDir)
    const back = last.checks.find((c) => c.id === 'back')
    const front = last.checks.find((c) => c.id === 'front')
    onLog(`헬스체크: 백엔드 ${back ? back.status : '?'} / 프론트 ${front ? front.status : '?'}`)
    if (last.healthy && back && back.status === 'ok') return { ok: true, result: last }
  }
  return { ok: false, result: last }
}

async function copyIfExists(from, to) {
  if (!fs.existsSync(from)) return false
  await fsp.mkdir(path.dirname(to), { recursive: true })
  await fsp.cp(from, to, { recursive: true, force: true })
  return true
}

/**
 * @returns { ok, stage, message, backupDir, dbBackup, migrationResults, rolledBack }
 */
async function run(installDir, targetTag, { onLog = () => {}, secretValues = [], profiles = [] } = {}) {
  if (running) return { ok: false, message: '이미 업데이트가 진행 중입니다.' }
  running = true
  const out = { ok: false, targetTag, migrationResults: [], rolledBack: false }
  try {
    if (!parseVersion(targetTag)) return { ...out, message: `버전 형식이 아닙니다: ${targetTag}` }

    // 확인 화면 이후 상황이 바뀌었을 수 있다 — 대상 버전 이미지를 다시 확인한다
    onLog(`대상 버전 ${targetTag} 확인 중…`)
    const [backTags, frontTags] = await Promise.all([ghcrTags(IMAGES.back), ghcrTags(IMAGES.front)])
    if (!backTags.includes(targetTag) || !frontTags.includes(targetTag)) {
      return { ...out, stage: 'check', message: `${targetTag} 이미지가 GHCR 에 없습니다.` }
    }

    const stamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19)
    const backupDir = path.join(installDir, 'backups', `update-${stamp}-to-${targetTag}`)
    await fsp.mkdir(backupDir, { recursive: true })
    out.backupDir = backupDir

    // ① DB 백업
    onLog('① DB 백업 중…')
    const dbEnv = await migrationSvc.readDbEnv(installDir)
    if (!dbEnv.ok) return { ...out, stage: 'backup', message: dbEnv.message }
    const rootPw = await readRootPassword(installDir)
    if (!rootPw) return { ...out, stage: 'backup', message: 'db/.env.prod 에서 MYSQL_ROOT_PASSWORD 를 읽지 못했습니다.' }
    const dbBackup = path.join(backupDir, `${dbEnv.database}.sql`)
    const dumped = await dumpDatabase(installDir, dbEnv, rootPw, dbBackup)
    if (!dumped.ok) return { ...out, stage: 'backup', message: `DB 백업 실패 — 업데이트를 중단합니다: ${dumped.message}` }
    out.dbBackup = dbBackup
    onLog(`✔ DB 백업: ${dbBackup} (${Math.round(fs.statSync(dbBackup).size / 1024)} KB)`)

    // ② 이미지 pull — 실패해도 아직 아무것도 바뀌지 않았다
    onLog('② 새 이미지 내려받는 중…')
    for (const repo of [IMAGES.back, IMAGES.front]) {
      const image = `ghcr.io/${repo}:${targetTag}`
      onLog(`$ docker pull ${image}`)
      const r = await runStream('docker', ['pull', image], { cwd: installDir, onLog, secretValues, timeout: 30 * 60 * 1000 })
      if (!r.ok) return { ...out, stage: 'pull', message: `이미지 내려받기 실패: ${image}` }
    }

    // ③ 배포 파일 — 임시 폴더에 전부 받은 뒤에만 반영한다
    onLog('③ 배포 파일 내려받는 중…')
    const staging = path.join(installDir, '.update-staging', targetTag)
    await fsp.rm(staging, { recursive: true, force: true })
    const files = await listSyncFiles(targetTag)
    for (const rel of files) {
      const dest = path.join(staging, ...rel.split('/'))
      await fsp.mkdir(path.dirname(dest), { recursive: true })
      await fsp.writeFile(dest, await fetchRaw(targetTag, rel))
    }
    onLog(`✔ ${files.length}개 파일`)

    // ④ 현재 파일 백업 → 반영
    onLog('④ 현재 설정 백업 후 새 배포 파일 반영…')
    const filesBackup = path.join(backupDir, 'files')
    for (const rel of [...RESTORE_ON_ROLLBACK, 'db/migration']) {
      await copyIfExists(path.join(installDir, rel), path.join(filesBackup, rel))
    }
    const localMigDir = path.join(installDir, 'db', 'migration')
    const before = new Set(fs.existsSync(localMigDir) ? await fsp.readdir(localMigDir) : [])
    await fsp.cp(staging, installDir, { recursive: true, force: true })
    await fsp.rm(path.join(installDir, '.update-staging'), { recursive: true, force: true })
    const newMigs = (await fsp.readdir(localMigDir)).filter((f) => f.toLowerCase().endsWith('.sql') && !before.has(f)).sort()

    const restoreFiles = async () => {
      for (const rel of RESTORE_ON_ROLLBACK) {
        await copyIfExists(path.join(filesBackup, rel), path.join(installDir, rel))
      }
    }

    // ⑤ 새 마이그레이션 — 이번 업데이트로 처음 들어온 파일만. 파괴적 문장은 parseFile 이 이미 뺐다.
    if (newMigs.length) {
      onLog(`⑤ 새 마이그레이션 ${newMigs.length}개 적용: ${newMigs.join(', ')}`)
      const selections = []
      for (const f of newMigs) {
        const parsed = migrationSvc.parseFile(f, await fsp.readFile(path.join(localMigDir, f), 'utf8'))
        selections.push({ file: f, groupKeys: parsed.groups.map((g) => g.key) })
      }
      const applied = await migrationSvc.apply(installDir, selections, { onLog, secretValues })
      out.migrationResults = applied.results
      if (!applied.ok) {
        await restoreFiles()
        return {
          ...out,
          stage: 'migration',
          message:
            '마이그레이션 적용에 실패해 업데이트를 중단했습니다. 서비스는 이전 버전 그대로입니다.\n' +
            `DB 가 일부 바뀌었을 수 있습니다. 백업: ${dbBackup}`
        }
      }
    } else {
      onLog('⑤ 새 마이그레이션 없음')
    }

    // ⑥ 태그 교체 → 재기동
    onLog(`⑥ 이미지 태그를 ${targetTag} 로 바꾸고 재기동…`)
    const envPath = path.join(installDir, '.env')
    const envText = fs.existsSync(envPath) ? await fsp.readFile(envPath, 'utf8') : ''
    await fsp.writeFile(envPath, withImageTag(envText, targetTag), 'utf8')
    const up = await composeSvc.up(installDir, { onLog, secretValues, profiles })

    // ⑦ 헬스체크 → 실패 시 롤백
    const health = up.ok ? await waitHealthy(installDir, onLog) : { ok: false }
    if (health.ok) {
      onLog(`✔ ${targetTag} 업데이트 완료`)
      return { ...out, ok: true, stage: 'done', health: health.result }
    }

    onLog('✘ 새 버전이 정상 기동하지 않았습니다. 이전 버전으로 되돌립니다…', 'stderr')
    const backLogs = await composeSvc.logs(installDir, 'back', 40, profiles)
    for (const l of String(backLogs.text || '').split('\n').filter(Boolean)) onLog(l, 'stderr')
    await restoreFiles()
    const reup = await composeSvc.up(installDir, { onLog, secretValues, profiles })
    const reHealth = reup.ok ? await waitHealthy(installDir, onLog) : { ok: false }
    return {
      ...out,
      stage: 'rollback',
      rolledBack: true,
      rollbackHealthy: reHealth.ok,
      message: reHealth.ok
        ? '새 버전이 기동하지 않아 이전 버전으로 되돌렸습니다. 위 백엔드 로그를 확인하세요.'
        : `이전 버전으로 되돌렸지만 아직 정상 응답이 없습니다. DB 백업: ${dbBackup}`
    }
  } catch (e) {
    return { ...out, message: e && e.message ? e.message : String(e) }
  } finally {
    running = false
  }
}

module.exports = {
  check,
  run,
  parseVersion,
  compareVersions,
  pickLatest,
  readCurrentTag,
  withImageTag,
  isSyncPath
}
