<template>
    <div class="book-form-wrapper">
        <div class="header">
            <h2>도서 등록</h2>
            <router-link to="/books" custom v-slot="{ navigate }">
                <button class="btn btn-outline-primary" type="button" @click="navigate">뒤로가기</button>
            </router-link>
        </div>
        

        <div class="input-group mb-3 mt-4" id="isbnInput">
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

        <div class="regi-btn">
            <button class="btn btn-primary" @click="submitBook">등록</button>
        </div>
    </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'

const book = reactive({
    isbn: '',
    title: '',
    author: '',
    publisher: '',
    publishDate: '',
    categoryLarge: '',
    categoryMedium: ''
})

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

        const doc = data?.docs?.[0] || null
        console.log('조회된 도서 정보:', doc)

        if (!doc) {
            // 이전 정보 초기화
            book.isbn = ''
            book.title = ''
            book.author = ''
            book.publisher = ''
            book.publishDate = ''
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
    const payload = {
        seqSortSecond: 0, // 미지정
        isbnBook: book.isbn,
        titleBook: book.title,
        authorBook: book.author,
        publisherBook: book.publisher,
        publishDateBook: book.publishDate
    };

    fetch('http://localhost:8080/books', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    })
        .then(res => {
            if (!res.ok) {
                throw new Error('등록 실패');
            } return res.json();
        })
        .then(data => {
            alert('도서가 성공적으로 등록되었습니다!');
            console.log('응답:', data);
        })
        .catch(err => {
            console.error('등록 중 오류 발생 : ', err);
            alert('도서 등록 중 오류가 발생했습니다.');
        })
}
</script>

<style scoped>
.book-form-wrapper {
    min-width: 720px;
    width: 100%;
    padding: 1rem;
    margin: 0 auto;
}
.header {
    display: flex;
    justify-content: space-between;
}
#isbnInput input {
    margin-bottom: 5px;
}
.regi-btn {
    display:flex;
    justify-content: end;
}
</style>
