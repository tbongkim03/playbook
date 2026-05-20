<template>
    <div class="terms-component">
        <!-- 약관 헤더 -->
        <div class="terms-header">
            <div class="terms-title">
                <div class="title-icon">
                    📜
                </div>
                <h5>플레이북 이용약관</h5>
                <span class="required-badge">필수</span>
            </div>
            <button class="view-all-btn" @click="toggleFullView">
                <span>{{ isFullView ? '요약보기' : '전체보기' }}</span>
                <svg 
                    width="16" 
                    height="16" 
                    viewBox="0 0 24 24" 
                    fill="none" 
                    xmlns="http://www.w3.org/2000/svg"
                    :class="{ rotated: isFullView }"
                >
                    <polyline points="6,9 12,15 18,9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            </button>
        </div>

        <!-- 약관 내용 -->
        <div class="terms-content" :class="{ expanded: isFullView }">
            <div class="terms-box">
                <div class="article">
                    <div class="terms-summary" v-if="!isFullView">
                        <h6>📋 주요 내용 요약</h6>
                        <ul class="summary-list">
                            <li>도서 대여 서비스 이용 조건 및 절차</li>
                            <li>회원의 권리와 의무</li>
                            <li>회원 상태: 정상/연체/정지 (정지 상태는 대출/반납 불가)</li>
                            <li>대출 기간: 7일 (최대 2권까지 동시 대출 가능)</li>
                            <li>연체 시 제재 사항 (연체일의 2배 기간 대출 제한)</li>
                            <li>도서 분실 및 훼손 시 배상 책임</li>
                            <li>회원 탈퇴 (대출중/연체중인 경우 탈퇴 불가)</li>
                        </ul>
                        <p class="summary-note">
                            <strong>※ 중요:</strong> 대출한 도서는 대출일로부터 7일 이내 반납해야 하며, 
                            연체 시 추가 대출이 제한됩니다.
                        </p>
                    </div>

                    <div class="terms-full" v-else>
                        <div v-if="termsContent" v-html="termsContent"></div>
                        <div v-else style="color:#a0aec0; padding: 12px 0;">약관을 불러오는 중...</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 동의 체크박스 -->
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
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
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
import axios from 'axios'

defineProps({
  isTermsAgree: {
    type: Boolean,
    required: true
  }
})

defineEmits(['update:isTermsAgree'])

const isFullView = ref(false)
const termsContent = ref('')

onMounted(async () => {
  try {
    const res = await axios.get('/api/terms/SERVICE')
    termsContent.value = res.data.data?.content || ''
  } catch {
    // 로드 실패 시 빈 상태 유지
  }
})

function toggleFullView() {
  isFullView.value = !isFullView.value
}
</script>

<style scoped>
.terms-component {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.terms-component:hover {
  border-color: #cbd5e1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.terms-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.25rem;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.terms-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.title-icon {
  font-size: 1.25rem;
}

.terms-title h5 {
  font-size: 1rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.required-badge {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
}

.view-all-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: transparent;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  color: #64748b;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.view-all-btn:hover {
  background: #f1f5f9;
  border-color: #94a3b8;
  color: #475569;
}

.view-all-btn svg {
  transition: transform 0.3s ease;
}

.view-all-btn svg.rotated {
  transform: rotate(180deg);
}

.terms-content {
  max-height: 300px;
  overflow: hidden;
  transition: max-height 0.3s ease;
}

.terms-content.expanded {
  max-height: 600px;
}

.terms-box {
  padding: 1.25rem;
  overflow-y: auto;
  height: 100%;
}

.terms-box::-webkit-scrollbar {
  width: 6px;
}

.terms-box::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 3px;
}

.terms-box::-webkit-scrollbar-track {
  background-color: #f1f5f9;
  border-radius: 3px;
}

.article h6 {
  font-size: 0.95rem;
  font-weight: 600;
  color: #374151;
  line-height: 1.5;
  margin: 0 0 1rem 0;
  padding: 0.75rem;
  background: #f0f9ff;
  border-left: 4px solid #0ea5e9;
  border-radius: 4px;
}

.terms-summary {
  padding: 0.5rem 0;
}

.terms-summary h6 {
  font-size: 1rem;
  color: #1e293b;
  margin-bottom: 1rem;
}

.summary-list {
  list-style: none;
  padding: 0;
  margin: 0 0 1rem 0;
}

.summary-list li {
  position: relative;
  padding-left: 1.5rem;
  margin-bottom: 0.5rem;
  color: #475569;
  line-height: 1.5;
}

.summary-list li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #10b981;
  font-weight: bold;
}

.summary-note {
  background: #fef3c7;
  border: 1px solid #f59e0b;
  border-radius: 6px;
  padding: 0.75rem;
  margin: 1rem 0 0 0;
  font-size: 0.9rem;
  line-height: 1.5;
  color: #92400e;
}

.terms-section {
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f1f5f9;
}

.terms-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.terms-section.highlight {
  background: #fef7f0;
  border: 1px solid #fed7aa;
  border-radius: 6px;
  padding: 1rem;
  margin: 1rem 0;
}

.terms-section h7 {
  display: block;
  font-size: 0.95rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.5rem;
}

.terms-section p {
  font-size: 0.9rem;
  line-height: 1.6;
  color: #475569;
  margin: 0;
}

.agreement-section {
  padding: 1.25rem;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
}

.agreement-checkbox {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.agreement-checkbox:hover {
  color: #374151;
}

.checkbox-input {
  display: none;
}

.checkbox-custom {
  width: 20px;
  height: 20px;
  border: 2px solid #cbd5e1;
  border-radius: 4px;
  margin-right: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  background: white;
  flex-shrink: 0;
}

.checkbox-input:checked + .checkbox-custom {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  color: white;
  transform: scale(1.05);
}

.checkbox-input:not(:checked) + .checkbox-custom svg {
  opacity: 0;
  transform: scale(0.5);
}

.checkbox-input:checked + .checkbox-custom svg {
  opacity: 1;
  transform: scale(1);
}

.checkbox-custom svg {
  transition: all 0.3s ease;
}

.checkbox-label {
  font-size: 0.95rem;
  font-weight: 500;
  color: #374151;
  user-select: none;
  line-height: 1.5;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .terms-header {
    flex-direction: column;
    gap: 0.75rem;
    align-items: flex-start;
  }
  
  .terms-title {
    flex-wrap: wrap;
  }
  
  .view-all-btn {
    align-self: flex-end;
  }
  
  .terms-content {
    max-height: 250px;
  }
  
  .terms-content.expanded {
    max-height: 500px;
  }
  
  .terms-box {
    padding: 1rem;
  }
  
  .agreement-section {
    padding: 1rem;
  }
}

@media (max-width: 480px) {
  .terms-title h5 {
    font-size: 0.9rem;
  }
  
  .checkbox-label {
    font-size: 0.9rem;
  }
  
  .terms-section h7 {
    font-size: 0.9rem;
  }
  
  .terms-section p {
    font-size: 0.85rem;
  }
}
</style>