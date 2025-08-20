<template>
    <div v-if="loading" class="main">
        로딩 중...
    </div>
    <div v-else-if="error" class="main">
        {{ error }}
    </div>
    <div v-else-if="book" class="main" :class="{ 'borrowed-book': book.bookBorrowed }">
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
                
                
                <!-- 대출중 오버레이 (이미지에만 적용) -->
                <div v-if="book.bookBorrowed && !book.borrowedByMe" class="borrowed-overlay">
                    <div class="borrowed-badge-large">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" fill="currentColor"/>
                            <path d="M9 12L11 14L15 10" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        <span class="borrowed-text-large">대출 중</span>
                    </div>
                    <div class="borrowed-dimmer"></div>
                    
                    <!-- 중앙 대여중 메시지 -->
                    <div class="borrowed-center-message">
                        <div class="borrowed-icon">
                            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </div>
                        <p class="borrowed-message">현재 대출 중</p>
                    </div>
                </div>

                <!-- 본인이 대출한 경우 오버레이 -->
                <div v-if="book.borrowedByMe" class="my-borrowed-overlay">
                    <div class="my-borrowed-badge">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        <span class="my-borrowed-text">대출 중</span>
                    </div>
                </div>
            </div>
        </div>
        <div class="right-area">
            <!-- 대출중 알림 배너 -->
            <div v-if="book.bookBorrowed && !book.borrowedByMe" class="borrowed-alert">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 9V13M12 17H12.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span>이 도서는 현재 대출 중입니다</span>
            </div>

            <!-- 내가 대출중인 경우 알림 배너 -->
            <div v-if="book.borrowedByMe" class="my-borrowed-alert">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span>현재 사용자님께서 대출 중인 도서입니다</span>
            </div>

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
                <div class="info-item">
                    <span class="label">대출 상태:</span>
                    <span class="value" :class="getStatusClass()">
                        {{ getStatusText() }}
                    </span>
                </div>
            </div>

            <!-- 액션 버튼 -->
            <div class="action-buttons">
                <button 
                    class="btn"
                    :class="getButtonClass()"
                    @click="handleBorrowOrReturn"
                    :disabled="book.bookBorrowed && !book.borrowedByMe"
                >
                    <svg v-if="!book.bookBorrowed" class="btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253z" />
                    </svg>
                    <svg v-else-if="book.borrowedByMe" class="btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                    <svg v-else class="btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728L5.636 5.636m12.728 12.728L18.364 5.636M5.636 18.364l12.728-12.728" />
                    </svg>
                    {{ getButtonText() }}
                </button>
                
                <button 
                    class="btn"
                    :class="isWishlisted ? 'btn-wishlisted' : 'btn-secondary'"
                    @click="handleWishlist"
                >
                    <svg v-if="!isWishlisted" class="btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                    </svg>
                    <svg v-else class="btn-icon" fill="currentColor" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                    </svg>
                    {{ isWishlisted ? '찜 해제' : '찜하기' }}
                </button>

                <button class="btn btn-tertiary" @click="handleShare">
                    <svg class="btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.367 2.684 3 3 0 00-5.367-2.684z" />
                    </svg>
                    공유하기
                </button>
            </div>

            <!-- 대출중일 때 추가 정보 (다른 사람이 대출한 경우에만) -->
            <div v-if="book.bookBorrowed && !book.borrowedByMe" class="borrowed-info">
                <h3>다른 옵션</h3>
                <ul>
                    <li>• 유사한 도서를 검색해보세요</li>
                    <li>• 찜하기를 통해 반납 시 디스코드로 알림을 받아보세요</li>
                </ul>
            </div>

            <!-- 본인이 대출중일 때 추가 정보 -->
            <div v-if="book.borrowedByMe" class="my-borrowed-info">
                <h3>반납 안내</h3>
                <ul>
                    <li>• 반납하기 버튼을 클릭하여 도서를 반납할 수 있습니다</li>
                    <li>• 대출 연장은 불가능합니다</li>
                    <li>• 반납 후에 다시 대출이 가능합니다</li>
                </ul>
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
const isWishlisted = ref(false) // 찜하기 상태 추가

const handleImageError = (event) => {
    console.error('이미지 로딩 실패:', event.target.src)
    event.target.src = noImage
}

const handleImageLoad = () => {
    console.log('이미지 로딩 성공')
}

const getStatusText = () => {
    if (book.value.borrowedByMe) {
        return '대출 중'
    } else if (book.value.bookBorrowed) {
        return '대출 중'
    } else {
        return '대출 가능'
    }
}

const getStatusClass = () => {
    if (book.value.borrowedByMe) {
        return 'status-my-borrowed'
    } else if (book.value.bookBorrowed) {
        return 'status-borrowed'
    } else {
        return 'status-available'
    }
}

const getButtonText = () => {
    if (book.value.borrowedByMe) {
        return '반납하기'
    } else if (book.value.bookBorrowed) {
        return '대출 불가'
    } else {
        return '대출하기'
    }
}

const getButtonClass = () => {
    if (book.value.borrowedByMe) {
        return 'btn-return'
    } else if (book.value.bookBorrowed) {
        return 'btn-disabled'
    } else {
        return 'btn-primary'
    }
}

const handleBorrowOrReturn = () => {
    if (book.value.borrowedByMe) {
        // 내가 대출한 경우 - 반납 페이지로 이동
        router.push('/return')
    } else if (book.value.bookBorrowed) {
        // 다른 사람이 대출한 경우
        alert('이 도서는 현재 대출 중입니다.')
        return
    } else {
        // 대출 가능한 경우 - 대출 페이지로 이동
        router.push('/borrow')
    }
}

const handleWishlist = async () => {
    try {
        const token = localStorage.getItem('jwtToken')
        
        if (!token) {
            alert('로그인이 필요합니다.')
            router.push('/login')
            return
        }
        
        let response
        
        if (isWishlisted.value) {
            // 찜하기 해제 - DELETE 요청
            response = await axios.delete('http://localhost:8080/favor', {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                data: book.value.seqBook
            })
        } else {
            // 찜하기 추가 - POST 요청
            response = await axios.post('http://localhost:8080/favor', book.value.seqBook, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                }
            })
        }
        
        if (response.status === 200) {
            // 상태 토글
            isWishlisted.value = !isWishlisted.value
        }
    } catch (error) {
        console.error('찜하기 요청 실패:', error)
        
        if (error.response) {
            const status = error.response.status
            const message = error.response.data || '오류가 발생했습니다.'
            
            if (status === 403) {
                alert(message)
            } else if (status === 401) {
                alert('로그인이 필요하거나 토큰이 만료되었습니다.')
                localStorage.removeItem('jwtToken')
                router.push('/login')
            } else {
                alert(`오류: ${message}`)
            }
        } else if (error.request) {
            alert('서버와의 연결에 실패했습니다. 잠시 후 다시 시도해주세요.')
        } else {
            alert('요청 처리 중 오류가 발생했습니다.')
        }
    }
}

const handleShare = () => {
    navigator.clipboard.writeText(book.value.titleBook + ' ' + book.value.authorBook)
    alert('클립보드에 복사되었습니다!')
}

// 찜하기 상태 확인 함수
const checkWishlistStatus = async () => {
    try {
        const token = localStorage.getItem('jwtToken')
        if (!token) return
        
        const response = await axios.get('http://localhost:8080/favor', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
        
        if (response.status === 200 && response.data) {
            const favorList = response.data
            isWishlisted.value = favorList.some(favor => 
                favor.titleBook === book.value.titleBook && 
                favor.authorBook === book.value.authorBook
            )
        }
    } catch (error) {
        console.log('찜하기 상태 확인 실패:', error)
    }
}

onMounted(async () => {
    try {
        const token = localStorage.getItem('jwtToken')
        const headers = {}
        if (token) {
            headers['Authorization'] = `Bearer ${token}`
        }
        
        const res = await axios.get(`http://localhost:8080/books/${bookId}`, {
            headers: headers
        })
        
        if (res.data) {
            book.value = res.data
            await checkWishlistStatus()
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
    position: relative;
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

/* 대여중 오버레이는 이미지 컨테이너에만 적용 (다른 사람이 대여한 경우) */
.borrowed-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 70%; /* 이미지와 같은 크기 */
    height: 100%;
    z-index: 10;
    border-radius: 4px;
    overflow: hidden;
}

.borrowed-dimmer {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.6);
    backdrop-filter: blur(3px);
}

.borrowed-badge-large {
    position: absolute;
    top: 15px;
    right: 15px;
    background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
    color: white;
    padding: 8px 12px;
    border-radius: 16px;
    font-size: 0.8rem;
    font-weight: 700;
    display: flex;
    align-items: center;
    gap: 6px;
    box-shadow: 0 6px 20px rgba(239, 68, 68, 0.5);
    animation: pulse 2s infinite;
    z-index: 11;
}

.borrowed-text-large {
    font-size: 0.75rem;
    letter-spacing: 0.5px;
}

.borrowed-center-message {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
    color: white;
    z-index: 11;
}

.borrowed-icon {
    background: rgba(239, 68, 68, 0.9);
    border-radius: 50%;
    width: 60px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 12px;
    box-shadow: 0 4px 20px rgba(239, 68, 68, 0.3);
}

.borrowed-message {
    font-size: 0.95rem;
    font-weight: 600;
    margin: 0;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

/* 내가 대여한 경우 오버레이 */
.my-borrowed-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 70%; /* 이미지와 같은 크기 */
    height: 100%;
    z-index: 10;
    border-radius: 4px;
    overflow: hidden;
}

.my-borrowed-badge {
    position: absolute;
    top: 15px;
    right: 15px;
    background: linear-gradient(135deg, #10b981 0%, #059669 100%);
    color: white;
    padding: 8px 12px;
    border-radius: 16px;
    font-size: 0.8rem;
    font-weight: 700;
    display: flex;
    align-items: center;
    gap: 6px;
    box-shadow: 0 6px 20px rgba(16, 185, 129, 0.5);
    z-index: 11;
}

.my-borrowed-text {
    font-size: 0.75rem;
    letter-spacing: 0.5px;
}

@keyframes pulse {
    0%, 100% {
        box-shadow: 0 6px 20px rgba(239, 68, 68, 0.5);
    }
    50% {
        box-shadow: 0 6px 25px rgba(239, 68, 68, 0.7);
        transform: scale(1.02);
    }
}

.right-area {
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    gap: 2rem;
}

/* 대여중 알림 배너 (다른 사람이 대여한 경우) */
.borrowed-alert {
    background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
    border: 2px solid #fca5a5;
    border-radius: 12px;
    padding: 16px;
    display: flex;
    align-items: center;
    gap: 12px;
    color: #dc2626;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(220, 38, 38, 0.1);
}

.borrowed-alert svg {
    flex-shrink: 0;
}

/* 내가 대여중인 경우 알림 배너 */
.my-borrowed-alert {
    background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 100%);
    border: 2px solid #6ee7b7;
    border-radius: 12px;
    padding: 16px;
    display: flex;
    align-items: center;
    gap: 12px;
    color: #059669;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(16, 185, 129, 0.1);
}

.my-borrowed-alert svg {
    flex-shrink: 0;
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

.status-borrowed {
    background-color: #fef2f2;
    color: #dc2626;
    font-weight: 700;
}

.status-available {
    background-color: #f0fdf4;
    color: #16a34a;
    font-weight: 700;
}

.status-my-borrowed {
    background-color: #ecfdf5;
    color: #059669;
    font-weight: 700;
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

.btn:disabled {
    cursor: not-allowed;
    opacity: 0.6;
    transform: none !important;
}

.btn-icon {
    width: 1.25rem;
    height: 1.25rem;
}

.btn-primary {
    background-color: #3b82f6;
    color: white;
}

.btn-primary:hover:not(:disabled) {
    background-color: #2563eb;
    transform: translateY(-1px);
}

.btn-return {
    background: linear-gradient(135deg, #10b981 0%, #059669 100%);
    color: white;
    box-shadow: 0 4px 15px rgba(16, 185, 129, 0.4);
}

.btn-return:hover {
    background: linear-gradient(135deg, #059669 0%, #047857 100%);
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba(16, 185, 129, 0.5);
}

.btn-disabled {
    background-color: #dc2626;
    color: white;
}

.btn-disabled:hover {
    background-color: #b91c1c;
}

.btn-secondary {
    background-color: #6b7280;
    color: white;
}

.btn-secondary:hover {
    background-color: #374151;
    transform: translateY(-1px);
}

.btn-wishlisted {
    background: linear-gradient(135deg, #ec4899 0%, #be185d 100%);
    color: white;
    box-shadow: 0 4px 15px rgba(236, 72, 153, 0.4);
}

.btn-wishlisted:hover {
    background: linear-gradient(135deg, #db2777 0%, #9d174d 100%);
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba(236, 72, 153, 0.5);
}

.btn-wishlisted .btn-icon {
    animation: heartbeat 1.5s ease-in-out infinite;
}

@keyframes heartbeat {
    0%, 100% {
        transform: scale(1);
    }
    14% {
        transform: scale(1.1);
    }
    28% {
        transform: scale(1);
    }
    42% {
        transform: scale(1.1);
    }
    70% {
        transform: scale(1);
    }
}

.btn-tertiary {
    background-color: #8b5cf6;
    color: white;
}

.btn-tertiary:hover {
    background-color: #7c3aed;
    transform: translateY(-1px);
}

/* 대여중일 때 추가 정보 (다른 사람이 대여한 경우에만) */
.borrowed-info {
    background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
    border-radius: 12px;
    padding: 20px;
    border: 1px solid #e2e8f0;
}

.borrowed-info h3 {
    color: #374151;
    font-size: 1.1rem;
    font-weight: 600;
    margin-bottom: 12px;
}

.borrowed-info ul {
    list-style: none;
    padding: 0;
    margin: 0;
}

.borrowed-info li {
    color: #6b7280;
    margin-bottom: 8px;
    font-size: 0.95rem;
}

/* 내가 대여중일 때 추가 정보 */
.my-borrowed-info {
    background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 100%);
    border-radius: 12px;
    padding: 20px;
    border: 1px solid #6ee7b7;
}

.my-borrowed-info h3 {
    color: #059669;
    font-size: 1.1rem;
    font-weight: 600;
    margin-bottom: 12px;
}

.my-borrowed-info ul {
    list-style: none;
    padding: 0;
    margin: 0;
}

.my-borrowed-info li {
    color: #047857;
    margin-bottom: 8px;
    font-size: 0.95rem;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
    .main {
        grid-template-columns: 1fr;
        height: auto;
    }
    
    .left-area, .right-area {
        min-width: auto;
    }
    
    .borrowed-badge-large, .my-borrowed-badge {
        top: 10px;
        right: 10px;
        padding: 8px 12px;
        font-size: 0.8rem;
    }
    
    .borrowed-center-message {
        padding: 0 20px;
    }
    
    .borrowed-icon {
        width: 60px;
        height: 60px;
    }
    
    .borrowed-message {
        font-size: 1rem;
    }
}
</style>