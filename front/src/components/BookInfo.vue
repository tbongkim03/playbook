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
                    :src="book.imageBook && book.imageBook.trim() !== '' ? book.imageBook : noImage" 
                    :alt="book.titleBook"
                    @error="handleImageError"
                    @load="handleImageLoad"
                />
            </div>
        </div>
        <div class="right-area">
            <!-- 책 기본 정보 -->
            <div class="book-info">
                <h1 class="book-title">{{ book.titleBook }}</h1>
                <p class="book-author">{{ book.authorBook }}</p>
                <p class="book-publisher">{{ book.publisherBook }}</p>
                <p class="book-date">{{ book.publishDateBook }}</p>
            </div>

            <!-- 상세 정보 -->
            <div class="detail-info">
                <div class="info-item">
                    <span class="label">ISBN:</span>
                    <span class="value">{{ book.isbnBook }}</span>
                </div>
                <div class="info-item">
                    <span class="label">바코드:</span>
                    <span class="value">{{ book.barcodeBook }}</span>
                </div>
            </div>

            <!-- 액션 버튼 -->
            <div class="action-buttons">
                <button class="btn btn-primary" @click="handleBorrow">
                    <svg class="btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253z" />
                    </svg>
                    대출하기
                </button>
                
                <button class="btn btn-secondary" @click="handleWishlist">
                    <svg class="btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                    </svg>
                    찜하기
                </button>

                <button class="btn btn-tertiary" @click="handleShare">
                    <svg class="btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.367 2.684 3 3 0 00-5.367-2.684z" />
                    </svg>
                    공유하기
                </button>
            </div>
        </div>
    </div>
    <div v-else>
        책 정보를 찾을 수 없습니다.
    </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ref, onMounted } from 'vue'
import axios from 'axios'
import noImage from '@/assets/free-icon-no-image-11542598.png'

const route = useRoute()
const router = useRouter()
const bookId = route.params.id
const book = ref(null)
const loading = ref(true)
const error = ref(null)
const bookImg = ref(null)
const overLay = ref(null)

const handleImageError = (event) => {
    console.error('이미지 로딩 실패:', event.target.src)
    event.target.src = noImage
}

const handleImageLoad = () => {
    console.log('이미지 로딩 성공')
}

const handleBorrow = () => {
  router.push('/borrow')
}

const handleWishlist = () => {
    console.log('찜하기 클릭')
    // 찜하기 로직 구현
}

const handleShare = () => {
    navigator.clipboard.writeText(book.value.titleBook + ' ' + book.value.authorBook)
    alert('클립보드에 복사되었습니다!')
}

onMounted(async () => {
    try {
        console.log('요청할 bookId:', bookId)
        const res = await axios.get(`http://localhost:8080/books/${bookId}`)
        
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
    width: 98%;
    padding: 0.7rem;
    margin: 0 auto;
}
.left-area {
    width: 50%;
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
.img-container, .book-img {
    width: 70%;
    height: auto;
    border: 1px solid #e1e3e5;
    object-fit: contain;
}
.right-area {
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    gap: 2rem;
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

/* 오른쪽 영역 스타일 */
.book-info {
    border-bottom: 1px solid #e1e3e5;
    padding-bottom: 1.5rem;
}

.book-title {
    font-size: 2rem;
    font-weight: bold;
    color: #1a1a1a;
    margin-bottom: 0.5rem;
    line-height: 1.2;
}

.book-author {
    font-size: 1.25rem;
    color: #666;
    margin-bottom: 0.25rem;
}

.book-publisher {
    font-size: 1rem;
    color: #888;
    margin-bottom: 0.25rem;
}

.book-date {
    font-size: 0.95rem;
    color: #999;
}

.detail-info {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
}

.info-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.label {
    font-weight: 600;
    color: #374151;
    min-width: 80px;
}

.value {
    font-family: 'Courier New', monospace;
    color: #1f2937;
    background-color: #f9fafb;
    padding: 0.25rem 0.5rem;
    border-radius: 0.25rem;
    font-size: 0.9rem;
}

.action-buttons {
    display: flex;
    gap: 0.75rem;
    flex-wrap: wrap;
}

.btn {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1.5rem;
    border: none;
    border-radius: 0.5rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
    font-size: 0.95rem;
}

.btn-icon {
    width: 1.25rem;
    height: 1.25rem;
}

.btn-primary {
    background-color: #3b82f6;
    color: white;
}

.btn-primary:hover {
    background-color: #2563eb;
    transform: translateY(-1px);
}

.btn-secondary {
    background-color: #6b7280;
    color: white;
}

.btn-secondary:hover {
    background-color: #374151;
    transform: translateY(-1px);
}

.btn-tertiary {
    background-color: #8b5cf6;
    color: white;
}

.btn-tertiary:hover {
    background-color: #7c3aed;
    transform: translateY(-1px);
}
</style>