<template>
  <div class="student-account-management">
    <div class="section-header">
      <h2 class="section-title">학생 계정 관리</h2>
      <p class="section-description">학생 계정을 조회, 삭제할 수 있습니다.</p>
    </div>

    <!-- 통계 카드 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2"/>
            <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ userList.length }}</div>
          <div class="stat-label">총 학생</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M22 12H18L15 21L9 3L6 12H2" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ getActiveStudents() }}</div>
          <div class="stat-label">대출 가능 학생</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ getOverdueStudents() }}</div>
          <div class="stat-label">연체 학생</div>
        </div>
      </div>
    </div>

    <!-- 필터 및 검색 -->
    <div class="filter-bar">
      <div class="search-box">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
          <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2"/>
        </svg>
        <input 
          type="text" 
          placeholder="학생 이름 또는 ID로 검색..." 
          v-model="searchQuery"
          @input="filterUsers"
        />
      </div>
      
      <div class="filter-group">
        <select v-model="selectedStatus" @change="filterUsers" class="status-filter">
          <option value="">모든 상태</option>
          <option value="available">정상</option>
          <option value="stop">정지</option>
          <option value="overdue">연체</option>
        </select>
      </div>
      
      <!-- 캠퍼스 필터 (전체 관리자만 표시) -->
      <div v-if="showCampusFilter" class="filter-group">
        <label class="filter-label">캠퍼스</label>
        <select v-model="selectedCampus" @change="onCampusChange" class="status-filter">
          <option value="">전체 캠퍼스</option>
          <option
            v-for="campus in campuses"
            :key="campus.seqCampus"
            :value="campus.seqCampus"
          >
            {{ campus.nameCampus }}
          </option>
        </select>
      </div>

      <button class="export-btn" @click="exportData">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" stroke-width="2"/>
          <polyline points="7,10 12,15 17,10" stroke="currentColor" stroke-width="2"/>
          <line x1="12" y1="15" x2="12" y2="3" stroke="currentColor" stroke-width="2"/>
        </svg>
        엑셀로 내보내기
      </button>
    </div>

    <!-- 학생 목록 테이블 -->
    <div class="student-table-container">
      <div class="table-header">
        <h3>학생 목록</h3>
        <div class="table-info">
          <span>총 {{ filteredUserList.length }}명</span>
        </div>
      </div>
      
      <div class="table-wrapper">
        <table class="student-table">
          <thead>
            <tr>
              <th>학생 명</th>
              <th>학생 ID</th>
              <th>상태</th>
              <th>가입 일</th>
              <th>과정 명</th>
              <th>과정 기간</th>
              <th>작업</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in pagedUserList" :key="user.idUser" class="student-row">
              <td class="student-name">{{ user.nameUser }}</td>
              <td class="student-id">{{ user.idUser }}</td>
              <td class="student-status">
                <span :class="['status-badge', getUserStatusClass(user.statusUser)]">
                  {{ getUserStatusText(user.statusUser) }}
                </span>
              </td>
              <td class="student-date">{{ formatDate(user.createdAt) }}</td>
              <td class="student-course">{{ user.courseName || '-' }}</td>
              <td class="student-period">
                <div v-if="user.courseStartDt && user.courseEndDt" class="course-period">
                  <div class="period-dates">
                    {{ formatDate(user.courseStartDt) }} ~ {{ formatDate(user.courseEndDt) }}
                  </div>
                  <div class="period-status" :class="getPeriodStatusClass(user.courseEndDt)">
                    {{ getPeriodStatus(user.courseEndDt) }}
                  </div>
                </div>
                <span v-else>-</span>
              </td>
              <td class="student-actions">
                <button class="view-btn" @click="viewUserDetail(user)">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M1 12S5 4 12 4S23 12 23 12S19 20 12 20S1 12 1 12Z" stroke="currentColor" stroke-width="2"/>
                    <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </button>
                <button class="reset-pw-btn" @click="confirmResetPassword(user)" title="비밀번호 초기화">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" stroke-width="2"/>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </button>
                <button class="delete-btn" @click="confirmDeleteUser(user)">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <polyline points="3,6 5,6 21,6" stroke="currentColor" stroke-width="2"/>
                    <path d="M19,6V20C19,20.5304 18.7893,21.0391 18.4142,21.4142C18.0391,21.7893 17.5304,22 17,22H7C6.46957,22 5.96086,21.7893 5.58579,21.4142C5.21071,21.0391 5,20.5304 5,20V6M8,6V4C8,3.46957 8.21071,2.96086 8.58579,2.58579C8.96086,2.21071 9.46957,2 10,2H14C14.5304,2 15.0391,2.21071 15.4142,2.58579C15.7893,2.96086 16,3.46957 16,4V6" stroke="currentColor" stroke-width="2"/>
                    <line x1="10" y1="11" x2="10" y2="17" stroke="currentColor" stroke-width="2"/>
                    <line x1="14" y1="11" x2="14" y2="17" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 페이지네이션 -->
      <div class="gl-pagination" v-if="totalPages > 1">
        <span class="gl-pagination-info">{{ paginationInfo }}</span>
        <nav class="gl-pagination-nav">
          <button class="gl-page-btn prev-btn" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            이전
          </button>
          <template v-for="item in paginationItems" :key="String(item) + '-um'">
            <span v-if="item === '...'" class="gl-page-ellipsis">…</span>
            <button v-else class="gl-page-btn" :class="{ active: item === currentPage }" @click="changePage(item)">{{ item }}</button>
          </template>
          <button class="gl-page-btn next-btn" :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">
            다음
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </button>
        </nav>
      </div>
    </div>

    <!-- 학생 상세 조회 모달 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-content detail-modal" @click.stop>
        <div class="modal-header">
          <h3>학생 상세 정보</h3>
          <button class="modal-close" @click="closeDetailModal">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2"/>
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2"/>
            </svg>
          </button>
        </div>
        <div class="detail-content">
          <div class="detail-section">
            <h4>기본 정보</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <label>학생 명</label>
                <span>{{ selectedUser.nameUser }}</span>
              </div>
              <div class="detail-item">
                <label>학생 ID</label>
                <span>{{ selectedUser.idUser }}</span>
              </div>
              <div class="detail-item">
                <label>상태</label>
                <span :class="['status-badge', getUserStatusClass(selectedUser.statusUser)]">
                  {{ getUserStatusText(selectedUser.statusUser) }}
                </span>
              </div>
              <div class="detail-item">
                <label>가입 일</label>
                <span>{{ formatDate(selectedUser.createdAt) }}</span>
              </div>
            </div>
          </div>
          
          <div class="detail-section" v-if="selectedUser.courseName">
            <h4>과정 정보</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <label>과정명</label>
                <span>{{ selectedUser.courseName }}</span>
              </div>
              <div class="detail-item">
                <label>시작 일</label>
                <span>{{ formatDate(selectedUser.courseStartDt) }}</span>
              </div>
              <div class="detail-item">
                <label>종료 일</label>
                <span>{{ formatDate(selectedUser.courseEndDt) }}</span>
              </div>
              <div class="detail-item">
                <label>진행 상태</label>
                <span :class="['period-status', getPeriodStatusClass(selectedUser.courseEndDt)]">
                  {{ getPeriodStatus(selectedUser.courseEndDt) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 학생 삭제 확인 모달 (비밀번호 검증 포함) -->
    <div v-if="showDeleteModal" class="modal-overlay" @click="closeDeleteModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>학생 계정 삭제 확인</h3>
          <button class="modal-close" @click="closeDeleteModal">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2"/>
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2"/>
            </svg>
          </button>
        </div>
        <div class="delete-warning">
          <div class="warning-icon">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 8V12M12 16H12.01M10.29 3.86L1.82 18C1.64466 18.3024 1.55685 18.6453 1.56455 18.9928C1.57225 19.3403 1.67516 19.6792 1.86244 19.9757C2.04973 20.2723 2.31561 20.5157 2.6289 20.6812C2.9422 20.8467 3.29427 20.9286 3.65 20.92H20.35C20.7057 20.9286 21.0578 20.8467 21.3711 20.6812C21.6844 20.5157 21.9503 20.2723 22.1376 19.9757C22.3248 19.6792 22.4278 19.3403 22.4355 18.9928C22.4432 18.6453 22.3553 18.3024 22.18 18L13.71 3.86C13.5317 3.56611 13.2807 3.32312 12.9812 3.15446C12.6817 2.98581 12.3438 2.89725 12 2.89725C11.6562 2.89725 11.3183 2.98581 11.0188 3.15446C10.7193 3.32312 10.4683 3.56611 10.29 3.86Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="warning-content">
            <h4>정말로 학생 계정을 삭제하시겠습니까?</h4>
            <p>이 작업은 되돌릴 수 없으며, 해당 학생의 모든 데이터가 삭제됩니다.</p>
            <div class="student-info">
              <strong>삭제할 학생: {{ deletingUser.nameUser }} ({{ deletingUser.idUser }})</strong>
            </div>
          </div>
        </div>
        <form @submit.prevent="deleteUser" class="modal-form">
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="closeDeleteModal">취소</button>
            <button 
              type="submit" 
              class="delete-confirm-btn" 
              :disabled="isLoading"
            >
              {{ isLoading ? '삭제 중...' : '삭제' }}
            </button>
          </div>
        </form>
      </div>
    </div>
    <!-- 학생 비밀번호 초기화 모달 -->
    <div v-if="showResetModal" class="modal-overlay" @click="closeResetModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>비밀번호 초기화</h3>
          <button class="modal-close" @click="closeResetModal">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2"/>
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2"/>
            </svg>
          </button>
        </div>
        <div class="reset-info">
          <p>학생 <strong>{{ resetingUser.nameUser }} ({{ resetingUser.idUser }})</strong>의 비밀번호를 초기화합니다.</p>
        </div>
        <form @submit.prevent="resetUserPassword" class="modal-form">
          <div class="form-group">
            <label class="form-label">새 비밀번호 <span class="required-mark">*</span></label>
            <input
              type="password"
              v-model="resetNewPassword"
              class="form-input"
              placeholder="새 비밀번호를 입력하세요 (4-8자, 영문+숫자)"
            />
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="closeResetModal">취소</button>
            <button type="submit" class="reset-confirm-btn" :disabled="isLoading">
              {{ isLoading ? '초기화 중...' : '초기화' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { exportToXlsx } from '@/utils/exportSheet'
import * as userApi from '@/api/user'
import { swAlert } from '@/utils/sweetAlert'
import { handleApiError } from '@/utils/apiErrorHandler'
import { formatDate } from '@/utils/dateFormatter'
import { getUserStatusText, getUserStatusClass } from '@/utils/statusMapper'
import { useAdminCampusFilter } from '@/composables/useAdminCampusFilter'
import { usePagination } from '@/composables/usePagination'

// 반응형 데이터
const userList = ref([])
const filteredUserList = ref([])
const isLoading = ref(false)
const searchQuery = ref('')
const selectedStatus = ref('')

// 페이지네이션
const { currentPage, totalPages, pagedList: pagedUserList,
        paginationItems, paginationInfo, changePage } = usePagination(filteredUserList, 20, '명')

// 캠퍼스 필터 관련
const {
  showCampusFilter,
  currentUserCampusId,
  selectedCampus,
  campuses,
  fetchAdminInfo,
  fetchCampuses,
  getCampusParam,
} = useAdminCampusFilter()

// 모달 상태
const showDetailModal = ref(false)
const showDeleteModal = ref(false)
const showResetModal = ref(false)

// 선택된 사용자 정보
const selectedUser = ref({})
const deletingUser = ref({})
const deletePassword = ref('')
const resetingUser = ref({})
const resetNewPassword = ref('')

// 현재 사용자 정보
const currentUser = ref({
  idAdmin: '',
  nameAdmin: ''
})

const handleKeydown = (event) => {
  if (event.key === 'Escape' && showDetailModal.value) {
    showDetailModal.value = false
  }
  if (event.key === 'Escape' && showDeleteModal.value) {
    showDeleteModal.value = false
  }
  if (event.key === 'Escape' && showResetModal.value) {
    closeResetModal()
  }
}

// 현재 사용자 정보 조회
const fetchCurrentUser = async () => {
  const data = await fetchAdminInfo()
  if (data) {
    currentUser.value = data
  }
}

// 캠퍼스 변경 핸들러
const onCampusChange = () => {
  fetchUserList()
}

// 학생 목록 조회
const fetchUserList = async () => {
  try {
    isLoading.value = true
    const campusId = showCampusFilter.value && selectedCampus.value ? selectedCampus.value : null
    const response = await userApi.getList(campusId)

    userList.value = response.data.data.map(u => ({
      nameUser: u.nameUser ?? u[0],
      idUser: u.idUser ?? u[1],
      statusUser: u.email ?? u[2],
      createdAt: u.registeredAt ?? u[3],
      courseName: u.courseName ?? u[4],
      courseStartDt: u.courseStartDt ?? u[5],
      courseEndDt: u.courseEndDt ?? u[6],
    }))
    
    filteredUserList.value = [...userList.value]
    
  } catch (error) {
    await handleApiError(error, '학생 목록을 불러오는데 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}


// 통계 계산
const getActiveStudents = () => {
  return userList.value.filter(user => user.statusUser === 'available').length
}

const getOverdueStudents = () => {
  return userList.value.filter(user => user.statusUser === 'overdue').length
}

// 과정 기간 상태 확인
const getPeriodStatus = (endDate) => {
  if (!endDate) return ''
  
  const today = new Date()
  const end = new Date(endDate)
  
  if (end < today) {
    return '완료'
  } else {
    const diffTime = end - today
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    
    if (diffDays <= 7) {
      return '곧 종료'
    } else {
      return '진행중'
    }
  }
}

const getPeriodStatusClass = (endDate) => {
  const status = getPeriodStatus(endDate)
  const statusClasses = {
    '완료': 'period-completed',
    '곧 종료': 'period-ending',
    '진행중': 'period-active'
  }
  return statusClasses[status] || ''
}

// 필터링 및 검색
const filterUsers = () => {
  let filtered = [...userList.value]
  
  // 검색어 필터링
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(user => 
      user.nameUser.toLowerCase().includes(query) ||
      user.idUser.toLowerCase().includes(query)
    )
  }
  
  // 상태 필터링
  if (selectedStatus.value) {
    filtered = filtered.filter(user => user.statusUser === selectedStatus.value)
  }
  
  filteredUserList.value = filtered
  currentPage.value = 1
}

// 학생 상세 조회
const viewUserDetail = (user) => {
  selectedUser.value = { ...user }
  showDetailModal.value = true
}

// 학생 삭제 확인
const confirmDeleteUser = (user) => {
  deletingUser.value = { ...user }
  showDeleteModal.value = true
}

// 학생 삭제
const deleteUser = async () => {
  try {
    isLoading.value = true
    
    // 학생 삭제
    const response = await userApi.removeByAdmin(deletingUser.value.idUser)
    
    await swAlert('학생 계정이 성공적으로 삭제되었습니다.', 'success')
    closeDeleteModal()
    await fetchUserList()

  } catch (error) {
    await handleApiError(error, '학생 삭제에 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

// 모달 관련 함수들
const closeDetailModal = () => {
  showDetailModal.value = false
  selectedUser.value = {}
}

const closeDeleteModal = () => {
  showDeleteModal.value = false
  deletePassword.value = ''
  deletingUser.value = {}
}

const confirmResetPassword = (user) => {
  resetingUser.value = { ...user }
  resetNewPassword.value = ''
  showResetModal.value = true
}

const closeResetModal = () => {
  showResetModal.value = false
  resetNewPassword.value = ''
  resetingUser.value = {}
}

const resetUserPassword = async () => {
  const pw = resetNewPassword.value?.trim()
  if (!pw) {
    await swAlert('새 비밀번호를 입력해주세요.', 'warning')
    return
  }
  const passwordRegex = /^(?=.*[a-zA-Z])(?=.*[0-9]).{4,8}$/
  if (!passwordRegex.test(pw)) {
    await swAlert('비밀번호는 4-8자의 영문자, 숫자를 포함해야 합니다.', 'warning')
    return
  }
  try {
    isLoading.value = true
    await userApi.adminResetPassword(resetingUser.value.idUser, pw)
    await swAlert(`${resetingUser.value.nameUser} 학생의 비밀번호가 초기화되었습니다.`, 'success')
    closeResetModal()
  } catch (error) {
    await handleApiError(error, '비밀번호 초기화에 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

// 날짜 포맷팅

const exportData = async () => {
  try {
    const campusId = showCampusFilter.value && selectedCampus.value ? selectedCampus.value : null
    const res = await userApi.exportExcel(campusId)
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = '학생계정.xlsx'
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('엑셀 내보내기 실패:', e)
  }
}

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
  await fetchCurrentUser()
  await fetchCampuses()
  await fetchUserList()
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
/* ── 루트 ── */
.student-account-management {
  max-width: 100%;
  font-size: 13px;
  color: var(--pb-color-text);
}

/* ── 섹션 헤더 ── */
.section-header {
  margin-bottom: 20px;
}

.section-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--pb-color-heading);
  margin: 0 0 3px;
}

.section-description {
  font-size: 13px;
  color: var(--pb-color-text-muted);
  margin: 0;
}

/* ── 통계 카드 ── */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-lg);
  box-shadow: var(--pb-shadow-xs);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: var(--pb-color-accent-soft);
  border-radius: var(--pb-radius-md);
  color: var(--pb-color-accent);
  flex-shrink: 0;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  line-height: 1;
  color: var(--pb-color-heading);
}

.stat-label {
  font-size: 12px;
  color: var(--pb-color-text-soft);
  margin-top: 2px;
}

/* ── 필터 바 ── */
.filter-bar {
  display: flex;
  gap: 8px;
  align-items: flex-end;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.export-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  padding: 0 14px;
  background: #fff;
  color: var(--pb-color-text-primary);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
  white-space: nowrap;
}
.export-btn:hover { background: var(--pb-color-bg-subtle); }

.search-box {
  position: relative;
  flex: 1;
  min-width: 260px;
}

.search-box svg {
  position: absolute;
  left: 10px;
  top: 50%;
  translate: 0 -50%;
  color: var(--pb-color-text-soft);
  pointer-events: none;
}

.search-box input {
  width: 100%;
  height: 34px;
  padding: 0 12px 0 36px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  box-sizing: border-box;
  transition: border-color 0.15s;
}

.search-box input:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px var(--pb-color-brand-muted);
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.filter-label {
  font-size: 11px;
  font-weight: 500;
  color: var(--pb-color-text-soft);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.status-filter {
  height: 34px;
  padding: 0 10px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  cursor: pointer;
  transition: border-color 0.15s;
}

.status-filter:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px var(--pb-color-brand-muted);
}

/* ── 테이블 컨테이너 ── */
.student-table-container {
  background: var(--pb-color-surface);
  border-radius: var(--pb-radius-lg);
  border: 1px solid var(--pb-color-border);
  box-shadow: var(--pb-shadow-sm);
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface-muted);
}

.table-header h3 {
  font-size: 13px;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0;
}

.table-info {
  font-size: 12px;
  color: var(--pb-color-text-soft);
}

.table-wrapper {
  overflow-x: auto;
}

/* ── 테이블 ── */
.student-table {
  width: 100%;
  border-collapse: collapse;
}

.student-table th {
  text-align: left;
  padding: 9px 16px;
  background: var(--pb-color-surface-subtle);
  color: var(--pb-color-text-soft);
  font-weight: 600;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 1px solid var(--pb-color-border);
  white-space: nowrap;
}

.student-table td {
  padding: 10px 16px;
  border-bottom: 1px solid var(--pb-color-border);
  color: var(--pb-color-text);
  font-size: 13px;
  vertical-align: middle;
}

.student-row:hover td {
  background: var(--pb-color-surface-muted);
}

.student-name {
  font-weight: 500;
  color: var(--pb-color-heading);
}

/* ── 상태 배지 ── */
.status-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: var(--pb-radius-xs);
  font-size: 11px;
  font-weight: 600;
  text-align: center;
  min-width: 48px;
}

.status-active {
  background: var(--pb-color-success-soft);
  color: var(--pb-color-success);
}

.status-stopped {
  background: var(--pb-color-danger-soft);
  color: var(--pb-color-danger);
}

.status-overdue {
  background: var(--pb-color-warning-soft);
  color: var(--pb-color-warning);
}

.status-default {
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text-muted);
}

/* ── 과정 기간 ── */
.course-period {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.period-dates {
  font-size: 12px;
  color: var(--pb-color-text);
}

.period-status {
  font-size: 11px;
  font-weight: 600;
  padding: 1px 7px;
  border-radius: var(--pb-radius-xs);
  width: fit-content;
}

.period-active {
  background: var(--pb-color-success-soft);
  color: var(--pb-color-success);
}

.period-ending {
  background: var(--pb-color-warning-soft);
  color: var(--pb-color-warning);
}

.period-completed {
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text-muted);
}

/* ── 액션 버튼 ── */
.student-actions {
  display: flex;
  gap: 4px;
}

.view-btn,
.reset-pw-btn,
.delete-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: var(--pb-radius-sm);
  border: 1px solid var(--pb-color-border);
  cursor: pointer;
  transition: background 0.12s, border-color 0.12s, color 0.12s;
}

.view-btn {
  background: var(--pb-color-surface);
  color: var(--pb-color-text-muted);
}

.view-btn:hover {
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text);
  border-color: var(--pb-color-border-strong);
}

.reset-pw-btn {
  background: var(--pb-color-warning-soft, #fff7ed);
  color: var(--pb-color-warning, #f59e0b);
  border-color: var(--pb-color-warning-muted, #fde68a);
}

.reset-pw-btn:hover {
  background: var(--pb-color-warning, #f59e0b);
  color: white;
  border-color: var(--pb-color-warning, #f59e0b);
}

.delete-btn {
  background: var(--pb-color-danger-soft);
  color: var(--pb-color-danger);
}

.delete-btn:hover {
  background: var(--pb-color-danger);
  color: var(--pb-color-surface);
  border-color: var(--pb-color-danger);
}

.reset-info {
  padding: 1rem 0;
  color: var(--pb-color-text);
  border-bottom: 1px solid var(--pb-color-border);
  margin-bottom: 1rem;
}

.reset-confirm-btn {
  padding: 10px 20px;
  border-radius: var(--pb-radius-sm);
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
  background: var(--pb-color-warning, #f59e0b);
  border: 1px solid var(--pb-color-warning, #f59e0b);
  color: white;
}

.reset-confirm-btn:hover:not(:disabled) {
  background: #d97706;
  border-color: #d97706;
}

.reset-confirm-btn:disabled {
  background: var(--pb-color-border);
  border-color: var(--pb-color-border);
  cursor: not-allowed;
}

/* ── 모달 ── */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(30, 31, 29, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: var(--pb-color-surface);
  border-radius: var(--pb-radius-lg);
  width: 90%;
  max-width: 500px;
  box-shadow: var(--pb-shadow-popover);
  border: 1px solid var(--pb-color-border);
  max-height: 90vh;
  overflow-y: auto;
}

.detail-modal {
  max-width: 600px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--pb-color-border);
}

.modal-header h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0;
}

.modal-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface-muted);
  border-radius: var(--pb-radius-sm);
  color: var(--pb-color-text-soft);
  cursor: pointer;
  transition: background 0.12s;
}

.modal-close:hover {
  background: var(--pb-color-surface-subtle);
  color: var(--pb-color-text);
}

/* ── 상세 내용 ── */
.detail-content {
  padding: 20px;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-section h4 {
  font-size: 12px;
  font-weight: 600;
  color: var(--pb-color-text-soft);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin: 0 0 10px;
  padding-bottom: 6px;
  border-bottom: 1px solid var(--pb-color-border);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 10px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 3px;
  padding: 10px 12px;
  background: var(--pb-color-surface-subtle);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-md);
}

.detail-item label {
  font-size: 11px;
  font-weight: 500;
  color: var(--pb-color-text-soft);
}

.detail-item span {
  font-size: 13px;
  color: var(--pb-color-heading);
  font-weight: 500;
}

/* ── 삭제 경고 ── */
.delete-warning {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 20px;
  text-align: center;
}

.warning-icon {
  color: var(--pb-color-danger);
  margin-bottom: 14px;
}

.warning-icon svg {
  width: 48px;
  height: 48px;
}

.warning-content h4 {
  font-size: 14px;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0 0 6px;
}

.warning-content p {
  font-size: 13px;
  color: var(--pb-color-text-muted);
  margin: 0 0 14px;
  line-height: 1.5;
}

.student-info {
  padding: 8px 12px;
  background: var(--pb-color-danger-soft);
  border: 1px solid var(--pb-color-danger);
  border-radius: var(--pb-radius-sm);
  color: var(--pb-color-danger);
  font-size: 12px;
}

/* ── 모달 폼 / 액션 ── */
.modal-form {
  padding: 0 20px 20px;
}

.form-group {
  margin-bottom: 14px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-size: 12px;
  font-weight: 500;
  color: var(--pb-color-text-muted);
}

.form-group input {
  width: 100%;
  height: 34px;
  padding: 0 12px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  box-sizing: border-box;
  transition: border-color 0.15s;
}

.form-group input:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px var(--pb-color-brand-muted);
}

.modal-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  padding: 14px 20px;
  border-top: 1px solid var(--pb-color-border);
}

.cancel-btn,
.delete-confirm-btn {
  height: 32px;
  padding: 0 16px;
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.12s;
  border: none;
}

.cancel-btn {
  background: var(--pb-color-surface-muted);
  border: 1px solid var(--pb-color-border);
  color: var(--pb-color-text);
}

.cancel-btn:hover {
  background: var(--pb-color-border);
}

.delete-confirm-btn {
  background: var(--pb-color-danger);
  color: var(--pb-color-surface);
}

.delete-confirm-btn:hover:not(:disabled) {
  background: var(--pb-color-danger-strong);
}

.delete-confirm-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ── 페이지네이션 ── */
.gl-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-top: 1px solid var(--pb-color-border);
}
.gl-pagination-info { font-size: 13px; color: var(--pb-color-text-muted); }
.gl-pagination-nav { display: flex; align-items: center; gap: 2px; }
.gl-page-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-width: 32px;
  height: 32px;
  padding: 0 8px;
  border: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  border-radius: var(--pb-radius-sm);
  cursor: pointer;
  font-size: 13px;
  transition: background 0.12s, color 0.12s, border-color 0.12s;
  white-space: nowrap;
}
.gl-page-btn:hover:not(:disabled):not(.active) {
  background: var(--pb-color-surface-muted);
  border-color: var(--pb-color-border-strong);
}
.gl-page-btn.active {
  background: var(--pb-color-brand);
  color: #fff;
  border-color: var(--pb-color-brand);
  font-weight: 600;
}
.gl-page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.gl-page-btn.prev-btn, .gl-page-btn.next-btn { padding: 0 10px; }
.gl-page-ellipsis {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  font-size: 13px;
  color: var(--pb-color-text-soft);
}

/* ── 반응형 ── */
@media (max-width: 768px) {
  .stats-grid { grid-template-columns: 1fr; }
  .filter-bar { flex-direction: column; align-items: stretch; }
  .search-box { min-width: unset; }
  .student-table th, .student-table td { padding: 9px 12px; }
  .modal-content { width: 95%; }
  .modal-actions { flex-direction: column; }
  .cancel-btn, .delete-confirm-btn { width: 100%; }
  .detail-grid { grid-template-columns: 1fr; }
  .table-header { flex-direction: column; align-items: flex-start; gap: 8px; }
  .table-actions { width: 100%; justify-content: flex-end; }
}

@media (max-width: 480px) {
  .student-actions { flex-direction: column; gap: 3px; }
  .view-btn, .reset-pw-btn, .delete-btn { width: 28px; height: 28px; }
}
</style>