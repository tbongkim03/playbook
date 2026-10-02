'use strict'

/**
 * 5단계 — 마스터 관리자 계정 검증.
 *
 * application.properties 의 기본값(admin / admin1234)을 그대로 쓰지 못하게 막는다.
 * 이 계정은 IP 허용목록·연동 설정까지 건드릴 수 있는 최상위 계정이라,
 * 기본값이 남아 있으면 설치 자체가 사고다.
 */

const FORBIDDEN_IDS = ['admin', 'administrator', 'root', 'test', 'user', 'master', 'playbook']
const FORBIDDEN_PWS = [
  'admin1234',
  'admin',
  'password',
  'password1234',
  '12345678',
  '1234567890',
  'qwerty123',
  'playbook',
  'playbook1234',
  'admin@1234',
  'passw0rd'
]

function validateId(id) {
  const v = String(id || '').trim()
  const errors = []
  if (!v) errors.push('아이디를 입력하세요.')
  else {
    if (v.length < 4 || v.length > 20) errors.push('아이디는 4~20자여야 합니다.')
    if (!/^[A-Za-z0-9_.-]+$/.test(v)) errors.push('아이디는 영문·숫자·(_ . -)만 쓸 수 있습니다.')
    if (FORBIDDEN_IDS.includes(v.toLowerCase())) {
      errors.push(`"${v}" 는 기본값·추측하기 쉬운 아이디라 사용할 수 없습니다.`)
    }
  }
  return { ok: errors.length === 0, errors }
}

function scorePassword(pw) {
  const v = String(pw || '')
  const checks = {
    length: v.length >= 10,
    lower: /[a-z]/.test(v),
    upper: /[A-Z]/.test(v),
    digit: /\d/.test(v),
    symbol: /[^A-Za-z0-9]/.test(v),
    noRepeat: !/(.)\1{3,}/.test(v),
    noSequence: !/(0123|1234|2345|3456|4567|5678|6789|abcd|qwer|asdf)/i.test(v)
  }
  const passed = Object.values(checks).filter(Boolean).length
  let level = 'weak'
  if (passed >= 6) level = 'strong'
  else if (passed >= 4) level = 'fair'
  return { checks, passed, total: Object.keys(checks).length, level }
}

function validatePassword(pw, { id } = {}) {
  const v = String(pw || '')
  const errors = []
  const warnings = []

  if (!v) {
    errors.push('비밀번호를 입력하세요.')
    return { ok: false, errors, warnings, strength: scorePassword('') }
  }
  if (v.length < 10) errors.push('비밀번호는 10자 이상이어야 합니다.')
  if (v.length > 72) errors.push('비밀번호는 72자를 넘을 수 없습니다. (BCrypt 한도)')
  if (FORBIDDEN_PWS.includes(v.toLowerCase())) {
    errors.push('기본값이거나 널리 알려진 비밀번호입니다. 다른 값을 쓰세요.')
  }
  if (id && v.toLowerCase().includes(String(id).toLowerCase())) {
    errors.push('비밀번호에 아이디를 포함할 수 없습니다.')
  }
  if (/[\r\n]/.test(v)) errors.push('비밀번호에 줄바꿈을 넣을 수 없습니다.')
  if (/[#"'\\]/.test(v)) {
    warnings.push('#, 따옴표, 역슬래시는 .env 파일 해석에서 문제를 일으킬 수 있어 피하는 것을 권장합니다.')
  }

  const strength = scorePassword(v)
  const categories = [strength.checks.lower, strength.checks.upper, strength.checks.digit, strength.checks.symbol]
  if (categories.filter(Boolean).length < 3) {
    errors.push('영문 대문자·소문자·숫자·특수문자 중 3종류 이상을 섞으세요.')
  }
  if (!strength.checks.noRepeat) warnings.push('같은 문자가 4번 이상 반복됩니다.')
  if (!strength.checks.noSequence) warnings.push('연속된 문자열(1234, qwer 등)이 들어 있습니다.')

  return { ok: errors.length === 0, errors, warnings, strength }
}

function validateAll({ id, pw, name, discord }) {
  const idRes = validateId(id)
  const pwRes = validatePassword(pw, { id })
  const errors = { id: idRes.errors, pw: pwRes.errors, name: [], discord: [] }

  const nameV = String(name || '').trim()
  if (!nameV) errors.name.push('이름을 입력하세요.')
  else if (nameV.length > 30) errors.name.push('이름은 30자 이하여야 합니다.')

  const dcV = String(discord || '').trim()
  if (!dcV) {
    errors.discord.push('디스코드 아이디를 입력하세요. (봇으로 계정을 연동할 때 사용합니다)')
  } else if (!/^[a-z0-9._]{2,32}$/.test(dcV)) {
    errors.discord.push('디스코드 사용자명은 영문 소문자·숫자·(. _) 2~32자입니다. 표시 이름이 아니라 사용자명을 넣으세요.')
  }

  const ok = Object.values(errors).every((list) => list.length === 0)
  return { ok, errors, warnings: pwRes.warnings, strength: pwRes.strength }
}

module.exports = { validateAll, validateId, validatePassword, scorePassword, FORBIDDEN_IDS, FORBIDDEN_PWS }
