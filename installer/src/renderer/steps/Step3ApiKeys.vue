<script setup>
import { computed, onMounted, ref } from 'vue'
import { store, patch, toast } from '../store'
import StepNav from '../components/StepNav.vue'
import SecretField from '../components/SecretField.vue'
import StatusPill from '../components/StatusPill.vue'

const urls = ref({})
const verifying = ref({ kakao: false, nl: false, work24: false })

const keys = computed(() => (store.state ? store.state.apiKeys : null))
const verify = computed(() => (store.state ? store.state.apiVerify : {}))

onMounted(async () => {
  const r = await window.wizard.apiIssueUrls()
  if (r.ok) urls.value = r.urls
})

function statusOf(which) {
  const v = verify.value ? verify.value[which] : null
  if (!v) return 'idle'
  return v.ok ? 'ok' : 'fail'
}

function messageOf(which) {
  const v = verify.value ? verify.value[which] : null
  return v ? v.message : ''
}

async function run(which) {
  verifying.value[which] = true
  try {
    const r = await window.wizard.verifyApiKey(which)
    if (!r.ok) {
      toast(r.message || '검증 요청에 실패했습니다.', 'error')
      return
    }
    const s = await window.wizard.getState()
    if (s.ok) store.state = s.state
    toast(r.result.message, r.result.ok ? 'success' : 'error', 6500)
  } finally {
    verifying.value[which] = false
  }
}

async function open(url) {
  const r = await window.wizard.openExternal(url)
  if (!r.ok) toast(r.message, 'error')
}

const allVerified = computed(() => {
  // 카카오는 선택 항목이라, 입력했을 때만 검증 완료를 요구한다
  const kakaoOk = !(keys.value && keys.value.kakaoRestApiKey.set) || statusOf('kakao') === 'ok'
  return kakaoOk && statusOf('nl') === 'ok' && statusOf('work24') === 'ok'
})
const anyMissing = computed(() => {
  const k = keys.value
  if (!k) return true
  // 카카오(표지 보조 조회)는 선택 — 비워 두면 국립중앙도서관 표지만 쓴다
  return !k.nlApiKey.set || !k.work24ApiKey.set
})
</script>

<template>
  <h1 class="page-title">3. 외부 API 키</h1>
  <p class="page-lead">
    도서 검색(국립중앙도서관 · 카카오)과 훈련과정 동기화(Work24)에 쓰이는 키입니다. 각 항목의
    <b>[검증]</b> 버튼은 실제 API 를 호출해 키가 살아 있는지 그 자리에서 확인합니다 — 백엔드의 연동 테스트와 같은
    요청을 보냅니다.
  </p>

  <div class="notice info">
    <strong>입력한 키는 화면에 남지 않습니다</strong>
    저장 즉시 마법사 내부로 옮겨지고 이후에는 마스킹된 형태만 표시됩니다. 서비스가 뜨면 이 값들은
    데이터베이스에 AES 로 암호화되어 저장되고, 이후에는 관리자 화면의 연동 탭에서 관리합니다.
  </div>

  <!-- 국립중앙도서관 -->
  <h2 class="section-title">국립중앙도서관 ISBN(서지정보) API</h2>
  <div class="card">
    <div class="card-head">
      <StatusPill :status="statusOf('nl')" :labels="{ ok: '검증 완료', fail: '검증 실패', idle: '미검증' }" />
      <span class="grow"></span>
      <button class="small link" @click="open(urls.nl)">발급 페이지 열기 ↗</button>
    </div>

    <SecretField
      path="apiKeys.nlApiKey"
      label="인증키 (NL_API_KEY)"
      required
      :secret="keys ? keys.nlApiKey : {}"
      placeholder="국립중앙도서관 서지정보 API 인증키"
      hint="검증 시 표본 ISBN(9788966261208)으로 실제 조회를 시도합니다."
    />

    <div class="row">
      <button class="small primary" :disabled="verifying.nl" @click="run('nl')">
        {{ verifying.nl ? '검증 중…' : '검증' }}
      </button>
      <span v-if="messageOf('nl')" class="check-detail">{{ messageOf('nl') }}</span>
    </div>
  </div>

  <!-- Work24 -->
  <h2 class="section-title">Work24(고용24) 훈련과정 API</h2>
  <div class="card">
    <div class="card-head">
      <StatusPill :status="statusOf('work24')" :labels="{ ok: '검증 완료', fail: '검증 실패', idle: '미검증' }" />
      <span class="grow"></span>
      <button class="small link" @click="open(urls.work24)">발급 페이지 열기 ↗</button>
    </div>

    <SecretField
      path="apiKeys.work24ApiKey"
      label="API 키 (WORK24_API_KEY)"
      required
      :secret="keys ? keys.work24ApiKey : {}"
      placeholder="Work24 OpenAPI 인증키"
      hint="검증 시 최근 6개월 '플레이데이터평생교육원' 과정을 조회합니다. 0건이어도 키가 유효하면 성공으로 봅니다."
    />

    <div class="row">
      <button class="small primary" :disabled="verifying.work24" @click="run('work24')">
        {{ verifying.work24 ? '검증 중…' : '검증' }}
      </button>
      <span v-if="messageOf('work24')" class="check-detail">{{ messageOf('work24') }}</span>
    </div>
  </div>

  <!-- 카카오 -->
  <h2 class="section-title">카카오 책 검색 API <span class="faint">(선택)</span></h2>
  <div class="card">
    <div class="card-head">
      <StatusPill :status="statusOf('kakao')" :labels="{ ok: '검증 완료', fail: '검증 실패', idle: '미검증' }" />
      <span class="grow"></span>
      <button class="small link" @click="open(urls.kakao)">발급 페이지 열기 ↗</button>
    </div>

    <SecretField
      path="apiKeys.kakaoRestApiKey"
      label="REST API 키 (KAKAO_REST_API_KEY)"
      :secret="keys ? keys.kakaoRestApiKey : {}"
      placeholder="카카오 디벨로퍼스 앱의 REST API 키"
      hint="국립중앙도서관 결과에 표지가 없을 때 표지 이미지를 찾는 데 씁니다. 비워 두면 표지 없이 등록됩니다."
    />

    <div class="row">
      <button class="small primary" :disabled="verifying.kakao" @click="run('kakao')">
        {{ verifying.kakao ? '검증 중…' : '검증' }}
      </button>
      <span v-if="messageOf('kakao')" class="check-detail">{{ messageOf('kakao') }}</span>
    </div>
  </div>

  <div v-if="!allVerified" class="notice warn">
    <strong>검증하지 않고도 진행할 수는 있습니다</strong>
    다만 키가 틀린 채로 설치하면 도서 검색이나 과정 동기화가 조용히 실패합니다. 가능하면 입력한 항목 모두
    <b>검증 완료</b> 상태로 만든 뒤 넘어가세요. 설치 후에는 관리자 화면의 연동 탭에서 언제든 바꿀 수 있습니다.
  </div>

  <StepNav :next-disabled="anyMissing" next-reason="필수 키(국립중앙도서관 · Work24)를 입력해야 다음으로 넘어갈 수 있습니다." />
</template>
