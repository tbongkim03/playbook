<template>
    <div class="terms-page">
        <div class="terms-header">
            <div class="breadcrumb">
                <router-link to="/">홈</router-link>
                <span> / </span>
                <span>플레이북 이용약관</span>
            </div>
            <h1>플레이북 이용약관</h1>
            <p class="description">도서 대출 서비스를 이용하기 위한 이용약관입니다. 서비스 이용 전 반드시 확인해 주세요.</p>
            <p class="last-updated">최종 업데이트: 2025년 5월 9일</p>
        </div>

        <div class="terms-body">
            <div v-if="dynamicContent" v-html="dynamicContent" class="terms-content"></div>
            <div v-else class="loading">약관을 불러오는 중...</div>
        </div>

        <div class="terms-footer">
            <button class="btn-back" @click="goBack">← 이전으로</button>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const dynamicContent = ref(null)

onMounted(async () => {
    try {
        const res = await axios.get('/api/terms/SERVICE')
        const content = res.data.data?.content
        if (content && content.trim()) dynamicContent.value = content
    } catch {
        // 로드 실패 시 로딩 메시지 유지
    }
})

function goBack() {
    router.back()
}
</script>

<style scoped>
.terms-page {
    max-width: 800px;
    margin: 80px auto 60px;
    padding: 0 24px;
    font-family: 'Noto Sans KR', sans-serif;
    color: #2d3748;
    line-height: 1.8;
}

.terms-header {
    margin-bottom: 32px;
    padding-bottom: 24px;
    border-bottom: 1px solid #e2e8f0;
}

.breadcrumb {
    font-size: 0.85rem;
    color: #718096;
    margin-bottom: 12px;
}

.breadcrumb a {
    color: #4a90d9;
    text-decoration: none;
}

.breadcrumb a:hover {
    text-decoration: underline;
}

.terms-header h1 {
    font-size: 1.8rem;
    font-weight: 700;
    margin: 0 0 8px;
    color: #1a202c;
}

.description {
    font-size: 0.95rem;
    color: #718096;
    margin: 0 0 8px;
}

.last-updated {
    font-size: 0.85rem;
    color: #a0aec0;
}

.terms-body {
    min-height: 200px;
}

.terms-content {
    font-size: 0.95rem;
}

.loading {
    color: #a0aec0;
    padding: 40px 0;
    text-align: center;
}

.terms-footer {
    margin-top: 48px;
    padding-top: 24px;
    border-top: 1px solid #e2e8f0;
}

.btn-back {
    background: none;
    border: 1px solid #cbd5e0;
    color: #4a5568;
    padding: 8px 20px;
    border-radius: 6px;
    font-size: 0.9rem;
    cursor: pointer;
    transition: all 0.2s;
}

.btn-back:hover {
    background: #f7fafc;
    border-color: #a0aec0;
}
</style>
