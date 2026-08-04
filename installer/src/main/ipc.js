'use strict'

const { ipcMain, shell, dialog, app } = require('electron')
const path = require('node:path')

const environment = require('./services/environment')
const apikeys = require('./services/apikeys')
const discordSvc = require('./services/discord')
const network = require('./services/network')
const envfile = require('./services/envfile')
const composeSvc = require('./services/compose')
const migrationSvc = require('./services/migration')
const finishSvc = require('./services/finish')
const master = require('./services/master')
const bookseed = require('./services/bookseed')
const { generateDbPassword, generateIntegrationSecretKey } = require('./util/secrets')
const { maskValue } = require('./util/mask')

/**
 * IPC 표면 — 렌더러가 할 수 있는 일의 전부다.
 *
 * 원칙
 *  1) 시크릿 평문은 이 경계를 **넘어가지 않는다.**
 *     예외는 사용자가 명시적으로 누른 두 가지뿐: secrets:reveal, backup:save
 *  2) 외부 API 호출·파일 쓰기·docker 실행은 전부 여기(메인)에서 한다.
 *  3) 모든 핸들러는 throw 하지 않고 { ok, ... } 를 돌려준다 — 렌더러에서 스택이 새지 않게.
 */

// 외부 브라우저로 열어도 되는 도메인 화이트리스트
const ALLOWED_EXTERNAL_HOSTS = new Set([
  'discord.com',
  'developers.naver.com',
  'www.nl.go.kr',
  'nl.go.kr',
  'www.work24.go.kr',
  'work24.go.kr',
  'www.docker.com',
  'docs.docker.com',
  'learn.microsoft.com',
  'localhost'
])

function safeHandler(fn) {
  return async (_event, ...args) => {
    try {
      return await fn(...args)
    } catch (e) {
      return { ok: false, message: e && e.message ? e.message : String(e), unexpected: true }
    }
  }
}

function register({ state, getWindow }) {
  const send = (channel, payload) => {
    const win = getWindow()
    if (win && !win.isDestroyed()) win.webContents.send(channel, payload)
  }

  const logger = (channel) => (line, stream) => send(channel, { line, stream: stream || 'stdout', at: Date.now() })

  // ── 상태 ────────────────────────────────────────────────────────────────
  ipcMain.handle('state:get', safeHandler(async () => ({ ok: true, state: state.projection() })))

  ipcMain.handle(
    'state:patch',
    safeHandler(async (patch) => {
      const r = await state.patch(patch)
      return { ok: true, state: state.projection(), rejectedSecretPaths: r.rejectedSecretPaths }
    })
  )

  ipcMain.handle(
    'state:setSecret',
    safeHandler(async (dotted, value) => {
      const r = await state.setSecret(dotted, value)
      return { ...r, state: state.projection() }
    })
  )

  ipcMain.handle(
    'state:reset',
    safeHandler(async () => {
      await state.reset()
      return { ok: true, state: state.projection() }
    })
  )

  ipcMain.handle('state:filePath', safeHandler(async () => ({ ok: true, path: state.filePath })))

  // ── 외부 링크 / 폴더 ─────────────────────────────────────────────────────
  ipcMain.handle(
    'shell:openExternal',
    safeHandler(async (url) => {
      let parsed
      try {
        parsed = new URL(url)
      } catch {
        return { ok: false, message: '올바른 URL 이 아닙니다.' }
      }
      if (!['http:', 'https:'].includes(parsed.protocol)) {
        return { ok: false, message: 'http/https 링크만 열 수 있습니다.' }
      }
      if (!ALLOWED_EXTERNAL_HOSTS.has(parsed.hostname)) {
        return { ok: false, message: `허용되지 않은 도메인입니다: ${parsed.hostname}` }
      }
      await shell.openExternal(parsed.toString())
      return { ok: true }
    })
  )

  ipcMain.handle(
    'shell:showItemInFolder',
    safeHandler(async (target) => {
      shell.showItemInFolder(path.resolve(target))
      return { ok: true }
    })
  )

  ipcMain.handle(
    'dialog:pickDirectory',
    safeHandler(async (defaultPath) => {
      const win = getWindow()
      const r = await dialog.showOpenDialog(win, {
        title: '설치 경로 선택',
        defaultPath: defaultPath || undefined,
        properties: ['openDirectory', 'createDirectory']
      })
      if (r.canceled || !r.filePaths.length) return { ok: false, canceled: true }
      return { ok: true, path: r.filePaths[0] }
    })
  )

  // ── 1단계: 환경 점검 ─────────────────────────────────────────────────────
  ipcMain.handle(
    'env:check',
    safeHandler(async () => {
      const result = await environment.runAll(state.get('installDir'))
      await state.set('environment.lastResult', result)
      return { ok: true, result }
    })
  )

  // ── 2단계: 시크릿 자동 생성 ──────────────────────────────────────────────
  ipcMain.handle(
    'secrets:generate',
    safeHandler(async ({ force = false } = {}) => {
      const existing = state.get('generated.integrationSecretKey')
      if (existing && !force) {
        return { ok: true, regenerated: false, state: state.projection() }
      }
      await state.setSecret('generated.dbPassword', generateDbPassword())
      await state.setSecret('generated.dbRootPassword', generateDbPassword())
      await state.setSecret('generated.integrationSecretKey', generateIntegrationSecretKey())
      await state.set('generated.generatedAt', new Date().toISOString())
      return { ok: true, regenerated: true, state: state.projection() }
    })
  )

  /**
   * 생성된 시크릿 평문 노출.
   * 요구사항이 "생성된 값을 화면에 보여준다" 이므로 명시적 클릭에서만 열어 준다.
   * 렌더러는 이 값을 저장하지 않고 표시만 한다.
   */
  ipcMain.handle(
    'secrets:reveal',
    safeHandler(async () => ({
      ok: true,
      values: {
        dbPassword: state.get('generated.dbPassword'),
        dbRootPassword: state.get('generated.dbRootPassword'),
        integrationSecretKey: state.get('generated.integrationSecretKey')
      }
    }))
  )

  ipcMain.handle(
    'campus:presets',
    safeHandler(async () => ({ ok: true, presets: envfile.CAMPUS_PRESETS }))
  )

  // ── 3단계: 외부 API 실검증 ───────────────────────────────────────────────
  ipcMain.handle(
    'apikey:verify',
    safeHandler(async (which) => {
      let result
      if (which === 'naver') {
        result = await apikeys.verifyNaver({
          clientId: state.get('apiKeys.naverClientId'),
          clientSecret: state.get('apiKeys.naverClientSecret')
        })
      } else if (which === 'nl') {
        result = await apikeys.verifyNl({ apiKey: state.get('apiKeys.nlApiKey') })
      } else if (which === 'work24') {
        result = await apikeys.verifyWork24({ apiKey: state.get('apiKeys.work24ApiKey') })
      } else {
        return { ok: false, message: `알 수 없는 검증 대상: ${which}` }
      }
      const record = { ok: result.ok, message: result.message, at: new Date().toISOString() }
      await state.set(`apiVerify.${which}`, record)
      return { ok: true, result: record }
    })
  )

  ipcMain.handle('apikey:issueUrls', safeHandler(async () => ({ ok: true, urls: apikeys.ISSUE_URLS })))

  // ── 4단계: 디스코드 ──────────────────────────────────────────────────────
  ipcMain.handle(
    'discord:portalUrl',
    safeHandler(async () => ({ ok: true, url: discordSvc.PORTAL_URL, permissions: discordSvc.permissionInteger() }))
  )

  ipcMain.handle(
    'discord:verifyToken',
    safeHandler(async () => {
      const token = state.get('discord.botToken')
      const r = await discordSvc.verifyToken(token)
      if (r.ok) {
        // 사용자가 앞뒤 공백/"Bot " 접두사를 붙여 넣었을 수 있으니 정규화해서 다시 저장
        await state.setSecret('discord.botToken', String(token).trim().replace(/^Bot\s+/i, ''))
        await state.patch({
          discord: {
            applicationId: r.applicationId,
            botName: r.botName,
            inviteUrl: r.inviteUrl,
            tokenVerifiedAt: new Date().toISOString()
          }
        })
      }
      return {
        ok: r.ok,
        message: r.message,
        botName: r.botName,
        applicationId: r.applicationId,
        inviteUrl: r.inviteUrl,
        state: state.projection()
      }
    })
  )

  ipcMain.handle(
    'discord:listGuilds',
    safeHandler(async () => {
      const token = state.get('discord.botToken')
      if (!token) return { ok: false, message: '먼저 봇 토큰을 검증하세요.' }
      const r = await discordSvc.listGuilds(token)
      if (!r.ok) return r
      if (r.guilds.length === 0) {
        return {
          ok: true,
          guilds: [],
          message:
            '봇이 아직 어떤 서버에도 들어가 있지 않습니다. [초대 링크 열기] 로 서버에 초대한 뒤 다시 조회하세요.'
        }
      }
      const lacking = r.guilds.filter((g) => !g.hasAllPermissions)
      return {
        ok: true,
        guilds: r.guilds,
        message:
          lacking.length === 0
            ? `서버 ${r.guilds.length}개를 찾았습니다. 권한도 모두 확인했습니다.`
            : `서버 ${r.guilds.length}개를 찾았습니다. 그중 ${lacking.length}개는 권한이 부족합니다.`
      }
    })
  )

  // 초대 승인만으로는 권한이 들어왔는지 알 수 없다. 실제 값을 확인해
  // ④ 에서 403 을 만나기 전에(그리고 런타임 역할 부여가 조용히 실패하기 전에) 잡는다.
  ipcMain.handle(
    'discord:checkPermissions',
    safeHandler(async ({ guildId } = {}) => {
      const token = state.get('discord.botToken')
      if (!token) return { ok: false, message: '먼저 봇 토큰을 검증하세요.' }
      if (!guildId) return { ok: false, message: '먼저 대상 서버를 선택하세요.' }
      return discordSvc.checkGuildPermissions(token, guildId)
    })
  )

  ipcMain.handle(
    'discord:provision',
    safeHandler(async ({ guildId, guildName, linkChannelName } = {}) => {
      const token = state.get('discord.botToken')
      if (!token) return { ok: false, message: '먼저 봇 토큰을 검증하세요.' }
      const campusName = state.get('campus.name')
      if (!campusName) return { ok: false, message: '2단계에서 캠퍼스를 먼저 지정하세요.' }
      if (!guildId) return { ok: false, message: '대상 서버를 선택하세요.' }

      const onLog = (line) => send('discord:log', { line, at: Date.now() })
      const r = await discordSvc.provisionGuild(token, { guildId, campusName, linkChannelName }, onLog)
      if (r.ok) {
        await state.patch({
          discord: {
            guildId,
            guildName: guildName || '',
            linkChannelId: r.linkChannelId,
            campusChannelId: r.campusChannelId,
            campusRoleId: r.campusRoleId,
            provisionedAt: new Date().toISOString(),
            skipped: false
          }
        })
      }
      return { ...r, state: state.projection() }
    })
  )

  ipcMain.handle(
    'discord:skip',
    safeHandler(async (skip) => {
      await state.patch({ discord: { skipped: !!skip } })
      return { ok: true, state: state.projection() }
    })
  )

  // ── 5단계: 마스터 계정 검증 ──────────────────────────────────────────────
  ipcMain.handle(
    'master:validate',
    safeHandler(async () => {
      const r = master.validateAll({
        id: state.get('master.id'),
        pw: state.get('master.pw'),
        name: state.get('master.name'),
        discord: state.get('master.discord')
      })
      return { ok: true, result: r }
    })
  )

  // ── 6단계: IP 허용목록 ───────────────────────────────────────────────────
  ipcMain.handle(
    'ip:detect',
    safeHandler(async () => {
      const detected = network.detect()
      await state.set('ipAllowlist.detected', detected)
      return { ok: true, detected }
    })
  )

  ipcMain.handle('ip:validate', safeHandler(async (value) => ({ ok: true, result: network.validateEntry(value) })))

  // ── 7단계: 모니터링 (env 키 확인용) ──────────────────────────────────────
  ipcMain.handle(
    'monitoring:envKeys',
    safeHandler(async () => ({
      ok: true,
      keys: [
        'MONITORING_REFRESH_ENABLED',
        'MONITORING_TOKEN_ENDPOINT',
        'MONITORING_CAMPUS',
        'MONITORING_BOOTSTRAP_SECRET',
        'MONITORING_ACCESS_TOKEN',
        'MONITORING_TOKEN_FILE',
        'MONITORING_REMOTE_WRITE_URL',
        'MONITORING_SCRAPE_TARGET',
        'MONITORING_SCRAPE_INTERVAL',
        'MANAGEMENT_PORT'
      ]
    }))
  )

  // ── 8단계: 요약 · 배포 ───────────────────────────────────────────────────
  ipcMain.handle('deploy:summary', safeHandler(async () => ({ ok: true, summary: envfile.summarize(state.data) })))

  ipcMain.handle(
    'deploy:run',
    safeHandler(async () => {
      const onLog = logger('deploy:log')
      const secretValues = state.secretValues()
      const installDir = state.get('installDir')
      if (!installDir) return { ok: false, message: '설치 경로가 지정되지 않았습니다.' }
      // alloy 는 compose profiles: [monitoring] 뒤에 있다 — 모니터링을 쓸 때만 프로파일을 켠다
      const profiles = composeSvc.profilesFor(state.get('monitoring.enabled'))

      onLog('[1/5] 배포 파일을 설치 경로로 복사합니다…')
      const staged = await composeSvc.stagePayload(app, installDir, (l) => onLog(l))
      if (!staged.ok) return staged
      await state.set('deploy.payloadStagedAt', new Date().toISOString())

      onLog('[2/5] 환경설정 파일(.env.prod 2종)을 생성합니다…')
      const written = await envfile.writeEnvFiles(state.data, installDir)
      await state.patch({
        deploy: {
          envWrittenAt: new Date().toISOString(),
          backEnvPath: written.backPath,
          dbEnvPath: written.dbPath,
          monitoringEnvPath: written.monitoringPath
        }
      })
      onLog(`  · ${written.backPath}`)
      onLog(`  · ${written.dbPath}`)
      onLog(`  · ${written.composePath} (compose 프로파일 — 시크릿 없음)`)
      if (written.monitoringPath) {
        onLog(`  · ${written.monitoringPath} (back·alloy 공용 모니터링 설정)`)
      }
      onLog('  (두 파일에는 비밀번호가 평문으로 들어 있습니다. 폴더 접근 권한을 확인하세요)')

      onLog('[3/5] compose 파일을 검증합니다…')
      const valid = await composeSvc.validateCompose(installDir, profiles)
      if (!valid.ok) {
        onLog(`compose 검증 실패: ${valid.message}`)
        return { ok: false, message: `docker-compose.prod.yml 검증 실패: ${valid.message}` }
      }
      onLog('  · 유효합니다.')

      onLog('[4/5] 컨테이너 이미지를 내려받습니다. 회선 속도에 따라 수 분 걸립니다…')
      const pulled = await composeSvc.pull(installDir, { onLog, secretValues, profiles })
      if (!pulled.ok) {
        return { ok: false, message: '이미지 내려받기 실패. 위 로그를 확인하세요.', stage: 'pull' }
      }
      await state.set('deploy.pulledAt', new Date().toISOString())

      // ★ 여기서 데이터베이스만 올린다.
      //   운영 백엔드는 ddl-auto=validate 라, 마이그레이션이 적용되지 않은 스키마에서는
      //   기동 자체가 실패한다. 그래서 순서가 db 기동 → 마이그레이션 → 나머지 서비스 다.
      onLog('[5/5] 데이터베이스 컨테이너만 먼저 기동합니다…')
      onLog('      (백엔드는 스키마 검증 모드로 동작하므로, 마이그레이션을 적용한 뒤 9단계에서 기동합니다)')
      const dbUp = await composeSvc.up(installDir, { onLog, secretValues, services: ['db'], profiles })
      if (!dbUp.ok) {
        return { ok: false, message: '데이터베이스 컨테이너 기동 실패. 위 로그를 확인하세요.', stage: 'db-up' }
      }

      onLog('데이터베이스가 접속을 받을 때까지 기다립니다…')
      const ready = await composeSvc.waitForDb(installDir, { onLog })
      if (!ready.ok) {
        return { ok: false, message: ready.message, stage: 'db-wait' }
      }
      onLog('데이터베이스 준비 완료.')
      await state.set('deploy.dbUppedAt', new Date().toISOString())

      onLog('')
      onLog('여기까지가 1/2 단계입니다. 9단계에서 마이그레이션을 적용한 뒤 나머지 서비스를 기동합니다.')
      const status = await composeSvc.ps(installDir, profiles)
      return { ok: true, phase: 'db-only', services: status.services, state: state.projection() }
    })
  )

  /**
   * 9단계 후반 — 마이그레이션이 끝난 뒤 나머지 서비스(back/front/redis/alloy)를 기동한다.
   * 이 순서를 지키지 않으면 백엔드가 스키마 검증에서 실패해 기동하지 못한다.
   */
  ipcMain.handle(
    'deploy:startServices',
    safeHandler(async () => {
      const onLog = logger('deploy:log')
      const installDir = state.get('installDir')
      if (!installDir) return { ok: false, message: '설치 경로가 지정되지 않았습니다.' }
      if (!state.get('deploy.envWrittenAt')) {
        return { ok: false, message: '8단계(설정 파일 생성·DB 기동)를 먼저 완료하세요.' }
      }

      const profiles = composeSvc.profilesFor(state.get('monitoring.enabled'))
      onLog('나머지 서비스를 기동합니다 (백엔드 · 프론트 · Redis' + (profiles.length ? ' · 모니터링 에이전트' : '') + ')…')
      const r = await composeSvc.up(installDir, { onLog, secretValues: state.secretValues(), profiles })
      if (!r.ok) {
        onLog('')
        onLog('기동에 실패했습니다. 가장 흔한 원인은 스키마 불일치입니다.')
        onLog('`docker compose logs back` 으로 백엔드 로그를 확인하세요.')
        return { ok: false, message: '서비스 기동 실패. 위 로그를 확인하세요.' }
      }
      await state.set('deploy.uppedAt', new Date().toISOString())
      onLog('')
      onLog('기동 완료. 백엔드가 초기화를 마칠 때까지 1~3분 걸릴 수 있습니다.')
      const status = await composeSvc.ps(installDir)
      return { ok: true, services: status.services, state: state.projection() }
    })
  )

  // ── 초기 도서 데이터(엑셀) 주입 ─────────────────────────────────
  // 서비스 기동 후에만 가능하다. 캠퍼스는 백엔드가 기동하며 만들고,
  // 도서 등록 규칙(중분류 매핑·길이 검증 등)은 백엔드 API 가 갖고 있다.
  ipcMain.handle(
    'books:pickFile',
    safeHandler(async () => {
      const win = getWindow()
      const r = await dialog.showOpenDialog(win, {
        title: '초기 도서 데이터 엑셀 선택',
        properties: ['openFile'],
        filters: [{ name: '엑셀 파일', extensions: ['xlsx', 'xls'] }]
      })
      if (r.canceled || !r.filePaths.length) return { ok: false, canceled: true }
      await state.set('bookSeed.filePath', r.filePaths[0])
      return { ok: true, filePath: r.filePaths[0], state: state.projection() }
    })
  )

  ipcMain.handle(
    'books:import',
    safeHandler(async () => {
      const onLog = logger('deploy:log')
      const filePath = state.get('bookSeed.filePath')
      if (!filePath) return { ok: false, message: '먼저 엑셀 파일을 선택하세요.' }
      if (!state.get('deploy.uppedAt')) {
        return { ok: false, message: '서비스를 먼저 기동하세요 (9단계의 [백엔드·프론트 기동]).' }
      }

      const r = await bookseed.importBooks(filePath, {
        id: state.get('master.id'),
        pw: state.get('master.pw')
      }, { onLog })

      if (r.ok) {
        await state.set('bookSeed.importedAt', new Date().toISOString())
        await state.set('bookSeed.inserted', r.inserted)
        await state.set('bookSeed.updated', r.updated)
        await state.set('bookSeed.skipped', r.skipped)
        await state.set('bookSeed.errorCount', (r.errors || []).length)
      }
      return { ...r, state: state.projection() }
    })
  )

  ipcMain.handle(
    'deploy:ps',
    safeHandler(async () =>
      composeSvc.ps(state.get('installDir'), composeSvc.profilesFor(state.get('monitoring.enabled')))
    )
  )

  ipcMain.handle(
    'deploy:logs',
    safeHandler(async (service, tail) =>
      composeSvc.logs(
        state.get('installDir'),
        service,
        tail,
        composeSvc.profilesFor(state.get('monitoring.enabled'))
      )
    )
  )

  // ── 9단계: DB 마이그레이션 ───────────────────────────────────────────────
  ipcMain.handle(
    'migration:scan',
    safeHandler(async () => {
      const r = await migrationSvc.scan(state.get('installDir'), state.get('migration.applied') || {})
      if (r.ok) await state.set('migration.lastScanAt', r.scannedAt)
      return r
    })
  )

  ipcMain.handle(
    'migration:apply',
    safeHandler(async (selections) => {
      if (!Array.isArray(selections) || selections.length === 0) {
        return { ok: false, message: '적용할 항목을 선택하세요.' }
      }
      const onLog = logger('migration:log')
      const r = await migrationSvc.apply(state.get('installDir'), selections, {
        onLog,
        secretValues: state.secretValues()
      })
      const ledger = { ...(state.get('migration.applied') || {}) }
      for (const res of r.results) {
        if (res.ok && !res.skipped) {
          ledger[res.file] = {
            checksum: res.checksum,
            appliedAt: res.appliedAt,
            groupKeys: res.groupKeys
          }
        }
      }
      await state.set('migration.applied', ledger)
      return { ...r, state: state.projection() }
    })
  )

  // ── 10단계: 완료 ─────────────────────────────────────────────────────────
  ipcMain.handle(
    'finish:health',
    safeHandler(async () => {
      const r = await finishSvc.healthCheck(state.get('installDir'))
      await state.set('finish.healthAt', r.checkedAt)
      return { ok: true, result: r }
    })
  )

  ipcMain.handle(
    'finish:shortcut',
    safeHandler(async () => {
      const r = await finishSvc.createDesktopShortcut()
      if (r.ok) await state.set('finish.shortcutPath', r.path)
      return r
    })
  )

  ipcMain.handle(
    'backup:save',
    safeHandler(async () => {
      const win = getWindow()
      const confirm = await dialog.showMessageBox(win, {
        type: 'warning',
        buttons: ['저장', '취소'],
        defaultId: 1,
        cancelId: 1,
        title: '설정 백업 저장',
        message: '비밀번호와 API 키가 평문으로 들어간 파일을 저장합니다.',
        detail:
          'USB·금고 등 오프라인 보관을 권장합니다. 메신저·메일로 보내지 마세요.\n' +
          'INTEGRATION_SECRET_KEY 를 분실하면 DB 에 저장된 연동값을 복호화할 수 없습니다.'
      })
      if (confirm.response !== 0) return { ok: false, canceled: true }

      const r = await finishSvc.saveBackup(state.data)
      if (r.ok) {
        await state.set('generated.backupSavedTo', r.path)
        await state.set('finish.backupPath', r.path)
      }
      return r
    })
  )

  ipcMain.handle(
    'app:info',
    safeHandler(async () => ({
      ok: true,
      version: app.getVersion(),
      platform: process.platform,
      isWindows: process.platform === 'win32',
      electron: process.versions.electron,
      node: process.versions.node,
      stateFile: state.filePath,
      maskExample: maskValue('example-secret-value')
    }))
  )
}

module.exports = { register }
