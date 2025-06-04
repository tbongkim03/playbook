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

        <button class="btn btn-primary" @click="submitBook">등록</button>
    </div>
</template>

<script setup>
import { reactive, ref } from 'vue'

const book = reactive({
    isbn: '',
    title: '',
    author: '',
    publisher: '',
    publishDate: ''
})

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
    max-width: 600px;
    padding: 1rem;
    margin-left: 0;
    margin-right: auto;
}

pre {
    background: #f4f4f4;
    padding: 1rem;
    overflow-x: auto;
    white-space: pre-wrap;
    word-break: break-word;
}
</style>
