<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { store, patch, toast } from '../store'
import StepNav from '../components/StepNav.vue'
import SecretField from '../components/SecretField.vue'

const id = ref('')
const name = ref('')
const discord = ref('')
const result = ref(null)

const m = computed(() => (store.state ? store.state.master : null))

onMounted(async () => {
  id.value = (m.value && m.value.id) || ''
  name.value = (m.value && m.value.name) || ''
  discord.value = (m.value && m.value.discord) || ''
  await validate()
})

async function saveField() {
  await patch({
    master: { id: id.value.trim(), name: name.value.trim(), discord: discord.value.trim().toLowerCase() }
  })
  await validate()
}

async function validate() {
  const r = await window.wizard.validateMaster()
  if (r.ok) result.value = r.result
}

const errors = computed(() => (result.value ? result.value.errors : { id: [], pw: [], name: [], discord: [] }))
const warnings = computed(() => (result.value ? result.value.warnings : []))
const strength = computed(() => (result.value ? result.value.strength : null))

const strengthLabel = computed(() => {
  if (!strength.value) return ''
  return { weak: '약함', fair: '보통', strong: '강함' }[strength.value.level]
})
const strengthColor = computed(() => {
  if (!strength.value) return 'var(--pb-text-faint)'
  return { weak: 'var(--pb-danger)', fair: 'var(--pb-warn)', strong: 'var(--pb-success)' }[strength.value.level]
})

const canProceed = computed(() => !!(result.value && result.value.ok))
</script>

<template>
  <h1 class="page-title">5. 마스터 관리자 계정</h1>
  <p class="page-lead">
    서비스가 처음 뜰 때 만들어지는 최상위 관리자 계정입니다. 이 계정으로 도서·회원·접속 허용 IP·연동 설정을
    모두 관리합니다.
  </p>

  <div class="notice danger">
    <strong>기본값 admin / admin1234 는 사용할 수 없습니다</strong>
    이 계정은 접속 허용 IP 규칙까지 바꿀 수 있습니다. 기본값을 그대로 두면 캠퍼스 네트워크 안에서 누구나
    관리자 화면에 들어올 수 있습니다.
  </div>

  <div class="card">
    <div class="field">
      <label class="field-label">아이디<span class="req">*</span></label>
      <input v-model="id" type="text" class="mono" autocomplete="off" placeholder="예) pb-manager" @change="saveField" @blur="saveField" />
      <div v-for="(e, i) in errors.id" :key="i" class="field-error">{{ e }}</div>
      <div class="field-hint">4~20자 영문·숫자·(_ . -). 환경변수 <code class="inline">MASTER_ID</code> 로 저장됩니다.</div>
    </div>

    <SecretField
      path="master.pw"
      label="비밀번호"
      required
      :secret="m ? m.pw : {}"
      placeholder="10자 이상, 대문자·소문자·숫자·특수문자 중 3종류 이상"
      hint="환경변수 MASTER_PW 로 저장되고, 최초 기동 시 BCrypt 로 해시되어 DB 에 들어갑니다."
      @saved="validate"
    />

    <div v-if="strength" class="field" style="margin-top: -4px">
      <div class="row" style="gap: 8px">
        <span class="faint" style="font-size: 12px">비밀번호 강도</span>
        <b :style="{ color: strengthColor }">{{ strengthLabel }}</b>
        <span class="faint" style="font-size: 12px">({{ strength.passed }}/{{ strength.total }} 조건 충족)</span>
      </div>
      <div class="row mt-8" style="gap: 6px; font-size: 11.5px">
        <span :class="strength.checks.length ? 'pill ok' : 'pill idle'">10자 이상</span>
        <span :class="strength.checks.lower ? 'pill ok' : 'pill idle'">소문자</span>
        <span :class="strength.checks.upper ? 'pill ok' : 'pill idle'">대문자</span>
        <span :class="strength.checks.digit ? 'pill ok' : 'pill idle'">숫자</span>
        <span :class="strength.checks.symbol ? 'pill ok' : 'pill idle'">특수문자</span>
        <span :class="strength.checks.noRepeat ? 'pill ok' : 'pill idle'">반복 없음</span>
        <span :class="strength.checks.noSequence ? 'pill ok' : 'pill idle'">연속 없음</span>
      </div>
      <div v-for="(e, i) in errors.pw" :key="i" class="field-error">{{ e }}</div>
      <div v-for="(w, i) in warnings" :key="'w' + i" class="check-hint">{{ w }}</div>
    </div>

    <div class="field">
      <label class="field-label">이름<span class="req">*</span></label>
      <input v-model="name" type="text" placeholder="예) 라운지 매니저" @change="saveField" @blur="saveField" />
      <div v-for="(e, i) in errors.name" :key="i" class="field-error">{{ e }}</div>
      <div class="field-hint">환경변수 <code class="inline">MASTER_NAME</code>.</div>
    </div>

    <div class="field">
      <label class="field-label">디스코드 사용자명<span class="req">*</span></label>
      <input v-model="discord" type="text" class="mono" placeholder="예) bubble_94" @change="saveField" @blur="saveField" />
      <div v-for="(e, i) in errors.discord" :key="i" class="field-error">{{ e }}</div>
      <div class="field-hint">
        표시 이름이 아니라 <b>@사용자명</b> 입니다. 디스코드에서 이 이름으로 연동 버튼을 눌러야 관리자 계정과
        연결됩니다. 환경변수 <code class="inline">MASTER_DISCORD</code>.
      </div>
    </div>
  </div>

  <div class="notice info">
    <strong>비밀번호를 잊으면</strong>
    설치 경로의 <code class="inline">back/.env.prod</code> 에 평문으로 남아 있고, 10단계에서 저장하는 백업
    파일에도 기록됩니다. 다만 계정이 이미 만들어진 뒤에는 환경변수를 바꿔도 DB 비밀번호가 따라 바뀌지 않으니,
    변경은 관리자 화면에서 하세요.
  </div>

  <StepNav :next-disabled="!canProceed" next-reason="입력값 오류를 먼저 해결하세요." />
</template>
