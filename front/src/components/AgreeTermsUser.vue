<template>
    <div class="terms-component">
        <div class="terms-header">
            <span class="terms-title">플레이북 이용약관</span>
            <span class="required-badge">필수</span>
        </div>
        <div class="terms-box">
            <div v-if="termsContent" v-html="termsContent"></div>
            <div v-else class="loading-text">약관을 불러오는 중...</div>
        </div>
        <div class="agreement-section">
            <label class="agreement-checkbox" for="agreeTermsUserCheck">
                <input
                    type="checkbox"
                    id="agreeTermsUserCheck"
                    class="checkbox-input"
                    :checked="isTermsAgree"
                    @change="$emit('update:isTermsAgree', $event.target.checked)"
                >
                <div class="checkbox-custom">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                        <polyline points="20,6 9,17 4,12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <span class="checkbox-label">플레이북 이용약관에 동의합니다.</span>
            </label>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as termsApi from '@/api/terms'

defineProps({ isTermsAgree: { type: Boolean, required: true } })
defineEmits(['update:isTermsAgree'])

const termsContent = ref('')

onMounted(async () => {
    try {
        const res = await termsApi.getByType('SERVICE')
        termsContent.value = res.data.data?.content || ''
    } catch {}
})
</script>

<style scoped>
.terms-component {
    border: 1px solid var(--pb-color-border);
    border-radius: var(--pb-radius-md);
    overflow: hidden;
    background: var(--pb-color-surface);
}

.terms-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 14px;
    border-bottom: 1px solid var(--pb-color-border);
    background: var(--pb-color-surface-subtle);
}

.terms-title {
    font-size: 0.875rem;
    font-weight: 600;
    color: var(--pb-color-heading);
}

.required-badge {
    font-size: 0.72rem;
    font-weight: 600;
    color: var(--pb-color-danger);
    background: var(--pb-color-danger-soft);
    padding: 2px 6px;
    border-radius: var(--pb-radius-xs);
}

.terms-box {
    height: 140px;
    overflow-y: auto;
    padding: 12px 14px;
    font-size: 0.85rem;
    line-height: 1.6;
    color: var(--pb-color-text);
}

.terms-box::-webkit-scrollbar { width: 5px; }
.terms-box::-webkit-scrollbar-thumb { background: var(--pb-color-border); border-radius: 3px; }
.terms-box::-webkit-scrollbar-track { background: transparent; }

.loading-text {
    color: var(--pb-color-text-soft);
    font-size: 0.85rem;
}

.agreement-section {
    padding: 10px 14px;
    border-top: 1px solid var(--pb-color-border);
    background: var(--pb-color-surface-subtle);
}

.agreement-checkbox {
    display: flex;
    align-items: center;
    cursor: pointer;
    gap: 8px;
}

.checkbox-input { display: none; }

.checkbox-custom {
    width: 18px;
    height: 18px;
    border: 1px solid var(--pb-color-border-strong);
    border-radius: var(--pb-radius-xs);
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--pb-color-surface);
    flex-shrink: 0;
    transition: background 0.12s, border-color 0.12s;
}

.checkbox-input:checked + .checkbox-custom {
    background: var(--pb-color-brand);
    border-color: var(--pb-color-brand);
    color: white;
}

.checkbox-input:not(:checked) + .checkbox-custom svg { opacity: 0; }
.checkbox-input:checked + .checkbox-custom svg { opacity: 1; }

.checkbox-label {
    font-size: 0.875rem;
    font-weight: 500;
    color: var(--pb-color-text);
    user-select: none;
}
</style>
