<template>
    <router-link
      :to="{ name: 'BookInfo', params: { id: book.seqBook } }"
      class="book-area"
      :class="{ 'borrowed': book.bookBorrowed }"
    >
        <div class="isBooked">
            <div class="img-area">
                <img 
                    :src="book.imageBook && book.imageBook.trim() !== '' ? book.imageBook : noImage" 
                    :alt="book.titleBook"
                    @error="handleImageError"
                />
                
                <!-- 대여중 오버레이 -->
                <div v-if="book.bookBorrowed" class="borrowed-overlay">
                    <div class="borrowed-badge">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" fill="currentColor"/>
                            <path d="M9 12L11 14L15 10" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                        <span class="borrowed-text">대여중</span>
                    </div>
                    <div class="borrowed-dimmer"></div>
                </div>
            </div>
            <div class="book-info">
                <div class="title">{{ book.titleBook }}</div>
                <div class="book-info-footer">
                    <div class="author">{{ book.authorBook }}</div>
                </div>
            </div>
        </div>
    </router-link>
</template>

<script setup>
import { defineProps } from 'vue'
import noImage from '@/assets/free-icon-no-image-11542598.png'

const props = defineProps({
    book: {
        type: Object,
        required: true,
        default: () => ({})
    }
})

const handleImageError = (event) => {
    event.target.src = noImage
}
</script>

<style scoped>
.book-area {
    background: #EDEFEF;
    border-radius: 8px;
    height: auto;
    text-decoration: none;
    transition: all 0.3s ease;
}

.book-area:hover .title {
    text-decoration: underline;
}

.book-area.borrowed {
    position: relative;
}

.book-area.borrowed::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    border-radius: 8px 8px 0 0;
    z-index: 5;
}

.isBooked {
    display: flex; 
    flex-direction: column;
    height: auto;
    position: relative;
}

.img-area {
    width: 100%;
    height: 430px;
    background-color: #f8f9fa;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0px 3px 8px rgba(50, 50, 50, 0.1);
    position: relative;
    overflow: hidden;
}

.img-area img {
    width: 100%;
    height: auto;
    object-fit: cover;
    transition: transform 0.3s ease;
}

.book-area:hover .img-area img {
    transform: scale(1.02);
}

/* 대여중 오버레이 스타일 */
.borrowed-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 10;
}

.borrowed-dimmer {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.4);
    backdrop-filter: blur(2px);
}

.borrowed-badge {
    position: absolute;
    top: 12px;
    right: 12px;
    background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
    color: white;
    padding: 6px 10px;
    border-radius: 16px;
    font-size: 0.75rem;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 4px;
    box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
    animation: pulse 2s infinite;
    z-index: 11;
}

@keyframes pulse {
    0%, 100% {
        box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
    }
    50% {
        box-shadow: 0 4px 16px rgba(239, 68, 68, 0.6);
        transform: scale(1.02);
    }
}

.borrowed-text {
    font-size: 0.7rem;
    letter-spacing: 0.5px;
}

.book-info {
    padding: 0.8rem 0 0.8rem 0;
    flex-grow: 1;
    display: flex;
    flex-direction: column;
    justify-content: end;
    gap: 0.5rem;
}

.book-info-footer {
    display: flex;
    flex-direction: row;
    justify-content: flex-start;
}

.title {
    font-size: 1.1rem;
    font-weight: bold;
    color: #323232;
    line-height: 1.4;
    letter-spacing: -0.025rem;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    transition: color 0.3s ease;
}

.author {
    font-size: 0.9rem;
    color: #323232;
    font-weight: 500;
    letter-spacing: -0.025rem;
    transition: color 0.3s ease;
}

.book-area.borrowed:hover .borrowed-badge {
    transform: scale(1.05);
    box-shadow: 0 6px 20px rgba(239, 68, 68, 0.5);
}

/* 반응형 디자인 */
@media (max-width: 768px) {
    .borrowed-badge {
        top: 8px;
        right: 8px;
        padding: 4px 8px;
        font-size: 0.7rem;
    }
    
    .borrowed-text {
        font-size: 0.65rem;
    }
}
</style>