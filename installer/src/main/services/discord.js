'use strict'

const { request, describeNetworkError, sleep } = require('../util/http')

/**
 * 4단계 — 디스코드 봇 가이드형 자동화 (Discord REST API v10).
 *
 * 자동화 범위
 *   토큰 검증 → 초대 URL 생성 → 봇이 들어간 서버 조회 →
 *   연동 안내 채널 / 캠퍼스 채널 / 캠퍼스 역할 생성(있으면 재사용) → 채널 권한 설정
 *
 * 자동화 불가 (공식 API 없음 → 화면 가이드로 대체)
 *   앱 생성, Bot 탭에서 토큰 발급, Privileged Intent(SERVER MEMBERS) 토글, 서버에 초대 승인
 */

const API = 'https://discord.com/api/v10'
const PORTAL_URL = 'https://discord.com/developers/applications'

/**
 * 봇에게 필요한 권한 비트.
 * 백엔드가 실제로 하는 일에서 역산한 최소 집합이다.
 *   MANAGE_CHANNELS  0x10        (16)          마법사가 캠퍼스/연동 채널 생성·권한 설정
 *   MANAGE_ROLES     0x10000000  (268435456)   PlaybookListener#grantCampusRole 이 역할 부여
 *   VIEW_CHANNEL     0x400       (1024)        채널 조회
 *   SEND_MESSAGES    0x800       (2048)        DiscordNotificationService 대여·반납 알림
 *   EMBED_LINKS      0x4000      (16384)       알림 임베드
 *   READ_MESSAGE_HISTORY 0x10000 (65536)       연동 버튼 메시지 재사용 확인
 */
const PERMISSION_BITS = {
  MANAGE_CHANNELS: 1n << 4n,
  VIEW_CHANNEL: 1n << 10n,
  SEND_MESSAGES: 1n << 11n,
  EMBED_LINKS: 1n << 14n,
  READ_MESSAGE_HISTORY: 1n << 16n,
  MANAGE_ROLES: 1n << 28n
}

const REQUIRED_PERMISSIONS = Object.values(PERMISSION_BITS).reduce((a, b) => a | b, 0n)

/** 사용자에게 보여줄 권한 이름 — 디스코드 한국어 UI 표기와 맞춘다 */
const PERMISSION_LABELS = {
  MANAGE_CHANNELS: '채널 관리',
  VIEW_CHANNEL: '채널 보기',
  SEND_MESSAGES: '메시지 보내기',
  EMBED_LINKS: '링크 첨부',
  READ_MESSAGE_HISTORY: '메시지 기록 보기',
  MANAGE_ROLES: '역할 관리'
}

/**
 * 서버에서의 실효 권한 정수를 해석한다.
 *
 * <p>초대 승인만으로는 권한이 들어왔는지 알 수 없다 — 승인 화면에서 사용자가 권한 체크를 끄거나,
 * 이미 들어가 있는 봇을 예전 권한 그대로 재사용하는 경우가 흔하다. 그러면 ④ 채널·역할 생성이
 * 그제서야 403 으로 실패하고, 더 나쁘게는 런타임의 역할 부여(grantCampusRole)가
 * <b>조용히</b> 실패한다. 그래서 초대 직후 실제 값을 확인한다.
 *
 * @param permissionsRaw /users/@me/guilds 의 permissions (문자열)
 */
function describePermissions(permissionsRaw) {
  let bits
  try {
    bits = BigInt(permissionsRaw || '0')
  } catch {
    bits = 0n
  }
  // ADMINISTRATOR 는 다른 모든 권한을 포함한다 (디스코드 규약)
  const isAdmin = (bits & (1n << 3n)) !== 0n
  const have = []
  const missing = []
  for (const [name, bit] of Object.entries(PERMISSION_BITS)) {
    const ok = isAdmin || (bits & bit) !== 0n
    ;(ok ? have : missing).push({ name, label: PERMISSION_LABELS[name], value: bit.toString() })
  }
  return { permissions: bits.toString(), isAdmin, have, missing, hasAll: missing.length === 0 }
}

/**
 * 특정 서버에서 봇이 필요한 권한을 전부 갖췄는지 확인한다.
 * 서버 목록에 그 서버가 없으면 = 초대가 아직 안 됐거나 승인이 취소된 것이다.
 */
async function checkGuildPermissions(token, guildId, opts = {}) {
  const r = await call(token, 'GET', '/users/@me/guilds', undefined, opts)
  if (!r.ok) return { ok: false, message: r.message }

  const guild = (r.data || []).find((g) => g.id === String(guildId))
  if (!guild) {
    return {
      ok: false,
      notInGuild: true,
      message:
        '봇이 그 서버에 없습니다. 초대 링크를 열어 [승인] 까지 마친 뒤 다시 확인하세요. ' +
        '(승인했는데도 이 메시지가 나오면 다른 서버를 고른 것은 아닌지 확인하세요)'
    }
  }

  const desc = describePermissions(guild.permissions)
  return {
    ok: true,
    guildId: guild.id,
    guildName: guild.name,
    ...desc,
    message: desc.hasAll
      ? '필요한 권한을 모두 가지고 있습니다.'
      : `권한이 부족합니다 — ${desc.missing.map((m) => m.label).join(', ')}`
  }
}

/** 초대 URL 에 실을 권한 정수 (문자열) */
function permissionInteger() {
  return REQUIRED_PERMISSIONS.toString()
}

function inviteUrl(applicationId) {
  const params = new URLSearchParams({
    client_id: String(applicationId),
    scope: 'bot applications.commands',
    permissions: permissionInteger()
  })
  return `https://discord.com/oauth2/authorize?${params.toString()}`
}

/** Discord 오류 코드/상태를 사용자 언어로 옮긴다 */
function describeDiscordError(status, json) {
  const code = json && json.code
  if (status === 401) return '봇 토큰이 유효하지 않습니다. Developer Portal 에서 토큰을 다시 발급(Reset Token)해 붙여넣으세요.'
  if (status === 403) {
    return (
      '권한이 부족합니다(403). 서버 설정 > 역할 에서 봇 역할에 "채널 관리"·"역할 관리" 권한이 있는지, ' +
      '그리고 봇 역할이 만들려는 역할보다 **위쪽**에 있는지 확인하세요.'
    )
  }
  if (status === 404) return '대상을 찾을 수 없습니다(404). 봇이 해당 서버에서 추방되었는지 확인하세요.'
  if (code === 30013) return '이 서버의 역할 개수 상한(250개)에 도달했습니다. 사용하지 않는 역할을 정리하세요.'
  if (code === 30013 || code === 30003) return '서버 리소스 상한에 도달했습니다.'
  if (json && json.message) return `디스코드 오류(${status}): ${json.message}`
  return `디스코드 오류: HTTP ${status}`
}

/**
 * 레이트리밋(429) 자동 재시도.
 * Discord 는 body.retry_after(초, 소수 가능) 또는 Retry-After 헤더로 대기시간을 준다.
 */
async function call(token, method, path, body, { retries = 3, onLog } = {}) {
  let attempt = 0
  for (;;) {
    let res
    try {
      res = await request(`${API}${path}`, {
        method,
        headers: {
          Authorization: `Bot ${token}`,
          'Content-Type': 'application/json',
          'User-Agent': 'PlaybookInstaller (https://github.com/tbongkim03/playbook, 0.2.0)'
        },
        body: body === undefined ? undefined : JSON.stringify(body)
      })
    } catch (e) {
      return { ok: false, message: describeNetworkError(e), networkError: true }
    }

    if (res.status === 429 && attempt < retries) {
      const retryAfterSec =
        (res.json && Number(res.json.retry_after)) || Number(res.headers.get('retry-after')) || 1
      const waitMs = Math.min(Math.ceil(retryAfterSec * 1000) + 250, 30000)
      attempt += 1
      onLog && onLog(`디스코드 호출 제한(429) — ${(waitMs / 1000).toFixed(1)}초 대기 후 재시도 (${attempt}/${retries})`)
      await sleep(waitMs)
      continue
    }

    if (res.status === 429) {
      return {
        ok: false,
        rateLimited: true,
        message: '디스코드 호출 제한(429)이 계속됩니다. 1~2분 뒤 [다시 시도] 를 눌러주세요.'
      }
    }

    if (!res.ok) {
      return { ok: false, status: res.status, raw: res.json, message: describeDiscordError(res.status, res.json) }
    }
    return { ok: true, data: res.json }
  }
}

/** 봇 토큰 검증 — GET /users/@me */
async function verifyToken(token) {
  if (!token || !token.trim()) return { ok: false, message: '봇 토큰을 입력하세요.' }
  const clean = token.trim().replace(/^Bot\s+/i, '')
  const r = await call(clean, 'GET', '/users/@me')
  if (!r.ok) return r
  const me = r.data || {}
  return {
    ok: true,
    message: `봇 확인 완료: ${me.username}${me.discriminator && me.discriminator !== '0' ? `#${me.discriminator}` : ''}`,
    botId: me.id,
    botName: me.username,
    // 봇 계정의 user id 는 애플리케이션(client) id 와 동일하다 → 초대 URL 에 그대로 쓴다
    applicationId: me.id,
    inviteUrl: inviteUrl(me.id)
  }
}

/** 봇이 들어가 있는 서버 목록 — GET /users/@me/guilds */
async function listGuilds(token) {
  const r = await call(token, 'GET', '/users/@me/guilds')
  if (!r.ok) return r
  // 목록 단계에서 이미 권한을 실어 보낸다 — 서버를 고르는 화면에서 바로
  // "권한 부족" 을 보여줘야 ④ 에서 403 을 만나기 전에 되돌릴 수 있다.
  const guilds = (r.data || []).map((g) => {
    const desc = describePermissions(g.permissions)
    return {
      id: g.id,
      name: g.name,
      owner: !!g.owner,
      permissions: desc.permissions,
      isAdmin: desc.isAdmin,
      hasAllPermissions: desc.hasAll,
      missingPermissions: desc.missing.map((m) => m.label)
    }
  })
  return { ok: true, guilds }
}

async function listChannels(token, guildId, opts) {
  return call(token, 'GET', `/guilds/${guildId}/channels`, undefined, opts)
}

async function listRoles(token, guildId, opts) {
  return call(token, 'GET', `/guilds/${guildId}/roles`, undefined, opts)
}

function findByName(list, name, type) {
  const target = String(name).toLowerCase()
  return (list || []).find(
    (x) => String(x.name).toLowerCase() === target && (type === undefined || x.type === type)
  )
}

/**
 * 서버에 필요한 리소스를 만들거나 재사용한다.
 *
 * 만드는 것
 *  1) 연동 안내 채널 (텍스트)  → DISCORD_LINK_CHANNEL_ID
 *  2) 캠퍼스 역할              → tb_campus_channel.discord_role_id
 *  3) 캠퍼스 채널 (텍스트)     → DISCORD_CHANNEL_{SEOCHO|GVALLEY|DONGJAK}
 *     · @everyone 은 보기 불가, 캠퍼스 역할만 보기 가능 (= 연동해야 열리는 채널)
 *
 * @returns { ok, linkChannelId, campusChannelId, campusRoleId, created:[], reused:[], warnings:[] }
 */
async function provisionGuild(token, { guildId, campusName, linkChannelName = '플북-연동안내' }, onLog = () => {}) {
  const created = []
  const reused = []
  const warnings = []

  const campusChannelName = sanitizeChannelName(`${campusName}-라운지`)
  const campusRoleName = `플북-${campusName}`
  const safeLinkChannelName = sanitizeChannelName(linkChannelName)

  // ── 사전 권한 검사 ──────────────────────────────────────────────
  // 여기서 막지 않으면 역할은 만들어졌는데 채널 생성에서 403 이 나는 식으로
  // **절반만 만들어진 상태**로 실패한다. 재시도 시 재사용 로직이 있어 복구는 되지만,
  // 사용자는 무엇이 잘못됐는지 알 수 없다. 아무것도 만들기 전에 끊는다.
  onLog('봇의 서버 내 권한을 확인합니다…')
  const perm = await checkGuildPermissions(token, guildId, { onLog })
  if (perm.ok && !perm.hasAll) {
    const labels = perm.missing.map((m) => m.label).join(', ')
    onLog(`권한 부족: ${labels}`)
    return {
      ok: false,
      permissionDenied: true,
      missingPermissions: perm.missing.map((m) => m.label),
      message:
        `봇에게 "${labels}" 권한이 없어 채널·역할을 만들 수 없습니다.\n` +
        '초대 링크로 다시 승인하거나, 서버 설정 > 역할 에서 봇 역할에 해당 권한을 켜 주세요.\n' +
        '※ 이미 서버에 들어가 있는 봇은 예전 권한을 그대로 유지합니다 — 초대만으로는 권한이 갱신되지 않을 수 있습니다.',
      created,
      reused,
      warnings
    }
  }
  if (!perm.ok) {
    // 조회 자체가 실패한 경우는 막지 않는다 — 실제 생성 호출에서 다시 걸린다
    warnings.push(`권한을 미리 확인하지 못했습니다: ${perm.message}`)
  } else if (perm.isAdmin) {
    onLog('봇이 관리자(ADMINISTRATOR) 권한을 가지고 있습니다.')
  } else {
    onLog('필요한 권한 6종을 모두 확인했습니다.')
  }

  // ※ GET /users/@me/guilds/{id}/member 는 사용자 OAuth2(guilds.members.read) 전용이라
  //   봇 토큰으로는 항상 403 이다. 권한 확인은 위 checkGuildPermissions 로 충분하다.

  onLog('기존 채널 목록을 조회합니다…')
  const channelsRes = await listChannels(token, guildId, { onLog })
  if (!channelsRes.ok) return { ok: false, message: channelsRes.message, created, reused, warnings }
  const channels = channelsRes.data || []

  onLog('기존 역할 목록을 조회합니다…')
  const rolesRes = await listRoles(token, guildId, { onLog })
  if (!rolesRes.ok) return { ok: false, message: rolesRes.message, created, reused, warnings }
  const roles = rolesRes.data || []

  // ── 1. 캠퍼스 역할 ─────────────────────────────────────────────
  let campusRole = findByName(roles, campusRoleName)
  if (campusRole) {
    reused.push(`역할 "${campusRoleName}" (기존 재사용, id=${campusRole.id})`)
    onLog(`역할 "${campusRoleName}" 이(가) 이미 있어 그대로 사용합니다.`)
  } else {
    onLog(`역할 "${campusRoleName}" 을(를) 생성합니다…`)
    const r = await call(
      token,
      'POST',
      `/guilds/${guildId}/roles`,
      { name: campusRoleName, mentionable: false, hoist: false, permissions: '0' },
      { onLog }
    )
    if (!r.ok) return { ok: false, message: `역할 생성 실패 — ${r.message}`, created, reused, warnings }
    campusRole = r.data
    created.push(`역할 "${campusRoleName}" (id=${campusRole.id})`)
  }

  // ── 2. 연동 안내 채널 (모두가 볼 수 있어야 한다) ────────────────
  let linkChannel = findByName(channels, safeLinkChannelName, 0)
  if (linkChannel) {
    reused.push(`채널 #${safeLinkChannelName} (기존 재사용, id=${linkChannel.id})`)
    onLog(`채널 #${safeLinkChannelName} 이(가) 이미 있어 그대로 사용합니다.`)
  } else {
    onLog(`채널 #${safeLinkChannelName} 을(를) 생성합니다…`)
    const r = await call(
      token,
      'POST',
      `/guilds/${guildId}/channels`,
      {
        name: safeLinkChannelName,
        type: 0,
        topic: '플북 계정 연동 안내 채널 — 이 채널의 버튼으로 디스코드 계정을 플북 계정과 연결합니다.'
      },
      { onLog }
    )
    if (!r.ok) return { ok: false, message: `연동 안내 채널 생성 실패 — ${r.message}`, created, reused, warnings }
    linkChannel = r.data
    created.push(`채널 #${safeLinkChannelName} (id=${linkChannel.id})`)
  }

  // ── 3. 캠퍼스 채널 (연동한 사람만 보이게) ───────────────────────
  // guildId 는 @everyone 역할 id 와 같다 (디스코드 규약)
  const overwrites = [
    { id: guildId, type: 0, deny: String(PERMISSION_BITS.VIEW_CHANNEL) },
    {
      id: campusRole.id,
      type: 0,
      allow: String(PERMISSION_BITS.VIEW_CHANNEL | PERMISSION_BITS.SEND_MESSAGES | PERMISSION_BITS.READ_MESSAGE_HISTORY)
    }
  ]

  let campusChannel = findByName(channels, campusChannelName, 0)
  if (campusChannel) {
    reused.push(`채널 #${campusChannelName} (기존 재사용, id=${campusChannel.id})`)
    onLog(`채널 #${campusChannelName} 이(가) 이미 있어 권한만 맞춥니다…`)
    const patch = await call(
      token,
      'PATCH',
      `/channels/${campusChannel.id}`,
      { permission_overwrites: overwrites },
      { onLog }
    )
    if (!patch.ok) {
      warnings.push(
        `기존 채널 #${campusChannelName} 의 권한을 수정하지 못했습니다 (${patch.message}). ` +
          '디스코드에서 직접 "@everyone 은 채널 보기 불가 / 플북 역할은 보기 가능" 으로 설정해 주세요.'
      )
    }
  } else {
    onLog(`채널 #${campusChannelName} 을(를) 생성합니다…`)
    const r = await call(
      token,
      'POST',
      `/guilds/${guildId}/channels`,
      {
        name: campusChannelName,
        type: 0,
        topic: `${campusName} 캠퍼스 대여·반납 알림 채널 (플북 계정 연동 시 열립니다)`,
        permission_overwrites: overwrites
      },
      { onLog }
    )
    if (!r.ok) return { ok: false, message: `캠퍼스 채널 생성 실패 — ${r.message}`, created, reused, warnings }
    campusChannel = r.data
    created.push(`채널 #${campusChannelName} (id=${campusChannel.id})`)
  }

  onLog('디스코드 리소스 준비가 끝났습니다.')

  return {
    ok: true,
    linkChannelId: linkChannel.id,
    linkChannelName: safeLinkChannelName,
    campusChannelId: campusChannel.id,
    campusChannelName,
    campusRoleId: campusRole.id,
    campusRoleName,
    created,
    reused,
    warnings
  }
}

/** 디스코드 채널명 규칙: 소문자, 공백→하이픈, 100자 이하 */
function sanitizeChannelName(name) {
  return String(name)
    .trim()
    .toLowerCase()
    .replace(/\s+/g, '-')
    .replace(/[^0-9a-z가-힣ㄱ-ㅎㅏ-ㅣ\-_]/g, '')
    .slice(0, 90) || 'playbook'
}

module.exports = {
  PORTAL_URL,
  verifyToken,
  listGuilds,
  checkGuildPermissions,
  describePermissions,
  PERMISSION_LABELS,
  provisionGuild,
  inviteUrl,
  permissionInteger,
  sanitizeChannelName,
  REQUIRED_PERMISSIONS
}
