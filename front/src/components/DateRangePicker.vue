<template>
  <div class="drp-root" ref="rootRef">
    <!-- 트리거 버튼 -->
    <button class="drp-trigger" :class="{ active: isOpen }" @click="toggle">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" class="drp-icon">
        <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
        <line x1="16" y1="2" x2="16" y2="6" stroke="currentColor" stroke-width="2"/>
        <line x1="8" y1="2" x2="8" y2="6" stroke="currentColor" stroke-width="2"/>
        <line x1="3" y1="10" x2="21" y2="10" stroke="currentColor" stroke-width="2"/>
      </svg>
      <span class="drp-label">{{ displayLabel }}</span>
      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" class="drp-chevron" :class="{ rotated: isOpen }">
        <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>
    </button>

    <!-- 모바일 백드롭 -->
    <div v-if="isOpen" class="drp-overlay" @click="isOpen = false" />

    <!-- 드롭다운 패널 -->
    <div v-if="isOpen" class="drp-panel">
      <!-- 왼쪽: 프리셋 목록 -->
      <div class="drp-presets">
        <button
          v-for="p in presets"
          :key="p.value"
          class="drp-preset-btn"
          :class="{ selected: activePeriod === p.value }"
          @click="selectPreset(p)"
        >{{ p.label }}</button>
      </div>

      <!-- 오른쪽: 달력 (직접 지정 시) -->
      <div class="drp-calendar" v-if="activePeriod === 'custom'">
        <!-- 월 헤더 -->
        <div class="drp-cal-header">
          <button class="drp-nav-btn" @click="prevMonth">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
          </button>
          <span class="drp-month-label">{{ currentMonthLabel }}</span>
          <button class="drp-nav-btn" @click="nextMonth">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
          </button>
        </div>

        <!-- 요일 헤더 (월요일 시작) -->
        <div class="drp-weekdays">
          <span v-for="d in ['월','화','수','목','금','토','일']" :key="d">{{ d }}</span>
        </div>

        <!-- 날짜 그리드 -->
        <div class="drp-days">
          <button
            v-for="cell in calendarCells"
            :key="cell.key"
            class="drp-day"
            :class="getDayClass(cell)"
            :disabled="!cell.currentMonth"
            @click="cell.currentMonth && selectDay(cell.date)"
            @mouseenter="cell.currentMonth && (hoverDate = cell.date)"
            @mouseleave="hoverDate = null"
          >{{ cell.day }}</button>
        </div>

        <!-- 선택된 범위 표시 -->
        <div class="drp-range-info" v-if="rangeStart">
          <span>{{ formatDate(rangeStart) }}</span>
          <span v-if="rangeEnd"> ~ {{ formatDate(rangeEnd) }}</span>
          <span v-else class="drp-hint"> (종료일 선택)</span>
        </div>

        <!-- 적용 버튼 -->
        <div class="drp-actions">
          <button class="drp-clear-btn" @click="clearCustom">초기화</button>
          <button class="drp-apply-btn" :disabled="!rangeStart || !rangeEnd" @click="applyCustom">적용</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

const emit = defineEmits(['change'])

const isOpen = ref(false)
const activePeriod = ref('all')
const rootRef = ref(null)

// 달력 상태
const viewYear = ref(new Date().getFullYear())
const viewMonth = ref(new Date().getMonth()) // 0-based
const rangeStart = ref(null)   // Date
const rangeEnd = ref(null)     // Date
const hoverDate = ref(null)

const presets = [
  { value: 'all',   label: '전체 기간' },
  { value: 'today', label: '오늘' },
  { value: 'week',  label: '이번 주' },
  { value: 'month', label: '이번 달' },
  { value: 'custom',label: '직접 지정' },
]

const displayLabel = computed(() => {
  if (activePeriod.value === 'custom' && rangeStart.value && rangeEnd.value) {
    return `${formatDate(rangeStart.value)} ~ ${formatDate(rangeEnd.value)}`
  }
  return presets.find(p => p.value === activePeriod.value)?.label || '기간 선택'
})

const currentMonthLabel = computed(() => {
  const y = viewYear.value
  const m = viewMonth.value + 1
  return `${y}년 ${m}월`
})

const calendarCells = computed(() => {
  const cells = []
  const year = viewYear.value
  const month = viewMonth.value

  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)

  // 월요일 시작: 0=월 1=화 ... 6=일
  let startDow = firstDay.getDay() // 0=일 1=월 ... 6=토
  startDow = startDow === 0 ? 6 : startDow - 1  // 월=0, 일=6으로 변환

  // 앞 달 채우기
  for (let i = startDow - 1; i >= 0; i--) {
    const d = new Date(year, month, -i)
    cells.push({ date: d, day: d.getDate(), currentMonth: false, key: `prev-${i}` })
  }

  // 이번 달
  for (let d = 1; d <= lastDay.getDate(); d++) {
    const date = new Date(year, month, d)
    cells.push({ date, day: d, currentMonth: true, key: `curr-${d}` })
  }

  // 뒷 달 채우기 (6주 * 7일 = 42셀 고정)
  const remaining = 42 - cells.length
  for (let i = 1; i <= remaining; i++) {
    const d = new Date(year, month + 1, i)
    cells.push({ date: d, day: d.getDate(), currentMonth: false, key: `next-${i}` })
  }

  return cells
})

function getDayClass(cell) {
  if (!cell.currentMonth) return ['drp-day-other']
  const classes = []
  const d = cell.date
  const today = new Date(); today.setHours(0, 0, 0, 0)
  if (isSameDay(d, today)) classes.push('drp-today')

  const effectiveEnd = rangeEnd.value || hoverDate.value
  if (rangeStart.value) {
    if (isSameDay(d, rangeStart.value)) classes.push('drp-range-start')
    if (effectiveEnd && isSameDay(d, effectiveEnd)) classes.push('drp-range-end')
    if (effectiveEnd) {
      const lo = rangeStart.value < effectiveEnd ? rangeStart.value : effectiveEnd
      const hi = rangeStart.value < effectiveEnd ? effectiveEnd : rangeStart.value
      if (d > lo && d < hi) classes.push('drp-in-range')
    }
  }
  return classes
}

function isSameDay(a, b) {
  return a.getFullYear() === b.getFullYear() &&
         a.getMonth() === b.getMonth() &&
         a.getDate() === b.getDate()
}

function selectDay(date) {
  if (!rangeStart.value || (rangeStart.value && rangeEnd.value)) {
    rangeStart.value = new Date(date)
    rangeEnd.value = null
  } else {
    if (date < rangeStart.value) {
      rangeEnd.value = new Date(rangeStart.value)
      rangeStart.value = new Date(date)
    } else {
      rangeEnd.value = new Date(date)
    }
  }
}

function applyCustom() {
  if (!rangeStart.value || !rangeEnd.value) return
  emit('change', {
    period: 'custom',
    startDate: toIsoDate(rangeStart.value),
    endDate: toIsoDate(rangeEnd.value),
  })
  isOpen.value = false
}

function clearCustom() {
  rangeStart.value = null
  rangeEnd.value = null
}

function selectPreset(p) {
  activePeriod.value = p.value
  if (p.value === 'custom') return  // 달력 열기

  rangeStart.value = null
  rangeEnd.value = null

  const today = new Date(); today.setHours(0, 0, 0, 0)
  let start = null, end = null

  if (p.value === 'today') {
    start = new Date(today)
    end = new Date(today)
  } else if (p.value === 'week') {
    const dow = today.getDay() === 0 ? 6 : today.getDay() - 1 // 월=0
    start = new Date(today); start.setDate(today.getDate() - dow)
    end = new Date(start); end.setDate(start.getDate() + 6)
  } else if (p.value === 'month') {
    start = new Date(today.getFullYear(), today.getMonth(), 1)
    end = new Date(today.getFullYear(), today.getMonth() + 1, 0)
  }

  emit('change', {
    period: p.value,
    startDate: start ? toIsoDate(start) : null,
    endDate: end ? toIsoDate(end) : null,
  })
  isOpen.value = false
}

function prevMonth() {
  if (viewMonth.value === 0) { viewMonth.value = 11; viewYear.value-- }
  else viewMonth.value--
}
function nextMonth() {
  if (viewMonth.value === 11) { viewMonth.value = 0; viewYear.value++ }
  else viewMonth.value++
}

function toggle() { isOpen.value = !isOpen.value }

function formatDate(d) {
  if (!d) return ''
  return `${d.getFullYear()}.${String(d.getMonth()+1).padStart(2,'0')}.${String(d.getDate()).padStart(2,'0')}`
}
function toIsoDate(d) {
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}

// 외부 클릭 닫기
function onClickOutside(e) {
  if (rootRef.value && !rootRef.value.contains(e.target)) isOpen.value = false
}
onMounted(() => document.addEventListener('mousedown', onClickOutside))
onBeforeUnmount(() => document.removeEventListener('mousedown', onClickOutside))

// 외부에서 리셋 가능하도록 노출
const reset = () => {
  activePeriod.value = 'all'
  rangeStart.value = null
  rangeEnd.value = null
}
defineExpose({ reset })
</script>

<style scoped>
.drp-root { position: relative; display: inline-block; }
.drp-root:has(.drp-panel) { z-index: 1000; }

/* 트리거 버튼 */
.drp-trigger {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 12px;
  border: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
  white-space: nowrap;
  min-width: 140px;
}
.drp-trigger:hover { border-color: var(--pb-color-brand); }
.drp-trigger.active { border-color: var(--pb-color-brand); box-shadow: 0 0 0 3px var(--pb-color-brand-soft); }

.drp-icon { color: var(--pb-color-text-soft); flex-shrink: 0; }
.drp-label { flex: 1; text-align: left; }
.drp-chevron { color: var(--pb-color-text-soft); transition: transform 0.15s; flex-shrink: 0; }
.drp-chevron.rotated { transform: rotate(180deg); }

/* 드롭다운 패널 */
.drp-panel {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  z-index: 1000;
  display: flex;
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-md);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  overflow: hidden;
}

/* 프리셋 목록 */
.drp-presets {
  display: flex;
  flex-direction: column;
  padding: 8px;
  border-right: 1px solid var(--pb-color-border);
  min-width: 110px;
}
.drp-preset-btn {
  display: block;
  width: 100%;
  padding: 7px 12px;
  text-align: left;
  border: none;
  background: transparent;
  color: var(--pb-color-text);
  font-size: 13px;
  border-radius: var(--pb-radius-sm);
  cursor: pointer;
  transition: background 0.12s;
  white-space: nowrap;
}
.drp-preset-btn:hover { background: var(--pb-color-surface-muted); }
.drp-preset-btn.selected {
  background: var(--pb-color-brand-soft);
  color: var(--pb-color-brand);
  font-weight: 600;
}

/* 달력 영역 */
.drp-calendar { padding: 14px; width: 252px; }

.drp-cal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.drp-month-label { font-size: 13px; font-weight: 600; color: var(--pb-color-heading); }
.drp-nav-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px; height: 28px;
  border: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface);
  border-radius: var(--pb-radius-sm);
  cursor: pointer;
  color: var(--pb-color-text-soft);
  transition: background 0.12s;
}
.drp-nav-btn:hover { background: var(--pb-color-surface-muted); }

/* 요일 헤더 */
.drp-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 4px;
}
.drp-weekdays span {
  text-align: center;
  font-size: 11px;
  font-weight: 600;
  color: var(--pb-color-text-soft);
  padding: 4px 0;
}

/* 날짜 그리드 */
.drp-days { display: grid; grid-template-columns: repeat(7, 1fr); gap: 1px; }

.drp-day {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 32px;
  border: none;
  background: transparent;
  font-size: 12px;
  color: var(--pb-color-text);
  cursor: pointer;
  border-radius: var(--pb-radius-sm);
  transition: background 0.1s, color 0.1s;
}
.drp-day:hover:not(:disabled):not(.drp-range-start):not(.drp-range-end) {
  background: var(--pb-color-surface-muted);
}
.drp-day-other { color: var(--pb-color-text-soft); opacity: 0.4; cursor: default; }
.drp-today { font-weight: 700; color: var(--pb-color-brand); }
.drp-range-start, .drp-range-end {
  background: var(--pb-color-brand) !important;
  color: #fff !important;
  border-radius: var(--pb-radius-sm);
  font-weight: 600;
}
.drp-in-range { background: var(--pb-color-brand-soft); border-radius: 0; }

/* 범위 정보 */
.drp-range-info {
  margin-top: 10px;
  font-size: 12px;
  color: var(--pb-color-text-muted);
  text-align: center;
}
.drp-hint { color: var(--pb-color-brand); }

/* 하단 버튼 */
.drp-actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  justify-content: flex-end;
}
.drp-clear-btn {
  height: 28px;
  padding: 0 12px;
  border: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface);
  color: var(--pb-color-text-muted);
  border-radius: var(--pb-radius-sm);
  font-size: 12px;
  cursor: pointer;
  transition: background 0.12s;
}
.drp-clear-btn:hover { background: var(--pb-color-surface-muted); }
.drp-apply-btn {
  height: 28px;
  padding: 0 14px;
  border: none;
  background: var(--pb-color-brand);
  color: #fff;
  border-radius: var(--pb-radius-sm);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.12s;
}
.drp-apply-btn:hover:not(:disabled) { background: var(--pb-color-brand-strong); }
.drp-apply-btn:disabled { opacity: 0.4; cursor: not-allowed; }

/* 백드롭: 모바일에서만 표시 */
.drp-overlay { display: none; }

/* 중간 화면: 패널을 우측 정렬해 뷰포트 이탈 방지 */
@media (max-width: 768px) {
  .drp-panel {
    left: auto;
    right: 0;
  }
}

/* 소형 화면: 바텀 시트 패턴 */
@media (max-width: 520px) {
  .drp-overlay {
    display: block;
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.35);
    z-index: 999;
  }

  .drp-panel {
    position: fixed;
    left: 0 !important;
    right: 0 !important;
    bottom: 0;
    top: auto;
    z-index: 1000;
    flex-direction: column;
    border-radius: 16px 16px 0 0;
    box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.15);
    max-height: 85vh;
    overflow-y: auto;
    width: 100%;
  }

  /* 손잡이 인디케이터 */
  .drp-panel::before {
    content: '';
    display: block;
    width: 36px;
    height: 4px;
    border-radius: 2px;
    background: var(--pb-color-border);
    margin: 10px auto 2px;
    flex-shrink: 0;
  }

  .drp-presets {
    flex-direction: row;
    flex-wrap: wrap;
    border-right: none;
    border-bottom: 1px solid var(--pb-color-border);
    padding: 10px 12px 8px;
    gap: 6px;
    min-width: unset;
  }

  .drp-preset-btn {
    flex: 1 1 calc(50% - 3px);
    text-align: center;
    padding: 10px 8px;
    min-height: 40px;
  }

  .drp-calendar {
    width: 100%;
    box-sizing: border-box;
    padding: 14px 16px 20px;
  }

  .drp-day {
    height: 40px;
  }

  .drp-trigger {
    min-width: 0;
    width: 100%;
  }
}
</style>
