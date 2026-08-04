<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: String, default: 'idle' },
  labels: { type: Object, default: () => ({}) }
})

const TEXT = {
  ok: '정상',
  warn: '주의',
  fail: '실패',
  skip: '건너뜀',
  idle: '미확인',
  pending: '미적용',
  applied: '적용됨'
}

const cls = computed(() => ['ok', 'warn', 'fail', 'skip'].includes(props.status) ? props.status : 'idle')
const text = computed(() => props.labels[props.status] || TEXT[props.status] || props.status)
</script>

<template>
  <span class="pill" :class="cls">{{ text }}</span>
</template>
