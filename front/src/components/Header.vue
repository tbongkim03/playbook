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
            <button type="button" class="user-info" :title="`${username} · ${campusName || '정보 없음'} — ${isAdmin ? '관리자 화면' : '마이페이지'}`" @click="navigate">
              <span class="user-avatar" aria-hidden="true">{{ initial }}</span>
              <span class="user-text">
                <span class="username">{{ username }}</span>
                <span class="user-campus">{{ campusName || '정보 없음' }}{{ isAdmin ? ' · 관리자' : '' }}</span>
              </span>
            </button>
          </router-link>

          <router-link to="/logout" custom v-slot="{ navigate }">
            <button type="button" class="logout-btn" aria-label="로그아웃" @click="navigate">
              <PhSignOut weight="duotone" :size="18" />
              <span class="logout-text">로그아웃</span>
            </button>
          </router-link>
        </template>

        <template v-else>
          <router-link to="/login" custom v-slot="{ navigate }">
            <button type="button" class="login-btn" v-if="showLoginButton" @click="navigate">
              <PhSignIn weight="duotone" :size="18" />
              로그인
            </button>
          </router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { PhSignIn, PhSignOut } from '@phosphor-icons/vue'
import * as adminApi from '@/api/admin'
import * as userApi from '@/api/user'
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const isLogin = ref(false) // 로그인 여부
const username = ref('')    // 로그인 사용자 이름
const isAdmin = ref(false)  // 관리자 여부
const campusName = ref('')  // 캠퍼스 이름

// 아바타에는 이름 첫 글자를 쓴다 (기본 사람 아이콘은 누구로 로그인했는지 알려주지 못한다)
const initial = computed(() => (username.value || '?').trim().charAt(0).toUpperCase())

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
  gap: 10px;
  padding: 4px 12px 4px 4px;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 999px;
  cursor: pointer;
  color: inherit;
  text-align: left;
  transition: background 0.12s ease, border-color 0.12s ease;
}
.user-info:hover {
  background: var(--pb-color-surface);
  border-color: var(--pb-color-border);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 999px;
  background: var(--pb-color-brand-soft);
  color: var(--pb-color-brand-strong);
  border: 1px solid var(--pb-color-brand-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
}

.user-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.username {
  font-size: 13px;
  font-weight: 600;
  color: var(--pb-color-heading);
}

.user-campus {
  font-size: 11px;
  color: var(--pb-color-text-soft);
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
  .header-content { width: calc(100% - 24px); }
  .logo-img { height: 24px; }
  .user-text,
  .logout-text { display: none; }
  .user-section { gap: 4px; }
  .user-info { padding: 4px; }
  .user-avatar { width: 36px; height: 36px; font-size: 15px; }
  /* 아이콘만 남는 버튼도 손가락 크기(44px)는 지킨다 */
  .logout-btn { width: 44px; height: 44px; padding: 0; justify-content: center; border-color: transparent; }
  .login-btn { height: 40px; padding: 0 14px; }
}
</style>
