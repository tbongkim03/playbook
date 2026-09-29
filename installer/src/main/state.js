'use strict'

const fs = require('node:fs')
const fsp = require('node:fs/promises')
const path = require('node:path')
const os = require('node:os')

const { maskValue } = require('./util/mask')

/**
 * 마법사 상태 저장소 — 메인 프로세스에만 존재한다.
 *
 * 보안 원칙
 *  - 시크릿(SECRET_PATHS)은 **렌더러로 절대 내보내지 않는다.**
 *    렌더러에는 { set: true, masked: 'ab••••wxyz' } 형태의 투영만 간다.
 *  - 사용자가 명시적으로 요청할 때만(2단계 "값 보기", 백업 파일 저장) 평문을 노출한다.
 *  - 상태 파일은 0600 으로 쓴다 (POSIX). Windows 는 파일 모드가 사실상 무시되므로
 *    사용자 프로필 하위(userData)에 두어 다른 사용자 계정으로부터 격리한다.
 */

const SECRET_PATHS = new Set([
  'generated.dbPassword',
  'generated.dbRootPassword',
  'generated.integrationSecretKey',
  'apiKeys.kakaoRestApiKey',
  'apiKeys.nlApiKey',
  'apiKeys.work24ApiKey',
  'discord.botToken',
  'master.pw',
  'monitoring.bootstrapSecret'
])

const STATE_VERSION = 2

function defaultState() {
  return {
    version: STATE_VERSION,
    updatedAt: null,
    currentStep: 1,
    maxVisitedStep: 1,
    installDir: process.platform === 'win32' ? 'C:\\Playbook' : path.join(os.homedir(), 'playbook-prod'),

    environment: { lastResult: null },

    campus: { name: '', envSlot: '', campusId: null, custom: false },

    generated: {
      dbPassword: '',
      dbRootPassword: '',
      integrationSecretKey: '',
      generatedAt: null,
      backupSavedTo: null
    },

    apiKeys: {
      kakaoRestApiKey: '',
      nlApiKey: '',
      work24ApiKey: ''
    },
    apiVerify: { kakao: null, nl: null, work24: null },

    discord: {
      botToken: '',
      applicationId: '',
      botName: '',
      tokenVerifiedAt: null,
      inviteUrl: '',
      guildId: '',
      guildName: '',
      linkChannelId: '',
      campusChannelId: '',
      campusRoleId: '',
      provisionedAt: null,
      skipped: false
    },

    master: { id: '', pw: '', name: '', discord: '' },

    ipAllowlist: { detected: null, entries: [], acknowledgedEmpty: false },

    monitoring: {
      enabled: false,
      endpoint: 'https://monitoring.tbongkim.com',
      campusLabel: '',
      bootstrapSecret: ''
    },

    deploy: {
      payloadStagedAt: null,
      envWrittenAt: null,
      backEnvPath: null,
      dbEnvPath: null,
      monitoringEnvPath: null,
      pulledAt: null,
      // db 컨테이너만 먼저 올린 시각 (마이그레이션 전)
      dbUppedAt: null,
      // 나머지 서비스까지 기동한 시각 (마이그레이션 후)
      uppedAt: null
    },

    migration: { applied: {}, lastScanAt: null },

    // 초기 도서 데이터(엑셀) — 선택 항목이다. 파일 경로는 시크릿이 아니라 평문으로 둔다.
    bookSeed: {
      filePath: null,
      importedAt: null,
      inserted: 0,
      updated: 0,
      skipped: 0,
      errorCount: 0
    },

    finish: { healthAt: null, shortcutPath: null, backupPath: null },

    // 설치 후 업데이트 — currentTag 는 마지막으로 확인·적용한 이미지 버전
    update: { lastCheckAt: null, currentTag: null, history: [] }
  }
}

function getIn(obj, dotted) {
  return dotted.split('.').reduce((acc, k) => (acc == null ? acc : acc[k]), obj)
}

function setIn(obj, dotted, value) {
  const keys = dotted.split('.')
  const last = keys.pop()
  let cur = obj
  for (const k of keys) {
    if (typeof cur[k] !== 'object' || cur[k] === null) cur[k] = {}
    cur = cur[k]
  }
  cur[last] = value
}

/** 깊은 병합 (배열은 통째로 교체) */
function deepMerge(base, patch) {
  for (const [k, v] of Object.entries(patch || {})) {
    if (v && typeof v === 'object' && !Array.isArray(v)) {
      if (!base[k] || typeof base[k] !== 'object' || Array.isArray(base[k])) base[k] = {}
      deepMerge(base[k], v)
    } else {
      base[k] = v
    }
  }
  return base
}

class WizardState {
  constructor(filePath) {
    this.filePath = filePath
    this.data = defaultState()
    this._writeQueue = Promise.resolve()
  }

  load() {
    try {
      if (!fs.existsSync(this.filePath)) return { restored: false }
      const raw = fs.readFileSync(this.filePath, 'utf8')
      const parsed = JSON.parse(raw)
      if (parsed.version !== STATE_VERSION) {
        // 구버전 상태는 값만 최대한 살려 병합 (필드 추가에 견디게)
        this.data = deepMerge(defaultState(), parsed)
        this.data.version = STATE_VERSION
      } else {
        this.data = deepMerge(defaultState(), parsed)
      }
      return { restored: true, updatedAt: this.data.updatedAt }
    } catch (e) {
      // 손상된 상태 파일 때문에 마법사가 아예 못 뜨는 상황을 막는다
      this.data = defaultState()
      return { restored: false, error: e.message }
    }
  }

  /** 직렬화 순서를 보장하는 원자적 저장 */
  save() {
    this.data.updatedAt = new Date().toISOString()
    const payload = JSON.stringify(this.data, null, 2)
    this._writeQueue = this._writeQueue.then(async () => {
      const dir = path.dirname(this.filePath)
      await fsp.mkdir(dir, { recursive: true })
      const tmp = `${this.filePath}.tmp`
      await fsp.writeFile(tmp, payload, { encoding: 'utf8', mode: 0o600 })
      await fsp.rename(tmp, this.filePath)
      try {
        await fsp.chmod(this.filePath, 0o600)
      } catch {
        /* Windows 는 chmod 가 사실상 무의미 — 무시 */
      }
    })
    return this._writeQueue
  }

  async reset() {
    this.data = defaultState()
    await this.save()
  }

  /** 메인 프로세스 내부용 — 평문 접근 */
  get(dotted) {
    return getIn(this.data, dotted)
  }

  async set(dotted, value) {
    setIn(this.data, dotted, value)
    await this.save()
    return true
  }

  async patch(patchObj) {
    // 시크릿 경로는 patch 로 들어올 수 없게 막는다 (반드시 setSecret 경유)
    const rejected = []
    const walk = (obj, prefix) => {
      for (const [k, v] of Object.entries(obj || {})) {
        const p = prefix ? `${prefix}.${k}` : k
        if (SECRET_PATHS.has(p)) {
          rejected.push(p)
          delete obj[k]
          continue
        }
        if (v && typeof v === 'object' && !Array.isArray(v)) walk(v, p)
      }
    }
    const clone = JSON.parse(JSON.stringify(patchObj || {}))
    walk(clone, '')
    deepMerge(this.data, clone)
    await this.save()
    return { ok: true, rejectedSecretPaths: rejected }
  }

  async setSecret(dotted, value) {
    if (!SECRET_PATHS.has(dotted)) {
      throw new Error(`시크릿 경로가 아닙니다: ${dotted}`)
    }
    setIn(this.data, dotted, value == null ? '' : String(value))
    await this.save()
    return { ok: true, set: !!value, masked: maskValue(value) }
  }

  /** 모든 시크릿 평문값 목록 — 로그 스크러빙용 */
  secretValues() {
    return [...SECRET_PATHS].map((p) => getIn(this.data, p)).filter((v) => typeof v === 'string' && v.length >= 6)
  }

  /**
   * 렌더러로 보낼 안전한 투영.
   * 시크릿은 { set, masked } 로 대체된다.
   */
  projection() {
    const clone = JSON.parse(JSON.stringify(this.data))
    for (const p of SECRET_PATHS) {
      const v = getIn(this.data, p)
      setIn(clone, p, { __secret: true, set: !!v, masked: maskValue(v) })
    }
    clone.__stateFile = this.filePath
    return clone
  }
}

module.exports = { WizardState, SECRET_PATHS, defaultState, STATE_VERSION }
