<script setup>
import { computed, onMounted } from 'vue'
import { store, STEPS, init, goto } from './store'

import Step1Environment from './steps/Step1Environment.vue'
import Step2Campus from './steps/Step2Campus.vue'
import Step3ApiKeys from './steps/Step3ApiKeys.vue'
import Step4Discord from './steps/Step4Discord.vue'
import Step5Master from './steps/Step5Master.vue'
import Step6IpAllowlist from './steps/Step6IpAllowlist.vue'
import Step7Monitoring from './steps/Step7Monitoring.vue'
import Step8Deploy from './steps/Step8Deploy.vue'
import Step9Migration from './steps/Step9Migration.vue'
import Step10Done from './steps/Step10Done.vue'

const COMPONENTS = {
  1: Step1Environment,
  2: Step2Campus,
  3: Step3ApiKeys,
  4: Step4Discord,
  5: Step5Master,
  6: Step6IpAllowlist,
  7: Step7Monitoring,
  8: Step8Deploy,
  9: Step9Migration,
  10: Step10Done
}

const current = computed(() => (store.state ? store.state.currentStep : 1))
const maxVisited = computed(() => (store.state ? store.state.maxVisitedStep || 1 : 1))
const CurrentStep = computed(() => COMPONENTS[current.value] || Step1Environment)

const restoredAt = computed(() => {
  if (!store.state || !store.state.updatedAt) return null
  if ((store.state.maxVisitedStep || 1) <= 1) return null
  try {
    return new Date(store.state.updatedAt).toLocaleString('ko-KR')
  } catch {
    return store.state.updatedAt
  }
})

function canJump(no) {
  return no <= maxVisited.value
}

onMounted(init)
</script>

<template>
  <div v-if="!store.ready" class="busy-overlay">
    <div style="text-align: center"><div class="spinner"></div>설치 마법사를 준비하는 중…</div>
  </div>

  <div v-else class="app-shell">
    <aside class="sidebar">
      <div class="sidebar-head">
        <div class="sidebar-title">Playbook 설치 마법사</div>
        <div class="sidebar-sub">
          캠퍼스 라운지 도서관리 시스템 ·
          {{ store.appInfo ? 'v' + store.appInfo.version : '' }}
        </div>
      </div>

      <ul class="step-list">
        <li
          v-for="s in STEPS"
          :key="s.no"
          class="step-item"
          :class="{ active: s.no === current, done: s.no < maxVisited, locked: !canJump(s.no) }"
          @click="canJump(s.no) && goto(s.no)"
        >
          <div class="step-no">{{ s.no < maxVisited ? '✓' : s.no }}</div>
          <div class="grow">
            <div class="step-label">{{ s.title }}</div>
            <div class="step-desc">{{ s.desc }}</div>
          </div>
        </li>
      </ul>

      <div v-if="restoredAt" class="sidebar-head" style="border-top: 1px solid var(--pb-border); border-bottom: none">
        <div class="faint" style="font-size: 11px">
          이전 진행 상태를 불러왔습니다<br />({{ restoredAt }})
        </div>
      </div>
    </aside>

    <main class="main">
      <div class="main-body">
        <component :is="CurrentStep" />
      </div>
    </main>

    <div v-if="store.toast" class="toast" :class="store.toast.kind">{{ store.toast.message }}</div>

    <div v-if="store.busy" class="busy-overlay">
      <div style="text-align: center">
        <div class="spinner"></div>
        {{ store.busyLabel || '처리 중…' }}
      </div>
    </div>
  </div>
</template>
