<template>
    <div class="terms-component">
        <!-- 약관 헤더 -->
        <div class="terms-header">
            <div class="terms-title">
                <div class="title-icon">
                    📄
                </div>
                <h5>개인정보 수집·이용 동의서</h5>
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
                        <h6>🔒 개인정보 처리 요약</h6>
                        <div class="info-grid">
                            <div class="info-item">
                                <div class="info-label">수집 항목</div>
                                <div class="info-value">이름, 아이디, 비밀번호, 디스코드 아이디</div>
                            </div>
                            <div class="info-item">
                                <div class="info-label">수집 목적</div>
                                <div class="info-value">회원 관리 및 도서 대여 서비스 제공</div>
                            </div>
                            <div class="info-item">
                                <div class="info-label">보유 기간</div>
                                <div class="info-value">회원 탈퇴 시까지</div>
                            </div>
                            <div class="info-item">
                                <div class="info-label">거부 권리</div>
                                <div class="info-value">동의 거부 가능 (단, 서비스 이용 제한)</div>
                            </div>
                        </div>
                        <div class="summary-highlight">
                            <strong>🔔 알림 서비스:</strong> 디스코드를 통해 반납 기한 알림을 발송합니다.
                        </div>
                    </div>

                    <div class="terms-full" v-else>
                        <h6>본인은 도서 대여 서비스 제공을 위하여 아래와 같은 개인정보를 수집·이용하는 것에 동의합니다.</h6>
                        
                        <div class="terms-section">
                            <h7>1. 수집 항목</h7>
                            <div class="collection-items">
                                <div class="item-tag">이름</div>
                                <div class="item-tag">아이디(ID)</div>
                                <div class="item-tag">비밀번호</div>
                                <div class="item-tag discord">디스코드 아이디</div>
                            </div>
                        </div>

                        <div class="terms-section">
                            <h7>2. 수집·이용 목적</h7>
                            <ul class="purpose-list">
                                <li>
                                    <span class="purpose-icon">👤</span>
                                    <span>회원 식별 및 관리</span>
                                </li>
                                <li>
                                    <span class="purpose-icon">📚</span>
                                    <span>도서 대여 및 반납 서비스 제공</span>
                                </li>
                                <li class="highlight-purpose">
                                    <span class="purpose-icon">🔔</span>
                                    <span>디스코드 아이디를 통한 반납 기한 알림 메시지 발송</span>
                                </li>
                            </ul>
                        </div>

                        <div class="terms-section">
                            <h7>3. 보유 및 이용 기간</h7>
                            <div class="retention-info">
                                <div class="retention-item">
                                    <div class="retention-icon">⏰</div>
                                    <div class="retention-content">
                                        <strong>기본 보유 기간</strong>
                                        <p>회원 탈퇴 시까지 보유 및 이용</p>
                                    </div>
                                </div>
                                <div class="retention-item">
                                    <div class="retention-icon">⚖️</div>
                                    <div class="retention-content">
                                        <strong>법령에 따른 보존</strong>
                                        <p>관련 법령에 따라 보존할 필요가 있는 경우 해당 법령에 따름</p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="terms-section warning">
                            <h7>4. 동의를 거부할 권리 및 불이익</h7>
                            <div class="warning-content">
                                <div class="warning-item">
                                    <span class="warning-icon">✅</span>
                                    <span>귀하는 개인정보 수집·이용에 동의하지 않을 수 있습니다.</span>
                                </div>
                                <div class="warning-item important">
                                    <span class="warning-icon">⚠️</span>
                                    <span>단, 필수 항목에 대한 동의를 거부할 경우, 서비스 이용이 제한될 수 있습니다.</span>
                                </div>
                            </div>
                        </div>

                        <div class="final-notice">
                            <strong>▶ [필수] 위 내용을 충분히 읽고 이해하였으며, 개인정보 수집·이용에 동의합니다.</strong>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 동의 체크박스 -->
        <div class="agreement-section">
            <label class="agreement-checkbox" for="agreeInfoUserCheck">
                <input 
                    type="checkbox" 
                    id="agreeInfoUserCheck" 
                    class="checkbox-input"
                    :checked="isInfoAgree" 
                    @change="$emit('update:isInfoAgree', $event.target.checked)"
                >
                <div class="checkbox-custom">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <polyline points="20,6 9,17 4,12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <span class="checkbox-label">개인정보 수집·이용에 동의합니다.</span>
            </label>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  isInfoAgree: {
    type: Boolean,
    required: true
  }
})

defineEmits(['update:isInfoAgree'])

const isFullView = ref(false)

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
  margin-bottom: 1.5rem;
}

.info-grid {
  display: grid;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.info-item {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 1rem;
}

.info-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 0.5rem;
}

.info-value {
  font-size: 0.9rem;
  color: #475569;
  line-height: 1.5;
}

.summary-highlight {
  background: #dbeafe;
  border: 1px solid #93c5fd;
  border-radius: 8px;
  padding: 1rem;
  color: #1e40af;
  font-size: 0.9rem;
  line-height: 1.5;
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

.terms-section.warning {
  background: #fef7f0;
  border: 1px solid #fed7aa;
  border-radius: 8px;
  padding: 1rem;
  margin: 1rem 0;
}

.terms-section h7 {
  display: block;
  font-size: 0.95rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.75rem;
}

.collection-items {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.item-tag {
  background: #e2e8f0;
  color: #475569;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
}

.item-tag.discord {
  background: #5865f2;
  color: white;
}

.purpose-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.purpose-list li {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  margin-bottom: 0.5rem;
  background: #f8fafc;
  border-radius: 6px;
  font-size: 0.9rem;
  color: #475569;
}

.purpose-list li.highlight-purpose {
  background: #dbeafe;
  border: 1px solid #93c5fd;
  color: #1e40af;
}

.purpose-icon {
  font-size: 1.1rem;
  flex-shrink: 0;
}

.retention-info {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.retention-item {
  display: flex;
  gap: 1rem;
  background: #f8fafc;
  border-radius: 8px;
  padding: 1rem;
}

.retention-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.retention-content strong {
  display: block;
  font-size: 0.9rem;
  color: #1e293b;
  margin-bottom: 0.25rem;
}

.retention-content p {
  font-size: 0.875rem;
  color: #64748b;
  margin: 0;
  line-height: 1.4;
}

.warning-content {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.warning-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  font-size: 0.9rem;
  line-height: 1.5;
}

.warning-item.important {
  color: #dc2626;
  font-weight: 500;
}

.warning-icon {
  flex-shrink: 0;
  margin-top: 0.1rem;
}

.final-notice {
  background: #ecfdf5;
  border: 1px solid #10b981;
  border-radius: 8px;
  padding: 1rem;
  color: #065f46;
  font-size: 0.9rem;
  line-height: 1.5;
  margin-top: 1.5rem;
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
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .retention-info {
    gap: 0.75rem;
  }
  
  .retention-item {
    flex-direction: column;
    gap: 0.5rem;
    text-align: center;
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
  
  .collection-items {
    flex-direction: column;
  }
  
  .item-tag {
    text-align: center;
  }
}
</style>