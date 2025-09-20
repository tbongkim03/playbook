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
            <tr v-for="user in filteredUserList" :key="user.idUser" class="student-row">
              <td class="student-name">{{ user.nameUser }}</td>
              <td class="student-id">{{ user.idUser }}</td>
              <td class="student-status">
                <span :class="['status-badge', getStatusClass(user.statusUser)]">
                  {{ user.statusUser }}
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
                <span :class="['status-badge', getStatusClass(selectedUser.statusUser)]">
                  {{ selectedUser.statusUser }}
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
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 9V13" stroke="currentColor" stroke-width="2"/>
              <path d="M12 17.02L12.01 16.991" stroke="currentColor" stroke-width="2"/>
              <path d="M10.29 3.86L1.82 18C1.64466 18.3024 1.55685 18.6453 1.56455 18.9928C1.57225 19.3403 1.67516 19.6792 1.86244 19.9757C2.04973 20.2723 2.31561 20.5157 2.6289 20.6812C2.9422 20.8467 3.29427 20.9286 3.65 20.92H20.35C20.7057 20.9286 21.0578 20.8467 21.3711 20.6812C21.6844 20.5157 21.9503 20.2723 22.1376 19.9757C22.3248 19.6792 22.4278 19.3403 22.4355 18.9928C22.4432 18.6453 22.3553 18.3024 22.18 18L13.71 3.86C13.5317 3.56611 13.2807 3.32312 12.9812 3.15446C12.6817 2.98581 12.3438 2.89725 12 2.89725C11.6562 2.89725 11.3183 2.98581 11.0188 3.15446C10.7193 3.32312 10.4683 3.56611 10.29 3.86Z" stroke="currentColor" stroke-width="2"/>
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
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import axios from 'axios'

// 반응형 데이터
const userList = ref([])
const filteredUserList = ref([])
const isLoading = ref(false)
const searchQuery = ref('')
const selectedStatus = ref('')

// 모달 상태
const showDetailModal = ref(false)
const showDeleteModal = ref(false)

// 선택된 사용자 정보
const selectedUser = ref({})
const deletingUser = ref({})
const deletePassword = ref('')

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
}

// API 헤더 설정
const getAuthHeaders = () => {
  const token = localStorage.getItem('jwtToken')
  return {
    Authorization: `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
}

// 현재 사용자 정보 조회
const fetchCurrentUser = async () => {
  try {
    const response = await axios.get('http://localhost:8080/admin/me', {
      headers: getAuthHeaders()
    })
    currentUser.value = response.data
  } catch (error) {
    console.error('현재 사용자 정보 조회 실패:', error)
  }
}

// 학생 목록 조회
const fetchUserList = async () => {
  try {
    isLoading.value = true
    const response = await axios.get('http://localhost:8080/users/list', {
      headers: getAuthHeaders()
    })

    userList.value = response.data.map(userArray => ({
      nameUser: userArray[0],
      idUser: userArray[1], 
      statusUser: formatStatus(userArray[2]),
      createdAt: userArray[3],
      courseName: userArray[4],
      courseStartDt: userArray[5],
      courseEndDt: userArray[6]
    }))
    
    filteredUserList.value = [...userList.value]
    
  } catch (error) {
    if (error.response?.status === 403) {
      alert('관리자 권한이 필요합니다.')
    } else if (error.response?.status === 401) {
      alert('로그인이 필요합니다.')
    } else {
      alert('학생 목록을 불러오는데 실패했습니다.')
    }
  } finally {
    isLoading.value = false
  }
}

// 상태 매핑
const statusMap = {
  'available': '정상',
  'stop': '정지',
  'overdue': '연체'
}

const formatStatus = (status) => {
  return statusMap[status] || status
}

// 상태별 CSS 클래스
const getStatusClass = (status) => {
  const statusClasses = {
    '정상': 'status-active',
    '정지': 'status-stopped',
    '연체': 'status-overdue'
  }
  return statusClasses[status] || 'status-default'
}

// 통계 계산
const getActiveStudents = () => {
  return userList.value.filter(user => user.statusUser === '정상').length
}

const getOverdueStudents = () => {
  return userList.value.filter(user => user.statusUser === '연체').length
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
    const selectedStatusKorean = formatStatus(selectedStatus.value)
    filtered = filtered.filter(user => user.statusUser === selectedStatusKorean)
  }
  
  filteredUserList.value = filtered
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
    const response = await axios.delete('http://localhost:8080/users', {
      headers: getAuthHeaders(),
      data: {
        idUser: deletingUser.value.idUser
      }
    })
    
    alert('학생 계정이 성공적으로 삭제되었습니다.')
    closeDeleteModal()
    await fetchUserList()
    
  } catch (error) {
    if (error.response?.status === 403) {
      alert('관리자 권한이 필요합니다.')
    } else if (error.response?.status === 401) {
      alert(error.response?.data)
    } else {
      alert(error.response?.data || '학생 삭제에 실패했습니다.')
    }
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

// 날짜 포맷팅
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR')
}

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
  await fetchCurrentUser()
  await fetchUserList()
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.student-account-management {
  max-width: 100%;
}

.section-header {
  margin-bottom: 32px;
}

.section-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 8px;
}

.section-description {
  font-size: 1rem;
  color: #718096;
  margin: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: linear-gradient(135deg, #bae6fd 0%, #ddd6fe 100%);
  border-radius: 20px;
  color: #2d3748;
  box-shadow: 0 8px 32px rgba(186, 230, 253, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 16px;
}

.stat-number {
  font-size: 2rem;
  font-weight: 700;
  line-height: 1;
}

.stat-label {
  font-size: 0.9rem;
  opacity: 0.9;
}

.filter-bar {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.search-box {
  position: relative;
  flex: 1;
  min-width: 300px;
}

.search-box svg {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
}

.search-box input {
  width: 100%;
  padding: 14px 18px 14px 48px;
  border: 2px solid #e2e8f0;
  border-radius: 16px;
  font-size: 1rem;
  transition: all 0.3s ease;
  box-sizing: border-box;
  background: #fafafa;
}

.search-box input:focus {
  outline: none;
  border-color: #a8dadc;
  box-shadow: 0 0 0 3px rgba(168, 218, 220, 0.15);
  background: white;
}

.filter-group {
  display: flex;
  gap: 12px;
  align-items: center;
}

.status-filter {
  padding: 14px 18px;
  border: 2px solid #e2e8f0;
  border-radius: 16px;
  font-size: 1rem;
  background: #fafafa;
  color: #2d3748;
  cursor: pointer;
  transition: all 0.3s ease;
}

.status-filter:focus {
  outline: none;
  border-color: #a8dadc;
  box-shadow: 0 0 0 3px rgba(168, 218, 220, 0.15);
  background: white;
}

.student-table-container {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.03);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid #f1f5f9;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.table-header h3 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
}

.table-info {
  font-size: 0.9rem;
  color: #718096;
}

.table-wrapper {
  overflow-x: auto;
}

.student-table {
  width: 100%;
  border-collapse: collapse;
}

.student-table th {
  text-align: left;
  padding: 18px 24px;
  background: #fafafa;
  color: #2d3748;
  font-weight: 600;
  font-size: 0.9rem;
  border-bottom: 1px solid #e2e8f0;
  white-space: nowrap;
}

.student-table td {
  padding: 18px 24px;
  border-bottom: 1px solid #f7fafc;
  color: #4a5568;
  vertical-align: top;
}

.student-row:hover {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.student-name {
  font-weight: 500;
  color: #2d3748;
}

.status-badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  text-align: center;
  min-width: 60px;
}

.status-active {
  background: linear-gradient(135deg, #c6f6d5 0%, #9ae6b4 100%);
  color: #22543d;
}

.status-stopped {
  background: linear-gradient(135deg, #fed7d7 0%, #feb2b2 100%);
  color: #742a2a;
}

.status-overdue {
  background: linear-gradient(135deg, #feebc8 0%, #fbd38d 100%);
  color: #7c2d12;
}

.status-default {
  background: linear-gradient(135deg, #e2e8f0 0%, #cbd5e0 100%);
  color: #4a5568;
}

.course-period {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.period-dates {
  font-size: 0.9rem;
  color: #4a5568;
}

.period-status {
  font-size: 0.8rem;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 12px;
  text-align: center;
  width: fit-content;
}

.period-active {
  background: linear-gradient(135deg, #c6f6d5 0%, #9ae6b4 100%);
  color: #22543d;
}

.period-ending {
  background: linear-gradient(135deg, #feebc8 0%, #fbd38d 100%);
  color: #7c2d12;
}

.period-completed {
  background: linear-gradient(135deg, #e2e8f0 0%, #cbd5e0 100%);
  color: #4a5568;
}

.student-actions {
  display: flex;
  gap: 8px;
}

.view-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #bae6fd 0%, #ddd6fe 100%);
  color: #2d3748;
  box-shadow: 0 2px 8px rgba(186, 230, 253, 0.3);
}

.view-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(186, 230, 253, 0.4);
}

.delete-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #feb2b2 0%, #fbb6ce 100%);
  color: #2d3748;
  box-shadow: 0 2px 8px rgba(254, 178, 178, 0.3);
}

.delete-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(254, 178, 178, 0.4);
}

/* 모달 스타일 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(8px);
}

.modal-content {
  background: white;
  border-radius: 20px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
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
  padding: 24px 24px 0 24px;
  margin-bottom: 24px;
}

.modal-header h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
}

.modal-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: linear-gradient(135deg, #e2e8f0 0%, #f1f5f9 100%);
  border-radius: 12px;
  color: #4a5568;
  cursor: pointer;
  transition: all 0.3s ease;
}

.modal-close:hover {
  background: linear-gradient(135deg, #cbd5e0 0%, #e2e8f0 100%);
  transform: translateY(-1px);
}

.detail-content {
  padding: 0 24px 24px 24px;
}

.detail-section {
  margin-bottom: 32px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-section h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2d3748;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #f1f5f9;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item label {
  font-size: 0.9rem;
  font-weight: 500;
  color: #718096;
}

.detail-item span {
  font-size: 1rem;
  color: #2d3748;
  font-weight: 500;
}

.delete-warning {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 24px 24px 24px;
  text-align: center;
}

.warning-icon {
  color: #f56565;
  margin-bottom: 16px;
}

.warning-content h4 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #2d3748;
  margin: 0 0 8px 0;
}

.warning-content p {
  color: #718096;
  margin: 0 0 16px 0;
  line-height: 1.5;
}

.student-info {
  padding: 12px 16px;
  background: linear-gradient(135deg, #fed7d7 0%, #feb2b2 100%);
  border-radius: 12px;
  color: #2d3748;
  font-size: 0.9rem;
}

.modal-form {
  padding: 0 24px 24px 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #2d3748;
}

.form-group input {
  width: 100%;
  padding: 14px 18px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s ease;
  box-sizing: border-box;
  background: #fafafa;
}

.form-group input:focus {
  outline: none;
  border-color: #a8dadc;
  box-shadow: 0 0 0 3px rgba(168, 218, 220, 0.15);
  background: white;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
}

.cancel-btn,
.delete-confirm-btn {
  padding: 12px 20px;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn {
  background: linear-gradient(135deg, #e2e8f0 0%, #f1f5f9 100%);
  color: #4a5568;
  box-shadow: 0 4px 16px rgba(226, 232, 240, 0.3);
}

.cancel-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(226, 232, 240, 0.4);
}

.delete-confirm-btn {
  background: linear-gradient(135deg, #feb2b2 0%, #fbb6ce 100%);
  color: #2d3748;
  box-shadow: 0 4px 16px rgba(254, 178, 178, 0.3);
}

.delete-confirm-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(254, 178, 178, 0.4);
}

.delete-confirm-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-box {
    min-width: unset;
  }
  
  .student-table th,
  .student-table td {
    padding: 14px 16px;
    font-size: 0.9rem;
  }
  
  .modal-content {
    width: 95%;
    margin: 20px;
  }
  
  .modal-header,
  .detail-content,
  .modal-form {
    padding: 16px;
  }
  
  .modal-actions {
    flex-direction: column;
  }
  
  .cancel-btn,
  .delete-confirm-btn {
    width: 100%;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .course-period {
    align-items: flex-start;
  }

  .period-status {
    width: auto;
  }
}

@media (max-width: 480px) {
  .student-table {
    font-size: 0.8rem;
  }
  
  .student-actions {
    flex-direction: column;
    gap: 6px;
  }
  
  .view-btn,
  .delete-btn {
    width: 32px;
    height: 32px;
  }
  
  .modal-content {
    border-radius: 16px;
  }
  
  .form-group input {
    padding: 12px 16px;
  }

  .status-badge {
    font-size: 0.7rem;
    padding: 4px 8px;
    min-width: 50px;
  }

  .period-dates {
    font-size: 0.8rem;
  }

  .period-status {
    font-size: 0.7rem;
  }
}
</style>