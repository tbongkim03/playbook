import { reactive, readonly } from 'vue'

/**
 * 렌더러 상태.
 *
 * ★ 여기에 시크릿 평문은 담기지 않는다.
 *   메인 프로세스가 돌려주는 투영에서 시크릿 자리는 { __secret, set, masked } 다.
 *   입력창은 "미확정 입력값(draft)" 만 로컬에 두고, blur/확인 시 setSecret 으로 올린 뒤 비운다.
 */

const STEPS = [
  { no: 1, key: 'environment', title: '환경 점검', desc: 'Docker · WSL2 · 포트 · 디스크' },
  { no: 2, key: 'campus', title: '캠퍼스 · 시크릿', desc: '캠퍼스 지정과 비밀번호 자동 생성' },
  { no: 3, key: 'apikeys', title: '외부 API 키', desc: '네이버 · 국립중앙도서관 · Work24' },
  { no: 4, key: 'discord', title: '디스코드 봇', desc: '토큰 검증 · 초대 · 채널/역할 생성' },
  { no: 5, key: 'master', title: '마스터 관리자', desc: '최상위 관리자 계정 설정' },
  { no: 6, key: 'ip', title: '접속 허용 IP', desc: '설치 PC · LAN 대역 등록' },
  { no: 7, key: 'monitoring', title: '중앙 모니터링', desc: '등록 또는 건너뛰기' },
  { no: 8, key: 'deploy', title: '설정 확인 · 배포', desc: '.env 생성 · pull · DB 기동' },
  { no: 9, key: 'migration', title: 'DB 마이그레이션', desc: '테이블별 확인 → 서비스 기동' },
  { no: 10, key: 'done', title: '완료', desc: '헬스체크 · 바로가기 · 백업' }
]

const store = reactive({
  ready: false,
  appInfo: null,
  state: null,
  toast: null,
  busy: false,
  busyLabel: ''
})

async function refresh() {
  const r = await window.wizard.getState()
  if (r && r.ok) store.state = r.state
  return store.state
}

async function init() {
  const info = await window.wizard.appInfo()
  if (info && info.ok) store.appInfo = info
  await refresh()
  store.ready = true
}

async function patch(patchObj) {
  // Vue ref 배열·객체는 Proxy 라 Electron IPC(구조화 복제)가 "could not be cloned" 로 거부한다.
  // 예전엔 6단계 허용 IP 목록이 이렇게 조용히 저장되지 않아 IP_ALLOWLIST_BOOTSTRAP 이 비었다.
  const plain = JSON.parse(JSON.stringify(patchObj ?? {}))
  let r
  try {
    r = await window.wizard.patchState(plain)
  } catch (e) {
    toast(`설정 저장에 실패했습니다: ${e && e.message ? e.message : e}`, 'error', 9000)
    return { ok: false, message: String(e && e.message ? e.message : e) }
  }
  if (r && r.ok) store.state = r.state
  else if (r && r.message) toast(`설정 저장에 실패했습니다: ${r.message}`, 'error', 9000)
  return r
}

async function setSecret(path, value) {
  const r = await window.wizard.setSecret(path, value)
  if (r && r.state) store.state = r.state
  return r
}

async function goto(stepNo) {
  const n = Math.max(1, Math.min(STEPS.length, stepNo))
  await patch({
    currentStep: n,
    maxVisitedStep: Math.max(n, (store.state && store.state.maxVisitedStep) || 1)
  })
}

function toast(message, kind = 'info', ttl = 4200) {
  store.toast = { message, kind, at: Date.now() }
  setTimeout(() => {
    if (store.toast && Date.now() - store.toast.at >= ttl - 50) store.toast = null
  }, ttl)
}

async function withBusy(label, fn) {
  store.busy = true
  store.busyLabel = label
  try {
    return await fn()
  } finally {
    store.busy = false
    store.busyLabel = ''
  }
}

export { store, readonly, STEPS, init, refresh, patch, setSecret, goto, toast, withBusy }
