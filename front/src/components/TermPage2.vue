<template>
    <div class="standalone-privacy-page">
        <!-- 페이지 헤더 -->
        <div class="page-header">
            <div class="header-content">
                <div class="breadcrumb">
                    <router-link to="/" class="breadcrumb-item">홈</router-link>
                    <span class="breadcrumb-separator">/</span>
                    <span class="breadcrumb-current">개인정보 처리방침</span>
                </div>
                
                <div class="page-title">
                    <div class="title-icon">📄</div>
                    <h1>개인정보 수집·이용 동의서</h1>
                    <div class="title-badge">개인정보보호</div>
                </div>
                
                <p class="page-description">
                    도서 대출 서비스 제공을 위한 개인정보 수집 및 이용에 관한 동의서입니다.
                    개인정보 보호를 위해 반드시 확인해 주세요.
                </p>
                
                <div class="last-updated">
                    최종 업데이트: 2025년 5월 9일
                </div>
            </div>
        </div>

        <!-- 개인정보 처리 내용 -->
        <div class="privacy-container">
            <div class="privacy-document">
                <!-- 목차 -->
                <div class="table-of-contents">
                    <h3>🔒 목차</h3>
                    <nav class="toc-nav">
                        <a href="#collection-items" class="toc-link">1. 수집 항목</a>
                        <a href="#collection-purpose" class="toc-link">2. 수집·이용 목적</a>
                        <a href="#retention-period" class="toc-link">3. 보유 및 이용 기간</a>
                        <a href="#rights-and-consequences" class="toc-link">4. 동의 거부 권리 및 불이익</a>
                    </nav>
                </div>

                <!-- 본문 -->
                <div class="privacy-content">
                    <div class="privacy-preamble">
                        <h2>개인정보 수집·이용 동의서</h2>
                        <div class="preamble-text">
                            본인은 도서 대출 서비스 제공을 위하여 아래와 같은 개인정보를 
                            수집·이용하는 것에 동의합니다.
                        </div>
                    </div>

                    <article id="collection-items" class="privacy-article">
                        <h3>1. 수집 항목</h3>
                        <div class="article-content">
                            <div class="collection-overview">
                                <p class="overview-text">서비스 제공을 위해 다음과 같은 개인정보를 수집합니다:</p>
                                
                                <div class="collection-grid">
                                    <div class="collection-item basic">
                                        <div class="item-icon">👤</div>
                                        <div class="item-content">
                                            <strong>이름</strong>
                                            <p>회원 식별 및 서비스 이용을 위한 기본 정보</p>
                                        </div>
                                    </div>
                                    
                                    <div class="collection-item basic">
                                        <div class="item-icon">🆔</div>
                                        <div class="item-content">
                                            <strong>아이디(ID)</strong>
                                            <p>로그인 및 계정 관리를 위한 고유 식별자</p>
                                        </div>
                                    </div>
                                    
                                    <div class="collection-item basic">
                                        <div class="item-icon">🔐</div>
                                        <div class="item-content">
                                            <strong>비밀번호</strong>
                                            <p>계정 보안 및 본인 인증을 위한 정보</p>
                                        </div>
                                    </div>
                                    
                                    <div class="collection-item discord">
                                        <div class="item-icon">
                                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M20.317 4.3698a19.7913 19.7913 0 00-4.8851-1.5152.0741.0741 0 00-.0785.0371c-.211.3753-.4447.8648-.6083 1.2495-1.8447-.2762-3.68-.2762-5.4868 0-.1636-.3933-.4058-.8742-.6177-1.2495a.077.077 0 00-.0785-.037 19.7363 19.7363 0 00-4.8852 1.515.0699.0699 0 00-.0321.0277C.5334 9.0458-.319 13.5799.0992 18.0578a.0824.0824 0 00.0312.0561c2.0528 1.5076 4.0413 2.4228 5.9929 3.0294a.0777.0777 0 00.0842-.0276c.4616-.6304.8731-1.2952 1.226-1.9942a.076.076 0 00-.0416-.1057c-.6528-.2476-1.2743-.5495-1.8722-.8923a.077.077 0 01-.0076-.1277c.1258-.0943.2517-.1923.3718-.2914a.0743.0743 0 01.0776-.0105c3.9278 1.7933 8.18 1.7933 12.0614 0a.0739.0739 0 01.0785.0095c.1202.099.246.1981.3728.2924a.077.077 0 01-.0066.1276 12.2986 12.2986 0 01-1.873.8914.0766.0766 0 00-.0407.1067c.3604.698.7719 1.3628 1.225 1.9932a.076.076 0 00.0842.0286c1.961-.6067 3.9495-1.5219 6.0023-3.0294a.077.077 0 00.0313-.0552c.5004-5.177-.8382-9.6739-3.5485-13.6604a.061.061 0 00-.0312-.0286zM8.02 15.3312c-1.1825 0-2.1569-1.0857-2.1569-2.419 0-1.3332.9555-2.4189 2.157-2.4189 1.2108 0 2.1757 1.0952 2.1568 2.419-.0189 1.3332-.9555 2.4189-2.1569 2.4189zm7.9748 0c-1.1825 0-2.1569-1.0857-2.1569-2.419 0-1.3332.9554-2.4189 2.1569-2.4189 1.2108 0 2.1757 1.0952 2.1568 2.419 0 1.3332-.9554 2.4189-2.1568 2.4189Z" fill="currentColor"/>
                                            </svg>
                                        </div>
                                        <div class="item-content">
                                            <strong>디스코드 아이디</strong>
                                            <p>도서 대출/반납 알림 메시지 발송을 위한 연락처</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </article>

                    <article id="collection-purpose" class="privacy-article">
                        <h3>2. 수집·이용 목적</h3>
                        <div class="article-content">
                            <div class="purpose-overview">
                                <p class="overview-text">수집된 개인정보는 다음과 같은 목적으로만 이용됩니다:</p>
                                
                                <div class="purpose-cards">
                                    <div class="purpose-card">
                                        <div class="purpose-icon">👤</div>
                                        <div class="purpose-content">
                                            <strong>회원 식별 및 관리</strong>
                                            <p>서비스 이용자 확인, 계정 관리, 회원 서비스 제공</p>
                                        </div>
                                    </div>
                                    
                                    <div class="purpose-card">
                                        <div class="purpose-icon">📚</div>
                                        <div class="purpose-content">
                                            <strong>도서 대출 및 반납 서비스 제공</strong>
                                            <p>도서 대출 신청 처리, 대여 현황 관리, 반납 처리</p>
                                        </div>
                                    </div>
                                    
                                    <div class="purpose-card highlight">
                                        <div class="purpose-icon">🔔</div>
                                        <div class="purpose-content">
                                            <strong>디스코드를 통한 알림 서비스</strong>
                                            <p>대출 완료, 반납 예정일, 연체 발생 등 중요 사항 알림</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </article>

                    <article id="retention-period" class="privacy-article">
                        <h3>3. 보유 및 이용 기간</h3>
                        <div class="article-content">
                            <div class="retention-overview">
                                <div class="retention-cards">
                                    <div class="retention-card primary">
                                        <div class="retention-icon">⏰</div>
                                        <div class="retention-content">
                                            <strong>기본 보유 기간</strong>
                                            <p>회원 탈퇴 시까지 보유 및 이용</p>
                                            <div class="retention-detail">
                                                회원이 서비스를 이용하는 동안 개인정보를 보유하며, 
                                                탈퇴 즉시 모든 개인정보를 삭제합니다.
                                            </div>
                                        </div>
                                    </div>
                                    
                                    <div class="retention-card secondary">
                                        <div class="retention-icon">⚖️</div>
                                        <div class="retention-content">
                                            <strong>법령에 따른 보존</strong>
                                            <p>관련 법령에 따라 보존할 필요가 있는 경우 해당 법령에 따름</p>
                                            <div class="retention-detail">
                                                전자상거래 등에서의 소비자보호에 관한 법률, 
                                                통신비밀보호법 등 관련 법령에 따른 보존
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </article>

                    <article id="rights-and-consequences" class="privacy-article warning">
                        <h3>4. 동의를 거부할 권리 및 불이익</h3>
                        <div class="article-content">
                            <div class="rights-overview">
                                <div class="rights-cards">
                                    <div class="rights-card positive">
                                        <div class="rights-icon">✅</div>
                                        <div class="rights-content">
                                            <strong>동의 거부 권리</strong>
                                            <p>귀하는 개인정보 수집·이용에 동의하지 않을 수 있습니다.</p>
                                        </div>
                                    </div>
                                    
                                    <div class="rights-card negative">
                                        <div class="rights-icon">⚠️</div>
                                        <div class="rights-content">
                                            <strong>동의 거부 시 불이익</strong>
                                            <p>필수 항목에 대한 동의를 거부할 경우, 서비스 이용이 제한될 수 있습니다.</p>
                                            <div class="limitation-detail">
                                                • 회원가입 불가<br>
                                                • 도서 대출 서비스 이용 불가<br>
                                                • 알림 서비스 이용 불가
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </article>

                    <!-- 개인정보 보호 관련 추가 정보 -->
                    <div class="protection-info">
                        <h3>🛡️ 개인정보 보호 방침</h3>
                        <div class="protection-content">
                            <div class="protection-item">
                                <div class="protection-icon">🔒</div>
                                <div class="protection-text">
                                    <strong>보안 조치</strong>
                                    <p>수집된 개인정보는 암호화하여 안전하게 보관됩니다.</p>
                                </div>
                            </div>
                            
                            <div class="protection-item">
                                <div class="protection-icon">🚫</div>
                                <div class="protection-text">
                                    <strong>제3자 제공 금지</strong>
                                    <p>동의 없이 개인정보를 제3자에게 제공하지 않습니다.</p>
                                </div>
                            </div>
                            
                            <div class="protection-item">
                                <div class="protection-icon">✋</div>
                                <div class="protection-text">
                                    <strong>목적 외 사용 금지</strong>
                                    <p>명시된 목적 이외의 용도로 개인정보를 사용하지 않습니다.</p>
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
                    <span>개인정보 처리방침을 모두 확인하셨나요?</span>
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
.standalone-privacy-page {
    min-height: 100vh;
    background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
}

.page-header {
    background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
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

.privacy-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 3rem 2rem 2rem 2rem;
}

.privacy-document {
    background: white;
    border-radius: 16px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
    overflow: hidden;
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
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
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
    background: #0ea5e9;
    color: white;
    border-color: #0ea5e9;
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba(14, 165, 233, 0.3);
}

.privacy-content {
    padding: 3rem;
}

.privacy-preamble {
    margin-bottom: 4rem;
    text-align: center;
    padding: 2rem;
    background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 100%);
    border-radius: 12px;
    border: 1px solid #10b981;
}

.privacy-preamble h2 {
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

.privacy-article {
    margin-bottom: 3rem;
    scroll-margin-top: 120px;
    padding: 2rem;
    border-radius: 12px;
    border: 1px solid #f1f5f9;
    background: white;
}

.privacy-article.warning {
    background: #fef7f0;
    border-color: #fed7aa;
}

.privacy-article h3 {
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

.collection-overview .overview-text,
.purpose-overview .overview-text {
    margin-bottom: 2rem;
    font-size: 1.05rem;
    color: #64748b;
}

.collection-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 1.5rem;
}

.collection-item {
    display: flex;
    gap: 1rem;
    padding: 1.5rem;
    border-radius: 12px;
    border: 1px solid #e2e8f0;
    background: #f8fafc;
    transition: all 0.3s ease;
}

.collection-item.basic {
    border-left: 4px solid #667eea;
}

.collection-item.discord {
    border-left: 4px solid #5865f2;
    background: #f0f4ff;
}

.collection-item:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.item-icon {
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

.item-content strong {
    display: block;
    font-size: 1rem;
    color: #1e293b;
    margin-bottom: 0.5rem;
    font-weight: 600;
}

.item-content p {
    margin: 0;
    font-size: 0.9rem;
    color: #64748b;
    line-height: 1.5;
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
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
    transition: all 0.3s ease;
}

.purpose-card.highlight {
    background: #f0f9ff;
    border-color: #0ea5e9;
    border-left: 4px solid #0ea5e9;
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
    margin: 0;
    font-size: 0.9rem;
    color: #64748b;
    line-height: 1.5;
}

.retention-cards {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.retention-card {
    display: flex;
    gap: 1.5rem;
    padding: 1.5rem;
    border-radius: 12px;
    transition: all 0.3s ease;
}

.retention-card.primary {
    background: #ecfdf5;
    border: 1px solid #10b981;
}

.retention-card.secondary {
    background: #fef3c7;
    border: 1px solid #f59e0b;
}

.retention-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.retention-icon {
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

.retention-content strong {
    display: block;
    font-size: 1rem;
    color: #1e293b;
    margin-bottom: 0.5rem;
    font-weight: 600;
}

.retention-content p {
    margin: 0 0 0.75rem 0;
    font-size: 0.9rem;
    color: #64748b;
    line-height: 1.5;
}

.retention-detail {
    font-size: 0.85rem;
    color: #6b7280;
    line-height: 1.4;
    padding: 0.75rem;
    background: rgba(255, 255, 255, 0.7);
    border-radius: 6px;
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

.limitation-detail {
    font-size: 0.85rem;
    color: #dc2626;
    line-height: 1.6;
    padding: 0.75rem;
    background: rgba(255, 255, 255, 0.7);
    border-radius: 6px;
}

.protection-info {
    margin-top: 3rem;
    padding: 2rem;
    background: #f0f9ff;
    border: 1px solid #0ea5e9;
    border-radius: 12px;
}

.protection-info h3 {
    font-size: 1.3rem;
    font-weight: 600;
    color: #1e293b;
    margin: 0 0 1.5rem 0;
}

.protection-content {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.protection-item {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 1rem;
    background: white;
    border-radius: 8px;
    border: 1px solid #e2e8f0;
}

.protection-icon {
    font-size: 1.25rem;
    flex-shrink: 0;
}

.protection-text strong {
    display: block;
    font-size: 0.95rem;
    color: #1e293b;
    margin-bottom: 0.25rem;
    font-weight: 600;
}

.protection-text p {
    margin: 0;
    font-size: 0.875rem;
    color: #64748b;
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
    
    .privacy-container {
        padding: 2rem 1rem;
    }
    
    .privacy-content {
        padding: 2rem;
    }
    
    .privacy-preamble {
        padding: 1.5rem;
        margin-bottom: 3rem;
    }
    
    .privacy-preamble h2 {
        font-size: 1.8rem;
    }
    
    .privacy-article {
        padding: 1.5rem;
        margin-bottom: 2rem;
    }
    
    .privacy-article h3 {
        font-size: 1.2rem;
    }
    
    .toc-nav {
        grid-template-columns: 1fr;
    }
    
    .collection-grid {
        grid-template-columns: 1fr;
    }
    
    .purpose-card,
    .retention-card,
    .rights-card {
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
    
    .privacy-content {
        padding: 1.5rem;
    }
    
    .privacy-article {
        padding: 1rem;
    }
    
    .collection-item,
    .purpose-card,
    .retention-card,
    .rights-card {
        padding: 1rem;
    }
}
</style>