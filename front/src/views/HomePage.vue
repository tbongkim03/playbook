<template>
  <div class="mainpage-bg-wrapper">
    <div class="mainpage-area">
      <div
        class="dropdown-wrapper"
        @mouseenter="hoveringWrapper = true"
        @mouseleave="hoveringWrapper = false"
        >
        <!-- 대분류 네비게이션 포함 -->
        <nav class="nav-bar bg-light px-4 py-2 fixed-top w-100" style="top: 72px; z-index: 1030;">
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
                <BookSearch style="width: 200px;" @search="onSearch" />
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
        <div class="article-area" v-if="filteredBookList.length > 0">
          <BookArea 
            v-for="book in filteredBookList" 
            :key="book.seqBook"
            :book="book"
          />
        </div>

        <!-- 책이 없을 때 표시할 메시지 -->
        <div class="no-books-message" v-else>
          <div class="no-books-content">
            <h3>해당 카테고리에 등록된 책이 없습니다</h3>
            <p>다른 카테고리를 선택해 주세요.</p>
          </div>
        </div>

        <!-- 페이지네이션 -->
        <div class="pagination-area mt-4 d-flex justify-content-center" v-if="totalCount > 0">
          <button 
            class="btn btn-outline-primary me-2" 
            :disabled="currentPage === 1"
            @click="loadBooks(currentPage - 1)"
          >
            이전
          </button>
          <span class="align-self-center mx-3">
            {{ currentPage }} / {{ Math.ceil(totalCount / 10) }}
          </span>
          <button 
            class="btn btn-outline-primary ms-2"
            :disabled="currentPage >= Math.ceil(totalCount / 10)"
            @click="loadBooks(currentPage + 1)"
          >
            다음
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import BookArea from '@/components/BookArea.vue'
import BookSearch from '@/components/BookSearch.vue'
import { ref, onMounted, computed } from 'vue'

const largeCategories = ref([])
const mediumCategoriesAll = ref([])

const selectedLargeCategory = ref('')
const selectedLargeCategorySeq = ref(null)
const selectedMediumCategory = ref(null)
const selectedMediumCategoryLargeSeq = ref(null) // 중분류가 속한 대분류 seq 저장

const hoveringWrapper = ref(false)
const hoveredLargeCategory = ref(null)
const hoveringMedium = ref(false)

const bookList = ref([])
const totalCount = ref(0)
const currentPage = ref(1)

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

    bookList.value = data.content || []
    totalCount.value = data.totalCount || 0
    currentPage.value = page
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

// 대분류 활성화 상태 판단 함수 추가
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
  if (!selectedMediumCategory.value) {
    return bookList.value
  }
  return bookList.value.filter(book => book.seqSortSecond === selectedMediumCategory.value)
})

const mainMarginTop = computed(() => {
  const baseMargin = shouldShowMediumDropdown.value ? '130px' : '60px'
  return baseMargin
})

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
  } else {
    url = new URL(`http://localhost:8080/books`);
    url.searchParams.set('page', page);
  }

  console.log('👉 호출 URL:', url.toString());

  const res = await fetch(url.toString());
  if (!res.ok) {
    console.error('❌ 서버 응답 오류:', res.status);
    return;
  }

  const data = await res.json();

  if (!data.content) {
    console.error('❌ 서버 응답 데이터 문제:', data);
    bookList.value = [];
    totalCount.value = 0;
    return;
  }

  totalCount.value = data.totalCount;
  bookList.value = data.content.map(book => {
    return {
      ...book
    };
  });

  console.log('✅ books.value 업데이트 완료:', books.value);
};

onMounted(async () => {
  await fetchLargeCategories()
  await fetchMediumCategories()
  selectedLargeCategory.value = '전체'
  await loadBooks(1)
})
</script>

<style scoped>
.mainpage-bg-wrapper {
  position: relative;
  width: 100%;
  height: 100vw;
  background-color: #EDEFEF;
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
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-left {
  display: flex;
  flex: 1;
}

.nav-right {
  display: flex;
  justify-content: flex-end;
  align-items: flex-start;
  min-width: 200px;
}
.nav-link {
  font-weight: bold;
  color: #555;
  cursor: pointer;
  margin: 0 1vw;
  z-index: 1029;
}
.nav-link.active {
  color: #42bbb2;
  border-bottom: 2px solid #42bbb2;
}
.dropdown-menu-custom {
  list-style: none;
  padding: 0.5rem 1rem;
  background-color: #f9f9f9;
  border-top: 1px solid #ddd;
  margin: 0;
  position: fixed;
  top: 150px;
  left: 0;
  width: 100%;
  z-index: 1029;
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}
.dropdown-item-custom {
  padding: 0.5rem 1rem;
  background-color: white;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: background-color 0.2s;
}
.dropdown-item-custom:hover {
  background-color: #e8f6f5;
}
.dropdown-item-custom.active {
  background-color: #42bbb2;
  color: white;
}
.article-area {
  display: grid;
  place-items: center;
  width: 100%;
  grid-template-columns: repeat(5, 1fr);
  gap: 50px;
  padding: 1rem;
  min-height: 400px;
}
.book-area {
  width: 220px;
  height: auto;
}
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
  background-color: white;
  padding: 3rem 2rem;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  max-width: 500px;
}
.no-books-content h3 {
  color: #666;
  margin-bottom: 1rem;
  font-size: 1.5rem;
}
.no-books-content p {
  color: #888;
  font-size: 1.1rem;
  margin: 0;
}
.main {
  width: 100%;
  padding: 1rem;
  transition: margin-top 0.3s ease;
}
.pagination-area {
  padding: 2rem 0;
}
</style>