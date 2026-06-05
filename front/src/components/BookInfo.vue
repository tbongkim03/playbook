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
                <div v-if="showCampusInfo && book.campusName" class="info-item">
                    <span class="label">캠퍼스:</span>
                    <span class="value campus-value">{{ book.campusName }}</span>
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
                    :disabled="(book.bookBorrowed && !book.borrowedByMe) || mobile"
                    :title="mobile ? 'PC에서만 이용 가능한 기능입니다' : ''"
                >
                    <svg v-if="!book.bookBorrowed" class="btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253z" />
                    </svg>
                    <svg v-else-if="book.borrowedByMe" class="btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                    <svg v-else class="btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    {{ getButtonText() }}
                </button>
                
                <button 
                    v-if="!isAdmin"
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
            </div>

            <!-- 대출 중일 때 추가 정보 (다른 사람이 대출한 경우에만) -->
            <div v-if="book.bookBorrowed && !book.borrowedByMe" class="borrowed-info">
                <h3>다른 옵션</h3>
                <ul>
                    <li>• 비슷한 도서를 검색해보세요</li>
                    <li>• 찜하기를 통해 반납 시 디스코드로 알림을 받아보세요</li>
                </ul>
            </div>

            <!-- 본인이 대출 중일 때 추가 정보 -->
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
import * as bookApi from '@/api/book'
import * as adminApi from '@/api/admin'
import * as favorApi from '@/api/favor'
import noImage from '@/assets/free-icon-no-image-11542598.png'
import { isMobile } from '@/utils/mobileDetect'
import { swAlert } from '@/utils/sweetAlert'

const mobile = isMobile()

const route = useRoute()
const router = useRouter()
const bookId = route.params.id
const book = ref(null)
const loading = ref(true)
const error = ref(null)
const bookImg = ref(null)
const overLay = ref(null)
const isWishlisted = ref(false) // 찜하기 상태 추가
const isAdmin = ref(false) // 운영자 여부
const isFullAdmin = ref(false) // 전체 관리자 여부 (캠퍼스가 없는 관리자)
const isGuest = ref(false) // 비회원 여부
const showCampusInfo = ref(false) // 캠퍼스 정보 표시 여부

const handleImageError = (event) => {
    event.target.src = noImage
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

const handleBorrowOrReturn = async () => {
    if (mobile) {
        await swAlert('PC에서만 이용 가능한 기능입니다.', 'info')
        return
    }
    // 로그인 체크
    if (!sessionStorage.getItem('userType')) {
        await swAlert('로그인이 필요합니다.', 'info')
        router.push('/login')
        return
    }

    if (book.value.borrowedByMe) {
        // 내가 대출한 경우 - 반납 페이지로 이동
        router.push('/return')
    } else if (book.value.bookBorrowed) {
        // 다른 사람이 대출한 경우
        await swAlert('이 도서는 현재 대출 중입니다.', 'info')
        return
    } else {
        // 대출 가능한 경우 - 대출 페이지로 이동
        router.push('/borrow')
    }
}

const handleWishlist = async () => {
    try {
        if (!sessionStorage.getItem('userType')) {
            await swAlert('로그인이 필요합니다.', 'info')
            router.push('/login')
            return
        }

        let response

        if (isWishlisted.value) {
            response = await favorApi.remove(book.value.seqBook)
        } else {
            response = await favorApi.add(book.value.seqBook)
        }
        
        if (response.status === 200) {
            // 상태 토글
            isWishlisted.value = !isWishlisted.value
        }
    } catch (error) {
        if (error.response) {
            const status = error.response.status
            const message = error.response?.data?.msg || '오류가 발생했습니다.'
            
            if (status === 403) {
                await swAlert(message, 'warning')
            } else if (status === 401) {
                await swAlert('로그인이 필요하거나 세션이 만료되었습니다.', 'warning')
                sessionStorage.removeItem('userType')
                sessionStorage.removeItem('campusId')
                router.push('/login')
            } else {
                await swAlert(`오류: ${message}`, 'error')
            }
        } else if (error.request) {
            await swAlert('서버와의 연결에 실패했습니다. 잠시 후 다시 시도해주세요.', 'error')
        } else {
            await swAlert('요청 처리 중 오류가 발생했습니다.', 'error')
        }
    }
}

const handleShare = async () => {
    navigator.clipboard.writeText(book.value.titleBook + ' ' + book.value.authorBook)
    await swAlert('클립보드에 복사되었습니다!', 'success')
}

// 찜하기 상태 확인 함수
const checkWishlistStatus = async () => {
    // 운영자인 경우 찜하기 상태 확인하지 않음
    if (isAdmin.value) return
    
    try {
        if (!sessionStorage.getItem('userType')) return

        const response = await favorApi.getAll()

        if (response.status === 200 && response.data.data) {
            const favorList = response.data.data
            isWishlisted.value = favorList.some(favor =>
                favor.titleBook === book.value.titleBook &&
                favor.authorBook === book.value.authorBook
            )
        }
    } catch (error) {
        if (error.response && error.response.status === 403) {
            // 비로그인 또는 유저가 아닌 경우 → 즐겨찾기 상태 확인 불가, 무시
            isWishlisted.value = false
            return
        }
        await swAlert(`찜 목록 확인 실패: ${error.message || error}`, 'error')
    }
}

// 운영자 여부 확인 함수
const checkAdminStatus = async () => {
    const userType = sessionStorage.getItem('userType')

    // 비회원 확인
    if (!userType) {
        isGuest.value = true
        isAdmin.value = false
        isFullAdmin.value = false
        showCampusInfo.value = true
        return
    }

    if (userType === 'admin') {
        isAdmin.value = true
        // 전체 관리자인지 확인
        try {
            const response = await adminApi.checkMe()

            if (response.status === 200) {
                // seqCampus가 null이면 전체 관리자
                if (!response.data.data.seqCampus) {
                    isFullAdmin.value = true
                    showCampusInfo.value = true // 전체 관리자는 캠퍼스 정보 표시
                } else {
                    isFullAdmin.value = false
                    showCampusInfo.value = false // 특정 캠퍼스 관리자는 표시하지 않음
                }
            } else {
                isAdmin.value = false
                isFullAdmin.value = false
                showCampusInfo.value = false
            }
        } catch (error) {
            isAdmin.value = false
            isFullAdmin.value = false
            showCampusInfo.value = false
        }
        return
    }

    // userType이 'user'인 경우
    try {
        const response = await adminApi.checkMe()
        
        if (response.status === 200) {
            isAdmin.value = true
            // seqCampus가 null이면 전체 관리자
            if (!response.data.seqCampus) {
                isFullAdmin.value = true
                showCampusInfo.value = true
            } else {
                isFullAdmin.value = false
                showCampusInfo.value = false
            }
        } else {
            isAdmin.value = false
            isFullAdmin.value = false
            showCampusInfo.value = false
        }
    } catch (error) {
        isAdmin.value = false
        isFullAdmin.value = false
        showCampusInfo.value = false
    }
}

onMounted(async () => {
    // 페이지 진입 시 스크롤을 맨 위로 초기화
    window.scrollTo(0, 0)
    
    // 운영자 여부 확인
    await checkAdminStatus()
    
    try {
        const res = await bookApi.getById(bookId)
        
        if (res.data.data) {
            book.value = res.data.data
            // 운영자가 아닌 경우에만 찜하기 상태 확인
            if (!isAdmin.value) {
                await checkWishlistStatus()
            }
        } else {
            error.value = '책 데이터가 없습니다.'
        }
    } catch (err) {
        error.value = `오류: ${err.message}`
    } finally {
        loading.value = false
    }
})
</script>

<style scoped>
.main {
    display: grid;
    grid-template-columns: 1fr 1fr;
    width: 98%;
    max-width: 1200px;
    padding: 2rem 1.5rem;
    margin: 0 auto;
    gap: 2rem;
}

.left-area {
    position: relative;
}

.left-area, .right-area {
    width: 100%;
    padding: 1rem;
}

.img-container {
    position: relative;
}

.img-container, .book-img {
    width: 70%;
    height: auto;
    border: 1px solid var(--pb-color-border);
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
    top: 12px;
    right: 12px;
    background: var(--pb-color-danger);
    color: #fff;
    padding: 5px 10px;
    border-radius: var(--pb-radius-sm);
    font-size: 0.75rem;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 5px;
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
    top: 12px;
    right: 12px;
    background: var(--pb-color-success);
    color: #fff;
    padding: 5px 10px;
    border-radius: var(--pb-radius-sm);
    font-size: 0.75rem;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 5px;
    z-index: 11;
}

.my-borrowed-text {
    font-size: 0.75rem;
    letter-spacing: 0.5px;
}


.right-area {
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    gap: 2rem;
}

/* 대여중 알림 배너 (다른 사람이 대여한 경우) */
.borrowed-alert {
    background: var(--pb-color-danger-soft);
    border: 1px solid var(--pb-color-danger);
    border-radius: var(--pb-radius-md);
    padding: 12px 16px;
    display: flex;
    align-items: center;
    gap: 10px;
    color: var(--pb-color-danger);
    font-weight: 600;
}

.borrowed-alert svg {
    flex-shrink: 0;
}

/* 내가 대여중인 경우 알림 배너 */
.my-borrowed-alert {
    background: var(--pb-color-success-soft);
    border: 1px solid var(--pb-color-success);
    border-radius: var(--pb-radius-md);
    padding: 12px 16px;
    display: flex;
    align-items: center;
    gap: 10px;
    color: var(--pb-color-success);
    font-weight: 600;
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
    color: var(--pb-color-text-muted);
    margin-bottom: 4px;
}

.publisher {
    font-size: 12px;
    color: var(--pb-color-text-soft);
    margin-bottom: 4px;
}

.publish-date {
    font-size: 12px;
    color: var(--pb-color-text-soft);
}

/* 오른쪽 영역 스타일 */
.book-info {
    border-bottom: 1px solid var(--pb-color-border);
    padding-bottom: 1.5rem;
}

.book-title {
    font-size: 2rem;
    font-weight: bold;
    color: var(--pb-color-heading);
    margin-bottom: 0.5rem;
    line-height: 1.2;
}

.book-author {
    font-size: 1.25rem;
    color: var(--pb-color-text-muted);
    margin-bottom: 0.25rem;
}

.book-publisher {
    font-size: 1rem;
    color: var(--pb-color-text-soft);
    margin-bottom: 0.25rem;
}

.book-date {
    font-size: 0.95rem;
    color: var(--pb-color-text-soft);
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
    color: var(--pb-color-text);
    min-width: 80px;
}

.value {
    font-family: 'Courier New', monospace;
    color: var(--pb-color-heading);
    background-color: var(--pb-color-surface-subtle);
    padding: 0.25rem 0.5rem;
    border-radius: 0.25rem;
    font-size: 0.9rem;
}

.campus-value {
    font-family: inherit;
    color: var(--pb-color-brand);
    background-color: var(--pb-color-brand-soft);
    font-weight: 600;
}

.status-borrowed {
    background: var(--pb-color-danger-soft);
    color: var(--pb-color-danger);
    font-weight: 600;
}

.status-available {
    background: var(--pb-color-brand-soft);
    color: var(--pb-color-brand);
    font-weight: 600;
}

.status-my-borrowed {
    background: var(--pb-color-success-soft);
    color: var(--pb-color-success);
    font-weight: 600;
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
    padding: 0.625rem 1.25rem;
    border: 1px solid transparent;
    border-radius: var(--pb-radius-sm);
    font-weight: 600;
    cursor: pointer;
    transition: background 0.15s ease, opacity 0.15s ease;
    font-size: 0.9rem;
}

.btn:disabled {
    cursor: not-allowed;
    opacity: 0.5;
}

.btn-icon {
    width: 1.125rem;
    height: 1.125rem;
}

.btn-primary {
    background: var(--pb-color-brand);
    color: #fff;
    border-color: var(--pb-color-brand);
}

.btn-primary:hover:not(:disabled) {
    background: var(--pb-color-brand-strong);
}

.btn-return {
    background: var(--pb-color-success);
    color: #fff;
    border-color: var(--pb-color-success);
}

.btn-return:hover {
    opacity: 0.88;
}

.btn-disabled {
    background: var(--pb-color-danger-soft);
    color: var(--pb-color-danger);
    border-color: var(--pb-color-danger);
}

.btn-secondary {
    background: var(--pb-color-surface-muted);
    color: var(--pb-color-text);
    border-color: var(--pb-color-border);
}

.btn-secondary:hover {
    background: var(--pb-color-border);
}

.btn-wishlisted {
    background: #c4688b;
    color: #fff;
    border-color: #c4688b;
}

.btn-wishlisted:hover {
    opacity: 0.88;
}

/* 대여중일 때 추가 정보 (다른 사람이 대여한 경우에만) */
.borrowed-info {
    background: var(--pb-color-surface-muted);
    border: 1px solid var(--pb-color-border);
    border-radius: var(--pb-radius-md);
    padding: 16px;
}

.borrowed-info h3 {
    color: var(--pb-color-text);
    font-size: 1rem;
    font-weight: 600;
    margin-bottom: 10px;
}

.borrowed-info ul {
    list-style: none;
    padding: 0;
    margin: 0;
}

.borrowed-info li {
    color: var(--pb-color-text-muted);
    margin-bottom: 6px;
    font-size: 0.9rem;
}

/* 내가 대여중일 때 추가 정보 */
.my-borrowed-info {
    background: var(--pb-color-success-soft);
    border: 1px solid var(--pb-color-success);
    border-radius: var(--pb-radius-md);
    padding: 16px;
}

.my-borrowed-info h3 {
    color: var(--pb-color-success);
    font-size: 1rem;
    font-weight: 600;
    margin-bottom: 10px;
}

.my-borrowed-info ul {
    list-style: none;
    padding: 0;
    margin: 0;
}

.my-borrowed-info li {
    color: var(--pb-color-success);
    margin-bottom: 6px;
    font-size: 0.9rem;
    opacity: 0.85;
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