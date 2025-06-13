<template>
    <div class="book-form-wrapper">
        <h2>도서 등록</h2>

        <div class="input-group mb-3 mt-4">
            <input type="text" class="form-control" placeholder="ISBN을 입력하세요" v-model="book.isbn" />
            <button class="btn btn-outline-secondary" type="button" @click="searchISBN">
                조회
            </button>
        </div>

        <div class="mb-3">
            <label class="form-label">도서 제목</label>
            <input type="text" class="form-control" v-model="book.title" />
        </div>

        <div class="mb-3">
            <label class="form-label">저자</label>
            <input type="text" class="form-control" v-model="book.author" />
        </div>

        <div class="mb-3">
            <label class="form-label">출판사</label>
            <input type="text" class="form-control" v-model="book.publisher" />
        </div>

        <div class="mb-3">
            <label class="form-label">출판일</label>
            <input type="date" class="form-control" v-model="book.publishDate" />
        </div>

        <!-- 대분류 드롭다운 -->
        <div class="mb-3">
            <label class="form-label">대분류</label>
            <select class="form-select" v-model="book.categoryLarge">
                <option disabled value="">대분류를 선택하세요</option>
                <option v-for="category in largeCategories" :key="category.nameSortFirst"
                    :value="category.nameSortFirst">
                    {{ category.korSortFirst }}
                </option>
            </select>
        </div>

        <!-- 중분류 드롭다운 -->
        <div class="mb-3">
            <label class="form-label">중분류</label>
            <select class="form-select" v-model="book.categoryMedium" :disabled="mediumCategories.length === 0">
                <option disabled value="">중분류를 선택하세요</option>
                <option v-for="(category, index) in mediumCategories" :key="index" :value="category">
                    {{ category }}
                </option>>
            </select>
        </div>

        <button class="btn btn-primary" @click="submitBook">등록</button>
    </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'

const book = reactive({
    isbn: '',
    title: '',
    author: '',
    publisher: '',
    publishDate: '',
    categoryLarge: '',
    categoryMedium: ''
})

// JSON 파싱 없이 바로 배열 정의
const largeCategories = [
    { nameSortFirst: 'A', korSortFirst: '일반' },
    { nameSortFirst: 'B', korSortFirst: '컴퓨터일반' },
    { nameSortFirst: 'C', korSortFirst: '웹/앱' },
    { nameSortFirst: 'D', korSortFirst: '데이터베이스/빅데이터/분석/엔지니어링' },
    { nameSortFirst: 'E', korSortFirst: '클라우드/데브옵스' },
    { nameSortFirst: 'F', korSortFirst: '인공지능' },
    { nameSortFirst: 'G', korSortFirst: '기타' },
    { nameSortFirst: 'H', korSortFirst: '엔코아' }
]

const mediumCategories = ref([])

const mediumCategoriesMap = {
    'A': ['일반'],
    'B': ['일반', '파이썬', '자바', '자바스크립트', '리눅스', '네트워크', '자격증', '알고리즘', '기타'],
    'C': ['일반', '프론트엔드', '백엔드', '풀스택', '앱개발'],
    'D': ['일반', '파이썬', 'R', '데이터베이스', '빅데이터 플랫폼', '기타'],
    'E': ['종합', 'AWS', '클라우드 플랫폼', '도커', '쿠버네티스', '기타'],
    'F': ['일반', '머신러닝/딥러닝', 'LLM', '기타'],
    'G': ['사물인터넷', '블록체인', '게임'],
    'H': ['데이터베이스', '데이터아키텍쳐', '데이터분석', '데이터엔지니어링', '기타']
}


watch(
    () => book.categoryLarge,
    (newVal) => {
        if (newVal && mediumCategoriesMap[newVal]) {
            mediumCategories.value = mediumCategoriesMap[newVal]
            book.categoryMedium = '' // 초기화
        } else {
            mediumCategories.value = []
            book.categoryMedium = ''
        }
    }
)

const jsonData = ref(null)

async function searchISBN() {
    const apiKey = import.meta.env.VITE_NL_API_KEY

    const isbn = book.isbn.trim()

    if (!isbn) {
        alert('ISBN을 입력해주세요.')
        return
    }

    // result_style=json 으로 변경
    const url = `https://www.nl.go.kr/seoji/SearchApi.do?cert_key=${apiKey}&result_style=json&page_no=1&page_size=1&isbn=${isbn}`

    try {
        const res = await fetch(url)
        const data = await res.json()

        jsonData.value = JSON.stringify(data, null, 2) // 화면에 출력용

        // jsonData.value를 다시 파싱해서 객체로 변환
        const parsedData = JSON.parse(jsonData.value)
        // JSON 내부 경로는 API 문서 및 실제 데이터를 확인 후 적절히 수정 필요
        const doc = parsedData?.docs?.[0] || null
        console.log('조회된 도서 정보:', doc)

        if (!doc) {
            alert('도서 정보를 찾을 수 없습니다.')
            return
        }

        book.title = doc['TITLE'] || ''
        book.author = doc['AUTHOR'] || ''
        book.publisher = doc['PUBLISHER'] || ''
        book.publishDate = formatDate(doc['PUBLISH_PREDATE'] || '')

    } catch (err) {
        console.error('API 조회 실패:', err)
        alert('도서 정보를 조회하는 중 오류가 발생했습니다.')
    }
}

function formatDate(dateStr) {
    // YYYYMMDD 형식인 경우 처리
    if (/^\d{8}$/.test(dateStr)) {
        const year = dateStr.substring(0, 4)
        const month = dateStr.substring(4, 6)
        const day = dateStr.substring(6, 8)
        return `${year}-${month}-${day}`
    }

    // YYYY 형식만 있는 경우
    if (/^\d{4}$/.test(dateStr)) {
        return `${dateStr}-01-01`
    }

    return ''
}

function submitBook() {
    console.log('등록 도서 정보:', book)
    alert('도서가 성공적으로 등록되었습니다.')
}
</script>

<style scoped>
.book-form-wrapper {
    width: 100%;
    padding: 1rem;
    margin: 0 auto;
}
</style>
