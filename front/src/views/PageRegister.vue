<template>
<div class="register-wrapper">
        <div class="register-format">
            <header>
                <router-link to="/" custom v-slot="{ navigate }">
                    <img src="@/assets/playbook_logo.png" alt="Logo" class="logo-img" @click="navigate" />
                </router-link>
            </header>
            <form @submit.prevent class="register-form">
                <input
                    type="text"
                    v-model="name"
                    @blur="validateName"
                    :class="{ 'input-error': errors.name }"
                    placeholder="이름"
                >
                <input
                    type="text"
                    v-model="username"
                    @blur="validateUsername"
                    :class="{ 'input-error': errors.username }"
                    placeholder="아이디"
                >
                <input
                    type="password"
                    v-model="password"
                    @blur="validatePassword"
                    :class="{ 'input-error': errors.password }"
                    placeholder="비밀번호"
                >

                <ul class="rule-message">
                    <li v-if="errors.name">{{ errors.name }}</li>
                    <li v-if="errors.username">{{ errors.username }}</li>
                    <li v-if="errors.password">{{ errors.password }}</li>
                </ul>

                <input
                    type="text"
                    class="register-discord"
                    id="registerDiscord"
                    placeholder="디스코드 아이디 (반납일 알림)"
                >

                <button type="submit">
                    <span>회원가입</span>
                </button>
                </form>
        </div>
        
        <footer>
            <span>Copyright © 2025 플레이데이터 All Rights Reserved.</span>
        </footer>
    </div>

</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const termsAgree = route.query.terms === 'true'
const infoAgree = route.query.info === 'true'
const discordAgree = route.query.discord === 'true'

const name = ref('')
const username = ref('')
const password = ref('')

const errors = ref({
  name: '',
  username: '',
  password: ''
})

// 유효성 검사 함수 (임시로 API 없이 구현, 나중에 실제 API 호출 부분 넣어줘)
function validateName() {
  if (!name.value.trim()) {
    errors.value.name = '이름: 필수 정보입니다.'
  } else {
    errors.value.name = ''
  }
}

function validateUsername() {
  if (username.value === 'admin') {
    errors.value.username = '아이디: 사용할 수 없는 아이디입니다. 다른 아이디를 입력해 주세요.'
  } else {
    errors.value.username = ''
  }
}

function validatePassword() {
  if (password.value.length < 6) {
    errors.value.password = '비밀번호: 6자 이상 입력해 주세요.'
  } else {
    errors.value.password = ''
  }
}
</script>

<style>
.register-wrapper {
    min-width: 1460px;
    min-height: 100%;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    justify-content: center;
    justify-self: center;
    align-items: center;
}
.register-format {
    min-width: 458px;
    flex: 1;
}
.register-form {
    display: flex;
    flex-direction: column;
}
.rule-message {
    height: fit-content;
    margin: 10px 0;
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
.register-format input {
    border: 1px solid #c5ccd2;
}
.register-format input {
    padding: 16px 15px 16px;
    z-index: 1;
    outline-color: #00ab90;
}
.register-format .regiseter-name {
    border-top-right-radius: 8px;
    border-top-left-radius: 8px;
}
.regiseter-format .regiseter-id {
    border: none;
}
.register-format .register-pw {
    border-bottom-right-radius: 8px;
    border-bottom-left-radius: 8px;
}
.rule-message {
  font-size: 13px;
  min-height: 39px;
  color: red;
  margin: 10px 0;
  padding-left: 4px;
}
.input-error {
  border: 1px solid red !important;
}
input:focus {
  outline-color: #00ab90;
}
.register-format button {
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
</style>