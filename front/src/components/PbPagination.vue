<template>
  <div v-if="totalPages > 1" class="pb-pagination">
    <span v-if="info" class="pb-pagination-info">{{ info }}</span>
    <nav class="pb-pagination-nav" aria-label="페이지 이동">
      <button
        type="button"
        class="pb-page-btn"
        :disabled="page <= 1"
        aria-label="이전 페이지"
        title="이전 페이지"
        @click="go(page - 1)"
      >
        <PhCaretLeft weight="duotone" :size="16" />
      </button>
      <template v-for="(item, i) in items" :key="`${item}-${i}`">
        <span v-if="item === '…'" class="pb-page-ellipsis" aria-hidden="true">…</span>
        <button
          v-else
          type="button"
          class="pb-page-btn"
          :class="{ active: item === page }"
          :aria-current="item === page ? 'page' : undefined"
          @click="go(item)"
        >{{ item }}</button>
      </template>
      <button
        type="button"
        class="pb-page-btn"
        :disabled="page >= totalPages"
        aria-label="다음 페이지"
        title="다음 페이지"
        @click="go(page + 1)"
      >
        <PhCaretRight weight="duotone" :size="16" />
      </button>
    </nav>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { PhCaretLeft, PhCaretRight } from '@phosphor-icons/vue'

/**
 * 목록 아래 페이지 이동 — 앱 전체가 이 하나를 쓴다.
 * 예전에는 화면마다 마크업·번호 계산·스타일이 복사돼 있었고 "이전/다음" 글자까지 붙어 있었다.
 * 화살표는 아이콘만 두고 읽기용 이름은 aria-label 로 준다.
 */
const props = defineProps({
  page: { type: Number, required: true },
  totalPages: { type: Number, required: true },
  // "1–15 / 672건" 같은 범위 표시. 비우면 번호만 보인다
  info: { type: String, default: '' }
})
const emit = defineEmits(['change'])

// 휴대폰 폭에서는 번호를 줄여 한 줄에 들어가게 한다 (1 … 5 … 45)
const narrow = ref(false)
let mq = null
const syncNarrow = () => { narrow.value = !!(mq && mq.matches) }
onMounted(() => {
  mq = window.matchMedia('(max-width: 480px)')
  syncNarrow()
  mq.addEventListener('change', syncNarrow)
})
onBeforeUnmount(() => mq && mq.removeEventListener('change', syncNarrow))

const items = computed(() => {
  const total = props.totalPages
  const current = props.page
  const around = narrow.value ? 0 : 1
  const maxPlain = narrow.value ? 5 : 7
  if (total <= maxPlain) return Array.from({ length: total }, (_, i) => i + 1)
  const out = [1]
  const start = Math.max(2, current - around)
  const end = Math.min(total - 1, current + around)
  if (start > 2) out.push('…')
  for (let i = start; i <= end; i++) out.push(i)
  if (end < total - 1) out.push('…')
  out.push(total)
  return out
})

const go = (p) => {
  if (p >= 1 && p <= props.totalPages && p !== props.page) emit('change', p)
}
</script>

<style scoped>
.pb-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 4px;
  margin-top: 8px;
}

.pb-pagination-info {
  font-size: 13px;
  color: var(--pb-color-text-muted);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

.pb-pagination-nav {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: auto;
}

.pb-page-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 34px;
  height: 34px;
  padding: 0 8px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  font-size: 13px;
  font-variant-numeric: tabular-nums;
  cursor: pointer;
  transition: background 0.12s, border-color 0.12s, color 0.12s;
}

.pb-page-btn:hover:not(:disabled):not(.active) {
  background: var(--pb-color-surface-muted);
  border-color: var(--pb-color-border-strong);
}

.pb-page-btn:focus-visible {
  outline: 2px solid var(--pb-color-brand);
  outline-offset: 1px;
}

.pb-page-btn.active {
  background: var(--pb-color-brand);
  border-color: var(--pb-color-brand);
  color: #fff;
  font-weight: 600;
}

.pb-page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pb-page-ellipsis {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  color: var(--pb-color-text-soft);
  user-select: none;
}

/* 휴대폰: 번호 줄을 가운데, 범위 표시는 그 위에 작게 */
@media (max-width: 480px) {
  .pb-pagination {
    flex-direction: column;
    gap: 8px;
  }

  .pb-pagination-nav {
    margin-left: 0;
  }

  .pb-page-btn {
    min-width: 36px;
    height: 36px;
  }
}
</style>
