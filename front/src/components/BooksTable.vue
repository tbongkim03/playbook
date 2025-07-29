<template>

  <Barcode 
    :seqBook="selectedSeqBook"
    :seqSortSecond="selectedSeqSortSecond"
    :cntBook="selectedCntBook"
    :barcodeBook="selectedBarcode" 
    :titleBook="selectedBookTitle" 
    :isOpen="isOpen" 
    @close="isOpen = false"/>

  <PrintBatch v-if="isPrintBatchOpen" :books="booksToPrint" @close="isPrintBatchOpen = false"/>

  <div class="container">
    <div class="tops">
        <h1 class="text-2xl font-bold mb-4">도서 목록</h1>
        <BookSearch @search="onSearch" />
        <router-link to="/books/register" custom v-slot="{ navigate }">
            <button type="button" class="btn btn-primary register-btn" @click="navigate">
                도서 등록 페이지
            </button>
        </router-link>
        
    </div>

    <div class="check-area">
        <label for="printCheck"><h5>프린트 여부</h5></label>
        <input type="checkbox" name="printCheck" id="printCheck" style="zoom: 1.4;" v-model="isPrint">
        <button class="btn btn-secondary" v-if="isPrint == true" @click="printBarcodes">모아서 출력 🖨️</button>
    </div>

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
            <input type="number" class="inp form-control" v-model="book.cntBook" aria-label="cntBook" min="1" @input="() => { if (book.cntBook < 1) book.cntBook = 1 }" />
          </td>
          <td>
            <div class="input-group flex-nowrap">
              <input type="text" class="inp form-control" v-model="book.barcodeBook" aria-label="barcodeBook" />
            </div>
          </td>
          <td class="flex row center actions">
            <button @click="barcodeCreate(book)" class="btn btn-outline-success">바코드</button>
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
import { ref, computed, onMounted, watch, watchEffect } from 'vue'
import BookSearch from './BookSearch.vue'
import Barcode from './Barcode.vue'
import PrintBatch from './BookPrintBatch.vue'

const API_BASE = 'http://localhost:8080';

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
const booksToPrint = ref([])

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
  console.log('검색 요청:', query, exact);
  fetchBooks(1, query, exact);
}


const fetchBooks = async (page = 1, query = '', exact = false) => {
  let url;
  if (query && query.trim()) {
    url = new URL(`${API_BASE}/books/search`);
    url.searchParams.set('q', query.trim());
    url.searchParams.set('exact', exact);
  } else {
    url = new URL(`${API_BASE}/books`);
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
    books.value = [];
    totalCount.value = 0;
    return;
  }

  totalCount.value = data.totalCount;
  books.value = data.content.map(book => {
    const largeCode = findLargeCodeFromSeqSecond(book.seqSortSecond);
    const mediumOptions = getMediumOptions(largeCode);
    return {
      ...book,
      categoryLarge: largeCode,
      categoryMedium: book.seqSortSecond ?? '',
      mediumOptions
    };
  });

  console.log('✅ books.value 업데이트 완료:', books.value);
};

onMounted(async () => {
  console.log('[onMounted] 시작')
  // 순서가 중요: 대분류와 중분류 데이터를 먼저 로드한 후 책 데이터 처리
  await fetchLargeCategories()
  await fetchMediumCategories()
  await fetchBooks(currentPage.value)
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

    // --------- 여기에 바코드 자동 생성 기능 추가 ---------

    // 대분류 객체 찾기
    const large = largeCategories.value.find(l => l.nameSortFirst === book.categoryLarge)
    // 중분류 객체 찾기
    const medium = mediumCategoriesAll.value.find(m => m.seqSortSecond === book.categoryMedium)

    if (large && medium) {
      const isbn = book.isbnBook
      const cnt = book.cntBook

      if (isbn && cnt && book.categoryLarge && book.categoryMedium !== '') {
        const barcode = `${large.nameSortFirst}${medium.nameSortSecond}-${isbn}-${cnt}`
        if (book.barcodeBook !== barcode) {
          book.barcodeBook = barcode
          console.log(`✅ 자동 생성 바코드(seqBook=${book.seqBook}):`, barcode)
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

watch(currentPage, (newPage) => {
  fetchBooks(newPage)
})

async function deleteBook(book) {
  console.log('✅ [deleteBook]', book)
  try {
    const response = await fetch(`${API_BASE}/books/${book.seqBook}`, {
      method: 'DELETE',
    });

    if (!response.ok) {
      throw new Error(`서버 오류: ${response.status}`);
    }

    books.value = books.value.filter(b => b.seqBook !== book.seqBook);
    alert('✅ 삭제에 성공하였습니다.');
  } catch (error) {
    alert('삭제 중 오류가 발생했습니다.', error);
  }
}

function barcodeCreate(book) {
  console.log('✅ [barcodeCreate]', book)
  selectedSeqBook.value = book.seqBook
  selectedSeqSortSecond.value = book.categoryMedium
  selectedCntBook.value = book.cntBook
  selectedBarcode.value = book.barcodeBook
  selectedBookTitle.value = book.titleBook
  isOpen.value = true

}

function printBarcodes() {
  booksToPrint.value = books.value.filter(book =>
    book.printCheckBook === false &&
    book.categoryLarge !== 0 &&
    book.categoryMedium !== 0 &&
    book.barcodeBook &&
    book.barcodeBook.trim() !== ''
  )
  isPrintBatchOpen.value = true
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
.tops {
    display: grid;
    grid-template-columns: 1fr 3fr 0.5fr;
}
.check-area input {
    margin:-4px 15px 0 15px; 
    vertical-align:middle;
}
</style>