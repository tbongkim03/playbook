#!/usr/bin/env node
/**
 * 저장소 루트의 배포 자산을 installer/payload 로 복사한다.
 * 마법사 .exe 에 이 페이로드가 동봉되고, 설치 시 사용자가 고른 설치 경로로 풀린다.
 *
 * 저장소 파일은 읽기만 한다 (원본 수정 없음).
 *
 * ★ 경로 해석은 전부 `import.meta.url` 기준이다 (cwd 무관).
 *   → 로컬에서 `cd installer && npm run build` 로 돌리든,
 *     CI 에서 `working-directory: installer` 로 돌리든 같은 결과가 나온다.
 *
 * ★ 복사 후 검증한다.
 *   docker-compose.prod.yml 이 참조하는 상대경로(bind mount·build context)가 페이로드에
 *   실제로 들어갔는지 대조하고, 하나라도 빠지면 **빌드를 실패시킨다.**
 *   이걸 안 하면 "빈 payload 가 든 exe" 가 만들어지고, 그 사실은 캠퍼스에서 설치할 때에야
 *   드러난다. 특히 아직 커밋되지 않은 디렉터리(monitoring/ 등)는 CI 체크아웃에 존재하지
 *   않으므로 로컬에서는 멀쩡하고 CI 산출물만 깨진다.
 */
import { cp, mkdir, rm, stat, writeFile, readFile, readdir } from 'node:fs/promises'
import { existsSync } from 'node:fs'
import { execFileSync } from 'node:child_process'
import { createHash } from 'node:crypto'
import { inflateRawSync } from 'node:zlib'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const here = path.dirname(fileURLToPath(import.meta.url))
const installerRoot = path.resolve(here, '..')
const repoRoot = path.resolve(installerRoot, '..')
const payloadDir = path.join(installerRoot, 'payload')

const COMPOSE = 'docker-compose.prod.yml'

/** [저장소 상대경로, 필수여부] */
const ITEMS = [
  [COMPOSE, true],
  ['db/Dockerfile', true],
  ['db/init', true],
  ['db/data', true],
  ['db/conf', true],
  ['db/migration', true],
  ['monitoring', false], // compose 참조 검증에서 다시 확인한다
  ['front/nginx-prod.conf', false] // 이미지에 이미 포함. 참고용 동봉
]

const EXCLUDE_BASENAMES = new Set(['.env.prod', '.env.dev', '.env.test', '.env.monitoring', '.env'])

const problems = []

/**
 * Windows 네이티브 접속 프록시(Caddy) — 검증한 버전으로 고정한다. 자동 최신화하지 않는다.
 * 버전을 올릴 때는 공식 caddy_<ver>_checksums.txt 의 windows_amd64.zip SHA-512 로 함께 바꾼다.
 */
const CADDY = {
  version: '2.11.4',
  url: 'https://github.com/caddyserver/caddy/releases/download/v2.11.4/caddy_2.11.4_windows_amd64.zip',
  sha512:
    'cd5ccfd86a4b40732cf715890d0dca5bf3f63adefec5a7914de85adf240c60ce7e5d2791631b88ef9758e46b23bb1730e020b9c5d696889740b284ffd4788e35'
}
const cacheDir = path.join(installerRoot, '.cache')

/** zip 에서 파일 하나를 꺼낸다 (중앙 디렉터리 기준, stored/deflate 만) */
function unzipEntry(zip, name) {
  let eocd = -1
  for (let i = zip.length - 22; i >= Math.max(0, zip.length - 65557); i--) {
    if (zip.readUInt32LE(i) === 0x06054b50) {
      eocd = i
      break
    }
  }
  if (eocd < 0) throw new Error('zip 끝 레코드를 찾지 못했습니다')
  const count = zip.readUInt16LE(eocd + 10)
  let p = zip.readUInt32LE(eocd + 16)
  for (let n = 0; n < count; n++) {
    if (zip.readUInt32LE(p) !== 0x02014b50) throw new Error('zip 중앙 디렉터리가 손상되었습니다')
    const method = zip.readUInt16LE(p + 10)
    const compSize = zip.readUInt32LE(p + 20)
    const nameLen = zip.readUInt16LE(p + 28)
    const extraLen = zip.readUInt16LE(p + 30)
    const commentLen = zip.readUInt16LE(p + 32)
    const localOff = zip.readUInt32LE(p + 42)
    const entryName = zip.toString('utf8', p + 46, p + 46 + nameLen)
    if (entryName === name) {
      const lNameLen = zip.readUInt16LE(localOff + 26)
      const lExtraLen = zip.readUInt16LE(localOff + 28)
      const data = zip.subarray(localOff + 30 + lNameLen + lExtraLen, localOff + 30 + lNameLen + lExtraLen + compSize)
      if (method === 0) return Buffer.from(data)
      if (method === 8) return inflateRawSync(data)
      throw new Error(`지원하지 않는 압축 방식: ${method}`)
    }
    p += 46 + nameLen + extraLen + commentLen
  }
  throw new Error(`zip 에 ${name} 이(가) 없습니다`)
}

/** caddy.exe 를 payload/proxy 에 넣는다. 받은 zip 은 .cache 에 두고 체크섬이 맞을 때만 쓴다. */
async function stageCaddy() {
  await mkdir(cacheDir, { recursive: true })
  const zipPath = path.join(cacheDir, path.basename(CADDY.url))
  const sha = (buf) => createHash('sha512').update(buf).digest('hex')
  let zip = existsSync(zipPath) ? await readFile(zipPath) : null
  if (!zip || sha(zip) !== CADDY.sha512) {
    console.log(`[prepare-payload] Caddy ${CADDY.version} 내려받는 중…`)
    const res = await fetch(CADDY.url)
    if (!res.ok) throw new Error(`Caddy 내려받기 실패 (HTTP ${res.status})`)
    zip = Buffer.from(await res.arrayBuffer())
    if (sha(zip) !== CADDY.sha512) {
      throw new Error('Caddy zip 의 SHA-512 가 고정값과 다릅니다. 변조되었거나 버전이 바뀌었습니다')
    }
    await writeFile(zipPath, zip)
  }
  const dest = path.join(payloadDir, 'proxy')
  await mkdir(dest, { recursive: true })
  await writeFile(path.join(dest, 'caddy.exe'), unzipEntry(zip, 'caddy.exe'))
  await writeFile(path.join(dest, 'CADDY-LICENSE.txt'), unzipEntry(zip, 'LICENSE'))
  return CADDY.version
}

/**
 * 설치 시 받을 컨테이너 이미지 태그.
 * cd.yml 은 `latest` 를 main push 때만 붙인다 — 태그(v*) 빌드 이미지는 `0.2.0-rc3` 처럼
 * 버전 태그만 있다. 마법사가 `:latest` 를 받으려 하면 없는 이미지라 pull 이 실패한다.
 *   ① PLAYBOOK_IMAGE_TAG 환경변수 (CI 태그 빌드가 넣어 준다. 앞의 v 는 떼어 낸다)
 *   ② 현재 커밋에 정확히 걸린 v* git 태그
 *   ③ 없으면 null → compose 기본값(latest)
 */
function resolveImageTag() {
  const strip = (t) => String(t).trim().replace(/^v/, '')
  if (process.env.PLAYBOOK_IMAGE_TAG && process.env.PLAYBOOK_IMAGE_TAG.trim()) {
    return { tag: strip(process.env.PLAYBOOK_IMAGE_TAG), source: 'PLAYBOOK_IMAGE_TAG' }
  }
  try {
    const t = execFileSync('git', ['describe', '--tags', '--exact-match', '--match', 'v*'], {
      cwd: repoRoot,
      stdio: ['ignore', 'pipe', 'ignore']
    }).toString()
    if (t.trim()) return { tag: strip(t), source: 'git tag' }
  } catch {
    /* 태그 없는 커밋이거나 git 없음 (C:\ 로 복사한 빌드 폴더 등) */
  }
  return { tag: null, source: null }
}

/** CI(GitHub Actions)에서는 주석(annotation)으로도 남긴다 */
const isCI = !!process.env.GITHUB_ACTIONS
const annotate = (level, msg) => {
  if (isCI) console.log(`::${level}::${msg}`)
}

/**
 * compose 파일이 참조하는 **상대경로 호스트 자산**을 뽑는다.
 *   volumes:  - ./monitoring/alloy/config.alloy:/etc/alloy/config.alloy:ro
 *   build:      context: ./db
 *   env_file: - ./back/.env.prod   ← 설치 시점에 마법사가 만드는 파일이므로 검증 제외
 */
function extractComposeRefs(text) {
  const refs = new Set()

  // bind mount: "- ./경로:/컨테이너경로[:옵션]"
  for (const m of text.matchAll(/^\s*-\s+(\.\/[^\s:]+):/gm)) refs.add(m[1])
  // build context
  for (const m of text.matchAll(/^\s*context:\s*(\.\/[^\s#]+)\s*$/gm)) refs.add(m[1].trim())

  return [...refs]
    .map((r) => r.replace(/^\.\//, ''))
    // .env* 는 페이로드에 있으면 오히려 사고다 (개발자 PC 시크릿 유출)
    .filter((r) => !path.basename(r).startsWith('.env'))
}

async function listSql(dir) {
  if (!existsSync(dir)) return []
  return (await readdir(dir)).filter((f) => f.toLowerCase().endsWith('.sql')).sort()
}

async function main() {
  await rm(payloadDir, { recursive: true, force: true })
  await mkdir(payloadDir, { recursive: true })

  const copied = []
  const missingOptional = []

  for (const [rel, required] of ITEMS) {
    const src = path.join(repoRoot, rel)
    if (!existsSync(src)) {
      if (required) problems.push(`필수 자산 누락: ${rel}`)
      else missingOptional.push(rel)
      continue
    }
    const dest = path.join(payloadDir, rel)
    await mkdir(path.dirname(dest), { recursive: true })
    const s = await stat(src)
    if (s.isDirectory()) {
      await cp(src, dest, {
        recursive: true,
        filter: (from) => !EXCLUDE_BASENAMES.has(path.basename(from))
      })
    } else {
      await cp(src, dest)
    }
    copied.push(rel)
  }

  // ── 검증 1: compose 가 참조하는 상대경로가 페이로드에 있는가 ──────────────
  let composeRefs = []
  const composeSrc = path.join(repoRoot, COMPOSE)
  if (existsSync(composeSrc)) {
    composeRefs = extractComposeRefs(await readFile(composeSrc, 'utf8'))
    for (const ref of composeRefs) {
      if (existsSync(path.join(payloadDir, ref))) continue
      const inRepo = existsSync(path.join(repoRoot, ref))
      problems.push(
        inRepo
          ? `${COMPOSE} 가 참조하는 "${ref}" 가 페이로드에 담기지 않았습니다. ` +
            'prepare-payload.mjs 의 ITEMS 에 추가하세요.'
          : `${COMPOSE} 가 참조하는 "${ref}" 가 저장소에 없습니다. ` +
            '아직 커밋되지 않은 파일이라면 CI 체크아웃에는 존재하지 않습니다 — 먼저 커밋하세요.'
      )
    }
  }

  // ── 검증 2: 마이그레이션 SQL 이 빠짐없이 담겼는가 ─────────────────────────
  const payloadSql = await listSql(path.join(payloadDir, 'db/migration'))
  const repoSql = await listSql(path.join(repoRoot, 'db/migration'))
  if (payloadSql.length === 0) {
    problems.push('db/migration 에 SQL 파일이 하나도 없습니다. 9단계 마이그레이션이 동작할 수 없습니다.')
  } else if (payloadSql.length !== repoSql.length) {
    problems.push(`마이그레이션 SQL 개수 불일치: 저장소 ${repoSql.length}개 / 페이로드 ${payloadSql.length}개`)
  }

  // ── 검증 3: 시크릿(.env*)이 섞여 들어가지 않았는가 ────────────────────────
  const leaked = []
  const walk = async (dir, rel = '') => {
    for (const e of await readdir(dir, { withFileTypes: true })) {
      const p = path.join(dir, e.name)
      const r = rel ? `${rel}/${e.name}` : e.name
      if (e.isDirectory()) await walk(p, r)
      else if (e.name.startsWith('.env')) leaked.push(r)
    }
  }
  await walk(payloadDir)
  if (leaked.length) {
    problems.push(`페이로드에 .env 파일이 섞였습니다(시크릿 유출 위험): ${leaked.join(', ')}`)
  }

  const image = resolveImageTag()

  // ── 네이티브 프록시 ───────────────────────────────────────────────────────
  let caddyVersion = null
  try {
    caddyVersion = await stageCaddy()
  } catch (e) {
    problems.push(`Caddy 동봉 실패: ${e.message}`)
  }

  await writeFile(
    path.join(payloadDir, 'PAYLOAD.json'),
    JSON.stringify(
      {
        generatedAt: new Date().toISOString(),
        imageTag: image.tag,
        caddyVersion,
        repoRoot,
        copied,
        missingOptional,
        composeRefs,
        migrationSql: payloadSql,
        problems
      },
      null,
      2
    ),
    'utf8'
  )

  // ── 보고 ──────────────────────────────────────────────────────────────────
  console.log(`[prepare-payload] 저장소 루트: ${repoRoot}`)
  console.log(`[prepare-payload] 복사 완료 (${copied.length}건): ${copied.join(', ')}`)
  console.log(`[prepare-payload] 마이그레이션 SQL ${payloadSql.length}개: ${payloadSql.join(', ') || '(없음)'}`)
  console.log(`[prepare-payload] compose 참조 자산: ${composeRefs.join(', ') || '(없음)'}`)
  if (image.tag) {
    console.log(`[prepare-payload] 이미지 태그: ${image.tag} (${image.source})`)
  } else {
    const w = '이미지 태그 미지정 → 설치 시 :latest 를 받습니다. 태그 빌드가 아니면 PLAYBOOK_IMAGE_TAG 를 지정하세요.'
    console.warn(`[prepare-payload] ${w}`)
    annotate('warning', `[payload] ${w}`)
  }

  for (const w of missingOptional) {
    // compose 가 참조하는 자산이면 위 검증 1 에서 problems 로 갔다. 여기 남는 건 순수 참고용.
    console.warn(`[prepare-payload] 선택 자산 누락(무시): ${w}`)
    annotate('warning', `[payload] 선택 자산 누락: ${w}`)
  }

  if (problems.length) {
    console.error('\n[prepare-payload] 페이로드 검증 실패:')
    for (const p of problems) {
      console.error(`  ✗ ${p}`)
      annotate('error', `[payload] ${p}`)
    }
    console.error('\n이 상태로 만든 설치 파일은 배포 자산이 빠져 있어, 캠퍼스 설치 시점에야 문제가 드러납니다.')
    process.exit(1)
  }

  console.log('[prepare-payload] 검증 통과')
}

main().catch((e) => {
  console.error('[prepare-payload] 실패:', e)
  process.exit(1)
})
