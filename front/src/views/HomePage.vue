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
          <nav class="nav-bar" style="top: var(--pb-header-height); z-index: 1030;">
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
          <div class="gl-pagination" v-if="!isLoading && totalPages > 1">
            <span class="gl-pagination-info">{{ paginationInfo }}</span>
            <nav class="gl-pagination-nav">
              <button class="gl-page-btn prev-btn" :disabled="currentPage === 1" @click="goToPage(currentPage - 1)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                이전
              </button>
              <template v-for="item in paginationItems" :key="String(item) + '-hp'">
                <span v-if="item === '...'" class="gl-page-ellipsis">…</span>
                <button v-else class="gl-page-btn" :class="{ active: item === currentPage }" @click="goToPage(item)">{{ item }}</button>
              </template>
              <button class="gl-page-btn next-btn" :disabled="currentPage >= totalPages" @click="goToPage(currentPage + 1)">
                다음
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </button>
            </nav>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import * as bookApi from '@/api/book'
import * as sortApi from '@/api/sort'
import * as campusApi from '@/api/campus'
import * as adminApi from '@/api/admin'
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

const totalPages = computed(() => Math.ceil(totalCount.value / ITEMS_PER_PAGE))

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
  const total = totalCount.value
  if (!total) return ''
  const start = (currentPage.value - 1) * ITEMS_PER_PAGE + 1
  const end = Math.min(currentPage.value * ITEMS_PER_PAGE, total)
  return `${start}–${end} / 전체 ${total}건`
})

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
const lastSearchQuery = ref('')
const lastSearchExact = ref(false)

// 로딩 상태 추가
const isLoading = ref(false)

// 정렬 상태 추가
const selectedSort = ref('latest')

const fetchLargeCategories = async () => {
  try {
    const res = await sortApi.getFirstCategories()
    largeCategories.value = res.data.data
  } catch (error) {
    console.warn('대분류 카테고리 조회 실패:', error.message)
  }
}

const fetchMediumCategories = async () => {
  try {
    const res = await sortApi.getSecondCategories()
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
    
    const campusId = (selectedCampus.value && showCampusFilter.value) ? selectedCampus.value : undefined
    const params = { page, size: ITEMS_PER_PAGE, sortBy, sortDir, campusId }

    const res = selectedLargeCategory.value === '전체'
      ? await bookApi.getAll(params)
      : await bookApi.getByCategory(selectedLargeCategorySeq.value, params)
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
  let list = bookList.value;

  if (selectedMediumCategory.value) {
    list = list.filter(book => book.seqSortSecond === selectedMediumCategory.value);
  }

  // 검색 모드는 전체 결과가 bookList에 있으므로 클라이언트 페이지네이션 적용
  if (isSearchMode.value) {
    const start = (currentPage.value - 1) * ITEMS_PER_PAGE;
    return list.slice(start, start + ITEMS_PER_PAGE);
  }

  return list;
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

const goToPage = async (page) => {
  if (page < 1 || page > totalPages.value) return;

  // 검색 모드는 bookList에 전체 결과가 있으므로 페이지만 변경
  if (isSearchMode.value) {
    currentPage.value = page;
    window.scrollTo({ top: 0, behavior: 'smooth' });
    return;
  }

  await loadBooks(page);
  window.scrollTo({ top: 0, behavior: 'smooth' });
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
  if (isSearchMode.value) {
    await fetchBooks(lastSearchQuery.value, lastSearchExact.value);
  } else {
    await loadBooks(1);
  }
};

const fetchBooks = async (query = '', exact = false) => {
  try {
    isLoading.value = true
    
    if (query && query.trim()) {
      // 검색 모드 활성화 및 쿼리 저장
      isSearchMode.value = true
      lastSearchQuery.value = query.trim()
      lastSearchExact.value = exact
    } else {
      // 검색어가 없으면 일반 목록으로 이동
      isSearchMode.value = false
      await loadBooks(1);
      return;
    }

    const res = await bookApi.search({ q: query.trim(), exact });
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
    const res = await campusApi.getAll()
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
      const response = await adminApi.checkMe()
      
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
/* ────────────────────────────────────────────────
   레이아웃 기반
──────────────────────────────────────────────── */
.mainpage-bg-wrapper {
  position: relative;
  width: 100%;
  min-height: 100vh;
  background: var(--pb-color-canvas);
  overflow-x: hidden;
}

.mainpage-area {
  width: 100%;
  min-height: 98%;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* ────────────────────────────────────────────────
   카테고리 네비바
──────────────────────────────────────────────── */
.nav-bar {
  position: fixed;
  top: var(--pb-header-height);
  left: 0;
  width: 100%;
  height: var(--pb-toolbar-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 max(24px, calc((100vw - var(--pb-content-max)) / 2));
  background: var(--pb-color-canvas);
  border-bottom: 1px solid var(--pb-color-border);
  z-index: 1030;
}

.nav-left {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.nav {
  display: flex;
  align-items: center;
  gap: 4px;
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
  min-width: 320px;
}

.search-component {
  width: 320px;
}

/* ────────────────────────────────────────────────
   nav 링크
──────────────────────────────────────────────── */
.nav-link {
  display: inline-block;
  padding: 7px 14px;
  font-weight: 500;
  font-size: 0.92rem;
  color: var(--pb-color-text-muted);
  text-decoration: none;
  cursor: pointer;
  border-radius: 999px;
  transition: background 0.12s ease, color 0.12s ease;
}

.nav-link:hover {
  color: var(--pb-color-text);
  background: var(--pb-color-surface-muted);
}

.nav-link.active {
  color: var(--pb-color-surface);
  background: var(--pb-color-brand);
}

/* ────────────────────────────────────────────────
   중분류 드롭다운
──────────────────────────────────────────────── */
.dropdown-menu-custom {
  list-style: none;
  padding: 12px max(24px, calc((100vw - var(--pb-content-max)) / 2));
  background: var(--pb-color-canvas);
  border-bottom: 1px solid var(--pb-color-border);
  margin: 0;
  position: fixed;
  top: calc(var(--pb-header-height) + var(--pb-toolbar-height));
  left: 0;
  width: 100%;
  z-index: 1029;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.dropdown-item-custom {
  padding: 7px 13px;
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.12s ease, border-color 0.12s ease;
  font-weight: 500;
  color: var(--pb-color-text-muted);
  font-size: 0.88rem;
  white-space: nowrap;
}

.dropdown-item-custom:hover {
  background: var(--pb-color-surface-muted);
  border-color: var(--pb-color-border-strong);
}

.dropdown-item-custom.active {
  background: var(--pb-color-brand);
  color: var(--pb-color-surface);
  border-color: transparent;
}

/* ────────────────────────────────────────────────
   본문 영역
──────────────────────────────────────────────── */
.main {
  width: min(100% - 48px, var(--pb-content-max));
  padding: 28px 0 48px;
  margin-left: auto;
  margin-right: auto;
  transition: margin-top 0.25s ease;
}

/* ────────────────────────────────────────────────
   콘텐츠 헤더
──────────────────────────────────────────────── */
.content-header {
  margin-bottom: 24px;
  text-align: left;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.section-title {
  font-size: 1.6rem;
  font-weight: 700;
  color: var(--pb-color-heading);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 0 0 auto;
  letter-spacing: 0;
}

.book-count {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--pb-color-text-muted);
  background: var(--pb-color-surface-muted);
  padding: 2px 10px;
  border-radius: 999px;
}

/* ────────────────────────────────────────────────
   필터 영역
──────────────────────────────────────────────── */
.filter-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.campus-filter {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-weight: 500;
  color: var(--pb-color-text-muted);
  font-size: 0.88rem;
  white-space: nowrap;
}

.campus-select,
.sort-select {
  padding: 8px 12px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  outline: none;
  transition: border-color 0.12s ease;
  min-width: 140px;
}

.campus-select:hover,
.sort-select:hover {
  border-color: var(--pb-color-border-strong);
}

.campus-select:focus,
.sort-select:focus {
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px var(--pb-color-brand-muted);
}

.sort-dropdown {
  display: flex;
  align-items: center;
}

/* ────────────────────────────────────────────────
   도서 그리드
──────────────────────────────────────────────── */
.article-area {
  display: grid;
  align-items: start;
  width: 100%;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px 16px;
  padding: 0;
  min-height: 400px;
}

.book-item {
  transition: none;
}

/* ────────────────────────────────────────────────
   도서 없음 메시지
──────────────────────────────────────────────── */
.no-books-message {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  width: 100%;
  padding: 2rem;
}

.no-books-content {
  text-align: center;
  background: var(--pb-color-surface);
  padding: 3.5rem 3rem;
  border-radius: var(--pb-radius-lg);
  border: 1px solid var(--pb-color-border);
  max-width: 480px;
}

.no-books-icon {
  color: var(--pb-color-text-soft);
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}

.no-books-content h3 {
  color: var(--pb-color-text-muted);
  margin-bottom: 12px;
  font-size: 1.25rem;
  font-weight: 600;
}

.no-books-content p {
  color: var(--pb-color-text-soft);
  font-size: 0.95rem;
  margin: 0;
}

/* ────────────────────────────────────────────────
   페이지네이션 (GitLab Offset style)
──────────────────────────────────────────────── */
.gl-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 0 28px;
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

/* ────────────────────────────────────────────────
   로딩
──────────────────────────────────────────────── */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  width: 100%;
  padding: 3rem 2rem;
}

.loading-content {
  text-align: center;
  background: var(--pb-color-surface);
  padding: 3.5rem 3rem;
  border-radius: var(--pb-radius-lg);
  border: 1px solid var(--pb-color-border);
}

.loading-spinner {
  position: relative;
  width: 56px;
  height: 56px;
  margin: 0 auto 20px;
}

.spinner-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 3px solid transparent;
  border-top-color: var(--pb-color-brand);
  border-radius: 50%;
  animation: spin 1.1s linear infinite;
}

.spinner-ring:nth-child(1) {
  animation-delay: -0.4s;
  border-top-color: var(--pb-color-brand);
}

.spinner-ring:nth-child(2) {
  animation-delay: -0.27s;
  border-top-color: var(--pb-color-brand-muted);
  width: 70%;
  height: 70%;
  top: 15%;
  left: 15%;
}

.spinner-ring:nth-child(3) {
  animation-delay: -0.13s;
  border-top-color: var(--pb-color-accent);
  width: 50%;
  height: 50%;
  top: 25%;
  left: 25%;
}

.spinner-ring:nth-child(4) {
  border-top-color: var(--pb-color-border-strong);
  width: 30%;
  height: 30%;
  top: 35%;
  left: 35%;
}

@keyframes spin {
  0%   { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  font-size: 0.95rem;
  font-weight: 500;
  color: var(--pb-color-text-muted);
  margin: 0;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0.55; }
}

/* ────────────────────────────────────────────────
   반응형: 1024px 이하
──────────────────────────────────────────────── */
@media (max-width: 1024px) {
  .nav-bar {
    padding-inline: 18px;
  }

  .nav-right,
  .search-component {
    min-width: 260px;
    width: 260px;
  }

  .header-top {
    align-items: flex-start;
    flex-direction: column;
  }

  .filter-area {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .article-area {
    grid-template-columns: repeat(4, 1fr);
  }
}

/* ────────────────────────────────────────────────
   반응형: 768px 이하
──────────────────────────────────────────────── */
@media (max-width: 768px) {
  body {
    overflow-x: hidden !important;
  }

  .mainpage-bg-wrapper {
    overflow-x: hidden !important;
  }

  .mainpage-area {
    max-width: 100%;
  }

  .nav-bar {
    height: auto;
    min-height: 136px;
    padding: 10px 14px;
    flex-direction: column;
    gap: 10px;
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
    display: flex !important;
    flex-direction: row !important;
    width: max-content;
    min-width: 100%;
  }

  .nav-item {
    flex-shrink: 0;
  }

  .nav-link {
    padding: 7px 13px;
    font-size: 0.85rem;
  }

  .nav-right,
  .search-component {
    width: 100%;
    min-width: 0;
  }

  ul.dropdown-menu-custom {
    position: fixed !important;
    top: calc(var(--pb-header-height) + 136px) !important;
    left: 0 !important;
    right: 0 !important;
    width: 100% !important;
    max-width: 100% !important;
    padding: 10px 14px !important;
    overflow-x: auto !important;
    overflow-y: hidden !important;
    -webkit-overflow-scrolling: touch !important;
    scrollbar-width: none !important;
    -ms-overflow-style: none !important;
    border-bottom: 1px solid var(--pb-color-border) !important;
    background: var(--pb-color-canvas) !important;
    flex-wrap: nowrap !important;
    z-index: 1029 !important;
    display: flex !important;
    margin: 0 !important;
    list-style: none !important;
    gap: 8px !important;
  }

  .dropdown-menu-custom::-webkit-scrollbar {
    display: none;
  }

  .dropdown-item-custom {
    flex-shrink: 0 !important;
    padding: 6px 12px;
    font-size: 0.8rem;
  }

  .main {
    width: min(100% - 24px, var(--pb-content-max));
    padding: 20px 0 36px;
    margin-top: 188px !important;
  }

  .header-top {
    flex-direction: column;
    gap: 12px;
  }

  .section-title {
    font-size: 1.2rem;
    justify-content: flex-start;
  }

  .book-count {
    font-size: 0.85rem;
  }

  .filter-area {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
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
    min-width: auto;
    font-size: 0.88rem;
  }

  .article-area {
    grid-template-columns: repeat(2, 1fr);
    gap: 14px 10px;
  }

  .pagination-wrapper {
    width: 100%;
    justify-content: space-between;
    gap: 8px;
    padding: 10px 12px;
  }

  .pagination-btn {
    padding: 7px 12px;
    min-width: 60px;
    font-size: 0.85rem;
  }

  .page-info {
    font-size: 0.88rem;
  }
}
</style>
