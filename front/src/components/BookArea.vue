<template>
    <div class="book-area">
        <div class="isBooked">
            <div class="img-area">
                <img 
                    :src="book.imageBook || '/default-book-image.jpg'" 
                    :alt="book.titleBook"
                    @error="handleImageError"
                >
            </div>
            <div class="book-info">
                <div class="title">{{ book.titleBook }}</div>
                <div class="book-info-footer">
                    <div class="author">{{ book.authorBook }}</div>
                    <div class="publisher">{{ book.publisherBook }}</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { defineProps } from 'vue'

const props = defineProps({
    book: {
        type: Object,
        required: true,
        default: () => ({})
    }
})

// 이미지 로드 실패시 기본 이미지로 대체
const handleImageError = (event) => {
    event.target.src = '/default-book-image.jpg'
}

</script>

<style scoped>
.book-area {
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    transition: transform 0.3s ease, box-shadow 0.3s ease;
    height: 100%;
}

.book-area:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.isBooked {
    display: flex;
    flex-direction: column;
    height: 100%;
}

.img-area {
    width: 100%;
    height: 200px;
    overflow: hidden;
    background-color: #f8f9fa;
    display: flex;
    align-items: center;
    justify-content: center;
}

.img-area img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
}

.book-area:hover .img-area img {
    transform: scale(1.05);
}

.book-info {
    padding: 0 0.8rem 0.8rem 0.8rem;
    flex-grow: 1;
    display: flex;
    flex-direction: column;
    justify-content: end;
    gap: 0.5rem;
}
.book-info-footer {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
}
.title {
    font-size: 1.1rem;
    font-weight: bold;
    color: #333;
    line-height: 1.4;
    margin-bottom: 0.5rem;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
}

.author {
    font-size: 0.9rem;
    color: #666;
    font-weight: 500;
}
</style>