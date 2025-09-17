<template>
    <div class="standalone-notification-page">
        <!-- 페이지 헤더 -->
        <div class="page-header">
            <div class="header-content">
                <div class="breadcrumb">
                    <router-link to="/" class="breadcrumb-item">홈</router-link>
                    <span class="breadcrumb-separator">/</span>
                    <span class="breadcrumb-current">도서 대출 및 반납 알림 수신 동의서</span>
                </div>
                
                <div class="page-title">
                    <div class="title-icon">🔔</div>
                    <h1>도서 대출 및 반납 알림 수신 동의서</h1>
                    <div class="title-badge">디스코드 알림</div>
                </div>
                
                <p class="page-description">
                    원활한 도서 대출 서비스 이용을 위한 디스코드 알림 수신에 관한 동의서입니다.
                    연체 방지와 서비스 품질 향상을 위해 확인해 주세요.
                </p>
                
                <div class="last-updated">
                    최종 업데이트: 2024년 1월 1일
                </div>
            </div>
        </div>

        <!-- 알림 동의 내용 -->
        <div class="notification-container">
            <div class="notification-document">
                <!-- 디스코드 소개 섹션 -->
                <div class="discord-intro">
                    <div class="discord-hero">
                        <div class="discord-logo-large">
                            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M20.317 4.3698a19.7913 19.7913 0 00-4.8851-1.5152.0741.0741 0 00-.0785.0371c-.211.3753-.4447.8648-.6083 1.2495-1.8447-.2762-3.68-.2762-5.4868 0-.1636-.3933-.4058-.8742-.6177-1.2495a.077.077 0 00-.0785-.037 19.7363 19.7363 0 00-4.8852 1.515.0699.0699 0 00-.0321.0277C.5334 9.0458-.319 13.5799.0992 18.0578a.0824.0824 0 00.0312.0561c2.0528 1.5076 4.0413 2.4228 5.9929 3.0294a.0777.0777 0 00.0842-.0276c.4616-.6304.8731-1.2952 1.226-1.9942a.076.076 0 00-.0416-.1057c-.6528-.2476-1.2743-.5495-1.8722-.8923a.077.077 0 01-.0076-.1277c.1258-.0943.2517-.1923.3718-.2914a.0743.0743 0 01.0776-.0105c3.9278 1.7933 8.18 1.7933 12.0614 0a.0739.0739 0 01.0785.0095c.1202.099.246.1981.3728.2924a.077.077 0 01-.0066.1276 12.2986 12.2986 0 01-1.873.8914.0766.0766 0 00-.0407.1067c.3604.698.7719 1.3628 1.225 1.9932a.076.076 0 00.0842.0286c1.961-.6067 3.9495-1.5219 6.0023-3.0294a.077.077 0 00.0313-.0552c.5004-5.177-.8382-9.6739-3.5485-13.6604a.061.061 0 00-.0312-.0286zM8.02 15.3312c-1.1825 0-2.1569-1.0857-2.1569-2.419 0-1.3332.9555-2.4189 2.157-2.4189 1.2108 0 2.1757 1.0952 2.1568 2.419-.0189 1.3332-.9555 2.4189-2.1569 2.4189zm7.9748 0c-1.1825 0-2.1569-1.0857-2.1569-2.419 0-1.3332.9554-2.4189 2.1569-2.4189 1.2108 0 2.1757 1.0952 2.1568 2.419 0 1.3332-.9554 2.4189-2.1568 2.4189Z" fill="currentColor"/>
                            </svg>
                        </div>
                        <div class="discord-hero-content">
                            <h3>디스코드 알림 서비스</h3>
                            <p>도서 대출/반납 관련 중요 정보를 디스코드 메시지로 실시간 알림받으세요</p>
                        </div>
                    </div>
                </div>

                <!-- 목차 -->
                <div class="table-of-contents">
                    <h3>🔔 목차</h3>
                    <nav class="toc-nav">
                        <a href="#notification-items" class="toc-link">1. 수신 항목 및 수단</a>
                        <a href="#notification-purpose" class="toc-link">2. 이용 목적</a>
                        <a href="#notification-period" class="toc-link">3. 보유 및 이용 기간</a>
                        <a href="#notification-rights" class="toc-link">4. 동의 거부 권리 및 불이익</a>
                    </nav>
                </div>

                <!-- 본문 -->
                <div class="notification-content">
                    <div class="notification-preamble">
                        <h2>도서 대여 및 반납 알림 수신 동의서</h2>
                        <div class="preamble-text">
                            본인은 도서 대출 및 반납 기한과 관련된 알림 메시지를 수신하는 것에 동의합니다.
                        </div>
                    </div>

                    <article id="notification-items" class="notification-article">
                        <h3>1. 수신 항목 및 수단</h3>
                        <div class="article-content">
                            <div class="reception-section">
                                <div class="reception-items">
                                    <h4>📋 수신 항목</h4>
                                    <div class="notification-types">
                                        <div class="notification-type">
                                            <div class="type-icon">📚</div>
                                            <div class="type-content">
                                                <strong>도서 대출 완료 안내</strong>
                                                <p>대출 신청이 승인되어 도서 대출이 완료되었을 때 발송되는 알림</p>
                                                <div class="example-message">
                                                    "📚 도서 대여가 완료되었습니다. 반납 예정일: 2024.01.15"
                                                </div>
                                            </div>
                                        </div>
                                        
                                        <div class="notification-type">
                                            <div class="type-icon">📅</div>
                                            <div class="type-content">
                                                <strong>반납 예정일 안내</strong>
                                                <p>반납 예정일 1-2일 전에 미리 알려주는 사전 알림</p>
                                                <div class="example-message">
                                                    "⏰ 대출하신 도서의 반납 예정일이 내일입니다. (2024.01.15)"
                                                </div>
                                            </div>
                                        </div>
                                        
                                        <div class="notification-type critical">
                                            <div class="type-icon">🚨</div>
                                            <div class="type-content">
                                                <strong>연체 발생 알림</strong>
                                                <p>반납 기한을 초과하여 연체가 발생했을 때 즉시 발송되는 알림</p>
                                                <div class="example-message critical">
                                                    "🚨 도서 연체가 발생했습니다. 즉시 반납해 주세요. (연체일: 3일, 대출정지: 6일)"
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                
                                <div class="reception-method">
                                    <h4>💬 수신 수단</h4>
                                    <div class="method-card">
                                        <div class="discord-method">
                                            <div class="discord-badge">
                                                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <path d="M20.317 4.3698a19.7913 19.7913 0 00-4.8851-1.5152.0741.0741 0 00-.0785.0371c-.211.3753-.4447.8648-.6083 1.2495-1.8447-.2762-3.68-.2762-5.4868 0-.1636-.3933-.4058-.8742-.6177-1.2495a.077.077 0 00-.0785-.037 19.7363 19.7363 0 00-4.8852 1.515.0699.0699 0 00-.0321.0277C.5334 9.0458-.319 13.5799.0992 18.0578a.0824.0824 0 00.0312.0561c2.0528 1.5076 4.0413 2.4228 5.9929 3.0294a.0777.0777 0 00.0842-.0276c.4616-.6304.8731-1.2952 1.226-1.9942a.076.076 0 00-.0416-.1057c-.6528-.2476-1.2743-.5495-1.8722-.8923a.077.077 0 01-.0076-.1277c.1258-.0943.2517-.1923.3718-.2914a.0743.0743 0 01.0776-.0105c3.9278 1.7933 8.18 1.7933 12.0614 0a.0739.0739 0 01.0785.0095c.1202.099.246.1981.3728.2924a.077.077 0 01-.0066.1276 12.2986 12.2986 0 01-1.873.8914.0766.0766 0 00-.0407.1067c.3604.698.7719 1.3628 1.225 1.9932a.076.076 0 00.0842.0286c1.961-.6067 3.9495-1.5219 6.0023-3.0294a.077.077 0 00.0313-.0552c.5004-5.177-.8382-9.6739-3.5485-13.6604a.061.061 0 00-.0312-.0286zM8.02 15.3312c-1.1825 0-2.1569-1.0857-2.1569-2.419 0-1.3332.9555-2.4189 2.157-2.4189 1.2108 0 2.1757 1.0952 2.1568 2.419-.0189 1.3332-.9555 2.4189-2.1569 2.4189zm7.9748 0c-1.1825 0-2.1569-1.0857-2.1569-2.419 0-1.3332.9554-2.4189 2.1569-2.4189 1.2108 0 2.1757 1.0952 2.1568 2.419 0 1.3332-.9554 2.4189-2.1568 2.4189Z" fill="currentColor"/>
                                                </svg>
                                                <span>디스코드 서버 메시지</span>
                                            </div>
                                            <div class="method-description">
                                                <p>플레이북 디스코드 서버에서 메시지를 발송합니다</p>
                                                <div class="method-benefits">
                                                    <div class="benefit-item">✓ 실시간 알림 수신</div>
                                                    <div class="benefit-item">✓ 놓치기 쉬운 중요 정보 확인</div>
                                                    <div class="benefit-item">✓ 연체 방지를 위한 사전 알림</div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </article>

                    <article id="notification-purpose" class="notification-article">
                        <h3>2. 이용 목적</h3>
                        <div class="article-content">
                            <div class="purpose-overview">
                                <p class="overview-text">알림 서비스는 다음과 같은 목적으로 제공됩니다:</p>
                                
                                <div class="purpose-cards">
                                    <div class="purpose-card primary">
                                        <div class="purpose-icon">📋</div>
                                        <div class="purpose-content">
                                            <strong>도서 대출 및 반납 기한 안내</strong>
                                            <p>정확한 대출/반납 일정을 실시간으로 알려드려 원활한 서비스 이용을 돕습니다</p>
                                            <div class="purpose-detail">
                                                대출 승인, 반납 예정일, 반납 완료 등 모든 과정을 투명하게 안내합니다
                                            </div>
                                        </div>
                                    </div>
                                    
                                    <div class="purpose-card highlight">
                                        <div class="purpose-icon">🛡️</div>
                                        <div class="purpose-content">
                                            <strong>연체 방지 및 원활한 서비스 운영</strong>
                                            <p>미리 알림을 통해 연체를 방지하고 모든 회원이 공평하게 서비스를 이용할 수 있도록 합니다</p>
                                            <div class="purpose-detail">
                                                연체로 인한 불이익을 예방하고 도서 회전율을 높여 더 많은 회원이 이용할 수 있게 합니다
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </article>

                    <article id="notification-period" class="notification-article">
                        <h3>3. 보유 및 이용 기간</h3>
                        <div class="article-content">
                            <div class="period-overview">
                                <div class="period-card">
                                    <div class="period-icon">📅</div>
                                    <div class="period-content">
                                        <strong>알림 수신 기간</strong>
                                        <p>동의일로부터 회원 탈퇴 또는 수신 동의 철회 시까지</p>
                                        
                                        <div class="period-details">
                                            <div class="detail-section">
                                                <h5>🔄 동의 철회 방법</h5>
                                                <div class="withdrawal-methods">
                                                    <div class="withdrawal-item">
                                                        <span class="withdrawal-icon">⚙️</span>
                                                        <span>마이페이지 > 알림 설정에서 언제든지 변경 가능</span>
                                                    </div>
                                                    <div class="withdrawal-item">
                                                        <span class="withdrawal-icon">📞</span>
                                                        <span>고객센터 문의를 통한 수신 중단 요청</span>
                                                    </div>
                                                </div>
                                            </div>
                                            
                                            <div class="detail-section">
                                                <h5>🗑️ 정보 삭제</h5>
                                                <p>수신 동의 철회 또는 회원 탈퇴 시 즉시 관련 정보가 삭제됩니다</p>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </article>

                    <article id="notification-rights" class="notification-article warning">
                        <h3>4. 동의를 거부할 권리 및 불이익</h3>
                        <div class="article-content">
                            <div class="rights-overview">
                                <div class="rights-cards">
                                    <div class="rights-card positive">
                                        <div class="rights-icon">✅</div>
                                        <div class="rights-content">
                                            <strong>동의 거부 권리</strong>
                                            <p>귀하는 알림 수신에 동의하지 않을 수 있습니다.</p>
                                            <div class="rights-detail">
                                                개인정보 자기결정권에 따라 언제든지 동의를 거부하거나 철회할 수 있는 권리가 있습니다.
                                            </div>
                                        </div>
                                    </div>
                                    
                                    <div class="rights-card negative">
                                        <div class="rights-icon">⚠️</div>
                                        <div class="rights-content">
                                            <strong>동의 거부 시 불이익</strong>
                                            <p>필수 항목에 대한 동의를 거부할 경우, 서비스 이용이 제한될 수 있습니다.</p>
                                            <div class="limitation-list">
                                                <div class="limitation-item">• 반납 기한 알림을 받을 수 없어 연체 위험 증가</div>
                                                <div class="limitation-item">• 중요한 서비스 변경사항을 놓칠 수 있음</div>
                                                <div class="limitation-item">• 도서 대출 서비스 전체 이용 제한 가능</div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </article>

                    <!-- 알림 예시 섹션 -->
                    <div class="notification-examples">
                        <h3>📱 알림 메시지 예시</h3>
                        <div class="examples-content">
                            <div class="example-message rental">
                                <div class="message-header">
                                    <span class="message-type">대출 완료</span>
                                    <span class="message-time">방금 전</span>
                                </div>
                                <div class="message-content">
                                    📚 <strong>[클린 코드]</strong> 도서 대출이 완료되었습니다.<br>
                                    📅 반납 예정일: 2024년 1월 15일<br>
                                    💡 반납일을 꼭 지켜주세요!
                                </div>
                            </div>
                            
                            <div class="example-message reminder">
                                <div class="message-header">
                                    <span class="message-type">반납 알림</span>
                                    <span class="message-time">1일 전</span>
                                </div>
                                <div class="message-content">
                                    ⏰ <strong>[클린 코드]</strong> 반납 예정일이 내일입니다.<br>
                                    📅 반납일: 2024년 1월 15일<br>
                                    🏃‍♂️ 연체되지 않도록 미리 준비해 주세요!
                                </div>
                            </div>
                            
                            <div class="example-message overdue">
                                <div class="message-header">
                                    <span class="message-type">연체 알림</span>
                                    <span class="message-time">연체 발생 시</span>
                                </div>
                                <div class="message-content">
                                    🚨 <strong>[클린 코드]</strong> 도서가 연체되었습니다.<br>
                                    📅 반납 예정일: 2024년 1월 15일 (3일 경과)<br>
                                    ⛔ 대여 정지 기간: 6일<br>
                                    🏃‍♂️ 즉시 반납해 주세요!
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 하단 네비게이션 -->
        <div class="page-footer">
            <div class="footer-content">
                <button class="btn-secondary" @click="goBack">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M19 12H6M12 5l-7 7 7 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    이전으로
                </button>
                
                <div class="page-info">
                    <span>알림 수신 동의서를 모두 확인하셨나요?</span>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goBack() {
    router.back()
}
</script>

<style scoped>
.standalone-notification-page {
    min-height: 100vh;
    background: linear-gradient(135deg, #f0f4ff 0%, #e0e7ff 100%);
}

.page-header {
    background: linear-gradient(135deg, #5865f2 0%, #4338ca 100%);
    color: white;
    padding: 4rem 0 3rem 0;
}

.header-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 2rem;
}

.breadcrumb {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 2rem;
    font-size: 0.9rem;
}

.breadcrumb-item {
    color: rgba(255, 255, 255, 0.8);
    text-decoration: none;
    transition: color 0.3s ease;
}

.breadcrumb-item:hover {
    color: white;
}

.breadcrumb-separator {
    color: rgba(255, 255, 255, 0.6);
}

.breadcrumb-current {
    color: white;
    font-weight: 500;
}

.page-title {
    display: flex;
    align-items: center;
    gap: 1.5rem;
    margin-bottom: 1.5rem;
    flex-wrap: wrap;
}

.title-icon {
    font-size: 3rem;
}

.page-title h1 {
    font-size: 2.8rem;
    font-weight: 700;
    margin: 0;
}

.title-badge {
    background: rgba(255, 255, 255, 0.2);
    padding: 0.5rem 1rem;
    border-radius: 20px;
    font-size: 0.875rem;
    font-weight: 600;
    backdrop-filter: blur(10px);
}

.page-description {
    font-size: 1.2rem;
    opacity: 0.9;
    margin: 0 0 1rem 0;
    max-width: 700px;
    line-height: 1.6;
}

.last-updated {
    font-size: 0.9rem;
    opacity: 0.8;
    margin-top: 1rem;
}

.notification-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 3rem 2rem 2rem 2rem;
}

.notification-document {
    background: white;
    border-radius: 16px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
    overflow: hidden;
}

.discord-intro {
    background: linear-gradient(135deg, #5865f2 0%, #4338ca 100%);
    color: white;
    padding: 3rem;
}

.discord-hero {
    display: flex;
    align-items: center;
    gap: 2rem;
    max-width: 800px;
    margin: 0 auto;
}

.discord-logo-large {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 80px;
    height: 80px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    flex-shrink: 0;
    backdrop-filter: blur(10px);
}

.discord-hero-content h3 {
    font-size: 1.8rem;
    font-weight: 600;
    margin: 0 0 0.75rem 0;
}

.discord-hero-content p {
    font-size: 1.1rem;
    margin: 0;
    opacity: 0.9;
    line-height: 1.6;
}

.table-of-contents {
    background: #f8fafc;
    padding: 2.5rem;
    border-bottom: 1px solid #e2e8f0;
}

.table-of-contents h3 {
    font-size: 1.4rem;
    font-weight: 600;
    color: #1e293b;
    margin: 0 0 2rem 0;
}

.toc-nav {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 1rem;
}

.toc-link {
    display: block;
    padding: 1rem 1.25rem;
    background: white;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    color: #475569;
    text-decoration: none;
    font-size: 0.95rem;
    font-weight: 500;
    transition: all 0.3s ease;
}

.toc-link:hover {
    background: #5865f2;
    color: white;
    border-color: #5865f2;
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba(88, 101, 242, 0.3);
}

.notification-content {
    padding: 3rem;
}

.notification-preamble {
    margin-bottom: 4rem;
    text-align: center;
    padding: 2rem;
    background: linear-gradient(135deg, #f0f4ff 0%, #e0e7ff 100%);
    border-radius: 12px;
    border: 1px solid #5865f2;
}

.notification-preamble h2 {
    font-size: 2.2rem;
    font-weight: 700;
    color: #1e293b;
    margin: 0 0 1.5rem 0;
}

.preamble-text {
    font-size: 1.1rem;
    color: #475569;
    line-height: 1.7;
    max-width: 800px;
    margin: 0 auto;
}

.notification-article {
    margin-bottom: 3rem;
    scroll-margin-top: 120px;
    padding: 2rem;
    border-radius: 12px;
    border: 1px solid #f1f5f9;
    background: white;
}

.notification-article.warning {
    background: #fef7f0;
    border-color: #fed7aa;
}

.notification-article h3 {
    font-size: 1.4rem;
    font-weight: 600;
    color: #1e293b;
    margin: 0 0 1.5rem 0;
    padding-bottom: 0.75rem;
    border-bottom: 2px solid #e2e8f0;
}

.article-content {
    font-size: 1rem;
    line-height: 1.7;
    color: #374151;
}

.reception-section {
    display: flex;
    flex-direction: column;
    gap: 2rem;
}

.reception-items h4,
.reception-method h4 {
    font-size: 1.2rem;
    font-weight: 600;
    color: #1e293b;
    margin: 0 0 1.5rem 0;
}

.notification-types {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.notification-type {
    display: flex;
    gap: 1.5rem;
    padding: 1.5rem;
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
    transition: all 0.3s ease;
}

.notification-type.critical {
    background: #fef2f2;
    border-color: #fca5a5;
}

.notification-type:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.type-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 48px;
    height: 48px;
    background: white;
    border-radius: 10px;
    font-size: 1.5rem;
    flex-shrink: 0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.type-content strong {
    display: block;
    font-size: 1rem;
    color: #1e293b;
    margin-bottom: 0.5rem;
    font-weight: 600;
}

.type-content p {
    margin: 0 0 1rem 0;
    font-size: 0.9rem;
    color: #64748b;
    line-height: 1.5;
}

.example-message {
    background: #f1f5f9;
    border: 1px solid #cbd5e1;
    border-radius: 8px;
    padding: 0.75rem;
    font-size: 0.85rem;
    color: #475569;
    font-family: 'Courier New', monospace;
    line-height: 1.4;
}

.example-message.critical {
    background: #fef2f2;
    border-color: #fca5a5;
    color: #dc2626;
}

.method-card {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
    padding: 1.5rem;
}

.discord-method {
    text-align: center;
}

.discord-badge {
    display: inline-flex;
    align-items: center;
    gap: 0.75rem;
    background: #5865f2;
    color: white;
    padding: 0.75rem 1.5rem;
    border-radius: 8px;
    font-size: 1rem;
    font-weight: 600;
    margin-bottom: 1rem;
}

.method-description p {
    margin: 0 0 1rem 0;
    font-size: 0.95rem;
    color: #64748b;
}

.method-benefits {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.benefit-item {
    font-size: 0.9rem;
    color: #059669;
    text-align: left;
}

.purpose-overview .overview-text {
    margin-bottom: 2rem;
    font-size: 1.05rem;
    color: #64748b;
}

.purpose-cards {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.purpose-card {
    display: flex;
    gap: 1.5rem;
    padding: 1.5rem;
    border-radius: 12px;
    transition: all 0.3s ease;
}

.purpose-card.primary {
    background: #f0f9ff;
    border: 1px solid #0ea5e9;
}

.purpose-card.highlight {
    background: #f0fdf4;
    border: 1px solid #10b981;
}

.purpose-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.purpose-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 48px;
    height: 48px;
    background: white;
    border-radius: 10px;
    font-size: 1.5rem;
    flex-shrink: 0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.purpose-content strong {
    display: block;
    font-size: 1rem;
    color: #1e293b;
    margin-bottom: 0.5rem;
    font-weight: 600;
}

.purpose-content p {
    margin: 0 0 0.75rem 0;
    font-size: 0.9rem;
    color: #64748b;
    line-height: 1.5;
}

.purpose-detail {
    font-size: 0.85rem;
    color: #6b7280;
    line-height: 1.4;
    padding: 0.75rem;
    background: rgba(255, 255, 255, 0.7);
    border-radius: 6px;
}

.period-card {
    display: flex;
    gap: 1.5rem;
    padding: 1.5rem;
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
    max-width: 800px;
    margin: 0 auto;
}

.period-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 48px;
    height: 48px;
    background: white;
    border-radius: 10px;
    font-size: 1.5rem;
    flex-shrink: 0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.period-content strong {
    display: block;
    font-size: 1.1rem;
    color: #1e293b;
    margin-bottom: 0.5rem;
    font-weight: 600;
}

.period-content > p {
    margin: 0 0 1.5rem 0;
    font-size: 0.95rem;
    color: #64748b;
    line-height: 1.5;
}

.period-details {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.detail-section h5 {
    font-size: 1rem;
    font-weight: 600;
    color: #1e293b;
    margin: 0 0 0.75rem 0;
}

.detail-section p {
    margin: 0;
    font-size: 0.9rem;
    color: #64748b;
    line-height: 1.5;
}

.withdrawal-methods {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
}

.withdrawal-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    font-size: 0.9rem;
    color: #475569;
}

.withdrawal-icon {
    flex-shrink: 0;
}

.rights-cards {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.rights-card {
    display: flex;
    gap: 1.5rem;
    padding: 1.5rem;
    border-radius: 12px;
    transition: all 0.3s ease;
}

.rights-card.positive {
    background: #ecfdf5;
    border: 1px solid #10b981;
}

.rights-card.negative {
    background: #fef2f2;
    border: 1px solid #fca5a5;
}

.rights-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.rights-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 48px;
    height: 48px;
    background: white;
    border-radius: 10px;
    font-size: 1.5rem;
    flex-shrink: 0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.rights-content strong {
    display: block;
    font-size: 1rem;
    color: #1e293b;
    margin-bottom: 0.5rem;
    font-weight: 600;
}

.rights-content p {
    margin: 0 0 0.75rem 0;
    font-size: 0.9rem;
    color: #64748b;
    line-height: 1.5;
}

.rights-detail {
    font-size: 0.85rem;
    color: #6b7280;
    line-height: 1.4;
    padding: 0.75rem;
    background: rgba(255, 255, 255, 0.7);
    border-radius: 6px;
}

.limitation-list {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.limitation-item {
    font-size: 0.85rem;
    color: #dc2626;
    line-height: 1.4;
    padding: 0.5rem;
    background: rgba(255, 255, 255, 0.7);
    border-radius: 4px;
}

.notification-examples {
    margin-top: 3rem;
    padding: 2rem;
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
}

.notification-examples h3 {
    font-size: 1.3rem;
    font-weight: 600;
    color: #1e293b;
    margin: 0 0 2rem 0;
    text-align: center;
}

.examples-content {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
    max-width: 600px;
    margin: 0 auto;
}

.example-message {
    background: white;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.example-message.rental {
    border-left: 4px solid #10b981;
}

.example-message.reminder {
    border-left: 4px solid #f59e0b;
}

.example-message.overdue {
    border-left: 4px solid #ef4444;
}

.message-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.75rem 1rem;
    background: #f8fafc;
    border-bottom: 1px solid #e2e8f0;
}

.message-type {
    font-size: 0.875rem;
    font-weight: 600;
    color: #374151;
}

.message-time {
    font-size: 0.8rem;
    color: #6b7280;
}

.message-content {
    padding: 1rem;
    font-family: 'Segoe UI', system-ui, sans-serif;
    font-size: 0.9rem;
    line-height: 1.5;
    color: #374151;
}

.final-agreement {
    margin-top: 3rem;
    padding: 2rem;
    background: #ecfdf5;
    border: 1px solid #10b981;
    border-radius: 12px;
}

.agreement-box h4 {
    font-size: 1.2rem;
    font-weight: 600;
    color: #1e293b;
    margin: 0 0 1rem 0;
}

.agreement-box p {
    margin: 0;
    font-size: 1rem;
    color: #065f46;
    line-height: 1.6;
}

.page-footer {
    background: white;
    border-top: 1px solid #e2e8f0;
    padding: 2rem 0;
}

.footer-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 2rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.btn-secondary {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1.5rem;
    background: #f8fafc;
    border: 1px solid #cbd5e1;
    border-radius: 8px;
    color: #475569;
    font-size: 0.95rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s ease;
}

.btn-secondary:hover {
    background: #f1f5f9;
    border-color: #94a3b8;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.page-info {
    color: #64748b;
    font-size: 0.95rem;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
    .page-header {
        padding: 3rem 0 2rem 0;
    }
    
    .header-content {
        padding: 0 1rem;
    }
    
    .page-title {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
    }
    
    .page-title h1 {
        font-size: 2.2rem;
    }
    
    .title-icon {
        font-size: 2.5rem;
    }
    
    .notification-container {
        padding: 2rem 1rem;
    }
    
    .notification-content {
        padding: 2rem;
    }
    
    .notification-preamble {
        padding: 1.5rem;
        margin-bottom: 3rem;
    }
    
    .notification-preamble h2 {
        font-size: 1.8rem;
    }
    
    .notification-article {
        padding: 1.5rem;
        margin-bottom: 2rem;
    }
    
    .notification-article h3 {
        font-size: 1.2rem;
    }
    
    .toc-nav {
        grid-template-columns: 1fr;
    }
    
    .discord-hero {
        flex-direction: column;
        text-align: center;
        gap: 1.5rem;
    }
    
    .notification-type {
        flex-direction: column;
        gap: 1rem;
        text-align: center;
    }
    
    .purpose-card,
    .rights-card {
        flex-direction: column;
        gap: 1rem;
        text-align: center;
    }
    
    .period-card {
        flex-direction: column;
        gap: 1rem;
        text-align: center;
    }
    
    .footer-content {
        flex-direction: column;
        gap: 1rem;
        padding: 0 1rem;
    }
}

@media (max-width: 480px) {
    .page-title h1 {
        font-size: 1.8rem;
    }
    
    .notification-content {
        padding: 1.5rem;
    }
    
    .notification-article {
        padding: 1rem;
    }
    
    .discord-intro {
        padding: 2rem;
    }
    
    .reception-section {
        gap: 1.5rem;
    }
    
    .notification-type {
        padding: 1rem;
    }
}
</style>