'use strict'
const crypto = require('node:crypto')

// .env / MySQL / docker compose 어디서도 이스케이프가 필요 없는 문자만 쓴다.
// (#, $, ", ', \, 공백, = 을 배제 — env_file 파싱과 셸 인용 사고를 원천 차단)
const ALPHABET = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'

/** 모듈로 편향 없는 암호학적 난수 문자열 */
function randomString(length, alphabet = ALPHABET) {
  const out = []
  const max = 256 - (256 % alphabet.length)
  while (out.length < length) {
    const buf = crypto.randomBytes(length * 2)
    for (const b of buf) {
      if (b >= max) continue // 편향 구간 폐기
      out.push(alphabet[b % alphabet.length])
      if (out.length === length) break
    }
  }
  return out.join('')
}

/** DB 비밀번호 (MySQL 8 기본 정책 + 특수문자 회피) */
function generateDbPassword() {
  return randomString(28)
}

/**
 * INTEGRATION_SECRET_KEY.
 * 백엔드 IntegrationCrypto 가 SHA-256 으로 파생하므로 길이 제약은 없지만,
 * 엔트로피를 충분히 두기 위해 48자를 쓴다.
 */
function generateIntegrationSecretKey() {
  return randomString(48)
}

module.exports = { randomString, generateDbPassword, generateIntegrationSecretKey }
