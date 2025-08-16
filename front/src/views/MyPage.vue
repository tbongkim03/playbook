<template>
  <div class="mypage-wrapper">
    <div class="mypage-container">
      <!-- 헤더 -->
      <header class="page-header">
        <div class="header-content">
          <h1 class="page-title">마이페이지</h1>
          <p class="page-subtitle">계정 정보와 활동 내역을 관리할 수 있습니다</p>
        </div>
      </header>

      <!-- 유저 정보 카드 -->
      <section class="user-info-section">
        <div class="section-header">
          <div class="section-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="8" r="5" stroke="currentColor" stroke-width="2"/>
              <path d="M20 21a8 8 0 1 0-16 0" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <h2 class="section-title">계정 정보</h2>
        </div>

        <div class="user-info-grid">
          <!-- 이름 -->
          <div class="info-item">
            <label class="info-label">이름</label>
            <div class="info-value">
              <span class="value-text">{{ userInfo.nameUser }}</span>
            </div>
          </div>

          <!-- 아이디 -->
          <div class="info-item">
            <label class="info-label">아이디</label>
            <div class="info-value">
              <span class="value-text">{{ userInfo.idUser }}</span>
            </div>
          </div>

          <!-- 비밀번호 변경 -->
          <div class="info-item">
            <label class="info-label">비밀번호</label>
            <div class="info-value">
              <span class="value-text">••••••••</span>
              <button @click="openPasswordModal" class="edit-button">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" stroke-width="2"/>
                  <path d="m18.5 2.5 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="2"/>
                </svg>
                변경
              </button>
            </div>
          </div>

          <!-- 디스코드 아이디 변경 -->
          <div class="info-item">
            <label class="info-label">디스코드 아이디</label>
            <div class="info-value">
              <span class="value-text">{{ userInfo.dcUser }}</span>
              <button @click="openDiscordModal" class="edit-button">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" stroke-width="2"/>
                  <path d="m18.5 2.5 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="2"/>
                </svg>
                변경
              </button>
            </div>
          </div>

          <!-- 수강중인 과정 변경 -->
          <div class="info-item">
            <label class="info-label">수강중인 과정</label>
            <div class="info-value">
              <span class="value-text">{{ currentCourse }}</span>
              <button @click="openCourseModal" class="edit-button">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" stroke-width="2"/>
                  <path d="m18.5 2.5 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="2"/>
                </svg>
                변경
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- 나의 찜 목록 -->
      <section class="favorites-section">
        <div class="section-header">
          <div class="section-icon favorites">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" fill="currentColor"/>
            </svg>
          </div>
          <h2 class="section-title">나의 찜 목록</h2>
          <span class="count-badge">{{ favoriteBooks.length }}</span>
        </div>

        <div v-if="favoriteBooks.length === 0" class="empty-state">
          <div class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <p class="empty-text">아직 찜한 도서가 없습니다</p>
          <p class="empty-subtext">마음에 드는 도서를 찜해보세요</p>
        </div>

        <div v-else class="books-grid">
          <div 
            v-for="book in favoriteBooks" 
            :key="book.seqBook" 
            class="book-card"
            @click="goToBookDetail(book.seqBook)"
          >
            <div class="book-info">
              <h3 class="book-title">{{ book.titleBook }}</h3>
              <p class="book-author">{{ book.authorBook }}</p>
            </div>
            <button 
              @click.stop="removeFavorite(book.seqBook)" 
              class="favorite-button active"
              title="찜 해제"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" fill="currentColor"/>
              </svg>
            </button>
          </div>
        </div>
      </section>

      <!-- 나의 서비스 이용 기록 -->
      <section class="rental-history-section">
        <div class="section-header">
          <div class="section-icon rental">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" stroke="currentColor" stroke-width="2"/>
              <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <h2 class="section-title">나의 서비스 이용 기록</h2>
          <span class="count-badge">{{ rentalHistory.length }}</span>
        </div>

        <div v-if="rentalHistory.length === 0" class="empty-state">
          <div class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" stroke="currentColor" stroke-width="2"/>
              <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <p class="empty-text">이용 내역이 없습니다</p>
          <p class="empty-subtext">도서를 대출해보세요</p>
        </div>

        <div v-else class="history-list">
          <div v-for="record in rentalHistory" :key="record.id" class="history-item">
            <div class="history-info">
              <h4 class="history-title">{{ record.title }}</h4>
              <p class="history-date">대여일: {{ formatDate(record.rentalDate) }}</p>
              <p class="history-status" :class="record.status">{{ getStatusText(record.status) }}</p>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- 비밀번호 변경 모달 -->
    <div v-if="passwordModal" class="modal-overlay" @click="closePasswordModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>비밀번호 변경</h3>
          <button @click="closePasswordModal" class="close-button">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2"/>
            </svg>
          </button>
        </div>
        <form @submit.prevent="changePassword" class="modal-form">
          <div class="input-group">
            <label class="input-label">현재 비밀번호</label>
            <input 
              type="password" 
              v-model="passwordForm.currentPassword"
              class="form-input"
              :class="{ error: passwordForm.errors.currentPassword }"
              placeholder="현재 비밀번호를 입력하세요"
            >
            <div v-if="passwordForm.errors.currentPassword" class="error-message">
              {{ passwordForm.errors.currentPassword }}
            </div>
          </div>
          <div class="input-group">
            <label class="input-label">새 비밀번호</label>
            <input 
              type="password" 
              v-model="passwordForm.newPassword"
              class="form-input"
              :class="{ error: passwordForm.errors.newPassword }"
              placeholder="새 비밀번호를 입력하세요 (4-8자, 영문+숫자)"
            >
            <div v-if="passwordForm.errors.newPassword" class="error-message">
              {{ passwordForm.errors.newPassword }}
            </div>
          </div>
          <div class="input-group">
            <label class="input-label">새 비밀번호 확인</label>
            <input 
              type="password" 
              v-model="passwordForm.confirmPassword"
              class="form-input"
              :class="{ error: passwordForm.errors.confirmPassword }"
              placeholder="새 비밀번호를 다시 입력하세요"
            >
            <div v-if="passwordForm.errors.confirmPassword" class="error-message">
              {{ passwordForm.errors.confirmPassword }}
            </div>
          </div>
          <div class="modal-actions">
            <button type="button" @click="closePasswordModal" class="cancel-button">취소</button>
            <button type="submit" class="submit-button" :disabled="passwordForm.loading">
              {{ passwordForm.loading ? '변경 중...' : '변경하기' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 디스코드 변경 모달 -->
    <div v-if="discordModal" class="modal-overlay" @click="closeDiscordModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>디스코드 아이디 변경</h3>
          <button @click="closeDiscordModal" class="close-button">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2"/>
            </svg>
          </button>
        </div>
        <form @submit.prevent="changeDiscord" class="modal-form">
          <div class="input-group">
            <label class="input-label">현재 비밀번호</label>
            <input 
              type="password" 
              v-model="discordForm.password"
              class="form-input"
              :class="{ error: discordForm.errors.password }"
              placeholder="본인 확인을 위해 비밀번호를 입력하세요"
            >
            <div v-if="discordForm.errors.password" class="error-message">
              {{ discordForm.errors.password }}
            </div>
          </div>
          <div class="input-group">
            <label class="input-label">새 디스코드 아이디</label>
            <input 
              type="text" 
              v-model="discordForm.newDiscord"
              class="form-input"
              :class="{ error: discordForm.errors.newDiscord }"
              placeholder="새 디스코드 아이디를 입력하세요"
            >
            <div v-if="discordForm.errors.newDiscord" class="error-message">
              {{ discordForm.errors.newDiscord }}
            </div>
          </div>
          <div class="modal-actions">
            <button type="button" @click="closeDiscordModal" class="cancel-button">취소</button>
            <button type="submit" class="submit-button" :disabled="discordForm.loading">
              {{ discordForm.loading ? '변경 중...' : '변경하기' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 과정 변경 모달 -->
    <div v-if="courseModal" class="modal-overlay" @click="closeCourseModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>수강 과정 변경</h3>
          <button @click="closeCourseModal" class="close-button">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2"/>
            </svg>
          </button>
        </div>
        <form @submit.prevent="changeCourse" class="modal-form">
          <div class="input-group">
            <label class="input-label">현재 비밀번호</label>
            <input 
              type="password" 
              v-model="courseForm.password"
              class="form-input"
              :class="{ error: courseForm.errors.password }"
              placeholder="본인 확인을 위해 비밀번호를 입력하세요"
            >
            <div v-if="courseForm.errors.password" class="error-message">
              {{ courseForm.errors.password }}
            </div>
          </div>
          <div class="input-group">
            <label class="input-label">새 수강 과정</label>
            <div class="select-container" @click="toggleCourseDropdown" ref="courseDropdownWrapper">
              <div class="select-input" :class="{ error: courseForm.errors.course, open: courseDropdownOpen }">
                <span class="select-text" :class="{ placeholder: !courseForm.selectedCourse }">
                  {{ courseForm.selectedCourse ? 
                     `${courseForm.selectedCourse.title} ${courseForm.selectedCourse.trprDegr}기` : 
                     '수강중인 훈련과정을 선택해주세요' }}
                </span>
                <div class="select-arrow">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" :class="{ rotated: courseDropdownOpen }">
                    <polyline points="6,9 12,15 18,9" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </div>
              </div>
              <div class="select-dropdown" :class="{ show: courseDropdownOpen }">
                <div class="dropdown-search">
                  <input 
                    type="text" 
                    v-model="courseSearchQuery" 
                    placeholder="훈련과정 검색..." 
                    class="search-input"
                    @click.stop
                  >
                </div>
                <ul class="dropdown-list">
                  <li
                    v-for="(course, index) in filteredCourseList"
                    :key="index"
                    class="dropdown-item"
                    :class="{ selected: courseForm.selectedCourse === course }"
                    @click.stop="selectCourse(course)"
                  >
                    <div class="course-info">
                      <div class="course-title">{{ course.title }}</div>
                      <div class="course-detail">{{ course.trprDegr }}기</div>
                    </div>
                  </li>
                  <li v-if="filteredCourseList.length === 0" class="no-results">
                    검색 결과가 없습니다.
                  </li>
                </ul>
              </div>
            </div>
            <div v-if="courseForm.errors.course" class="error-message">
              {{ courseForm.errors.course }}
            </div>
          </div>
          <div class="modal-actions">
            <button type="button" @click="closeCourseModal" class="cancel-button">취소</button>
            <button type="submit" class="submit-button" :disabled="courseForm.loading">
              {{ courseForm.loading ? '변경 중...' : '변경하기' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// JWT 토큰 (실제로는 로컬스토리지나 스토어에서 가져와야 함)
const jwtToken = ref(localStorage.getItem('jwtToken') || '')

// API 기본 URL
const API_BASE_URL = 'http://localhost:8080'

// 유저 정보
const userInfo = ref({
  seqCourse: null,
  idUser: '',
  nameUser: '',
  dcUser: ''
})

// 현재 과정명
const currentCourse = ref('')

// 찜한 도서 목록
const favoriteBooks = ref([])

// 대여 기록
const rentalHistory = ref([])

// 모달 상태
const passwordModal = ref(false)
const discordModal = ref(false)
const courseModal = ref(false)

// 과정 드롭다운
const courseDropdownOpen = ref(false)
const courseDropdownWrapper = ref(null)
const courseSearchQuery = ref('')
const courseList = ref([])

// 폼 데이터
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
  loading: false,
  errors: {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
})

const discordForm = ref({
  password: '',
  newDiscord: '',
  loading: false,
  errors: {
    password: '',
    newDiscord: ''
  }
})

const courseForm = ref({
  password: '',
  selectedCourse: null,
  loading: false,
  errors: {
    password: '',
    course: ''
  }
})

// API 헤더 설정
const getHeaders = () => ({
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${jwtToken.value}`
})

// 과정 검색 필터링
const filteredCourseList = computed(() => {
  if (!courseSearchQuery.value) return courseList.value
  return courseList.value.filter(course => 
    course.title.toLowerCase().includes(courseSearchQuery.value.toLowerCase()) ||
    course.trprDegr.toString().includes(courseSearchQuery.value)
  )
})

onMounted(() => {
  loadUserData()
  loadFavoriteBooks()
  loadRentalHistory()
  getCourseList()
  document.addEventListener('click', handleClickOutside)
  document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('keydown', handleKeydown)
})

// 유저 정보 로드
async function loadUserData() {
  try {
    const response = await fetch(`${API_BASE_URL}/users/me`, {
      headers: getHeaders()
    })
    
    if (response.ok) {
      const data = await response.json()
      userInfo.value = data
      
      // 현재 과정명 설정 (과정 목록에서 찾아서 설정)
      await getCourseList()
      const course = courseList.value.find(c => c.seqCourse === data.seqCourse)
      if (course) {
        currentCourse.value = `${course.title} ${course.trprDegr}기`
      }
    }
  } catch (error) {
    console.error('유저 정보 로드 실패:', error)
  }
}

// 찜한 도서 목록 로드
async function loadFavoriteBooks() {
  try {
    const response = await fetch(`${API_BASE_URL}/favor`, {
      headers: getHeaders()
    })
    
    if (response.ok) {
      const data = await response.json()
      favoriteBooks.value = data
    }
  } catch (error) {
    console.error('찜 목록 로드 실패:', error)
  }
}

// 대여 기록 로드
async function loadRentalHistory() {
  try {
    const response = await fetch(`${API_BASE_URL}/history/me`, {
      headers: getHeaders()
    })
    
    if (response.ok) {
      const data = await response.json()
      rentalHistory.value = data
    }
  } catch (error) {
    console.error('대여 기록 로드 실패:', error)
  }
}

// 과정 목록 가져오기 (회원가입 페이지와 동일한 로직)
async function getCourseList() {
  const apiKey = import.meta.env.VITE_WORK24_API_KEY
  const today = new Date()
  const sixMonthsAgo = new Date()
  sixMonthsAgo.setMonth(sixMonthsAgo.getMonth() - 6)

  function formatDateToYYYYMMDD(date) {
    const yyyy = date.getFullYear()
    const mm = String(date.getMonth() + 1).padStart(2, '0')
    const dd = String(date.getDate()).padStart(2, '0')
    return `${yyyy}${mm}${dd}`
  }

  const srchTraStDt = formatDateToYYYYMMDD(sixMonthsAgo)
  const srchTraEndDt = formatDateToYYYYMMDD(today)

  const url =
    `https://www.work24.go.kr/cm/openApi/call/hr/callOpenApiSvcInfo310L01.do?authKey=${apiKey}` +
    `&returnType=JSON&outType=1&pageNum=1&pageSize=100` +
    `&srchTraStDt=${srchTraStDt}&srchTraEndDt=${srchTraEndDt}` +
    `&srchTraArea1=11&srchNcs1=20&crseTracseSe=C0104&srchTraGbn=M1001&srchTraOrganNm=플레이데이터평생교육원` +
    `&sort=ASC&sortCol=2`

  try {
    const res = await fetch(url)
    const data = await res.json()
    const apiCoursesRaw = data?.srchList || []

    if (apiCoursesRaw.length === 0) return

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

    const dbRes = await fetch(`${API_BASE_URL}/courses`)
    const dbCourses = await dbRes.json()

    for (const apiItem of apiCourses) {
      const exists = dbCourses.find(dbItem => dbItem.nameCourse === apiItem.nameCourse)
      if (!exists) {
        await fetch(`${API_BASE_URL}/courses`, {
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

    for (const dbItem of dbCourses) {
      const exists = apiCourses.find(apiItem => apiItem.nameCourse === dbItem.nameCourse)
      if (!exists) {
        await fetch(`${API_BASE_URL}/courses/${dbItem.seqCourse}`, {
          method: 'DELETE'
        })
      }
    }

    for (const apiItem of apiCourses) {
      const dbItem = dbCourses.find(db => db.nameCourse === apiItem.nameCourse)
      if (dbItem) {
        const isDifferent =
          dbItem.startDtCourse !== apiItem.startDtCourse ||
          dbItem.finishDtCourse !== apiItem.finishDtCourse

        if (isDifferent) {
          await fetch(`${API_BASE_URL}/courses/${dbItem.seqCourse}`, {
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

    const finalDbRes = await fetch(`${API_BASE_URL}/courses`)
    const finalDbCourses = await finalDbRes.json()

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
  }
}

// 도서 상세 페이지로 이동
function goToBookDetail(seqBook) {
  router.push(`/books/info/${seqBook}`)
}

// 찜 해제
async function removeFavorite(seqBook) {
  try {
    const response = await fetch(`${API_BASE_URL}/favor`, {
      method: 'DELETE',
      headers: getHeaders(),
      body: JSON.stringify({ seqBook })
    })

    if (response.ok) {
      favoriteBooks.value = favoriteBooks.value.filter(book => book.seqBook !== seqBook)
    } else {
      alert('찜 해제에 실패했습니다.')
    }
  } catch (error) {
    console.error('찜 해제 실패:', error)
    alert('찜 해제 중 오류가 발생했습니다.')
  }
}

// 날짜 포맷팅
function formatDate(dateString) {
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR')
}

// 상태 텍스트 변환
function getStatusText(status) {
  const statusMap = {
    'rented': '대여중',
    'returned': '반납완료',
    'overdue': '연체'
  }
  return statusMap[status] || status
}

// 모달 열기/닫기 함수들
function openPasswordModal() {
  passwordModal.value = true
  resetPasswordForm()
}

function closePasswordModal() {
  passwordModal.value = false
  resetPasswordForm()
}

function openDiscordModal() {
  discordModal.value = true
  resetDiscordForm()
}

function closeDiscordModal() {
  discordModal.value = false
  resetDiscordForm()
}

function openCourseModal() {
  courseModal.value = true
  resetCourseForm()
}

function closeCourseModal() {
  courseModal.value = false
  courseDropdownOpen.value = false
  resetCourseForm()
}

// 폼 리셋 함수들
function resetPasswordForm() {
  passwordForm.value = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
    loading: false,
    errors: {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  }
}

function resetDiscordForm() {
  discordForm.value = {
    password: '',
    newDiscord: userInfo.value.dcUser,
    loading: false,
    errors: {
      password: '',
      newDiscord: ''
    }
  }
}

function resetCourseForm() {
  courseForm.value = {
    password: '',
    selectedCourse: null,
    loading: false,
    errors: {
      password: '',
      course: ''
    }
  }
}

// 과정 드롭다운 관련
function toggleCourseDropdown() {
  courseDropdownOpen.value = !courseDropdownOpen.value
  courseSearchQuery.value = ''
}

function selectCourse(course) {
  courseForm.value.selectedCourse = course
  courseDropdownOpen.value = false
  courseSearchQuery.value = ''
  courseForm.value.errors.course = ''
}

function handleClickOutside(event) {
  if (
    courseDropdownOpen.value &&
    courseDropdownWrapper.value &&
    !courseDropdownWrapper.value.contains(event.target)
  ) {
    courseDropdownOpen.value = false
    courseSearchQuery.value = ''
  }
}

// ESC 키 처리 함수
function handleKeydown(event) {
  if (event.key === 'Escape') {
    // 드롭다운이 열려있으면 드롭다운만 닫기
    if (courseDropdownOpen.value) {
      courseDropdownOpen.value = false
      courseSearchQuery.value = ''
      return
    }
    
    // 모달이 열려있으면 모달 닫기
    if (passwordModal.value) {
      closePasswordModal()
    } else if (discordModal.value) {
      closeDiscordModal()
    } else if (courseModal.value) {
      closeCourseModal()
    }
  }
}

// 비밀번호 검증
async function validatePassword(password) {
  try {
    const response = await fetch(`${API_BASE_URL}/users/validate`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify({ password })
    })

    return response.ok
  } catch (error) {
    console.error('비밀번호 검증 실패:', error)
    return false
  }
}

// 비밀번호 변경
async function changePassword() {
  // 폼 검증
  passwordForm.value.errors = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  }

  if (!passwordForm.value.currentPassword) {
    passwordForm.value.errors.currentPassword = '현재 비밀번호를 입력해주세요.'
    return
  }

  if (!passwordForm.value.newPassword) {
    passwordForm.value.errors.newPassword = '새 비밀번호를 입력해주세요.'
    return
  }

  const passwordRegex = /^(?=.*[a-zA-Z])(?=.*[0-9]).{4,8}$/
  if (!passwordRegex.test(passwordForm.value.newPassword)) {
    passwordForm.value.errors.newPassword = '4-8자의 영문자, 숫자를 포함해야 합니다.'
    return
  }

  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    passwordForm.value.errors.confirmPassword = '새 비밀번호가 일치하지 않습니다.'
    return
  }

  passwordForm.value.loading = true

  try {
    // 현재 비밀번호 검증
    const isValid = await validatePassword(passwordForm.value.currentPassword)
    if (!isValid) {
      passwordForm.value.errors.currentPassword = '현재 비밀번호가 올바르지 않습니다.'
      passwordForm.value.loading = false
      return
    }

    // 비밀번호 변경
    const response = await fetch(`${API_BASE_URL}/users/password`, {
      method: 'PUT',
      headers: getHeaders(),
      body: JSON.stringify({ newPassword: passwordForm.value.newPassword })
    })

    if (response.ok) {
      alert('비밀번호가 성공적으로 변경되었습니다.')
      closePasswordModal()
    } else {
      alert('비밀번호 변경에 실패했습니다.')
    }
  } catch (error) {
    console.error('비밀번호 변경 실패:', error)
    alert('비밀번호 변경 중 오류가 발생했습니다.')
  } finally {
    passwordForm.value.loading = false
  }
}

// 디스코드 아이디 변경
async function changeDiscord() {
  // 폼 검증
  discordForm.value.errors = {
    password: '',
    newDiscord: ''
  }

  if (!discordForm.value.password) {
    discordForm.value.errors.password = '비밀번호를 입력해주세요.'
    return
  }

  if (!discordForm.value.newDiscord) {
    discordForm.value.errors.newDiscord = '디스코드 아이디를 입력해주세요.'
    return
  }

  discordForm.value.loading = true

  try {
    // 비밀번호 검증
    const isValid = await validatePassword(discordForm.value.password)
    if (!isValid) {
      discordForm.value.errors.password = '비밀번호가 올바르지 않습니다.'
      discordForm.value.loading = false
      return
    }

    // 디스코드 아이디 변경
    const response = await fetch(`${API_BASE_URL}/users/discord`, {
      method: 'PUT',
      headers: getHeaders(),
      body: JSON.stringify({ newDiscord: discordForm.value.newDiscord })
    })

    if (response.ok) {
      userInfo.value.dcUser = discordForm.value.newDiscord
      alert('디스코드 아이디가 성공적으로 변경되었습니다.')
      closeDiscordModal()
    } else {
      alert('디스코드 아이디 변경에 실패했습니다.')
    }
  } catch (error) {
    console.error('디스코드 아이디 변경 실패:', error)
    alert('디스코드 아이디 변경 중 오류가 발생했습니다.')
  } finally {
    discordForm.value.loading = false
  }
}

// 수강 과정 변경
async function changeCourse() {
  // 폼 검증
  courseForm.value.errors = {
    password: '',
    course: ''
  }

  if (!courseForm.value.password) {
    courseForm.value.errors.password = '비밀번호를 입력해주세요.'
    return
  }

  if (!courseForm.value.selectedCourse) {
    courseForm.value.errors.course = '수강 과정을 선택해주세요.'
    return
  }

  courseForm.value.loading = true

  try {
    // 비밀번호 검증
    const isValid = await validatePassword(courseForm.value.password)
    if (!isValid) {
      courseForm.value.errors.password = '비밀번호가 올바르지 않습니다.'
      courseForm.value.loading = false
      return
    }

    // 수강 과정 변경
    const response = await fetch(`${API_BASE_URL}/users/course`, {
      method: 'PUT',
      headers: getHeaders(),
      body: JSON.stringify({ newSeqCourse: courseForm.value.selectedCourse.seqCourse })
    })

    if (response.ok) {
      userInfo.value.seqCourse = courseForm.value.selectedCourse.seqCourse
      currentCourse.value = `${courseForm.value.selectedCourse.title} ${courseForm.value.selectedCourse.trprDegr}기`
      alert('수강 과정이 성공적으로 변경되었습니다.')
      closeCourseModal()
    } else {
      alert('수강 과정 변경에 실패했습니다.')
    }
  } catch (error) {
    console.error('수강 과정 변경 실패:', error)
    alert('수강 과정 변경 중 오류가 발생했습니다.')
  } finally {
    courseForm.value.loading = false
  }
}
</script>

<style scoped>
.mypage-wrapper {
  min-height: 100vh;
  background: #fafafa;
  padding: 2rem 0;
}

.mypage-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 2rem;
}

.page-header {
  margin-bottom: 3rem;
  text-align: center;
}

.header-content h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 0.5rem 0;
}

.page-subtitle {
  font-size: 1.1rem;
  color: #6b7280;
  margin: 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e5e7eb;
}

.section-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: #f3f4f6;
  border-radius: 8px;
  color: #374151;
}

.section-icon.favorites {
  background: #fef2f2;
  color: #dc2626;
}

.section-icon.rental {
  background: #f0f9ff;
  color: #0284c7;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  flex: 1;
}

.count-badge {
  background: #e5e7eb;
  color: #374151;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 500;
  min-width: 24px;
  text-align: center;
}

.user-info-section,
.favorites-section,
.rental-history-section {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  margin-bottom: 2rem;
  border: 1px solid #e5e7eb;
}

.user-info-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 0;
  border-bottom: 1px solid #f3f4f6;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-weight: 500;
  color: #374151;
  min-width: 120px;
}

.info-value {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex: 1;
}

.value-text {
  color: #1a1a1a;
  flex: 1;
  text-align: right;
}

.edit-button {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #f3f4f6;
  border: none;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.875rem;
  color: #374151;
}

.edit-button:hover {
  background: #e5e7eb;
}

.empty-state {
  text-align: center;
  padding: 3rem 2rem;
  color: #6b7280;
}

.empty-icon {
  margin-bottom: 1rem;
  color: #d1d5db;
}

.empty-text {
  font-size: 1.1rem;
  font-weight: 500;
  margin: 0 0 0.5rem 0;
}

.empty-subtext {
  margin: 0;
}

.books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
}

.book-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.book-card:hover {
  border-color: #d1d5db;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.book-info {
  flex: 1;
}

.book-title {
  font-size: 1rem;
  font-weight: 500;
  color: #1a1a1a;
  margin: 0 0 0.25rem 0;
  line-height: 1.4;
}

.book-author {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0;
}

.favorite-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: all 0.2s;
  color: #d1d5db;
}

.favorite-button.active {
  color: #dc2626;
}

.favorite-button:hover {
  background: #f3f4f6;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.history-item {
  padding: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.history-title {
  font-size: 1rem;
  font-weight: 500;
  color: #1a1a1a;
  margin: 0 0 0.5rem 0;
}

.history-date {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0 0 0.25rem 0;
}

.history-status {
  font-size: 0.875rem;
  font-weight: 500;
  margin: 0;
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
}

.history-status.rented {
  background: #dbeafe;
  color: #1d4ed8;
}

.history-status.returned {
  background: #dcfce7;
  color: #16a34a;
}

.history-status.overdue {
  background: #fee2e2;
  color: #dc2626;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 2rem;
  /* 드롭다운이 모달 영역을 넘어서도 보이도록 설정 */
  overflow: visible;
}

.modal-content {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  width: 100%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
  /* 드롭다운이 모달 밖으로 나갈 수 있도록 설정 */
  overflow: visible;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.close-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  color: #6b7280;
  transition: all 0.2s;
}

.close-button:hover {
  background: #f3f4f6;
  color: #374151;
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.input-label {
  font-weight: 500;
  color: #374151;
  font-size: 0.875rem;
}

.form-input {
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input.error {
  border-color: #dc2626;
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1);
}

.error-message {
  font-size: 0.875rem;
  color: #dc2626;
}

.select-container {
  position: relative;
}

.select-input {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.select-input:hover {
  border-color: #9ca3af;
}

.select-input.open,
.select-input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.select-input.error {
  border-color: #dc2626;
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1);
}

.select-text {
  flex: 1;
  font-size: 1rem;
  color: #1a1a1a;
}

.select-text.placeholder {
  color: #9ca3af;
}

.select-arrow {
  color: #6b7280;
  transition: transform 0.2s;
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
  border: 1px solid #d1d5db;
  border-radius: 8px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  margin-top: 4px;
  z-index: 9999; /* 모달보다 높은 z-index 설정 */
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  transition: all 0.2s;
}

.select-dropdown.show {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.dropdown-search {
  padding: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.875rem;
}

.search-input:focus {
  outline: none;
  border-color: #3b82f6;
}

.dropdown-list {
  max-height: 200px;
  overflow-y: auto;
  padding: 8px;
  margin: 0;
  list-style: none;
}

.dropdown-item {
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.dropdown-item:hover {
  background: #f3f4f6;
}

.dropdown-item.selected {
  background: #3b82f6;
  color: white;
}

.course-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-title {
  font-weight: 500;
}

.course-detail {
  font-size: 0.875rem;
  opacity: 0.8;
}

.no-results {
  padding: 20px;
  text-align: center;
  color: #6b7280;
  font-size: 0.875rem;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 1rem;
}

.cancel-button,
.submit-button {
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-button {
  background: #f3f4f6;
  border: 1px solid #d1d5db;
  color: #374151;
}

.cancel-button:hover {
  background: #e5e7eb;
}

.submit-button {
  background: #3b82f6;
  border: 1px solid #3b82f6;
  color: white;
}

.submit-button:hover:not(:disabled) {
  background: #2563eb;
}

.submit-button:disabled {
  background: #d1d5db;
  border-color: #d1d5db;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .mypage-container {
    padding: 0 1rem;
  }

  .user-info-section,
  .favorites-section,
  .rental-history-section {
    padding: 1.5rem;
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .info-value {
    width: 100%;
    justify-content: space-between;
  }

  .books-grid {
    grid-template-columns: 1fr;
  }

  .modal-overlay {
    padding: 1rem;
  }

  .modal-content {
    padding: 1.5rem;
  }

  .modal-actions {
    flex-direction: column;
  }
}
</style>