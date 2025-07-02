<template>
  <div class="container">
    <h1 class="text-2xl font-bold mb-4">도서 목록</h1>
    <table class="table table-hover">
      <thead>
        <tr>
          <th>제목</th>
          <th>ISBN</th>
          <th>저자</th>
          <th>출판사</th>
          <th>출판일</th>
          <th>대분류</th>
          <th>중분류</th>
          <th>책권수</th>
          <th>바코드</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="book in paginatedBooks" :key="book.seqBook" class="hover:bg-gray-50">
          <td>{{ book.titleBook }}</td>
          <td>{{ book.isbnBook }}</td>
          <td>{{ book.authorBook }}</td>
          <td>{{ book.publisherBook }}</td>
          <td>{{ book.publishDateBook }}</td>
          <td>
            <div class="mb-3">
              <select class="form-select" v-model="book.categoryLarge">
                <option
                  v-for="category in largeCategories"
                  :key="category.nameSortFirst"
                  :value="category.nameSortFirst"
                >
                  {{ category.korSortFirst }}
                </option>
              </select>
            </div>
          </td>
          <td>
            <div class="mb-3">
              <select
                class="form-select"
                v-model="book.categoryMedium"
                :disabled="!book.mediumOptions.length"

                style="min-width: 11ch"
              >
                <option
                  v-for="(category, index) in book.mediumOptions"
                  :key="index"
                  :value="category.seqSortSecond"
                >
                  {{ category.korSortSecond }}
                </option>
              </select>
            </div>
          </td>
          <td>
            <input type="number" class="inp form-control" v-model="book.cntBook" aria-label="cntBook" />
          </td>
          <td>
            <div class="input-group flex-nowrap">
              <input type="text" class="inp form-control" v-model="book.barcodeBook" aria-label="barcodeBook" />
            </div>
          </td>
          <td class="flex justify-center">
            <button @click="barcodeCreate(book)" class="btn btn-outline-primary">바코드 생성</button>
            <button @click="saveBook(book)" class="btn btn-outline-success">저장</button>
            <button @click="deleteBook(book)" class="btn btn-outline-danger">삭제</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>

  <div class="mt-4 flex justify-center space-x-2">
    <button
      v-for="page in totalPages"
      :key="page"
      @click="currentPage = page"
      :class="[
        'px-3 py-1 border rounded',
        page === currentPage
          ? 'bg-blue-500 text-white border-blue-500'
          : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-100'
      ]"
    >
      {{ page }}
    </button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watchEffect } from 'vue'

const books = ref([])
const largeCategories = ref([])
const mediumCategoriesAll = ref([])

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

const fetchBooks = async () => {
  const res = await fetch('http://localhost:8080/books')
  const data = await res.json()

  console.log('[fetchBooks] raw books from API:', data)

  // books 초기화 - 모든 카테고리 데이터가 로드된 후에 실행
  books.value = data.map(book => {
    // 책의 seqSortSecond를 통해 올바른 대분류 코드 찾기
    const largeCode = findLargeCodeFromSeqSecond(book.seqSortSecond)
    const mediumOptions = getMediumOptions(largeCode)

    // 초기 중분류는 책의 seqSortSecond 값 사용
    const initialMedium = book.seqSortSecond ?? ''

    console.log(`[fetchBooks] Book seqBook=${book.seqBook}`)
    console.log(`  - seqSortSecond: ${book.seqSortSecond}`)
    console.log(`  - largeCode: ${largeCode}`)
    console.log(`  - initialMedium: ${initialMedium}`)
    console.log(`  - mediumOptions:`, mediumOptions)

    return {
      ...book,
      categoryLarge: largeCode,
      categoryMedium: initialMedium,
      mediumOptions
    }
  })

  console.log('[fetchBooks] processed books:', books.value)
}

onMounted(async () => {
  console.log('[onMounted] 시작')
  // 순서가 중요: 대분류와 중분류 데이터를 먼저 로드한 후 책 데이터 처리
  await fetchLargeCategories()
  await fetchMediumCategories()
  await fetchBooks() // 이 시점에서 모든 카테고리 데이터가 준비되어 있음
})

/**
 * 각 book의 categoryLarge가 바뀔 때 개별 감시
 */
watchEffect(() => {
  books.value.forEach(book => {
    const largeCode = book.categoryLarge
    const oldOptions = book.mediumOptions?.map(m => m.seqSortSecond) || []

    // 대분류가 없으면 중분류 비움
    if (!largeCode) {
      if (book.categoryMedium !== '') {
        console.log(`[watch] Reset categoryMedium for seqBook=${book.seqBook}`)
        book.categoryMedium = ''
      }
      book.mediumOptions = []
    } else {
      // 대분류에 맞는 중분류 옵션
      const newOptions = getMediumOptions(largeCode)
      const newOptionsIds = newOptions.map(m => m.seqSortSecond)
      
      // 옵션이 변경되었을 때만 업데이트
      if (JSON.stringify(oldOptions) !== JSON.stringify(newOptionsIds)) {
        console.log(`[watch] Update mediumOptions for seqBook=${book.seqBook}`)
        book.mediumOptions = newOptions
        
        // 현재 선택된 중분류가 새 옵션에 없으면 초기화
        if (!newOptionsIds.includes(book.categoryMedium)) {
          book.categoryMedium = newOptions.length > 0 ? newOptions[0].seqSortSecond : ''
        }
      }
    }
  })
})

const currentPage = ref(1)
const pageSize = 10

const totalPages = computed(() => Math.ceil(books.value.length / pageSize))

const paginatedBooks = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return books.value.slice(start, start + pageSize)
})

function saveBook(book) {
  console.log('✅ [saveBook]', book)
  console.log('  - categoryLarge:', book.categoryLarge)
  console.log('  - categoryMedium:', book.categoryMedium)
}

function deleteBook(book) {
  console.log('✅ [deleteBook]', book)
  books.value = books.value.filter(b => b.seqBook !== book.seqBook)
}

function barcodeCreate(book) {
  console.log('✅ [barcodeCreate]', book)
}
</script>

<style>
.container {
  width: 100%;
  max-width: none;
}
.table {
  min-width: 1440px;
  width: 100%;
  table-layout: auto;
}
.btn {
  margin-right: 5px;
  margin-bottom: 5px;
}
.inp {
  width: 60px;
  box-sizing: border-box;
}
.input-group {
  width: 25ch;
}
</style>