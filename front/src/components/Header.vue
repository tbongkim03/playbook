<template>
  <header class="main-header">
    <div class="header-content">
      <div class="logo-container" @click="goHome">
        <img src="@/assets/playbook_logo-removebg-preview.png" alt="Logo" class="logo-img" />
      </div>

      <div class="user-section">
        <template v-if="isLogin">
          <!-- 사용자 정보 클릭시 페이지 이동 -->
          <router-link 
            :to="isAdmin ? '/admin' : '/users'" 
            custom 
            v-slot="{ navigate }"
          >
            <div class="user-info" @click="navigate">
              <div class="user-avatar">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              <span class="username">{{ username }}({{ campusName || '정보 없음' }})</span>
            </div>
          </router-link>

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
import * as adminApi from '@/api/admin'
import * as userApi from '@/api/user'
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const isLogin = ref(false) // 로그인 여부
const username = ref('')    // 로그인 사용자 이름
const isAdmin = ref(false)  // 관리자 여부
const campusName = ref('')  // 캠퍼스 이름

const route = useRoute()
const router = useRouter()

const showLoginButton = computed(() => {
  return !isLogin.value && route.path !== '/login'
})

function goHome() {
  window.location.href = '/';
}


// 사용자 정보 가져오기 함수
async function fetchUserInfo() {
  const userType = sessionStorage.getItem('userType');
  if (!userType) {
    isLogin.value = false;
    username.value = '';
    isAdmin.value = false;
    return;
  }

  try {
    if (userType === 'user') {
      const userRes = await userApi.checkMe();
      if (userRes.status === 200) {
        const data = userRes.data.data;
        if (data.seqCampus) {
          sessionStorage.setItem('campusId', data.seqCampus);
          campusName.value = data.campusName || '정보 없음';
        } else {
          sessionStorage.removeItem('campusId');
          campusName.value = '정보 없음';
        }
        username.value = data.nameUser || data.idUser || '사용자';
        isLogin.value = true;
        isAdmin.value = false;
        return;
      }
    } else if (userType === 'admin') {
      const adminRes = await adminApi.checkMe();
      if (adminRes.status === 200) {
        const data = adminRes.data.data;
        if (data.seqCampus?.seqCampus) {
          sessionStorage.setItem('campusId', data.seqCampus?.seqCampus);
          campusName.value = data.seqCampus?.nameCampus || '정보 없음';
        } else {
          campusName.value = '전체';
        }
        username.value = data.nameAdmin || data.idAdmin || '관리자';
        isLogin.value = true;
        isAdmin.value = true;
        return;
      }
    }

    // 세션 만료 등 실패 시
    sessionStorage.removeItem('userType');
    sessionStorage.removeItem('campusId');
    isLogin.value = false;
    username.value = '';
    isAdmin.value = false;
    campusName.value = '';
  } catch (error) {
    isLogin.value = false;
    username.value = '';
    isAdmin.value = false;
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
  height: var(--pb-header-height);
  z-index: 1040;
  background: var(--pb-color-canvas);
  border-bottom: 1px solid var(--pb-color-border);
  display: flex;
  align-items: center;
}

.header-content {
  width: min(100% - 48px, var(--pb-content-max));
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.logo-container {
  display: flex;
  align-items: center;
  cursor: pointer;
  border-radius: var(--pb-radius-md);
  padding: 4px 6px;
  transition: background 0.12s ease;
}
.logo-container:hover {
  background: var(--pb-color-surface-muted);
}
.logo-img {
  height: 28px;
  width: auto;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 10px 5px 6px;
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-md);
  cursor: pointer;
  text-decoration: none;
  color: inherit;
  transition: border-color 0.12s ease, background 0.12s ease;
}
.user-info:hover {
  background: var(--pb-color-brand-soft);
  border-color: var(--pb-color-brand-muted);
  text-decoration: none;
  color: inherit;
}

.user-avatar {
  width: 24px;
  height: 24px;
  border-radius: 999px;
  background: var(--pb-color-brand);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.username {
  font-size: 13px;
  font-weight: 500;
  color: var(--pb-color-text);
}

.login-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  padding: 0 14px;
  font-size: 13px;
  font-weight: 500;
  background: var(--pb-color-brand);
  color: #fff;
  border: 1px solid var(--pb-color-brand);
  border-radius: var(--pb-radius-md);
  cursor: pointer;
  transition: background 0.12s ease;
}
.login-btn:hover {
  background: var(--pb-color-brand-strong);
  border-color: var(--pb-color-brand-strong);
}

.logout-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  padding: 0 12px;
  font-size: 13px;
  font-weight: 500;
  background: transparent;
  color: var(--pb-color-text-muted);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-md);
  cursor: pointer;
  transition: background 0.12s ease, color 0.12s ease;
}
.logout-btn:hover {
  background: var(--pb-color-danger-soft);
  color: var(--pb-color-danger);
  border-color: var(--pb-color-danger);
}

@media (max-width: 768px) {
  .header-content { width: calc(100% - 32px); }
  .logo-img { height: 24px; }
  .username { display: none; }
}
</style>
