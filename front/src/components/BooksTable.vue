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
          <!-- 라우터 링크 대신 이벤트 emit -->
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

    <!-- 검색 및 필터 영역 -->
    <div class="search-section">
      <div class="search-card">
        <div class="search-content">
          <BookSearch @search="onSearch" />
          
          <div class="print-controls">
            <div class="print-toggle">
              <label class="toggle-label">
                <input 
                  type="checkbox" 
                  v-model="isPrint" 
                  class="toggle-input"
                />
                <span class="toggle-slider"></span>
                <span class="toggle-text">프린트</span>
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
              바코드 일괄 출력 ({{ booksToPrint.length }}개)
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 통계 정보 -->
    <div class="stats-section">
      <div class="stat-card">
        <div class="stat-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M4 19.5C4 18.1193 5.11929 17 6.5 17H20" stroke="currentColor" stroke-width="2"/>
            <path d="M6.5 2H20V22H6.5C5.11929 22 4 20.8807 4 19.5V4.5C4 3.11929 5.11929 2 6.5 2Z" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ totalCount }}</div>
          <div class="stat-label">총 도서</div>
        </div>
      </div>
      
      <div class="stat-card" v-if="isPrint">
        <div class="stat-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <polyline points="6,9 6,2 18,2 18,9" stroke="currentColor" stroke-width="2"/>
            <path d="M6,18H4C3.46957,18 2.96086,17.7893 2.58579,17.4142C2.21071,17.0391 2,16.5304 2,16V11C2,10.4696 2.21071,9.96086 2.58579,9.58579C2.96086,9.21071 3.46957,9 4,9H20C20.5304,9 21.0391,9.21071 21.4142,9.58579C21.7893,9.96086 22,10.4696 22,11V16C22,16.5304 21.7893,17.0391 21.4142,17.4142C21.0391,17.7893 20.5304,18 20,18H18" stroke="currentColor" stroke-width="2"/>
            <rect x="6" y="14" width="12" height="8" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ paginatedBooks.length }}</div>
          <div class="stat-label">미출력 도서</div>
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
            <span class="result-count">{{ totalCount }}개 중 {{ paginatedBooks.length }}개 표시</span>
          </div>
        </div>
        
        <div class="table-wrapper">
          <table class="books-table">
            <thead>
              <tr>
                <th>제목</th>
                <th>ISBN</th>
                <th>저자</th>
                <th>출판사</th>
                <th>출판일</th>
                <th>대분류</th>
                <th>중분류</th>
                <th>수량</th>
                <th>바코드</th>
                <th>작업</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="book in paginatedBooks" :key="book.seqBook" class="book-row">
                <td class="book-title">
                  <div class="title-content">
                    <span class="title-text">{{ book.titleBook }}</span>
                  </div>
                </td>
                <td class="isbn">{{ book.isbnBook }}</td>
                <td class="author">{{ book.authorBook }}</td>
                <td class="publisher">{{ book.publisherBook }}</td>
                <td class="publish-date">{{ formatDate(book.publishDateBook) }}</td>
                <td class="category-large">
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
                <td class="category-medium">
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
                <td class="book-count">
                  <input 
                    type="number" 
                    class="count-input" 
                    v-model="book.cntBook" 
                    min="1" 
                    @input="() => { if (book.cntBook < 1) book.cntBook = 1 }" 
                  />
                </td>
                <td class="barcode">
                  <div class="barcode-display">
                    <input 
                      type="text" 
                      class="barcode-input" 
                      v-model="book.barcodeBook" 
                      readonly
                    />
                  </div>
                </td>
                <td class="actions">
                  <div class="action-buttons">
                    <button @click="barcodeCreate(book)" class="action-btn barcode-btn">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <rect x="3" y="4" width="4" height="16" stroke="currentColor" stroke-width="2"/>
                        <rect x="9" y="4" width="2" height="16" stroke="currentColor" stroke-width="2"/>
                        <rect x="13" y="4" width="2" height="16" stroke="currentColor" stroke-width="2"/>
                        <rect x="17" y="4" width="4" height="16" stroke="currentColor" stroke-width="2"/>
                      </svg>
                    </button>
                    <button @click="deleteBook(book)" class="action-btn delete-btn">
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
            <p>조건에 맞는 도서가 없습니다. 새로운 도서를 등록해보세요.</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 페이지네이션 -->
    <div class="pagination-section" v-if="totalPages > 1">
      <div class="pagination">
        <button
          v-for="page in totalPages"
          :key="page"
          @click="currentPage = page"
          :class="[
            'page-btn',
            { 'active': page === currentPage }
          ]"
        >
          {{ page }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, watchEffect } from 'vue'
import BookSearch from './BookSearch.vue'
import Barcode from './Barcode.vue'
import PrintBatch from './BookPrintBatch.vue'

// emit 정의
defineEmits(['open-register-modal'])

const API_BASE = 'http://localhost:8080'

const books = ref([])
const largeCategories = ref([])
const mediumCategoriesAll = ref([])
const totalCount = ref(0)
const isOpen = ref(false)
const selectedBarcode = ref('')
const selectedBookTitle = ref('')
const selectedSeqBook = ref('')
const selectedSeqSortSecond = ref('')
const selectedCntBook = ref('')
const isPrint = ref(false)
const isPrintBatchOpen = ref(false)
const isRefreshing = ref(false)

const fetchLargeCategories = async () => {
  const res = await fetch('http://localhost:8080/subjects')
  largeCategories.value = await res.json()
  console.log('[fetchLargeCategories]', largeCategories.value)
}

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
  
  // seqSortFirst로 대분류 정보 찾기
  const large = largeCategories.value.find(l => l.seqSortFirst === medium.seqSortFirst)
  return large?.nameSortFirst || ''
}

// 특정 대분류 nameSortFirst에 해당하는 중분류 옵션들
const getMediumOptions = (largeCode) => {
  // 대분류 nameSortFirst로 seqSortFirst 찾기
  const large = largeCategories.value.find(l => l.nameSortFirst === largeCode)
  if (!large) return []
  
  // 해당 seqSortFirst에 속하는 중분류들 반환
  return mediumCategoriesAll.value.filter(m => m.seqSortFirst === large.seqSortFirst)
}

function onSearch({ query, exact }) {
  console.log('검색 요청:', query, exact)
  fetchBooks(1, query, exact)
}

const fetchBooks = async (page = 1, query = '', exact = false) => {
  let url
  if (query && query.trim()) {
    url = new URL(`${API_BASE}/books/search`)
    url.searchParams.set('q', query.trim())
    url.searchParams.set('exact', exact)
  } else {
    url = new URL(`${API_BASE}/books`)
    url.searchParams.set('page', page)
  }

  const res = await fetch(url.toString())
  if (!res.ok) {
    alert('검색 오류: ', res.status)
    return
  }

  const data = await res.json()

  if (!data.content) {
    console.error('서버 응답 데이터 오류: ', data)
    books.value = []
    totalCount.value = 0
    return
  }

  totalCount.value = data.totalCount
  books.value = data.content.map(book => {
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

onMounted(async () => {
  // 대분류와 중분류 데이터를 먼저 로드한 후 책 데이터 처리
  await fetchLargeCategories()
  await fetchMediumCategories()
  await fetchBooks(currentPage.value)
})

// 각 book의 categoryLarge가 바뀔 때 개별 감시
watchEffect(() => {
  books.value.forEach(book => {
    const largeCode = book.categoryLarge
    const oldOptions = book.mediumOptions?.map(m => m.seqSortSecond) || []

    // 대분류가 없으면 중분류 비움
    if (!largeCode) {
      if (book.categoryMedium !== '') {
        book.categoryMedium = ''
      }
      book.mediumOptions = []
    } else {
      // 대분류에 맞는 중분류 옵션
      const newOptions = getMediumOptions(largeCode)
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

const currentPage = ref(1)
const pageSize = 10

const totalPages = computed(() => Math.ceil(totalCount.value / pageSize))

const paginatedBooks = computed(() => {
  if (isPrint.value) {
    return books.value.filter(book => book.printCheckBook === false)
  } else {
    return books.value
  }
})

const booksToPrint = computed(() => {
  return books.value.filter(book =>
    book.printCheckBook === false &&
    book.categoryLarge !== 0 &&
    book.categoryMedium !== 0 &&
    book.barcodeBook &&
    book.barcodeBook.trim() !== ''
  )
})

watch(currentPage, (newPage) => {
  fetchBooks(newPage)
})

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

    books.value = books.value.filter(b => b.seqBook !== book.seqBook)
    totalCount.value--
    alert('삭제에 성공하였습니다.')
  } catch (error) {
    alert(`삭제 실패: ${error.message}`)
  }
}

function barcodeCreate(book) {
  selectedSeqBook.value = book.seqBook
  selectedSeqSortSecond.value = book.categoryMedium
  selectedCntBook.value = book.cntBook
  selectedBarcode.value = book.barcodeBook
  selectedBookTitle.value = book.titleBook
  isOpen.value = true
}

function printBarcodes() {
  isPrintBatchOpen.value = true
}

function formatDate(dateString) {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR')
}

// 도서 목록 새로고침 메서드
const refreshBooks = async () => {
  isRefreshing.value = true
  try {
    await fetchBooks(currentPage.value)
  } catch (error) {
    console.error('새로고침 실패:', error)
    alert('목록을 새로고침하는 중 오류가 발생했습니다.')
  } finally {
    isRefreshing.value = false
  }
}

</script>

<!-- 기존 스타일 유지 -->
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
  color: #212529;
  margin-bottom: 0.5rem;
}

.page-subtitle {
  color: #6c757d;
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
  padding: 0.75rem 1.5rem;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
}

.register-btn:hover {
  background: #0056b3;
  transform: translateY(-1px);
}

.search-section {
  margin: 0 0 1.5rem 0;
}

.search-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
  border: 1px solid #dee2e6;
}

.search-content {
  padding: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 2rem;
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
  width: 44px;
  height: 24px;
  background: #ced4da;
  border-radius: 24px;
  transition: background 0.3s ease;
}

.toggle-slider::before {
  content: '';
  position: absolute;
  top: 2px;
  left: 2px;
  width: 20px;
  height: 20px;
  background: white;
  border-radius: 50%;
  transition: transform 0.3s ease;
}

.toggle-input:checked + .toggle-slider {
  background: #007bff;
}

.toggle-input:checked + .toggle-slider::before {
  transform: translateX(20px);
}

.toggle-text {
  font-weight: 500;
  color: #495057;
}

.batch-print-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0.5rem 1rem;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.batch-print-btn:hover:not(:disabled) {
  background: #218838;
}

.batch-print-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.stats-section {
  margin: 0 0 1.5rem 0;
  display: flex;
  gap: 1rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
  border: 1px solid #dee2e6;
  min-width: 200px;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: #007bff;
  color: white;
  border-radius: 8px;
}

.stat-number {
  font-size: 1.5rem;
  font-weight: 700;
  color: #212529;
  line-height: 1;
}

.stat-label {
  font-size: 0.9rem;
  color: #6c757d;
}

.table-section {
  margin: 0;
}

.table-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
  border: 1px solid #dee2e6;
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #dee2e6;
  background: #f8f9fa;
}

.table-header h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #212529;
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
  padding: 0.5rem 1rem;
  background: #f8f9fa;
  color: #495057;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.refresh-btn:hover:not(:disabled) {
  background: #e9ecef;
  border-color: #adb5bd;
  color: #495057;
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
  color: #6c757d;
  font-size: 0.9rem;
}

.table-wrapper {
  overflow-x: auto;
}

.books-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1200px;
}

.books-table th {
  padding: 1rem 0.75rem;
  background: #f8f9fa;
  color: #495057;
  font-weight: 600;
  font-size: 0.9rem;
  border-bottom: 1px solid #dee2e6;
  text-align: left;
  white-space: nowrap;
}

.books-table td {
  padding: 1rem 0.75rem;
  border-bottom: 1px solid #f1f3f4;
  color: #495057;
  font-size: 0.9rem;
  vertical-align: middle;
}

.book-row:hover {
  background: #f8f9fa;
}

.book-title {
  max-width: 200px;
}

.title-content {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.title-text {
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.isbn,
.author,
.publisher {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.publish-date {
  white-space: nowrap;
}

.category-select {
  width: 120px;
  padding: 0.375rem 0.5rem;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 0.85rem;
  background: white;
}

.category-select:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.category-select:disabled {
  background: #e9ecef;
  opacity: 0.6;
}

.count-input {
  width: 60px;
  padding: 0.375rem 0.5rem;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 0.85rem;
  text-align: center;
}

.count-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.barcode-display {
  max-width: 180px;
}

.barcode-input {
  width: 100%;
  padding: 0.375rem 0.5rem;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 0.8rem;
  background: #f8f9fa;
  font-family: 'Courier New', monospace;
  overflow: hidden;
  text-overflow: ellipsis;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.barcode-btn {
  background: rgba(0, 123, 255, 0.1);
  color: #007bff;
}

.barcode-btn:hover {
  background: #007bff;
  color: white;
}

.delete-btn {
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
}

.delete-btn:hover {
  background: #dc3545;
  color: white;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  color: #6c757d;
  text-align: center;
}

.empty-state svg {
  margin-bottom: 1.5rem;
  opacity: 0.5;
}

.empty-state h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #495057;
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
  gap: 0.5rem;
}

.page-btn {
  padding: 0.5rem 0.75rem;
  border: 1px solid #dee2e6;
  background: white;
  color: #495057;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  min-width: 40px;
  text-align: center;
}

.page-btn:hover {
  background: #e9ecef;
  border-color: #adb5bd;
}

.page-btn.active {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

/* 반응형 디자인 */
@media (max-width: 1200px) {
  .header-content {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .search-content {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .print-controls {
    justify-content: space-between;
  }
}

@media (max-width: 768px) {
  .book-management-container {
    padding: 20px 0;
  }
  
  .page-title {
    font-size: 1.5rem;
  }
  
  .stats-section {
    flex-direction: column;
  }
  
  .stat-card {
    min-width: auto;
  }
  
  .books-table th,
  .books-table td {
    padding: 0.75rem 0.5rem;
    font-size: 0.8rem;
  }
  
  .books-table {
    min-width: 900px;
  }
  
  .category-select {
    width: 100px;
    font-size: 0.8rem;
  }
  
  .count-input {
    width: 50px;
  }
  
  .barcode-input {
    font-size: 0.75rem;
  }
  
  .action-btn {
    width: 28px;
    height: 28px;
  }
}
</style>