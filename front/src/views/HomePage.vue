<template>
  <BorrowReturn v-if="isModalOpen" @close="isModalOpen = false" />
  
  <div class="mainpage-bg-wrapper">
    <div class="mainpage-area">
      <div
        class="dropdown-wrapper"
        @mouseenter="hoveringWrapper = true"
        @mouseleave="hoveringWrapper = false"
        >
        <!-- 대분류 네비게이션 포함 -->
        <nav class="nav-bar" style="top: 72px; z-index: 1030;">
            <!-- 왼쪽: 카테고리 목록 -->
            <div class="nav-left">
                <ul class="nav">
                <li class="nav-item">
                    <a
                    class="nav-link"
                    :class="{ active: selectedLargeCategory === '전체' }"
                    href="#"
                    @click.prevent="selectLargeCategory('전체')"
                    @mouseenter="hoveredLargeCategory = '전체'"
                    >
                    전체
                    </a>
                </li>
                <li
                    class="nav-item"
                    v-for="(large, index) in largeCategories.filter(l => l.seqSortFirst !== 0)"
                    :key="index"
                    @mouseenter="hoveredLargeCategory = large.nameSortFirst"
                >
                    <a
                    class="nav-link"
                    :class="{ active: isLargeCategoryActive(large.nameSortFirst) }"
                    href="#"
                    @click.prevent="selectLargeCategory(large.nameSortFirst, large.seqSortFirst)"
                    >
                    {{ large.korSortFirst }}
                    </a>
                </li>
                </ul>
            </div>

            <!-- 오른쪽: 검색창 -->
            <div class="nav-right">
                <BookSearch class="search-component" @search="onSearch" />
            </div>
        </nav>

        <ul
            v-if="shouldShowMediumDropdown"
            class="dropdown-menu-custom"
        >
            <li
            class="dropdown-item-custom"
            :class="{ active: selectedMediumCategory === medium.seqSortSecond }"
            v-for="(medium, idx) in getMediumOptions(currentLargeForMedium)"
            :key="idx"
            @click="selectMediumCategory(medium.seqSortSecond, medium.seqSortFirst)"
            >
            {{ medium.korSortSecond }}
            </li>
        </ul>
      </div>

      <!-- 본문 -->
      <div class="main" :style="{ marginTop: mainMarginTop }">
        <div class="content-header" v-if="filteredBookList.length > 0">
          <h2 class="section-title">
            {{ getSectionTitle() }}
            <span class="book-count">({{ displayCount }}권)</span>
          </h2>
        </div>

        <div class="article-area" v-if="filteredBookList.length > 0">
          <BookArea 
            v-for="book in filteredBookList" 
            :key="book.seqBook"
            :book="book"
            class="book-item"
          />
        </div>

        <!-- 책이 없을 때 표시할 메시지 -->
        <div class="no-books-message" v-else>
          <div class="no-books-content">
            <div class="no-books-icon">
              <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="12" r="1" fill="currentColor"/>
                <circle cx="12" cy="8" r="1" fill="currentColor"/>
                <circle cx="12" cy="16" r="1" fill="currentColor"/>
              </svg>
            </div>
            <h3>해당 카테고리에 등록된 책이 없습니다</h3>
            <p>다른 카테고리를 선택해 주세요.</p>
          </div>
        </div>

        <!-- 페이지네이션 -->
        <div class="pagination-area" v-if="totalCount > 0">
          <div class="pagination-wrapper">
            <button 
              class="pagination-btn prev-btn" 
              :disabled="currentPage === 1"
              @click="loadBooks(currentPage - 1)"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              이전
            </button>
            
            <div class="page-info">
              <span class="current-page">{{ currentPage }}</span>
              <span class="page-divider">/</span>
              <span class="total-pages">{{ Math.ceil(totalCount / 10) }}</span>
            </div>
            
            <button 
              class="pagination-btn next-btn"
              :disabled="currentPage >= Math.ceil(totalCount / 10)"
              @click="loadBooks(currentPage + 1)"
            >
              다음
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import BookArea from '@/components/BookArea.vue'
import BookSearch from '@/components/BookSearch.vue'
import BorrowReturn from '@/components/BorrowReturn.vue'
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'

const isModalOpen = ref(true)

const largeCategories = ref([])
const mediumCategoriesAll = ref([])

const selectedLargeCategory = ref('')
const selectedLargeCategorySeq = ref(null)
const selectedMediumCategory = ref(null)
const selectedMediumCategoryLargeSeq = ref(null) // 중분류가 속한 대분류 seq 저장

const hoveringWrapper = ref(false)
const hoveredLargeCategory = ref(null)

const bookList = ref([])
const totalCount = ref(0)
const currentPage = ref(1)

const handleKeydown = (event) => {
  if (event.key === 'Escape' && isModalOpen.value) {
    isModalOpen.value = false
  }
}

// 검색 상태 추가
const isSearchMode = ref(false)

const fetchLargeCategories = async () => {
  try {
    const res = await fetch('http://localhost:8080/subjects')
    largeCategories.value = await res.json()
  } catch (error) {
    console.error('대분류 카테고리 조회 실패:', error)
  }
}

const fetchMediumCategories = async () => {
  try {
    const res = await fetch('http://localhost:8080/subtitles')
    mediumCategoriesAll.value = await res.json()
  } catch (error) {
    console.error('중분류 카테고리 조회 실패:', error)
  }
}

const loadBooks = async (page = 1) => {
  try {
    let url = ''
    if (selectedLargeCategory.value === '전체') {
      url = `http://localhost:8080/books?page=${page}`
    } else {
      url = `http://localhost:8080/books/sortFirst?id=${selectedLargeCategorySeq.value}&page=${page}`
    }

    const res = await fetch(url)
    const data = await res.json()

    // printCheckBook이 1인 책만 필터링
    const filteredBooks = (data.content || []).filter(book => book.printCheckBook === true)
    
    bookList.value = filteredBooks
    totalCount.value = filteredBooks.length // 필터링된 책의 개수로 업데이트
    currentPage.value = page
    
    // 검색 모드 해제
    isSearchMode.value = false
  } catch (error) {
    console.error('책 목록 조회 실패:', error)
    bookList.value = []
    totalCount.value = 0
  }
}

const getMediumOptions = (largeCode) => {
  const large = largeCategories.value.find(l => l.nameSortFirst === largeCode)
  if (!large) return []
  return mediumCategoriesAll.value.filter(m => m.seqSortFirst === large.seqSortFirst)
}

// 대분류 활성화 상태 판단 함수
const isLargeCategoryActive = (largeCategoryName) => {
  // 직접 선택된 경우
  if (selectedLargeCategory.value === largeCategoryName) {
    return true
  }
  
  // 중분류가 선택되었을 때, 해당 중분류의 상위 대분류인 경우
  if (selectedMediumCategory.value && selectedMediumCategoryLargeSeq.value) {
    const large = largeCategories.value.find(l => l.nameSortFirst === largeCategoryName)
    return large && large.seqSortFirst === selectedMediumCategoryLargeSeq.value
  }
  
  return false
}

// 중분류 노출 여부 조건 (선택된 중분류나 대분류가 있으면 계속 표시)
const shouldShowMediumDropdown = computed(() => {
  const hasSelection = selectedMediumCategory.value || (selectedLargeCategory.value && selectedLargeCategory.value !== '전체')
  
  return (
    (hoveringWrapper.value || hasSelection) &&
    currentLargeForMedium.value &&
    getMediumOptions(currentLargeForMedium.value).length > 0
  )
})

// 중분류용 기준 대분류 결정
const currentLargeForMedium = computed(() => {
  // 중분류가 선택된 경우, 해당 중분류의 상위 대분류 찾기
  if (selectedMediumCategory.value && selectedMediumCategoryLargeSeq.value) {
    const large = largeCategories.value.find(l => l.seqSortFirst === selectedMediumCategoryLargeSeq.value)
    if (large) {
      return large.nameSortFirst
    }
  }
  
  return hoveredLargeCategory.value || selectedLargeCategory.value
})

const filteredBookList = computed(() => {
  let filtered = bookList.value.filter(book => 
    book.seqSortFirst !== 0 && 
    book.seqSortSecond !== 0 &&
    book.printCheckBook === true  // printCheckBook이 1인 책만 표시
  );
  
  if (selectedMediumCategory.value) {
    filtered = filtered.filter(book => book.seqSortSecond === selectedMediumCategory.value);
  }
  
  return filtered;
});

const displayCount = computed(() => {
  return filteredBookList.value.length;
});

const mainMarginTop = computed(() => {
  const baseMargin = shouldShowMediumDropdown.value ? '180px' : '120px'
  return baseMargin
})

// 섹션 제목을 동적으로 생성하는 함수
const getSectionTitle = () => {
  // 검색 모드인 경우
  if (isSearchMode.value) {
    return '검색 결과'
  }
  
  // 중분류가 선택된 경우
  if (selectedMediumCategory.value && selectedMediumCategoryLargeSeq.value) {
    const large = largeCategories.value.find(l => l.seqSortFirst === selectedMediumCategoryLargeSeq.value)
    const medium = mediumCategoriesAll.value.find(m => m.seqSortSecond === selectedMediumCategory.value)
    
    if (large && medium) {
      return `${large.korSortFirst} / ${medium.korSortSecond}`
    }
  }
  
  // 대분류가 선택된 경우
  if (selectedLargeCategory.value === '전체') {
    return '전체 도서'
  } else {
    const large = largeCategories.value.find(l => l.nameSortFirst === selectedLargeCategory.value)
    return large ? large.korSortFirst : selectedLargeCategory.value
  }
}

function selectLargeCategory(categoryName, categorySeq = null) {
  selectedLargeCategory.value = categoryName
  selectedLargeCategorySeq.value = categorySeq
  selectedMediumCategory.value = null
  selectedMediumCategoryLargeSeq.value = null
  currentPage.value = 1
  loadBooks(1)
}

function selectMediumCategory(mediumSeq, largeSeq) {
  selectedMediumCategory.value = mediumSeq
  selectedMediumCategoryLargeSeq.value = largeSeq
  
  // 해당하는 대분류로 선택 상태 변경
  const large = largeCategories.value.find(l => l.seqSortFirst === largeSeq)
  if (large) {
    selectedLargeCategory.value = large.nameSortFirst
    selectedLargeCategorySeq.value = large.seqSortFirst
  }
}

function onSearch({ query, exact }) {
  console.log('검색 요청:', query, exact);
  fetchBooks(1, query, exact);
}

const fetchBooks = async (page = 1, query = '', exact = false) => {
  let url;
  if (query && query.trim()) {
    url = new URL(`http://localhost:8080/books/search`);
    url.searchParams.set('q', query.trim());
    url.searchParams.set('exact', exact);
    
    // 검색 모드 활성화
    isSearchMode.value = true
  } else {
    url = new URL(`http://localhost:8080/books`);
    url.searchParams.set('page', page);
    
    // 검색 모드 비활성화
    isSearchMode.value = false
  }

  const res = await fetch(url.toString());
  if (!res.ok) {
    const errorMessage = await res.text();
    throw new Error(errorMessage || `서버 오류: ${res.status}`)
  }

  const data = await res.json();

  console.log(data)

  if (!data.content) {
    alert('서버 응답 데이터 오류: ', data);
    bookList.value = [];
    totalCount.value = 0;
    return;
  }

  // 검색 결과에서 printCheckBook이 1인 책만 필터링
  const filteredBooks = data.content.filter(book => book.printCheckBook === true);

  totalCount.value = filteredBooks.length;
  bookList.value = filteredBooks.map(book => {
    return {
      ...book
    };
  });

  // console.log('✅ books.value 업데이트 완료:', books.value);
};

onMounted(async () => {
  await fetchLargeCategories()
  await fetchMediumCategories()
  selectedLargeCategory.value = '전체'
  await loadBooks(1)
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
})

</script>

<style scoped>
.mainpage-bg-wrapper {
  position: relative;
  width: 100%;
  min-height: 100vh;
  /* background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); */
  background: linear-gradient(135deg, #f5f7fa 0%, #EDEFEF 100%);
}

.mainpage-area {
  min-width: 1450px;
  min-height: 98%;
  overflow-x: inherit;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
}

.nav-bar {
  position: fixed;
  top: 72px;
  left: 0;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 32px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
  z-index: 1030;
}

.nav-left {
  display: flex;
  flex: 1;
}

.nav {
  display: flex;
  align-items: center;
  margin: 0;
  padding: 0;
  list-style: none;
}

.nav-item {
  margin: 0;
}

.nav-right {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  min-width: 280px;
}

.search-component {
  width: 280px;
}

.nav-link {
  display: inline-block;
  padding: 12px 20px;
  font-weight: 600;
  font-size: 0.95rem;
  color: #64748b;
  text-decoration: none;
  cursor: pointer;
  margin: 0 4px;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.nav-link::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.nav-link:hover::before {
  left: 100%;
}

.nav-link:hover {
  color: #475569;
  background: rgba(100, 116, 139, 0.1);
  transform: translateY(-2px);
}

.nav-link.active {
  color: #ffffff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transform: translateY(-2px);
}

.dropdown-menu-custom {
  list-style: none;
  padding: 20px 32px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  margin: 0;
  position: fixed;
  top: 168px;
  left: 0;
  width: 100%;
  z-index: 1029;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.dropdown-item-custom {
  padding: 10px 16px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
  color: #475569;
  font-size: 0.9rem;
}

.dropdown-item-custom:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.dropdown-item-custom.active {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 15px rgba(17, 153, 142, 0.4);
}

.main {
  width: 100%;
  padding: 32px;
  transition: margin-top 0.3s ease;
}

.content-header {
  margin-bottom: 32px;
  text-align: center;
}

.section-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.book-count {
  font-size: 1.2rem;
  font-weight: 500;
  color: #64748b;
  background: rgba(100, 116, 139, 0.1);
  padding: 4px 12px;
  border-radius: 20px;
}

.article-area {
  display: grid;
  place-items: center;
  width: 100%;
  grid-template-columns: repeat(5, 1fr);
  gap: 32px;
  padding: 0;
  min-height: 400px;
}

.book-item {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.book-item:hover {
  transform: translateY(-8px);
}

.no-books-message {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 500px;
  width: 100%;
  padding: 2rem;
}

.no-books-content {
  text-align: center;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  padding: 4rem 3rem;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.no-books-icon {
  color: #94a3b8;
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
}

.no-books-content h3 {
  color: #475569;
  margin-bottom: 16px;
  font-size: 1.5rem;
  font-weight: 600;
}

.no-books-content p {
  color: #64748b;
  font-size: 1.1rem;
  margin: 0;
  font-weight: 400;
}

.pagination-area {
  display: flex;
  justify-content: center;
  padding: 3rem 0;
  margin-top: 2rem;
}

.pagination-wrapper {
  display: flex;
  align-items: center;
  gap: 24px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  padding: 16px 32px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.pagination-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  color: #475569;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 500;
  font-size: 0.95rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 80px;
  justify-content: center;
}

.pagination-btn:hover:not(:disabled) {
  background: #f8fafc;
  border-color: #cbd5e1;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.page-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1e293b;
}

.current-page {
  font-size: 1.1rem;
  color: #667eea;
}

.page-divider {
  color: #cbd5e1;
}

.total-pages {
  font-size: 1rem;
  color: #64748b;
}

/* 반응형 디자인 */
@media (max-width: 1200px) {
  .article-area {
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 24px;
  }
}

@media (max-width: 768px) {
  .nav-bar {
    padding: 12px 16px;
    flex-direction: column;
    gap: 16px;
  }
  
  .nav-left {
    width: 100%;
    overflow-x: auto;
  }
  
  .nav {
    flex-wrap: nowrap;
    white-space: nowrap;
  }
  
  .nav-right {
    width: 100%;
    min-width: auto;
  }
  
  .search-component {
    width: 100%;
  }
  
  .article-area {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
  }
  
  .main {
    padding: 16px;
  }
  
  .pagination-wrapper {
    padding: 12px 20px;
    gap: 16px;
  }
  
  .pagination-btn {
    padding: 10px 16px;
    min-width: 70px;
  }
}
</style>