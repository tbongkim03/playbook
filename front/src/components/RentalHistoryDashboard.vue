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
          <div class="stat-label">총 대출 건수</div>
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
          <DateRangePicker ref="datePickerRef" @change="onDateRangeChange" />
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
        
        <!-- 캠퍼스 필터 (전체 관리자만 표시) -->
        <div v-if="showCampusFilter" class="filter-group">
          <label>캠퍼스</label>
          <select v-model="selectedCampus" @change="onCampusChange" class="filter-select">
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
              <th>도서 명</th>
              <th>저자</th>
              <th>바코드</th>
              <th>사용자</th>
              <th>과정 명</th>
              <th>대출 일</th>
              <th>반납예정일</th>
              <th>반납 일</th>
              <th>상태</th>
              <th>작업</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="rental in filteredRentals" :key="rental.id" class="history-row">
              <td class="book-title">{{ rental.bookTitle }}</td>
              <td class="book-author">{{ rental.bookAuthor }}</td>
              <td class="barcode">{{ rental.barcodeBook || '-' }}</td>
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
                <button class="detail-btn" @click="showRentalDetail(rental)">
                  상세보기
                </button>
                <button class="delete-btn" @click="deleteHistoryItem(rental.seqHistory)">
                  삭제
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
      <div class="gl-pagination" v-if="totalPages > 1">
        <span class="gl-pagination-info">{{ paginationInfo }}</span>
        <nav class="gl-pagination-nav">
          <button class="gl-page-btn prev-btn" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            이전
          </button>
          <template v-for="item in paginationItems" :key="String(item) + '-rh'">
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
              <div class="detail-item">
                <label>바코드</label>
                <span>{{ selectedRental.barcodeBook || '-' }}</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <h4>사용자 정보</h4>
            <div class="detail-grid user-info-grid">
              <div class="detail-item">
                <label>이름</label>
                <span>{{ selectedRental.userName }}</span>
              </div>
              <div class="detail-item">
                <label>사용자 ID</label>
                <span>{{ selectedRental.userId }}</span>
              </div>
              <div class="detail-item">
                <label>과정 명</label>
                <span>{{ selectedRental.courseName || '-' }}</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <h4>대출 정보</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <label>대출 일</label>
                <span>{{ selectedRental.rentalDate }}</span>
              </div>
              <div class="detail-item">
                <label>반납예정일</label>
                <span>{{ selectedRental.dueDate }}</span>
              </div>
              <div class="detail-item">
                <label>반납 일</label>
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
import { exportToXlsx } from '@/utils/exportSheet'
import * as historyApi from '@/api/history'
import { swAlert, swConfirm } from '@/utils/sweetAlert'
import { handleApiError } from '@/utils/apiErrorHandler'
import { formatDate } from '@/utils/dateFormatter'
import { useAdminCampusFilter } from '@/composables/useAdminCampusFilter'
import DateRangePicker from './DateRangePicker.vue'

// 반응형 데이터
const rentalHistory = ref([])
const isLoading = ref(false)
const showDetailModal = ref(false)
const selectedRental = ref(null)

// 캠퍼스 필터 관련 (캠퍼스 관리자는 자기 캠퍼스 고정, 필터 숨김)
const {
  showCampusFilter,
  currentUserCampusId,
  selectedCampus,
  campuses,
  fetchAdminInfo,
  fetchCampuses,
  getCampusParam,
} = useAdminCampusFilter({ showFilterForCampusAdmin: false })

// 통계 데이터
const stats = ref({
  totalRentals: 0,    // totalBorrowed
  activeRentals: 0,   // currentlyBorrowed  
  totalReturns: 0,    // totalReturned
  overdueRentals: 0   // overdueCount
})

// DateRangePicker 참조 및 날짜 상태
const datePickerRef = ref(null)
const activeDateRange = ref({ period: 'all', startDate: null, endDate: null })

const onDateRangeChange = (range) => {
  activeDateRange.value = range
  currentPage.value = 1
  fetchRentalHistory()
}

// 필터 및 검색
const filters = ref({
  status: 'all'
})

const searchQuery = ref('')
const currentPage = ref(1)
const itemsPerPage = 10


// 과정명을 첫 단어와 마지막 단어만 표시하는 함수
const formatCourseName = (courseName) => {
  if (!courseName) return ''
  
  const words = courseName.trim().split(/\s+/)
  if (words.length === 0) return ''
  if (words.length === 1) return words[0]
  
  return `${words[0]} ${words[words.length - 1]}`
}

// 필터만 적용 (페이지네이션 제외) — 내보내기 용
const applyRentalFilters = (source) => {
  let filtered = source

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
      if (filters.value.status === 'rented') return rental.status === 'rented'
      if (filters.value.status === 'overdue') return rental.status === 'overdue'
      return rental.status === filters.value.status
    })
  }

  return filtered
}

const allFilteredRentals = computed(() => applyRentalFilters(rentalHistory.value))

// 계산된 속성
const filteredRentals = computed(() => {
  const filtered = allFilteredRentals.value
  // 페이지네이션 적용
  const startIndex = (currentPage.value - 1) * itemsPerPage
  return filtered.slice(startIndex, startIndex + itemsPerPage)
})

const totalPages = computed(() => {
  return Math.ceil(rentalHistory.value.length / itemsPerPage)
})

const paginationItems = computed(() => {
  const total = totalPages.value
  const current = currentPage.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)
  const items = [1]
  if (current > 3) items.push('...')
  const start = Math.max(2, current - 1)
  const end = Math.min(total - 1, current + 1)
  for (let i = start; i <= end; i++) items.push(i)
  if (current < total - 2) items.push('...')
  items.push(total)
  return items
})

const paginationInfo = computed(() => {
  const total = rentalHistory.value.length
  const start = (currentPage.value - 1) * itemsPerPage + 1
  const end = Math.min(currentPage.value * itemsPerPage, total)
  return `${start}–${end} / 전체 ${total}건`
})


// 캠퍼스 변경 핸들러
const onCampusChange = () => {
  applyFilters()
}

const fetchRentalHistory = async () => {
  try {
    isLoading.value = true

    const campusId = showCampusFilter.value && selectedCampus.value ? selectedCampus.value : null
    const { startDate, endDate } = activeDateRange.value
    const response = await historyApi.getBooks(campusId, startDate, endDate)
    
    // 응답 데이터 구조 확인 및 처리 (HistoryBookResponseDto 기준)
    const responseData = response.data.data
    
    // 1. 통계 데이터 처리 (RentalSummaryDto)
    if (responseData && responseData.summary) {
      stats.value = {
        totalRentals: responseData.summary.totalBorrowed || 0,
        activeRentals: responseData.summary.currentlyBorrowed || 0,
        totalReturns: responseData.summary.totalReturned || 0,
        overdueRentals: responseData.summary.overdueCount || 0
      }
      // console.log('설정된 통계:', stats.value)
    } else {
      // console.warn('통계 데이터가 없습니다')
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
    }
    // 3. 데이터 변환 및 설정 (RentalHistoryDto 기준)
    rentalHistory.value = historyList.map((item, index) => {      
      const mappedItem = {
        id: index + 1,
        seqHistory: item.seqHistory,
        bookTitle: item.bookTitle || '제목 없음',
        bookAuthor: item.bookAuthor || '저자 정보 없음',
        bookIsbn: item.bookIsbn || '',
        barcodeBook: item.barcodeBook || null, // 바코드 데이터
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
    
  } 
  catch (error) {  
    if (error.response?.status === 403) {
      await swAlert('관리자만 접근할 수 있습니다.', 'warning')
      window.history.back()
      return
    }
    
    const errorMessage = error.response?.data?.msg ||
                        error.message ||
                        '대출 히스토리를 불러오는데 실패했습니다.'
    await swAlert(errorMessage, 'error')

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
      return '대출 중'
    default:
      return '반납완료'
  }
}


const showRentalDetail = (rental) => {
  selectedRental.value = rental
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedRental.value = null
}

const deleteHistoryItem = async (historyId) => {
  const confirmed = await swConfirm('이 대출 기록을 삭제하시겠습니까?', '삭제된 기록은 복구되지 않습니다.')
  if (!confirmed) return
  try {
    await historyApi.deleteHistory(historyId)
    rentalHistory.value = rentalHistory.value.filter(h => h.seqHistory !== historyId)
    await swAlert('대출 기록이 삭제되었습니다.', 'success')
  } catch (error) {
    await handleApiError(error, '대출 기록 삭제에 실패했습니다.')
  }
}

const applyFilters = () => {
  currentPage.value = 1
  fetchRentalHistory()
}

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

const exportData = async () => {
  try {
    const campusId = showCampusFilter.value && selectedCampus.value ? selectedCampus.value : null
    const res = await historyApi.exportExcel(campusId)
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = '대출이력.xlsx'
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('엑셀 내보내기 실패:', e)
  }
}

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
  await fetchCampuses()
  await fetchAdminInfo()
  await fetchRentalHistory()
})
</script>

<style scoped>
/* ── 루트 ── */
.rental-history-dashboard {
  max-width: 100%;
  font-size: 13px;
  color: var(--pb-color-text);
}

/* ── 섹션 헤더 ── */
.section-header { margin-bottom: 20px; }

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
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-lg);
  box-shadow: var(--pb-shadow-xs);
}

.rental-card  { background: var(--pb-color-surface); }
.active-card  { background: var(--pb-color-surface); }
.return-card  { background: var(--pb-color-surface); }
.overdue-card { background: var(--pb-color-surface); }

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--pb-radius-md);
  flex-shrink: 0;
}
.rental-card .stat-icon  { background: var(--pb-color-surface-muted); color: var(--pb-color-text-muted); }
.active-card .stat-icon  { background: var(--pb-color-accent-soft);   color: var(--pb-color-accent); }
.return-card .stat-icon  { background: var(--pb-color-success-soft);  color: var(--pb-color-success); }
.overdue-card .stat-icon { background: var(--pb-color-danger-soft);   color: var(--pb-color-danger); }

.overdue-card .stat-icon { color: var(--pb-color-danger); }
.return-card  .stat-icon { color: var(--pb-color-success); }
.active-card  .stat-icon { color: var(--pb-color-accent); }

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

.text-coral { color: var(--pb-color-danger) !important; }

/* ── 필터 영역 ── */
.filter-section {
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-lg);
  padding: 14px 16px;
  margin-bottom: 16px;
  box-shadow: var(--pb-shadow-xs);
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
  align-items: end;
}

.filter-group,
.search-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.filter-group label,
.search-group label {
  font-size: 11px;
  font-weight: 500;
  color: var(--pb-color-text-soft);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.filter-group select,
.filter-group input,
.search-input-wrapper input {
  height: 34px;
  padding: 0 10px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  transition: border-color 0.15s;
}

.filter-group select:focus,
.filter-group input:focus,
.search-input-wrapper input:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px var(--pb-color-brand-muted);
}

.filter-select { cursor: pointer; }

.search-input-wrapper {
  position: relative;
}

.search-input-wrapper input {
  width: 100%;
  padding-right: 36px;
  box-sizing: border-box;
}

.search-input-wrapper svg {
  position: absolute;
  right: 10px;
  top: 50%;
  translate: 0 -50%;
  color: var(--pb-color-text-soft);
  pointer-events: none;
}

/* ── 히스토리 테이블 컨테이너 ── */
.history-table-container {
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-lg);
  box-shadow: var(--pb-shadow-sm);
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 18px;
  border-bottom: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface-muted);
}

.table-header h3 {
  font-size: 13px;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0;
}

.table-actions { display: flex; gap: 6px; }

.export-btn,
.refresh-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  height: 30px;
  padding: 0 11px;
  border-radius: var(--pb-radius-sm);
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.12s;
  border: none;
}

.export-btn {
  background: var(--pb-color-brand);
  color: var(--pb-color-surface);
}

.export-btn:hover { background: var(--pb-color-brand-strong); }

.refresh-btn {
  background: var(--pb-color-surface-muted);
  border: 1px solid var(--pb-color-border);
  color: var(--pb-color-text);
}

.refresh-btn:hover { background: var(--pb-color-border); }

/* ── 테이블 ── */
.table-wrapper {
  overflow-x: auto;
  min-height: 360px;
}

.history-table {
  width: 100%;
  border-collapse: collapse;
}

.history-table th {
  text-align: left;
  padding: 9px 14px;
  background: var(--pb-color-surface-subtle);
  color: var(--pb-color-text-soft);
  font-weight: 600;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 1px solid var(--pb-color-border);
  white-space: nowrap;
}

.history-table td {
  padding: 9px 14px;
  border-bottom: 1px solid var(--pb-color-border);
  color: var(--pb-color-text);
  font-size: 13px;
  vertical-align: middle;
}

.history-row:hover td { background: var(--pb-color-surface-muted); }

.book-title {
  font-weight: 500;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--pb-color-heading);
}

.book-author {
  max-width: 130px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--pb-color-text-soft);
  font-size: 12px;
}

.barcode {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--pb-color-text-soft);
  font-family: 'Courier New', monospace;
  font-size: 11px;
}

.user-name {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}

.course-name {
  font-size: 12px;
  color: var(--pb-color-text-soft);
  font-weight: 500;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rental-date, .due-date, .return-date {
  white-space: nowrap;
  min-width: 84px;
  font-size: 12px;
}

/* ── 상태 배지 ── */
.status-badge {
  padding: 2px 7px;
  border-radius: var(--pb-radius-xs);
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.status-rented   { background: var(--pb-color-accent-soft);   color: var(--pb-color-accent); }
.status-returned { background: var(--pb-color-success-soft); color: var(--pb-color-success); }
.status-overdue  { background: var(--pb-color-danger-soft);  color: var(--pb-color-danger); }

/* ── 액션 버튼 ── */
.actions { display: flex; gap: 4px; }

.detail-btn {
  height: 26px;
  padding: 0 9px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  font-size: 11px;
  font-weight: 500;
  cursor: pointer;
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text);
  transition: background 0.12s;
}

.detail-btn:hover { background: var(--pb-color-border); }

.delete-btn {
  height: 26px;
  padding: 0 9px;
  margin-left: 4px;
  border: 1px solid var(--pb-color-danger, #e53e3e);
  border-radius: var(--pb-radius-sm);
  font-size: 11px;
  font-weight: 500;
  cursor: pointer;
  background: transparent;
  color: var(--pb-color-danger, #e53e3e);
  transition: background 0.12s, color 0.12s;
}

.delete-btn:hover {
  background: var(--pb-color-danger, #e53e3e);
  color: #fff;
}

/* ── 로딩 / 빈 상태 ── */
.loading-container,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 56px 20px;
  color: var(--pb-color-text-soft);
  text-align: center;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--pb-color-border);
  border-left-color: var(--pb-color-brand);
  border-radius: 50%;
  animation: spin 0.9s linear infinite;
  margin-bottom: 14px;
}

@keyframes spin { to { rotate: 360deg; } }

.empty-state svg {
  margin-bottom: 14px;
  opacity: 0.35;
  color: var(--pb-color-text-soft);
}

.empty-state h3 {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 6px;
  color: var(--pb-color-heading);
}

.empty-state p { font-size: 13px; margin: 0; }

/* ── 페이지네이션 (GitLab Offset style) ── */
.gl-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 4px;
  margin-top: 8px;
}
.gl-pagination-info {
  font-size: 13px;
  color: var(--pb-color-text-muted);
}
.gl-pagination-nav {
  display: flex;
  align-items: center;
  gap: 2px;
}
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
.gl-page-btn.prev-btn,
.gl-page-btn.next-btn { padding: 0 10px; }
.gl-page-ellipsis {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  font-size: 13px;
  color: var(--pb-color-text-soft);
  cursor: default;
  user-select: none;
}

/* ── 모달 ── */
.modal-overlay {
  position: fixed;
  top: 72px;
  left: 0;
  width: 100%;
  height: calc(100% - 72px);
  background: rgba(30, 31, 29, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
}

.modal-content {
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-lg);
  width: 90%;
  max-width: 640px;
  max-height: calc(100vh - 192px);
  overflow-y: auto;
  box-shadow: var(--pb-shadow-popover);
}

.detail-modal { max-width: 820px; }

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
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

.detail-content {
  padding: 20px;
}

.detail-section { margin-bottom: 20px; }
.detail-section:last-child { margin-bottom: 0; }

.detail-section h4 {
  font-size: 11px;
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
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 8px;
}

.user-info-grid { grid-template-columns: repeat(3, 1fr) !important; }

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

.detail-actions {
  display: flex;
  justify-content: center;
  padding-top: 14px;
  border-top: 1px solid var(--pb-color-border);
  margin-top: 14px;
}

.return-confirm-btn {
  height: 34px;
  padding: 0 22px;
  background: var(--pb-color-brand);
  color: var(--pb-color-surface);
  border: none;
  border-radius: var(--pb-radius-sm);
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.12s;
}

.return-confirm-btn:hover { background: var(--pb-color-brand-strong); }

/* ── 반응형 ── */
@media (max-width: 1024px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .filter-row { grid-template-columns: 1fr; }
}

@media (max-width: 768px) {
  .stats-grid { grid-template-columns: 1fr; }
  .table-header { flex-direction: column; gap: 10px; align-items: stretch; }
  .table-actions { justify-content: center; }
  .history-table th, .history-table td { padding: 8px 10px; }
  .actions { flex-direction: column; gap: 3px; }
  .pagination { flex-wrap: wrap; gap: 3px; }
  .detail-grid { grid-template-columns: 1fr; }
  .user-info-grid { grid-template-columns: 1fr 1fr !important; }
}

@media (max-width: 480px) {
  .book-title { max-width: 100px; }
  .barcode { max-width: 70px; }
  .course-name { max-width: 80px; }
  .book-author { max-width: 100px; }
  .user-name { max-width: 80px; }
  .modal-content { width: 95%; }
  .user-info-grid { grid-template-columns: 1fr !important; }
}
</style>