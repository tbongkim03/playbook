<template>
    <div class="login-wrapper">
        <div class="login-container">
            <div class="page-title">
                <router-link to="/" custom v-slot="{ navigate }">
                    <img src="@/assets/playbook_logo-removebg-preview.png" alt="Logo" class="logo-img" @click="navigate" />
                </router-link>
                <h1>로그인</h1>
            </div>

            <!-- 로그인 폼 -->
            <div class="login-card">
                <!-- 로그인 모드 토글 버튼 -->
                <div class="login-mode-toggle">
                    <div class="toggle-container">
                        <button 
                            @click="setLoginMode(false)" 
                            :class="['toggle-btn', { active: !isAdminMode }]"
                        >
                            <PhUser weight="duotone" :size="18" />
                            일반 회원
                        </button>
                        <button 
                            @click="setLoginMode(true)" 
                            :class="['toggle-btn', { active: isAdminMode }]"
                        >
                            <PhUserGear weight="duotone" :size="18" />
                            관리자
                        </button>
                    </div>
                </div>

                <form @submit.prevent="handleLogin" class="login-form">
                    <!-- 아이디 입력 필드 -->
                    <div class="input-group">
                        <div class="input-container">
                            <div class="input-icon">
                                <PhUser weight="duotone" :size="20" />
                            </div>
                            <input 
                                type="text" 
                                class="form-input" 
                                :placeholder="isAdminMode ? '관리자 아이디를 입력하세요' : '아이디를 입력하세요'" 
                                v-model="userId" 
                                @keydown.space.prevent 
                                @keydown="blockJavascriptInput"
                                required
                            >
                            <!-- 아이디 지우기 버튼 -->
                            <button 
                                v-if="userId" 
                                type="button" 
                                class="input-action-btn clear-btn"
                                @click="clearUserId"
                                title="아이디 지우기"
                                tabindex="-1"
                            >
                                <PhXCircle weight="duotone" :size="16" />
                            </button>
                        </div>
                    </div>

                    <!-- 비밀번호 입력 필드 -->
                    <div class="input-group">
                        <div class="input-container">
                            <div class="input-icon">
                                <PhLockKey weight="duotone" :size="20" />
                            </div>
                            <input 
                                :type="showPassword ? 'text' : 'password'" 
                                class="form-input password-input" 
                                :placeholder="isAdminMode ? '관리자 비밀번호를 입력하세요' : '비밀번호를 입력하세요'" 
                                v-model="password" 
                                @keydown.space.prevent 
                                @keydown="blockJavascriptInput"
                                required
                            >
                            <!-- 비밀번호 액션 버튼들 -->
                            <div v-if="password" class="password-actions">
                                <!-- 비밀번호 지우기 버튼 -->
                                <button 
                                    type="button" 
                                    class="input-action-btn clear-btn"
                                    @click="clearPassword"
                                    title="비밀번호 지우기"
                                    tabindex="-1"
                                >
                                    <PhXCircle weight="duotone" :size="16" />
                                </button>
                                <!-- 비밀번호 보기/숨기기 버튼 -->
                                <button 
                                    type="button" 
                                    class="input-action-btn toggle-password-btn"
                                    @click="togglePasswordVisibility"
                                    :title="showPassword ? '비밀번호 숨기기' : '비밀번호 보기'"
                                    tabindex="-1"
                                >
                                    <!-- 눈 보이기 아이콘 -->
                                    <PhEye weight="duotone" :size="16" v-if="!showPassword" />
                                    <!-- 눈 숨기기 아이콘 -->
                                    <PhEyeSlash weight="duotone" :size="16" />
                                </button>
                            </div>
                        </div>
                    </div>

                    <button type="submit" class="login-button" :disabled="!userId || !password">
                        <span class="button-text">{{ isAdminMode ? '관리자 로그인' : '로그인' }}</span>
                        <div class="button-icon">
                            <PhSignIn weight="duotone" :size="20" />
                        </div>
                    </button>
                </form>

                <!-- 링크 메뉴 (일반 사용자 모드일 때만 표시) -->
                <div v-if="!isAdminMode" class="link-menu">
                    <div class="link-items">
                        <a @click.prevent="goToTerms" class="link-item signup-link">
                            <PhUserCheck weight="duotone" :size="16" />
                            회원가입
                        </a>
                        <span class="link-divider">|</span>
                        <a :href="discordServerUrl" target="_blank" rel="noopener noreferrer" class="link-item find-account-link">
                            <PhMagnifyingGlass weight="duotone" :size="16" />
                            아이디 · 비밀번호 찾기
                        </a>
                    </div>
                    <div class="find-account-hint">
                        <PhChatCircle weight="duotone" :size="13" style="flex-shrink:0" />
                        <span>디스코드 서버에서 <code>/findid</code> · <code>/resetpw</code> 명령어로 임시 비밀번호를 발급받을 수 있습니다</span>
                    </div>
                </div>

                <!-- 관리자 모드 안내 (관리자 모드일 때만 표시) -->
                <div v-if="isAdminMode" class="admin-notice">
                    <div class="notice-content">
                        <PhWarningCircle weight="duotone" :size="20" />
                        <span>관리자 계정으로 로그인합니다</span>
                    </div>
                </div>
            </div>

        </div>
    </div>
</template>

<script setup>
import { PhChatCircle, PhEye, PhEyeSlash, PhLockKey, PhMagnifyingGlass, PhSignIn, PhUser, PhUserCheck, PhUserGear, PhWarningCircle, PhXCircle } from '@phosphor-icons/vue'
import * as adminApi from '@/api/admin'
import * as userApi from '@/api/user'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { swAlert } from '@/utils/sweetAlert'

const router = useRouter()
const userId = ref('')
const password = ref('')
const isAdminMode = ref(false)
const showPassword = ref(false)
const discordServerUrl = import.meta.env.VITE_DISCORD_SERVER_URL || 'https://discord.com'

function setLoginMode(adminMode) {
  isAdminMode.value = adminMode
  // 모드 변경 시 입력 값 초기화
  userId.value = ''
  password.value = ''
  showPassword.value = false
}

function goToTerms() {
  router.push({
    name: 'PageTerm',
    state: { fromLogin: true }
  })
}

// 아이디 지우기 함수
function clearUserId() {
  userId.value = ''
}

// 비밀번호 지우기 함수
function clearPassword() {
  password.value = ''
}

// 비밀번호 표시/숨기기 토글 함수
function togglePasswordVisibility() {
  showPassword.value = !showPassword.value
}

async function handleLogin() {
  try {
    let loginData, apiUrl
    
    if (isAdminMode.value) {
      loginData = { idAdmin: userId.value, pwAdmin: password.value }
      await adminApi.login(loginData)
    } else {
      loginData = { idUser: userId.value, pwUser: password.value }
      await userApi.login(loginData)
    }

    sessionStorage.setItem('userType', isAdminMode.value ? 'admin' : 'user')

    // 사용자 정보 조회하여 캠퍼스 저장
    try {
      const userInfo = isAdminMode.value ? await adminApi.getMe() : await userApi.getMe()

      // 캠퍼스 정보가 있으면 저장
      if (userInfo.data.data) {
        if (isAdminMode.value && userInfo.data.data.seqCampus) {
          // 관리자의 경우
          const campus = userInfo.data.data.seqCampus
          if (campus) {
            sessionStorage.setItem('campusId', campus.seqCampus)
          }
        } else if (!isAdminMode.value && userInfo.data.data.seqCampus) {
          // 일반 사용자의 경우
          sessionStorage.setItem('campusId', userInfo.data.data.seqCampus)
        } else {
          // 캠퍼스 정보가 없는 경우 (과정 종료 등)
          sessionStorage.removeItem('campusId')
        }
      }
    } catch (infoError) {
      console.error('캠퍼스 정보 조회 실패:', infoError)
      // 캠퍼스 정보 조회 실패해도 로그인은 진행
    }

    // 성공 시 메인 페이지로 이동
    router.push('/')
    
  } catch (error) {
    let errorMessage = '알 수 없는 오류가 발생했습니다.'
    
    if (error.response) {
      // 서버에서 응답이 온 경우
      if (error.response.status === 401) {
        errorMessage = isAdminMode.value 
          ? '관리자 아이디 또는 비밀번호가 올바르지 않습니다.' 
          : '아이디 또는 비밀번호가 올바르지 않습니다.'
      } else if (error.response.data) {
        // 백엔드에서 문자열로 에러 메시지를 보낸 경우
        errorMessage = typeof error.response.data === 'string' 
          ? error.response.data 
          : error.response.data.message || errorMessage
      }
    } else if (error.request) {
      // 네트워크 오류
      errorMessage = '서버에 연결할 수 없습니다. 네트워크를 확인해주세요.'
    }
    
    await swAlert(errorMessage, 'error')
  }
}

async function blockJavascriptInput(event) {
  const input = event.target.value;
  
  // JavaScript 관련 키워드 패턴
  const jsPatterns = [
    /<script[^>]*>.*?<\/script>/gi,
    /javascript:/gi,
    /on\w+\s*=/gi, // onclick, onload 등
    /eval\s*\(/gi,
    /Function\s*\(/gi,
    /setTimeout\s*\(/gi,
    /setInterval\s*\(/gi
  ];
  
  // 패턴 검사
  for (let pattern of jsPatterns) {
    if (pattern.test(input)) {
      event.preventDefault();
      await swAlert('JavaScript 코드는 입력할 수 없습니다.', 'warning');
      
      // 해당 부분 제거
      event.target.value = input.replace(pattern, '');
      return false;
    }
  }
}
</script>

<style scoped>
.login-wrapper {
  min-height: calc(100vh - var(--pb-header-height));
  width: 100%;
  background: var(--pb-color-canvas);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 48px 2rem;
}

.login-container {
  width: 100%;
  max-width: 480px;
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

.login-card {
  background: var(--pb-color-surface);
  border-radius: var(--pb-radius-lg);
  padding: 2.5rem;
  box-shadow: var(--pb-shadow-md);
  border: 1px solid var(--pb-color-border);
}

.login-mode-toggle {
  margin-bottom: 2rem;
}

.toggle-container {
  display: flex;
  background: var(--pb-color-surface-subtle);
  border-radius: var(--pb-radius-md);
  padding: 4px;
}

.toggle-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  background: transparent;
  border: none;
  border-radius: var(--pb-radius-sm);
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--pb-color-text-muted);
  cursor: pointer;
  transition: background 0.15s, color 0.15s, box-shadow 0.15s;
}

.toggle-btn.active {
  background: var(--pb-color-surface);
  color: var(--pb-color-brand);
  box-shadow: var(--pb-shadow-xs);
}

.toggle-btn:hover:not(.active) {
  color: var(--pb-color-text);
  background: var(--pb-color-surface-muted);
}

.login-form {
  margin-bottom: 2rem;
}

.input-group {
  margin-bottom: 1.5rem;
}

.input-container {
  position: relative;
  width: 100%;
}

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--pb-color-text-muted);
  z-index: 2;
}

.form-input {
  width: 100%;
  padding: 16px 16px 16px 48px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-md);
  font-size: 1rem;
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  transition: border-color 0.15s, box-shadow 0.15s;
  box-sizing: border-box;
}

.password-input {
  padding-right: 80px;
}

.form-input:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px var(--pb-color-brand-soft);
}

.input-action-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--pb-color-text-soft);
  cursor: pointer;
  transition: color 0.15s;
  padding: 4px;
  border-radius: var(--pb-radius-xs);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
}

.input-action-btn:hover {
  color: var(--pb-color-text-muted);
}

.clear-btn {
  right: 16px;
}

.password-actions {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  gap: 4px;
  z-index: 3;
}

.password-actions .clear-btn {
  position: relative;
  right: 0;
  top: 0;
  transform: none;
}

.toggle-password-btn {
  position: relative;
  right: 0;
  top: 0;
  transform: none;
}

.toggle-password-btn:hover {
  color: var(--pb-color-brand);
}

.login-button {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px 24px;
  background: var(--pb-color-brand);
  border: none;
  border-radius: var(--pb-radius-md);
  color: white;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}

.login-button:hover:not(:disabled) {
  background: var(--pb-color-brand-strong);
}

.login-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.button-icon {
  transition: transform 0.2s ease;
}

.login-button:hover .button-icon {
  transform: translateX(4px);
}

.link-menu {
  border-top: 1px solid var(--pb-color-border);
  padding-top: 1.5rem;
}

.link-items {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.link-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--pb-color-text-muted);
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: 500;
  padding: 8px 12px;
  border-radius: var(--pb-radius-sm);
  transition: color 0.15s, background 0.15s;
}

.link-item:hover {
  color: var(--pb-color-text);
  background: var(--pb-color-surface-muted);
}

.signup-link {
  color: var(--pb-color-brand);
  font-weight: 600;
}

.signup-link:hover {
  color: var(--pb-color-brand-strong);
  background: var(--pb-color-brand-soft);
}

.link-divider {
  color: var(--pb-color-border-strong);
  font-size: 0.75rem;
  user-select: none;
}

.find-account-link {
  color: var(--pb-color-text-muted);
}

.find-account-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 0.75rem;
  font-size: 0.8rem;
  color: var(--pb-color-text-soft, var(--pb-color-text-muted));
  text-align: center;
}

.find-account-hint code {
  background: var(--pb-color-surface-subtle);
  border: 1px solid var(--pb-color-border);
  border-radius: 4px;
  padding: 1px 5px;
  font-size: 0.75rem;
  color: var(--pb-color-text);
}

.divider {
  width: 1px;
  height: 16px;
  background: var(--pb-color-border);
}

.admin-notice {
  border-top: 1px solid var(--pb-color-border);
  padding-top: 1.5rem;
}

.notice-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--pb-color-brand-soft);
  border-radius: var(--pb-radius-sm);
  color: var(--pb-color-brand);
  font-size: 0.9rem;
  font-weight: 600;
}

@media (max-width: 768px) {
  .login-wrapper { padding: 1rem; }
  .login-card { padding: 2rem; }
  .welcome-text h1 { font-size: 2rem; }
  .welcome-text p { font-size: 1rem; }
  .toggle-btn { font-size: 0.8rem; padding: 10px 12px; min-height: 44px; }
  .link-items { flex-direction: column; gap: 0.5rem; }
  .link-item { min-height: 44px; }
  .divider { width: 80%; height: 1px; }
  .password-input { padding-right: 76px; }
  .password-actions { gap: 2px; }
}

@media (max-width: 480px) {
  .login-card { padding: 1.5rem; }
  .welcome-text h1 { font-size: 1.75rem; }
  .form-input { padding: 14px 14px 14px 44px; }
  .password-input { padding-right: 70px; }
  .login-button { padding: 14px 20px; font-size: 1rem; }
  .toggle-btn { font-size: 0.75rem; padding: 8px 10px; gap: 6px; min-height: 44px; }
  .toggle-btn svg { width: 16px; height: 16px; }
  .input-action-btn { padding: 3px; }

  .input-action-btn svg {
    width: 14px;
    height: 14px;
  }
}
</style>