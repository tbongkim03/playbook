'use strict'

const fsp = require('node:fs/promises')
const path = require('node:path')

const { maskValue } = require('../util/mask')

/**
 * 8단계 — 환경설정 파일 생성.
 *
 * ★ 변수 이름의 정본은 back/src/main/resources/application*.properties 와
 *   docker-compose.prod.yml 이다. 아래는 전부 그 파일에서 직접 확인한 이름이다 (추측 없음).
 *
 * 만드는 파일 4종 — docker-compose.prod.yml 의 env_file 배치와 1:1로 맞춘다.
 *
 *   <설치경로>/back/.env.prod        back 전용. 서비스 시크릿 전부. (0600)
 *   <설치경로>/back/.env.monitoring  back + alloy 공용. MONITORING_* 전용. (0600)
 *   <설치경로>/db/.env.prod          db 전용. MySQL 계정. (0600)
 *   <설치경로>/.env                  compose 프로젝트 변수(COMPOSE_PROFILES). 시크릿 없음. (0644)
 *
 * ★ MONITORING_* 를 .env.prod 에 쓰지 않는 이유
 *   compose 가 alloy 에는 .env.monitoring 만 물려 준다. 시크릿이 든 .env.prod 를
 *   외부로 나가는 alloy 에 넘기지 않으려는 분리이고, compose 주석에
 *   "MONITORING_* 는 이 파일에만 정의한다(.env.prod 와 키가 겹치면 안 됨)" 로 못박혀 있다.
 *   check.mjs 가 이 분리를 테스트로 강제한다.
 *
 *   application.properties
 *     INTEGRATION_SECRET_KEY, MASTER_ID/PW/NAME/DISCORD,
 *     KAKAO_REST_API_KEY, NL_API_KEY, WORK24_API_KEY,
 *     DISCORD_CHANNEL_SEOCHO/GVALLEY/DONGJAK, DISCORD_LINK_CHANNEL_ID,
 *     CORS_ALLOWED_ORIGINS,
 *     monitoring.* ← MONITORING_REFRESH_ENABLED, MONITORING_TOKEN_ENDPOINT,
 *                    MONITORING_CAMPUS, MONITORING_ACCESS_TOKEN,
 *                    MONITORING_BOOTSTRAP_SECRET, MONITORING_TOKEN_FILE
 *   application-prod.properties
 *     DB_USERNAME, DB_PASSWORD, JPA_DDL_AUTO, MANAGEMENT_PORT,
 *     IP_ALLOWLIST_ENABLED, IP_ALLOWLIST_TRUSTED_PROXIES
 *   IntegrationService (@Value)
 *     DISCORD_BOT_TOKEN, DISCORD_APPLICATION_ID  ← 앱이 기동 시 이 값을 읽어
 *     tb_integration_config 에 시드하고 시크릿을 AES 로 암호화한다.
 *     → 마법사는 DB 에 직접 쓰지 않고 .env 만 만든다.
 *   monitoring/alloy/config.alloy (env)
 *     MONITORING_REMOTE_WRITE_URL, MONITORING_SCRAPE_TARGET,
 *     MONITORING_SCRAPE_INTERVAL, MONITORING_ALLOY_LOG_LEVEL
 *   IpAllowlistFilter (코드에서 env 직접 읽음)
 *     IP_ALLOWLIST_BOOTSTRAP, IP_ALLOWLIST_BYPASS
 */

/**
 * 캠퍼스 프리셋 — 백엔드 IntegrationService.seedCampusChannels() 가
 * campusId 1/2/3 에 각각 아래 env 슬롯을 매핑한다.
 *
 * ⚠ 매핑 기준은 **campusId** 이지 캠퍼스 이름이 아니다.
 *   신규 설치는 tb_campus 가 비어 있어 마법사가 만든 캠퍼스가 campusId=1 이 되므로,
 *   이름이 무엇이든 SEOCHO 슬롯이 그 캠퍼스를 가리킨다 (slotsFor 참고).
 */
const CAMPUS_PRESETS = [
  { campusId: 1, name: '서초', envSlot: 'DISCORD_CHANNEL_SEOCHO', envRoleSlot: 'DISCORD_ROLE_SEOCHO' },
  { campusId: 2, name: 'G밸리', envSlot: 'DISCORD_CHANNEL_GVALLEY', envRoleSlot: 'DISCORD_ROLE_GVALLEY' },
  { campusId: 3, name: '동작', envSlot: 'DISCORD_CHANNEL_DONGJAK', envRoleSlot: 'DISCORD_ROLE_DONGJAK' }
]

const DB_NAME = 'playbookdb'
const DB_USER = 'playdata'

/**
 * env_file 은 셸이 아니다 — 따옴표를 붙이면 따옴표까지 값이 된다.
 * 그래서 값에 개행/따옴표가 들어가지 않게 막고, 그대로 KEY=VALUE 로 쓴다.
 * (마법사가 생성하는 시크릿은 애초에 영숫자만 쓴다 → util/secrets.js)
 */
function sanitizeEnvValue(key, value) {
  const s = value === null || value === undefined ? '' : String(value)
  if (/[\r\n]/.test(s)) {
    throw new Error(`${key} 값에 줄바꿈이 들어 있어 .env 로 쓸 수 없습니다.`)
  }
  return s
}

function renderEnv(lines) {
  const out = []
  for (const entry of lines) {
    if (typeof entry === 'string') {
      out.push(entry) // 주석 또는 빈 줄
      continue
    }
    const { key, value, skipIfEmpty } = entry
    const v = sanitizeEnvValue(key, value)
    if (skipIfEmpty && v === '') continue
    out.push(`${key}=${v}`)
  }
  return out.join('\n') + '\n'
}

function resolveCampus(state) {
  const name = (state.campus && state.campus.name) || ''
  const preset = CAMPUS_PRESETS.find((c) => c.name === name)
  return {
    name,
    preset: preset || null,
    campusId: preset ? preset.campusId : null,
    envSlot: preset ? preset.envSlot : null,
    envRoleSlot: preset ? preset.envRoleSlot : null
  }
}

/**
 * 이 캠퍼스의 디스코드 채널·역할 env 슬롯을 정한다.
 *
 * - 프리셋 이름(서초/G밸리/동작)이면 그 슬롯을 쓴다 — 기존 3캠퍼스 DB 에 설치하는 경우다.
 * - 그 외 이름이면 **campusId=1 슬롯(SEOCHO)** 을 쓴다.
 *   신규 설치는 tb_campus 가 0행이라 InitialCampusInitializer 가 만든 캠퍼스가
 *   AUTO_INCREMENT 첫 값인 1 을 받고, 백엔드는 campusId 로 슬롯을 찾기 때문이다.
 *   (이름이 프리셋과 다른데 DB 에 이미 캠퍼스가 있다면 그 캠퍼스 자체가 존재하지 않는 상황이라
 *    어차피 수동 등록이 필요하다 — 그 경우를 위해 안내 주석을 함께 남긴다.)
 */
function slotsFor(campus) {
  if (campus.preset) {
    return { channel: campus.envSlot, role: campus.envRoleSlot, assumedFreshInstall: false }
  }
  const first = CAMPUS_PRESETS[0]
  return { channel: first.envSlot, role: first.envRoleSlot, assumedFreshInstall: true }
}

function buildBackEnv(state) {
  const campus = resolveCampus(state)
  const g = state.generated || {}
  const k = state.apiKeys || {}
  const d = state.discord || {}
  const m = state.master || {}
  const mon = state.monitoring || {}
  const ip = state.ipAllowlist || {}

  const lines = [
    '# ============================================================',
    '# Playbook 백엔드 운영 환경변수 — 설치 마법사가 생성했습니다.',
    `# 생성 일시: ${new Date().toISOString()}`,
    `# 캠퍼스: ${campus.name || '(미지정)'}`,
    '#',
    '# ⚠ 이 파일에는 DB 비밀번호·API 키·봇 토큰이 평문으로 들어 있습니다.',
    '#   절대 Git 에 커밋하거나 메신저로 공유하지 마세요.',
    '#   봇 토큰과 API 키는 앱이 기동하면서 tb_integration_config 에 AES 암호화되어',
    '#   저장되고, 이후에는 관리자 화면(연동 탭)에서 관리합니다.',
    '# ============================================================',
    '',
    '# ── 프로파일 ──',
    { key: 'SPRING_PROFILES_ACTIVE', value: 'prod' },
    '',
    '# ── 데이터베이스 (application-prod.properties) ──',
    { key: 'DB_USERNAME', value: DB_USER },
    { key: 'DB_PASSWORD', value: g.dbPassword },
    '',
    '# 운영 스키마는 db/migration/*.sql 로만 바꾼다. Hibernate 가 테이블을 건드리지 못하게 validate 고정.',
    '# ⚠ 그래서 마이그레이션을 적용하지 않으면 백엔드가 기동조차 하지 못한다.',
    '#   (설치 마법사는 DB 기동 → 마이그레이션 → 나머지 서비스 기동 순서로 진행한다)',
    '# 엔티티↔스키마 불일치로 기동이 막히면 이 값을 none 으로 바꾸고 컨테이너를 재시작해 우회할 수 있다.',
    { key: 'JPA_DDL_AUTO', value: 'validate' },
    '',
    '# ── 연동 시크릿 암호화 키 (IntegrationCrypto) ──',
    '# 이 값을 바꾸면 이미 저장된 봇 토큰·API 키를 복호화할 수 없습니다. 분실 주의.',
    { key: 'INTEGRATION_SECRET_KEY', value: g.integrationSecretKey },
    '',
    '# ── 마스터 관리자 계정 ──',
    { key: 'MASTER_ID', value: m.id },
    { key: 'MASTER_PW', value: m.pw },
    { key: 'MASTER_NAME', value: m.name },
    { key: 'MASTER_DISCORD', value: m.discord },
    '',
    '# ── 카카오 책 검색 API (표지 이미지 보조 조회, 선택) ──',
    { key: 'KAKAO_REST_API_KEY', value: k.kakaoRestApiKey },
    '',
    '# ── 국립중앙도서관 ISBN API ──',
    { key: 'NL_API_KEY', value: k.nlApiKey },
    '',
    '# ── Work24 훈련과정 API ──',
    { key: 'WORK24_API_KEY', value: k.work24ApiKey },
    '',
    '# ── 디스코드 (IntegrationService 가 DB로 시드) ──',
    { key: 'DISCORD_BOT_TOKEN', value: d.botToken, skipIfEmpty: true },
    { key: 'DISCORD_APPLICATION_ID', value: d.applicationId, skipIfEmpty: true },
    { key: 'DISCORD_LINK_CHANNEL_ID', value: d.linkChannelId, skipIfEmpty: true }
  ]

  // ── 캠퍼스별 디스코드 채널·역할 매핑 ──
  // 백엔드 IntegrationService 가 tb_campus_channel 이 비어 있을 때만 시드한다.
  // 이미 매핑 행이 있으면 건너뛰므로, 연동 화면에서 고친 값을 재기동이 덮어쓰지 않는다.
  if (d.campusChannelId || d.campusRoleId) {
    const slot = slotsFor(campus)
    lines.push('', '# ── 캠퍼스 디스코드 매핑 (tb_campus_channel 최초 시드용) ──')

    if (slot.assumedFreshInstall) {
      lines.push(
        `# 캠퍼스 "${campus.name}" 은 기본 3캠퍼스(서초/G밸리/동작) 이름이 아닙니다.`,
        '# 신규 설치는 tb_campus 가 0행이라 이 캠퍼스가 campusId=1 로 만들어지고,',
        '# 백엔드는 campusId 로 슬롯을 찾으므로 아래 SEOCHO 슬롯이 이 캠퍼스를 가리킵니다.',
        '# ※ 기존 캠퍼스가 이미 있는 DB 에 설치했다면 이 값은 엉뚱한 캠퍼스에 붙을 수 있습니다.',
        '#   그 경우 관리자 화면 > 연동 탭 > 캠퍼스 채널 매핑에서 직접 확인·수정하세요.'
      )
    }

    if (d.campusChannelId) {
      lines.push('# 대여·반납 알림이 발송될 캠퍼스 채널', { key: slot.channel, value: d.campusChannelId })
    }
    if (d.campusRoleId) {
      lines.push(
        '# 플북 계정 연동 시 부여되어 캠퍼스 채널을 해금하는 역할',
        { key: slot.role, value: d.campusRoleId }
      )
    }
  }

  // ── 신규 설치 최초 캠퍼스 ──
  // db/migration/001 은 신규 설치 DB 에서 "Table 'tb_campus' already exists" 로 첫 구문에서
  // 중단되어 기본 캠퍼스 INSERT 가 실행되지 않는다(그래서 마법사도 001 을 적용하지 않는다).
  // 그대로 두면 tb_campus 가 0행이라 캠퍼스 선택이 비고 tb_book.seq_campus 가 NOT NULL + FK 라
  // 도서 등록 자체가 막힌다. InitialCampusInitializer 가 이 값으로 최초 1건을 만든다.
  //
  // 캠퍼스가 한 행이라도 있으면(Soft Delete 행 포함) 아무 동작도 하지 않으므로,
  // 기존 DB 에 재설치해도 캠퍼스가 늘어나지 않는다.
  if (campus.name) {
    lines.push(
      '',
      '# ── 신규 설치 최초 캠퍼스 (InitialCampusInitializer) ──',
      '# tb_campus 가 0행일 때만 1건 생성. 기존 캠퍼스가 있으면 아무 동작도 하지 않는다.',
      { key: 'INITIAL_CAMPUS_NAME', value: campus.name },
      '# 위치는 선택 항목입니다. 필요하면 값을 채우고 컨테이너를 재시작하세요.',
      { key: 'INITIAL_CAMPUS_LOCATION', value: '' }
    )
  }

  lines.push(
    '',
    '# ── 접속 허용 IP (IpAllowlistFilter · application-prod.properties) ──',
    '# 차단 기능 on/off. 기본 false — Windows 는 네이티브 접속 프록시(Caddy)가 실제 IP 를 넘기는지',
    '#   마법사가 검증한 뒤에만 켤 수 있다. 프록시 없이 켜면 모든 접속이 Docker 게이트웨이 IP 로 보여',
    '#   전부 차단되거나 전부 허용된다 (installer/README.md 4.7). 규칙은 아래 BOOTSTRAP 으로 미리 시드된다.',
    { key: 'IP_ALLOWLIST_ENABLED', value: ip.enabled ? 'true' : 'false' },
    '# 이 대역에서 온 요청에 한해 X-Forwarded-For 의 "마지막 항목"을 실제 클라이언트로 신뢰한다.',
    '# (첫 항목은 클라이언트가 위조할 수 있다 — 보안감사 S-1)',
    '# nginx(front) 컨테이너가 이 대역에 있다. 비우면 차단이 사실상 무력화된다.',
    '# ⚠ docker-compose.prod.yml 의 prod-network subnet 과 반드시 일치해야 한다.',
    '#   어긋나면 nginx 가 신뢰 프록시로 인식되지 않아 XFF 를 통째로 무시하고,',
    '#   모든 요청이 nginx IP 하나로 보여 차단이 오작동한다.',
    { key: 'IP_ALLOWLIST_TRUSTED_PROXIES', value: '172.31.240.0/24' },
    '# 상시허용 내부망 — 의도적으로 비워 둔다 (보안감사 S-2).',
    '# TRUSTED_PROXIES 와 겹치면 XFF 해석 실패 시 프록시 IP 가 상시허용에 매칭되어',
    '# "차단"이 아니라 "전면 허용"이 된다. 루프백은 이 값과 무관하게 항상 허용된다.',
    { key: 'IP_ALLOWLIST_INTERNAL_NETWORKS', value: '' },
    '# 최초 기동 시 DB(tb_allowed_ip)에 심을 규칙. 쉼표 구분 CIDR/단일 IP 목록.',
    '# 비어 있으면 규칙 0건 = 전면 허용 상태로 기동한다.',
    { key: 'IP_ALLOWLIST_BOOTSTRAP', value: (ip.entries || []).join(',') },
    '# 잠김 사고 시 비상 우회용. 평소에는 비워 둔다.',
    { key: 'IP_ALLOWLIST_BYPASS', value: '' },
    '',
    '# ── CORS ──',
    '# nginx 가 프론트와 /api 를 같은 오리진으로 서빙하므로 비워 둡니다(교차 출처 전면 차단).',
    { key: 'CORS_ALLOWED_ORIGINS', value: '' }
  )

  lines.push(
    '',
    '# ── Actuator 격리 포트 ──',
    '# 호스트에 매핑하지 않는다 — 같은 prod-network 의 alloy 만 /actuator/prometheus 에 접근한다.',
    { key: 'MANAGEMENT_PORT', value: '8081' },
    '',
    '# ── 중앙 모니터링 ──',
    '# ★ MONITORING_* 는 이 파일에 쓰지 않는다.',
    '#   docker-compose.prod.yml 이 back 과 alloy 에 back/.env.monitoring 을 따로 물려 주고,',
    '#   "MONITORING_* 는 이 파일에만 정의한다(.env.prod 와 키가 겹치면 안 됨)" 규약이 있다.',
    '#   alloy 에 서비스 시크릿(DB_PASSWORD·DISCORD_BOT_TOKEN 등)을 넘기지 않으려는 분리다.',
    mon.enabled
      ? '#   → 설정값은 back/.env.monitoring 에 있다.'
      : '#   → 모니터링 미사용: back/.env.monitoring 파일을 만들지 않는다(compose 의 required:false).'
  )

  return renderEnv(lines)
}

/**
 * `<설치경로>/back/.env.monitoring` — back 과 alloy 가 **공유**하는 모니터링 전용 파일.
 *
 * docker-compose.prod.yml 규약:
 *   back  : env_file [ ./back/.env.prod, ./back/.env.monitoring(required:false) ]
 *   alloy : env_file [ ./back/.env.monitoring(required:false) ]
 *   "MONITORING_* 는 이 파일에만 정의한다(.env.prod 와 키가 겹치면 안 됨)"
 *
 * 키 출처
 *   back  ← application.properties 의 monitoring.* 프로퍼티
 *   alloy ← monitoring/alloy/config.alloy 의 env(...) 호출
 */
function buildMonitoringEnv(state) {
  const campus = resolveCampus(state)
  const mon = state.monitoring || {}
  const base = String(mon.endpoint || 'https://monitoring.tbongkim.com').replace(/\/+$/, '')
  const label = mon.campusLabel || campus.name

  return renderEnv([
    '# ============================================================',
    '# Playbook 중앙 모니터링 환경변수 — 설치 마법사가 생성했습니다.',
    `# 생성 일시: ${new Date().toISOString()}`,
    '#',
    '# 이 파일은 back 컨테이너와 alloy(수집 에이전트)가 함께 읽습니다.',
    '# 서비스 시크릿(DB 비밀번호·봇 토큰·관리자 비밀번호)은 여기에 두지 않습니다 —',
    '# alloy 는 외부로 나가는 컴포넌트라 필요한 값만 받습니다.',
    '#',
    '# ⚠ MONITORING_BOOTSTRAP_SECRET 은 시크릿입니다. 이 파일을 공유하지 마세요.',
    '# ============================================================',
    '',
    '# ── back: 토큰 발급·회전 (application.properties monitoring.token.*) ──',
    { key: 'MONITORING_REFRESH_ENABLED', value: 'true' },
    { key: 'MONITORING_TOKEN_ENDPOINT', value: `${base}/auth/token` },
    { key: 'MONITORING_BOOTSTRAP_SECRET', value: mon.bootstrapSecret },
    '# 최초 기동 시 부트스트랩 시크릿으로 발급받아 채워집니다(처음에는 비워 둡니다).',
    { key: 'MONITORING_ACCESS_TOKEN', value: '' },
    '',
    '# ── back·alloy 공용 ──',
    '# 대시보드에서 캠퍼스를 구분하는 라벨. 중앙에서 발급한 토큰의 campus 값과 반드시 같아야 합니다.',
    { key: 'MONITORING_CAMPUS', value: label },
    '# back 이 회전 토큰을 기록하고 alloy 가 bearer_token_file 로 읽는 공유 볼륨(monitoring-token) 경로.',
    { key: 'MONITORING_TOKEN_FILE', value: '/monitoring/access_token' },
    '',
    '# ── alloy: scrape · remote_write (monitoring/alloy/config.alloy) ──',
    { key: 'MONITORING_REMOTE_WRITE_URL', value: `${base}/api/v1/write` },
    { key: 'MONITORING_SCRAPE_TARGET', value: 'back:8081' },
    { key: 'MONITORING_SCRAPE_INTERVAL', value: '30s' },
    { key: 'MONITORING_ALLOY_LOG_LEVEL', value: 'info' }
  ])
}

function buildDbEnv(state) {
  const g = state.generated || {}
  return renderEnv([
    '# ============================================================',
    '# Playbook DB(MySQL) 운영 환경변수 — 설치 마법사가 생성했습니다.',
    `# 생성 일시: ${new Date().toISOString()}`,
    '#',
    '# ⚠ MYSQL_PASSWORD 는 back/.env.prod 의 DB_PASSWORD 와 반드시 같아야 합니다.',
    '#   컨테이너 볼륨(db-data)이 이미 초기화된 뒤에는 이 값을 바꿔도 DB 계정이',
    '#   따라 바뀌지 않습니다. 비밀번호를 바꾸려면 DB 안에서 직접 변경해야 합니다.',
    '# ============================================================',
    '',
    { key: 'MYSQL_ROOT_PASSWORD', value: g.dbRootPassword },
    { key: 'MYSQL_DATABASE', value: DB_NAME },
    { key: 'MYSQL_USER', value: DB_USER },
    { key: 'MYSQL_PASSWORD', value: g.dbPassword }
  ])
}

/**
 * docker compose 가 프로젝트 디렉터리에서 자동으로 읽는 .env.
 * 여기에 COMPOSE_PROFILES 를 넣어 두면, 운영자가 나중에 손으로
 * `docker compose -f docker-compose.prod.yml up -d` 를 쳐도 alloy 가 함께 뜬다.
 * (마법사는 --profile 플래그를 명시적으로 붙이므로 이 파일에 의존하지 않는다)
 */
const BACK_IMAGE_REPO = 'ghcr.io/tbongkim03/playbook-back'
const FRONT_IMAGE_REPO = 'ghcr.io/tbongkim03/playbook-front'

function buildComposeEnv(state, { imageTag } = {}) {
  const mon = state.monitoring || {}
  // 설치본에 새겨진 이미지 태그로 고정한다. 없으면 compose 기본값(:latest)
  const imageLines = imageTag
    ? [
        '# 이 설치본이 빌드될 때의 이미지 태그입니다. 롤백이 필요하면 다른 태그로 바꾸세요.',
        { key: 'BACK_IMAGE', value: `${BACK_IMAGE_REPO}:${imageTag}` },
        { key: 'FRONT_IMAGE', value: `${FRONT_IMAGE_REPO}:${imageTag}` }
      ]
    : [
        '# 이미지 롤백이 필요하면 특정 태그로 고정하세요.',
        `# BACK_IMAGE=${BACK_IMAGE_REPO}:sha-abc1234`,
        `# FRONT_IMAGE=${FRONT_IMAGE_REPO}:sha-abc1234`
      ]
  // Windows: front 를 localhost 전용으로 게시하고 네이티브 프록시(Caddy)가 80 을 받는다
  const px = state.proxy || {}
  const proxyLines =
    px.enabled && px.token
      ? [
          '',
          '# 네이티브 접속 프록시 — front 는 127.0.0.1:18080 에만 게시, 80 은 Caddy 가 받는다.',
          '# NATIVE_PROXY_TOKEN 은 Caddy 가 보내는 X-Campus-Proxy-Token 과 같아야 한다.',
          ...require('./proxy').composeEnvEntries(px.token)
        ]
      : []
  return renderEnv([
    '# docker compose 프로젝트 환경변수 — 설치 마법사가 생성했습니다.',
    '# 시크릿은 들어 있지 않습니다 (그건 back/.env.prod, db/.env.prod 에 있습니다).',
    '',
    '# 모니터링 사이드카(alloy)는 compose 의 profiles: [monitoring] 뒤에 있습니다.',
    { key: 'COMPOSE_PROFILES', value: mon.enabled ? 'monitoring' : '' },
    '',
    ...imageLines,
    ...proxyLines
  ])
}

/** 설치 경로에 .env 3종을 쓴다. 시크릿 파일은 0600. */
async function writeEnvFiles(state, installDir, { imageTag } = {}) {
  const backDir = path.join(installDir, 'back')
  const dbDir = path.join(installDir, 'db')
  await fsp.mkdir(backDir, { recursive: true })
  await fsp.mkdir(dbDir, { recursive: true })

  const backPath = path.join(backDir, '.env.prod')
  const dbPath = path.join(dbDir, '.env.prod')
  const composePath = path.join(installDir, '.env')
  const monitoringPath = path.join(backDir, '.env.monitoring')

  await fsp.writeFile(backPath, buildBackEnv(state), { encoding: 'utf8', mode: 0o600 })
  await fsp.writeFile(dbPath, buildDbEnv(state), { encoding: 'utf8', mode: 0o600 })
  await fsp.writeFile(composePath, buildComposeEnv(state, { imageTag }), { encoding: 'utf8', mode: 0o644 })

  const secretFiles = [backPath, dbPath]
  let monitoringWritten = null
  if (state.monitoring && state.monitoring.enabled) {
    await fsp.writeFile(monitoringPath, buildMonitoringEnv(state), { encoding: 'utf8', mode: 0o600 })
    secretFiles.push(monitoringPath)
    monitoringWritten = monitoringPath
  } else {
    // 모니터링을 껐는데 예전 파일이 남아 있으면 back 이 그 값을 그대로 읽어
    // "껐다고 생각했는데 계속 전송되는" 상태가 된다. 명시적으로 지운다.
    await fsp.rm(monitoringPath, { force: true })
  }

  for (const p of secretFiles) {
    try {
      await fsp.chmod(p, 0o600)
    } catch {
      /* Windows: 파일 모드가 사실상 무시된다 — README 의 NTFS 권한 안내 참고 */
    }
  }
  return { backPath, dbPath, composePath, monitoringPath: monitoringWritten }
}

/**
 * 8단계 요약 화면용 — 시크릿은 마스킹해서 내보낸다.
 * 실제 파일 내용은 절대 렌더러로 보내지 않는다.
 */
function summarize(state) {
  const campus = resolveCampus(state)
  const g = state.generated || {}
  const k = state.apiKeys || {}
  const d = state.discord || {}
  const m = state.master || {}
  const mon = state.monitoring || {}
  const ip = state.ipAllowlist || {}
  const v = state.apiVerify || {}

  return {
    campus: {
      name: campus.name,
      campusId: campus.campusId,
      envSlot: campus.envSlot,
      customCampusWarning: !campus.preset && !!campus.name
    },
    generated: {
      dbPassword: maskValue(g.dbPassword),
      dbRootPassword: maskValue(g.dbRootPassword),
      integrationSecretKey: maskValue(g.integrationSecretKey),
      generatedAt: g.generatedAt,
      backupSavedTo: g.backupSavedTo
    },
    apiKeys: {
      kakaoRestApiKey: maskValue(k.kakaoRestApiKey),
      nlApiKey: maskValue(k.nlApiKey),
      work24ApiKey: maskValue(k.work24ApiKey),
      verified: {
        kakao: v.kakao ? v.kakao.ok : null,
        nl: v.nl ? v.nl.ok : null,
        work24: v.work24 ? v.work24.ok : null
      }
    },
    discord: {
      skipped: !!d.skipped,
      botName: d.botName,
      applicationId: d.applicationId,
      botToken: maskValue(d.botToken),
      guildName: d.guildName,
      guildId: d.guildId,
      linkChannelId: d.linkChannelId,
      campusChannelId: d.campusChannelId,
      campusRoleId: d.campusRoleId
    },
    master: { id: m.id, name: m.name, discord: m.discord, pw: maskValue(m.pw) },
    ipAllowlist: { entries: ip.entries || [], empty: (ip.entries || []).length === 0 },
    monitoring: {
      enabled: !!mon.enabled,
      endpoint: mon.endpoint,
      campusLabel: mon.campusLabel || campus.name,
      bootstrapSecret: maskValue(mon.bootstrapSecret)
    },
    files: {
      back: path.join(state.installDir || '', 'back', '.env.prod'),
      db: path.join(state.installDir || '', 'db', '.env.prod')
    }
  }
}

/** 사용자가 따로 보관할 설치 정보 백업 (시크릿 평문 포함 — 명시적 요청 시에만) */
function buildBackupText(state) {
  const campus = resolveCampus(state)
  const g = state.generated || {}
  const k = state.apiKeys || {}
  const d = state.discord || {}
  const m = state.master || {}
  const mon = state.monitoring || {}
  const ip = state.ipAllowlist || {}

  const L = []
  L.push('==========================================================')
  L.push(' Playbook 설치 정보 백업')
  L.push(` 생성 일시: ${new Date().toLocaleString('ko-KR')}`)
  L.push('==========================================================')
  L.push('')
  L.push('⚠ 이 파일에는 비밀번호와 API 키가 그대로 들어 있습니다.')
  L.push('  USB·금고 등 오프라인에 보관하고, 메신저·메일로 보내지 마세요.')
  L.push('  분실 시 INTEGRATION_SECRET_KEY 없이는 DB 에 저장된 연동값을 복호화할 수 없습니다.')
  L.push('')
  L.push('[설치]')
  L.push(`  설치 경로        : ${state.installDir}`)
  L.push(`  캠퍼스           : ${campus.name}${campus.campusId ? ` (seq_campus=${campus.campusId})` : ' (사용자 지정)'}`)
  L.push('')
  L.push('[자동 생성된 시크릿]')
  L.push(`  DB 비밀번호(playdata) : ${g.dbPassword}`)
  L.push(`  DB root 비밀번호      : ${g.dbRootPassword}`)
  L.push(`  INTEGRATION_SECRET_KEY: ${g.integrationSecretKey}`)
  L.push('')
  L.push('[마스터 관리자]')
  L.push(`  아이디  : ${m.id}`)
  L.push(`  비밀번호: ${m.pw}`)
  L.push(`  이름    : ${m.name}`)
  L.push(`  디스코드: ${m.discord}`)
  L.push('')
  L.push('[외부 API 키]')
  L.push(`  카카오 REST API 키  : ${k.kakaoRestApiKey}`)
  L.push(`  국립중앙도서관 키   : ${k.nlApiKey}`)
  L.push(`  Work24 키           : ${k.work24ApiKey}`)
  L.push('')
  L.push('[디스코드]')
  if (d.skipped) {
    L.push('  (건너뜀 — 설치 후 관리자 화면 > 연동 탭에서 설정)')
  } else {
    L.push(`  봇 이름       : ${d.botName}`)
    L.push(`  애플리케이션ID: ${d.applicationId}`)
    L.push(`  봇 토큰       : ${d.botToken}`)
    L.push(`  서버          : ${d.guildName} (${d.guildId})`)
    L.push(`  연동 안내 채널: ${d.linkChannelId}`)
    L.push(`  캠퍼스 채널   : ${d.campusChannelId}`)
    L.push(`  캠퍼스 역할   : ${d.campusRoleId}  ← 연동 탭에 수동 입력 필요`)
  }
  L.push('')
  L.push('[접속 허용 IP]')
  if ((ip.entries || []).length === 0) {
    L.push('  (등록 없음 — 규칙 0건이므로 전면 허용 상태)')
  } else {
    for (const e of ip.entries) L.push(`  - ${e}`)
  }
  L.push('')
  L.push('[중앙 모니터링]')
  if (!mon.enabled) {
    L.push('  사용 안 함')
  } else {
    L.push(`  엔드포인트        : ${mon.endpoint}`)
    L.push(`  캠퍼스 라벨       : ${mon.campusLabel || campus.name}`)
    L.push(`  부트스트랩 시크릿 : ${mon.bootstrapSecret}`)
  }
  L.push('')
  L.push('==========================================================')
  return L.join('\n')
}

module.exports = {
  CAMPUS_PRESETS,
  DB_NAME,
  DB_USER,
  buildBackEnv,
  buildDbEnv,
  buildComposeEnv,
  buildMonitoringEnv,
  writeEnvFiles,
  summarize,
  buildBackupText,
  resolveCampus
}
