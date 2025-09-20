<template>
    <div class="login-wrapper">
        <div class="login-container">
            <!-- 로고 헤더 -->
            <header class="login-header">
                <router-link to="/" custom v-slot="{ navigate }">
                    <div class="logo-container" @click="navigate">
                        <img src="@/assets/playbook_logo-removebg-preview.png" alt="Logo" class="logo-img" />
                    </div>
                </router-link>
                <div class="welcome-text">
                    <h1>환영합니다</h1>
                    <p>계정에 로그인하여 도서관 서비스를 이용하세요</p>
                </div>
            </header>

            <!-- 로그인 폼 -->
            <div class="login-card">
                <!-- 로그인 모드 토글 버튼 -->
                <div class="login-mode-toggle">
                    <div class="toggle-container">
                        <button 
                            @click="setLoginMode(false)" 
                            :class="['toggle-btn', { active: !isAdminMode }]"
                        >
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            일반 회원
                        </button>
                        <button 
                            @click="setLoginMode(true)" 
                            :class="['toggle-btn', { active: isAdminMode }]"
                        >
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            관리자
                        </button>
                    </div>
                </div>

                <form @submit.prevent="handleLogin" class="login-form">
                    <!-- 아이디 입력 필드 -->
                    <div class="input-group">
                        <div class="input-container">
                            <div class="input-icon">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                    <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
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
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                  <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                  <path d="M15 9l-6 6M9 9l6 6" stroke="currentColor" stroke-width="2"/>
                                </svg>
                            </button>
                        </div>
                    </div>

                    <!-- 비밀번호 입력 필드 -->
                    <div class="input-group">
                        <div class="input-container">
                            <div class="input-icon">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                    <circle cx="12" cy="16" r="1" fill="currentColor"/>
                                    <path d="M7 11V7C7 5.67392 7.52678 4.40215 8.46447 3.46447C9.40215 2.52678 10.6739 2 12 2C13.3261 2 14.5979 2.52678 15.5355 3.46447C16.4732 4.40215 17 5.67392 17 7V11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
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
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                      <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                                      <path d="M15 9l-6 6M9 9l6 6" stroke="currentColor" stroke-width="2"/>
                                    </svg>
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
                                    <svg v-if="!showPassword" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="2"/>
                                        <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
                                    </svg>
                                    <!-- 눈 숨기기 아이콘 -->
                                    <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" stroke="currentColor" stroke-width="2"/>
                                        <line x1="1" y1="1" x2="23" y2="23" stroke="currentColor" stroke-width="2"/>
                                    </svg>
                                </button>
                            </div>
                        </div>
                    </div>

                    <button type="submit" class="login-button" :disabled="!userId || !password">
                        <span class="button-text">{{ isAdminMode ? '관리자 로그인' : '로그인' }}</span>
                        <div class="button-icon">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M15 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <polyline points="10,17 15,12 10,7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <line x1="15" y1="12" x2="3" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </div>
                    </button>
                </form>

                <!-- 관리자 모드 안내 (관리자 모드일 때만 표시) -->
                <div v-if="isAdminMode" class="admin-notice">
                    <div class="notice-content">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12 9V13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            <path d="M12 17H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        <span>관리자 계정으로 로그인합니다</span>
                    </div>
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
import axios from 'axios'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const userId = ref('')
const password = ref('')
const isAdminMode = ref(false)
const showPassword = ref(false)

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
      // 관리자 로그인
      loginData = {
        idAdmin: userId.value,
        pwAdmin: password.value
      }
      apiUrl = 'http://localhost:8080/admin/login'
    } else {
      // 일반 사용자 로그인
      loginData = {
        idUser: userId.value,
        pwUser: password.value
      }
      apiUrl = 'http://localhost:8080/users/login'
    }

    const res = await axios.post(apiUrl, loginData)
    
    localStorage.setItem('jwtToken', res.data.token)
    localStorage.setItem('userType', isAdminMode.value ? 'admin' : 'user')

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
    
    alert(errorMessage)
  }
}

function blockJavascriptInput(event) {
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
      alert('JavaScript 코드는 입력할 수 없습니다.');
      
      // 해당 부분 제거
      event.target.value = input.replace(pattern, '');
      return false;
    }
  }
}
</script>

<style scoped>
.login-wrapper {
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

.login-container {
  width: 100%;
  max-width: 480px;
  position: relative;
  z-index: 10;
}

.login-header {
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
  max-width: 200px;
  height: auto;
  transition: all 0.3s ease;
}

.welcome-text h1 {
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 0.5rem 0;
  text-shadow: none;
}

.welcome-text p {
  font-size: 1.1rem;
  color: #64748b;
  margin: 0;
  font-weight: 400;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 2.5rem;
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.1),
    0 4px 25px rgba(102, 126, 234, 0.1),
    0 0 0 1px rgba(102, 126, 234, 0.05);
  border: 1px solid rgba(102, 126, 234, 0.1);
}

/* 로그인 모드 토글 스타일 */
.login-mode-toggle {
  margin-bottom: 2rem;
}

.toggle-container {
  display: flex;
  background: #f1f5f9;
  border-radius: 12px;
  padding: 4px;
  position: relative;
  overflow: hidden;
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
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 2;
}

.toggle-btn.active {
  background: white;
  color: #667eea;
  box-shadow: 
    0 2px 8px rgba(0, 0, 0, 0.1),
    0 1px 3px rgba(102, 126, 234, 0.2);
  transform: translateY(-1px);
}

.toggle-btn:hover:not(.active) {
  color: #475569;
  background: rgba(255, 255, 255, 0.5);
}

.toggle-btn svg {
  transition: all 0.3s ease;
}

.toggle-btn.active svg {
  color: #667eea;
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
  color: #64748b;
  z-index: 2;
  transition: all 0.3s ease;
}

.form-input {
  width: 100%;
  padding: 16px 16px 16px 48px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 1rem;
  background: #ffffff;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-sizing: border-box;
}

.password-input {
  padding-right: 80px;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  transform: translateY(-2px);
}

.form-input:focus ~ .input-icon {
  color: #667eea;
}

/* 입력 필드 액션 버튼들 스타일 */
.input-action-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 4px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
}

.input-action-btn:hover {
  color: #64748b;
  background: rgba(100, 116, 139, 0.1);
}

.clear-btn {
  right: 16px;
}

/* 비밀번호 필드의 액션 버튼들 */
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
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

.login-button {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  position: relative;
  overflow: hidden;
}

.login-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s;
}

.login-button:hover::before {
  left: 100%;
}

.login-button:hover:not(:disabled) {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
}

.login-button:active:not(:disabled) {
  transform: translateY(0) scale(0.98);
}

.login-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
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

.login-button:hover .button-icon {
  transform: translateX(4px);
}

.link-menu {
  border-top: 1px solid #e2e8f0;
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
  color: #64748b;
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: 500;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.link-item:hover {
  color: #475569;
  background: rgba(100, 116, 139, 0.1);
  transform: translateY(-1px);
}

.signup-link {
  color: #667eea;
  font-weight: 600;
}

.signup-link:hover {
  color: #5a67d8;
  background: rgba(102, 126, 234, 0.1);
}

.divider {
  width: 1px;
  height: 16px;
  background: #e2e8f0;
}

/* 관리자 모드 안내 스타일 */
.admin-notice {
  border-top: 1px solid #e2e8f0;
  padding-top: 1.5rem;
}

.notice-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  border-radius: 8px;
  color: #667eea;
  font-size: 0.9rem;
  font-weight: 600;
}

.notice-content svg {
  color: #667eea;
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
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 60px;
  height: 60px;
  top: 70%;
  right: 15%;
  animation-delay: 2s;
}

.shape-3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
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
  .login-wrapper {
    padding: 1rem;
  }
  
  .login-card {
    padding: 2rem;
  }
  
  .welcome-text h1 {
    font-size: 2rem;
  }
  
  .welcome-text p {
    font-size: 1rem;
  }
  
  .toggle-btn {
    font-size: 0.8rem;
    padding: 10px 12px;
  }
  
  .link-items {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .divider {
    width: 80%;
    height: 1px;
  }
  
  .password-input {
    padding-right: 76px;
  }
  
  .password-actions {
    gap: 2px;
  }
}

@media (max-width: 480px) {
  .login-card {
    padding: 1.5rem;
  }
  
  .welcome-text h1 {
    font-size: 1.75rem;
  }
  
  .form-input {
    padding: 14px 14px 14px 44px;
  }
  
  .password-input {
    padding-right: 70px;
  }
  
  .login-button {
    padding: 14px 20px;
    font-size: 1rem;
  }
  
  .toggle-btn {
    font-size: 0.75rem;
    padding: 8px 10px;
    gap: 6px;
  }
  
  .toggle-btn svg {
    width: 16px;
    height: 16px;
  }
  
  .input-action-btn {
    padding: 3px;
  }
  
  .input-action-btn svg {
    width: 14px;
    height: 14px;
  }
}
</style>