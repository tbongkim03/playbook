<template>
    <div class="terms-wrapper">
        <div class="terms-container">
            <div class="page-title">
                <router-link to="/" custom v-slot="{ navigate }">
                    <img src="@/assets/playbook_logo-removebg-preview.png" alt="Logo" class="logo-img" @click="navigate" />
                </router-link>
                <h1>약관 동의</h1>
            </div>

            <!-- 진행 단계 — 폼 위에 둬야 지금 몇 단계인지 먼저 보인다 -->
            <div class="progress-indicator">
                <div class="progress-step active">
                    <div class="step-number">1</div>
                    <span>약관 동의</span>
                </div>
                <div class="progress-line"></div>
                <div class="progress-step">
                    <div class="step-number">2</div>
                    <span>회원 정보 입력</span>
                </div>
                <div class="progress-line"></div>
                <div class="progress-step">
                    <div class="step-number">3</div>
                    <span>가입 완료</span>
                </div>
            </div>

            <!-- 약관 동의 카드 -->
            <div class="terms-card">
                <!-- 전체 동의 -->
                <div class="all-agree-section">
                    <label class="all-agree-checkbox">
                        <input 
                            type="checkbox" 
                            id="allTermsAgree" 
                            :checked="allAgree" 
                            @change="onAllAgreeChange"
                            class="checkbox-input"
                        >
                        <div class="checkbox-custom">
                            <PhCheck weight="duotone" :size="16" />
                        </div>
                        <span class="checkbox-label">전체 동의하기</span>
                    </label>
                    <div class="all-agree-description">
                        모든 약관에 동의하시면 서비스를 이용하실 수 있습니다.
                    </div>
                </div>

                <!-- 개별 약관 -->
                <div class="terms-list">
                    <div class="terms-section">
                        <div class="section-title">
                            <div class="title-icon">
                                <PhFileText weight="duotone" :size="20" />
                            </div>
                            <h3>개별 약관 동의</h3>
                        </div>
                        
                        <!-- 개별 약관 컴포넌트들 -->
                        <div class="terms-components">
                            <AgreeTermsUser v-model:isTermsAgree="termsAgree"/>
                            <AgreeInfoUser v-model:isInfoAgree="infoAgree"/>
                            <AgreeDiscordUser v-model:isDiscordAgree="discordAgree"/>
                        </div>
                    </div>
                </div>

                <!-- 다음 버튼 -->
                <div class="action-section">
                    <button 
                        class="next-button" 
                        :disabled="!allChecked" 
                        :class="{ active: allChecked }" 
                        @click="goToRegister"
                    >
                        <span class="button-text">다음 단계로</span>
                        <div class="button-icon">
                            <PhArrowRight weight="duotone" :size="20" />
                        </div>
                    </button>
                </div>
            </div>


        </div>
    </div>
</template>

<script setup>
import { PhArrowRight, PhCheck, PhFileText } from '@phosphor-icons/vue'
import { ref, watch, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { swAlert } from '@/utils/sweetAlert'
import AgreeTermsUser from '@/components/AgreeTermsUser.vue'
import AgreeInfoUser from '@/components/AgreeInfoUser.vue'
import AgreeDiscordUser from '@/components/AgreeDiscordUser.vue'

const termsAgree = ref(false)
const infoAgree = ref(false)
const discordAgree = ref(false)

const allAgree = ref(false)

const allChecked = computed(() => termsAgree.value && infoAgree.value && discordAgree.value)

const isManualTrigger = ref(false)

const route = useRoute()
const router = useRouter()

onMounted(async () => {
  const fromLogin = window.history.state?.fromLogin
  if (!fromLogin) {
    await swAlert('잘못된 접근입니다.', 'warning')
    router.replace('/')
  }
})

watch(allAgree, (agree) => {
  if (!isManualTrigger.value) return

  termsAgree.value = agree
  infoAgree.value = agree
  discordAgree.value = agree

  isManualTrigger.value = false
})

watch([termsAgree, infoAgree, discordAgree], ([term, info, discord]) => {
  const newAllAgree = term && info && discord
  
  if (allAgree.value !== newAllAgree) {
    allAgree.value = newAllAgree
  }
})

function onAllAgreeChange(event) {
  isManualTrigger.value = true
  allAgree.value = event.target.checked
}

function goToRegister() {
  if (!allChecked.value) return

  router.push({
    name: 'PageRegister',
    state: {
      fromTerm: true
    }
  })
}
</script>

<style scoped>
.terms-wrapper {
  min-height: calc(100vh - var(--pb-header-height));
  width: 100%;
  background: var(--pb-color-canvas);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 48px 2rem;
}

.terms-container {
  width: 100%;
  max-width: 650px;
}

.page-title {
  margin-bottom: 2rem;
}

.logo-img {
  display: block;
  height: 32px;
  width: auto;
  margin-bottom: 1.25rem;
  cursor: pointer;
}

.page-title h1 {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--pb-color-heading);
  margin: 0 0 0.375rem 0;
}

.page-title p {
  font-size: 0.95rem;
  color: var(--pb-color-text-muted);
  margin: 0;
}

.terms-card {
  background: var(--pb-color-surface);
  border-radius: var(--pb-radius-lg);
  padding: 2rem;
  box-shadow: var(--pb-shadow-md);
  border: 1px solid var(--pb-color-border);
  margin-bottom: 1.5rem;
}

.all-agree-section {
  background: var(--pb-color-brand);
  border-radius: var(--pb-radius-md);
  padding: 1.25rem;
  margin-bottom: 1.5rem;
  color: white;
}

.all-agree-checkbox {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-bottom: 0.375rem;
}

.checkbox-input {
  display: none;
}

.checkbox-custom {
  width: 22px;
  height: 22px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-radius: var(--pb-radius-xs);
  margin-right: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s;
  background: rgba(255, 255, 255, 0.15);
  flex-shrink: 0;
}

.checkbox-input:checked + .checkbox-custom {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(255, 255, 255, 0.95);
  color: var(--pb-color-brand);
}

.checkbox-input:not(:checked) + .checkbox-custom svg {
  opacity: 0;
}

.checkbox-input:checked + .checkbox-custom svg {
  opacity: 1;
}

.checkbox-label {
  font-size: 1rem;
  font-weight: 600;
  user-select: none;
}

.all-agree-description {
  font-size: 0.875rem;
  opacity: 0.88;
  margin-left: 32px;
  line-height: 1.5;
}

.terms-list {
  margin-bottom: 1.5rem;
}

.terms-section {
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-md);
  padding: 1.25rem;
  background: var(--pb-color-surface-subtle);
}

.section-title {
  display: flex;
  align-items: center;
  margin-bottom: 1.25rem;
  padding-bottom: 0.875rem;
  border-bottom: 1px solid var(--pb-color-border);
  gap: 10px;
}

.title-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  background: var(--pb-color-brand);
  border-radius: var(--pb-radius-sm);
  color: white;
  flex-shrink: 0;
}

.section-title h3 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0;
}

.terms-components {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.action-section {
  text-align: center;
}

.next-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 11px 28px;
  border: none;
  border-radius: var(--pb-radius-md);
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
  min-width: 180px;
}

.next-button:disabled {
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text-soft);
  cursor: not-allowed;
}

.next-button.active {
  background: var(--pb-color-brand);
  color: white;
}

.next-button.active:hover {
  background: var(--pb-color-brand-strong);
}

.progress-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1.5rem;
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.step-number {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.875rem;
  margin-bottom: 0.375rem;
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text-soft);
  border: 1px solid var(--pb-color-border);
}

.progress-step.active .step-number {
  background: var(--pb-color-brand);
  color: white;
  border-color: var(--pb-color-brand);
}

.progress-step span {
  font-size: 0.8rem;
  color: var(--pb-color-text-muted);
  font-weight: 500;
}

.progress-step.active span {
  color: var(--pb-color-heading);
  font-weight: 600;
}

.progress-line {
  width: 60px;
  height: 2px;
  background: var(--pb-color-border);
  margin: 0 1rem;
  margin-bottom: 1.25rem;
}

@media (max-width: 768px) {
  .terms-wrapper { padding: 1.5rem 1rem; }
  .terms-card { padding: 1.5rem; }
  /* 모바일에서도 가로 한 줄 — 세로로 세우면 단계 표시가 화면 하나를 다 먹는다 */
  .progress-indicator { flex-direction: row; gap: 0; align-items: flex-start; }
  /* 이름이 두 줄로 꺾여도 동그라미 높이가 맞도록 위쪽 기준, 선은 동그라미 가운데 높이에 */
  .progress-line { width: 20px; height: 2px; margin: 16px 2px 0; flex-shrink: 0; }
  .progress-step { min-width: 64px; text-align: center; }
  .progress-step span { font-size: 12px; line-height: 1.3; white-space: nowrap; }
  .all-agree-section { padding: 1rem; }
  .section-title { flex-direction: column; text-align: center; gap: 0.5rem; }
  .title-icon { margin-right: 0; }
}

@media (max-width: 480px) {
  .terms-card { padding: 1.25rem; }
  .header-text h1 { font-size: 1.3rem; }
  .next-button { width: 100%; }
}

:deep(.terms-box) {
  width: 100%;
  overflow: auto;
  box-sizing: border-box;
  max-height: 120px;
  margin: 8px 0;
  padding: 12px;
  border-radius: var(--pb-radius-sm);
  border: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface);
  font-size: 0.875rem;
  line-height: 1.5;
}

:deep(.terms-box::-webkit-scrollbar) { width: 6px; }
:deep(.terms-box::-webkit-scrollbar-thumb) { background-color: var(--pb-color-border); border-radius: 3px; }
:deep(.terms-box::-webkit-scrollbar-track) { background-color: var(--pb-color-surface-subtle); }
</style>