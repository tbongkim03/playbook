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
          <p>계정 정보를 입력하여 회원가입을 완료하세요</p>
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
                  type="password"
                  v-model="password"
                  @blur="validatePassword"
                  class="form-input"
                  :class="{ 
                    'error': errors.password,
                    'success': password.length >= 4 && !errors.password
                  }"
                  @keydown.space.prevent
                  @keydown="blockJavascriptInput"
                  placeholder="비밀번호를 입력하세요"
                />
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
import { ref, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const dropdownWrapper = ref(null)
const dropdownPositionUp = ref(false)
const courseBoxOpen = ref(false)
const searchQuery = ref('')

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

const fromTerm = window.history.state?.fromTerm

const token = localStorage.getItem("jwtToken")

if (!fromTerm) {
  alert('잘못된 접근입니다.')
  router.replace('/')
}

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

onMounted(() => {
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
    // const res = await fetch(url)
    const res = await fetch('http://localhost:8080/api/work24/course', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    const data = await res.json()
    const apiCoursesRaw = data?.srchList || []

    if (apiCoursesRaw.length === 0) {
      alert('훈련 과정을 찾을 수 없습니다.')
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
    const dbRes = await fetch('http://localhost:8080/courses')
    const dbCourses = await dbRes.json()

    // 4. 추가: API에는 있는데 DB에는 없는 과정 → INSERT
    for (const apiItem of apiCourses) {
      const exists = dbCourses.find(dbItem => dbItem.nameCourse === apiItem.nameCourse)
      if (!exists) {
        await fetch('http://localhost:8080/courses', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            nameCourse: apiItem.nameCourse,
            startDtCourse: apiItem.startDtCourse,
            finishDtCourse: apiItem.finishDtCourse
          })
        })
      }
    }

    // 5. 삭제: DB에는 있는데 API에는 없는 과정 → DELETE
    for (const dbItem of dbCourses) {
      const exists = apiCourses.find(apiItem => apiItem.nameCourse === dbItem.nameCourse)
      if (!exists) {
        await fetch(`http://localhost:8080/courses/${dbItem.seqCourse}`, {
          method: 'DELETE'
        })
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
          await fetch(`http://localhost:8080/courses/${dbItem.seqCourse}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              nameCourse: apiItem.nameCourse,
              startDtCourse: apiItem.startDtCourse,
              finishDtCourse: apiItem.finishDtCourse
            })
          })
        }
      }
    }

    // 7. 모든 동기화 작업 완료 후 최신 DB 데이터를 다시 가져오기
    const finalDbRes = await fetch('http://localhost:8080/courses')
    const finalDbCourses = await finalDbRes.json()

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
    console.error('API 조회 실패:', err)
    alert('훈련과정 정보를 조회하는 중 오류가 발생했습니다.')
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
    const response = await fetch(
      `http://localhost:8080/users/register/validate?id=${encodeURIComponent(trimmedId)}`
    )
    if (!response.ok) throw new Error('네트워크 오류')

    const data = await response.json()

    if (data.flag === true) {
      errors.value.username = '이미 사용중인 아이디입니다.'
    } else if (data.flag === false) {
      errors.value.username = ''
    }
  } catch (error) {
    console.error('아이디 검사 실패:', error)
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

function blockJavascriptInput(event) {
  // JavaScript 입력 방지 로직이 있다면 여기에
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
    const response = await fetch('http://localhost:8080/users/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })

    const result = await response.json()

    if (result.code === 200) {
      alert('회원가입이 완료되었습니다!')
      router.push('/login')
    } else {
      alert(`회원가입 실패: ${result.message || '알 수 없는 오류'}`)
    }
  } catch (error) {
    console.error('회원가입 중 오류 발생:', error)
    alert('회원가입 요청 중 오류가 발생했습니다.')
  }
}
</script>

<style scoped>
.register-wrapper {
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

.register-container {
  width: 100%;
  max-width: 600px;
  position: relative;
  z-index: 10;
}

.register-header {
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
  max-width: 180px;
  height: auto;
  transition: all 0.3s ease;
}

.header-text h1 {
  font-size: 2.2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 0.5rem 0;
}

.header-text p {
  font-size: 1.1rem;
  color: #64748b;
  margin: 0;
  font-weight: 400;
}

.register-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 2.5rem;
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.1),
    0 4px 25px rgba(102, 126, 234, 0.1),
    0 0 0 1px rgba(102, 126, 234, 0.05);
  border: 1px solid rgba(102, 126, 234, 0.1);
  margin-bottom: 2rem;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.form-section {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 1.5rem;
  background: #fafbfc;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e2e8f0;
  gap: 12px;
}

.section-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  color: white;
  flex-shrink: 0;
}

.section-icon.discord {
  background: #5865f2;
}

.section-header h3 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  flex: 1;
}

.section-badge {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.input-group {
  margin-bottom: 1.5rem;
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

.form-input:focus + .form-label,
.form-input:not(:placeholder-shown) + .form-label {
  transform: translateY(-12px) translateX(-8px) scale(0.85);
  color: #667eea;
  background: white;
  padding: 0 8px;
}

.form-input:focus ~ .input-icon {
  color: #667eea;
}

.form-input.error {
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.form-input.success {
  border-color: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

.form-label {
  position: absolute;
  left: 48px;
  top: 50%;
  transform: translateY(-50%);
  color: #64748b;
  font-size: 1rem;
  font-weight: 500;
  pointer-events: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1;
}

.input-description {
  font-size: 0.875rem;
  color: #64748b;
  margin-top: 0.5rem;
  padding-left: 4px;
}

.error-message {
  color: #ef4444;
  font-size: 0.875rem;
  margin-top: 0.5rem;
  padding-left: 4px;
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
  padding: 16px 16px 16px 48px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  background: #ffffff;
  cursor: pointer;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.select-input:hover {
  border-color: #cbd5e1;
}

.select-input.open,
.select-input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.select-input.error {
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.select-icon {
  position: absolute;
  left: 16px;
  color: #64748b;
  transition: all 0.3s ease;
}

.select-input.open .select-icon {
  color: #667eea;
}

.select-text {
  flex: 1;
  font-size: 1rem;
  color: #1e293b;
}

.select-text.placeholder {
  color: #9ca3af;
  background-color: #fff !important;
}

.select-arrow {
  margin-left: 12px;
  color: #64748b;
  transition: all 0.3s ease;
}

.select-arrow svg.rotated {
  transform: rotate(180deg);
}

.select-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  margin-top: 8px;
  z-index: 1000;
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  transition: all 0.3s ease;
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
  margin-bottom: 8px;
}

.dropdown-search {
  padding: 12px;
  border-bottom: 1px solid #e2e8f0;
}

.search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 0.9rem;
  box-sizing: border-box;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
}

.dropdown-list {
  max-height: 200px;
  overflow-y: auto;
  padding: 8px;
  margin: 0;
  list-style: none;
}

.dropdown-list::-webkit-scrollbar {
  width: 6px;
}

.dropdown-list::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 3px;
}

.dropdown-list::-webkit-scrollbar-track {
  background-color: #f1f5f9;
}

.dropdown-item {
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 4px;
}

.dropdown-item:hover {
  background: #f1f5f9;
}

.dropdown-item.selected {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.course-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-title {
  font-weight: 500;
  font-size: 0.95rem;
}

.course-detail {
  font-size: 0.85rem;
  opacity: 0.8;
}

.no-results {
  padding: 20px;
  text-align: center;
  color: #64748b;
  font-size: 0.9rem;
}

.submit-section {
  text-align: center;
  margin-top: 1rem;
}

.submit-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px 32px;
  border: none;
  border-radius: 12px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  min-width: 220px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.submit-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s;
}

.submit-button:hover::before {
  left: 100%;
}

.submit-button:hover:not(:disabled) {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
}

.submit-button:disabled {
  background: #e2e8f0;
  color: #94a3b8;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
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

.submit-button:hover:not(:disabled) .button-icon {
  transform: translateX(4px);
}

.progress-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 2rem;
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  margin-bottom: 0.5rem;
  transition: all 0.3s ease;
  background: #e2e8f0;
  color: #94a3b8;
}

.progress-step.completed .step-number {
  background: #10b981;
  color: white;
}

.progress-step.active .step-number {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: scale(1.1);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.progress-step span {
  font-size: 0.9rem;
  color: #64748b;
  font-weight: 500;
}

.progress-step.active span,
.progress-step.completed span {
  color: #1e293b;
  font-weight: 600;
}

.progress-line {
  width: 60px;
  height: 2px;
  background: #e2e8f0;
  margin: 0 1rem;
}

.progress-line.completed {
  background: #10b981;
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
  top: 15%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 60px;
  height: 60px;
  top: 65%;
  right: 15%;
  animation-delay: 2s;
}

.shape-3 {
  width: 100px;
  height: 100px;
  bottom: 25%;
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
  .register-wrapper {
    padding: 1rem;
  }
  
  .register-card {
    padding: 2rem;
  }
  
  .header-text h1 {
    font-size: 1.8rem;
  }
  
  .progress-indicator {
    flex-direction: column;
    gap: 1rem;
  }
  
  .progress-line {
    width: 2px;
    height: 40px;
    margin: 0;
  }
  
  .form-section {
    padding: 1.25rem;
  }
  
  .section-header {
    flex-wrap: wrap;
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .register-card {
    padding: 1.5rem;
  }
  
  .header-text h1 {
    font-size: 1.6rem;
  }
  
  .submit-button {
    width: 100%;
    padding: 14px 20px;
    font-size: 1rem;
  }
  
  .form-input {
    padding: 14px 14px 14px 44px;
  }
}
</style>