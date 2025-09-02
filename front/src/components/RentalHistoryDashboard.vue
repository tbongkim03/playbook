<template>
  <div class="rental-history-dashboard">
    <div class="section-header">
      <h2 class="section-title">대출/반납 히스토리</h2>
      <p class="section-description">도서 대출 및 반납 현황을 모니터링할 수 있습니다.</p>
    </div>

    <!-- 통계 카드 -->
    <div class="stats-grid">
      <div class="stat-card rental-card">
        <div class="stat-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M4 19.5C4 18.1193 5.11929 17 6.5 17H20" stroke="currentColor" stroke-width="2"/>
            <path d="M6.5 2H20V22H6.5C5.11929 22 4 20.8807 4 19.5V4.5C4 3.11929 5.11929 2 6.5 2Z" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.totalRentals }}</div>
          <div class="stat-label">총 대여 건수</div>
        </div>
      </div>

      <div class="stat-card active-card">
        <div class="stat-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <polyline points="12,6 12,12 16,14" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.activeRentals }}</div>
          <div class="stat-label">현재 대출 중</div>
        </div>
      </div>

      <div class="stat-card return-card">
        <div class="stat-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <polyline points="20,6 9,17 4,12" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.totalReturns }}</div>
          <div class="stat-label">총 반납 건수</div>
        </div>
      </div>

      <div class="stat-card overdue-card">
        <div class="stat-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2"/>
            <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number text-coral">{{ stats.overdueRentals }}</div>
          <div class="stat-label">연체 도서</div>
        </div>
      </div>
    </div>

    <!-- 필터 및 검색 -->
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-group">
          <label>기간 선택</label>
          <select v-model="filters.period" @change="applyFilters">
            <option value="all">전체 기간</option>
            <option value="today">오늘</option>
            <option value="week">이번 주</option>
            <option value="month">이번 달</option>
            <option value="custom">사용자 지정</option>
          </select>
        </div>

        <div class="filter-group" v-if="filters.period === 'custom'">
          <label>시작일</label>
          <input type="date" v-model="filters.startDate" @change="applyFilters">
        </div>

        <div class="filter-group" v-if="filters.period === 'custom'">
          <label>종료일</label>
          <input type="date" v-model="filters.endDate" @change="applyFilters">
        </div>

        <div class="filter-group">
          <label>상태</label>
          <select v-model="filters.status" @change="applyFilters">
            <option value="all">전체</option>
            <option value="booked">대출 중</option>
            <option value="returned">반납 완료</option>
            <option value="overdue">연체</option>
          </select>
        </div>

        <div class="search-group">
          <label>검색</label>
          <div class="search-input-wrapper">
            <input 
              type="text" 
              v-model="searchQuery" 
              @input="applyFilters"
              placeholder="도서명, 사용자명 검색..."
            >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
              <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
        </div>
      </div>
    </div>

    <!-- 히스토리 테이블 -->
    <div class="history-table-container">
      <div class="table-header">
        <h3>대출/반납 히스토리</h3>
        <div class="table-actions">
          <button class="export-btn" @click="exportData">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" stroke-width="2"/>
              <polyline points="7,10 12,15 17,10" stroke="currentColor" stroke-width="2"/>
              <line x1="12" y1="15" x2="12" y2="3" stroke="currentColor" stroke-width="2"/>
            </svg>
            내보내기
          </button>
          <button class="refresh-btn" @click="fetchRentalHistory">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <polyline points="23,4 23,10 17,10" stroke="currentColor" stroke-width="2"/>
              <polyline points="1,20 1,14 7,14" stroke="currentColor" stroke-width="2"/>
              <path d="M20.49 9C19.9828 7.56678 19.1209 6.28825 17.9845 5.27675C16.8482 4.26525 15.4745 3.55993 13.9917 3.22426C12.5089 2.88859 10.9652 2.93462 9.50481 3.35875C8.04437 3.78288 6.71475 4.57146 5.64 5.64L1 10M23 14L18.36 18.36C17.2853 19.4285 15.9556 20.2171 14.4952 20.6413C13.0348 21.0654 11.4911 21.1114 10.0083 20.7757C8.52547 20.44 7.1518 19.7347 6.01547 18.7233C4.87913 17.7118 4.01717 16.4332 3.51 15" stroke="currentColor" stroke-width="2"/>
            </svg>
            새로고침
          </button>
        </div>
      </div>
      
      <div class="table-wrapper">
        <table class="history-table" v-if="!isLoading">
          <thead>
            <tr>
              <th>도서명</th>
              <th>저자</th>
              <th>사용자</th>
              <th>과정</th>
              <th>대출일</th>
              <th>반납예정일</th>
              <th>반납일</th>
              <th>상태</th>
              <th>작업</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="rental in filteredRentals" :key="rental.id" class="history-row">
              <td class="book-title">{{ rental.bookTitle }}</td>
              <td class="book-author">{{ rental.bookAuthor }}</td>
              <td class="user-name">{{ rental.userName }}</td>
              <td class="course-name">{{ rental.courseDisplay }}</td>
              <td class="rental-date">{{ formatDate(rental.rentalDate) }}</td>
              <td class="due-date">{{ formatDate(rental.dueDate) }}</td>
              <td class="return-date">{{ rental.returnDate ? formatDate(rental.returnDate) : '-' }}</td>
              <td class="status">
                <span class="status-badge" :class="getStatusClass(rental)">
                  {{ getStatusText(rental) }}
                </span>
              </td>
              <td class="actions">
                <button 
                  v-if="rental.status === 'rented'" 
                  class="return-btn" 
                  @click="processReturn(rental)"
                >
                  반납 처리
                </button>
                <button class="detail-btn" @click="showRentalDetail(rental)">
                  상세보기
                </button>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- 로딩 상태 -->
        <div v-if="isLoading" class="loading-container">
          <div class="loading-spinner"></div>
          <p>데이터를 불러오는 중...</p>
        </div>

        <!-- 데이터 없음 -->
        <div v-if="!isLoading && filteredRentals.length === 0" class="empty-state">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M4 19.5C4 18.1193 5.11929 17 6.5 17H20" stroke="currentColor" stroke-width="2"/>
            <path d="M6.5 2H20V22H6.5C5.11929 22 4 20.8807 4 19.5V4.5C4 3.11929 5.11929 2 6.5 2Z" stroke="currentColor" stroke-width="2"/>
          </svg>
          <h3>데이터가 없습니다</h3>
          <p>조건에 맞는 대출/반납 기록이 없습니다.</p>
        </div>
      </div>

      <!-- 페이지네이션 -->
      <div class="pagination" v-if="totalPages > 1">
        <button 
          class="page-btn" 
          :disabled="currentPage === 1" 
          @click="changePage(currentPage - 1)"
        >
          이전
        </button>
        
        <button 
          v-for="page in visiblePages" 
          :key="page"
          class="page-btn" 
          :class="{ active: page === currentPage }"
          @click="changePage(page)"
        >
          {{ page }}
        </button>
        
        <button 
          class="page-btn" 
          :disabled="currentPage === totalPages" 
          @click="changePage(currentPage + 1)"
        >
          다음
        </button>
      </div>
    </div>

    <!-- 상세보기 모달 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-content detail-modal" @click.stop>
        <div class="modal-header">
          <h3>대출 상세 정보</h3>
          <button class="modal-close" @click="closeDetailModal">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2"/>
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2"/>
            </svg>
          </button>
        </div>
        <div class="detail-content" v-if="selectedRental">
          <div class="detail-section">
            <h4>도서 정보</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <label>제목</label>
                <span>{{ selectedRental.bookTitle }}</span>
              </div>
              <div class="detail-item">
                <label>저자</label>
                <span>{{ selectedRental.bookAuthor }}</span>
              </div>
              <div class="detail-item">
                <label>ISBN</label>
                <span>{{ selectedRental.bookIsbn || '-' }}</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <h4>사용자 정보</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <label>이름</label>
                <span>{{ selectedRental.userName }}</span>
              </div>
              <div class="detail-item">
                <label>사용자 ID</label>
                <span>{{ selectedRental.userId }}</span>
              </div>
              <div class="detail-item">
                <label>과정명</label>
                <span>{{ selectedRental.courseName || '-' }}</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <h4>대출 정보</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <label>대출일</label>
                <span>{{ selectedRental.rentalDate }}</span>
              </div>
              <div class="detail-item">
                <label>반납예정일</label>
                <span>{{ selectedRental.dueDate }}</span>
              </div>
              <div class="detail-item">
                <label>반납일</label>
                <span>{{ selectedRental.returnDate ? selectedRental.returnDate : '미반납' }}</span>
              </div>
              <div class="detail-item">
                <label>상태</label>
                <span class="status-badge" :class="getStatusClass(selectedRental)">
                  {{ getStatusText(selectedRental) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

// 반응형 데이터
const rentalHistory = ref([])
const isLoading = ref(false)
const showDetailModal = ref(false)
const selectedRental = ref(null)

// 통계 데이터
const stats = ref({
  totalRentals: 0,    // totalBorrowed
  activeRentals: 0,   // currentlyBorrowed  
  totalReturns: 0,    // totalReturned
  overdueRentals: 0   // overdueCount
})

// 필터 및 검색
const filters = ref({
  period: 'all',
  startDate: '',
  endDate: '',
  status: 'all'
})

const searchQuery = ref('')
const currentPage = ref(1)
const itemsPerPage = 10

// API 헤더 설정
const getAuthHeaders = () => {
  const token = localStorage.getItem('jwtToken')

  return {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
}

// 과정명을 첫 단어와 마지막 단어만 표시하는 함수
const formatCourseName = (courseName) => {
  if (!courseName) return ''
  
  const words = courseName.trim().split(/\s+/)
  if (words.length === 0) return ''
  if (words.length === 1) return words[0]
  
  return `${words[0]} ${words[words.length - 1]}`
}

// 계산된 속성
const filteredRentals = computed(() => {
  let filtered = rentalHistory.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(rental => 
      rental.bookTitle.toLowerCase().includes(query) ||
      (rental.bookAuthor && rental.bookAuthor.toLowerCase().includes(query)) ||
      rental.userName.toLowerCase().includes(query)
    )
  }

  if (filters.value.status !== 'all') {
    filtered = filtered.filter(rental => {
      if (filters.value.status === 'rented') {
        return rental.status === 'rented'
      } else if (filters.value.status === 'overdue') {
        return rental.status === 'overdue'
      } else {
        return rental.status === filters.value.status
      }
    })
  }

  // 기간 필터
  if (filters.value.period !== 'all') {
    const now = new Date()
    let startDate, endDate

    switch (filters.value.period) {
      case 'today':
        startDate = new Date(now.getFullYear(), now.getMonth(), now.getDate())
        endDate = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1)
        break
      case 'week':
        const dayOfWeek = now.getDay()
        startDate = new Date(now.getFullYear(), now.getMonth(), now.getDate() - dayOfWeek)
        endDate = new Date(now.getFullYear(), now.getMonth(), now.getDate() + (7 - dayOfWeek))
        break
      case 'month':
        startDate = new Date(now.getFullYear(), now.getMonth(), 1)
        endDate = new Date(now.getFullYear(), now.getMonth() + 1, 1)
        break
      case 'custom':
        if (filters.value.startDate && filters.value.endDate) {
          startDate = new Date(filters.value.startDate)
          endDate = new Date(filters.value.endDate)
          endDate.setDate(endDate.getDate() + 1) // 종료일 포함
        }
        break
    }

    if (startDate && endDate) {
      filtered = filtered.filter(rental => {
        const rentalDate = new Date(rental.rentalDate)
        return rentalDate >= startDate && rentalDate < endDate
      })
    }
  }

  // 페이지네이션 적용
  const startIndex = (currentPage.value - 1) * itemsPerPage
  return filtered.slice(startIndex, startIndex + itemsPerPage)
})

const totalPages = computed(() => {
  return Math.ceil(rentalHistory.value.length / itemsPerPage)
})

const visiblePages = computed(() => {
  const pages = []
  const maxVisible = 5
  let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2))
  let end = Math.min(totalPages.value, start + maxVisible - 1)

  if (end - start + 1 < maxVisible && start > 1) {
    start = Math.max(1, end - maxVisible + 1)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})

// 메서드
const fetchRentalHistory = async () => {
  try {
    isLoading.value = true
    
    const headers = getAuthHeaders()
    if (!headers) return
    
    const response = await axios.get('http://localhost:8080/history/book', {
      headers: headers
    })
    
    console.log('API 응답 성공:', response.data)
    console.log('응답 데이터 타입:', typeof response.data)
    console.log('응답 데이터 키들:', Object.keys(response.data || {}))
    
    // 응답 데이터 구조 확인 및 처리 (HistoryBookResponseDto 기준)
    const responseData = response.data
    
    // 1. 통계 데이터 처리 (RentalSummaryDto)
    if (responseData && responseData.summary) {
      console.log('통계 데이터:', responseData.summary)
      stats.value = {
        totalRentals: responseData.summary.totalBorrowed || 0,
        activeRentals: responseData.summary.currentlyBorrowed || 0,
        totalReturns: responseData.summary.totalReturned || 0,
        overdueRentals: responseData.summary.overdueCount || 0
      }
      console.log('설정된 통계:', stats.value)
    } else {
      console.warn('통계 데이터가 없습니다')
      stats.value = {
        totalRentals: 0,
        activeRentals: 0,
        totalReturns: 0,
        overdueRentals: 0
      }
    }
    
    // 2. 히스토리 데이터 처리 (List<RentalHistoryDto>)
    let historyList = []
    
    if (responseData && responseData.history && Array.isArray(responseData.history)) {
      historyList = responseData.history
      console.log('히스토리 데이터 발견:', historyList.length, '개')
    } else {
      console.warn('히스토리 데이터가 없거나 잘못된 형식입니다')
      console.log('응답 데이터 구조:', responseData)
    }
    
    console.log('히스토리 리스트:', historyList)
    console.log('히스토리 리스트 길이:', historyList.length)
    
    if (historyList.length > 0) {
      console.log('첫 번째 히스토리 아이템:', historyList[0])
    }
    
    // 3. 데이터 변환 및 설정 (RentalHistoryDto 기준)
    rentalHistory.value = historyList.map((item, index) => {
      console.log(`히스토리 아이템 ${index}:`, item)
      
      const mappedItem = {
        id: index + 1, // ID 생성
        bookTitle: item.bookTitle || '제목 없음',
        bookAuthor: item.bookAuthor || '저자 정보 없음',
        bookIsbn: item.bookIsbn || '',
        userName: item.userName || '사용자 정보 없음',
        userId: item.userId || '사용자 정보 없음',
        courseName: item.courseName || null, // 원본 과정명 저장
        courseDisplay: formatCourseName(item.courseName), // 표시용 과정명
        rentalDate: item.borrowDate, // LocalDate 형태
        returnDate: item.returnDate || null, // LocalDate 형태 또는 null
        dueDate: null, // 계산해서 설정
        status: item.status || 'unknown' // 백엔드에서 제공하는 상태값 사용
      }
      
      // 반납예정일 계산 (대여일 + 7일)
      if (mappedItem.rentalDate) {
        mappedItem.dueDate = calculateDueDate(mappedItem.rentalDate)
      }
      
      return mappedItem
    })
    
  } catch (error) {
    console.error('API 요청 실패:', error)
    console.error('에러 상세:', {
      message: error.message,
      response: error.response?.data,
      status: error.response?.status
    })
    
    if (error.response?.status === 403) {
      alert('관리자만 접근할 수 있습니다.')
      window.history.back()
      return
    }
    
    const errorMessage = error.response?.data?.message || 
                        error.response?.data || 
                        error.message || 
                        '대출 히스토리를 불러오는데 실패했습니다.'
    alert(errorMessage)
    
  } finally {
    isLoading.value = false
  }
}

const calculateDueDate = (borrowDate) => {
  if (!borrowDate) return null
  const date = new Date(borrowDate)
  date.setDate(date.getDate() + 7)
  return date.toISOString().split('T')[0]
}

const getStatusClass = (rental) => {
  switch (rental.status) {
    case 'returned':
      return 'status-returned'
    case 'overdue':
      return 'status-overdue'
    case 'booked':
      return 'status-rented'
    default:
      return 'status-returned'
  }
}

const getStatusText = (rental) => {
  switch (rental.status) {
    case 'returned':
      return '반납완료'
    case 'overdue':
      return '연체'
    case 'booked':
      return '대출중'
    default:
      return '반납완료'
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR')
}

const showRentalDetail = (rental) => {
  selectedRental.value = rental
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedRental.value = null
}

const applyFilters = () => {
  currentPage.value = 1 // 필터 적용 시 첫 페이지로 이동
}

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

const exportData = () => {
  const csvContent = [
    ['도서명', '저자', '사용자', '과정', '대출일', '반납예정일', '반납일', '상태'].join(','),
    ...rentalHistory.value.map(rental => [
      rental.bookTitle,
      rental.bookAuthor,
      rental.userName,
      rental.courseDisplay,
      formatDate(rental.rentalDate),
      formatDate(rental.dueDate),
      rental.returnDate ? formatDate(rental.returnDate) : '',
      getStatusText(rental)
    ].join(','))
  ].join('\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `rental_history_${new Date().toISOString().split('T')[0]}.csv`
  link.click()
}

const processReturn = (rental) => {
  // 반납 처리 로직 (필요시 구현)
  console.log('반납 처리:', rental)
}

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  fetchRentalHistory()
})
</script>

<style scoped>
.rental-history-dashboard {
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
  border-radius: 20px;
  color: white;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.rental-card {
  background: linear-gradient(135deg, #a8dadc 0%, #b8e6c1 100%);
  color: #2d3748;
}

.active-card {
  background: linear-gradient(135deg, #ffd89b 0%, #ffe4b5 100%);
  color: #2d3748;
}

.return-card {
  background: linear-gradient(135deg, #b8e6c1 0%, #d4f1d4 100%);
  color: #2d3748;
}

.overdue-card {
  background: linear-gradient(135deg, #fdb5b5 0%, #fdc7c7 100%);
  color: #2d3748;
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

.text-coral {
  color: #e85a4f !important;
}

.filter-section {
  background: white;
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.03);
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  align-items: end;
}

.filter-group,
.search-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-group label,
.search-group label {
  font-weight: 500;
  color: #4a5568;
  font-size: 0.9rem;
}

.filter-group select,
.filter-group input,
.search-input-wrapper input {
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  background: #fafafa;
}

.filter-group select:focus,
.filter-group input:focus,
.search-input-wrapper input:focus {
  outline: none;
  border-color: #a8dadc;
  box-shadow: 0 0 0 3px rgba(168, 218, 220, 0.15);
  background: white;
}

.search-input-wrapper {
  position: relative;
}

.search-input-wrapper svg {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #a0aec0;
}

.history-table-container {
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

.table-actions {
  display: flex;
  gap: 12px;
}

.export-btn,
.refresh-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  border: none;
  border-radius: 12px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.export-btn {
  background: linear-gradient(135deg, #b8e6c1 0%, #d4f1d4 100%);
  color: #2d3748;
  box-shadow: 0 4px 16px rgba(184, 230, 193, 0.3);
}

.export-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(184, 230, 193, 0.4);
}

.refresh-btn {
  background: linear-gradient(135deg, #e2e8f0 0%, #f1f5f9 100%);
  color: #4a5568;
  box-shadow: 0 4px 16px rgba(226, 232, 240, 0.3);
}

.refresh-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(226, 232, 240, 0.4);
}

.table-wrapper {
  overflow-x: auto;
  min-height: 400px;
}

.history-table {
  width: 100%;
  border-collapse: collapse;
}

.history-table th {
  text-align: left;
  padding: 18px 24px;
  background: #fafafa;
  color: #2d3748;
  font-weight: 600;
  font-size: 0.9rem;
  border-bottom: 1px solid #e2e8f0;
  white-space: nowrap;
}

.history-table td {
  padding: 18px 24px;
  border-bottom: 1px solid #f7fafc;
  color: #4a5568;
  font-size: 0.9rem;
}

.history-row:hover {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.book-title {
  font-weight: 500;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #2d3748;
}

.course-name {
  font-size: 0.8rem;
  color: #6b7280;
  font-weight: 500;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-badge {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-rented {
  background: linear-gradient(135deg, #a8dadc 0%, #b8e6c1 100%);
  color: #2d3748;
}

.status-returned {
  background: linear-gradient(135deg, #b8e6c1 0%, #d4f1d4 100%);
  color: #2d3748;
}

.status-overdue {
  background: linear-gradient(135deg, #fdb5b5 0%, #fdc7c7 100%);
  color: #2d3748;
}

.actions {
  display: flex;
  gap: 8px;
}

.return-btn,
.detail-btn {
  padding: 8px 14px;
  border: none;
  border-radius: 10px;
  font-size: 0.8rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.return-btn {
  background: linear-gradient(135deg, #b8e6c1 0%, #d4f1d4 100%);
  color: #2d3748;
  box-shadow: 0 2px 8px rgba(184, 230, 193, 0.3);
}

.return-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(184, 230, 193, 0.4);
}

.detail-btn {
  background: linear-gradient(135deg, #e2e8f0 0%, #f1f5f9 100%);
  color: #4a5568;
  box-shadow: 0 2px 8px rgba(226, 232, 240, 0.3);
}

.detail-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(226, 232, 240, 0.4);
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #718096;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f1f5f9;
  border-left-color: #a8dadc;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #718096;
  text-align: center;
}

.empty-state svg {
  margin-bottom: 20px;
  opacity: 0.4;
  color: #a0aec0;
}

.empty-state h3 {
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 8px;
  color: #2d3748;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  padding: 24px;
  border-top: 1px solid #f1f5f9;
  background: #fafafa;
}

.page-btn {
  padding: 10px 14px;
  border: 1px solid #e2e8f0;
  background: white;
  color: #4a5568;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.page-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.page-btn.active {
  background: linear-gradient(135deg, #a8dadc 0%, #b8e6c1 100%);
  color: #2d3748;
  border-color: #a8dadc;
  box-shadow: 0 4px 12px rgba(168, 218, 220, 0.3);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
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
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.detail-modal {
  max-width: 800px;
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

.detail-section h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #f1f5f9;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 16px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 12px;
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

.detail-actions {
  display: flex;
  justify-content: center;
  padding-top: 20px;
  border-top: 1px solid #f1f5f9;
  margin-top: 20px;
}

.return-confirm-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #b8e6c1 0%, #d4f1d4 100%);
  color: #2d3748;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(184, 230, 193, 0.3);
}

.return-confirm-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(184, 230, 193, 0.4);
}

/* 반응형 디자인 */
@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .filter-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .table-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .table-actions {
    justify-content: center;
  }
  
  .history-table th,
  .history-table td {
    padding: 14px 16px;
    font-size: 0.8rem;
  }
  
  .actions {
    flex-direction: column;
    gap: 6px;
  }
  
  .return-btn,
  .detail-btn {
    font-size: 0.7rem;
    padding: 6px 10px;
  }
  
  .pagination {
    flex-wrap: wrap;
    gap: 6px;
  }
  
  .page-btn {
    padding: 8px 12px;
    font-size: 0.8rem;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .history-table {
    font-size: 0.7rem;
  }
  
  .book-title {
    max-width: 120px;
  }
  
  .course-name {
    max-width: 100px;
  }
  
  .modal-content {
    width: 95%;
    margin: 10px;
    border-radius: 16px;
  }
  
  .modal-header,
  .detail-content {
    padding: 16px;
  }
  
  .detail-item {
    padding: 12px;
  }
}
</style>