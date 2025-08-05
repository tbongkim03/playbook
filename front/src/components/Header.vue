<template>
  <header class="main-header">
    <div class="header-content">
      <router-link to="/" custom v-slot="{ navigate }">
        <div class="logo-container" @click="navigate">
          <img src="@/assets/playbook_logo.png" alt="Logo" class="logo-img" />
        </div>
      </router-link>

      <div class="user-section">
        <template v-if="isLogin">
          <div class="user-info">
            <div class="user-avatar">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <span class="username">{{ username }} 님</span>
          </div>
          <router-link to="/logout" custom v-slot="{ navigate }">
            <button type="button" class="logout-btn" @click="navigate">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M9 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <polyline points="16,17 21,12 16,7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <line x1="21" y1="12" x2="9" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              로그아웃
            </button>
          </router-link>
        </template>

        <template v-else>
          <router-link to="/login" custom v-slot="{ navigate }">
            <button type="button" class="login-btn" v-if="showLoginButton" @click="navigate">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M15 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <polyline points="10,17 15,12 10,7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <line x1="15" y1="12" x2="3" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              로그인
            </button>
          </router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import axios from 'axios'
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const isLogin = ref(false) // 로그인 여부
const username = ref('')    // 로그인 사용자 이름

const route = useRoute()
const router = useRouter()

const showLoginButton = computed(() => {
  return !isLogin.value && route.path !== '/login'
})

const showLogoutButton = computed(() => {
  return isLogin.value
})

// 사용자 정보 가져오기 함수
async function fetchUserInfo() {
  const token = localStorage.getItem('jwtToken');
  if (!token) {
    isLogin.value = false;
    username.value = '';
    return;
  }

  try {
    const headers = {
      Authorization: `Bearer ${token}`
    };

    const [userRes, adminRes] = await Promise.allSettled([
      axios.get('http://localhost:8080/users/me', { headers }),
      axios.get('http://localhost:8080/admin/me', { headers })
    ]);

    if (userRes.status === 'fulfilled') {
      const data = userRes.value.data;
      username.value = data.nameUser || data.idUser || '사용자';
      isLogin.value = true;
    } else if (adminRes.status === 'fulfilled') {
      const data = adminRes.value.data;
      username.value = data.nameAdmin || data.idAdmin || '관리자';
      isLogin.value = true;
    } else {
      // 둘 다 실패한 경우
      const userError = userRes.reason?.response?.data || userRes.reason?.message;
      const adminError = adminRes.reason?.response?.data || adminRes.reason?.message;
      alert("사용자 정보 불러오기 실패");
      localStorage.removeItem('jwtToken');
      localStorage.removeItem('userType');
      isLogin.value = false;
      username.value = '';
      router.push('/login');
    }
  } catch (error) {
    alert('예기치 못한 오류 발생: ' + (error?.message || error));
    isLogin.value = false;
    username.value = '';
  }
}


onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.main-header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 1040;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 
    0 1px 3px rgba(0, 0, 0, 0.05),
    0 4px 20px rgba(0, 0, 0, 0.08);
  animation: slideDown 0.5s ease-out;
}

@keyframes slideDown {
  from {
    transform: translateY(-100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.header-content {
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 32px;
  height: 72px;
}

.logo-container {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 12px;
  padding: 8px;
}

.logo-container:hover {
  background: rgba(102, 126, 234, 0.05);
  transform: scale(1.02);
}

.logo-img {
  max-width: 180px;
  height: auto;
  transition: all 0.3s ease;
  filter: brightness(1) saturate(1);
}

.logo-container:hover .logo-img {
  filter: brightness(1.1) saturate(1.2);
}

.user-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
}

.user-info:hover {
  background: rgba(102, 126, 234, 0.08);
  border-color: rgba(102, 126, 234, 0.2);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  color: white;
  transition: all 0.3s ease;
}

.user-info:hover .user-avatar {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.username {
  font-weight: 600;
  font-size: 0.95rem;
  color: #1e293b;
  letter-spacing: -0.01em;
}

.login-btn,
.logout-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: 2px solid transparent;
  border-radius: 12px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  text-decoration: none;
  letter-spacing: -0.01em;
  position: relative;
  overflow: hidden;
}

.login-btn::before,
.logout-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s;
}

.login-btn:hover::before,
.logout-btn:hover::before {
  left: 100%;
}

.login-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.login-btn:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.login-btn:active {
  transform: translateY(0) scale(0.98);
}

.logout-btn {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
  border-color: rgba(239, 68, 68, 0.2);
}

.logout-btn:hover {
  background: #dc2626;
  color: white;
  border-color: #dc2626;
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 8px 25px rgba(220, 38, 38, 0.3);
}

.logout-btn:active {
  transform: translateY(0) scale(0.98);
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .header-content {
    padding: 12px 16px;
  }
  
  .logo-img {
    max-width: 140px;
  }
  
  .user-info {
    padding: 6px 12px;
    gap: 8px;
  }
  
  .user-avatar {
    width: 28px;
    height: 28px;
  }
  
  .username {
    font-size: 0.85rem;
  }
  
  .login-btn,
  .logout-btn {
    padding: 8px 16px;
    font-size: 0.85rem;
  }
}

@media (max-width: 480px) {
  .user-section {
    gap: 8px;
  }
  
  .username {
    display: none;
  }
  
  .user-info {
    padding: 8px;
  }
  
  .login-btn span,
  .logout-btn span {
    display: none;
  }
}

/* 다크모드 대응 (선택사항) */
@media (prefers-color-scheme: dark) {
  .main-header {
    background: rgba(15, 23, 42, 0.98);
    border-bottom-color: rgba(255, 255, 255, 0.1);
  }
  
  .username {
    color: #f1f5f9;
  }
  
  .user-info {
    background: rgba(100, 116, 139, 0.1);
    border-color: rgba(100, 116, 139, 0.2);
  }
  
  .user-info:hover {
    background: rgba(100, 116, 139, 0.15);
  }
}
</style>