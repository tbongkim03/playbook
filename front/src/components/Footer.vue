<template>
    <footer class="site-footer">
        <div class="footer-inner">
            <div class="footer-brand">
                <img src="@/assets/playbook_logo-removebg-preview.png" alt="PLAYBOOK" class="footer-logo" />
                <span class="footer-tagline">플레이데이터 캠퍼스 라운지 도서 관리</span>
            </div>

            <nav class="footer-nav" aria-label="바닥글">
                <div class="footer-group">
                    <span class="footer-group-title">서비스</span>
                    <a href="borrow" @click="handleBorrowReturnClick">도서 대출</a>
                    <a href="return" @click="handleBorrowReturnClick">도서 반납</a>
                </div>
                <div class="footer-group">
                    <span class="footer-group-title">안내</span>
                    <a href="/service/terms" @click="navigateToPage">이용 약관</a>
                    <a href="/service/info" @click="navigateToPage" class="is-strong">개인정보 처리방침</a>
                    <a href="/service/alarm" @click="navigateToPage">알림 수신 안내</a>
                </div>
                <div class="footer-group">
                    <span class="footer-group-title">플레이데이터</span>
                    <a href="https://playdata.io/" target="_blank" rel="noopener">홈페이지</a>
                    <a href="https://www.en-core.com/resource/playdata" target="_blank" rel="noopener">회사 소개</a>
                    <a href="https://www.youtube.com/@%EC%97%94%EC%BD%94%EC%95%84" target="_blank" rel="noopener">유튜브</a>
                </div>
            </nav>
        </div>
        <div class="footer-bottom">
            <span>© {{ year }} 플레이데이터. All rights reserved.</span>
        </div>
    </footer>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { swAlert } from '@/utils/sweetAlert'

const year = new Date().getFullYear()
const router = useRouter()

function navigateToPage(event) {
  event.preventDefault()
  const href = event.currentTarget.getAttribute('href')
  router.push(href)
}

async function handleBorrowReturnClick(event) {
  event.preventDefault()
  const href = event.currentTarget.getAttribute('href')
  
  // 로그인 체크
  if (!sessionStorage.getItem('userType')) {
    await swAlert('로그인이 필요합니다.', 'info')
    router.push('/login')
    return
  }
  
  router.push(href)
}

</script>

<style scoped>
.site-footer {
    margin-top: auto;
    background: var(--pb-color-surface);
    border-top: 1px solid var(--pb-color-border);
    color: var(--pb-color-text-muted);
    font-size: 13px;
}

.footer-inner {
    width: min(100% - 48px, var(--pb-content-max));
    margin: 0 auto;
    padding: 28px 0 20px;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 32px;
}

.footer-brand {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.footer-logo {
    height: 22px;
    width: auto;
    align-self: flex-start;
}

.footer-tagline {
    color: var(--pb-color-text-soft);
}

.footer-nav {
    display: flex;
    gap: 48px;
}

.footer-group {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.footer-group-title {
    font-size: 12px;
    font-weight: 600;
    color: var(--pb-color-heading);
    margin-bottom: 2px;
}

.footer-group a {
    color: var(--pb-color-text-muted);
    text-decoration: none;
}

.footer-group a:hover {
    color: var(--pb-color-brand);
}

/* 개인정보 처리방침은 법적으로 눈에 띄게 표시한다 */
.footer-group a.is-strong {
    font-weight: 600;
    color: var(--pb-color-text);
}

.footer-bottom {
    width: min(100% - 48px, var(--pb-content-max));
    margin: 0 auto;
    padding: 14px 0 20px;
    border-top: 1px solid var(--pb-color-border);
    font-size: 12px;
    color: var(--pb-color-text-soft);
}

@media (max-width: 768px) {
    .footer-inner,
    .footer-bottom {
        width: calc(100% - 32px);
    }

    .footer-inner {
        flex-direction: column;
        gap: 20px;
        padding-top: 24px;
    }

    .footer-nav {
        width: 100%;
        display: grid;
        grid-template-columns: repeat(3, minmax(0, 1fr));
        gap: 16px;
    }

    /* 손가락으로 누르기 좋은 높이 */
    .footer-group a {
        padding: 4px 0;
    }
}
</style>
