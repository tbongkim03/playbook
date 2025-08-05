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
                        </div>
                    </div>

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
                                type="password" 
                                class="form-input" 
                                :placeholder="isAdminMode ? '관리자 비밀번호를 입력하세요' : '비밀번호를 입력하세요'" 
                                v-model="password" 
                                @keydown.space.prevent 
                                @keydown="blockJavascriptInput"
                                required
                            >
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

                <!-- 링크 메뉴 (일반 사용자 모드일 때만 표시) -->
                <div v-if="!isAdminMode" class="link-menu">
                    <div class="link-items">
                        <a href="" class="link-item">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <path d="M19.4 15C19.2669 15.3016 19.2272 15.6362 19.286 15.9606C19.3448 16.285 19.4995 16.5842 19.73 16.82L19.79 16.88C19.976 17.0657 20.1235 17.2863 20.2241 17.5291C20.3248 17.7719 20.3766 18.0322 20.3766 18.295C20.3766 18.5578 20.3248 18.8181 20.2241 19.0609C20.1235 19.3037 19.976 19.5243 19.79 19.71C19.6043 19.896 19.3837 20.0435 19.1409 20.1441C18.8981 20.2448 18.6378 20.2966 18.375 20.2966C18.1122 20.2966 17.8519 20.2448 17.6091 20.1441C17.3663 20.0435 17.1457 19.896 16.96 19.71L16.9 19.65C16.6642 19.4195 16.365 19.2648 16.0406 19.206C15.7162 19.1472 15.3816 19.1869 15.08 19.32C14.7842 19.4468 14.532 19.6572 14.3543 19.9255C14.1766 20.1938 14.0813 20.5082 14.08 20.83V21C14.08 21.5304 13.8693 22.0391 13.4942 22.4142C13.1191 22.7893 12.6104 23 12.08 23C11.5496 23 11.0409 22.7893 10.6658 22.4142C10.2907 22.0391 10.08 21.5304 10.08 21V20.91C10.0723 20.579 9.96512 20.2569 9.77251 19.9859C9.5799 19.7148 9.31074 19.5067 9 19.385C8.69838 19.2518 8.36381 19.2121 8.03941 19.2709C7.71502 19.3297 7.41583 19.4844 7.18 19.715L7.12 19.775C6.93425 19.961 6.71368 20.1085 6.47088 20.2091C6.22808 20.3098 5.96783 20.3616 5.705 20.3616C5.44217 20.3616 5.18192 20.3098 4.93912 20.2091C4.69632 20.1085 4.47575 19.961 4.29 19.775C4.10405 19.5893 3.95653 19.3687 3.85588 19.1259C3.75523 18.8831 3.70343 18.6228 3.70343 18.36C3.70343 18.0972 3.75523 17.8369 3.85588 17.5941C3.95653 17.3513 4.10405 17.1307 4.29 16.945L4.35 16.885C4.58054 16.6492 4.73519 16.35 4.794 16.0256C4.85282 15.7012 4.81312 15.3666 4.68 15.065C4.55324 14.7692 4.34276 14.517 4.07447 14.3393C3.80618 14.1616 3.49179 14.0663 3.17 14.065H3C2.46957 14.065 1.96086 13.8543 1.58579 13.4792C1.21071 13.1041 1 12.5954 1 12.065C1 11.5346 1.21071 11.0259 1.58579 10.6508C1.96086 10.2757 2.46957 10.065 3 10.065H3.09C3.42099 10.0573 3.742 9.95012 4.01309 9.75751C4.28417 9.5649 4.49226 9.29574 4.615 8.985C4.74826 8.68338 4.78796 8.34881 4.72914 8.02441C4.67032 7.70002 4.51566 7.40083 4.285 7.165L4.225 7.105C4.03905 6.91925 3.89153 6.69868 3.79088 6.45588C3.69023 6.21308 3.63843 5.95283 3.63843 5.69C3.63843 5.42717 3.69023 5.16692 3.79088 4.92412C3.89153 4.68132 4.03905 4.46075 4.225 4.275C4.41075 4.08905 4.63132 3.94153 4.87412 3.84088C5.11692 3.74023 5.37717 3.68843 5.64 3.68843C5.90283 3.68843 6.16308 3.74023 6.40588 3.84088C6.64868 3.94153 6.86925 4.08905 7.055 4.275L7.115 4.335C7.35083 4.56554 7.65002 4.72019 7.97441 4.779C8.29881 4.83782 8.63338 4.79812 8.935 4.665H9C9.29577 4.53824 9.54802 4.32776 9.72569 4.05947C9.90337 3.79118 9.99872 3.47679 10 3.155V3C10 2.46957 10.2107 1.96086 10.5858 1.58579C10.9609 1.21071 11.4696 1 12 1C12.5304 1 13.0391 1.21071 13.4142 1.58579C13.7893 1.96086 14 2.46957 14 3V3.09C14.0013 3.41179 14.0966 3.72618 14.2743 3.99447C14.452 4.26276 14.7042 4.47324 15 4.6C15.3016 4.73326 15.6362 4.77296 15.9606 4.71414C16.285 4.65532 16.5842 4.50066 16.82 4.27L16.88 4.21C17.0657 4.02405 17.2863 3.87653 17.5291 3.77588C17.7719 3.67523 18.0322 3.62343 18.295 3.62343C18.5578 3.62343 18.8181 3.67523 19.0609 3.77588C19.3037 3.87653 19.5243 4.02405 19.71 4.21C19.896 4.39575 20.0435 4.61632 20.1441 4.85912C20.2448 5.10192 20.2966 5.36217 20.2966 5.625C20.2966 5.88783 20.2448 6.14808 20.1441 6.39088C20.0435 6.63368 19.896 6.85425 19.71 7.04L19.65 7.1C19.4195 7.33583 19.2648 7.63502 19.206 7.95941C19.1472 8.28381 19.1869 8.61838 19.32 8.92V9C19.4468 9.29577 19.6572 9.54802 19.9255 9.72569C20.1938 9.90337 20.5082 9.99872 20.83 10H21C21.5304 10 22.0391 10.2107 22.4142 10.5858C22.7893 10.9609 23 11.4696 23 12C23 12.5304 22.7893 13.0391 22.4142 13.4142C22.0391 13.7893 21.5304 14 21 14H20.91C20.5882 14.0013 20.2738 14.0966 20.0055 14.2743C19.7372 14.452 19.5268 14.7042 19.4 15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            비밀번호 찾기
                        </a>
                        <div class="divider"></div>
                        <a href="" class="link-item">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M8 7V3C8 2.46957 8.21071 1.96086 8.58579 1.58579C8.96086 1.21071 9.46957 1 10 1H18C18.5304 1 19.0391 1.21071 19.4142 1.58579C19.7893 1.96086 20 2.46957 20 3V21C20 21.5304 19.7893 22.0391 19.4142 22.4142C19.0391 22.7893 18.5304 23 18 23H10C9.46957 23 8.96086 22.7893 8.58579 22.4142C8.21071 22.0391 8 21.5304 8 21V17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <polyline points="15,10 12,7 15,4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <line x1="15" y1="7" x2="3" y2="7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            아이디 찾기
                        </a>
                        <div class="divider"></div>
                        <a @click.prevent="goToTerms" class="link-item signup-link">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M16 21V19C16 17.9391 15.5786 16.9217 14.8284 16.1716C14.0783 15.4214 13.0609 15 12 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <circle cx="8.5" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <line x1="20" y1="8" x2="20" y2="14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <line x1="17" y1="11" x2="23" y2="11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            회원가입
                        </a>
                    </div>
                </div>

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

function setLoginMode(adminMode) {
  isAdminMode.value = adminMode
  // 모드 변경 시 입력 값 초기화
  userId.value = ''
  password.value = ''
}

function goToTerms() {
  router.push({
    name: 'PageTerm',
    state: { fromLogin: true }
  })
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
    
    // 로그인 성공 시 jwt 토큰 로컬 스토리지에 저장
    localStorage.setItem('jwtToken', res.data.token)
    
    // 관리자와 일반 사용자 구분해서 저장 (필요한 경우)
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
  // JavaScript 입력 방지 로직이 있다면 여기에
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

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  transform: translateY(-2px);
}

.form-input:focus ~ .input-icon {
  color: #667eea;
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
}
</style>