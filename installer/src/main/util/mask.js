'use strict'

/**
 * 시크릿 마스킹.
 * 화면·로그·상태 파일 응답 어디에도 평문 시크릿이 나가지 않게 하는 단일 지점.
 */

/** 'abcd…wxyz' 형태로 앞 2 / 뒤 4 만 남긴다. 짧은 값은 전부 가린다. */
function maskValue(value) {
  if (value === null || value === undefined || value === '') return ''
  const s = String(value)
  if (s.length <= 8) return '•'.repeat(Math.max(s.length, 4))
  return `${s.slice(0, 2)}${'•'.repeat(6)}${s.slice(-4)}`
}

/** 로그 한 줄에서 알려진 시크릿 값들을 전부 치환 */
function scrubLine(line, secretValues) {
  let out = String(line)
  for (const v of secretValues) {
    if (!v || String(v).length < 6) continue
    // 정규식 특수문자 이스케이프
    const esc = String(v).replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
    out = out.replace(new RegExp(esc, 'g'), '••••[마스킹됨]••••')
  }
  // 흔한 key=value 형태의 시크릿도 보수적으로 한 번 더 거른다
  out = out.replace(
    /\b(PASSWORD|PASSWD|PWD|SECRET|TOKEN|API_KEY|APIKEY|CERT_KEY|AUTHKEY|MYSQL_PWD)\s*[=:]\s*("?)([^\s"']{4,})\2/gi,
    (_m, k, q) => `${k}=${q}••••[마스킹됨]${q}`
  )
  return out
}

module.exports = { maskValue, scrubLine }
