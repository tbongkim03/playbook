<script setup>
import { computed, onMounted, ref } from 'vue'
import { store, patch, toast, withBusy } from '../store'
import StepNav from '../components/StepNav.vue'
import StatusPill from '../components/StatusPill.vue'

const result = computed(() => (store.state && store.state.environment ? store.state.environment.lastResult : null))
const installDir = ref('')
const checking = ref(false)

onMounted(async () => {
  installDir.value = (store.state && store.state.installDir) || ''
  if (!result.value) await runCheck()
})

async function runCheck() {
  checking.value = true
  try {
    await patch({ installDir: installDir.value })
    const r = await window.wizard.checkEnvironment()
    if (!r.ok) toast(r.message || '환경 점검에 실패했습니다.', 'error')
    else if (r.result.canProceed) toast('환경 점검 통과', 'success')
    else toast(`해결해야 할 항목이 ${r.result.blockingCount}개 있습니다.`, 'warn')
  } finally {
    checking.value = false
  }
}

async function pickDir() {
  const r = await window.wizard.pickDirectory(installDir.value)
  if (r.ok) {
    installDir.value = r.path
    await patch({ installDir: r.path })
    await runCheck()
  }
}

async function open(url) {
  const r = await window.wizard.openExternal(url)
  if (!r.ok) toast(r.message, 'error')
}

const canProceed = computed(() => !!(result.value && result.value.canProceed))
const notWindows = computed(() => store.appInfo && !store.appInfo.isWindows)
</script>

<template>
  <h1 class="page-title">1. 설치 환경 점검</h1>
  <p class="page-lead">
    Playbook 은 이 PC 의 Docker 위에서 동작합니다. 설치를 시작하기 전에 필요한 조건이 모두 갖춰졌는지 확인합니다.
  </p>

  <div v-if="notWindows" class="notice warn">
    <strong>Windows 가 아닌 환경에서 실행 중입니다</strong>
    WSL2 점검은 건너뛰며, 바탕화면 바로가기 등 일부 기능은 Windows 에서만 정상 동작합니다.
  </div>

  <div class="card">
    <div class="card-head"><span class="card-title">설치 경로</span></div>
    <div class="input-row">
      <input v-model="installDir" type="text" class="mono" placeholder="C:\Playbook" @change="patch({ installDir })" />
      <button class="small" @click="pickDir">폴더 선택</button>
    </div>
    <div class="field-hint">
      배포 파일(docker-compose.prod.yml · db · monitoring)과 설정 파일(.env.prod)이 이 폴더에 만들어집니다.
      경로에 한글·공백이 없는 짧은 경로를 권장합니다.
    </div>
  </div>

  <h2 class="section-title">점검 결과</h2>

  <div class="card">
    <div v-if="!result" class="muted">점검을 실행하세요.</div>

    <div v-else>
      <div v-for="c in result.checks" :key="c.id" class="check-row">
        <StatusPill :status="c.status" />
        <div class="grow">
          <div style="font-weight: 600">{{ c.label }}</div>
          <div class="check-detail">{{ c.detail }}</div>
          <div v-if="c.hint" class="check-hint">→ {{ c.hint }}</div>
        </div>
        <button v-if="c.downloadUrl" class="small" @click="open(c.downloadUrl)">다운로드 페이지 열기</button>
      </div>
    </div>
  </div>

  <div v-if="result && !result.canProceed" class="notice danger">
    <strong>아직 진행할 수 없습니다</strong>
    위에서 <b>실패</b> 로 표시된 항목을 해결한 뒤 [다시 검사] 를 누르세요. 조건을 만족하지 않은 채 설치하면
    컨테이너가 기동하지 못하거나 서비스가 외부에서 열리지 않습니다.
  </div>

  <div v-if="result && result.canProceed" class="notice success">
    <strong>설치 가능한 환경입니다</strong>
    다음 단계로 진행하세요.
  </div>

  <StepNav
    :next-disabled="!canProceed"
    next-reason="필수 점검 항목을 먼저 통과해야 합니다."
  >
    <template #extra>
      <button :disabled="checking" @click="runCheck">{{ checking ? '검사 중…' : '다시 검사' }}</button>
    </template>
  </StepNav>
</template>
