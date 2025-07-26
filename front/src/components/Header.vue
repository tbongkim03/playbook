<template>
  <header class="main-header">
    <router-link to="/" custom v-slot="{ navigate }">
      <img src="@/assets/playbook_logo.png" alt="Logo" class="logo-img" @click="navigate" />
    </router-link>

    <div class="login-button">
      <template v-if="isLogin">
        <span>{{ username }}님 환영합니다!</span>
        <router-link to="/logout" custom v-slot="{ navigate }">
          <button type="button" class="btn btn-primary" @click="navigate">
            로그아웃
          </button>
        </router-link>
      </template>

      <template v-else>
        <router-link to="/login" custom v-slot="{ navigate }">
          <button type="button" class="btn btn-primary" v-if="showLoginButton" @click="navigate">
            로그인
          </button>
        </router-link>
      </template>
    </div>
  </header>
</template>


<script setup>
import axios from 'axios'
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const isLogin = ref(false) // 로그인 여부
const username = ref('')    // 로그인 사용자 이름

const route = useRoute()

const showLoginButton = computed(() => {
  return !isLogin.value && route.path !== '/login'
})

const showLogoutButton = computed(() => {
  return isLogin.value
})

// 사용자 정보 가져오기 함수
async function fetchUserInfo() {
  const token = localStorage.getItem('jwtToken')
  if (!token) {
    isLogin.value = false
    username.value = ''
    return
  }
  try {
    const res = await axios.get('http://localhost:8080/users/me', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    console.log("me 테스트")
    console.log(res)
    // username.value = res.data.idUser || res.data.username || '사용자'
    // isLogin.value = true
  } catch (error) {
    console.log('사용자 정보 불러오기 실패:', error)
    isLogin.value = false
    username.value = ''
  }
}

onMounted(() => {
  fetchUserInfo()
})

</script>


<style scoped>
.main-header {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: fixed;
    top: 0;
    left: 0;
    z-index: 1000;
    background-color: white;
    padding: 1rem 2rem;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.logo-img {
    max-width: 200px;
    height: auto;
    opacity: 1;
    -webkit-transition: .3s ease-in-out;
    transition: .3s ease-in-out;
}

.logo-img:hover {
    opacity: .5;
}
</style>