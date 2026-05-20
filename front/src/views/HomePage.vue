<template>
  <div class="homepage-wrapper">
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
          <!-- 로딩 상태 -->
          <div class="loading-container" v-if="isLoading">
            <div class="loading-content">
              <div class="loading-spinner">
                <div class="spinner-ring"></div>
                <div class="spinner-ring"></div>
                <div class="spinner-ring"></div>
                <div class="spinner-ring"></div>
              </div>
              <p class="loading-text">도서를 불러오는 중...</p>
            </div>
          </div>

          <!-- 로딩이 아닐 때만 컨텐츠 표시 -->
          <template v-else>
            <div class="content-header" v-if="filteredBookList.length > 0 || showCampusFilter">
              <div class="header-top">
                <h2 class="section-title">
                  {{ getSectionTitle() }}
                  <span class="book-count">({{ displayCount }}권)</span>
                </h2>

                <!-- 필터 영역 -->
                <div class="filter-area">
                  <!-- 캠퍼스 필터 (전체 관리자/비회원만 표시) -->
                  <div v-if="showCampusFilter" class="campus-filter">
                    <label class="filter-label">캠퍼스:</label>
                    <select v-model="selectedCampus" @change="onCampusChange" class="campus-select">
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
                  
                  <!-- 정렬 드롭다운 -->
                  <div class="sort-dropdown">
                    <select v-model="selectedSort" @change="onSortChange" class="sort-select">
                      <option value="latest">최신 등록순</option>
                      <option value="title">제목순 (가나다)</option>
                      <option value="author">저자순 (가나다)</option>
                      <option value="popular">인기순</option>
                    </select>
                  </div>
                </div>
              </div>
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
                <h3>해당 카테고리에 등록된 도서가 없습니다</h3>
                <p>다른 카테고리를 선택해 주세요.</p>
              </div>
            </div>
          </template>

          <!-- 페이지네이션 -->
          <div class="pagination-area" v-if="!isLoading && totalCount > 0">
            <div class="pagination-wrapper">
              <button 
                class="pagination-btn prev-btn" 
                :disabled="currentPage === 1"
                @click="goToPage(currentPage - 1)"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                이전
              </button>
              
              <div class="page-info">
                <span class="current-page">{{ currentPage }}</span>
                <span class="page-divider">/</span>
                <span class="total-pages">{{ Math.ceil(totalCount / ITEMS_PER_PAGE) }}</span>
              </div>
              
              <button 
                class="pagination-btn next-btn"
                :disabled="currentPage >= Math.ceil(totalCount / ITEMS_PER_PAGE)"
                @click="goToPage(currentPage + 1)"
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
  </div>
</template>

<script setup>
import axios from 'axios'
import BookArea from '@/components/BookArea.vue'
import BookSearch from '@/components/BookSearch.vue'
import BorrowReturn from '@/components/BorrowReturn.vue'
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { swAlert } from '@/utils/sweetAlert'

const isModalOpen = ref(true)

const largeCategories = ref([])
const mediumCategoriesAll = ref([])

const selectedLargeCategory = ref('')
const selectedLargeCategorySeq = ref(null)
const selectedMediumCategory = ref(null)
const selectedMediumCategoryLargeSeq = ref(null) // 중분류가 속한 대분류 seq 저장

const hoveringWrapper = ref(false)
const hoveredLargeCategory = ref(null)


const ITEMS_PER_PAGE = 20
const bookList = ref([])
const totalCount = ref(0)
const currentPage = ref(1)

// 캠퍼스 필터 관련
const campuses = ref([])
const selectedCampus = ref('')
const showCampusFilter = ref(false)
const isFullAdmin = ref(false)
const isGuest = ref(false)

const handleKeydown = (event) => {
  if (event.key === 'Escape' && isModalOpen.value) {
    isModalOpen.value = false
  }
}

// 검색 상태 추가
const isSearchMode = ref(false)

// 로딩 상태 추가
const isLoading = ref(false)

// 정렬 상태 추가
const selectedSort = ref('latest')

const fetchLargeCategories = async () => {
  try {
    const res = await axios.get('/api/subjects')
    largeCategories.value = res.data.data
  } catch (error) {
    console.warn('대분류 카테고리 조회 실패:', error.message)
  }
}

const fetchMediumCategories = async () => {
  try {
    const res = await axios.get('/api/subtitles')
    mediumCategoriesAll.value = res.data.data
  } catch (error) {
    console.warn('중분류 카테고리 조회 실패:', error.message)
  }
}

const loadBooks = async (page = 1) => {
  try {
    isLoading.value = true
    
    // 정렬 필드 매핑
    const sortFieldMap = {
      'latest': 'seqBook',
      'title': 'titleBook',
      'author': 'authorBook',
      'popular': 'borrowCount'
    }
    
    const sortBy = sortFieldMap[selectedSort.value] || 'seqBook'
    const sortDir = selectedSort.value === 'popular' ? 'desc' : (selectedSort.value === 'latest' ? 'desc' : 'asc')
    
    let url = ''
    // 캠퍼스 필터가 선택된 경우 쿼리 파라미터로 전달
    const campusParam = (selectedCampus.value && showCampusFilter.value) ? `&campusId=${selectedCampus.value}` : ''
    
    if (selectedLargeCategory.value === '전체') {
      url = `/api/books?page=${page}&size=${ITEMS_PER_PAGE}&sortBy=${sortBy}&sortDir=${sortDir}${campusParam}`
    } else {
      url = `/api/books/sortFirst?id=${selectedLargeCategorySeq.value}&page=${page}&size=${ITEMS_PER_PAGE}&sortBy=${sortBy}&sortDir=${sortDir}${campusParam}`
    }

    const res = await axios.get(url)
    const data = res.data.data

    // 서버에서 이미 필터링된 데이터를 받음
    bookList.value = data.content || []
    totalCount.value = data.totalCount || 0
    currentPage.value = page

    // 검색 모드 해제
    isSearchMode.value = false
    
    // 페이지 상단으로 스크롤 이동
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } catch (error) {
    console.warn('책 목록 조회 실패:', error.message)
    bookList.value = []
    totalCount.value = 0
  } finally {
    isLoading.value = false
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
  // 서버에서 이미 필터링된 데이터를 받으므로 추가 필터링은 중분류만
  if (selectedMediumCategory.value) {
    return bookList.value.filter(book => 
      book.seqSortSecond === selectedMediumCategory.value
    );
  }
  
  // 일반 필터링 (서버에서 이미 처리됨)
  return bookList.value;
});

const displayCount = computed(() => {
  if (selectedMediumCategory.value) {
    // 중분류가 선택된 경우 현재 표시된 책의 개수 반환
    return bookList.value.filter(book => 
      book.seqSortSecond === selectedMediumCategory.value
    ).length;
  }
  return totalCount.value;
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
  
  // 중분류 선택 시 첫 페이지로 리셋하고 서버에서 데이터 요청
  currentPage.value = 1
  loadBooks(1).then(() => {
    // 중분류 필터링 적용 (클라이언트 사이드)
    const mediumFilteredBooks = bookList.value.filter(book => 
      book.seqSortSecond === mediumSeq
    );
    bookList.value = mediumFilteredBooks
  })
  
  // 페이지 상단으로 스크롤 이동
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 페이지 이동 함수 추가
const goToPage = async (page) => {
  if (page < 1 || page > Math.ceil(totalCount.value / ITEMS_PER_PAGE)) {
    return;
  }
  
  // 중분류가 선택된 경우는 클라이언트 사이드 필터링 유지
  if (selectedMediumCategory.value) {
    // 중분류 필터링은 클라이언트 사이드에서 처리 (기존 로직 유지)
    // 하지만 서버에서 받은 데이터가 이미 필터링되어 있으므로 재요청 필요
    await loadBooks(page);
    // 중분류 필터링 적용
    const mediumFilteredBooks = bookList.value.filter(book => 
      book.seqSortSecond === selectedMediumCategory.value
    );
    bookList.value = mediumFilteredBooks;
  } else {
    // 서버에서 해당 페이지 데이터 요청
    await loadBooks(page);
  }
  
  // 페이지 상단으로 스크롤 이동
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function onSearch({ query, exact }) {
  // console.log('검색 요청:', query, exact);
  fetchBooks(query, exact);
}

// 정렬 함수
const sortBooks = (books) => {
  if (!books || books.length === 0) return books;

  const sortedBooks = [...books];

  switch (selectedSort.value) {
    case 'latest':
      // 최신 등록순 (seqBook 내림차순)
      return sortedBooks.sort((a, b) => b.seqBook - a.seqBook);

    case 'title':
      // 제목순 (가나다순)
      return sortedBooks.sort((a, b) => {
        const titleA = a.titleBook || '';
        const titleB = b.titleBook || '';
        return titleA.localeCompare(titleB, 'ko-KR');
      });

    case 'author':
      // 저자순 (가나다순)
      return sortedBooks.sort((a, b) => {
        const authorA = a.authorBook || '';
        const authorB = b.authorBook || '';
        return authorA.localeCompare(authorB, 'ko-KR');
      });

    case 'popular':
      // 인기순 (대출 횟수 내림차순)
      return sortedBooks.sort((a, b) => {
        const countA = a.borrowCount || 0;
        const countB = b.borrowCount || 0;
        return countB - countA;
      });

    default:
      return sortedBooks;
  }
};

// 정렬 변경 핸들러
const onSortChange = async () => {
  currentPage.value = 1;
  // 서버에서 정렬된 첫 페이지 데이터 요청
  await loadBooks(1);
};

const fetchBooks = async (query = '', exact = false) => {
  try {
    isLoading.value = true
    
    let url;
    if (query && query.trim()) {
      const params = new URLSearchParams();
      params.set('q', query.trim());
      params.set('exact', exact);
      url = `/api/books/search?${params.toString()}`;
      
      // 검색 모드 활성화
      isSearchMode.value = true
    } else {
      // 검색어가 없으면 일반 목록으로 이동
      isSearchMode.value = false
      await loadBooks(1);
      return;
    }

    const res = await axios.get(url);
    const data = res.data.data;

    if (!data.content) {
      console.warn('서버 응답 데이터 오류:', data);
      bookList.value = [];
      totalCount.value = 0;
      return;
    }

    // 검색 결과는 서버에서 이미 필터링된 데이터
    // 검색 결과는 페이지네이션 없이 전체 표시 (기존 동작 유지)
    const filteredBooks = data.content.filter(book => book.printCheckBook === true);
    const sortedBooks = sortBooks(filteredBooks);

    bookList.value = sortedBooks;
    totalCount.value = sortedBooks.length;
    currentPage.value = 1

    // 페이지 상단으로 스크롤 이동
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } catch (error) {
    await swAlert('도서 검색 중 오류가 발생했습니다.', 'error')
    bookList.value = []
    totalCount.value = 0
  } finally {
    isLoading.value = false
  }
};

// 캠퍼스 목록 가져오기
const fetchCampuses = async () => {
  try {
    const res = await axios.get('/api/campus')
    campuses.value = res.data.data || []
  } catch (error) {
    console.error('캠퍼스 목록 조회 실패:', error)
  }
}

// 전체 관리자/비회원 확인
const checkUserType = async () => {
  const userType = sessionStorage.getItem('userType')

  if (!userType) {
    // 비회원
    isGuest.value = true
    isFullAdmin.value = false
    showCampusFilter.value = true
    return
  }

  if (userType === 'admin') {
    try {
      const response = await axios.get('/api/admin/me', {
        validateStatus: () => true
      })
      
      if (response.status === 200) {
        // seqCampus가 null이면 전체 관리자
        if (!response.data.data.seqCampus) {
          isFullAdmin.value = true
          showCampusFilter.value = true
        } else {
          isFullAdmin.value = false
          showCampusFilter.value = false
        }
      }
    } catch (error) {
      isFullAdmin.value = false
      showCampusFilter.value = false
    }
  } else {
    // 일반 사용자
    isFullAdmin.value = false
    showCampusFilter.value = false
  }
}

// 캠퍼스 변경 핸들러
const onCampusChange = () => {
  currentPage.value = 1
  loadBooks(1)
}

onMounted(async () => {
  await fetchLargeCategories()
  await fetchMediumCategories()
  await fetchCampuses()
  await checkUserType()
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
  width: 100%;
  min-height: 98%;
  overflow-x: hidden;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
}

/* 데스크톱에서는 최소 너비 유지 */
@media (min-width: 769px) {
  .mainpage-area {
    min-width: 1450px;
  }
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

.header-top {
  display: flex;
  justify-content: center;
  align-items: center;
  max-width: 100%;
  margin: 0 auto;
  padding: 0 16px;
  gap: 24px;
  position: relative;
}

.filter-area {
  display: flex;
  align-items: center;
  gap: 16px;
  position: absolute;
  right: 16px;
}

.campus-filter {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-weight: 500;
  color: #475569;
  font-size: 0.9rem;
  white-space: nowrap;
}

.campus-select {
  padding: 10px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #ffffff;
  color: #475569;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
  min-width: 150px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.campus-select:hover {
  border-color: #cbd5e1;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.campus-select:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
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
  flex: 0 0 auto;
}

.book-count {
  font-size: 1.2rem;
  font-weight: 500;
  color: #64748b;
  background: rgba(100, 116, 139, 0.1);
  padding: 4px 12px;
  border-radius: 20px;
}

.sort-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sort-select {
  padding: 10px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #ffffff;
  color: #475569;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
  min-width: 150px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.sort-select:hover {
  border-color: #cbd5e1;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.sort-select:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.sort-select option {
  padding: 8px;
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

/* 로딩 스타일 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 500px;
  width: 100%;
  padding: 4rem 2rem;
}

.loading-content {
  text-align: center;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 4rem 3rem;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.loading-spinner {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
}

.spinner-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 4px solid transparent;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;
}

.spinner-ring:nth-child(1) {
  animation-delay: -0.45s;
  border-top-color: #667eea;
}

.spinner-ring:nth-child(2) {
  animation-delay: -0.3s;
  border-top-color: #764ba2;
  width: 70%;
  height: 70%;
  top: 15%;
  left: 15%;
}

.spinner-ring:nth-child(3) {
  animation-delay: -0.15s;
  border-top-color: #11998e;
  width: 50%;
  height: 50%;
  top: 25%;
  left: 25%;
}

.spinner-ring:nth-child(4) {
  border-top-color: #38ef7d;
  width: 30%;
  height: 30%;
  top: 35%;
  left: 35%;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: 1.1rem;
  font-weight: 500;
  color: #475569;
  margin: 0;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

/* 반응형 디자인 */
@media (max-width: 1200px) {
  .article-area {
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 24px;
  }
}

@media (max-width: 768px) {
  body {
    overflow-x: hidden !important;
  }

  .mainpage-bg-wrapper {
    overflow-x: hidden !important;
  }

  .mainpage-area {
    /* overflow-x: hidden !important;
    max-width: 100vw !important; */
    max-width: 100%;
  }

  .nav-bar {
    padding: 12px 16px;
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
    max-width: 100vw;
    box-sizing: border-box;
  }

  .nav-left {
    width: 100%;
    max-width: 100%;
    overflow-x: auto;
    overflow-y: hidden;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;
    -ms-overflow-style: none;
  }

  .nav-left::-webkit-scrollbar {
    display: none;
  }

  .nav {
    flex-wrap: nowrap !important;
    white-space: nowrap;
    gap: 8px;
    display: flex !important;
    flex-direction: row !important;
    width: max-content;
    min-width: 100%;
  }

  .nav-item {
    flex-shrink: 0;
  }

  .nav-link {
    padding: 8px 16px;
    font-size: 0.85rem;
    border-radius: 20px;
  }

  .nav-link:hover {
    transform: translateY(5px);
  }

  .nav-right {
    width: 100%;
    min-width: auto;
    max-width: 100%;
  }

  .search-component {
    width: 100%;
    max-width: 100%;
  }

  /* 중분류 드롭다운 가로 스크롤 */
  ul.dropdown-menu-custom {
    position: fixed !important;
    top: 214px !important;
    left: 0 !important;
    right: 0 !important;
    width: 100% !important;
    max-width: 100% !important;
    padding: 12px 16px !important;
    overflow-x: auto !important;
    overflow-y: hidden !important;
    -webkit-overflow-scrolling: touch !important;
    scrollbar-width: none !important;
    -ms-overflow-style: none !important;
    border-top: 1px solid rgba(0, 0, 0, 0.1) !important;
    border-bottom: 1px solid rgba(0, 0, 0, 0.1) !important;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1) !important;
    background: rgba(255, 255, 255, 0.98) !important;
    backdrop-filter: blur(10px) !important;
    flex-wrap: nowrap !important;
    z-index: 1029 !important;
    display: flex !important;
    margin: 0 !important;
    list-style: none !important;
    gap: 12px !important;
  }

  .dropdown-menu-custom::-webkit-scrollbar {
    display: none;
  }

  .dropdown-item-custom {
    flex-shrink: 0 !important;
    padding: 8px 14px;
    font-size: 0.8rem;
    border-radius: 16px;
    white-space: nowrap;
  }

  /* 메인 컨텐츠 여백 조정 */
  .main {
    margin-top: 200px !important;
    max-width: 100vw !important;
    box-sizing: border-box !important;
  }

  /* 헤더 섹션 */
  .header-top {
    flex-direction: column;
    gap: 12px;
    padding: 0;
  }

  .section-title {
    font-size: 1.3rem;
    justify-content: center;
  }

  .book-count {
    font-size: 0.9rem;
    padding: 3px 10px;
  }

  .filter-area {
    position: static;
    width: 100%;
    flex-direction: column;
    gap: 12px;
  }
  
  .sort-dropdown {
    width: 100%;
  }
  
  .campus-filter {
    width: 100%;
    justify-content: space-between;
  }
  
  .campus-select {
    flex: 1;
    min-width: auto;
  }

  .sort-select {
    width: 100%;
    max-width: 250px;
    min-width: auto;
    font-size: 0.9rem;
    padding: 10px 14px;
  }

  /* 도서 그리드 */
  .article-area {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .main {
    padding: 16px;
  }

  /* 페이지네이션 */
  .pagination-wrapper {
    padding: 12px 16px;
    gap: 12px;
  }

  .pagination-btn {
    padding: 8px 14px;
    min-width: 60px;
    font-size: 0.85rem;
  }

  .page-info {
    font-size: 0.9rem;
  }
}
</style>