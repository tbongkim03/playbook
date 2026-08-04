'use strict'

/**
 * 메인 프로세스 전용 HTTP 헬퍼.
 * 렌더러는 절대 외부 API 를 직접 호출하지 않는다 — 시크릿이 렌더러에 상주하지 않게 하기 위함.
 */

const DEFAULT_TIMEOUT = 15000

async function request(url, { method = 'GET', headers = {}, body, timeout = DEFAULT_TIMEOUT } = {}) {
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), timeout)
  try {
    const res = await fetch(url, {
      method,
      headers,
      body,
      signal: controller.signal,
      redirect: 'follow'
    })
    const text = await res.text()
    let json = null
    try {
      json = text ? JSON.parse(text) : null
    } catch {
      json = null
    }
    return { ok: res.ok, status: res.status, headers: res.headers, text, json }
  } finally {
    clearTimeout(timer)
  }
}

/** 네트워크 예외를 한국어 사유로 번역 */
function describeNetworkError(err) {
  const name = err && (err.name || '')
  const code = (err && (err.cause?.code || err.code)) || ''
  if (name === 'AbortError') return '응답 시간 초과(15초). 캠퍼스 방화벽·프록시가 외부 통신을 막고 있는지 확인하세요.'
  if (code === 'ENOTFOUND' || code === 'EAI_AGAIN') return 'DNS 조회 실패. 인터넷 연결 또는 DNS 설정을 확인하세요.'
  if (code === 'ECONNREFUSED') return '연결이 거부되었습니다. 방화벽 또는 프록시 설정을 확인하세요.'
  if (code === 'CERT_HAS_EXPIRED' || String(code).startsWith('UNABLE_TO_VERIFY')) {
    return 'TLS 인증서 검증 실패. 사내 프록시가 SSL 을 가로채고 있을 수 있습니다.'
  }
  return `네트워크 오류: ${err && err.message ? err.message : String(err)}`
}

function sleep(ms) {
  return new Promise((r) => setTimeout(r, ms))
}

module.exports = { request, describeNetworkError, sleep, DEFAULT_TIMEOUT }
