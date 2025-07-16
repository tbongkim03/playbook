<template>
  <header class="main-header">
    <router-link to="/" custom v-slot="{ navigate }">
      <img src="@/assets/playbook_logo.png" alt="Logo" class="logo-img" @click="navigate" />
    </router-link>

    <div class="login-button">
      <router-link to="/login" custom v-slot="{ navigate }">
        <button
          type="button"
          class="btn btn-primary"
          v-if="showLoginButton"
          @click="navigate"
        >
          로그인
        </button>
      </router-link>

      <router-link to="/logout" custom v-slot="{ navigate }">
        <button
          type="button"
          class="btn btn-primary"
          v-if="showLogoutButton"
          @click="navigate"
        >
          로그아웃
        </button>
      </router-link>
    </div>
  </header>
</template>


<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'

const isLogin = ref(false) // 로그인 여부 (세션체크 등으로 갱신하면 됨)
const route = useRoute()

// 로그인 버튼 조건: 로그인 안 했고, 현재 페이지가 /login이 아닐 때
const showLoginButton = computed(() => {
  return !isLogin.value && route.path !== '/login'
})

// 로그아웃 버튼 조건: 로그인했을 때만
const showLogoutButton = computed(() => {
  return isLogin.value
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