<script setup>
import { computed } from 'vue'
import { store, STEPS, goto } from '../store'

const props = defineProps({
  nextDisabled: { type: Boolean, default: false },
  nextLabel: { type: String, default: '다음' },
  nextReason: { type: String, default: '' },
  hideNext: { type: Boolean, default: false },
  hideBack: { type: Boolean, default: false }
})

const emit = defineEmits(['next', 'back'])

const current = computed(() => (store.state ? store.state.currentStep : 1))
const isFirst = computed(() => current.value <= 1)
const isLast = computed(() => current.value >= STEPS.length)

function back() {
  emit('back')
  goto(current.value - 1)
}

function next() {
  emit('next')
  if (!isLast.value) goto(current.value + 1)
}
</script>

<template>
  <div class="stepnav">
    <button v-if="!hideBack" class="ghost" :disabled="isFirst" @click="back">← 이전</button>
    <span v-if="nextDisabled && nextReason" class="faint" style="font-size: 12px">{{ nextReason }}</span>
    <div class="foot-spacer"></div>
    <slot name="extra"></slot>
    <button v-if="!hideNext" class="primary" :disabled="nextDisabled" @click="next">{{ nextLabel }} →</button>
  </div>
</template>

<style scoped>
.stepnav {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 26px;
  padding-top: 16px;
  border-top: 1px solid var(--pb-border);
}
.foot-spacer {
  flex: 1;
}
</style>
