<template>
  <div class="register-wrapper">
    <div class="register-container">
      <!-- 로고 헤더 -->
      <header class="register-header">
        <router-link to="/" custom v-slot="{ navigate }">
          <div class="logo-container" @click="navigate">
            <img src="@/assets/playbook_logo-removebg-preview.png" alt="Logo" class="logo-img" />
          </div>
        </router-link>
        <div class="header-text">
          <h1>회원가입</h1>
        </div>
      </header>

      <!-- 회원가입 카드 -->
      <div class="register-card">
        <form @submit.prevent="handleSubmit" class="register-form">
          <!-- 개인 정보 섹션 -->
          <div class="form-section">
            <div class="section-header">
              <div class="section-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              <h3>개인 정보</h3>
            </div>

            <div class="input-group">
              <div class="input-container">
                <div class="input-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
                <input
                  type="text"
                  v-model="name"
                  @blur="validateName"
                  class="form-input"
                  :class="{ 
                    'error': errors.name,
                    'success': name && !errors.name
                  }"
                  placeholder="이름을 입력하세요"
                  @keydown="blockJavascriptInput"
                />
                <button 
                  v-if="name" 
                  type="button" 
                  class="input-action-btn clear-btn"
                  @click="clearName"
                  title="이름 지우기"
                  tabindex="-1"
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M15 9l-6 6M9 9l6 6" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </button>
              </div>
            </div>

            <div class="input-group">
              <div class="input-container">
                <div class="input-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M4 4H20C21.1 4 22 4.9 22 6V18C22 19.1 21.1 20 20 20H4C2.9 20 2 19.1 2 18V6C2 4.9 2.9 4 4 4Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <polyline points="22,6 12,13 2,6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
                <input
                  type="text"
                  v-model="username"
                  @blur="validateUsername"
                  class="form-input"
                  :class="{ 
                    'error': errors.username,
                    'success': username && !errors.username
                  }"
                  @keydown.space.prevent
                  @keydown="blockJavascriptInput"
                  placeholder="아이디를 입력하세요"
                />
                <button 
                  v-if="username" 
                  type="button" 
                  class="input-action-btn clear-btn"
                  @click="clearUsername"
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

            <div class="input-group">
              <div class="input-container">
                <div class="input-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <circle cx="12" cy="16" r="1" fill="currentColor"/>
                    <path d="M7 11V7C7 5.67392 7.52678 4.40215 8.46447 3.46447C9.40215 2.52678 10.6739 2 12 2C13.3261 2 14.5979 2.52678 15.5355 3.46447C16.4732 4.40215 17 5.67392 17 7V11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
                <input
                  :type="showPassword ? 'text' : 'password'"
                  v-model="password"
                  @blur="validatePassword"
                  class="form-input password-input"
                  :class="{ 
                    'error': errors.password,
                    'success': password.length >= 4 && !errors.password
                  }"
                  @keydown.space.prevent
                  @keydown="blockJavascriptInput"
                  placeholder="비밀번호를 입력하세요"
                />
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
            <div class="error-message" v-if="errors.name">{{ errors.name }}</div>
            <div class="error-message" v-if="errors.username">{{ errors.username }}</div>
            <div class="error-message" v-if="errors.password">{{ errors.password }}</div>
          </div>

          <!-- 알림 설정 섹션 -->
          <div class="form-section">
            <div class="section-header">
              <div class="section-icon discord">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M20.317 4.3698a19.7913 19.7913 0 00-4.8851-1.5152.0741.0741 0 00-.0785.0371c-.211.3753-.4447.8648-.6083 1.2495-1.8447-.2762-3.68-.2762-5.4868 0-.1636-.3933-.4058-.8742-.6177-1.2495a.077.077 0 00-.0785-.037 19.7363 19.7363 0 00-4.8852 1.515.0699.0699 0 00-.0321.0277C.5334 9.0458-.319 13.5799.0992 18.0578a.0824.0824 0 00.0312.0561c2.0528 1.5076 4.0413 2.4228 5.9929 3.0294a.0777.0777 0 00.0842-.0276c.4616-.6304.8731-1.2952 1.226-1.9942a.076.076 0 00-.0416-.1057c-.6528-.2476-1.2743-.5495-1.8722-.8923a.077.077 0 01-.0076-.1277c.1258-.0943.2517-.1923.3718-.2914a.0743.0743 0 01.0776-.0105c3.9278 1.7933 8.18 1.7933 12.0614 0a.0739.0739 0 01.0785.0095c.1202.099.246.1981.3728.2924a.077.077 0 01-.0066.1276 12.2986 12.2986 0 01-1.873.8914.0766.0766 0 00-.0407.1067c.3604.698.7719 1.3628 1.225 1.9932a.076.076 0 00.0842.0286c1.961-.6067 3.9495-1.5219 6.0023-3.0294a.077.077 0 00.0313-.0552c.5004-5.177-.8382-9.6739-3.5485-13.6604a.061.061 0 00-.0312-.0286zM8.02 15.3312c-1.1825 0-2.1569-1.0857-2.1569-2.419 0-1.3332.9555-2.4189 2.157-2.4189 1.2108 0 2.1757 1.0952 2.1568 2.419-.0189 1.3332-.9555 2.4189-2.1569 2.4189zm7.9748 0c-1.1825 0-2.1569-1.0857-2.1569-2.419 0-1.3332.9554-2.4189 2.1569-2.4189 1.2108 0 2.1757 1.0952 2.1568 2.419 0 1.3332-.9554 2.4189-2.1568 2.4189Z" fill="currentColor"/>
                </svg>
              </div>
              <h3>알림 설정</h3>
              <div class="section-badge">
                <span>🔔 반납 기한 알림</span>
              </div>
            </div>

            <div class="input-group">
              <div class="input-container">
                <div class="input-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M20.317 4.3698a19.7913 19.7913 0 00-4.8851-1.5152.0741.0741 0 00-.0785.0371c-.211.3753-.4447.8648-.6083 1.2495-1.8447-.2762-3.68-.2762-5.4868 0-.1636-.3933-.4058-.8742-.6177-1.2495a.077.077 0 00-.0785-.037 19.7363 19.7363 0 00-4.8852 1.515.0699.0699 0 00-.0321.0277C.5334 9.0458-.319 13.5799.0992 18.0578a.0824.0824 0 00.0312.0561c2.0528 1.5076 4.0413 2.4228 5.9929 3.0294a.0777.0777 0 00.0842-.0276c.4616-.6304.8731-1.2952 1.226-1.9942a.076.076 0 00-.0416-.1057c-.6528-.2476-1.2743-.5495-1.8722-.8923a.077.077 0 01-.0076-.1277c.1258-.0943.2517-.1923.3718-.2914a.0743.0743 0 01.0776-.0105c3.9278 1.7933 8.18 1.7933 12.0614 0a.0739.0739 0 01.0785.0095c.1202.099.246.1981.3728.2924a.077.077 0 01-.0066.1276 12.2986 12.2986 0 01-1.873.8914.0766.0766 0 00-.0407.1067c.3604.698.7719 1.3628 1.225 1.9932a.076.076 0 00.0842.0286c1.961-.6067 3.9495-1.5219 6.0023-3.0294a.077.077 0 00.0313-.0552c.5004-5.177-.8382-9.6739-3.5485-13.6604a.061.061 0 00-.0312-.0286zM8.02 15.3312c-1.1825 0-2.1569-1.0857-2.1569-2.419 0-1.3332.9555-2.4189 2.157-2.4189 1.2108 0 2.1757 1.0952 2.1568 2.419-.0189 1.3332-.9555 2.4189-2.1569 2.4189zm7.9748 0c-1.1825 0-2.1569-1.0857-2.1569-2.419 0-1.3332.9554-2.4189 2.1569-2.4189 1.2108 0 2.1757 1.0952 2.1568 2.419 0 1.3332-.9554 2.4189-2.1568 2.4189Z" fill="currentColor"/>
                  </svg>
                </div>
                <input
                  type="text"
                  v-model="discord"
                  @blur="validateDiscord"
                  class="form-input"
                  :class="{ 
                    'error': errors.discord,
                    'success': discord.length >= 6 && !errors.discord
                  }"
                  placeholder="디스코드 아이디를 입력하세요"
                />
                <button 
                  v-if="discord" 
                  type="button" 
                  class="input-action-btn clear-btn"
                  @click="clearDiscord"
                  title="디스코드 아이디 지우기"
                  tabindex="-1"
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M15 9l-6 6M9 9l6 6" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </button>
              </div>
              <div class="input-description">
                도서 반납 기한 알림을 받기 위해 디스코드 아이디가 필요합니다.
              </div>
              <div class="error-message" v-if="errors.discord">{{ errors.discord }}</div>
            </div>
          </div>

          <!-- 훈련과정 선택 섹션 -->
          <div class="form-section">
            <div class="section-header">
              <div class="section-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M22 10V6C22 4.9 21.1 4 20 4H4C2.9 4 2 4.9 2 6V10C3.1 10 4 10.9 4 12S3.1 14 2 14V18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V14C20.9 14 20 13.1 20 12S20.9 10 22 10Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M13 16L15.5 12L13 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M11 8L8.5 12L11 16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              <h3>훈련과정 선택</h3>
            </div>

            <div class="input-group">
              <div class="select-container" @click="toggleCourseBox" ref="dropdownWrapper">
                <div class="select-input" :class="{ 'error': errors.course, 'open': courseBoxOpen }">
                  <div class="select-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M22 10V6C22 4.9 21.1 4 20 4H4C2.9 4 2 4.9 2 6V10C3.1 10 4 10.9 4 12S3.1 14 2 14V18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V14C20.9 14 20 13.1 20 12S20.9 10 22 10Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </div>
                  <span class="select-text" :class="{ placeholder: !selectedCourse }">
                    {{ selectedCourse ? `${selectedCourse.title} ${selectedCourse.trprDegr}기` : '수강중인 훈련과정을 선택해주세요' }}
                  </span>
                  <div class="select-arrow">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" :class="{ rotated: courseBoxOpen }">
                      <polyline points="6,9 12,15 18,9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </div>
                </div>
                <div class="select-dropdown" :class="{ 'position-up': dropdownPositionUp, 'show': courseBoxOpen }">
                  <div class="dropdown-search">
                    <input 
                      type="text" 
                      v-model="searchQuery" 
                      placeholder="훈련과정 검색..." 
                      class="search-input"
                      @click.stop
                    >
                  </div>
                  <ul class="dropdown-list">
                    <li
                      v-for="(item, index) in filteredCourseList"
                      :key="index"
                      class="dropdown-item"
                      :class="{ selected: selectedCourse === item }"
                      @click.stop="selectCourse(item)"
                    >
                      <div class="course-info">
                        <div class="course-title">{{ item.title }}</div>
                        <div class="course-detail">{{ item.trprDegr }}기</div>
                      </div>
                    </li>
                    <li v-if="filteredCourseList.length === 0" class="no-results">
                      검색 결과가 없습니다.
                    </li>
                  </ul>
                </div>
              </div>
              <div class="error-message" v-if="errors.course">{{ errors.course }}</div>
            </div>
          </div>

          <!-- 제출 버튼 -->
          <div class="submit-section">
            <button type="submit" class="submit-button" :disabled="!isFormValid">
              <span class="button-text">회원가입 완료</span>
              <div class="button-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M22 11.08V12C21.9988 14.1564 21.3005 16.2547 20.0093 17.9818C18.7182 19.7088 16.9033 20.9725 14.8354 21.5839C12.7674 22.1953 10.5573 22.1219 8.53447 21.3746C6.51168 20.6273 4.78465 19.2461 3.61096 17.4371C2.43727 15.628 1.87979 13.4905 2.02168 11.3363C2.16356 9.18218 2.99721 7.13677 4.39828 5.49707C5.79935 3.85736 7.69279 2.71548 9.79619 2.24015C11.8996 1.76482 14.1003 1.98506 16.07 2.86" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <polyline points="22,4 12,14.01 9,11.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
            </button>
          </div>
        </form>
      </div>

      <!-- 진행 상태 표시 -->
      <div class="progress-indicator">
        <div class="progress-step completed">
          <div class="step-number">1</div>
          <span>약관 동의</span>
        </div>
        <div class="progress-line completed"></div>
        <div class="progress-step active">
          <div class="step-number">2</div>
          <span>회원 정보 입력</span>
        </div>
        <div class="progress-line"></div>
        <div class="progress-step">
          <div class="step-number">3</div>
          <span>가입 완료</span>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { swAlert } from '@/utils/sweetAlert'

const route = useRoute()
const router = useRouter()

const dropdownWrapper = ref(null)
const dropdownPositionUp = ref(false)
const courseBoxOpen = ref(false)
const searchQuery = ref('')

const showPassword = ref(false)
const name = ref('')
const username = ref('')
const password = ref('')
const discord = ref('')

const errors = ref({
  name: '',
  username: '',
  password: '',
  discord: '',
  course: ''
})

const courseList = ref([])
const selectedCourse = ref(null)


// 폼 유효성 검사
const isFormValid = computed(() => {
  return name.value && 
         username.value && 
         password.value && 
         discord.value && 
         selectedCourse.value &&
         !Object.values(errors.value).some(error => error !== '')
})

// 과정 검색 필터링
const filteredCourseList = computed(() => {
  if (!searchQuery.value) return courseList.value
  return courseList.value.filter(course => 
    course.title.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    course.trprDegr.toString().includes(searchQuery.value)
  )
})

function clearName() {
  name.value = ''
}

function clearUsername() {
  username.value = ''
}

function clearPassword() {
  password.value = ''
}

function clearDiscord() {
  discord.value = ''
}

// 비밀번호 표시/숨기기 토글 함수
function togglePasswordVisibility() {
  showPassword.value = !showPassword.value
}

// 날짜 계산
// const today = new Date()
// const sixMonthsAgo = new Date()
// sixMonthsAgo.setMonth(sixMonthsAgo.getMonth() - 6)

// function formatDateToYYYYMMDD(date) {
//   const yyyy = date.getFullYear()
//   const mm = String(date.getMonth() + 1).padStart(2, '0')
//   const dd = String(date.getDate()).padStart(2, '0')
//   return `${yyyy}${mm}${dd}`
// }

// const srchTraStDt = formatDateToYYYYMMDD(sixMonthsAgo)
// const srchTraEndDt = formatDateToYYYYMMDD(today)

onMounted(async () => {
  const fromTerm = window.history.state?.fromTerm
  if (!fromTerm) {
    await swAlert('잘못된 접근입니다.', 'warning')
    router.replace('/')
    return
  }
  getCourseList()
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})

async function getCourseList() {
  // const apiKey = import.meta.env.VITE_WORK24_API_KEY
  // const url =
  //   `https://www.work24.go.kr/cm/openApi/call/hr/callOpenApiSvcInfo310L01.do?authKey=${apiKey}` +
  //   `&returnType=JSON&outType=1&pageNum=1&pageSize=100` +
  //   `&srchTraStDt=${srchTraStDt}&srchTraEndDt=${srchTraEndDt}` +
  //   `&srchTraArea1=11&srchNcs1=20&crseTracseSe=C0104&srchTraGbn=M1001&srchTraOrganNm=플레이데이터평생교육원` +
  //   `&sort=ASC&sortCol=2`

  try {
    // 1. 외부 API에서 데이터 가져오기
    const res = await axios.get('/api/work24/course')
    const data = res.data.data
    const apiCoursesRaw = data?.srchList || []

    if (apiCoursesRaw.length === 0) {
      await swAlert('훈련 과정을 찾을 수 없습니다.', 'error')
      return
    }

    // 2. 외부 API 데이터 가공
    const apiCourses = apiCoursesRaw.map(item => {
      const title = item.title.includes(' - ') ? item.title.split(' - ')[0] : item.title
      const fullName = `${title} ${item.trprDegr}기`
      return {
        nameCourse: fullName,
        startDtCourse: item.traStartDate,
        finishDtCourse: item.traEndDate,
        trprDegr: item.trprDegr,
        seqCourse: item.trprId
      }
    })

    // 3. DB 데이터 가져오기
    const dbRes = await axios.get('/api/courses')
    const dbCourses = dbRes.data.data

    // 4. 추가: API에는 있는데 DB에는 없는 과정 → INSERT
    for (const apiItem of apiCourses) {
      const exists = dbCourses.find(dbItem => dbItem.nameCourse === apiItem.nameCourse)
      if (!exists) {
        await axios.post('/api/courses', {
          nameCourse: apiItem.nameCourse,
          startDtCourse: apiItem.startDtCourse,
          finishDtCourse: apiItem.finishDtCourse
        })
      }
    }

    // 5. 삭제: DB에는 있는데 API에는 없는 과정 → DELETE
    for (const dbItem of dbCourses) {
      const exists = apiCourses.find(apiItem => apiItem.nameCourse === dbItem.nameCourse)
      if (!exists) {
        await axios.delete(`/api/courses/${dbItem.seqCourse}`)
      }
    }

    // 6. 수정: 둘 다 있지만 데이터 변경되었으면 → UPDATE
    for (const apiItem of apiCourses) {
      const dbItem = dbCourses.find(db => db.nameCourse === apiItem.nameCourse)
      if (dbItem) {
        const isDifferent =
          dbItem.startDtCourse !== apiItem.startDtCourse ||
          dbItem.finishDtCourse !== apiItem.finishDtCourse

        if (isDifferent) {
          await axios.put(`/api/courses/${dbItem.seqCourse}`, {
            nameCourse: apiItem.nameCourse,
            startDtCourse: apiItem.startDtCourse,
            finishDtCourse: apiItem.finishDtCourse
          })
        }
      }
    }

    // 7. 모든 동기화 작업 완료 후 최신 DB 데이터를 다시 가져오기
    const finalDbRes = await axios.get('/api/courses')
    const finalDbCourses = finalDbRes.data.data

    // 8. 드롭다운 표시용 courseList 값 세팅
    courseList.value = finalDbCourses
      .map(item => {
        const parts = item.nameCourse.split(' ')
        const isLastPartGeneration = parts[parts.length - 1].endsWith('기')
        const title = isLastPartGeneration
          ? parts.slice(0, -1).join(' ')
          : item.nameCourse
        const generation = isLastPartGeneration 
          ? parts[parts.length - 1].replace('기', '') 
          : '1'
        
        return {
          title,
          trprDegr: generation,
          seqCourse: item.seqCourse,
          nameCourse: item.nameCourse,
          startDtCourse: item.startDtCourse,
          finishDtCourse: item.finishDtCourse
        }
      })
      .sort((a, b) => a.title.localeCompare(b.title, 'ko'))

  } catch (err) {
    // console.error('API 조회 실패:', err)
    await swAlert('훈련과정 정보를 조회하는 중 오류가 발생했습니다.', 'error')
  }
}

function toggleCourseBox() {
  courseBoxOpen.value = !courseBoxOpen.value
  searchQuery.value = ''

  nextTick(() => {
    if (dropdownWrapper.value) {
      const rect = dropdownWrapper.value.getBoundingClientRect()
      const dropdownHeight = 240
      const spaceBelow = window.innerHeight - rect.bottom
      dropdownPositionUp.value = spaceBelow < dropdownHeight
    }
  })
}

function selectCourse(item) {
  selectedCourse.value = item
  courseBoxOpen.value = false
  searchQuery.value = ''
  validateCourse()
}

// 외부 클릭 시 드롭다운 닫기
function handleClickOutside(event) {
  if (
    courseBoxOpen.value &&
    dropdownWrapper.value &&
    !dropdownWrapper.value.contains(event.target)
  ) {
    courseBoxOpen.value = false
    searchQuery.value = ''
  }
}

// 유효성 검사
function validateName() {
  errors.value.name = name.value.trim() ? '' : '이름을 입력해주세요.'
}

async function validateUsername() {
  const trimmedId = username.value.trim()

  if (!trimmedId) {
    errors.value.username = '아이디를 입력해주세요.'
    return
  }

  // admin 아이디 금지
  if (trimmedId === 'admin') {
    errors.value.username = '사용할 수 없는 아이디입니다.'
    return
  }

  try {
    const response = await axios.get(
      `/api/users/register/validate?id=${encodeURIComponent(trimmedId)}`
    )
    const data = response.data.data

    if (data.flag === true) {
      errors.value.username = '이미 사용중인 아이디입니다.'
    } else if (data.flag === false) {
      errors.value.username = ''
    }
  } catch (error) {
    errors.value.username = '아이디 확인 중 오류가 발생했습니다.'
  }
}

function validatePassword() {
  const trimmedPw = password.value.trim()
  var passwordRegex = /^(?=.*[a-zA-Z])(?=.*[0-9]).{4,8}$/;

  errors.value.password =
    !passwordRegex.test(trimmedPw)
      ? '4-8자의 영문자, 숫자를 포함해야 합니다.'
      : ''
}

function validateDiscord() {
  errors.value.discord = discord.value.trim() ? '' : '디스코드 아이디를 입력해주세요.'
}

function validateCourse() {
  errors.value.course = selectedCourse.value ? '' : '훈련과정을 선택해주세요.'
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

async function handleSubmit() {
  validateName()
  await validateUsername() 
  validatePassword()
  validateDiscord()
  validateCourse()

  const hasError = Object.values(errors.value).some(error => error !== '')
  if (hasError) {
    return
  }

  const payload = {
    seqCorse: selectedCourse.value.seqCourse,
    idUser: username.value,
    pwUser: password.value,
    nameUser: name.value,
    dcUser: discord.value,
    agreeTermsUser: true,
    agreeInfoUser: true,
    agreeDiscordAlarmUser: true
  }

  try {
    const response = await axios.post('/api/users/register', payload)

    if (response.data.code === '0000') {
      await swAlert('회원가입이 완료되었습니다!', 'success')
      router.push('/login')
    } else {
      await swAlert(`회원가입 실패: ${response.data.msg || '알 수 없는 오류'}`, 'error')
    }
  } catch (error) {
    await swAlert(`회원가입 요청 중 오류가 발생했습니다: ${error.response?.data?.msg || error.message}`, 'error')
  }
}
</script>

<style scoped>
.register-wrapper {
  min-height: calc(100vh - var(--pb-header-height));
  width: 100%;
  background: var(--pb-color-canvas);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 48px 2rem;
}

.register-container {
  width: 100%;
  max-width: 650px;
}

.register-header {
  text-align: center;
  margin-bottom: 2rem;
}

.logo-container {
  display: inline-block;
  cursor: pointer;
  margin-bottom: 1.25rem;
}

.logo-img {
  max-width: 160px;
  height: auto;
}

.header-text h1 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--pb-color-heading);
  margin: 0 0 0.375rem 0;
}

.header-text p {
  font-size: 0.95rem;
  color: var(--pb-color-text-muted);
  margin: 0;
}

.register-card {
  background: var(--pb-color-surface);
  border-radius: var(--pb-radius-lg);
  padding: 2rem;
  box-shadow: var(--pb-shadow-md);
  border: 1px solid var(--pb-color-border);
  margin-bottom: 1.5rem;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-section {
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-md);
  padding: 1.25rem;
  background: var(--pb-color-surface-subtle);
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 1.25rem;
  padding-bottom: 0.875rem;
  border-bottom: 1px solid var(--pb-color-border);
  gap: 10px;
}

.section-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  background: var(--pb-color-brand);
  border-radius: var(--pb-radius-sm);
  color: white;
  flex-shrink: 0;
}

.section-icon.discord {
  background: #5865f2;
}

.section-header h3 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0;
  flex: 1;
}

.section-badge {
  background: var(--pb-color-brand-soft);
  color: var(--pb-color-brand);
  padding: 3px 10px;
  border-radius: var(--pb-radius-sm);
  font-size: 0.8rem;
  font-weight: 500;
  border: 1px solid var(--pb-color-brand-muted);
}

.input-group {
  margin-bottom: 1.25rem;
}

.input-group:last-child {
  margin-bottom: 0;
}

.input-container {
  position: relative;
  width: 100%;
}

.input-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--pb-color-text-muted);
  z-index: 2;
}

.form-input {
  width: 100%;
  padding: 10px 12px 10px 42px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-md);
  font-size: 0.95rem;
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  transition: border-color 0.15s, box-shadow 0.15s;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px var(--pb-color-brand-soft);
}

.form-input.error {
  border-color: var(--pb-color-danger);
  box-shadow: 0 0 0 3px var(--pb-color-danger-soft);
}

.form-input.success {
  border-color: var(--pb-color-success);
  box-shadow: 0 0 0 3px var(--pb-color-success-soft);
}

.password-input {
  padding-right: 76px;
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
  border-radius: var(--pb-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
}

.input-action-btn:hover {
  color: var(--pb-color-text-muted);
}

.input-description {
  font-size: 0.85rem;
  color: var(--pb-color-text-muted);
  margin-top: 0.375rem;
}

.clear-btn {
  right: 14px;
}

.password-actions {
  position: absolute;
  right: 14px;
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

.error-message {
  color: var(--pb-color-danger);
  font-size: 0.85rem;
  margin-top: 0.375rem;
  font-weight: 500;
}

.select-container {
  position: relative;
  width: 100%;
}

.select-input {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 10px 12px 10px 42px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-md);
  background: var(--pb-color-surface);
  cursor: pointer;
  transition: border-color 0.15s;
  box-sizing: border-box;
}

.select-input:hover {
  border-color: var(--pb-color-border-strong);
}

.select-input.open {
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px var(--pb-color-brand-soft);
}

.select-input.error {
  border-color: var(--pb-color-danger);
  box-shadow: 0 0 0 3px var(--pb-color-danger-soft);
}

.select-icon {
  position: absolute;
  left: 14px;
  color: var(--pb-color-text-muted);
}

.select-text {
  flex: 1;
  font-size: 0.95rem;
  color: var(--pb-color-text);
}

.select-text.placeholder {
  color: var(--pb-color-text-soft);
  background-color: transparent !important;
}

.select-arrow {
  margin-left: 10px;
  color: var(--pb-color-text-muted);
}

.select-arrow svg.rotated {
  transform: rotate(180deg);
}

.select-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-md);
  box-shadow: var(--pb-shadow-popover);
  margin-top: 4px;
  z-index: 1000;
  opacity: 0;
  visibility: hidden;
  transform: translateY(-6px);
  transition: opacity 0.15s, transform 0.15s;
}

.select-dropdown.show {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.select-dropdown.position-up {
  top: auto;
  bottom: 100%;
  margin-top: 0;
  margin-bottom: 4px;
}

.dropdown-search {
  padding: 10px;
  border-bottom: 1px solid var(--pb-color-border);
}

.search-input {
  width: 100%;
  padding: 7px 10px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  font-size: 0.875rem;
  box-sizing: border-box;
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
}

.search-input:focus {
  outline: none;
  border-color: var(--pb-color-brand);
}

.dropdown-list {
  max-height: 200px;
  overflow-y: auto;
  padding: 6px;
  margin: 0;
  list-style: none;
}

.dropdown-list::-webkit-scrollbar { width: 6px; }
.dropdown-list::-webkit-scrollbar-thumb { background-color: var(--pb-color-border); border-radius: 3px; }
.dropdown-list::-webkit-scrollbar-track { background-color: var(--pb-color-surface-subtle); }

.dropdown-item {
  padding: 10px 12px;
  border-radius: var(--pb-radius-sm);
  cursor: pointer;
  transition: background 0.12s;
  margin-bottom: 2px;
}

.dropdown-item:hover {
  background: var(--pb-color-surface-muted);
}

.dropdown-item.selected {
  background: var(--pb-color-brand);
  color: white;
}

.course-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-title {
  font-weight: 500;
  font-size: 0.9rem;
}

.course-detail {
  font-size: 0.82rem;
  opacity: 0.8;
}

.no-results {
  padding: 18px;
  text-align: center;
  color: var(--pb-color-text-muted);
  font-size: 0.875rem;
}

.submit-section {
  text-align: center;
  margin-top: 0.5rem;
}

.submit-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 11px 28px;
  border: none;
  border-radius: var(--pb-radius-md);
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
  min-width: 200px;
  background: var(--pb-color-brand);
  color: white;
}

.submit-button:hover:not(:disabled) {
  background: var(--pb-color-brand-strong);
}

.submit-button:disabled {
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text-soft);
  cursor: not-allowed;
}

.progress-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1.5rem;
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.step-number {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.875rem;
  margin-bottom: 0.375rem;
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text-soft);
  border: 1px solid var(--pb-color-border);
}

.progress-step.completed .step-number {
  background: var(--pb-color-success);
  color: white;
  border-color: var(--pb-color-success);
}

.progress-step.active .step-number {
  background: var(--pb-color-brand);
  color: white;
  border-color: var(--pb-color-brand);
}

.progress-step span {
  font-size: 0.8rem;
  color: var(--pb-color-text-muted);
  font-weight: 500;
}

.progress-step.active span,
.progress-step.completed span {
  color: var(--pb-color-heading);
  font-weight: 600;
}

.progress-line {
  width: 60px;
  height: 2px;
  background: var(--pb-color-border);
  margin: 0 1rem;
  margin-bottom: 1.25rem;
}

.progress-line.completed {
  background: var(--pb-color-success);
}

@media (max-width: 768px) {
  .register-wrapper { padding: 1.5rem 1rem; }
  .register-card { padding: 1.5rem; }
  .progress-indicator { flex-direction: column; gap: 0.75rem; }
  .progress-line { width: 2px; height: 32px; margin: 0; }
  .form-section { padding: 1rem; }
  .password-input { padding-right: 72px; }
  .section-header { flex-wrap: wrap; gap: 8px; }
}

@media (max-width: 480px) {
  .register-card { padding: 1.25rem; }
  .header-text h1 { font-size: 1.3rem; }
  .submit-button { width: 100%; }
}
</style>