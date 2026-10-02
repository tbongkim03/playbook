'use strict'

const os = require('node:os')

const { runCapture } = require('./exec')

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

/**
 * 어댑터 이름 → 연결된 네트워크 이름 (Windows).
 * Wi-Fi 는 네트워크 프로필 이름이 곧 Wi-Fi 이름(SSID)이다. 관리자 권한·위치 권한 없이 읽힌다.
 * 비전문가는 IP 대역보다 "어느 Wi-Fi 인지" 로 이해한다 — 화면 설명에 같이 쓴다.
 */
async function connectionNames() {
  if (process.platform !== 'win32') return {}
  // PowerShell 은 콘솔 코드페이지(CP949)로 출력한다 — 한글 Wi-Fi 이름이 깨지지 않게 UTF-8 로 고정
  const script =
    "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; Get-NetConnectionProfile | ForEach-Object { @{ alias = $_.InterfaceAlias; name = $_.Name; category = [string]$_.NetworkCategory } } | ConvertTo-Json -Compress"
  const r = await runCapture('powershell.exe', ['-NoProfile', '-Command', script], { timeout: 20000 })
  try {
    const j = JSON.parse((r.stdout || '').trim())
    const list = Array.isArray(j) ? j : [j]
    return Object.fromEntries(list.filter((x) => x && x.alias).map((x) => [x.alias, { name: x.name, category: x.category }]))
  } catch {
    return {}
  }
}

/** 사람이 읽을 네트워크 표시 — "Wi-Fi '개발본부5G'" / "유선 '네트워크 2'" */
function describeNetwork(a) {
  if (!a || !a.networkName) return ''
  const wifi = /wi-?fi|wlan|wireless|무선/i.test(a.interfaceName || '')
  return `${wifi ? 'Wi-Fi' : '유선'} '${a.networkName}'`
}

function detect(names = {}) {
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
        cidr24: networkCidr(a.address, 24),
        networkName: names[name] ? names[name].name : null,
        networkCategory: names[name] ? names[name].category : null
      })
    }
  }

  // 실물 LAN 사설 IP 를 최우선 추천
  const ranked = [...addresses].sort((a, b) => {
    const score = (x) => (x.virtual ? 0 : 2) + (x.private ? 1 : 0)
    return score(b) - score(a)
  })

  const primary = ranked[0] || null
  if (primary) primary.networkLabel = describeNetwork(primary)

  const suggestions = []
  if (primary) {
    const net = primary.networkLabel
    suggestions.push({
      value: `${primary.address}/32`,
      label: `이 PC 한 대만 (${primary.address})`,
      recommended: false
    })
    suggestions.push({
      value: primary.cidr24,
      label: net
        ? `${net} 에 연결된 기기 전체 (${primary.cidr24})`
        : `이 PC 와 같은 네트워크의 기기 전체 (${primary.cidr24})`,
      recommended: true
    })
    if (primary.prefix !== 24) {
      suggestions.push({
        value: primary.nativeCidr,
        label: `${net || '이 네트워크'} — 어댑터가 알려주는 실제 범위 (${primary.nativeCidr})`,
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

/** CIDR(IPv4) 안에 ip 가 들어가는지 */
function cidrContains(cidr, ip) {
  const [net, p] = String(cidr || '').split('/')
  if (!/^\d+\.\d+\.\d+\.\d+$/.test(net || '') || !/^\d+\.\d+\.\d+\.\d+$/.test(ip || '')) return false
  const prefix = p === undefined ? 32 : Number(p)
  return networkCidr(ip, prefix) === networkCidr(net, prefix)
}

module.exports = { detect, connectionNames, describeNetwork, cidrContains, validateEntry, networkCidr, isPrivateV4 }
