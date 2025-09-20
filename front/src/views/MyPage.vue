<template>
  <div class="mypage-wrapper">
    <div class="mypage-container">
      <header class="page-header">
        <div class="header-content">
          <h1 class="page-title">마이페이지</h1>
          <p class="page-subtitle">계정 정보와 활동 내역을 관리할 수 있습니다</p>
        </div>
      </header>

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
          <div class="info-item">
            <label class="info-label">이름</label>
            <div class="info-value">
              <span class="value-text">{{ userInfo.nameUser }}</span>
            </div>
          </div>

          <div class="info-item">
            <label class="info-label">아이디</label>
            <div class="info-value">
              <span class="value-text">{{ userInfo.idUser }}</span>
            </div>
          </div>

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

          <div class="info-item">
            <label class="info-label">수강중인 과정</label>
            <div class="info-value">
              <span class="value-text">{{ currentCourse }}</span>
            </div>
          </div>
        </div>
      </section>

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

      <section class="rental-history-section">
        <div class="section-header">
          <div class="section-icon rental">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" stroke="currentColor" stroke-width="2"/>
              <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <h2 class="section-title">나의 서비스 이용 기록</h2>
          <span class="count-badge">{{ rentalSummary.totalBorrowed }}</span>
        </div>

        <div class="rental-stats">
          <div class="stat-card">
            <div class="stat-value">{{ rentalSummary.totalBorrowed }}</div>
            <div class="stat-label">총 대여</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ rentalSummary.currentlyBorrowed }}</div>
            <div class="stat-label">현재 대여중</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ rentalSummary.totalReturned }}</div>
            <div class="stat-label">반납 완료</div>
          </div>
          <div class="stat-card" :class="{ warning: rentalSummary.overdueCount > 0 }">
            <div class="stat-value">{{ rentalSummary.overdueCount }}</div>
            <div class="stat-label">연체</div>
          </div>
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
          <div v-for="record in rentalHistory" :key="`${record.bookIsbn}-${record.borrowDate}`" class="history-item">
            <div class="history-info">
              <h4 class="history-title">{{ record.bookTitle }}</h4>
              <p class="history-author">{{ record.bookAuthor }}</p>
              <div class="history-details">
                <p class="history-date">대여일: {{ formatDate(record.borrowDate) }}</p>
                <p v-if="record.returnDate" class="history-date">반납일: {{ formatDate(record.returnDate) }}</p>
                <p class="history-status" :class="record.status">{{ getStatusText(record.status) }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

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
            <div class="input-container">
              <input 
                :type="showCurrentPassword ? 'text' : 'password'"
                v-model="passwordForm.currentPassword"
                class="form-input"
                :class="{ 
                  error: passwordForm.errors.currentPassword,
                  'has-buttons': true
                }"
                placeholder="현재 비밀번호를 입력하세요"
              >
              
              <div class="input-buttons">
                <button 
                v-if="passwordForm.currentPassword"
                type="button"
                @click="clearCurrentPassword"
                class="clear-button"
                title="입력 내용 지우기"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                  <path d="M15 9l-6 6M9 9l6 6" stroke="currentColor" stroke-width="2"/>
                </svg>
              </button>
                <button 
                  type="button"
                  @click="toggleCurrentPassword"
                  class="toggle-password-button"
                  :title="showCurrentPassword ? '비밀번호 숨기기' : '비밀번호 보기'"
                >
                  <svg v-if="showCurrentPassword" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" stroke="currentColor" stroke-width="2"/>
                    <line x1="1" y1="1" x2="23" y2="23" stroke="currentColor" stroke-width="2"/>
                  </svg>
                  <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="2"/>
                    <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </button>
              </div>
            </div>
            <div v-if="passwordForm.errors.currentPassword" class="error-message">
              {{ passwordForm.errors.currentPassword }}
            </div>
          </div>

          <div class="input-group">
            <label class="input-label">새 비밀번호</label>
            <div class="input-container">
              <input 
                :type="showNewPassword ? 'text' : 'password'"
                v-model="passwordForm.newPassword"
                class="form-input"
                :class="{ 
                  error: passwordForm.errors.newPassword,
                  'has-buttons': true
                }"
                placeholder="새 비밀번호를 입력하세요 (4-8자, 영문+숫자)"
              >
              
              <div class="input-buttons">
                <button 
                v-if="passwordForm.newPassword"
                type="button"
                @click="clearNewPassword"
                class="clear-button"
                title="입력 내용 지우기"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                  <path d="M15 9l-6 6M9 9l6 6" stroke="currentColor" stroke-width="2"/>
                </svg>
              </button>
                <button 
                  type="button"
                  @click="toggleNewPassword"
                  class="toggle-password-button"
                  :title="showNewPassword ? '비밀번호 숨기기' : '비밀번호 보기'"
                >
                  <svg v-if="showNewPassword" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" stroke="currentColor" stroke-width="2"/>
                    <line x1="1" y1="1" x2="23" y2="23" stroke="currentColor" stroke-width="2"/>
                  </svg>
                  <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="2"/>
                    <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </button>
              </div>
            </div>
            <div v-if="passwordForm.errors.newPassword" class="error-message">
              {{ passwordForm.errors.newPassword }}
            </div>
          </div>

          <div class="input-group">
            <label class="input-label">새 비밀번호 확인</label>
            <div class="input-container">
              <input 
                :type="showConfirmPassword ? 'text' : 'password'"
                v-model="passwordForm.confirmPassword"
                class="form-input"
                :class="{ 
                  error: passwordForm.errors.confirmPassword,
                  'has-buttons': true
                }"
                placeholder="새 비밀번호를 다시 입력하세요"
              >
              
              <div class="input-buttons">
                <button 
                v-if="passwordForm.confirmPassword"
                type="button"
                @click="clearConfirmPassword"
                class="clear-button"
                title="입력 내용 지우기"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                  <path d="M15 9l-6 6M9 9l6 6" stroke="currentColor" stroke-width="2"/>
                </svg>
              </button>
                <button 
                  type="button"
                  @click="toggleConfirmPassword"
                  class="toggle-password-button"
                  :title="showConfirmPassword ? '비밀번호 숨기기' : '비밀번호 보기'"
                >
                  <svg v-if="showConfirmPassword" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" stroke="currentColor" stroke-width="2"/>
                    <line x1="1" y1="1" x2="23" y2="23" stroke="currentColor" stroke-width="2"/>
                  </svg>
                  <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="2"/>
                    <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </button>
              </div>
            </div>
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
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, nextTick } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()

const jwtToken = ref(localStorage.getItem('jwtToken'))

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

// 대여 기록 및 통계
const rentalHistory = ref([])
const rentalSummary = ref({
  totalBorrowed: 0,
  totalReturned: 0,
  currentlyBorrowed: 0,
  overdueCount: 0
})

// 모달 상태
const passwordModal = ref(false)
const discordModal = ref(false)
const courseModal = ref(false)

// 과정 드롭다운
const courseDropdownOpen = ref(false)
const courseDropdownWrapper = ref(null)
const courseSearchQuery = ref('')
const courseList = ref([])

// 비밀번호 표시/숨김 상태 추가
const showCurrentPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)

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
    alert('유저 정보 로드 실패:', error.response?.data)
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
    alert('찜 목록 로드 실패:', error.response?.data)
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
      rentalHistory.value = data.history || []
      rentalSummary.value = data.summary || {
        totalBorrowed: 0,
        totalReturned: 0,
        currentlyBorrowed: 0,
        overdueCount: 0
      }
    }
  } catch (error) {
    alert('대여 기록 로드 실패:', error.response?.data)
  }
}

// 과정 목록 가져오기
async function getCourseList() {
  try {
    const token = localStorage.getItem('jwtToken')
    const res = await fetch('http://localhost:8080/api/work24/course', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      }
    })
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
    alert('과정 조회 실패:', err.response?.data)
  }
}

// 도서 상세 페이지로 이동
function goToBookDetail(seqBook) {
  router.push(`/books/info/${seqBook}`)
}

// 찜 해제
async function removeFavorite(seqBook) {
  try {
    if (!jwtToken.value) {
      alert('로그인이 필요합니다.')
      router.push('/login')
      return
    }
    
    // console.log('삭제할 seqBook:', seqBook)

    const response = await axios.delete('http://localhost:8080/favor', {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${jwtToken.value}`
      },
      data: seqBook
    })

    if (response.status === 200) {
      favoriteBooks.value = favoriteBooks.value.filter(book => book.seqBook !== seqBook)
    }
  } catch (error) {
    // console.error('찜 해제 실패:', error)
    
    if (error.response) {
      const status = error.response.status
      const message = error.response.data || '오류가 발생했습니다.'
      
      if (status === 403) {
        alert(message)
      } else if (status === 401) {
        alert('로그인이 필요하거나 토큰이 만료되었습니다.')
        localStorage.removeItem('jwtToken')
        router.push('/login')
      } else {
        alert(`오류: ${message}`)
      }
    } else if (error.request) {
      alert('서버와의 연결에 실패했습니다. 잠시 후 다시 시도해주세요.')
    } else {
      alert('찜 해제 중 오류가 발생했습니다.')
    }
  }
}

// 날짜 포맷팅
function formatDate(dateString) {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR')
}

// 상태 텍스트 변환
function getStatusText(status) {
  const statusMap = {
    'booked': '대여중',
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
  // 비밀번호 표시/숨김 상태도 리셋
  showCurrentPassword.value = false
  showNewPassword.value = false
  showConfirmPassword.value = false
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

// 비밀번호 표시/숨김 토글 함수들
function toggleCurrentPassword() {
  showCurrentPassword.value = !showCurrentPassword.value
}

function toggleNewPassword() {
  showNewPassword.value = !showNewPassword.value
}

function toggleConfirmPassword() {
  showConfirmPassword.value = !showConfirmPassword.value
}

// 입력 내용 삭제 함수들
function clearCurrentPassword() {
  passwordForm.value.currentPassword = ''
  passwordForm.value.errors.currentPassword = ''
}

function clearNewPassword() {
  passwordForm.value.newPassword = ''
  passwordForm.value.errors.newPassword = ''
}

function clearConfirmPassword() {
  passwordForm.value.confirmPassword = ''
  passwordForm.value.errors.confirmPassword = ''
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
    alert('비밀번호 변경 중 오류가 발생했습니다.')
  } finally {
    passwordForm.value.loading = false
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

.rental-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 1.5rem;
  text-align: center;
  transition: all 0.2s;
}

.stat-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.stat-card.warning {
  background: #fef2f2;
  border-color: #fecaca;
}

.stat-card.warning .stat-value {
  color: #dc2626;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 0.5rem;
}

.stat-label {
  font-size: 0.875rem;
  color: #64748b;
  font-weight: 500;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.history-item {
  padding: 1.5rem;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.history-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 0.5rem 0;
  line-height: 1.4;
}

.history-author {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0 0 1rem 0;
}

.history-details {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  align-items: center;
}

.history-date {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0;
}

.history-status {
  font-size: 0.875rem;
  font-weight: 500;
  margin: 0;
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
}

.history-status.booked {
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
}

.modal-content {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  width: 100%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
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

/* 개선된 입력 필드 스타일 */
.input-container {
  position: relative;
  display: flex;
  align-items: center;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.2s;
  outline: none;
}

.form-input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input.error {
  border-color: #dc2626;
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1);
}

.form-input.has-buttons {
  padding-right: 80px;
}

.input-buttons {
  position: absolute;
  right: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.clear-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  color: #9ca3af;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.clear-button:hover {
  color: #6b7280;
  background: #f3f4f6;
}

.toggle-password-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  color: #9ca3af;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toggle-password-button:hover {
  color: #6b7280;
  background: #f3f4f6;
}

.error-message {
  font-size: 0.875rem;
  color: #dc2626;
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

  .rental-stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .history-details {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
}
</style>