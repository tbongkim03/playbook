<template>
    <div class="login-wrapper">
        <div class="login-format">
            <header>
                <router-link to="/" custom v-slot="{ navigate }">
                    <img src="@/assets/playbook_logo.png" alt="Logo" class="logo-img" @click="navigate" />
                </router-link>
            </header>
            <form @submit.prevent="handleLogin">
                <input type="text" class="input-id" placeholder="아이디" v-model="userId">
                <input type="password" class="input-pw" placeholder="비밀번호" v-model="password">
                <button type="submit">
                    <span>로그인</span>
                </button>
            </form>
            <div class="lis">
                <ul>
                    <li>
                        <a href="">비밀번호 찾기</a>
                    </li>
                    <li class="li-b">
                        <a href="">아이디 찾기</a>
                    </li>
                    <li class="li-b">
                        <a @click.prevent="goToTerms">회원가입</a>
                    </li>
                </ul>
            </div>
        </div>
        <footer>
            <span>Copyright © 2025 플레이데이터 All Rights Reserved.</span>
        </footer>
    </div>
</template>

<script setup>
import axios from 'axios'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const userId = ref('')
const password = ref('')

function goToTerms() {
  router.push({
    name: 'PageTerm',
    state: { fromLogin: true }
  })
}

async function handleLogin() {
  try {
    const loginData = {
      idUser: userId.value,
      password: password.value
    }
    const res = await axios.post('http://localhost:8080/users/login', loginData)
    // 로그인 성공 시 jwt 토큰 로컬 스토리지에 저장
    localStorage.setItem('jwtToken', res.data.token)

    router.push('/')
  } catch (error) {
    alert('로그인 실패: 아이디 또는 비밀번호를 확인하세요.')
    console.log(error)
  }
}


</script>

<style scoped>
.login-wrapper {
    min-width: 1457px;
    min-height: 98%;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
}
.login-format {
    min-width: 458px;
    flex: 1;
}
.login-format header {
    display: flex;
    justify-content: center;
}
.logo-img {
    height: auto;
    opacity: 1;
    -webkit-transition: .3s ease-in-out;
    transition: .3s ease-in-out;
    margin-bottom: 25px;
}
.logo-img:hover {
    opacity: .5;
}
.login-format form {
    display: flex;
    flex-direction: column;
    width: 100%;
    border: 1px solid #e1e3e5;
    border-radius: 12px;
    padding: 24px;
    margin-bottom: 24px;
}
.login-format input {
    border: 1px solid #c5ccd2;
}
.login-format input {
    padding: 16px 15px 16px;
    z-index: 1;
    outline-color: #00ab90;
}
.login-format .input-id {
    border-top-right-radius: 8px;
    border-top-left-radius: 8px;
    border-bottom: none;
}
.login-format .input-pw {
    border-bottom-right-radius: 8px;
    border-bottom-left-radius: 8px;
}
.login-format button {
    border: none;
    background-color: #00ab90;
    color: #fff;
    padding: 13px 20px;;
    margin-top: 24px;
    font-weight: 400;
    font-size: 17px;
    line-height: 24px;
    border-radius: 8px;
}
.login-format .lis {
    display: flex;
    justify-content: center;
}

.login-format .lis ul {
    display: flex;
    flex-wrap: wrap;
    padding: 0;
    margin: 0;
    list-style: none;
}

.login-format .lis li {
  padding: 0;
  margin: 0;
}
.login-format .lis .li-b::before {
    content: "|";
    padding: 0 13px;
}

.login-format .lis a {
    text-decoration: none;
    color: gray;
}
footer {
    flex: 0 0 auto;
    bottom: 0;
}
</style>