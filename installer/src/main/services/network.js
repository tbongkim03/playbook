'use strict'

const os = require('node:os')

/**
 * 6단계 — 설치 PC 의 IPv4 와 소속 대역 자동 감지.
 * 결과는 IP_ALLOWLIST_BOOTSTRAP (쉼표 구분 CIDR 목록) 으로 나간다.
 */

function ipToInt(ip) {
  return ip.split('.').reduce((acc, o) => (acc << 8) + Number(o), 0) >>> 0
}

function intToIp(n) {
  return [(n >>> 24) & 255, (n >>> 16) & 255, (n >>> 8) & 255, n & 255].join('.')
}

function prefixFromNetmask(mask) {
  try {
    return ipToInt(mask)
      .toString(2)
      .split('')
      .filter((c) => c === '1').length
  } catch {
    return 24
  }
}

/** 주어진 IP 가 속한 네트워크 주소를 지정 프리픽스로 계산 */
function networkCidr(ip, prefix) {
  const bits = Math.max(0, Math.min(32, prefix))
  const maskInt = bits === 0 ? 0 : (0xffffffff << (32 - bits)) >>> 0
  return `${intToIp(ipToInt(ip) & maskInt)}/${bits}`
}

/** 가상 어댑터(Docker/WSL/VirtualBox 등)로 보이는 인터페이스인지 */
function looksVirtual(name) {
  return /(docker|wsl|vethernet|vmware|virtualbox|vbox|hyper-v|loopback|npcap|tailscale|zerotier|utun|tun|tap)/i.test(
    name
  )
}

/** IPv4 사설 대역인지 */
function isPrivateV4(ip) {
  const n = ipToInt(ip)
  return (
    (n >>> 24) === 10 ||
    (n >= ipToInt('172.16.0.0') && n <= ipToInt('172.31.255.255')) ||
    (n >= ipToInt('192.168.0.0') && n <= ipToInt('192.168.255.255'))
  )
}

function detect() {
  const ifaces = os.networkInterfaces()
  const addresses = []

  for (const [name, list] of Object.entries(ifaces)) {
    for (const a of list || []) {
      const family = a.family === 'IPv4' || a.family === 4 ? 'IPv4' : String(a.family)
      if (family !== 'IPv4' || a.internal) continue
      const prefix = a.cidr ? Number(a.cidr.split('/')[1]) : prefixFromNetmask(a.netmask)
      addresses.push({
        interfaceName: name,
        address: a.address,
        netmask: a.netmask,
        prefix,
        virtual: looksVirtual(name),
        private: isPrivateV4(a.address),
        nativeCidr: networkCidr(a.address, prefix),
        cidr24: networkCidr(a.address, 24)
      })
    }
  }

  // 실물 LAN 사설 IP 를 최우선 추천
  const ranked = [...addresses].sort((a, b) => {
    const score = (x) => (x.virtual ? 0 : 2) + (x.private ? 1 : 0)
    return score(b) - score(a)
  })

  const primary = ranked[0] || null

  const suggestions = []
  if (primary) {
    suggestions.push({
      value: `${primary.address}/32`,
      label: `이 PC 단독 (${primary.address})`,
      recommended: false
    })
    suggestions.push({
      value: primary.cidr24,
      label: `이 PC 가 속한 /24 대역 (${primary.cidr24}) — 라운지 LAN 전체`,
      recommended: true
    })
    if (primary.prefix !== 24) {
      suggestions.push({
        value: primary.nativeCidr,
        label: `어댑터가 알려주는 실제 대역 (${primary.nativeCidr}, /${primary.prefix})`,
        recommended: false
      })
    }
  }

  return {
    detectedAt: new Date().toISOString(),
    addresses,
    primary,
    suggestions,
    // 기본 선택값 — 추천 항목만
    defaultEntries: suggestions.filter((s) => s.recommended).map((s) => s.value)
  }
}

/** 단일 IPv4 / IPv4 CIDR / 단일 IPv6 를 허용. 백엔드 검증과 어긋나지 않게 보수적으로 본다. */
function validateEntry(raw) {
  const value = String(raw || '').trim()
  if (!value) return { ok: false, message: '값이 비어 있습니다.' }
  if (value.length > 64) return { ok: false, message: '64자를 넘을 수 없습니다.' }

  if (value.includes(':')) {
    // IPv6 (CIDR 포함) — 형식만 느슨하게 확인
    const [addr, prefix] = value.split('/')
    if (!/^[0-9a-fA-F:]+$/.test(addr)) return { ok: false, message: 'IPv6 표기가 올바르지 않습니다.' }
    if (prefix !== undefined && (!/^\d{1,3}$/.test(prefix) || Number(prefix) > 128)) {
      return { ok: false, message: 'IPv6 프리픽스는 0~128 이어야 합니다.' }
    }
    return { ok: true, type: prefix === undefined ? 'SINGLE' : 'CIDR', normalized: value }
  }

  const [addr, prefix] = value.split('/')
  const octets = addr.split('.')
  if (octets.length !== 4 || octets.some((o) => !/^\d{1,3}$/.test(o) || Number(o) > 255)) {
    return { ok: false, message: 'IPv4 표기가 올바르지 않습니다. 예) 192.168.0.15' }
  }
  if (prefix === undefined) {
    return { ok: true, type: 'SINGLE', normalized: addr }
  }
  if (!/^\d{1,2}$/.test(prefix) || Number(prefix) > 32) {
    return { ok: false, message: 'CIDR 프리픽스는 0~32 이어야 합니다. 예) 192.168.0.0/24' }
  }
  const p = Number(prefix)
  const normalized = networkCidr(addr, p)
  return {
    ok: true,
    type: 'CIDR',
    normalized,
    note: normalized !== value ? `네트워크 주소로 정규화했습니다: ${value} → ${normalized}` : undefined
  }
}

module.exports = { detect, validateEntry, networkCidr, isPrivateV4 }
