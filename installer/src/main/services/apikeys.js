'use strict'

const { request, describeNetworkError } = require('../util/http')

/**
 * 3단계 — 외부 API 키 실검증.
 *
 * 호출 URL·헤더는 백엔드 IntegrationController 의 testNaver/testNl/testWork24 와
 * Work24CourseClient 를 그대로 옮긴 것이다. 마법사에서 통과한 키는 서버에서도 통과한다.
 *   - Naver : IntegrationController#testNaver
 *   - NL    : IntegrationController#testNl
 *   - Work24: Work24CourseClient#fetchRaw (URL_TEMPLATE)
 */

const ISSUE_URLS = {
  naver: 'https://developers.naver.com/apps/#/register',
  nl: 'https://www.nl.go.kr/NL/contents/N31101030700.do',
  work24: 'https://www.work24.go.kr/cm/z/b/openApiMain.do'
}

function ymd(d) {
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}${p(d.getMonth() + 1)}${p(d.getDate())}`
}

/** 네이버 책 검색 API — X-Naver-Client-Id / X-Naver-Client-Secret */
async function verifyNaver({ clientId, clientSecret }) {
  if (!clientId || !clientSecret) {
    return { ok: false, message: 'Client ID 와 Client Secret 을 모두 입력하세요.' }
  }
  const url = 'https://openapi.naver.com/v1/search/book.json?query=%EC%9E%90%EB%B0%94&display=1'
  try {
    const res = await request(url, {
      headers: {
        'X-Naver-Client-Id': clientId,
        'X-Naver-Client-Secret': clientSecret,
        Accept: 'application/json'
      }
    })
    if (res.ok) {
      const total = res.json && typeof res.json.total === 'number' ? res.json.total : null
      return {
        ok: true,
        message: `네이버 책 검색 API 호출 성공${total !== null ? ` (검색 결과 ${total}건)` : ''}`
      }
    }
    if (res.status === 401) {
      return { ok: false, message: '인증 실패(401). Client ID 또는 Secret 이 올바르지 않습니다.' }
    }
    if (res.status === 403) {
      return {
        ok: false,
        message: '권한 없음(403). 네이버 개발자 센터에서 이 애플리케이션에 "검색" API 사용을 추가했는지 확인하세요.'
      }
    }
    if (res.status === 429) {
      return { ok: false, message: '호출 한도 초과(429). 잠시 후 다시 시도하세요.' }
    }
    const detail = res.json && res.json.errorMessage ? ` - ${res.json.errorMessage}` : ''
    return { ok: false, message: `네이버 API 응답 오류: HTTP ${res.status}${detail}` }
  } catch (e) {
    return { ok: false, message: `네이버 API 호출 실패: ${describeNetworkError(e)}` }
  }
}

/** 국립중앙도서관 ISBN(서지) API — 표본 ISBN 9788966261208 로 실호출 */
async function verifyNl({ apiKey }) {
  if (!apiKey) return { ok: false, message: '인증키를 입력하세요.' }
  const url =
    `https://www.nl.go.kr/seoji/SearchApi.do?cert_key=${encodeURIComponent(apiKey)}` +
    '&result_style=json&page_no=1&page_size=1&isbn=9788966261208'
  try {
    const res = await request(url, {
      headers: {
        Accept: 'application/json',
        'User-Agent': 'Mozilla/5.0 (compatible; BookManager/1.0)'
      }
    })
    if (!res.ok) {
      return { ok: false, message: `국립중앙도서관 API 응답 오류: HTTP ${res.status}` }
    }
    if (!res.text || !res.text.trim()) {
      return { ok: false, message: '응답 본문이 비어 있습니다. 인증키를 다시 확인하세요.' }
    }
    // 이 API 는 키가 틀려도 200 을 주고 본문에 에러를 담는 경우가 있다
    const body = res.text
    if (/인증키|cert_key|ERROR|오류/i.test(body) && !/TOTAL_COUNT|docs/i.test(body)) {
      return {
        ok: false,
        message: '인증키가 거부되었습니다. 국립중앙도서관에서 발급받은 서지정보 API 인증키인지 확인하세요.'
      }
    }
    const total = res.json && res.json.TOTAL_COUNT !== undefined ? res.json.TOTAL_COUNT : null
    return {
      ok: true,
      message: `국립중앙도서관 ISBN API 호출 성공${total !== null ? ` (표본 조회 ${total}건)` : ''}`
    }
  } catch (e) {
    return { ok: false, message: `국립중앙도서관 API 호출 실패: ${describeNetworkError(e)}` }
  }
}

/** Work24 훈련과정 API — Work24CourseClient 의 URL 템플릿과 동일 */
async function verifyWork24({ apiKey }) {
  if (!apiKey) return { ok: false, message: 'API 키를 입력하세요.' }
  const today = new Date()
  const sixMonthsAgo = new Date(today)
  sixMonthsAgo.setMonth(sixMonthsAgo.getMonth() - 6)

  const url =
    'https://www.work24.go.kr/cm/openApi/call/hr/callOpenApiSvcInfo310L01.do' +
    `?authKey=${encodeURIComponent(apiKey)}&returnType=JSON&outType=1&pageNum=1&pageSize=100` +
    `&srchTraStDt=${ymd(sixMonthsAgo)}&srchTraEndDt=${ymd(today)}` +
    '&srchTraArea1=11&srchNcs1=20&crseTracseSe=C0104&srchTraGbn=M1001' +
    `&srchTraOrganNm=${encodeURIComponent('플레이데이터평생교육원')}&sort=ASC&sortCol=2`

  try {
    const res = await request(url, { headers: { Accept: 'application/json' }, timeout: 25000 })
    if (!res.ok) {
      return { ok: false, message: `Work24 API 응답 오류: HTTP ${res.status}` }
    }
    if (!res.json) {
      // 키가 틀리면 JSON 대신 에러 XML/HTML 이 돌아온다
      const head = (res.text || '').replace(/\s+/g, ' ').slice(0, 120)
      return { ok: false, message: `Work24 응답이 JSON 이 아닙니다(키 오류 가능): ${head}` }
    }
    const list = Array.isArray(res.json.srchList) ? res.json.srchList : null
    if (list === null) {
      const msg = res.json.message || res.json.errMsg || JSON.stringify(res.json).slice(0, 160)
      return { ok: false, message: `Work24 호출 실패: ${msg}` }
    }
    return {
      ok: true,
      message: `Work24 호출 성공 - 과정 ${list.length}건 수신` + (list.length === 0 ? ' (최근 6개월 개설 과정 없음 — 키는 정상)' : '')
    }
  } catch (e) {
    return { ok: false, message: `Work24 호출 실패: ${describeNetworkError(e)}` }
  }
}

module.exports = { verifyNaver, verifyNl, verifyWork24, ISSUE_URLS }
