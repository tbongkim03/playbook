<template>
    <!-- 카테고리 리스트 -->
    <nav class="bg-light px-4 py-2 fixed-top w-100" style="top: 72px; z-index: 1030;">
        <ul class="nav">
            <li class="nav-item" v-for="(category, index) in categories" :key="index">
                <a class="nav-link" :class="{ active: selectedCategory === category }" href="#"
                    @click.prevent="selectCategory(category)">
                    {{ category }}
                </a>
            </li>
        </ul>
    </nav>

    <!-- 본문 영역 -->
    <main style="padding-top: 140px;">
        <h3>{{ selectedCategory }} 카테고리 콘텐츠</h3>
    </main>
</template>

<script setup>
// 필요한 Vue 기능을 여기에 import 해서 사용
import { ref } from 'vue'

const categories = ['일반', '컴퓨터일반', '웹/앱', '데이터베이스/빅데이터/분석/엔지니어링', '클라우드/데브옵스', '인공지능', '기타']
const selectedCategory = ref(categories[0])

function selectCategory(category) {
    selectedCategory.value = category
}
</script>

<style scoped>
.nav-link {
    font-weight: bold;
    color: #555;
    cursor: pointer;
    margin: 0 1vw;
}

.nav-link.active {
    font-weight: bold;
    color: #42bbb2;
    border-bottom: 2px solid #42bbb2;
}
</style>
