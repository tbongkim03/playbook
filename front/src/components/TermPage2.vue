<template>
    <div class="terms-page">
        <div class="terms-header">
            <div class="breadcrumb">
                <router-link to="/">홈</router-link>
                <span> / </span>
                <span>개인정보처리방침</span>
            </div>
            <h1>개인정보 수집·이용 동의서</h1>
            <p class="description">도서 대출 서비스 제공을 위한 개인정보 수집 및 이용에 관한 동의서입니다. 개인정보 보호를 위해 반드시 확인해 주세요.</p>
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
import * as termsApi from '@/api/terms'

const router = useRouter()
const dynamicContent = ref(null)

onMounted(async () => {
    try {
        const res = await termsApi.getByType('PRIVACY')
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
    color: var(--pb-color-text);
    line-height: 1.8;
}

.terms-header {
    margin-bottom: 32px;
    padding-bottom: 24px;
    border-bottom: 1px solid var(--pb-color-border);
}

.breadcrumb {
    font-size: 0.85rem;
    color: var(--pb-color-text-muted);
    margin-bottom: 12px;
}

.breadcrumb a {
    color: var(--pb-color-brand);
    text-decoration: none;
}

.breadcrumb a:hover {
    text-decoration: underline;
}

.terms-header h1 {
    font-size: 1.8rem;
    font-weight: 700;
    margin: 0 0 8px;
    color: var(--pb-color-heading);
}

.description {
    font-size: 0.95rem;
    color: var(--pb-color-text-muted);
    margin: 0 0 8px;
}

.last-updated {
    font-size: 0.85rem;
    color: var(--pb-color-text-soft);
}

.terms-body {
    min-height: 200px;
}

.terms-content {
    font-size: 0.95rem;
}

.loading {
    color: var(--pb-color-text-soft);
    padding: 40px 0;
    text-align: center;
}

.terms-footer {
    margin-top: 48px;
    padding-top: 24px;
    border-top: 1px solid var(--pb-color-border);
}

.btn-back {
    background: none;
    border: 1px solid var(--pb-color-border);
    color: var(--pb-color-text);
    padding: 8px 20px;
    border-radius: var(--pb-radius-xs);
    font-size: 0.9rem;
    cursor: pointer;
    transition: background 0.15s, border-color 0.15s;
}

.btn-back:hover {
    background: var(--pb-color-surface-subtle);
    border-color: var(--pb-color-border-strong);
}
</style>
