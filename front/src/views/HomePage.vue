<template>
  <div class="mainpage-area">
    <!-- 대분류 리스트 -->
    <nav class="bg-light px-4 py-2 fixed-top w-100" style="top: 72px; z-index: 1030;">
        <ul class="nav">
        <li class="nav-item">
            <a
            class="nav-link"
            :class="{ active: selectedLargeCategory === '전체' }"
            href="#"
            @click.prevent="selectLargeCategory('전체')"
            >
            전체
            </a>
        </li>
        <li class="nav-item" v-for="(large, index) in largeCategories" :key="index">
            <a
            class="nav-link"
            :class="{ active: selectedLargeCategory === large.nameSortFirst }"
            href="#"
            @click.prevent="selectLargeCategory(large.nameSortFirst, large.seqSortFirst)"
            >
            {{ large.korSortFirst }}
            </a>
        </li>
        </ul>
    </nav>

    <!-- 중분류 드롭다운 리스트 -->
    <ul
        v-if="selectedLargeCategory && selectedLargeCategory !== '전체'"
        class="dropdown-menu-custom"
    >
        <li
        class="dropdown-item-custom"
        :class="{ active: selectedMediumCategory === medium.seqSortSecond }"
        v-for="(medium, idx) in getMediumOptions(selectedLargeCategory)"
        :key="idx"
        @click="selectMediumCategory(medium.seqSortSecond)"
        >
        {{ medium.korSortSecond }}
        </li>
    </ul>

    <!-- 본문 영역 -->
    <main style="padding-top: 200px;">
        <!-- <h3>{{ selectedLargeCategory }} 카테고리 콘텐츠</h3> -->
        <div class="article-area">
        <BookArea 
            v-for="book in filteredBookList" 
            :key="book.seqBook"
            :book="book"
        />
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
    </main>
  </div>
  
</template>

<script setup>
import BookArea from '@/components/BookArea.vue'
import { ref, onMounted, computed } from 'vue'

const largeCategories = ref([])
const mediumCategoriesAll = ref([])

const selectedLargeCategory = ref('')
const selectedLargeCategorySeq = ref(null)
const selectedMediumCategory = ref(null)

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

// 중분류 필터링된 책 목록
const filteredBookList = computed(() => {
  if (!selectedMediumCategory.value) {
    return bookList.value
  }
  
  return bookList.value.filter(book => book.seqSortSecond === selectedMediumCategory.value)
})

function selectLargeCategory(categoryName, categorySeq = null) {
  selectedLargeCategory.value = categoryName
  selectedLargeCategorySeq.value = categorySeq
  selectedMediumCategory.value = null // 대분류 변경시 중분류 선택 초기화
  currentPage.value = 1
  loadBooks(1)
}

function selectMediumCategory(mediumSeq) {
  selectedMediumCategory.value = mediumSeq
}

onMounted(async () => {
  await fetchLargeCategories()
  await fetchMediumCategories()
  
  selectedLargeCategory.value = '전체'
  await loadBooks(1)
})
</script>

<style scoped>
.mainpage-area {
    min-width: 1550px;
    min-height: 98%;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
}
.nav-link {
  font-weight: bold;
  color: #555;
  cursor: pointer;
  margin: 0 1vw;
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
  top: 130px;
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
main {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding-top: 50px !important;
}
.article-area {
  display: grid;
  width: 100%;
  grid-template-columns: repeat(5, 1fr);
  gap: 50px;
  padding: 1rem;
}
.book-area {
  width: 220px;
  height: 310px;
}

.pagination-area {
  padding: 2rem 0;
}
</style>