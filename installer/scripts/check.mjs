#!/usr/bin/env node
/**
 * 리눅스/CI 에서 돌릴 수 있는 자체 점검.
 * Electron 없이 순수 Node 로 검증 가능한 것만 본다.
 *
 *   1) 메인 프로세스 모듈이 전부 로드되는가 (문법·순환참조)
 *   2) SQL 파서가 실제 db/migration/*.sql 을 제대로 쪼개는가
 *   3) DROP / TRUNCATE / DELETE FROM 이 실행 대상에서 빠지는가  ← 최우선 제약
 *   4) 주석 안의 롤백 DROP 이 오탐을 만들지 않는가
 *   5) .env 렌더러가 정본 변수 이름을 그대로 쓰는가
 *   6) IP 검증/정규화가 맞는가
 *   7) 시크릿 생성기가 .env 안전 문자만 쓰는가
 */
import { createRequire } from 'node:module'
import { readFileSync, readdirSync, existsSync } from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const require = createRequire(import.meta.url)
const here = path.dirname(fileURLToPath(import.meta.url))
const root = path.resolve(here, '..')
const repoRoot = path.resolve(root, '..')

let pass = 0
let fail = 0
const failures = []

function check(name, fn) {
  try {
    const r = fn()
    if (r === false) throw new Error('조건 불충족')
    pass += 1
    console.log(`  ok   ${name}${typeof r === 'string' ? `\n       └ ${r}` : ''}`)
  } catch (e) {
    fail += 1
    failures.push(`${name}: ${e.message}`)
    console.log(`  FAIL ${name} — ${e.message}`)
  }
}

function assert(cond, msg) {
  if (!cond) throw new Error(msg || '단언 실패')
}

console.log('\n[1] 메인 프로세스 모듈 로드')
const MODULES = [
  'src/main/state.js',
  'src/main/util/secrets.js',
  'src/main/util/mask.js',
  'src/main/util/http.js',
  'src/main/services/exec.js',
  'src/main/services/environment.js',
  'src/main/services/apikeys.js',
  'src/main/services/discord.js',
  'src/main/services/network.js',
  'src/main/services/envfile.js',
  'src/main/services/compose.js',
  'src/main/services/migration.js',
  'src/main/services/master.js',
  'src/main/services/finish.js'
]
for (const m of MODULES) {
  check(m, () => {
    require(path.join(root, m))
    return true
  })
}

// electron 을 require 하는 모듈(index.js / ipc.js)은 Node 단독으로 로드할 수 없으므로
// 문법만 확인한다.
console.log('\n[2] electron 의존 모듈 문법 검사')
for (const m of ['src/main/index.js', 'src/main/ipc.js', 'src/main/preload.js']) {
  check(m, () => {
    const vm = require('node:vm')
    const src = readFileSync(path.join(root, m), 'utf8')
    new vm.Script(src, { filename: m })
    return true
  })
}

const migration = require(path.join(root, 'src/main/services/migration.js'))
const envfile = require(path.join(root, 'src/main/services/envfile.js'))
const network = require(path.join(root, 'src/main/services/network.js'))
const secrets = require(path.join(root, 'src/main/util/secrets.js'))
const mask = require(path.join(root, 'src/main/util/mask.js'))
const discord = require(path.join(root, 'src/main/services/discord.js'))
const master = require(path.join(root, 'src/main/services/master.js'))

console.log('\n[3] SQL 파서 — 실제 마이그레이션 파일')
const migDir = path.join(repoRoot, 'db', 'migration')
if (existsSync(migDir)) {
  for (const f of readdirSync(migDir).filter((x) => x.endsWith('.sql')).sort()) {
    check(`파싱 ${f}`, () => {
      const sql = readFileSync(path.join(migDir, f), 'utf8')
      const parsed = migration.parseFile(f, sql)
      assert(parsed.totalStatements > 0, '문장이 하나도 파싱되지 않음')
      // 실행 대상 문장에 파괴적 문장이 하나라도 있으면 안 된다
      for (const g of parsed.groups) {
        for (const s of g.statements) {
          const d = migration.isDestructive(s)
          assert(!d.destructive, `실행 대상에 파괴적 문장이 남음: ${s.slice(0, 80)}`)
        }
      }
      return (
        `문장 ${parsed.totalStatements}개 / 그룹 ${parsed.groups.length}개 / 제외 ${parsed.excluded.length}개` +
        ` [${parsed.groups.map((g) => g.label).join(', ')}]`
      )
    })
  }
} else {
  console.log('  skip db/migration 없음')
}

console.log('\n[4] 파괴적 문장 판정')
check('DROP TABLE 은 제외된다', () => {
  const p = migration.parseFile('t.sql', 'CREATE TABLE a (id INT);\nDROP TABLE b;')
  assert(p.excluded.length === 1, `제외 1개 기대, 실제 ${p.excluded.length}`)
  assert(p.groups.length === 1 && p.groups[0].table === 'a', '남은 그룹이 a 가 아님')
  return true
})
check('TRUNCATE 는 제외된다', () => {
  const p = migration.parseFile('t.sql', 'TRUNCATE TABLE tb_book;')
  assert(p.excluded.length === 1 && p.groups.length === 0)
  return true
})
check('DELETE FROM 은 WHERE 유무와 무관하게 제외된다', () => {
  const a = migration.parseFile('t.sql', 'DELETE FROM tb_book;')
  const b = migration.parseFile('t.sql', "DELETE FROM tb_book WHERE id = 1;")
  assert(a.excluded.length === 1 && a.groups.length === 0, 'WHERE 없는 DELETE 미제외')
  assert(b.excluded.length === 1 && b.groups.length === 0, 'WHERE 있는 DELETE 미제외')
  return true
})
check('ALTER ... DROP FOREIGN KEY 는 제외된다', () => {
  const p = migration.parseFile('t.sql', 'ALTER TABLE a DROP FOREIGN KEY fk_x;')
  assert(p.excluded.length === 1 && p.groups.length === 0)
  return true
})
check('주석 안의 롤백 DROP 은 오탐을 만들지 않는다', () => {
  const sql = [
    'CREATE TABLE tb_allowed_ip (seq INT);',
    '-- 롤백',
    '-- DROP TABLE IF EXISTS tb_allowed_ip;',
    '/* DROP TABLE tb_allowed_ip; */'
  ].join('\n')
  const p = migration.parseFile('005.sql', sql)
  assert(p.excluded.length === 0, `주석 DROP 이 제외 목록에 잡힘 (${p.excluded.length}건)`)
  assert(p.groups.length === 1 && p.groups[0].table === 'tb_allowed_ip')
  return true
})
check('DROP 이 들어간 컬럼명은 오탐이 아니다', () => {
  const p = migration.parseFile('t.sql', 'ALTER TABLE a ADD COLUMN drop_yn CHAR(1);')
  assert(p.excluded.length === 0, '컬럼명 때문에 오탐 발생')
  return true
})
check('문자열 리터럴 안의 세미콜론이 문장을 쪼개지 않는다', () => {
  const st = migration.splitStatements("INSERT INTO a (v) VALUES ('x;y');\nSELECT 1;")
  assert(st.length === 2, `문장 2개 기대, 실제 ${st.length}`)
  return true
})

console.log('\n[5] .env 렌더러 — 정본 변수 이름')
const SAMPLE_STATE = {
  installDir: '/tmp/pb',
  campus: { name: '서초' },
  generated: { dbPassword: 'PwAbc123', dbRootPassword: 'RootAbc123', integrationSecretKey: 'KeyAbc123' },
  apiKeys: { kakaoRestApiKey: 'kakaokey', nlApiKey: 'nlkey', work24ApiKey: 'w24' },
  apiVerify: {},
  discord: {
    botToken: 'MDAwMDAwMDAwMDAwMDAwMDAw.SAMPLE.botTokenValue',
    applicationId: '111',
    linkChannelId: '222',
    campusChannelId: '333',
    campusRoleId: '444'
  },
  master: { id: 'pbadmin', pw: 'Str0ng!Pass', name: '관리자', discord: 'someone' },
  ipAllowlist: { entries: ['192.168.0.0/24'] },
  monitoring: {
    enabled: true,
    endpoint: 'https://monitoring.tbongkim.com',
    campusLabel: 'seocho',
    bootstrapSecret: 'bootstrapSecretSample123'
  }
}

const REQUIRED_BACK_KEYS = [
  'SPRING_PROFILES_ACTIVE',
  'DB_USERNAME',
  'DB_PASSWORD',
  'INTEGRATION_SECRET_KEY',
  'MASTER_ID',
  'MASTER_PW',
  'MASTER_NAME',
  'MASTER_DISCORD',
  'KAKAO_REST_API_KEY',
  'NL_API_KEY',
  'WORK24_API_KEY',
  'DISCORD_BOT_TOKEN',
  'DISCORD_APPLICATION_ID',
  'DISCORD_LINK_CHANNEL_ID',
  'DISCORD_CHANNEL_SEOCHO',
  // 마법사가 만든 캠퍼스 역할 ID. 없으면 운영자가 연동 탭에서 손으로 옮겨 적어야 한다.
  'DISCORD_ROLE_SEOCHO',
  // 신규 설치 최초 캠퍼스. 없으면 tb_campus 가 0행이라 도서 등록이 막힌다.
  //
  // ⚠ 이 두 키는 application.properties 에 선언되어 있지 않다
  //   (KEY=${KEY:} 형태로 적으면 Circular placeholder 로 백엔드 기동이 실패한다).
  //   그래서 위쪽의 "properties 참조 변수 대조" 검사가 이 키의 누락을 잡지 못한다.
  //   여기 명시적으로 넣어야만 회귀가 걸린다.
  'INITIAL_CAMPUS_NAME',
  'INITIAL_CAMPUS_LOCATION',
  'JPA_DDL_AUTO',
  'IP_ALLOWLIST_ENABLED',
  'IP_ALLOWLIST_TRUSTED_PROXIES',
  'IP_ALLOWLIST_INTERNAL_NETWORKS',
  'IP_ALLOWLIST_BOOTSTRAP',
  'IP_ALLOWLIST_BYPASS',
  'CORS_ALLOWED_ORIGINS',
  'MANAGEMENT_PORT'
]

const backEnv = envfile.buildBackEnv(SAMPLE_STATE)
for (const k of REQUIRED_BACK_KEYS) {
  check(`back/.env.prod 에 ${k}`, () => {
    assert(new RegExp(`^${k}=`, 'm').test(backEnv), '없음')
    return true
  })
}

check('.env.prod 에는 MONITORING_* 가 없어야 한다 (compose 규약)', () => {
  const leaked = [...backEnv.matchAll(/^(MONITORING_[A-Z0-9_]+)=/gm)].map((m) => m[1])
  assert(leaked.length === 0, `.env.prod 에 유출: ${leaked.join(', ')}`)
  return true
})

const monEnv = envfile.buildMonitoringEnv(SAMPLE_STATE)
for (const k of [
  'MONITORING_REFRESH_ENABLED',
  'MONITORING_TOKEN_ENDPOINT',
  'MONITORING_BOOTSTRAP_SECRET',
  'MONITORING_ACCESS_TOKEN',
  'MONITORING_CAMPUS',
  'MONITORING_TOKEN_FILE',
  'MONITORING_REMOTE_WRITE_URL',
  'MONITORING_SCRAPE_TARGET',
  'MONITORING_SCRAPE_INTERVAL',
  'MONITORING_ALLOY_LOG_LEVEL'
]) {
  check(`back/.env.monitoring 에 ${k}`, () => {
    assert(new RegExp(`^${k}=`, 'm').test(monEnv), '없음')
    return true
  })
}
check('.env.monitoring 에 서비스 시크릿이 섞이지 않는다', () => {
  // alloy 가 읽는 파일이다 — DB 비밀번호·봇 토큰·관리자 비밀번호가 여기 있으면 안 된다
  for (const v of ['PwAbc123', 'RootAbc123', 'KeyAbc123', 'MDAwMDAwMDAwMDAwMDAwMDAw.SAMPLE.botTokenValue', 'Str0ng!Pass', 'nsec', 'nlkey', 'w24']) {
    assert(!monEnv.includes(v), `유출: ${v}`)
  }
  for (const k of ['DB_PASSWORD', 'DISCORD_BOT_TOKEN', 'MASTER_PW', 'INTEGRATION_SECRET_KEY']) {
    assert(!new RegExp(`^${k}=`, 'm').test(monEnv), `키 유출: ${k}`)
  }
  return true
})
check('.env.monitoring 의 URL 이 엔드포인트에서 파생된다', () => {
  assert(/^MONITORING_TOKEN_ENDPOINT=https:\/\/monitoring\.tbongkim\.com\/auth\/token$/m.test(monEnv), monEnv)
  assert(/^MONITORING_REMOTE_WRITE_URL=https:\/\/monitoring\.tbongkim\.com\/api\/v1\/write$/m.test(monEnv), monEnv)
  return true
})

check('application.properties 가 참조하는 변수를 전부 덮는가', () => {
  const props = path.join(repoRoot, 'back/src/main/resources/application.properties')
  const prodProps = path.join(repoRoot, 'back/src/main/resources/application-prod.properties')
  if (!existsSync(props)) return true
  const text = readFileSync(props, 'utf8') + (existsSync(prodProps) ? readFileSync(prodProps, 'utf8') : '')
  const referenced = new Set()
  for (const m of text.matchAll(/\$\{([A-Z0-9_]+)(?::[^}]*)?\}/g)) referenced.add(m[1])
  // 캠퍼스별 채널은 설치 캠퍼스 것만 쓰므로 나머지는 제외
  // MONITORING_* 는 .env.monitoring 에 있으므로 두 파일을 합쳐서 대조한다
  const generated = backEnv + '\n' + monEnv
  const optional = new Set(['DISCORD_CHANNEL_GVALLEY', 'DISCORD_CHANNEL_DONGJAK'])
  const missing = [...referenced].filter(
    (k) => !optional.has(k) && !new RegExp(`^${k}=`, 'm').test(generated) && !/^MONITORING_REFRESH_(INTERVAL|INITIAL)/.test(k)
  )
  assert(missing.length === 0, `누락: ${missing.join(', ')}`)
  return true
})

const dbEnv = envfile.buildDbEnv(SAMPLE_STATE)
for (const k of ['MYSQL_ROOT_PASSWORD', 'MYSQL_DATABASE', 'MYSQL_USER', 'MYSQL_PASSWORD']) {
  check(`db/.env.prod 에 ${k}`, () => {
    assert(new RegExp(`^${k}=`, 'm').test(dbEnv), '없음')
    return true
  })
}
check('DB_PASSWORD 와 MYSQL_PASSWORD 가 일치', () => {
  const a = backEnv.match(/^DB_PASSWORD=(.*)$/m)[1]
  const b = dbEnv.match(/^MYSQL_PASSWORD=(.*)$/m)[1]
  assert(a === b, `${a} !== ${b}`)
  return true
})
check('요약에 시크릿 평문이 새지 않는다', () => {
  const s = JSON.stringify(envfile.summarize(SAMPLE_STATE))
  for (const v of ['PwAbc123', 'RootAbc123', 'KeyAbc123', 'nsec', 'nlkey', 'w24', 'MDAwMDAwMDAwMDAwMDAwMDAw.SAMPLE.botTokenValue', 'Str0ng!Pass', 'bootstrapSecretSample123']) {
    assert(!s.includes(v), `평문 노출: ${v}`)
  }
  return true
})
check('모니터링 미사용 시 .env.prod 에 MONITORING_* 가 없다', () => {
  // 파일 자체를 만들지 않는다 (compose 의 required:false). application.properties 기본값이
  // monitoring.token.refresh.enabled=false 이므로 부재 = 비활성이다.
  const off = envfile.buildBackEnv({ ...SAMPLE_STATE, monitoring: { enabled: false } })
  assert(!/^MONITORING_/m.test(off), off)
  return true
})

check('모니터링 사용 시 COMPOSE_PROFILES=monitoring', () => {
  const on = envfile.buildComposeEnv(SAMPLE_STATE)
  assert(/^COMPOSE_PROFILES=monitoring$/m.test(on), on)
  const off = envfile.buildComposeEnv({ ...SAMPLE_STATE, monitoring: { enabled: false } })
  assert(/^COMPOSE_PROFILES=$/m.test(off), off)
  return true
})
check('compose .env 에는 시크릿이 없다', () => {
  const t = envfile.buildComposeEnv(SAMPLE_STATE)
  for (const v of ['PwAbc123', 'KeyAbc123', 'MDAwMDAwMDAwMDAwMDAwMDAw.SAMPLE.botTokenValue', 'bootstrapSecretSample123']) assert(!t.includes(v), `평문 노출: ${v}`)
  return true
})

console.log('\n[6] IP 검증')
check('/24 정규화', () => {
  const r = network.validateEntry('192.168.0.15/24')
  assert(r.ok && r.normalized === '192.168.0.0/24', JSON.stringify(r))
  return true
})
check('단일 IPv4', () => {
  const r = network.validateEntry('10.0.0.5')
  assert(r.ok && r.type === 'SINGLE')
  return true
})
check('잘못된 옥텟 거부', () => {
  assert(!network.validateEntry('999.1.1.1').ok)
  return true
})
check('프리픽스 33 거부', () => {
  assert(!network.validateEntry('10.0.0.0/33').ok)
  return true
})
check('detect() 가 예외 없이 동작', () => {
  const d = network.detect()
  assert(Array.isArray(d.addresses) && Array.isArray(d.suggestions))
  return true
})

console.log('\n[7] 시크릿 · 마스킹')
check('생성 비밀번호는 영숫자만', () => {
  for (let i = 0; i < 200; i += 1) {
    const p = secrets.generateDbPassword()
    assert(/^[A-Za-z0-9]{28}$/.test(p), `부적합: ${p}`)
  }
  return true
})
check('생성 키 길이 48', () => {
  assert(/^[A-Za-z0-9]{48}$/.test(secrets.generateIntegrationSecretKey()))
  return true
})
check('마스킹은 앞2/뒤4 만 남긴다', () => {
  const m = mask.maskValue('abcdefghijklmnop')
  assert(m === 'ab••••••mnop', m)
  return true
})
check('로그 스크러빙', () => {
  const out = mask.scrubLine('token=SUPERSECRETVALUE123 뒤에 텍스트', ['SUPERSECRETVALUE123'])
  assert(!out.includes('SUPERSECRETVALUE123'), out)
  return true
})

console.log('\n[8] 디스코드 권한 비트')
check('필요 권한 정수', () => {
  // MANAGE_CHANNELS(16) + VIEW_CHANNEL(1024) + SEND_MESSAGES(2048)
  // + EMBED_LINKS(16384) + READ_MESSAGE_HISTORY(65536) + MANAGE_ROLES(268435456)
  const expected = String(16 + 1024 + 2048 + 16384 + 65536 + 268435456)
  assert(discord.permissionInteger() === expected, `${discord.permissionInteger()} !== ${expected}`)
  return true
})
check('초대 URL 형식', () => {
  const u = discord.inviteUrl('123456')
  assert(u.startsWith('https://discord.com/oauth2/authorize?'), u)
  assert(u.includes('client_id=123456') && u.includes('permissions='))
  return true
})
check('채널명 정규화', () => {
  assert(discord.sanitizeChannelName('서초 라운지') === '서초-라운지')
  assert(discord.sanitizeChannelName('Playbook LINK!') === 'playbook-link')
  return true
})

// 실제 개발서버(플레이북-개발용)에서 관측된 값으로 만든 회귀 테스트다.
// 봇이 초대는 되어 있는데 "채널 관리"·"역할 관리" 가 빠져 있었고, 그 상태로는
// ④ 채널·역할 생성이 403 으로 실패하고 런타임 역할 부여가 조용히 실패한다.
check('권한 부족 판정 — 실제 관측값 2248473465843265', () => {
  const d = discord.describePermissions('2248473465843265')
  assert(!d.hasAll, '부족한데 통과로 판정됨')
  assert(!d.isAdmin, 'ADMINISTRATOR 오판')
  const missing = d.missing.map((m) => m.name).sort()
  assert(
    JSON.stringify(missing) === JSON.stringify(['MANAGE_CHANNELS', 'MANAGE_ROLES']),
    `누락 목록이 다름: ${missing.join(',')}`
  )
  return true
})
check('권한 충족 판정 — 필요 비트 정확히 보유', () => {
  const d = discord.describePermissions(discord.permissionInteger())
  assert(d.hasAll, '충족인데 부족으로 판정됨')
  assert(d.missing.length === 0)
  assert(d.have.length === 6, `have=${d.have.length}`)
  return true
})
check('ADMINISTRATOR 는 전 권한을 포함', () => {
  const d = discord.describePermissions(String(1n << 3n))
  assert(d.isAdmin && d.hasAll, 'ADMINISTRATOR 인데 부족 판정')
  return true
})
check('권한 0 / 잘못된 값은 전부 부족으로 판정', () => {
  for (const v of ['0', '', null, undefined, 'not-a-number']) {
    const d = discord.describePermissions(v)
    assert(!d.hasAll, `${String(v)} 가 통과됨`)
    assert(d.missing.length === 6, `${String(v)} → missing=${d.missing.length}`)
  }
  return true
})

console.log('\n[9] 마스터 계정 검증')
check('admin/admin1234 는 거부', () => {
  const r = master.validateAll({ id: 'admin', pw: 'admin1234', name: '관리자', discord: 'lounge_manager' })
  assert(!r.ok, '기본값이 통과됨')
  assert(r.errors.id.length > 0 && r.errors.pw.length > 0)
  return true
})
check('강한 값은 통과', () => {
  const r = master.validateAll({ id: 'pb-manager', pw: 'Lounge#2026pb', name: '라운지 매니저', discord: 'lounge_manager' })
  assert(r.ok, JSON.stringify(r.errors))
  return true
})
check('비밀번호에 아이디 포함 시 거부', () => {
  const r = master.validateAll({ id: 'pbmanager', pw: 'pbmanager#2026A', name: 'x', discord: 'a_b' })
  assert(!r.ok)
  return true
})

console.log('\n[10] 렌더러 상태 투영')
check('projection 이 시크릿을 가린다', () => {
  const { WizardState } = require(path.join(root, 'src/main/state.js'))
  const s = new WizardState('/tmp/pb-wizard-check.json')
  s.data.generated.dbPassword = 'PLAINTEXTPASSWORD'
  s.data.discord.botToken = 'PLAINTEXTTOKEN'
  const p = JSON.stringify(s.projection())
  assert(!p.includes('PLAINTEXTPASSWORD') && !p.includes('PLAINTEXTTOKEN'), '평문 노출')
  assert(p.includes('__secret'), '시크릿 표식 없음')
  return true
})

console.log('\n[13] 마이그레이션 미적용 기동 차단')
{
  const mig = require(path.join(root, 'src/main/services/migration.js'))
  const scanned = {
    files: [
      { file: '001_add_campus.sql', groups: [{ table: 'tb_campus', createsTable: true, tableExists: true }] },
      { file: '002_add_campus_columns.sql', groups: [{ table: 'tb_user', createsTable: false, tableExists: true }] },
      { file: '003_add_access_audit_log.sql', groups: [
        { table: 'tb_access_log', createsTable: true, tableExists: false },
        { table: null, createsTable: false, tableExists: null }
      ] }
    ]
  }
  check('없는 테이블만 골라낸다 (ALTER·조회문·이미 있는 테이블 제외)', () => {
    const m = mig.missingTables(scanned)
    assert(m.length === 1 && m[0].table === 'tb_access_log', JSON.stringify(m))
    return true
  })
  check('모두 존재하면 빈 목록', () => {
    assert(mig.missingTables({ files: [scanned.files[0]] }).length === 0)
    return true
  })
}

console.log('\n[12] 이미지 태그 고정')
check('imageTag 가 있으면 compose .env 에 BACK_IMAGE·FRONT_IMAGE 를 쓴다', () => {
  const t = envfile.buildComposeEnv(SAMPLE_STATE, { imageTag: '0.2.0-rc9' })
  assert(t.includes('BACK_IMAGE=ghcr.io/tbongkim03/playbook-back:0.2.0-rc9'), t)
  assert(t.includes('FRONT_IMAGE=ghcr.io/tbongkim03/playbook-front:0.2.0-rc9'), t)
  return true
})
check('imageTag 가 없으면 주석으로만 남는다 (compose 기본값 사용)', () => {
  const t = envfile.buildComposeEnv(SAMPLE_STATE)
  assert(!/^BACK_IMAGE=/m.test(t) && !/^FRONT_IMAGE=/m.test(t), t)
  return true
})

console.log('\n[11] 명령 출력 스트리밍')
{
  // compose·migration 은 onLog 로 넘긴다 — 이 출력이 버려지면 pull 실패 원인이 화면에 안 남는다
  const { runStream } = require(path.join(root, 'src/main/services/exec.js'))
  const lines = []
  const r = await runStream(process.execPath, ['-e', "console.log('out-line'); console.error('err-line')"], {
    onLog: (l) => lines.push(l)
  })
  check('runStream 이 onLog 로 stdout·stderr 를 전달한다', () => {
    assert(r.ok, '명령 실패')
    assert(lines.includes('out-line') && lines.includes('err-line'), `받은 줄: ${JSON.stringify(lines)}`)
    return true
  })
}

console.log(`\n결과: ${pass} 통과 / ${fail} 실패`)
if (fail) {
  console.log('\n실패 목록:')
  for (const f of failures) console.log(`  - ${f}`)
  process.exit(1)
}
