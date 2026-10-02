<script setup>
import { nextTick, ref, watch } from 'vue'

const props = defineProps({
  lines: { type: Array, default: () => [] },
  placeholder: { type: String, default: '아직 출력이 없습니다.' },
  height: { type: String, default: '300px' }
})

const box = ref(null)
const stick = ref(true)

function onScroll() {
  const el = box.value
  if (!el) return
  stick.value = el.scrollHeight - el.scrollTop - el.clientHeight < 40
}

watch(
  () => props.lines.length,
  async () => {
    if (!stick.value) return
    await nextTick()
    if (box.value) box.value.scrollTop = box.value.scrollHeight
  }
)
</script>

<template>
  <div ref="box" class="logview" :style="{ height }" @scroll="onScroll">
    <div v-if="!lines.length" class="l-empty">{{ placeholder }}</div>
    <div v-for="(l, i) in lines" :key="i" :class="l.stream === 'stderr' ? 'l-stderr' : ''">{{ l.line }}</div>
  </div>
</template>
