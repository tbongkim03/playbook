<template>
    <div class="terms-wrapper">
        <div class="terms-container">
            <div class="page-title">
                <router-link to="/" custom v-slot="{ navigate }">
                    <img src="@/assets/playbook_logo-removebg-preview.png" alt="Logo" class="logo-img" @click="navigate" />
                </router-link>
                <h1>약관 동의</h1>
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
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <polyline points="20,6 9,17 4,12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
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
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                    <polyline points="14,2 14,8 20,8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                    <line x1="16" y1="13" x2="8" y2="13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                    <line x1="16" y1="17" x2="8" y2="17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                    <polyline points="10,9 9,9 8,9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
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
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <line x1="5" y1="12" x2="19" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <polyline points="12,5 19,12 12,19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </div>
                    </button>
                </div>
            </div>

            <!-- 진행 상태 표시 -->
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

        </div>
    </div>
</template>

<script setup>
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
  .progress-indicator { flex-direction: column; gap: 0.75rem; }
  .progress-line { width: 2px; height: 32px; margin: 0; }
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