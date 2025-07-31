<template>
    <router-link
      :to="{ name: 'BookInfo', params: { id: book.seqBook } }"
      class="book-area"
    >
        <div class="isBooked">
            <div class="img-area">
                <img 
                    :src="book.imageBook && book.imageBook.trim() !== '' ? book.imageBook : noImage" 
                    :alt="book.titleBook"
                    @error="handleImageError"
                />
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

</script>

<style scoped>
.book-area {
    background: #EDEFEF;
    border-radius: 8px;
    height: auto;
    text-decoration: none;
}
.book-area:hover .title {
  text-decoration: underline;
}
.isBooked {
    display: flex; 
    flex-direction: column;
    height: auto;
}

.img-area {
    width: 100%;
    height: auto;
    background-color: #f8f9fa;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0px 3px 8px rgba(50, 50, 50, 0.1);
}

.img-area img {
    width: 100%;
    height: auto;
    object-fit: contain;
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
}
.author {
    font-size: 0.9rem;
    color: #323232;
    font-weight: 500;
    letter-spacing: -0.025rem;
}
</style>