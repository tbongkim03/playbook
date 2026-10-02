/**
 * 모달 배경 전용 디렉티브 — `<div class="modal-overlay" v-modal-backdrop="close">`
 *
 * 1) 배경에서 "누르고 뗐을 때"만 닫는다.
 *    @click 을 배경에 바로 걸면, 모달 안(입력창 드래그 등)에서 눌러 밖에서 떼도
 *    브라우저가 공통 조상인 배경으로 click 을 보내 모달이 닫힌다.
 * 2) 모달이 떠 있는 동안 뒤 화면 스크롤을 잠근다.
 *    여러 모달이 겹칠 수 있어 개수를 세고, 마지막 하나가 닫힐 때 푼다.
 *
 * 전역 등록하지 않는다 — 쓰는 파일에서 import 한다 (<script setup> 은 vXxx 이름이면 자동 인식).
 */

let openCount = 0
let saved = null

function lockScroll() {
  if (openCount++ > 0) return
  const html = document.documentElement
  const body = document.body
  // 스크롤바가 사라지며 화면이 옆으로 튀지 않게 그 폭만큼 채운다
  const scrollbar = window.innerWidth - html.clientWidth
  saved = { html: html.style.overflow, body: body.style.overflow, paddingRight: body.style.paddingRight }
  // main.css 가 html 에 overflow-x 를 걸어 두어 body 의 overflow 가 화면 스크롤로 전달되지 않는다 → html 도 잠근다
  html.style.overflow = 'hidden'
  body.style.overflow = 'hidden'
  if (scrollbar > 0) body.style.paddingRight = `${scrollbar}px`
}

function unlockScroll() {
  if (openCount === 0 || --openCount > 0) return
  document.documentElement.style.overflow = saved ? saved.html : ''
  document.body.style.overflow = saved ? saved.body : ''
  document.body.style.paddingRight = saved ? saved.paddingRight : ''
  saved = null
}

export const vModalBackdrop = {
  mounted(el, binding) {
    el._pbBackdrop = {
      close: binding.value,
      downOnSelf: false,
      onDown: (e) => {
        el._pbBackdrop.downOnSelf = e.target === el
      },
      onClick: (e) => {
        const { downOnSelf, close } = el._pbBackdrop
        el._pbBackdrop.downOnSelf = false
        if (downOnSelf && e.target === el && typeof close === 'function') close(e)
      }
    }
    el.addEventListener('pointerdown', el._pbBackdrop.onDown)
    el.addEventListener('click', el._pbBackdrop.onClick)
    lockScroll()
  },
  updated(el, binding) {
    if (el._pbBackdrop) el._pbBackdrop.close = binding.value
  },
  unmounted(el) {
    if (el._pbBackdrop) {
      el.removeEventListener('pointerdown', el._pbBackdrop.onDown)
      el.removeEventListener('click', el._pbBackdrop.onClick)
      delete el._pbBackdrop
    }
    unlockScroll()
  }
}
