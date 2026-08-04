<script setup>
import { computed, onMounted, ref } from 'vue'
import { store, patch } from '../store'
import StepNav from '../components/StepNav.vue'
import SecretField from '../components/SecretField.vue'

const endpoint = ref('https://monitoring.tbongkim.com')
const campusLabel = ref('')
const envKeys = ref([])

const mon = computed(() => (store.state ? store.state.monitoring : null))
const enabled = computed(() => !!(mon.value && mon.value.enabled))

onMounted(async () => {
  endpoint.value = (mon.value && mon.value.endpoint) || 'https://monitoring.tbongkim.com'
  campusLabel.value = (mon.value && mon.value.campusLabel) || (store.state.campus && store.state.campus.name) || ''
  const r = await window.wizard.monitoringEnvKeys()
  if (r.ok) envKeys.value = r.keys
})

async function setEnabled(v) {
  await patch({ monitoring: { enabled: v } })
}

async function save() {
  await patch({
    monitoring: { endpoint: endpoint.value.trim().replace(/\/+$/, ''), campusLabel: campusLabel.value.trim() }
  })
}

const canProceed = computed(() => {
  if (!enabled.value) return true
  return !!(endpoint.value.trim() && campusLabel.value.trim() && mon.value && mon.value.bootstrapSecret.set)
})
</script>

<template>
  <h1 class="page-title">7. 중앙 모니터링 등록</h1>
  <p class="page-lead">
    캠퍼스 서버의 상태(가동 여부 · 응답 시간 · 차단된 접속 수)를 중앙 대시보드로 보냅니다. 개인정보나 도서
    데이터는 전송되지 않고, 숫자 지표만 나갑니다. 사용하지 않아도 서비스는 정상 동작합니다.
  </p>

  <div class="card">
    <div class="row">
      <label class="check">
        <input type="radio" :checked="enabled" @change="setEnabled(true)" />
        중앙 모니터링 사용
      </label>
      <label class="check">
        <input type="radio" :checked="!enabled" @change="setEnabled(false)" />
        사용 안 함 (건너뛰기)
      </label>
    </div>
  </div>

  <template v-if="enabled">
    <div class="card">
      <div class="field">
        <label class="field-label">모니터링 엔드포인트<span class="req">*</span></label>
        <input v-model="endpoint" type="text" class="mono" placeholder="https://monitoring.tbongkim.com" @change="save" />
        <div class="field-hint">
          이 주소를 기준으로 토큰 발급은 <code class="inline">/auth/token</code>, 지표 전송은
          <code class="inline">/api/v1/write</code> 로 자동 구성됩니다.
        </div>
      </div>

      <div class="field">
        <label class="field-label">캠퍼스 라벨<span class="req">*</span></label>
        <input v-model="campusLabel" type="text" placeholder="예) seocho" @change="save" />
        <div class="field-hint">
          대시보드에서 캠퍼스를 구분하는 이름입니다. 중앙에서 발급한 토큰의 캠퍼스 값과
          <b>반드시 같아야</b> 합니다. 다르면 중앙이 위조로 간주해 지표를 거부합니다.
        </div>
      </div>

      <SecretField
        path="monitoring.bootstrapSecret"
        label="부트스트랩 시크릿"
        required
        :secret="mon ? mon.bootstrapSecret : {}"
        placeholder="중앙 모니터링 담당자에게 받은 값"
        hint="최초 1회 이 값으로 액세스 토큰을 발급받고, 이후에는 서버가 자동으로 토큰을 회전시킵니다."
      />
    </div>

    <div class="notice info">
      <strong>생성될 환경변수</strong>
      <span class="mono" style="font-size: 11.5px">{{ envKeys.join(' · ') }}</span>
      <div class="mt-8 faint">
        이름의 정본은 <code class="inline">back/src/main/resources/application.properties</code> 의
        <code class="inline">monitoring.*</code> 프로퍼티와 <code class="inline">monitoring/alloy/config.alloy</code> 입니다.
      </div>
    </div>
  </template>

  <div v-else class="notice warn">
    <strong>중앙 모니터링을 사용하지 않습니다</strong>
    <code class="inline">MONITORING_REFRESH_ENABLED=false</code> 로 설정되어 토큰 회전 스케줄러가 아예
    생성되지 않습니다. 서버가 멈춰도 중앙에서 알 수 없으니, 캠퍼스에서 직접 확인해야 합니다.
    나중에 켜려면 <code class="inline">back/.env.prod</code> 를 수정하고 컨테이너를 재시작하면 됩니다.
  </div>

  <StepNav :next-disabled="!canProceed" next-reason="엔드포인트·캠퍼스 라벨·부트스트랩 시크릿을 모두 입력하세요." />
</template>
