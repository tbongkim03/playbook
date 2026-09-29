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
                <PhUser weight="duotone" :size="20" />
              </div>
              <h3>개인 정보</h3>
            </div>

            <div class="input-group">
              <div class="input-container">
                <div class="input-icon">
                  <PhUser weight="duotone" :size="18" />
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
                  <PhXCircle weight="duotone" :size="16" />
                </button>
              </div>
            </div>

            <div class="input-group">
              <div class="input-container">
                <div class="input-icon">
                  <PhEnvelope weight="duotone" :size="18" />
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
                  <PhXCircle weight="duotone" :size="16" />
                </button>
              </div>
              
            </div>

            <div class="input-group">
              <div class="input-container">
                <div class="input-icon">
                  <PhLockKey weight="duotone" :size="18" />
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
                    <PhXCircle weight="duotone" :size="16" />
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
                      <PhEye weight="duotone" :size="16" v-if="!showPassword" />
                      <!-- 눈 숨기기 아이콘 -->
                      <PhEyeSlash weight="duotone" :size="16" />
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
                <PhDiscordLogo weight="duotone" :size="20" />
              </div>
              <h3>알림 설정</h3>
              <div class="section-badge">
                <span>🔔 반납 기한 알림</span>
              </div>
            </div>

            <div class="input-group">
              <div class="input-container">
                <div class="input-icon">
                  <PhDiscordLogo weight="duotone" :size="18" />
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
                  <PhXCircle weight="duotone" :size="16" />
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
                <PhTicket weight="duotone" :size="20" />
              </div>
              <h3>훈련과정 선택</h3>
            </div>

            <div class="input-group">
              <div class="select-container" @click="toggleCourseBox" ref="dropdownWrapper">
                <div class="select-input" :class="{ 'error': errors.course, 'open': courseBoxOpen }">
                  <div class="select-icon">
                    <PhTicket weight="duotone" :size="18" />
                  </div>
                  <span class="select-text" :class="{ placeholder: !selectedCourse }">
                    {{ selectedCourse ? `${selectedCourse.title} ${selectedCourse.trprDegr}기` : '수강중인 훈련과정을 선택해주세요' }}
                  </span>
                  <div class="select-arrow">
                    <PhCaretDown weight="duotone" :size="16" :class="{ rotated: courseBoxOpen }" />
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
                <PhCheckCircle weight="duotone" :size="20" />
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
import { PhCaretDown, PhCheckCircle, PhDiscordLogo, PhEnvelope, PhEye, PhEyeSlash, PhLockKey, PhTicket, PhUser, PhXCircle } from '@phosphor-icons/vue'
import { ref, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as userApi from '@/api/user'
import * as courseApi from '@/api/course'
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
  // Work24 외부 API 동기화는 백엔드 스케줄러(매일 09:00)가 담당한다.
  // 회원가입 페이지는 DB에 저장된 과정만 조회해 드롭다운을 채운다.
  try {
    const dbRes = await courseApi.getAll()
    const finalDbCourses = dbRes.data.data || []

    if (finalDbCourses.length === 0) {
      await swAlert('훈련 과정을 찾을 수 없습니다.', 'error')
      return
    }

    // 드롭다운 표시용 courseList 값 세팅
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
    const response = await userApi.validateId(trimmedId)
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
    const response = await userApi.register(payload)

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