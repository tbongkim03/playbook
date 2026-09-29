<template>
  <div class="logout-container">
    <div class="logout-content">
      <div class="logout-icon">
        <PhSignOut weight="duotone" :size="64" class="logout-svg" />
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
import { PhSignOut } from '@phosphor-icons/vue'
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