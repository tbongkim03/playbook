'use strict'

const fs = require('fs')
const path = require('path')

/**
 * 9단계 후반 — 초기 도서 데이터(엑셀) 주입.
 *
 * <p>왜 SQL 직접 주입이 아니라 백엔드 API 를 쓰는가</p>
 * 도서 등록에는 중분류 이름→ID 매핑, 캠퍼스 스코프, 컬럼 길이 검증, 대여상태 초기화 같은
 * 규칙이 붙어 있고 그건 전부 백엔드에 있다. 마법사가 SQL 을 직접 만들면 그 규칙을 두 번
 * 구현하게 되고, 한쪽만 바뀌면 조용히 어긋난다. 이미 있는 `POST /api/books/import` 를 쓴다.
 *
 * <p>접속 경로</p>
 * 백엔드(8080)는 호스트에 노출되지 않는다 — nginx 게이트를 우회하는 두 번째 출입구가 되기
 * 때문에 docker-compose.prod.yml 에서 포트 매핑을 뺐다. 그래서 nginx(80) 를 경유한다.
 * 설치 PC 에서 localhost 로 붙으므로 IP 허용목록 필터는 루프백으로 항상 통과한다.
 *
 * <p>전제</p>
 * 백엔드가 이미 기동해 있어야 한다(9단계에서 서비스를 올린 뒤 호출). 캠퍼스도 그때
 * InitialCampusInitializer 가 만들어 둔 상태라, 전체관리자 계정으로 올려도 캠퍼스가
 * 하나뿐이라 백엔드가 그 캠퍼스로 등록한다.
 */

const BASE = 'http://localhost/api'
const LOGIN_TIMEOUT_MS = 15_000
const UPLOAD_TIMEOUT_MS = 180_000 // 672권 기준 수 초지만, 느린 PC 를 감안해 넉넉히

/** 백엔드가 응답할 때까지 기다린다. 기동 직후에는 아직 안 뜬 상태일 수 있다. */
async function waitForBackend({ attempts = 30, intervalMs = 4000, onLog = () => {} } = {}) {
  for (let i = 1; i <= attempts; i++) {
    try {
      const res = await fetch(`${BASE}/campus`, {
        method: 'GET',
        signal: AbortSignal.timeout(5000)
      })
      // 200 이든 4xx 든 "응답한다" 는 사실이 중요하다 (5xx·연결거부만 미기동으로 본다)
      if (res.status < 500) {
        onLog(`백엔드 응답 확인 (${i}회차)`)
        return { ok: true }
      }
    } catch {
      // 아직 안 떴다
    }
    if (i === 1 || i % 5 === 0) onLog(`백엔드 기동 대기 중… (${i}/${attempts})`)
    await new Promise((r) => setTimeout(r, intervalMs))
  }
  return {
    ok: false,
    message:
      '백엔드가 응답하지 않습니다. 서비스가 모두 기동했는지 확인한 뒤 다시 시도하세요.\n' +
      '(설치 경로에서 `docker compose -f docker-compose.prod.yml logs back` 으로 확인)'
  }
}

/** 마스터 관리자로 로그인해 세션 쿠키를 얻는다. */
async function login(master, onLog = () => {}) {
  let res
  try {
    res = await fetch(`${BASE}/admin/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      // seqCampus 는 DTO 상 primitive int 라 반드시 보내야 한다. 전체관리자는 0.
      body: JSON.stringify({ seqCampus: 0, idAdmin: master.id, pwAdmin: master.pw }),
      signal: AbortSignal.timeout(LOGIN_TIMEOUT_MS)
    })
  } catch (e) {
    return { ok: false, message: `백엔드에 접속하지 못했습니다: ${e.message}` }
  }

  if (res.status === 401) {
    return {
      ok: false,
      message:
        '마스터 관리자 로그인에 실패했습니다(401). 5단계에서 정한 계정과 실제 생성된 계정이 다를 수 있습니다.\n' +
        '백엔드를 처음 기동할 때 .env 의 MASTER_ID/MASTER_PW 로 계정이 만들어집니다.'
    }
  }
  if (!res.ok) {
    return { ok: false, message: `로그인 실패 (HTTP ${res.status})` }
  }

  const raw = res.headers.getSetCookie ? res.headers.getSetCookie() : []
  const cookie = raw.map((c) => c.split(';')[0]).join('; ')
  if (!cookie) {
    return { ok: false, message: '로그인은 됐으나 세션 쿠키를 받지 못했습니다.' }
  }
  onLog(`관리자 로그인 성공 (${master.id})`)
  return { ok: true, cookie }
}

/**
 * 엑셀 파일을 업로드한다.
 * @returns { ok, inserted, updated, skipped, errors[] }
 */
async function importBooks(filePath, master, { onLog = () => {} } = {}) {
  if (!filePath || !fs.existsSync(filePath)) {
    return { ok: false, message: '엑셀 파일을 찾을 수 없습니다.' }
  }
  const stat = fs.statSync(filePath)
  // 백엔드 multipart 상한이 10MB 다 (spring.servlet.multipart.max-file-size)
  if (stat.size > 10 * 1024 * 1024) {
    return {
      ok: false,
      message: `파일이 10MB 를 넘습니다(${(stat.size / 1024 / 1024).toFixed(1)}MB). 나눠서 올려주세요.`
    }
  }

  onLog(`파일 확인: ${path.basename(filePath)} (${(stat.size / 1024).toFixed(0)} KB)`)

  const ready = await waitForBackend({ onLog })
  if (!ready.ok) return ready

  const session = await login(master, onLog)
  if (!session.ok) return session

  onLog('도서 데이터를 업로드합니다… (권수가 많으면 1~2분 걸릴 수 있습니다)')

  let res
  try {
    const form = new FormData()
    form.append('file', new Blob([fs.readFileSync(filePath)]), path.basename(filePath))
    // allowInsert=true 는 설치 마법사 전용이다. 관리자 화면은 이 값을 보내지 않아
    // "기존 도서 갱신" 전용으로 동작한다 (빈 행이 조용히 등록돼 중복이 쌓이는 것을 막는다).
    res = await fetch(`${BASE}/books/import?allowInsert=true`, {
      method: 'POST',
      headers: { Cookie: session.cookie },
      body: form,
      signal: AbortSignal.timeout(UPLOAD_TIMEOUT_MS)
    })
  } catch (e) {
    return { ok: false, message: `업로드 중 오류가 발생했습니다: ${e.message}` }
  }

  let json
  try {
    json = await res.json()
  } catch {
    return { ok: false, message: `응답을 해석하지 못했습니다 (HTTP ${res.status})` }
  }

  if (!res.ok) {
    return { ok: false, message: json.msg || `업로드 실패 (HTTP ${res.status})` }
  }

  const d = json.data || {}
  const inserted = d.inserted || 0
  const updated = d.updated || 0
  const skipped = d.skipped || 0
  const errors = d.errors || []

  onLog(`완료 — 신규 ${inserted}권, 갱신 ${updated}권, 건너뜀 ${skipped}행`)
  if (errors.length) {
    onLog(`오류 ${errors.length}건:`)
    // 전부 쏟아내면 로그가 묻힌다. 앞쪽만 보여주고 나머지는 개수로 알린다.
    errors.slice(0, 20).forEach((e) => onLog(`  · ${e}`))
    if (errors.length > 20) onLog(`  … 외 ${errors.length - 20}건`)
  }

  return { ok: true, inserted, updated, skipped, errors }
}

module.exports = { importBooks, waitForBackend }
