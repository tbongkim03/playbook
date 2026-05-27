<template>
  <div class="logout-container">
    <div class="logout-content">
      <div class="logout-icon">
        <svg 
          width="64" 
          height="64" 
          viewBox="0 0 24 24" 
          fill="none" 
          xmlns="http://www.w3.org/2000/svg"
          class="logout-svg"
        >
          <path 
            d="M9 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H9" 
            stroke="currentColor" 
            stroke-width="2" 
            stroke-linecap="round" 
            stroke-linejoin="round"
          />
          <polyline 
            points="16,17 21,12 16,7" 
            stroke="currentColor" 
            stroke-width="2" 
            stroke-linecap="round" 
            stroke-linejoin="round"
          />
          <line 
            x1="21" 
            y1="12" 
            x2="9" 
            y2="12" 
            stroke="currentColor" 
            stroke-width="2" 
            stroke-linecap="round" 
            stroke-linejoin="round"
          />
        </svg>
      </div>
      
      <div class="logout-message">
        <h2 class="logout-title">로그아웃 중...</h2>
        <p class="logout-description">안전하게 로그아웃하고 있습니다.</p>
      </div>
      
      <div class="loading-spinner">
        <div class="spinner"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import * as adminApi from '@/api/admin'
import * as userApi from '@/api/user'

const router = useRouter()

onMounted(async () => {
  const userType = sessionStorage.getItem('userType')
  try {
    if (userType === 'admin') await adminApi.logout()
    else await userApi.logout()
  } catch (e) {
    // 세션이 이미 만료된 경우 무시
  }

  sessionStorage.removeItem('userType')
  sessionStorage.removeItem('campusId')

  setTimeout(() => {
    router.push('/')
  }, 1200)
})
</script>

<style scoped>
.logout-container {
  min-height: calc(100vh - var(--pb-header-height));
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--pb-color-canvas);
  padding: 2rem;
}

.logout-content {
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-lg);
  padding: 3rem 2rem;
  text-align: center;
  box-shadow: var(--pb-shadow-md);
  max-width: 400px;
  width: 100%;
  animation: slideUp 0.4s ease-out;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.logout-icon {
  margin-bottom: 1.5rem;
  display: flex;
  justify-content: center;
}

.logout-svg {
  color: var(--pb-color-danger);
}

.logout-message {
  margin-bottom: 2rem;
}

.logout-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--pb-color-heading);
  margin-bottom: 0.5rem;
}

.logout-description {
  font-size: 1rem;
  color: var(--pb-color-text-muted);
  margin: 0;
}

.loading-spinner {
  display: flex;
  justify-content: center;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--pb-color-border);
  border-top: 3px solid var(--pb-color-brand);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .logout-container { padding: 1.5rem; }
  .logout-content { padding: 2rem 1.5rem; }
  .logout-title { font-size: 1.5rem; }
  .logout-description { font-size: 0.9rem; }
  .logout-svg { width: 48px; height: 48px; }
}

@media (max-width: 480px) {
  .logout-content { padding: 1.5rem 1rem; }
  .logout-title { font-size: 1.25rem; }
  .logout-svg { width: 40px; height: 40px; }
}
</style>