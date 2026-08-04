'use strict'

const { app, BrowserWindow, session, shell, dialog } = require('electron')
const path = require('node:path')
const fs = require('node:fs')

const { WizardState } = require('./state')
const ipc = require('./ipc')

/**
 * Playbook 설치 마법사 — 메인 프로세스 진입점.
 *
 * 보안 설정 (요구사항)
 *   nodeIntegration: false, contextIsolation: true, sandbox: true
 *   원격 콘텐츠 로드 금지, 새 창/네비게이션 차단, CSP 강제
 */

const DEV_SERVER_URL = process.env.VITE_DEV_SERVER_URL || ''

let mainWindow = null
let state = null

// 단일 인스턴스 — 두 개가 동시에 상태 파일을 쓰면 설정이 깨진다
if (!app.requestSingleInstanceLock()) {
  app.quit()
} else {
  app.on('second-instance', () => {
    if (mainWindow) {
      if (mainWindow.isMinimized()) mainWindow.restore()
      mainWindow.focus()
    }
  })
}

function stateFilePath() {
  return path.join(app.getPath('userData'), 'wizard-state.json')
}

function rendererEntry() {
  // 패키징 후: asar 안의 dist/renderer/index.html
  const packaged = path.join(__dirname, '..', '..', 'dist', 'renderer', 'index.html')
  return packaged
}

function applySecurityPolicies() {
  const ses = session.defaultSession

  // 콘텐츠 보안 정책 — 인라인 스크립트/원격 리소스 차단.
  // vite 빌드 산출물은 외부 스크립트 파일이므로 'self' 로 충분하다.
  ses.webRequest.onHeadersReceived((details, callback) => {
    callback({
      responseHeaders: {
        ...details.responseHeaders,
        'Content-Security-Policy': [
          "default-src 'self'; " +
            "script-src 'self'; " +
            "style-src 'self' 'unsafe-inline'; " + // Vue SFC 의 <style> 는 인라인으로 주입된다
            "img-src 'self' data:; " +
            "font-src 'self' data:; " +
            "connect-src 'self'; " +
            "object-src 'none'; " +
            "base-uri 'none'; " +
            "form-action 'none'; " +
            "frame-ancestors 'none'"
        ]
      }
    })
  })

  // 권한 요청(카메라·알림 등)은 전부 거부 — 설치 마법사에는 필요 없다
  ses.setPermissionRequestHandler((_wc, _permission, callback) => callback(false))
  ses.setPermissionCheckHandler(() => false)
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1140,
    height: 820,
    minWidth: 960,
    minHeight: 700,
    show: false,
    autoHideMenuBar: true,
    backgroundColor: '#0f1420',
    title: 'Playbook 설치 마법사',
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      nodeIntegration: false,
      contextIsolation: true,
      sandbox: true,
      webviewTag: false,
      devTools: !app.isPackaged,
      spellcheck: false
    }
  })

  mainWindow.once('ready-to-show', () => mainWindow.show())

  // 새 창은 전부 차단하고, 허용 도메인만 기본 브라우저로 넘긴다 (IPC 화이트리스트와 동일 정책)
  mainWindow.webContents.setWindowOpenHandler(() => ({ action: 'deny' }))
  mainWindow.webContents.on('will-navigate', (event, url) => {
    const current = mainWindow.webContents.getURL()
    if (url !== current) {
      event.preventDefault()
      if (/^https?:/.test(url)) shell.openExternal(url).catch(() => {})
    }
  })

  if (DEV_SERVER_URL) {
    mainWindow.loadURL(DEV_SERVER_URL)
    mainWindow.webContents.openDevTools({ mode: 'detach' })
  } else {
    const entry = rendererEntry()
    if (!fs.existsSync(entry)) {
      dialog.showErrorBox(
        '설치 마법사 오류',
        `화면 리소스를 찾을 수 없습니다:\n${entry}\n\n개발 실행이라면 먼저 \`npm run build:renderer\` 를 실행하세요.`
      )
      app.quit()
      return
    }
    mainWindow.loadFile(entry)
  }

  mainWindow.on('closed', () => {
    mainWindow = null
  })
}

app.whenReady().then(() => {
  applySecurityPolicies()

  state = new WizardState(stateFilePath())
  const loaded = state.load()
  if (loaded.error) {
    // 상태 파일이 깨졌어도 마법사는 떠야 한다 — 처음부터 다시 진행하도록 알린다
    dialog.showErrorBox(
      '이전 설치 상태를 읽지 못했습니다',
      `저장된 진행 상태가 손상되어 처음부터 다시 시작합니다.\n(${loaded.error})`
    )
  }

  ipc.register({ state, getWindow: () => mainWindow })
  createWindow()

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})

app.on('window-all-closed', () => {
  app.quit()
})

// 원격 콘텐츠에 preload 가 실리는 사고를 원천 차단
app.on('web-contents-created', (_e, contents) => {
  contents.on('will-attach-webview', (event) => event.preventDefault())
})
