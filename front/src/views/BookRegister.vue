<template>
    <div class="book-form-wrapper">
        <div class="left">
            <div class="header">
                <h2>도서 등록</h2>
                <router-link to="/books" custom v-slot="{ navigate }">
                    <button class="btn btn-outline-primary" type="button" @click="navigate">뒤로가기</button>
                </router-link>
            </div>
            <div class="input-group mb-3 mt-4" id="isbnInput">
                <input type="number" class="form-control" placeholder="ISBN을 입력하세요" v-model="book.isbn" @keydown="blockJavascriptInput" @keydown.space.prevent />
                <button class="btn btn-outline-secondary" type="button" @click="searchISBN">
                    조회
                </button>
            </div>

            <div class="mb-3">
                <label class="form-label">도서 제목</label>
                <input type="text" class="form-control" v-model="book.title" @keydown="blockJavascriptInput" @keydown.space.prevent />
            </div>

            <div class="mb-3">
                <label class="form-label">저자</label>
                <input type="text" class="form-control" v-model="book.author" @keydown="blockJavascriptInput" @keydown.space.prevent />
            </div>

            <div class="mb-3">
                <label class="form-label">출판사</label>
                <input type="text" class="form-control" v-model="book.publisher" @keydown="blockJavascriptInput" @keydown.space.prevent />
            </div>

            <div class="mb-3">
                <label class="form-label">출판일</label>
                <input type="date" class="form-control" v-model="book.publishDate" @keydown="blockJavascriptInput" @keydown.space.prevent />
            </div>

            <div class="regi-btn">
                <button class="btn btn-primary" @click="submitBook">등록</button>
            </div>
        </div>
        <div class="right">
            <h2>책 표지</h2>
            <div :class="{'img-container':book.title_url}" ref="bookImg" @mousemove="useMouse" @mouseleave="resetTransform">
                <div :class="{'overlay':book.title_url}" ref="overLay"></div>
                <img class="book-img" :src="book.title_url">
            </div>
        </div>
    </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch, nextTick } from 'vue'

const bookImg = ref(null)
const overLay = ref(null)

const book = reactive({
    isbn: '',
    title: '',
    author: '',
    publisher: '',
    publishDate: '',
    categoryLarge: '',
    categoryMedium: '',
    title_url: ''
})

async function searchISBN() {
    const apiKey = import.meta.env.VITE_NL_API_KEY

    const isbn = book.isbn

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
            book.title_url = ''
            alert('도서 정보를 찾을 수 없습니다.')
            return
        }

        book.title = doc['TITLE'] || ''
        book.author = doc['AUTHOR'] || ''
        book.publisher = doc['PUBLISHER'] || ''
        book.publishDate = formatDate(doc['PUBLISH_PREDATE'] || '')
        book.title_url = doc['TITLE_URL'] || ''

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
        publishDateBook: book.publishDate,
        imageBook: book.title_url
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

function useMouse(e) {
    const x = e.pageX;
    const y = e.pageY;
    console.log(x, y)
    const rotateY = (-20/173) * x + 114
    const rotateX = (-20 / 231) * y + (7780 / 231)

    if (bookImg.value) {
        overLay.value.style = `background-position : ${x/4 + y/4}%`
        bookImg.value.style = `transform : perspective(370px) rotateY(${rotateY}deg) rotateX(${rotateX}deg)`;
    }
};

function resetTransform() {
  if (bookImg.value) {
    overLay.value.style = 'filter : opacity(0)'
    bookImg.value.style.transform = 'perspective(600px) rotateY(0deg) rotateX(0deg)';
    bookImg.value.style.transition = 'transform 0.3s ease';
  }
}


</script>

<style scoped>
.book-form-wrapper {
    width: 98%;
    padding: 0.7rem;
    margin: 0 auto;
    max-width: none;
    display: grid;
    grid-template-columns: 1fr 1fr;
}
.left, .right {
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
.img-container {
    position: relative;
}
.overlay {
    position: absolute;
    width: 360px;
    height: 462.07px;
    background: linear-gradient(105deg,
    transparent 40%,
    rgba(255, 219, 112, 0.8) 45%,
    rgba(132, 50, 255, 0.6) 50%,
    transparent 54%);
    filter: brightness(1.1) opacity(0.8);
    mix-blend-mode: color-dodge;
    background-size: 150% 150%;
    background-position: 100%;
    transition: all 0.1s;
}
.img-container, .book-img {
    width: 360px;
    height: auto;
    border: 1px solid #e1e3e5;
    object-fit: contain;
}
</style>
