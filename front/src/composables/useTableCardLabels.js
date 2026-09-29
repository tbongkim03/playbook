import { onBeforeUnmount, onMounted } from 'vue'

/**
 * 모바일 카드형 표를 위해, root 안 모든 표의 칸(td)에 제목 행(th) 글자를 data-label 로 붙인다.
 * CSS 가 좁은 화면에서 행을 카드로 바꾸고 td::before { content: attr(data-label) } 로 항목명을 보여 준다.
 *
 * 표가 12개 컴포넌트에 흩어져 있어 각 td 에 손으로 라벨을 다는 대신 여기서 한 번에 처리한다.
 * 탭 전환·페이지 이동으로 행이 바뀌면 MutationObserver 가 다시 붙인다 (childList 만 관찰 — 속성 변경으로 되먹임되지 않는다).
 */
export function useTableCardLabels(rootRef) {
  let observer = null
  let frame = 0

  const apply = () => {
    frame = 0
    const root = rootRef.value
    if (!root) return
    for (const table of root.querySelectorAll('table')) {
      const heads = [...table.querySelectorAll('thead th')].map((th) => th.textContent.replace(/\s+/g, ' ').trim())
      if (!heads.length) continue
      for (const tr of table.querySelectorAll('tbody tr')) {
        let col = 0
        for (const td of tr.children) {
          const label = td.colSpan > 1 ? '' : heads[col] || ''
          if (td.dataset.label !== label) td.dataset.label = label
          col += td.colSpan || 1
        }
      }
    }
  }

  const schedule = () => {
    if (!frame) frame = requestAnimationFrame(apply)
  }

  onMounted(() => {
    apply()
    observer = new MutationObserver(schedule)
    if (rootRef.value) observer.observe(rootRef.value, { childList: true, subtree: true })
  })

  onBeforeUnmount(() => {
    observer && observer.disconnect()
    if (frame) cancelAnimationFrame(frame)
  })
}
