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

const router = useRouter()

onMounted(() => {
  localStorage.removeItem("jwtToken")
  localStorage.removeItem("userType")
  
  setTimeout(() => {
    router.push('/')
  }, 1200)
})
</script>

<style scoped>
.logout-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 80px 20px 20px;
  margin-top: -6rem;
  position: relative;
  overflow: hidden;
}

.logout-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    rgba(255, 255, 255, 0.1) 0%,
    transparent 50%,
    rgba(255, 255, 255, 0.05) 100%
  );
  animation: shimmer 3s ease-in-out infinite;
}

@keyframes shimmer {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.logout-content {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 3rem 2rem;
  text-align: center;
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.1),
    0 0 0 1px rgba(255, 255, 255, 0.2);
  max-width: 400px;
  width: 100%;
  animation: slideUp 0.6s ease-out;
  position: relative;
  z-index: 1;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.logout-icon {
  margin-bottom: 1.5rem;
  display: flex;
  justify-content: center;
}

.logout-svg {
  color: #dc2626;
  animation: fadeIn 0.8s ease-out 0.2s both;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.logout-message {
  margin-bottom: 2rem;
}

.logout-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.5rem;
  animation: fadeIn 0.8s ease-out 0.4s both;
}

.logout-description {
  font-size: 1rem;
  color: #6b7280;
  margin: 0;
  animation: fadeIn 0.8s ease-out 0.6s both;
}

.loading-spinner {
  display: flex;
  justify-content: center;
  animation: fadeIn 0.8s ease-out 0.8s both;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e5e7eb;
  border-top: 3px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .logout-container {
    padding: 60px 15px 15px;
  }
  
  .logout-content {
    padding: 2rem 1.5rem;
    border-radius: 16px;
  }
  
  .logout-title {
    font-size: 1.5rem;
  }
  
  .logout-description {
    font-size: 0.9rem;
  }
  
  .logout-svg {
    width: 48px;
    height: 48px;
  }
}

@media (max-width: 480px) {
  .logout-content {
    padding: 1.5rem 1rem;
  }
  
  .logout-title {
    font-size: 1.25rem;
  }
  
  .logout-svg {
    width: 40px;
    height: 40px;
  }
}

/* 다크모드 대응 */
@media (prefers-color-scheme: dark) {
  .logout-content {
    background: rgba(31, 41, 55, 0.95);
  }
  
  .logout-title {
    color: #f9fafb;
  }
  
  .logout-description {
    color: #d1d5db;
  }
  
  .spinner {
    border-color: #4b5563;
    border-top-color: #667eea;
  }
}
</style>