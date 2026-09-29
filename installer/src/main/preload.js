'use strict'

const { contextBridge, ipcRenderer } = require('electron')

/**
 * 렌더러가 만질 수 있는 유일한 창구.
 *
 * - nodeIntegration: false / contextIsolation: true 이므로 렌더러에는 require 도 process 도 없다.
 * - 여기서 노출하는 함수 목록이 곧 공격 표면 전부다. 일반화된 invoke(channel, ...) 는 만들지 않는다.
 * - 시크릿은 setSecret 으로 **올려보내기만** 하고, 되받는 값은 마스킹된 형태다.
 *   (예외: revealGeneratedSecrets — 사용자가 "값 보기" 를 눌렀을 때만)
 */

const invoke = (channel, ...args) => ipcRenderer.invoke(channel, ...args)

/** 스트리밍 로그 구독 — 해제 함수를 돌려준다 */
function subscribe(channel, handler) {
  const listener = (_e, payload) => handler(payload)
  ipcRenderer.on(channel, listener)
  return () => ipcRenderer.removeListener(channel, listener)
}

contextBridge.exposeInMainWorld('wizard', {
  // ── 앱/상태 ──
  appInfo: () => invoke('app:info'),
  getState: () => invoke('state:get'),
  patchState: (patch) => invoke('state:patch', patch),
  setSecret: (path, value) => invoke('state:setSecret', path, value),
  resetState: () => invoke('state:reset'),
  stateFilePath: () => invoke('state:filePath'),

  // ── 셸/다이얼로그 ──
  openExternal: (url) => invoke('shell:openExternal', url),
  showInFolder: (p) => invoke('shell:showItemInFolder', p),
  pickDirectory: (defaultPath) => invoke('dialog:pickDirectory', defaultPath),

  // ── 1단계 ──
  checkEnvironment: () => invoke('env:check'),

  // ── 2단계 ──
  generateSecrets: (opts) => invoke('secrets:generate', opts),
  revealGeneratedSecrets: () => invoke('secrets:reveal'),
  campusPresets: () => invoke('campus:presets'),

  // ── 3단계 ──
  verifyApiKey: (which) => invoke('apikey:verify', which),
  apiIssueUrls: () => invoke('apikey:issueUrls'),

  // ── 4단계 ──
  discordPortal: () => invoke('discord:portalUrl'),
  discordVerifyToken: () => invoke('discord:verifyToken'),
  discordListGuilds: () => invoke('discord:listGuilds'),
  discordCheckPermissions: (guildId) => invoke('discord:checkPermissions', { guildId }),
  discordProvision: (opts) => invoke('discord:provision', opts),
  discordSkip: (skip) => invoke('discord:skip', skip),
  onDiscordLog: (handler) => subscribe('discord:log', handler),

  // ── 5단계 ──
  validateMaster: () => invoke('master:validate'),

  // ── 6단계 ──
  detectIp: () => invoke('ip:detect'),
  validateIpEntry: (value) => invoke('ip:validate', value),

  // ── 7단계 ──
  monitoringEnvKeys: () => invoke('monitoring:envKeys'),

  // ── 8단계 ──
  deploySummary: () => invoke('deploy:summary'),
  deployRun: () => invoke('deploy:run'),
  deployStartServices: () => invoke('deploy:startServices'),
  deployPs: () => invoke('deploy:ps'),
  deployLogs: (service, tail) => invoke('deploy:logs', service, tail),
  onDeployLog: (handler) => subscribe('deploy:log', handler),

  // ── 9단계 ──
  migrationScan: () => invoke('migration:scan'),
  migrationApply: (selections) => invoke('migration:apply', selections),
  onMigrationLog: (handler) => subscribe('migration:log', handler),
  // 초기 도서 데이터(선택) — 서비스 기동 후에만 동작한다
  pickBookFile: () => invoke('books:pickFile'),
  importBooks: () => invoke('books:import'),

  // ── 10단계 ──
  healthCheck: () => invoke('finish:health'),
  createShortcut: () => invoke('finish:shortcut'),
  saveBackup: () => invoke('backup:save'),

  // ── 네이티브 접속 프록시 (Windows) ──
  proxyStatus: () => invoke('proxy:status'),
  proxyInstall: () => invoke('proxy:install'),
  proxyVerify: () => invoke('proxy:verify'),
  setAllowlistEnabled: (enabled) => invoke('allowlist:setEnabled', enabled),
  onProxyLog: (handler) => subscribe('proxy:log', handler),

  // ── 설치 후 업데이트 ──
  updateCheck: () => invoke('update:check'),
  updateRun: (targetTag) => invoke('update:run', targetTag),
  onUpdateLog: (handler) => subscribe('update:log', handler)
})
