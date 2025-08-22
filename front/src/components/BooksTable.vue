<!-- BooksTable.vue -->
<template>
  <div class="book-management-container">
    <!-- 바코드 및 프린트 모달 -->
    <Barcode 
      :seqBook="selectedSeqBook"
      :seqSortSecond="selectedSeqSortSecond"
      :cntBook="selectedCntBook"
      :barcodeBook="selectedBarcode" 
      :titleBook="selectedBookTitle" 
      :isOpen="isOpen" 
      @close="isOpen = false"
    />

    <PrintBatch 
      v-if="isPrintBatchOpen" 
      :books="booksToPrint" 
      @close="isPrintBatchOpen = false"
    />

    <!-- 헤더 영역 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M4 19.5C4 18.1193 5.11929 17 6.5 17H20" stroke="currentColor" stroke-width="2"/>
              <path d="M6.5 2H20V22H6.5C5.11929 22 4 20.8807 4 19.5V4.5C4 3.11929 5.11929 2 6.5 2Z" stroke="currentColor" stroke-width="2"/>
            </svg>
            도서 관리
          </h1>
          <p class="page-subtitle">도서 등록, 수정, 삭제 및 바코드 관리</p>
        </div>
        
        <div class="header-actions">
          <button type="button" class="register-btn" @click="$emit('open-register-modal')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <line x1="12" y1="8" x2="12" y2="16" stroke="currentColor" stroke-width="2"/>
              <line x1="8" y1="12" x2="16" y2="12" stroke="currentColor" stroke-width="2"/>
            </svg>
            도서 등록
          </button>
        </div>
      </div>
    </div>

    <!-- 필터 및 검색 영역 개선 -->
    <div class="filter-section">
      <div class="filter-card">
        <div class="filter-content">
          <!-- 첫 번째 줄: 검색 및 필터 -->
          <div class="filter-row primary-filters">
            <div class="filter-group search-group">
              <label class="filter-label">검색</label>
              <div class="search-input-wrapper">
                <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
                  <path d="m21 21-4.35-4.35" stroke="currentColor" stroke-width="2"/>
                </svg>
                <input 
                  type="text" 
                  v-model="filters.searchQuery"
                  placeholder="제목, 저자, 출판사, ISBN으로 검색..."
                  class="search-input"
                />
                <button 
                  v-if="filters.searchQuery"
                  @click="filters.searchQuery = ''"
                  class="clear-search-btn"
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2"/>
                    <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </button>
              </div>
            </div>

            <div class="filter-group">
              <label class="filter-label">대분류</label>
              <select v-model="filters.categoryLarge" class="filter-select">
                <option value="">전체</option>
                <option
                  v-for="category in largeCategories"
                  :key="category.seqSortFirst"
                  :value="category.seqSortFirst"
                >
                  {{ category.korSortFirst }}
                </option>
              </select>
            </div>

            <div class="filter-group">
              <label class="filter-label">중분류</label>
              <select 
                v-model="filters.categoryMedium" 
                class="filter-select"
                :disabled="!filters.categoryLarge"
              >
                <option value="">전체</option>
                <option
                  v-for="category in availableMediumCategories"
                  :key="category.seqSortSecond"
                  :value="category.seqSortSecond"
                >
                  {{ category.korSortSecond }}
                </option>
              </select>
            </div>

            <div class="filter-group">
              <label class="filter-label">정렬</label>
              <select v-model="filters.sortBy" class="filter-select">
                <option value="title_asc">제목 가나다순</option>
                <option value="title_desc">제목 역순</option>
                <option value="author_asc">저자 가나다순</option>
                <option value="author_desc">저자 역순</option>
                <option value="publisher_asc">출판사 가나다순</option>
                <option value="publisher_desc">출판사 역순</option>
                <option value="date_desc">출판일 최신순</option>
                <option value="date_asc">출판일 오래된순</option>
              </select>
            </div>
          </div>
          
          <!-- 두 번째 줄: 액션 버튼들 -->
          <div class="filter-row action-controls">
            <div class="control-group">
              <button @click="resetFilters" class="reset-filters-btn">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12Z" stroke="currentColor" stroke-width="2"/>
                  <path d="M12 3V7M12 17V21M21 12H17M7 12H3" stroke="currentColor" stroke-width="2"/>
                </svg>
                초기화
              </button>
            </div>
            
            <div class="print-controls">
              <div class="print-toggle">
                <label class="toggle-label">
                  <input 
                    type="checkbox" 
                    v-model="isPrint" 
                    class="toggle-input"
                  />
                  <span class="toggle-slider"></span>
                  <span class="toggle-text">프린트 모드</span>
                </label>
              </div>
              
              <button 
                v-if="isPrint" 
                @click="printBarcodes" 
                class="batch-print-btn"
                :disabled="booksToPrint.length === 0"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <polyline points="6,9 6,2 18,2 18,9" stroke="currentColor" stroke-width="2"/>
                  <path d="M6,18H4C3.46957,18 2.96086,17.7893 2.58579,17.4142C2.21071,17.0391 2,16.5304 2,16V11C2,10.4696 2.21071,9.96086 2.58579,9.58579C2.96086,9.21071 3.46957,9 4,9H20C20.5304,9 21.0391,9.21071 21.4142,9.58579C21.7893,9.96086 22,10.4696 22,11V16C22,16.5304 21.7893,17.0391 21.4142,17.4142C21.0391,17.7893 20.5304,18 20,18H18" stroke="currentColor" stroke-width="2"/>
                  <rect x="6" y="14" width="12" height="8" stroke="currentColor" stroke-width="2"/>
                </svg>
                일괄 출력
                <span class="count-badge">{{ booksToPrint.length }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 통계 정보 -->
    <div class="stats-section">
      <div class="stat-card total-books">
        <div class="stat-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M4 19.5C4 18.1193 5.11929 17 6.5 17H20" stroke="currentColor" stroke-width="2"/>
            <path d="M6.5 2H20V22H6.5C5.11929 22 4 20.8807 4 19.5V4.5C4 3.11929 5.11929 2 6.5 2Z" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ filteredBooks.length }}</div>
          <div class="stat-label">표시된 도서</div>
        </div>
      </div>
      
      <div class="stat-card total-books">
        <div class="stat-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2" stroke="currentColor" stroke-width="2"/>
            <line x1="16" y1="2" x2="16" y2="6" stroke="currentColor" stroke-width="2"/>
            <line x1="8" y1="2" x2="8" y2="6" stroke="currentColor" stroke-width="2"/>
            <line x1="3" y1="10" x2="21" y2="10" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ allBooks.length }}</div>
          <div class="stat-label">전체 도서</div>
        </div>
      </div>

      <div v-if="isPrint" class="stat-card print-ready">
        <div class="stat-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <polyline points="6,9 6,2 18,2 18,9" stroke="currentColor" stroke-width="2"/>
            <path d="M6,18H4C3.46957,18 2.96086,17.7893 2.58579,17.4142C2.21071,17.0391 2,16.5304 2,16V11C2,10.4696 2.21071,9.96086 2.58579,9.58579C2.96086,9.21071 3.46957,9 4,9H20C20.5304,9 21.0391,9.21071 21.4142,9.58579C21.7893,9.96086 22,10.4696 22,11V16C22,16.5304 21.7893,17.0391 21.4142,17.4142C21.0391,17.7893 20.5304,18 20,18H18" stroke="currentColor" stroke-width="2"/>
            <rect x="6" y="14" width="12" height="8" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ booksToPrint.length }}</div>
          <div class="stat-label">출력 대기</div>
        </div>
      </div>
    </div>

    <!-- 도서 테이블 -->
    <div class="table-section">
      <div class="table-card">
        <div class="table-header">
          <h3>도서 목록</h3>
          <div class="table-actions">
            <button 
              @click="refreshBooks" 
              class="refresh-btn"
              :disabled="isRefreshing"
              title="목록 새로고침"
            >
              <svg 
                width="16" 
                height="16" 
                viewBox="0 0 24 24" 
                fill="none" 
                xmlns="http://www.w3.org/2000/svg"
                :class="{ 'spinning': isRefreshing }"
              >
                <path d="M3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12Z" stroke="currentColor" stroke-width="2"/>
                <path d="M12 3V7M12 17V21M21 12H17M7 12H3" stroke="currentColor" stroke-width="2"/>
              </svg>
              {{ isRefreshing ? '새로고침 중...' : '새로고침' }}
            </button>
            <span class="result-count">{{ paginatedBooks.length }}개 표시 (페이지 {{ currentPage }}/{{ totalPages }})</span>
          </div>
        </div>
        
        <div class="table-wrapper">
          <table class="books-table">
            <thead>
              <tr>
                <th class="col-title">제목</th>
                <th class="col-isbn">ISBN</th>
                <th class="col-author">저자</th>
                <th class="col-publisher">출판사</th>
                <th class="col-date">출판일</th>
                <th class="col-category">대분류</th>
                <th class="col-category">중분류</th>
                <th class="col-count">수량</th>
                <th class="col-barcode">바코드</th>
                <th class="col-actions">작업</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="book in paginatedBooks" :key="book.seqBook" class="book-row">
                <td class="book-title col-title">
                  <div class="title-content">
                    <span class="title-text" :title="book.titleBook">{{ book.titleBook }}</span>
                  </div>
                </td>
                <td class="isbn col-isbn" :title="book.isbnBook">{{ book.isbnBook }}</td>
                <td class="author col-author" :title="book.authorBook">{{ book.authorBook }}</td>
                <td class="publisher col-publisher" :title="book.publisherBook">{{ book.publisherBook }}</td>
                <td class="publish-date col-date">{{ formatDate(book.publishDateBook) }}</td>
                <td class="category-large col-category">
                  <select class="category-select" v-model="book.categoryLarge">
                    <option
                      v-for="category in largeCategories"
                      :key="category.nameSortFirst"
                      :value="category.nameSortFirst"
                    >
                      {{ category.korSortFirst }}
                    </option>
                  </select>
                </td>
                <td class="category-medium col-category">
                  <select
                    class="category-select"
                    v-model="book.categoryMedium"
                    :disabled="!book.mediumOptions.length"
                  >
                    <option
                      v-for="(category, index) in book.mediumOptions"
                      :key="index"
                      :value="category.seqSortSecond"
                    >
                      {{ category.korSortSecond }}
                    </option>
                  </select>
                </td>
                <td class="book-count col-count">
                  <input 
                    type="number" 
                    class="count-input" 
                    v-model="book.cntBook" 
                    min="1" 
                    @input="() => { if (book.cntBook < 1) book.cntBook = 1 }" 
                  />
                </td>
                <td class="barcode col-barcode">
                  <div class="barcode-display">
                    <input 
                      type="text" 
                      class="barcode-input" 
                      v-model="book.barcodeBook" 
                      readonly
                      :title="book.barcodeBook"
                    />
                  </div>
                </td>
                <td class="actions col-actions">
                  <div class="action-buttons">
                    <button @click="barcodeCreate(book)" class="action-btn barcode-btn" title="바코드 생성">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="3" y="4" width="4" height="16" stroke="currentColor" stroke-width="2"/>
                        <rect x="9" y="4" width="2" height="16" stroke="currentColor" stroke-width="2"/>
                        <rect x="13" y="4" width="2" height="16" stroke="currentColor" stroke-width="2"/>
                        <rect x="17" y="4" width="4" height="16" stroke="currentColor" stroke-width="2"/>
                      </svg>
                    </button>
                    <button @click="deleteBook(book)" class="action-btn delete-btn" title="도서 삭제">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <polyline points="3,6 5,6 21,6" stroke="currentColor" stroke-width="2"/>
                        <path d="M19,6V20C19,20.5304 18.7893,21.0391 18.4142,21.4142C18.0391,21.7893 17.5304,22 17,22H7C6.46957,22 5.96086,21.7893 5.58579,21.4142C5.21071,21.0391 5,20.5304 5,20V6M8,6V4C8,3.46957 8.21071,2.96086 8.58579,2.58579C8.96086,2.21071 9.46957,2 10,2H14C14.5304,2 15.0391,2.21071 15.4142,2.58579C15.7893,2.96086 16,3.46957 16,4V6" stroke="currentColor" stroke-width="2"/>
                      </svg>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>

          <!-- 빈 상태 -->
          <div v-if="paginatedBooks.length === 0" class="empty-state">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M4 19.5C4 18.1193 5.11929 17 6.5 17H20" stroke="currentColor" stroke-width="2"/>
              <path d="M6.5 2H20V22H6.5C5.11929 22 4 20.8807 4 19.5V4.5C4 3.11929 5.11929 2 6.5 2Z" stroke="currentColor" stroke-width="2"/>
            </svg>
            <h3>도서가 없습니다</h3>
            <p>조건에 맞는 도서가 없습니다. 필터를 초기화하거나 새로운 도서를 등록해보세요.</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 페이지네이션 -->
    <div class="pagination-section" v-if="totalPages > 1">
      <div class="pagination">
        <button
          @click="currentPage = Math.max(1, currentPage - 1)"
          :disabled="currentPage === 1"
          class="page-btn prev-btn"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <polyline points="15,18 9,12 15,6" stroke="currentColor" stroke-width="2"/>
          </svg>
        </button>
        
        <button
          v-for="page in visiblePages"
          :key="page"
          @click="currentPage = page"
          :class="[
            'page-btn',
            { 'active': page === currentPage }
          ]"
        >
          {{ page }}
        </button>
        
        <button
          @click="currentPage = Math.min(totalPages, currentPage + 1)"
          :disabled="currentPage === totalPages"
          class="page-btn next-btn"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <polyline points="9,18 15,12 9,6" stroke="currentColor" stroke-width="2"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watchEffect } from 'vue'
import Barcode from './Barcode.vue'
import PrintBatch from './BookPrintBatch.vue'

// emit 정의
defineEmits(['open-register-modal'])

const API_BASE = 'http://localhost:8080'

// 데이터 상태
const allBooks = ref([])
const largeCategories = ref([])
const mediumCategoriesAll = ref([])
const isOpen = ref(false)
const selectedBarcode = ref('')
const selectedBookTitle = ref('')
const selectedSeqBook = ref('')
const selectedSeqSortSecond = ref('')
const selectedCntBook = ref('')
const isPrint = ref(false)
const isPrintBatchOpen = ref(false)
const isRefreshing = ref(false)

// 필터 상태
const filters = ref({
  searchQuery: '',
  categoryLarge: '',
  categoryMedium: '',
  sortBy: 'title_asc'
})

// 페이지네이션 상태
const currentPage = ref(1)
const pageSize = 15 // 줄여서 한 화면에 더 잘 들어가도록

// 키보드 이벤트 핸들러
const handleKeydown = (event) => {
  if (event.key === 'Escape' && isOpen.value) {
    isOpen.value = false
  }

  if (event.key === 'Escape' && isPrintBatchOpen.value) {
    isPrintBatchOpen.value = false
  }
}

// 대분류 데이터 가져오기
const fetchLargeCategories = async () => {
  const res = await fetch('http://localhost:8080/subjects')
  largeCategories.value = await res.json()
  console.log('[fetchLargeCategories]', largeCategories.value)
}

// 중분류 데이터 가져오기
const fetchMediumCategories = async () => {
  const res = await fetch('http://localhost:8080/subtitles')
  mediumCategoriesAll.value = await res.json()
  console.log('[fetchMediumCategories]', mediumCategoriesAll.value)
}

// seqSortSecond(중분류 시퀀스)로 중분류 정보 찾기
const findMediumCategory = (seqSecond) =>
  mediumCategoriesAll.value.find(m => m.seqSortSecond === seqSecond)

// seqSortSecond(중분류 시퀀스)로 대분류 nameSortFirst 찾기
const findLargeCodeFromSeqSecond = (seqSecond) => {
  const medium = findMediumCategory(seqSecond)
  if (!medium) return ''
  
  const large = largeCategories.value.find(l => l.seqSortFirst === medium.seqSortFirst)
  return large?.nameSortFirst || ''
}

// 특정 대분류 seqSortFirst에 해당하는 중분류 옵션들
const getMediumOptions = (largeSeq) => {
  if (!largeSeq) return []
  return mediumCategoriesAll.value.filter(m => m.seqSortFirst === largeSeq)
}

// 선택된 대분류에 따른 중분류 옵션
const availableMediumCategories = computed(() => {
  if (!filters.value.categoryLarge) return []
  return getMediumOptions(filters.value.categoryLarge)
})

// 모든 도서 데이터 가져오기 (페이지네이션 없이)
const fetchBooks = async () => {
  const token = localStorage.getItem('jwtToken')
  const url = `${API_BASE}/books/all`

  const res = await fetch(url, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  })

  if (!res.ok) { 
    const errorText = await res.text()
    alert(`데이터 로드 오류: ${errorText}`)
    console.error('API Error:', res.status, errorText)
    return
  }

  const data = await res.json()

  if (!Array.isArray(data)) {
    console.error('서버 응답 데이터 오류: ', data)
    allBooks.value = []
    return
  }

  allBooks.value = data.map(book => {
    const largeCode = findLargeCodeFromSeqSecond(book.seqSortSecond)
    const mediumOptions = getMediumOptions(largeCode)
    return {
      ...book,
      categoryLarge: largeCode,
      categoryMedium: book.seqSortSecond ?? '',
      mediumOptions
    }
  })
}

// 한글 문자열 비교를 위한 함수
const compareKorean = (a, b) => {
  return a.localeCompare(b, 'ko-KR')
}

// 필터링된 도서 목록
const filteredBooks = computed(() => {
  let result = [...allBooks.value]

  // 검색 필터
  if (filters.value.searchQuery.trim()) {
    const query = filters.value.searchQuery.trim().toLowerCase()
    result = result.filter(book => 
      book.titleBook?.toLowerCase().includes(query) ||
      book.authorBook?.toLowerCase().includes(query) ||
      book.publisherBook?.toLowerCase().includes(query) ||
      book.isbnBook?.toLowerCase().includes(query)
    )
  }

  // 대분류 필터
  if (filters.value.categoryLarge) {
    result = result.filter(book => {
      const large = largeCategories.value.find(l => l.seqSortFirst === filters.value.categoryLarge)
      if (!large) return false
      return book.categoryLarge === large.nameSortFirst
    })
  }

  // 중분류 필터
  if (filters.value.categoryMedium) {
    result = result.filter(book => book.categoryMedium === filters.value.categoryMedium)
  }

  // 프린트 모드 필터
  if (isPrint.value) {
    result = result.filter(book => book.printCheckBook === false)
  }

  // 정렬
  result.sort((a, b) => {
    switch (filters.value.sortBy) {
      case 'title_asc':
        return compareKorean(a.titleBook || '', b.titleBook || '')
      case 'title_desc':
        return compareKorean(b.titleBook || '', a.titleBook || '')
      case 'author_asc':
        return compareKorean(a.authorBook || '', b.authorBook || '')
      case 'author_desc':
        return compareKorean(b.authorBook || '', a.authorBook || '')
      case 'publisher_asc':
        return compareKorean(a.publisherBook || '', b.publisherBook || '')
      case 'publisher_desc':
        return compareKorean(b.publisherBook || '', a.publisherBook || '')
      case 'date_desc':
        return new Date(b.publishDateBook || 0) - new Date(a.publishDateBook || 0)
      case 'date_asc':
        return new Date(a.publishDateBook || 0) - new Date(b.publishDateBook || 0)
      default:
        return 0
    }
  })

  return result
})

// 페이지네이션 계산
const totalPages = computed(() => Math.ceil(filteredBooks.value.length / pageSize))

const paginatedBooks = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredBooks.value.slice(start, end)
})

// 페이지네이션 표시 페이지 번호들
const visiblePages = computed(() => {
  const total = totalPages.value
  const current = currentPage.value
  const delta = 2

  let start = Math.max(1, current - delta)
  let end = Math.min(total, current + delta)

  if (end - start < 4) {
    if (start === 1) {
      end = Math.min(total, start + 4)
    } else {
      start = Math.max(1, end - 4)
    }
  }

  const pages = []
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

// 프린트할 도서 목록
const booksToPrint = computed(() => {
  return filteredBooks.value.filter(book =>
    book.printCheckBook === false &&
    book.categoryLarge !== 0 &&
    book.categoryMedium !== 0 &&
    book.barcodeBook &&
    book.barcodeBook.trim() !== ''
  )
})

// 필터 초기화
const resetFilters = () => {
  filters.value = {
    searchQuery: '',
    categoryLarge: '',
    categoryMedium: '',
    sortBy: 'title_asc'
  }
  currentPage.value = 1
}

// 대분류 변경 시 중분류 초기화
watchEffect(() => {
  if (!filters.value.categoryLarge) {
    filters.value.categoryMedium = ''
  } else {
    const availableOptions = availableMediumCategories.value
    if (filters.value.categoryMedium && !availableOptions.find(opt => opt.seqSortSecond === filters.value.categoryMedium)) {
      filters.value.categoryMedium = ''
    }
  }
})

// 필터 변경 시 첫 페이지로 이동
watchEffect(() => {
  currentPage.value = 1
}, { flush: 'sync' })

// 각 book의 categoryLarge가 바뀔 때 개별 감시
watchEffect(() => {
  allBooks.value.forEach(book => {
    const largeCode = book.categoryLarge
    const oldOptions = book.mediumOptions?.map(m => m.seqSortSecond) || []

    // 대분류가 없으면 중분류 비움
    if (!largeCode) {
      if (book.categoryMedium !== '') {
        book.categoryMedium = ''
      }
      book.mediumOptions = []
    } else {
      // 대분류에 맞는 중분류 옵션 - nameSortFirst로 찾기
      const large = largeCategories.value.find(l => l.nameSortFirst === largeCode)
      if (!large) {
        book.mediumOptions = []
        book.categoryMedium = ''
        return
      }
      
      const newOptions = mediumCategoriesAll.value.filter(m => m.seqSortFirst === large.seqSortFirst)
      const newOptionsIds = newOptions.map(m => m.seqSortSecond)

      // 옵션이 변경되었을 때만 업데이트
      if (JSON.stringify(oldOptions) !== JSON.stringify(newOptionsIds)) {
        book.mediumOptions = newOptions

        // 현재 선택된 중분류가 새 옵션에 없으면 초기화
        if (!newOptionsIds.includes(book.categoryMedium)) {
          book.categoryMedium = newOptions.length > 0 ? newOptions[0].seqSortSecond : ''
        }
      }
    }

    // 바코드 자동 생성 기능
    const large = largeCategories.value.find(l => l.nameSortFirst === book.categoryLarge)
    const medium = mediumCategoriesAll.value.find(m => m.seqSortSecond === book.categoryMedium)

    if (large && medium) {
      const isbn = book.isbnBook
      const cnt = book.cntBook

      if (isbn && cnt && book.categoryLarge && book.categoryMedium !== '') {
        const barcode = `${large.nameSortFirst}${medium.nameSortSecond}-${isbn}-${cnt}`
        if (book.barcodeBook !== barcode) {
          book.barcodeBook = barcode
        }
      }
    }
  })
})

// 마운트 시 데이터 로드
onMounted(async () => {
  await fetchLargeCategories()
  await fetchMediumCategories()
  await fetchBooks()
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
})

// 도서 삭제
async function deleteBook(book) {
  if (!confirm(`"${book.titleBook}" 도서를 삭제하시겠습니까?`)) {
    return
  }

  try {
    const token = localStorage.getItem('jwtToken')
    const response = await fetch(`${API_BASE}/books/${book.seqBook}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`
      }
    })

    if (!response.ok) {
      const errorMessage = await response.text()
      throw new Error(errorMessage || `서버 오류: ${response.status}`)
    }

    allBooks.value = allBooks.value.filter(b => b.seqBook !== book.seqBook)
    alert('삭제에 성공하였습니다.')
  } catch (error) {
    alert(`삭제 실패: ${error.message}`)
  }
}

// 바코드 생성
function barcodeCreate(book) {
  selectedSeqBook.value = book.seqBook
  selectedSeqSortSecond.value = book.categoryMedium
  selectedCntBook.value = book.cntBook
  selectedBarcode.value = book.barcodeBook
  selectedBookTitle.value = book.titleBook
  isOpen.value = true
}

// 일괄 프린트
function printBarcodes() {
  isPrintBatchOpen.value = true
}

// 날짜 포맷팅
function formatDate(dateString) {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR')
}

// 도서 목록 새로고침
const refreshBooks = async () => {
  isRefreshing.value = true
  try {
    await fetchBooks()
  } catch (error) {
    console.error('새로고침 실패:', error)
    alert('목록을 새로고침하는 중 오류가 발생했습니다.')
  } finally {
    isRefreshing.value = false
  }
}
</script>

<style scoped>
.book-management-container {
  min-height: 100vh;
  padding: 20px 0;
}

.page-header {
  margin-bottom: 2rem;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section {
  flex: 1;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 2rem;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 0.5rem;
}

.page-subtitle {
  color: #718096;
  margin: 0;
  font-size: 1rem;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.register-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: linear-gradient(135deg, #b8e6c1 0%, #d4f1d4 100%);
  color: #2d3748;
  border: none;
  border-radius: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  box-shadow: 0 6px 20px rgba(184, 230, 193, 0.3);
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(184, 230, 193, 0.4);
}

/* 필터 섹션 개선 */
.filter-section {
  margin: 0 0 1.5rem 0;
}

.filter-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.03);
}

.filter-content {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.filter-row {
  display: flex;
  align-items: flex-end;
  gap: 1rem;
  flex-wrap: wrap;
}

.primary-filters {
  flex: 1;
}

.action-controls {
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f1f5f9;
  padding-top: 1.5rem;
  margin-top: 0;
}

.control-group {
  display: flex;
  gap: 1rem;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  min-width: 140px;
}

.search-group {
  min-width: 280px;
}

.filter-label {
  font-size: 0.9rem;
  font-weight: 500;
  color: #4a5568;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px;
  color: #a0aec0;
  z-index: 1;
}

.search-input {
  width: 100%;
  padding: 12px 12px 12px 40px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 0.9rem;
  background: #fafafa;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #a8dadc;
  box-shadow: 0 0 0 3px rgba(168, 218, 220, 0.15);
  background: white;
}

.clear-search-btn {
  position: absolute;
  right: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  color: #a0aec0;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.clear-search-btn:hover {
  background: #f7fafc;
  color: #4a5568;
}

.filter-select {
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 0.9rem;
  background: #fafafa;
  transition: all 0.3s ease;
  min-width: 120px;
}

.filter-select:focus {
  outline: none;
  border-color: #a8dadc;
  box-shadow: 0 0 0 3px rgba(168, 218, 220, 0.15);
  background: white;
}

.filter-select:disabled {
  background: #f1f5f9;
  opacity: 0.6;
}

.reset-filters-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f56565 0%, #fc8181 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(245, 101, 101, 0.3);
}

.reset-filters-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(245, 101, 101, 0.4);
}

.print-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.print-toggle {
  display: flex;
  align-items: center;
}

.toggle-label {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
  user-select: none;
}

.toggle-input {
  display: none;
}

.toggle-slider {
  position: relative;
  width: 48px;
  height: 26px;
  background: #e2e8f0;
  border-radius: 26px;
  transition: background 0.3s ease;
}

.toggle-slider::before {
  content: '';
  position: absolute;
  top: 2px;
  left: 2px;
  width: 22px;
  height: 22px;
  background: white;
  border-radius: 50%;
  transition: transform 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.toggle-input:checked + .toggle-slider {
  background: linear-gradient(135deg, #a8dadc 0%, #b8e6c1 100%);
}

.toggle-input:checked + .toggle-slider::before {
  transform: translateX(22px);
}

.toggle-text {
  font-weight: 500;
  color: #2d3748;
  white-space: nowrap;
}

.batch-print-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  background: linear-gradient(135deg, #ddbff0 0%, #e6ccf7 100%);
  color: #2d3748;
  border: none;
  border-radius: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(221, 191, 240, 0.3);
  white-space: nowrap;
}

.batch-print-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(221, 191, 240, 0.4);
}

.batch-print-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.count-badge {
  background: rgba(255, 255, 255, 0.3);
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 600;
}

.stats-section {
  margin: 0 0 1.5rem 0;
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 20px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.03);
  min-width: 180px;
  flex: 1;
}

.stat-card.total-books .stat-icon {
  background: linear-gradient(135deg, #a8dadc 0%, #b8e6c1 100%);
  color: #2d3748;
}

.stat-card.print-ready .stat-icon {
  background: linear-gradient(135deg, #ddbff0 0%, #e6ccf7 100%);
  color: #2d3748;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.stat-number {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2d3748;
  line-height: 1;
}

.stat-label {
  font-size: 0.9rem;
  color: #718096;
}

.table-section {
  margin: 0;
}

.table-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.03);
  overflow: hidden;
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
  font-size: 1.25rem;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #e2e8f0 0%, #f1f5f9 100%);
  color: #4a5568;
  border: none;
  border-radius: 12px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(226, 232, 240, 0.3);
}

.refresh-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(226, 232, 240, 0.4);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.refresh-btn .spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.result-count {
  color: #718096;
  font-size: 0.9rem;
  white-space: nowrap;
}

.table-wrapper {
  overflow-x: auto;
}

/* 테이블 컬럼 너비 최적화 */
.books-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  min-width: 1200px;
}

.books-table th,
.books-table td {
  padding: 10px 6px;
  border-bottom: 1px solid #f7fafc;
  font-size: 0.8rem;
  vertical-align: middle;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.books-table th {
  background: #fafafa;
  color: #2d3748;
  font-weight: 600;
  text-align: left;
  position: sticky;
  top: 0;
  z-index: 10;
  font-size: 0.75rem;
}

/* 컬럼별 너비 설정 - 더 컴팩트하게 */
.col-title { width: 220px; }
.col-isbn { width: 110px; }
.col-author { width: 100px; }
.col-publisher { width: 100px; }
.col-date { width: 85px; }
.col-category { width: 90px; }
.col-count { width: 50px; }
.col-barcode { width: 160px; }
.col-actions { width: 80px; }

.book-row:hover {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.book-title .title-text {
  font-weight: 500;
  color: #2d3748;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
}

.category-select {
  width: 100%;
  padding: 4px 6px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 0.7rem;
  background: #fafafa;
  transition: all 0.3s ease;
}

.category-select:focus {
  outline: none;
  border-color: #a8dadc;
  box-shadow: 0 0 0 2px rgba(168, 218, 220, 0.15);
  background: white;
}

.category-select:disabled {
  background: #f1f5f9;
  opacity: 0.6;
}

.count-input {
  width: 100%;
  padding: 4px 6px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 0.7rem;
  text-align: center;
  background: #fafafa;
  transition: all 0.3s ease;
}

.count-input:focus {
  outline: none;
  border-color: #a8dadc;
  box-shadow: 0 0 0 2px rgba(168, 218, 220, 0.15);
  background: white;
}

.barcode-input {
  width: 100%;
  padding: 4px 6px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 0.65rem;
  background: #f8fafc;
  font-family: 'Courier New', monospace;
  color: #4a5568;
  overflow: hidden;
  text-overflow: ellipsis;
}

.action-buttons {
  display: flex;
  gap: 2px;
  justify-content: center;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.barcode-btn {
  background: linear-gradient(135deg, #a8dadc 0%, #b8e6c1 100%);
  color: #2d3748;
  box-shadow: 0 1px 4px rgba(168, 218, 220, 0.3);
}

.barcode-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(168, 218, 220, 0.4);
}

.delete-btn {
  background: linear-gradient(135deg, #fdb5b5 0%, #fdc7c7 100%);
  color: #2d3748;
  box-shadow: 0 1px 4px rgba(253, 181, 181, 0.3);
}

.delete-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(253, 181, 181, 0.4);
}

.action-btn svg {
  width: 12px;
  height: 12px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  color: #718096;
  text-align: center;
}

.empty-state svg {
  margin-bottom: 1.5rem;
  opacity: 0.4;
  color: #a0aec0;
}

.empty-state h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #2d3748;
}

.empty-state p {
  margin: 0;
  opacity: 0.8;
}

.pagination-section {
  margin: 2rem 0 0 0;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.page-btn {
  padding: 10px 14px;
  border: 1px solid #e2e8f0;
  background: white;
  color: #4a5568;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  min-width: 44px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-btn.active {
  background: linear-gradient(135deg, #a8dadc 0%, #b8e6c1 100%);
  color: #2d3748;
  border-color: #a8dadc;
  box-shadow: 0 4px 12px rgba(168, 218, 220, 0.3);
}

.prev-btn,
.next-btn {
  padding: 10px 12px;
}

/* 반응형 디자인 개선 */
@media (max-width: 1400px) {
  .filter-row {
    flex-wrap: wrap;
  }
  
  .primary-filters {
    width: 100%;
    margin-bottom: 1rem;
  }
  
  .action-controls {
    width: 100%;
    padding-top: 1rem;
    border-top: 1px solid #f1f5f9;
  }
}

@media (max-width: 1200px) {
  .header-content {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-group {
    width: 100%;
    min-width: auto;
  }
  
  .search-group {
    min-width: auto;
  }
  
  .action-controls {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .print-controls {
    justify-content: space-between;
  }
  
  .stats-section {
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .book-management-container {
    padding: 20px 0;
  }
  
  .page-title {
    font-size: 1.5rem;
  }
  
  .filter-content {
    padding: 16px;
  }
  
  .table-header {
    padding: 16px;
  }
  
  .books-table th,
  .books-table td {
    padding: 8px 4px;
    font-size: 0.75rem;
  }
  
  .books-table th {
    font-size: 0.7rem;
  }
  
  .books-table {
    min-width: 1000px;
  }
  
  .action-btn {
    width: 24px;
    height: 24px;
  }
  
  .action-btn svg {
    width: 10px;
    height: 10px;
  }
  
  /* 컬럼별 너비 재조정 */
  .col-title { width: 180px; }
  .col-isbn { width: 90px; }
  .col-author { width: 80px; }
  .col-publisher { width: 80px; }
  .col-date { width: 70px; }
  .col-category { width: 75px; }
  .col-count { width: 45px; }
  .col-barcode { width: 140px; }
  .col-actions { width: 70px; }
}

@media (max-width: 480px) {
  .toggle-text {
    display: none;
  }
  
  .batch-print-btn {
    padding: 8px 12px;
    font-size: 0.85rem;
  }
  
  .result-count {
    font-size: 0.8rem;
  }
}</style>