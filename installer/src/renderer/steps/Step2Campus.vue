<script setup>
import { computed, onMounted, ref } from 'vue'
import { store, patch, toast, withBusy } from '../store'
import StepNav from '../components/StepNav.vue'

const presets = ref([])
const mode = ref('preset') // 'preset' | 'custom'
const selected = ref('')
const customName = ref('')

const revealed = ref(null)
const revealing = ref(false)

const generated = computed(() => (store.state ? store.state.generated : null))
const hasSecrets = computed(() => !!(generated.value && generated.value.integrationSecretKey.set))
const backupPath = computed(() => (generated.value ? generated.value.backupSavedTo : null))

onMounted(async () => {
  const p = await window.wizard.campusPresets()
  if (p.ok) presets.value = p.presets

  const name = (store.state.campus && store.state.campus.name) || ''
  if (name && presets.value.some((x) => x.name === name)) {
    mode.value = 'preset'
    selected.value = name
  } else if (name) {
    mode.value = 'custom'
    customName.value = name
  }

  await window.wizard.generateSecrets({ force: false })
  await refreshState()
})

async function refreshState() {
  const r = await window.wizard.getState()
  if (r.ok) store.state = r.state
}

const campusName = computed(() => (mode.value === 'preset' ? selected.value : customName.value.trim()))

async function saveCampus() {
  const name = campusName.value
  const preset = presets.value.find((p) => p.name === name)
  await patch({
    campus: {
      name,
      envSlot: preset ? preset.envSlot : '',
      campusId: preset ? preset.campusId : null,
      custom: !preset
    },
    monitoring: { campusLabel: (store.state.monitoring && store.state.monitoring.campusLabel) || name }
  })
}

async function regenerate() {
  await withBusy('시크릿을 다시 생성하는 중…', async () => {
    await window.wizard.generateSecrets({ force: true })
    await refreshState()
    revealed.value = null
  })
  toast('새 시크릿을 생성했습니다. 반드시 백업 파일을 다시 저장하세요.', 'warn')
}

async function reveal() {
  revealing.value = true
  try {
    const r = await window.wizard.revealGeneratedSecrets()
    if (r.ok) revealed.value = r.values
  } finally {
    revealing.value = false
  }
}

function hide() {
  revealed.value = null
}

async function saveBackup() {
  const r = await window.wizard.saveBackup()
  if (r.ok) {
    await refreshState()
    toast(`백업 파일을 저장했습니다: ${r.path}`, 'success', 7000)
  } else if (!r.canceled) {
    toast(r.message || '백업 저장에 실패했습니다.', 'error')
  }
}

const canProceed = computed(() => !!campusName.value && hasSecrets.value)
</script>

<template>
  <h1 class="page-title">2. 캠퍼스 정보 · 시크릿 생성</h1>
  <p class="page-lead">
    이 PC 에서 운영할 캠퍼스를 지정합니다. 데이터베이스 비밀번호와 연동 암호화 키는 사람이 정하지 않고
    <b>암호학적 난수로 자동 생성</b>합니다.
  </p>

  <h2 class="section-title">캠퍼스</h2>
  <div class="card">
    <div class="field">
      <label class="field-label">지정 방식</label>
      <div class="row">
        <label class="check">
          <input v-model="mode" type="radio" value="preset" @change="saveCampus" />
          기본 캠퍼스에서 선택
        </label>
        <label class="check">
          <input v-model="mode" type="radio" value="custom" @change="saveCampus" />
          직접 입력
        </label>
      </div>
    </div>

    <div v-if="mode === 'preset'" class="field">
      <label class="field-label">캠퍼스<span class="req">*</span></label>
      <select v-model="selected" @change="saveCampus">
        <option value="">선택하세요</option>
        <option v-for="p in presets" :key="p.campusId" :value="p.name">
          {{ p.name }} (seq_campus = {{ p.campusId }})
        </option>
      </select>
      <div class="field-hint">
        db/migration/001_add_campus.sql 에 정의된 기본 캠퍼스입니다. 디스코드 채널 ID 가
        <code class="inline">{{ presets.find((p) => p.name === selected)?.envSlot || 'DISCORD_CHANNEL_*' }}</code>
        환경변수로 자동 연결됩니다.
      </div>
    </div>

    <div v-else class="field">
      <label class="field-label">캠퍼스 이름<span class="req">*</span></label>
      <input v-model="customName" type="text" placeholder="예) 판교" @change="saveCampus" />
      <div class="notice warn mt-8">
        <strong>직접 입력한 캠퍼스에는 전용 환경변수 슬롯이 없습니다</strong>
        백엔드는 서초·G밸리·동작 세 곳에 대해서만
        <code class="inline">DISCORD_CHANNEL_SEOCHO / _GVALLEY / _DONGJAK</code> 환경변수를 읽습니다.
        새 캠퍼스는 설치 후 <b>관리자 화면 &gt; 연동 탭 &gt; 캠퍼스 채널 매핑</b>에서 채널·역할 ID 를 직접 등록해야
        디스코드 알림이 동작합니다. (4단계에서 만든 ID 는 백업 파일에 기록됩니다)
      </div>
    </div>
  </div>

  <h2 class="section-title">자동 생성된 시크릿</h2>

  <div class="notice info">
    <strong>이 값들은 마법사가 만들고, 사람이 외울 필요가 없습니다</strong>
    다만 <code class="inline">INTEGRATION_SECRET_KEY</code> 를 잃어버리면 데이터베이스에 저장된 봇 토큰·API 키를
    복호화할 수 없습니다. 반드시 백업 파일을 저장해 오프라인으로 보관하세요.
  </div>

  <div class="card">
    <table class="kv">
      <tbody>
        <tr>
          <th>DB 비밀번호 (playdata)</th>
          <td class="mono selectable">{{ revealed ? revealed.dbPassword : generated?.dbPassword.masked }}</td>
        </tr>
        <tr>
          <th>DB root 비밀번호</th>
          <td class="mono selectable">{{ revealed ? revealed.dbRootPassword : generated?.dbRootPassword.masked }}</td>
        </tr>
        <tr>
          <th>INTEGRATION_SECRET_KEY</th>
          <td class="mono selectable" style="word-break: break-all">
            {{ revealed ? revealed.integrationSecretKey : generated?.integrationSecretKey.masked }}
          </td>
        </tr>
        <tr>
          <th>생성 시각</th>
          <td class="muted">
            {{ generated?.generatedAt ? new Date(generated.generatedAt).toLocaleString('ko-KR') : '-' }}
          </td>
        </tr>
      </tbody>
    </table>

    <div class="row mt-12">
      <button v-if="!revealed" class="small" :disabled="revealing" @click="reveal">값 보기</button>
      <button v-else class="small" @click="hide">가리기</button>
      <button class="small primary" @click="saveBackup">백업 파일로 저장</button>
      <button class="small danger" @click="regenerate">다시 생성</button>
    </div>

    <div v-if="backupPath" class="field-hint mt-8">
      마지막 백업: <span class="mono selectable">{{ backupPath }}</span>
    </div>
    <div v-else class="check-hint mt-8">아직 백업 파일을 저장하지 않았습니다.</div>
  </div>

  <StepNav :next-disabled="!canProceed" next-reason="캠퍼스를 지정해야 다음으로 넘어갈 수 있습니다." />
</template>
