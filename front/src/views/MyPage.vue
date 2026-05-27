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
            <label class="info-label">캠퍼스</label>
            <div class="info-value">
              <span class="value-text">{{ userInfo.campusName || '-' }}</span>
            </div>
          </div>

          <div class="info-item">
            <label class="info-label">수강중인 과정</label>
            <div class="info-value">
              <span class="value-text">{{ currentCourse }}</span>
            </div>
          </div>

          <div class="info-item">
            <label class="info-label">계정 상태</label>
            <div class="info-value">
              <span class="value-text" :class="getUserStatusClass()">{{ getUserStatusText() }}</span>
            </div>
          </div>
        </div>

        <!-- 과정 종료 안내 -->
        <div v-if="!userInfo.seqCourse && userInfo.statusUser === 'stop'" class="course-ended-notice">
          <div class="notice-icon">⚠️</div>
          <div class="notice-content">
            <h4>과정이 종료되었습니다</h4>
            <p>수강하신 과정이 종료되어 도서 대출 및 반납 서비스를 이용하실 수 없습니다. 회원 탈퇴를 진행해주세요.</p>
          </div>
        </div>

        <!-- 정지 상태 안내 -->
        <div v-else-if="userInfo.statusUser === 'stop'" class="status-stop-notice">
          <div class="notice-icon">🚫</div>
          <div class="notice-content">
            <h4>계정이 정지 상태입니다</h4>
            <p>현재 계정이 정지 상태로 도서 대출 및 반납 서비스를 이용하실 수 없습니다.</p>
          </div>
        </div>

        <!-- 연체 상태 안내 -->
        <div v-else-if="userInfo.statusUser === 'overdue'" class="status-overdue-notice">
          <div class="notice-icon">🚨</div>
          <div class="notice-content">
            <h4>연체 중인 도서가 있습니다</h4>
            <p>연체된 도서를 반납하시면 다시 대출 서비스를 이용하실 수 있습니다.</p>
          </div>
        </div>

        <div class="account-actions">
          <button @click="openWithdrawModal" class="withdraw-button">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" stroke="currentColor" stroke-width="2"/>
            </svg>
            회원 탈퇴
          </button>
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

    <!-- 회원 탈퇴 모달 -->
    <div v-if="withdrawModal" class="modal-overlay" @click="closeWithdrawModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3 class="modal-title">회원 탈퇴</h3>
          <button @click="closeWithdrawModal" class="close-button">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <path d="M15 9l-6 6M9 9l6 6" stroke="currentColor" stroke-width="2"/>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <div class="withdraw-warning">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="#ef4444" stroke-width="2"/>
              <path d="M12 8v4M12 16h.01" stroke="#ef4444" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <h4>정말 탈퇴하시겠습니까?</h4>
            <p>회원 탈퇴 시 다음 사항을 확인해주세요:</p>
            <ul class="withdraw-notice-list">
              <li>대출 중인 도서가 있으면 탈퇴할 수 없습니다.</li>
              <li>연체 중인 도서가 있으면 탈퇴할 수 없습니다.</li>
              <li>탈퇴 후 모든 개인정보가 삭제되며 복구할 수 없습니다.</li>
              <li>탈퇴 후 찜 목록, 대출 기록 등 모든 데이터가 삭제됩니다.</li>
            </ul>
          </div>
        </div>

        <div class="modal-actions">
          <button type="button" @click="closeWithdrawModal" class="cancel-button">취소</button>
          <button type="button" @click="handleWithdraw" class="withdraw-confirm-button" :disabled="withdrawLoading">
            {{ withdrawLoading ? '탈퇴 중...' : '탈퇴하기' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, nextTick } from 'vue'
import * as userApi from '@/api/user'
import * as favorApi from '@/api/favor'
import * as historyApi from '@/api/history'
import * as courseApi from '@/api/course'
import { useRouter } from 'vue-router'
import { swAlert, swConfirm } from '@/utils/sweetAlert'

const router = useRouter()

// 유저 정보
const userInfo = ref({
  seqCourse: null,
  seqCampus: null,
  campusName: '',
  idUser: '',
  nameUser: '',
  dcUser: '',
  statusUser: ''
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
const withdrawModal = ref(false)

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

const withdrawLoading = ref(false)

// API 헤더 설정
const getHeaders = () => ({
  'Content-Type': 'application/json'
})

// 과정 검색 필터링
const filteredCourseList = computed(() => {
  if (!courseSearchQuery.value) return courseList.value
  return courseList.value.filter(course => 
    course.title.toLowerCase().includes(courseSearchQuery.value.toLowerCase()) ||
    course.trprDegr.toString().includes(courseSearchQuery.value)
  )
})

// 사용자 인증 확인
const checkUserAuth = async () => {
  const userType = sessionStorage.getItem('userType')

  if (userType !== 'user') {
    await swAlert('로그인이 필요합니다.', 'info')
    router.push('/login')
    return false
  }
  return true
}

onMounted(async () => {
  if (!(await checkUserAuth())) {
    return
  }

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
    const response = await userApi.getMe()
    const data = response.data.data
    userInfo.value = data

    // 현재 과정명 설정 (과정 목록에서 찾아서 설정)
    if (data.seqCourse) {
      await getCourseList()
      const course = courseList.value.find(c => c.seqCourse === data.seqCourse)
      if (course) {
        currentCourse.value = `${course.title} ${course.trprDegr}기`
      } else {
        currentCourse.value = '과정 정보 없음'
      }
    } else {
      currentCourse.value = '과정 정보 없음 (과정 종료)'
    }
  } catch (error) {
    if (error.response?.status === 401) {
      await swAlert('로그인이 필요하거나 세션이 만료되었습니다.', 'warning')
      sessionStorage.removeItem('userType')
      sessionStorage.removeItem('campusId')
      router.push('/login')
    }
  }
}

// 찜한 도서 목록 로드
async function loadFavoriteBooks() {
  try {
    const response = await favorApi.getAll()
    favoriteBooks.value = response.data.data || []
  } catch (error) {
    console.error('찜 목록 로드 실패:', error.response?.data?.msg)
  }
}

// 대여 기록 로드
async function loadRentalHistory() {
  try {
    const response = await historyApi.getMe()
    const data = response.data.data
    rentalHistory.value = data?.history || []
    rentalSummary.value = data?.summary || {
      totalBorrowed: 0,
      totalReturned: 0,
      currentlyBorrowed: 0,
      overdueCount: 0
    }
  } catch (error) {
    console.error('대여 기록 로드 실패:', error.response?.data?.msg)
  }
}

// 과정 목록 가져오기
async function getCourseList() {
  try {
    const res = await courseApi.getWork24()
    const data = res.data.data
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

    const dbRes = await courseApi.getAll()
    const dbCourses = dbRes.data.data

    for (const apiItem of apiCourses) {
      const exists = dbCourses.find(dbItem => dbItem.nameCourse === apiItem.nameCourse)
      if (!exists) {
        await courseApi.create({
          nameCourse: apiItem.nameCourse,
          startDtCourse: apiItem.startDtCourse,
          finishDtCourse: apiItem.finishDtCourse
        })
      }
    }

    for (const dbItem of dbCourses) {
      const exists = apiCourses.find(apiItem => apiItem.nameCourse === dbItem.nameCourse)
      if (!exists) {
        await courseApi.remove(dbItem.seqCourse)
      }
    }

    for (const apiItem of apiCourses) {
      const dbItem = dbCourses.find(db => db.nameCourse === apiItem.nameCourse)
      if (dbItem) {
        const isDifferent =
          dbItem.startDtCourse !== apiItem.startDtCourse ||
          dbItem.finishDtCourse !== apiItem.finishDtCourse

        if (isDifferent) {
          await courseApi.update(dbItem.seqCourse, {
            nameCourse: apiItem.nameCourse,
            startDtCourse: apiItem.startDtCourse,
            finishDtCourse: apiItem.finishDtCourse
          })
        }
      }
    }

    const finalDbRes = await courseApi.getAll()
    const finalDbCourses = finalDbRes.data.data

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
    console.warn('과정 조회 실패:', err.response?.data)
  }
}

// 도서 상세 페이지로 이동
function goToBookDetail(seqBook) {
  router.push(`/books/info/${seqBook}`)
}

// 찜 해제
async function removeFavorite(seqBook) {
  try {
    if (!sessionStorage.getItem('userType')) {
      await swAlert('로그인이 필요합니다.', 'info')
      router.push('/login')
      return
    }

    const response = await favorApi.remove(seqBook)

    if (response.status === 200) {
      favoriteBooks.value = favoriteBooks.value.filter(book => book.seqBook !== seqBook)
    }
  } catch (error) {
    // console.error('찜 해제 실패:', error)
    
    if (error.response) {
      const status = error.response.status
      const message = error.response.data?.msg || '오류가 발생했습니다.'
      
      if (status === 403) {
        await swAlert(message, 'warning')
      } else if (status === 401) {
        await swAlert('로그인이 필요하거나 세션이 만료되었습니다.', 'warning')
        sessionStorage.removeItem('userType')
        sessionStorage.removeItem('campusId')
        router.push('/login')
      } else {
        await swAlert(`오류: ${message}`, 'error')
      }
    } else if (error.request) {
      await swAlert('서버와의 연결에 실패했습니다. 잠시 후 다시 시도해주세요.', 'error')
    } else {
      await swAlert('찜 해제 중 오류가 발생했습니다.', 'error')
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

function openWithdrawModal() {
  withdrawModal.value = true
}

function closeWithdrawModal() {
  withdrawModal.value = false
}

function getUserStatusText() {
  const statusMap = {
    'available': '정상',
    'overdue': '연체',
    'stop': '정지'
  }
  return statusMap[userInfo.value.statusUser] || userInfo.value.statusUser || '알 수 없음'
}

function getUserStatusClass() {
  const statusClassMap = {
    'available': 'status-available',
    'overdue': 'status-overdue',
    'stop': 'status-stop'
  }
  return statusClassMap[userInfo.value.statusUser] || ''
}

async function handleWithdraw() {
  if (!(await swConfirm('정말로 탈퇴하시겠습니까?', '이 작업은 되돌릴 수 없습니다.', { isDangerous: true }))) {
    return
  }

  withdrawLoading.value = true

  try {
    await userApi.deleteAccount()

    sessionStorage.removeItem('userType')
    sessionStorage.removeItem('campusId')

    await swAlert('회원 탈퇴가 완료되었습니다.', 'success')

    // 강제 새로고침으로 메인 페이지 이동 (히스토리 없이)
    if (window.location.pathname === '/') {
      window.location.reload()
    } else {
      window.location.replace('/')
    }
  } catch (error) {
    await swAlert(error.response?.data?.msg || '회원 탈퇴 중 오류가 발생했습니다.', 'error')
    withdrawLoading.value = false
  }
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
    } else if (withdrawModal.value) {
      closeWithdrawModal()
    }
  }
}

// 비밀번호 검증
async function validatePassword(password) {
  try {
    const response = await userApi.validatePassword(password)
    return response.data.data === true
  } catch (error) {
    console.error('비밀번호 검증 중 오류:', error)
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
    await userApi.updatePassword(passwordForm.value.newPassword)
    await swAlert('비밀번호가 성공적으로 변경되었습니다.', 'success')
    closePasswordModal()
  } catch (error) {
    await swAlert(error.response?.data?.msg || '비밀번호 변경 중 오류가 발생했습니다.', 'error')
  } finally {
    passwordForm.value.loading = false
  }
}
</script>

<style scoped>
.mypage-wrapper {
  min-height: 100vh;
  background: var(--pb-color-canvas);
  padding: 20px 0 40px;
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
  color: var(--pb-color-heading);
  margin: 0 0 0.5rem 0;
}

.page-subtitle {
  font-size: 1.1rem;
  color: var(--pb-color-text-muted);
  margin: 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--pb-color-border);
}

.section-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: var(--pb-color-surface-subtle);
  border-radius: var(--pb-radius-sm);
  color: var(--pb-color-text);
}

.section-icon.favorites {
  background: var(--pb-color-danger-soft);
  color: var(--pb-color-danger);
}

.section-icon.rental {
  background: var(--pb-color-brand-soft);
  color: var(--pb-color-brand);
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0;
  flex: 1;
}

.count-badge {
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text);
  padding: 4px 8px;
  border-radius: var(--pb-radius-sm);
  font-size: 0.875rem;
  font-weight: 500;
  min-width: 24px;
  text-align: center;
}

.user-info-section,
.favorites-section,
.rental-history-section {
  background: var(--pb-color-surface);
  border-radius: var(--pb-radius-md);
  padding: 2rem;
  margin-bottom: 2rem;
  border: 1px solid var(--pb-color-border);
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
  border-bottom: 1px solid var(--pb-color-surface-subtle);
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-weight: 500;
  color: var(--pb-color-text);
  min-width: 120px;
}

.info-value {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex: 1;
}

.value-text {
  color: var(--pb-color-heading);
  flex: 1;
  text-align: right;
}

.edit-button {
  display: flex;
  align-items: center;
  gap: 6px;
  background: var(--pb-color-surface-subtle);
  border: none;
  padding: 8px 12px;
  border-radius: var(--pb-radius-xs);
  cursor: pointer;
  transition: background 0.15s;
  font-size: 0.875rem;
  color: var(--pb-color-text);
}

.edit-button:hover {
  background: var(--pb-color-surface-muted);
}

.empty-state {
  text-align: center;
  padding: 3rem 2rem;
  color: var(--pb-color-text-muted);
}

.empty-icon {
  margin-bottom: 1rem;
  color: var(--pb-color-border-strong);
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
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  cursor: pointer;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.book-card:hover {
  border-color: var(--pb-color-border-strong);
  box-shadow: var(--pb-shadow-xs);
}

.book-info {
  flex: 1;
}

.book-title {
  font-size: 1rem;
  font-weight: 500;
  color: var(--pb-color-heading);
  margin: 0 0 0.25rem 0;
  line-height: 1.4;
}

.book-author {
  font-size: 0.875rem;
  color: var(--pb-color-text-muted);
  margin: 0;
}

.favorite-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  border-radius: var(--pb-radius-xs);
  transition: background 0.15s;
  color: var(--pb-color-border-strong);
}

.favorite-button.active {
  color: var(--pb-color-danger);
}

.favorite-button:hover {
  background: var(--pb-color-surface-subtle);
}

.rental-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: var(--pb-color-surface-subtle);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  padding: 1.5rem;
  text-align: center;
  transition: border-color 0.15s;
}

.stat-card:hover {
  border-color: var(--pb-color-border-strong);
}

.stat-card.warning {
  background: var(--pb-color-danger-soft);
  border-color: var(--pb-color-danger-muted);
}

.stat-card.warning .stat-value {
  color: var(--pb-color-danger);
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: var(--pb-color-heading);
  margin-bottom: 0.5rem;
}

.stat-label {
  font-size: 0.875rem;
  color: var(--pb-color-text-muted);
  font-weight: 500;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.history-item {
  padding: 1.5rem;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
}

.history-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0 0 0.5rem 0;
  line-height: 1.4;
}

.history-author {
  font-size: 0.875rem;
  color: var(--pb-color-text-muted);
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
  color: var(--pb-color-text-muted);
  margin: 0;
}

.history-status {
  font-size: 0.875rem;
  font-weight: 500;
  margin: 0;
  display: inline-block;
  padding: 4px 12px;
  border-radius: var(--pb-radius-sm);
}

.history-status.booked {
  background: var(--pb-color-brand-soft);
  color: var(--pb-color-brand-strong);
}

.history-status.returned {
  background: var(--pb-color-success-soft);
  color: var(--pb-color-success);
}

.history-status.overdue {
  background: var(--pb-color-danger-soft);
  color: var(--pb-color-danger);
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
  background: var(--pb-color-surface);
  border-radius: var(--pb-radius-md);
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
  border-bottom: 1px solid var(--pb-color-border);
}

.modal-header h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0;
}

.close-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: var(--pb-radius-xs);
  color: var(--pb-color-text-muted);
  transition: background 0.15s, color 0.15s;
}

.close-button:hover {
  background: var(--pb-color-surface-subtle);
  color: var(--pb-color-text);
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
  color: var(--pb-color-text);
  font-size: 0.875rem;
}

.input-container {
  position: relative;
  display: flex;
  align-items: center;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  font-size: 1rem;
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  transition: border-color 0.15s, box-shadow 0.15s;
  outline: none;
}

.form-input:focus {
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px var(--pb-color-brand-soft);
}

.form-input.error {
  border-color: var(--pb-color-danger);
  box-shadow: 0 0 0 3px var(--pb-color-danger-soft);
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
  border-radius: var(--pb-radius-xs);
  color: var(--pb-color-text-soft);
  transition: color 0.15s, background 0.15s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.clear-button:hover {
  color: var(--pb-color-text-muted);
  background: var(--pb-color-surface-subtle);
}

.toggle-password-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: var(--pb-radius-xs);
  color: var(--pb-color-text-soft);
  transition: color 0.15s, background 0.15s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toggle-password-button:hover {
  color: var(--pb-color-text-muted);
  background: var(--pb-color-surface-subtle);
}

.error-message {
  font-size: 0.875rem;
  color: var(--pb-color-danger);
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
  border-radius: var(--pb-radius-sm);
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
}

.cancel-button {
  background: var(--pb-color-surface-subtle);
  border: 1px solid var(--pb-color-border);
  color: var(--pb-color-text);
}

.cancel-button:hover {
  background: var(--pb-color-surface-muted);
}

.submit-button {
  background: var(--pb-color-brand);
  border: 1px solid var(--pb-color-brand);
  color: white;
}

.submit-button:hover:not(:disabled) {
  background: var(--pb-color-brand-strong);
}

.submit-button:disabled {
  background: var(--pb-color-border);
  border-color: var(--pb-color-border);
  cursor: not-allowed;
}

.account-actions {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid var(--pb-color-border);
  display: flex;
  justify-content: flex-end;
}

.withdraw-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: var(--pb-color-danger-soft);
  border: 1px solid var(--pb-color-danger-muted);
  border-radius: var(--pb-radius-sm);
  color: var(--pb-color-danger);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
}

.withdraw-button:hover {
  background: var(--pb-color-danger-soft);
  border-color: var(--pb-color-danger);
}

.withdraw-warning {
  text-align: center;
  padding: 1rem 0;
}

.withdraw-warning svg {
  margin-bottom: 1rem;
}

.withdraw-warning h4 {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0 0 0.5rem 0;
}

.withdraw-warning > p {
  color: var(--pb-color-text-muted);
  margin: 0 0 1.5rem 0;
}

.withdraw-notice-list {
  text-align: left;
  list-style: none;
  padding: 1rem;
  margin: 1.5rem 0 0 0;
  background: var(--pb-color-danger-soft);
  border: 1px solid var(--pb-color-danger-muted);
  border-radius: var(--pb-radius-sm);
}

.withdraw-notice-list li {
  padding: 0.5rem 0;
  color: var(--pb-color-danger);
  font-size: 0.875rem;
  position: relative;
  padding-left: 1.5rem;
}

.withdraw-notice-list li::before {
  content: '⚠️';
  position: absolute;
  left: 0;
}

.withdraw-confirm-button {
  padding: 12px 24px;
  border-radius: var(--pb-radius-sm);
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
  background: var(--pb-color-danger);
  border: 1px solid var(--pb-color-danger);
  color: white;
}

.withdraw-confirm-button:hover:not(:disabled) {
  background: var(--pb-color-danger-strong);
  border-color: var(--pb-color-danger-strong);
}

.withdraw-confirm-button:disabled {
  background: var(--pb-color-border);
  border-color: var(--pb-color-border);
  cursor: not-allowed;
}

.status-available {
  color: var(--pb-color-success);
  font-weight: 600;
}

.status-overdue {
  color: var(--pb-color-danger);
  font-weight: 600;
}

.status-stop {
  color: var(--pb-color-warning);
  font-weight: 600;
}

.course-ended-notice,
.status-stop-notice,
.status-overdue-notice {
  margin-top: 1.5rem;
  padding: 1.25rem;
  border-radius: var(--pb-radius-md);
  display: flex;
  gap: 1rem;
  align-items: flex-start;
}

.course-ended-notice {
  background: var(--pb-color-warning-soft);
  border: 1px solid var(--pb-color-warning-muted);
}

.status-stop-notice {
  background: var(--pb-color-danger-soft);
  border: 1px solid var(--pb-color-danger-muted);
}

.status-overdue-notice {
  background: var(--pb-color-danger-soft);
  border: 1px solid var(--pb-color-danger-muted);
}

.notice-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.notice-content h4 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0 0 0.5rem 0;
}

.notice-content p {
  font-size: 0.9rem;
  color: var(--pb-color-text-muted);
  margin: 0;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .mypage-container { padding: 0 1rem; }
  .user-info-section, .favorites-section, .rental-history-section { padding: 1.5rem; }
  .info-item { flex-direction: column; align-items: flex-start; gap: 0.5rem; }
  .info-value { width: 100%; justify-content: space-between; }
  .books-grid { grid-template-columns: 1fr; }
  .modal-overlay { padding: 1rem; }
  .modal-content { padding: 1.5rem; }
  .modal-actions { flex-direction: column; }
  .rental-stats { grid-template-columns: repeat(2, 1fr); }
  .history-details { flex-direction: column; align-items: flex-start; gap: 0.5rem; }
}
</style>