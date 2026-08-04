'use strict'

const fs = require('node:fs')
const fsp = require('node:fs/promises')
const path = require('node:path')
const crypto = require('node:crypto')

const { runCapture, runStream } = require('./exec')

/**
 * 9단계 — DB 마이그레이션.
 *
 * ★ 최우선 제약 (사용자 명시)
 *   "DB 테이블을 전부 밀지 않는다. 테이블별로 물어보면서 진행한다."
 *   → DROP / TRUNCATE / DELETE FROM 이 포함된 문장은 **실행 대상에서 제외**하고
 *     경고로만 표시한다. 사용자가 체크해도 실행되지 않는다 (UI 가 아니라 여기서 걸러진다).
 *
 * ★ scripts/migrate-db.sh 를 절대 호출하지 않는다.
 *   이름과 달리 그 스크립트는 마이그레이션 실행기가 아니라 **Dev DB 를 mysqldump 해
 *   Prod DB 를 통째로 덮어쓰는** 도구다(스크립트 자체에 "Prod 데이터가 Dev 데이터로
 *   덮어써집니다" 경고가 있다). 설치 마법사가 호출하면 캠퍼스 운영 DB 가 날아간다.
 *   대신 db/migration/*.sql 을 파일명 순번대로 mysql 에 직접 먹인다.
 *
 * 동작
 *   1) db/migration/*.sql 을 읽어 문장 단위로 분해
 *   2) 문장마다 대상 테이블·종류를 판정, 파괴적 문장을 분리
 *   3) 실행 중인 db 컨테이너의 SHOW TABLES 와 대조해 "미적용" 후보 산출
 *   4) 사용자가 테이블 단위로 체크한 것만 필터링해 mysql 로 파이프
 */

const DB_SERVICE = 'db'

// ── SQL 파서 ────────────────────────────────────────────────────────────────

/** 주석 제거 + 문자열/식별자 인용을 존중하는 세미콜론 분리 */
function splitStatements(sql) {
  const statements = []
  let buf = ''
  let i = 0
  const n = sql.length
  let quote = null // "'" | '"' | '`'

  while (i < n) {
    const c = sql[i]
    const c2 = sql[i + 1]

    if (quote) {
      buf += c
      if (c === '\\' && quote !== '`') {
        // 이스케이프 시퀀스
        if (c2 !== undefined) {
          buf += c2
          i += 2
          continue
        }
      }
      if (c === quote) {
        // '' 형태의 이스케이프된 인용부호
        if (c2 === quote) {
          buf += c2
          i += 2
          continue
        }
        quote = null
      }
      i += 1
      continue
    }

    // 라인 주석
    if ((c === '-' && c2 === '-' && (sql[i + 2] === undefined || /\s/.test(sql[i + 2]))) || c === '#') {
      while (i < n && sql[i] !== '\n') i += 1
      continue
    }
    // 블록 주석
    if (c === '/' && c2 === '*') {
      i += 2
      while (i < n && !(sql[i] === '*' && sql[i + 1] === '/')) i += 1
      i += 2
      continue
    }
    if (c === "'" || c === '"' || c === '`') {
      quote = c
      buf += c
      i += 1
      continue
    }
    if (c === ';') {
      const trimmed = buf.trim()
      if (trimmed) statements.push(trimmed)
      buf = ''
      i += 1
      continue
    }
    buf += c
    i += 1
  }
  const tail = buf.trim()
  if (tail) statements.push(tail)
  return statements
}

const ident = '`?([A-Za-z0-9_$]+)`?'

/**
 * 파괴적 문장 판정 — 보수적으로 넓게 잡는다 (오탐 < 미탐).
 *
 * ★ 판정은 **주석을 걷어낸 실행문**에 대해서만 한다.
 *   마이그레이션 SQL 하단에는 롤백용 DROP 이 주석으로 들어 있는 경우가 많은데,
 *   splitStatements 가 주석을 이미 제거하므로 여기까지 오지 않는다.
 *   (주석 속 DROP 때문에 멀쩡한 파일이 통째로 막히는 오탐을 피하는 지점)
 */
function isDestructive(stmt) {
  const s = stmt.replace(/\s+/g, ' ').trim()
  if (/^DROP\b/i.test(s)) return { destructive: true, reason: 'DROP 문' }
  if (/^TRUNCATE\b/i.test(s)) return { destructive: true, reason: 'TRUNCATE 문' }
  if (/^DELETE\s+FROM\b/i.test(s)) {
    return {
      destructive: true,
      reason: /\bWHERE\b/i.test(s) ? 'DELETE 문 (데이터 삭제)' : 'WHERE 없는 DELETE (전체 삭제)'
    }
  }
  if (/\bDROP\s+(TABLE|DATABASE|SCHEMA|COLUMN|INDEX|KEY|FOREIGN\s+KEY|PRIMARY\s+KEY|CONSTRAINT|PARTITION)\b/i.test(s)) {
    return { destructive: true, reason: 'DROP 절이 포함된 ALTER 문' }
  }
  if (/^RENAME\s+TABLE\b/i.test(s)) return { destructive: true, reason: 'RENAME TABLE (기존 테이블 이동)' }
  return { destructive: false }
}

/** 문장에서 대상 테이블과 종류를 뽑는다 */
function classify(stmt) {
  const s = stmt.replace(/\s+/g, ' ').trim()
  const m = (re) => {
    const r = s.match(re)
    return r ? r[1] : null
  }

  let table = null
  let kind = 'OTHER'

  if (/^CREATE\s+(TEMPORARY\s+)?TABLE\b/i.test(s)) {
    kind = 'CREATE TABLE'
    table = m(new RegExp(`^CREATE\\s+(?:TEMPORARY\\s+)?TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?${ident}`, 'i'))
  } else if (/^ALTER\s+TABLE\b/i.test(s)) {
    kind = 'ALTER TABLE'
    table = m(new RegExp(`^ALTER\\s+TABLE\\s+${ident}`, 'i'))
  } else if (/^CREATE\s+(UNIQUE\s+|FULLTEXT\s+|SPATIAL\s+)?INDEX\b/i.test(s)) {
    kind = 'CREATE INDEX'
    table = m(new RegExp(`\\bON\\s+${ident}`, 'i'))
  } else if (/^INSERT\b/i.test(s)) {
    kind = 'INSERT'
    table = m(new RegExp(`^INSERT\\s+(?:IGNORE\\s+|LOW_PRIORITY\\s+|HIGH_PRIORITY\\s+)?INTO\\s+${ident}`, 'i'))
  } else if (/^REPLACE\b/i.test(s)) {
    kind = 'REPLACE'
    table = m(new RegExp(`^REPLACE\\s+(?:INTO\\s+)?${ident}`, 'i'))
  } else if (/^UPDATE\b/i.test(s)) {
    kind = 'UPDATE'
    table = m(new RegExp(`^UPDATE\\s+${ident}`, 'i'))
  } else if (/^DELETE\b/i.test(s)) {
    kind = 'DELETE'
    table = m(new RegExp(`\\bFROM\\s+${ident}`, 'i'))
  } else if (/^SELECT\b/i.test(s)) {
    kind = 'SELECT'
    table = null // 검증용 조회 — 대상 테이블에 묶지 않는다
  } else if (/^DROP\b/i.test(s)) {
    kind = 'DROP'
    table = m(new RegExp(`^DROP\\s+(?:TABLE|VIEW)\\s+(?:IF\\s+EXISTS\\s+)?${ident}`, 'i'))
  } else if (/^TRUNCATE\b/i.test(s)) {
    kind = 'TRUNCATE'
    table = m(new RegExp(`^TRUNCATE\\s+(?:TABLE\\s+)?${ident}`, 'i'))
  }

  return { kind, table }
}

/** 파일 하나를 파싱해 테이블 그룹으로 묶는다 */
function parseFile(fileName, sql) {
  const statements = splitStatements(sql)
  const groups = new Map() // table -> group
  const excluded = []
  let order = 0

  const groupFor = (table) => {
    const key = table || '__verify__'
    if (!groups.has(key)) {
      groups.set(key, {
        key,
        table: table || null,
        label: table || '(검증용 SELECT · 대상 테이블 없음)',
        statements: [],
        kinds: new Set(),
        createsTable: false,
        order: order++
      })
    }
    return groups.get(key)
  }

  for (const stmt of statements) {
    const { kind, table } = classify(stmt)
    const d = isDestructive(stmt)
    if (d.destructive) {
      excluded.push({
        table: table || null,
        kind,
        reason: d.reason,
        preview: stmt.replace(/\s+/g, ' ').slice(0, 160)
      })
      continue // ★ 실행 대상에서 완전히 제외
    }
    const g = groupFor(table)
    g.statements.push(stmt)
    g.kinds.add(kind)
    if (kind === 'CREATE TABLE') g.createsTable = true
  }

  return {
    file: fileName,
    checksum: crypto.createHash('sha256').update(sql, 'utf8').digest('hex').slice(0, 16),
    totalStatements: statements.length,
    excluded,
    groups: [...groups.values()]
      .sort((a, b) => a.order - b.order)
      .map((g) => ({
        key: g.key,
        table: g.table,
        label: g.label,
        kinds: [...g.kinds],
        statementCount: g.statements.length,
        createsTable: g.createsTable,
        statements: g.statements
      }))
  }
}

// ── DB 상태 조회 ─────────────────────────────────────────────────────────────

function mysqlExecArgs(dbEnv, extra) {
  // 비밀번호를 argv 에 노출하지 않으려고 MYSQL_PWD 환경변수를 쓴다.
  return ['exec', '-i', '-e', `MYSQL_PWD=${dbEnv.password}`, DB_SERVICE, 'mysql', '-u', dbEnv.user, ...extra]
}

/** db/.env.prod 에서 접속 정보를 읽는다 (파일이 정본) */
async function readDbEnv(installDir) {
  const p = path.join(installDir, 'db', '.env.prod')
  if (!fs.existsSync(p)) {
    return { ok: false, message: `${p} 을(를) 찾을 수 없습니다. 8단계를 먼저 완료하세요.` }
  }
  const text = await fsp.readFile(p, 'utf8')
  const get = (k) => {
    const m = text.match(new RegExp(`^${k}=(.*)$`, 'm'))
    return m ? m[1].trim() : ''
  }
  const database = get('MYSQL_DATABASE')
  const user = get('MYSQL_USER')
  const password = get('MYSQL_PASSWORD')
  if (!database || !user || !password) {
    return { ok: false, message: 'db/.env.prod 에서 MYSQL_DATABASE/USER/PASSWORD 를 읽지 못했습니다.' }
  }
  return { ok: true, database, user, password }
}

async function listExistingTables(installDir, dbEnv) {
  const r = await runCapture(
    'docker',
    mysqlExecArgs(dbEnv, ['-N', '-B', '-D', dbEnv.database, '-e', 'SHOW TABLES;']),
    { cwd: installDir, timeout: 60000 }
  )
  if (!r.ok) {
    return { ok: false, message: (r.stderr || r.stdout || '').split('\n').filter(Boolean).slice(-3).join(' ') }
  }
  return {
    ok: true,
    tables: r.stdout
      .split('\n')
      .map((s) => s.trim())
      .filter(Boolean)
  }
}

async function countRows(installDir, dbEnv, table) {
  const r = await runCapture(
    'docker',
    mysqlExecArgs(dbEnv, ['-N', '-B', '-D', dbEnv.database, '-e', `SELECT COUNT(*) FROM \`${table}\`;`]),
    { cwd: installDir, timeout: 30000 }
  )
  if (!r.ok) return null
  const n = Number(r.stdout.trim())
  return Number.isFinite(n) ? n : null
}

// ── 스캔 ─────────────────────────────────────────────────────────────────────

/**
 * 설치 경로의 db/migration/*.sql 을 스캔해 적용 상태를 판정한다.
 * @param appliedLedger 마법사가 기록해 둔 적용 이력 { [file]: { checksum, appliedAt } }
 */
async function scan(installDir, appliedLedger = {}) {
  const migDir = path.join(installDir, 'db', 'migration')
  if (!fs.existsSync(migDir)) {
    return { ok: false, message: `${migDir} 이(가) 없습니다. 8단계(배포 파일 전개)를 먼저 완료하세요.` }
  }

  const dbEnv = await readDbEnv(installDir)
  if (!dbEnv.ok) return { ok: false, message: dbEnv.message }

  const existing = await listExistingTables(installDir, dbEnv)
  if (!existing.ok) {
    return {
      ok: false,
      message:
        `DB 컨테이너에 접속하지 못했습니다: ${existing.message}\n` +
        '8단계에서 컨테이너가 정상 기동했는지 확인하세요. (docker compose ps 의 db 가 healthy 여야 합니다)'
    }
  }
  const existingSet = new Set(existing.tables.map((t) => t.toLowerCase()))

  const names = (await fsp.readdir(migDir)).filter((f) => f.toLowerCase().endsWith('.sql')).sort()

  const files = []
  for (const name of names) {
    const sql = await fsp.readFile(path.join(migDir, name), 'utf8')
    const parsed = parseFile(name, sql)
    const ledger = appliedLedger[name]

    const groups = []
    for (const g of parsed.groups) {
      const tableExists = g.table ? existingSet.has(g.table.toLowerCase()) : null
      let rows = null
      if (g.table && tableExists) rows = await countRows(installDir, dbEnv, g.table)

      // 안전 기본값: 테이블이 없을 때만 기본 체크. 이미 있으면 사용자가 직접 켜야 한다.
      const recommended = g.table ? !tableExists : false

      groups.push({
        key: g.key,
        table: g.table,
        label: g.label,
        kinds: g.kinds,
        statementCount: g.statementCount,
        createsTable: g.createsTable,
        tableExists,
        rowCount: rows,
        recommended,
        note: g.table
          ? tableExists
            ? `이미 존재하는 테이블입니다${rows !== null ? ` (${rows}행)` : ''}. 중복 적용하면 오류가 나거나 데이터가 바뀔 수 있습니다.`
            : '아직 없는 테이블입니다. 적용을 권장합니다.'
          : '적용 결과를 확인하는 조회문입니다. 데이터를 바꾸지 않습니다.'
      })
    }

    const createTargets = parsed.groups.filter((g) => g.createsTable).map((g) => g.table)
    const allCreatedExist = createTargets.length > 0 && createTargets.every((t) => existingSet.has(String(t).toLowerCase()))

    let status = 'pending'
    if (ledger && ledger.checksum === parsed.checksum) status = 'applied'
    else if (ledger) status = 'changed'
    else if (allCreatedExist) status = 'likely-applied'

    files.push({
      file: name,
      checksum: parsed.checksum,
      status,
      statusLabel: {
        applied: '이 마법사로 적용 완료',
        changed: '적용 후 SQL 파일이 변경됨 — 내용 확인 필요',
        'likely-applied': '이미 적용된 것으로 보임 (대상 테이블이 모두 존재)',
        pending: '미적용'
      }[status],
      appliedAt: ledger ? ledger.appliedAt : null,
      totalStatements: parsed.totalStatements,
      excluded: parsed.excluded,
      groups
    })
  }

  return {
    ok: true,
    scannedAt: new Date().toISOString(),
    database: dbEnv.database,
    existingTables: existing.tables,
    files,
    excludedTotal: files.reduce((a, f) => a + f.excluded.length, 0)
  }
}

// ── 적용 ─────────────────────────────────────────────────────────────────────

/**
 * 선택된 그룹만 실행한다.
 * @param selections [{ file, groupKeys: [] }]
 */
async function apply(installDir, selections, { onLog, secretValues = [] }) {
  const dbEnv = await readDbEnv(installDir)
  if (!dbEnv.ok) return { ok: false, message: dbEnv.message, results: [] }

  const migDir = path.join(installDir, 'db', 'migration')
  const results = []

  for (const sel of selections) {
    const filePath = path.join(migDir, sel.file)
    if (!fs.existsSync(filePath)) {
      results.push({ file: sel.file, ok: false, message: '파일을 찾을 수 없습니다.' })
      continue
    }
    const sql = await fsp.readFile(filePath, 'utf8')
    const parsed = parseFile(sel.file, sql)

    const wanted = new Set(sel.groupKeys || [])
    const chosen = parsed.groups.filter((g) => wanted.has(g.key))

    if (chosen.length === 0) {
      results.push({ file: sel.file, ok: true, skipped: true, message: '선택된 항목이 없어 건너뜁니다.' })
      continue
    }

    // ★ 이 시점의 statements 에는 파괴적 문장이 이미 들어 있지 않다 (parseFile 에서 제외됨)
    const body = chosen
      .map((g) => `-- [${sel.file}] ${g.label}\n${g.statements.map((s) => `${s};`).join('\n')}`)
      .join('\n\n')

    const script =
      `-- 설치 마법사 생성 스크립트 (DROP/TRUNCATE 제외)\n` +
      `SET SESSION sql_mode = '';\n` +
      `${body}\n`

    onLog(`─── ${sel.file} 적용 시작 (${chosen.length}개 항목, ${chosen.reduce((a, g) => a + g.statements.length, 0)}개 문장)`)
    if (parsed.excluded.length) {
      onLog(`※ 파괴적 문장 ${parsed.excluded.length}개는 실행하지 않습니다: ${parsed.excluded.map((e) => e.kind).join(', ')}`)
    }

    const r = await runStream(
      'docker',
      mysqlExecArgs(dbEnv, ['--show-warnings', '-D', dbEnv.database]),
      {
        cwd: installDir,
        input: script,
        onLog,
        secretValues: [...secretValues, dbEnv.password],
        timeout: 15 * 60 * 1000
      }
    )

    if (r.ok) {
      onLog(`✔ ${sel.file} 적용 완료`)
      results.push({
        file: sel.file,
        ok: true,
        checksum: parsed.checksum,
        groupKeys: chosen.map((g) => g.key),
        appliedAt: new Date().toISOString()
      })
    } else {
      onLog(`✘ ${sel.file} 적용 실패 — 아래 오류를 확인하세요.`)
      results.push({
        file: sel.file,
        ok: false,
        message:
          (r.stderr || '').trim() ||
          'mysql 실행이 실패했습니다. 이미 적용된 마이그레이션을 다시 실행했을 가능성이 큽니다.'
      })
    }
  }

  return { ok: results.every((r) => r.ok), results }
}

module.exports = { scan, apply, parseFile, splitStatements, isDestructive, classify, readDbEnv }
