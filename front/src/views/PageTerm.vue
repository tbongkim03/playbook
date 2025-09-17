<template>
    <div class="terms-wrapper">
        <div class="terms-container">
            <!-- 로고 헤더 -->
            <header class="terms-header">
                <router-link to="/" custom v-slot="{ navigate }">
                    <div class="logo-container" @click="navigate">
                        <img src="@/assets/playbook_logo-removebg-preview.png" alt="Logo" class="logo-img" />
                    </div>
                </router-link>
                <div class="header-text">
                    <h1>약관 동의</h1>
                    <p>서비스 이용을 위해 아래 약관에 동의해주세요</p>
                </div>
            </header>

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

            <!-- 장식 요소 -->
            <div class="decoration-elements">
                <div class="floating-shape shape-1"></div>
                <div class="floating-shape shape-2"></div>
                <div class="floating-shape shape-3"></div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
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

const fromLogin = window.history.state?.fromLogin

if (!fromLogin) {
  alert('잘못된 접근입니다.')
  router.replace('/')
}

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
    path: '/register',
    name: 'PageRegister',
    state: {
      fromTerm: true
    }
  })
}
</script>

<style scoped>
.terms-wrapper {
  min-height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
  margin-top: -6rem;
  position: relative;
  overflow: hidden;
}

.terms-container {
  width: 100%;
  max-width: 650px;
  position: relative;
  z-index: 10;
}

.terms-header {
  text-align: center;
  margin-bottom: 2rem;
}

.logo-container {
  display: inline-block;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 1.5rem;
}

.logo-container:hover {
  transform: scale(1.05);
}

.logo-img {
  max-width: 180px;
  height: auto;
  transition: all 0.3s ease;
}

.header-text h1 {
  font-size: 2.2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 0.5rem 0;
}

.header-text p {
  font-size: 1.1rem;
  color: #64748b;
  margin: 0;
  font-weight: 400;
}

.terms-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 2.5rem;
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.1),
    0 4px 25px rgba(102, 126, 234, 0.1),
    0 0 0 1px rgba(102, 126, 234, 0.05);
  border: 1px solid rgba(102, 126, 234, 0.1);
  margin-bottom: 2rem;
}

.all-agree-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  color: white;
}

.all-agree-checkbox {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-bottom: 0.5rem;
}

.checkbox-input {
  display: none;
}

.checkbox-custom {
  width: 24px;
  height: 24px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 6px;
  margin-right: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.1);
}

.checkbox-input:checked + .checkbox-custom {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(255, 255, 255, 0.9);
  color: #667eea;
  transform: scale(1.1);
}

.checkbox-input:not(:checked) + .checkbox-custom svg {
  opacity: 0;
  transform: scale(0.5);
}

.checkbox-input:checked + .checkbox-custom svg {
  opacity: 1;
  transform: scale(1);
}

.checkbox-custom svg {
  transition: all 0.3s ease;
}

.checkbox-label {
  font-size: 1.2rem;
  font-weight: 600;
  user-select: none;
}

.all-agree-description {
  font-size: 0.95rem;
  opacity: 0.9;
  margin-left: 36px;
  line-height: 1.5;
}

.terms-list {
  margin-bottom: 2rem;
}

.terms-section {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 1.5rem;
  background: #fafbfc;
}

.section-title {
  display: flex;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e2e8f0;
}

.title-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  color: white;
  margin-right: 12px;
}

.section-title h3 {
  font-size: 1.3rem;
  font-weight: 600;
  color: #1e293b;
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
  gap: 12px;
  padding: 16px 32px;
  border: none;
  border-radius: 12px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  min-width: 200px;
}

.next-button:disabled {
  background: #e2e8f0;
  color: #94a3b8;
  cursor: not-allowed;
  transform: none;
}

.next-button.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.next-button.active::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s;
}

.next-button.active:hover::before {
  left: 100%;
}

.next-button.active:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
}

.button-text {
  position: relative;
  z-index: 1;
}

.button-icon {
  position: relative;
  z-index: 1;
  transition: transform 0.3s ease;
}

.next-button.active:hover .button-icon {
  transform: translateX(4px);
}

.progress-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 2rem;
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  margin-bottom: 0.5rem;
  transition: all 0.3s ease;
  background: #e2e8f0;
  color: #94a3b8;
}

.progress-step.active .step-number {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: scale(1.1);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.progress-step span {
  font-size: 0.9rem;
  color: #64748b;
  font-weight: 500;
}

.progress-step.active span {
  color: #1e293b;
  font-weight: 600;
}

.progress-line {
  width: 60px;
  height: 2px;
  background: #e2e8f0;
  margin: 0 1rem;
}

.decoration-elements {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.floating-shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(102, 126, 234, 0.08);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 80px;
  height: 80px;
  top: 15%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 60px;
  height: 60px;
  top: 65%;
  right: 15%;
  animation-delay: 2s;
}

.shape-3 {
  width: 100px;
  height: 100px;
  bottom: 25%;
  left: 5%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
    opacity: 0.3;
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
    opacity: 0.6;
  }
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .terms-wrapper {
    padding: 1rem;
  }
  
  .terms-card {
    padding: 2rem;
  }
  
  .header-text h1 {
    font-size: 1.8rem;
  }
  
  .progress-indicator {
    flex-direction: column;
    gap: 1rem;
  }
  
  .progress-line {
    width: 2px;
    height: 40px;
    margin: 0;
  }
  
  .all-agree-section {
    padding: 1.25rem;
  }
  
  .section-title {
    flex-direction: column;
    text-align: center;
    gap: 0.5rem;
  }
  
  .title-icon {
    margin-right: 0;
  }
}

@media (max-width: 480px) {
  .terms-card {
    padding: 1.5rem;
  }
  
  .header-text h1 {
    font-size: 1.6rem;
  }
  
  .next-button {
    width: 100%;
    padding: 14px 20px;
    font-size: 1rem;
  }
}

/* 기존 약관 박스 스타일 유지 */
:deep(.terms-box) {
  width: 100%;
  overflow: auto;
  box-sizing: border-box;
  max-height: 120px;
  margin: 9px 0;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: white;
  font-size: 0.9rem;
  line-height: 1.5;
}

:deep(.terms-box::-webkit-scrollbar) {
  width: 8px;
}

:deep(.terms-box::-webkit-scrollbar-thumb) {
  background-color: #cbd5e1;
  border-radius: 4px;
}

:deep(.terms-box::-webkit-scrollbar-track) {
  background-color: #f1f5f9;
  border-radius: 4px;
}

/* 체크박스 스타일 개선 */
:deep(input[type="checkbox"]) {
  display: none;
}

:deep(.checkbox-wrapper) {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
  cursor: pointer;
}

:deep(.checkbox-custom-small) {
  width: 20px;
  height: 20px;
  border: 2px solid #cbd5e1;
  border-radius: 4px;
  margin-right: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  background: white;
}

:deep(input[type="checkbox"]:checked + .checkbox-custom-small) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  color: white;
}
</style>