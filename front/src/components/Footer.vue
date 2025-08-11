<template>
    <footer class="modern-footer">
        <div class="footer-content">
            <div class="footer-main">
                <div class="footer-brand">
                    <div class="brand-icon">
                        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            <circle cx="12" cy="8" r="1" fill="currentColor"/>
                            <circle cx="12" cy="12" r="1" fill="currentColor"/>
                            <circle cx="12" cy="16" r="1" fill="currentColor"/>
                        </svg>
                    </div>
                    <div class="brand-text">
                        <h3>PLAYBOOK</h3>
                        <p>플레이데이터 도서 대여 서비스</p>
                    </div>
                </div>
                
                <div class="footer-links">
                    <div class="link-group">
                        <h4>서비스</h4>
                        <ul>
                            <li><a href="#" @click.prevent>도서 대여</a></li>
                            <li><a href="#" @click.prevent>도서 반납</a></li>
                            <li><a href="#" @click.prevent>이용 안내</a></li>
                        </ul>
                    </div>
                    
                    <div class="link-group">
                        <h4>지원</h4>
                        <ul>
                            <li><a href="#" @click.prevent>자주 묻는 질문</a></li>
                            <li><a href="#" @click.prevent>문의하기</a></li>
                            <li><a href="#" @click.prevent>피드백</a></li>
                        </ul>
                    </div>
                    
                    <div class="link-group">
                        <h4>정보</h4>
                        <ul>
                            <li><a href="#" @click.prevent>회사 소개</a></li>
                            <li><a href="#" @click.prevent>개인정보 처리방침</a></li>
                            <li><a href="#" @click.prevent>이용 약관</a></li>
                            <li><a href="#" @click.prevent>공지사항</a></li>
                        </ul>
                    </div>
                </div>
                
                <div class="footer-social">
                    <h4>소셜 미디어</h4>
                    <div class="social-links">
                        <a href="https://www.facebook.com/encore1997" target='_blank' class="social-link">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M18 2H15C13.6739 2 12.4021 2.52678 11.4645 3.46447C10.5268 4.40215 10 5.67392 10 7V10H7V14H10V22H14V14H17L18 10H14V7C14 6.73478 14.1054 6.48043 14.2929 6.29289C14.4804 6.10536 14.7348 6 15 6H18V2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </a>
                        <a href="https://x.com/encore_mk" target='_blank' class="social-link">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M23 3C22.0424 3.67548 20.9821 4.19211 19.86 4.53C19.2577 3.83751 18.4573 3.34669 17.567 3.12393C16.6767 2.90116 15.7395 2.9572 14.8821 3.28445C14.0247 3.6117 13.2884 4.19439 12.773 4.95371C12.2575 5.71303 11.9877 6.61234 12 7.53V8.53C10.2426 8.57557 8.50127 8.18581 6.93101 7.39624C5.36074 6.60667 4.01032 5.43666 3 4C3 4 -1 13 8 17C5.94053 18.398 3.48716 19.099 1 19C10 24 21 19 21 7.5C20.9991 7.22145 20.9723 6.94359 20.92 6.67C21.9406 5.66349 22.6608 4.39271 23 3V3Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </a>
                        <a href="https://www.youtube.com/@%EC%97%94%EC%BD%94%EC%95%84" target='_blank' class="social-link">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <rect x="2" y="3" width="20" height="14" rx="2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <path d="M8 21L12 17L16 21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </a>
                        <a href="https://playdata.io/" target='_blank' class="social-link">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M16 8C16 6.93913 15.5786 5.92172 14.8284 5.17157C14.0783 4.42143 13.0609 4 12 4C10.9391 4 9.92172 4.42143 9.17157 5.17157C8.42143 5.92172 8 6.93913 8 8C8 15 5 18 5 18H19S16 15 16 8Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <path d="M13.73 21C13.5542 21.3031 13.3019 21.5547 12.9982 21.7295C12.6946 21.9044 12.3504 21.9965 12 21.9965C11.6496 21.9965 11.3054 21.9044 11.0018 21.7295C10.6982 21.5547 10.4458 21.3031 10.27 21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </a>
                    </div>
                </div>
            </div>
            
            <div class="footer-bottom">
                <div class="copyright">
                    <span>Copyright © 2025 플레이데이터 All Rights Reserved.</span>
                </div>
                <div class="footer-meta">
                    <span class="version">Version 2.0</span>
                    <span class="divider">•</span>
                    <span class="update">Last updated: {{ currentDate }}</span>
                </div>
            </div>
        </div>
        
        <div class="footer-decoration">
            <div class="decoration-line"></div>
            <div class="decoration-dots">
                <span></span>
                <span></span>
                <span></span>
            </div>
        </div>
    </footer>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const currentDate = ref('')

onMounted(() => {
    const now = new Date()
    currentDate.value = now.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    })
})
</script>

<style scoped>
.modern-footer {
    position: relative;
    background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
    color: #f1f5f9;
    margin-top: auto;
    overflow: hidden;
}

.footer-content {
    max-width: 1440px;
    margin: 0 auto;
    padding: 4rem 2rem 2rem;
    position: relative;
    z-index: 2;
}

.footer-main {
    display: grid;
    grid-template-columns: 1fr 2fr 1fr;
    gap: 3rem;
    margin-bottom: 3rem;
}

.footer-brand {
    display: flex;
    align-items: flex-start;
    gap: 1rem;
}

.brand-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 48px;
    height: 48px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
    color: white;
    flex-shrink: 0;
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.brand-text h3 {
    font-size: 1.5rem;
    font-weight: 700;
    margin: 0 0 0.5rem 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.brand-text p {
    color: #94a3b8;
    margin: 0;
    font-size: 0.9rem;
}

.footer-links {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 2rem;
}

.link-group h4 {
    font-size: 1rem;
    font-weight: 600;
    margin: 0 0 1rem 0;
    color: #e2e8f0;
}

.link-group ul {
    list-style: none;
    padding: 0;
    margin: 0;
}

.link-group li {
    margin-bottom: 0.5rem;
}

.link-group a {
    color: #94a3b8;
    text-decoration: none;
    font-size: 0.9rem;
    transition: all 0.3s ease;
    display: inline-block;
    position: relative;
}

.link-group a::after {
    content: '';
    position: absolute;
    bottom: -2px;
    left: 0;
    width: 0;
    height: 2px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    transition: width 0.3s ease;
}

.link-group a:hover {
    color: #e2e8f0;
    transform: translateX(4px);
}

.link-group a:hover::after {
    width: 100%;
}

.footer-social h4 {
    font-size: 1rem;
    font-weight: 600;
    margin: 0 0 1rem 0;
    color: #e2e8f0;
}

.social-links {
    display: flex;
    gap: 1rem;
}

.social-link {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    background: rgba(100, 116, 139, 0.2);
    border: 1px solid rgba(100, 116, 139, 0.3);
    border-radius: 10px;
    color: #94a3b8;
    text-decoration: none;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;
}

.social-link::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
}

.social-link svg {
    position: relative;
    z-index: 1;
    transition: all 0.3s ease;
}

.social-link:hover::before {
    opacity: 1;
}

.social-link:hover {
    color: white;
    transform: translateY(-2px) scale(1.05);
    border-color: transparent;
    box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.footer-bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 2rem;
    border-top: 1px solid rgba(100, 116, 139, 0.3);
}

.copyright {
    font-size: 0.9rem;
    color: #94a3b8;
}

.footer-meta {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.8rem;
    color: #64748b;
}

.divider {
    opacity: 0.5;
}

.version {
    background: rgba(102, 126, 234, 0.1);
    color: #667eea;
    padding: 2px 8px;
    border-radius: 4px;
    font-weight: 500;
}

.footer-decoration {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, #667eea 0%, #764ba2 50%, #11998e 100%);
}

.decoration-line {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 100px;
    height: 1px;
    background: linear-gradient(90deg, transparent, #667eea, transparent);
    opacity: 0.5;
}

.decoration-dots {
    position: absolute;
    bottom: 1rem;
    right: 2rem;
    display: flex;
    gap: 0.5rem;
}

.decoration-dots span {
    width: 4px;
    height: 4px;
    background: #64748b;
    border-radius: 50%;
    animation: pulse 2s infinite;
}

.decoration-dots span:nth-child(2) {
    animation-delay: 0.5s;
}

.decoration-dots span:nth-child(3) {
    animation-delay: 1s;
}

@keyframes pulse {
    0%, 100% {
        opacity: 0.3;
        transform: scale(1);
    }
    50% {
        opacity: 1;
        transform: scale(1.2);
    }
}

/* 반응형 디자인 */
@media (max-width: 1024px) {
    .footer-main {
        grid-template-columns: 1fr;
        gap: 2rem;
    }
    
    .footer-links {
        grid-template-columns: repeat(2, 1fr);
    }
}

@media (max-width: 768px) {
    .footer-content {
        padding: 3rem 1rem 1.5rem;
    }
    
    .footer-links {
        grid-template-columns: 1fr;
        gap: 1.5rem;
    }
    
    .footer-bottom {
        flex-direction: column;
        gap: 1rem;
        text-align: center;
    }
    
    .social-links {
        justify-content: center;
    }
    
    .decoration-dots {
        right: 1rem;
    }
}

@media (max-width: 480px) {
    .footer-brand {
        flex-direction: column;
        align-items: center;
        text-align: center;
    }
    
    .footer-meta {
        flex-direction: column;
        gap: 0.25rem;
    }
    
    .divider {
        display: none;
    }
}
</style>