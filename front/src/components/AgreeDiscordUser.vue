<template>
    <div class="terms-component">
        <!-- 약관 헤더 -->
        <div class="terms-header">
            <div class="terms-title">
                <div class="title-icon">
                    🔔
                </div>
                <h5>도서 대여 및 반납 알림 수신 동의서</h5>
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
                        <h6>🔔 알림 수신 서비스 요약</h6>
                        <div class="notification-overview">
                            <div class="discord-info">
                                <div class="discord-logo">
                                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M20.317 4.3698a19.7913 19.7913 0 00-4.8851-1.5152.0741.0741 0 00-.0785.0371c-.211.3753-.4447.8648-.6083 1.2495-1.8447-.2762-3.68-.2762-5.4868 0-.1636-.3933-.4058-.8742-.6177-1.2495a.077.077 0 00-.0785-.037 19.7363 19.7363 0 00-4.8852 1.515.0699.0699 0 00-.0321.0277C.5334 9.0458-.319 13.5799.0992 18.0578a.0824.0824 0 00.0312.0561c2.0528 1.5076 4.0413 2.4228 5.9929 3.0294a.0777.0777 0 00.0842-.0276c.4616-.6304.8731-1.2952 1.226-1.9942a.076.076 0 00-.0416-.1057c-.6528-.2476-1.2743-.5495-1.8722-.8923a.077.077 0 01-.0076-.1277c.1258-.0943.2517-.1923.3718-.2914a.0743.0743 0 01.0776-.0105c3.9278 1.7933 8.18 1.7933 12.0614 0a.0739.0739 0 01.0785.0095c.1202.099.246.1981.3728.2924a.077.077 0 01-.0066.1276 12.2986 12.2986 0 01-1.873.8914.0766.0766 0 00-.0407.1067c.3604.698.7719 1.3628 1.225 1.9932a.076.076 0 00.0842.0286c1.961-.6067 3.9495-1.5219 6.0023-3.0294a.077.077 0 00.0313-.0552c.5004-5.177-.8382-9.6739-3.5485-13.6604a.061.061 0 00-.0312-.0286zM8.02 15.3312c-1.1825 0-2.1569-1.0857-2.1569-2.419 0-1.3332.9555-2.4189 2.157-2.4189 1.2108 0 2.1757 1.0952 2.1568 2.419-.0189 1.3332-.9555 2.4189-2.1569 2.4189zm7.9748 0c-1.1825 0-2.1569-1.0857-2.1569-2.419 0-1.3332.9554-2.4189 2.1569-2.4189 1.2108 0 2.1757 1.0952 2.1568 2.419 0 1.3332-.9554 2.4189-2.1568 2.4189Z" fill="currentColor"/>
                                    </svg>
                                </div>
                                <div class="discord-content">
                                    <strong>디스코드 알림 서비스</strong>
                                    <p>도서 대여/반납 관련 중요 정보를 디스코드 메시지로 받아보세요</p>
                                </div>
                            </div>
                        </div>
                        
                        <div class="notification-types">
                            <h7>알림 종류</h7>
                            <div class="notification-grid">
                                <div class="notification-item">
                                    <span class="notif-icon">📚</span>
                                    <span>대여 완료 안내</span>
                                </div>
                                <div class="notification-item">
                                    <span class="notif-icon">⏰</span>
                                    <span>반납 예정일 안내</span>
                                </div>
                                <div class="notification-item highlight">
                                    <span class="notif-icon">⚠️</span>
                                    <span>연체 발생 알림</span>
                                </div>
                            </div>
                        </div>
                        
                        <div class="summary-note">
                            <strong>📌 중요:</strong> 연체 방지와 원활한 서비스 이용을 위해 디스코드 알림 수신이 필수입니다.
                        </div>
                    </div>

                    <div class="terms-full" v-else>
                        <h6>본인은 도서 대여 및 반납 기한과 관련된 알림 메시지를 수신하는 것에 동의합니다.</h6>
                        
                        <div class="terms-section">
                            <h7>1. 수신 항목 및 수단</h7>
                            <div class="reception-info">
                                <div class="reception-items">
                                    <strong>수신 항목</strong>
                                    <div class="item-list">
                                        <div class="list-item">
                                            <span class="item-icon">📚</span>
                                            <span>도서 대여 완료 안내</span>
                                        </div>
                                        <div class="list-item">
                                            <span class="item-icon">📅</span>
                                            <span>반납 예정일 안내</span>
                                        </div>
                                        <div class="list-item highlight">
                                            <span class="item-icon">🚨</span>
                                            <span>연체 발생 알림</span>
                                        </div>
                                    </div>
                                </div>
                                
                                <div class="reception-method">
                                    <strong>수신 수단</strong>
                                    <div class="method-card">
                                        <div class="discord-badge">
                                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M20.317 4.3698a19.7913 19.7913 0 00-4.8851-1.5152.0741.0741 0 00-.0785.0371c-.211.3753-.4447.8648-.6083 1.2495-1.8447-.2762-3.68-.2762-5.4868 0-.1636-.3933-.4058-.8742-.6177-1.2495a.077.077 0 00-.0785-.037 19.7363 19.7363 0 00-4.8852 1.515.0699.0699 0 00-.0321.0277C.5334 9.0458-.319 13.5799.0992 18.0578a.0824.0824 0 00.0312.0561c2.0528 1.5076 4.0413 2.4228 5.9929 3.0294a.0777.0777 0 00.0842-.0276c.4616-.6304.8731-1.2952 1.226-1.9942a.076.076 0 00-.0416-.1057c-.6528-.2476-1.2743-.5495-1.8722-.8923a.077.077 0 01-.0076-.1277c.1258-.0943.2517-.1923.3718-.2914a.0743.0743 0 01.0776-.0105c3.9278 1.7933 8.18 1.7933 12.0614 0a.0739.0739 0 01.0785.0095c.1202.099.246.1981.3728.2924a.077.077 0 01-.0066.1276 12.2986 12.2986 0 01-1.873.8914.0766.0766 0 00-.0407.1067c.3604.698.7719 1.3628 1.225 1.9932a.076.076 0 00.0842.0286c1.961-.6067 3.9495-1.5219 6.0023-3.0294a.077.077 0 00.0313-.0552c.5004-5.177-.8382-9.6739-3.5485-13.6604a.061.061 0 00-.0312-.0286zM8.02 15.3312c-1.1825 0-2.1569-1.0857-2.1569-2.419 0-1.3332.9555-2.4189 2.157-2.4189 1.2108 0 2.1757 1.0952 2.1568 2.419-.0189 1.3332-.9555 2.4189-2.1569 2.4189zm7.9748 0c-1.1825 0-2.1569-1.0857-2.1569-2.419 0-1.3332.9554-2.4189 2.1569-2.4189 1.2108 0 2.1757 1.0952 2.1568 2.419 0 1.3332-.9554 2.4189-2.1568 2.4189Z" fill="currentColor"/>
                                            </svg>
                                            <span>디스코드 메시지</span>
                                        </div>
                                        <p>실시간으로 중요한 알림을 받아보세요</p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="terms-section">
                            <h7>2. 이용 목적</h7>
                            <div class="purpose-cards">
                                <div class="purpose-card">
                                    <div class="purpose-icon">📋</div>
                                    <div class="purpose-content">
                                        <strong>도서 대여 및 반납 기한 안내</strong>
                                        <p>정확한 대여/반납 일정을 알려드립니다</p>
                                    </div>
                                </div>
                                <div class="purpose-card highlight">
                                    <div class="purpose-icon">🛡️</div>
                                    <div class="purpose-content">
                                        <strong>연체 방지 및 원활한 서비스 운영</strong>
                                        <p>미리 알림을 통해 연체를 방지하고 서비스 품질을 향상시킵니다</p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="terms-section">
                            <h7>3. 보유 및 이용 기간</h7>
                            <div class="duration-info">
                                <div class="duration-card">
                                    <div class="duration-icon">📅</div>
                                    <div class="duration-content">
                                        <strong>알림 수신 기간</strong>
                                        <p>동의일로부터 회원 탈퇴 또는 수신 동의 철회 시까지</p>
                                        <div class="duration-note">
                                            언제든지 알림 수신을 중단할 수 있습니다
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="terms-section warning">
                            <h7>4. 동의를 거부할 권리 및 불이익</h7>
                            <div class="warning-content">
                                <div class="warning-item">
                                    <span class="warning-icon">✅</span>
                                    <span>귀하는 알림 수신에 동의하지 않을 수 있습니다.</span>
                                </div>
                                <div class="warning-item important">
                                    <span class="warning-icon">⚠️</span>
                                    <span>단, 필수 항목에 대한 동의를 거부할 경우, 서비스 이용이 제한될 수 있습니다.</span>
                                </div>
                            </div>
                        </div>

                        <div class="final-notice">
                            <strong>▶ [필수] 위 내용을 충분히 읽고 이해하였으며, 도서 대여 및 반납 알림 수신에 동의합니다.</strong>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 동의 체크박스 -->
        <div class="agreement-section">
            <label class="agreement-checkbox" for="agreeDiscordUserCheck">
                <input 
                    type="checkbox" 
                    id="agreeDiscordUserCheck" 
                    class="checkbox-input"
                    :checked="isDiscordAgree" 
                    @change="$emit('update:isDiscordAgree', $event.target.checked)"
                >
                <div class="checkbox-custom">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <polyline points="20,6 9,17 4,12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </div>
                <span class="checkbox-label">도서 대여 및 반납 알림 수신에 동의합니다.</span>
            </label>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  isDiscordAgree: {
    type: Boolean,
    required: true
  }
})

defineEmits(['update:isDiscordAgree'])

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

.notification-overview {
  margin-bottom: 1.5rem;
}

.discord-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  background: #5865f2;
  color: white;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.discord-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  flex-shrink: 0;
}

.discord-content strong {
  display: block;
  font-size: 1rem;
  margin-bottom: 0.25rem;
}

.discord-content p {
  margin: 0;
  opacity: 0.9;
  font-size: 0.9rem;
}

.notification-types h7 {
  display: block;
  font-size: 0.95rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.75rem;
}

.notification-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.notification-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: #f8fafc;
  padding: 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  color: #475569;
}

.notification-item.highlight {
  background: #fef2f2;
  border: 1px solid #fca5a5;
  color: #dc2626;
}

.notif-icon {
  flex-shrink: 0;
}

.summary-note {
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

.reception-info {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.reception-items strong {
  display: block;
  font-size: 0.9rem;
  color: #1e293b;
  margin-bottom: 0.75rem;
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.list-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: #f8fafc;
  padding: 0.75rem;
  border-radius: 6px;
  font-size: 0.9rem;
  color: #475569;
}

.list-item.highlight {
  background: #fef2f2;
  border: 1px solid #fca5a5;
  color: #dc2626;
}

.item-icon {
  flex-shrink: 0;
}

.reception-method strong {
  display: block;
  font-size: 0.9rem;
  color: #1e293b;
  margin-bottom: 0.75rem;
}

.method-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 1rem;
}

.discord-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: #5865f2;
  color: white;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  margin-bottom: 0.5rem;
}

.method-card p {
  margin: 0;
  font-size: 0.875rem;
  color: #64748b;
}

.purpose-cards {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.purpose-card {
  display: flex;
  gap: 1rem;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 1rem;
}

.purpose-card.highlight {
  background: #f0f9ff;
  border-color: #0ea5e9;
}

.purpose-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: white;
  border-radius: 8px;
  font-size: 1.25rem;
  flex-shrink: 0;
}

.purpose-content strong {
  display: block;
  font-size: 0.9rem;
  color: #1e293b;
  margin-bottom: 0.25rem;
}

.purpose-content p {
  margin: 0;
  font-size: 0.875rem;
  color: #64748b;
  line-height: 1.4;
}

.duration-info {
  display: flex;
  justify-content: center;
}

.duration-card {
  display: flex;
  gap: 1rem;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 1rem;
  max-width: 400px;
}

.duration-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: white;
  border-radius: 8px;
  font-size: 1.25rem;
  flex-shrink: 0;
}

.duration-content strong {
  display: block;
  font-size: 0.9rem;
  color: #1e293b;
  margin-bottom: 0.25rem;
}

.duration-content p {
  margin: 0 0 0.5rem 0;
  font-size: 0.875rem;
  color: #64748b;
  line-height: 1.4;
}

.duration-note {
  background: #ecfdf5;
  color: #065f46;
  padding: 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  border: 1px solid #10b981;
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
  
  .notification-grid {
    grid-template-columns: 1fr;
  }
  
  .discord-info {
    flex-direction: column;
    text-align: center;
  }
  
  .purpose-cards {
    gap: 0.75rem;
  }
  
  .purpose-card {
    flex-direction: column;
    text-align: center;
    gap: 0.5rem;
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
  
  .discord-info {
    padding: 0.75rem;
  }
  
  .discord-content strong {
    font-size: 0.9rem;
  }
  
  .discord-content p {
    font-size: 0.8rem;
  }
}
</style>