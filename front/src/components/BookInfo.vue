<template>
    <div v-if="loading" class="main">
        로딩 중...
    </div>
    <div v-else-if="error" class="main">
        {{ error }}
    </div>
    <div v-else-if="book" class="main">
        <div class="left-area">
            <div :class="{'img-container':book.title_url}" ref="bookImg" @mousemove="useMouse" @mouseleave="resetTransform">
                <div :class="{'overlay':book.title_url}" ref="overLay"></div>
                <img 
                    class="book-img" 
                    :src="book.imageBook" 
                    :alt="book.titleBook" 
                    @error="handleImageError"
                    @load="handleImageLoad"
                >
            </div>
        </div>
        <div class="right-area">
            <div class="title">{{ book.titleBook }}</div>
            <div class="author">{{ book.authorBook }}</div>
            <div class="publisher">{{ book.publisherBook }}</div>
            <div class="publish-date">{{ book.publishDateBook }}</div>
        </div>
    </div>
    <div v-else>
        책 정보를 찾을 수 없습니다.
    </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { ref, onMounted } from 'vue'
import axios from 'axios'

const route = useRoute()
const bookId = route.params.id
const book = ref(null)
const loading = ref(true)
const error = ref(null)

const handleImageError = (event) => {
    console.error('이미지 로딩 실패:', event.target.src)
    // 대체 이미지로 변경하거나 숨김 처리
    event.target.style.display = 'none'
}

const handleImageLoad = () => {
    console.log('이미지 로딩 성공')
}

function useMouse(e) {
    const x = e.pageX;
    const y = e.pageY;
    console.log(x, y)
    // const rotateY = (-20/173) * x + 114
    // const rotateX = (-20 / 231) * y + (7780 / 231)

    // if (bookImg.value) {
    //     overLay.value.style = `background-position : ${x/4 + y/4}%`
    //     bookImg.value.style = `transform : perspective(370px) rotateY(${rotateY}deg) rotateX(${rotateX}deg)`;
    // }
};

function resetTransform() {
  if (bookImg.value) {
    // overLay.value.style = 'filter : opacity(0)'
    // bookImg.value.style.transform = 'perspective(600px) rotateY(0deg) rotateX(0deg)';
    // bookImg.value.style.transition = 'transform 0.3s ease';
  }
}

onMounted(async () => {
    try {
        console.log('요청할 bookId:', bookId)
        const res = await axios.get(`http://localhost:8080/books/${bookId}`)
        console.log('응답 데이터:', res.data)
        
        if (res.data) {
            book.value = res.data
        } else {
            error.value = '책 데이터가 없습니다.'
        }
    } catch (err) {
        console.error('책 정보를 가져오는 중 오류 발생:', err)
        error.value = `오류: ${err.message}`
    } finally {
        loading.value = false
    }
})
</script>

<style scoped>
.main {
    height: 50vw;
    display: grid;
    grid-template-columns: 1fr 1fr;
}
.left-area, .right-area {
    min-width: 720px;
    width: 100%;
    padding: 1rem;
    margin: 0 auto;
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
.right-area {
    display: inline-block;
    vertical-align: top;
}

.title {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 8px;
}

.author {
    font-size: 14px;
    color: #666;
    margin-bottom: 4px;
}

.publisher {
    font-size: 12px;
    color: #888;
    margin-bottom: 4px;
}

.publish-date {
    font-size: 12px;
    color: #888;
}
</style>