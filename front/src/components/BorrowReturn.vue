<template>
    <div class="modal-overlay" @click="close">
        <div class="modal-content" @click.stop>
            <div class="modal-container">
                <div class="modal-header">
                    <div class="logo">
                        <img src="@/assets/playbook_logo.png" alt="Logo" class="logo-img" @click="navigate" />
                    </div>
                    <button class="close-btn" @click="close">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                    </button>
                </div>
                <div class="function-area">
                    <div class="function-card borrow-card" @click="navigateTo('/borrow')">
                        <div class="card-icon">
                            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <path d="M9 7h6M9 11h6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </div>
                        <h2 class="card-title">도서 대출</h2>
                        <p class="card-description">새로운 책을 대출해보세요</p>
                    </div>
                    <div class="function-card return-card" @click="navigateTo('/return')">
                        <div class="card-icon">
                            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <path d="M15 7L12 10L9 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </div>
                        <h2 class="card-title">도서 반납</h2>
                        <p class="card-description">대출한 책을 반납하세요</p>
                    </div>
                </div>
            </div>        
        </div>
    </div>
</template>

<script setup>
import router from '@/router'
import { swAlert } from '@/utils/sweetAlert'

const emit = defineEmits(['close'])

function close() {
  emit('close')
}
const navigateTo = async (r) => {
  // 로그인 체크
  if (!sessionStorage.getItem('userType')) {
    close()
    await swAlert('로그인이 필요합니다.', 'info')
    router.push('/login')
    return
  }

  close()
  router.push(r)
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(30, 31, 29, 0.45);
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-lg);
  width: min(90vw, 680px);
  max-height: 90vh;
  box-shadow: var(--pb-shadow-popover);
  position: relative;
  z-index: 10000;
  overflow: hidden;
}

.modal-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  padding: 24px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--pb-color-border);
}

.logo-img {
  height: 26px;
  width: auto;
  cursor: pointer;
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface);
  border-radius: var(--pb-radius-sm);
  color: var(--pb-color-text-muted);
  cursor: pointer;
  transition: background 0.12s, color 0.12s;
}
.close-btn:hover {
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text);
}

.function-area {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  flex: 1;
  align-items: stretch;
}

.function-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 64px 24px;
  border-radius: var(--pb-radius-lg);
  cursor: pointer;
  transition: background 0.12s ease, border-color 0.12s ease;
  border: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface-subtle);
}
.function-card:hover {
  background: var(--pb-color-surface-muted);
  border-color: var(--pb-color-border-strong);
}

.borrow-card {
  background: var(--pb-color-brand-soft);
  border-color: var(--pb-color-brand-muted);
  color: var(--pb-color-brand-strong);
}
.borrow-card:hover {
  background: var(--pb-color-brand);
  border-color: var(--pb-color-brand);
  color: #fff;
}

.return-card {
  background: var(--pb-color-success-soft);
  border-color: var(--pb-color-success);
  color: var(--pb-color-success);
}
.return-card:hover {
  background: var(--pb-color-success);
  border-color: var(--pb-color-success);
  color: #fff;
}

.card-icon {
  margin-bottom: 16px;
}

.card-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 6px 0;
}

.card-description {
  font-size: 0.85rem;
  margin: 0;
  opacity: 0.8;
  font-weight: 400;
  line-height: 1.5;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .modal-content {
    width: calc(100vw - 32px);
    margin: 0 16px;
  }

  .modal-container {
    padding: 24px;
  }

  .function-area {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .function-card {
    padding: 40px 24px;
  }

  .card-title {
    font-size: 1.25rem;
  }

  .modal-header {
    margin-bottom: 32px;
  }
}

@media (max-width: 480px) {
  .modal-content {
    width: calc(100vw - 32px);
    margin: 0 16px;
    border-radius: 16px;
  }

  .function-card {
    padding: 32px 20px;
  }
}
</style>