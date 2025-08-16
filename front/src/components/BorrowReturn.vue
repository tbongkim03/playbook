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
                        <p class="card-description">새로운 책을 대여해보세요</p>
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
                        <p class="card-description">대여한 책을 반납하세요</p>
                    </div>
                </div>
            </div>        
        </div>
    </div>
</template>

<script setup>
import router from '@/router'

const emit = defineEmits(['close'])
function close() {
  emit('close')
}
const navigateTo = (r) => {
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
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-content {
  background: #ffffff;
  border-radius: 20px;
  padding: 0;
  width: min(90vw, 800px);
  max-height: 90vh;
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  position: relative;
  z-index: 10000;
  animation: slideUp 0.3s ease-out;
  overflow: hidden;
}

.modal-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  padding: 32px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f1f5f9;
}

.logo-img {
  max-width: 180px;
  height: auto;
  transition: all 0.3s ease;
  cursor: pointer;
}

.logo-img:hover {
  transform: scale(1.05);
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: none;
  background: #f8fafc;
  border-radius: 12px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #f1f5f9;
  color: #334155;
  transform: scale(1.05);
}

.function-area {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  flex: 1;
  align-items: center;
}

.function-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 48px 32px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  border: 2px solid transparent;
}

.function-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: inherit;
  padding: 2px;
  background: linear-gradient(135deg, transparent, rgba(255, 255, 255, 0.1));
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask-composite: xor;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.function-card:hover::before {
  opacity: 1;
}

.borrow-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 10px 25px -3px rgba(102, 126, 234, 0.3);
}

.borrow-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 20px 40px -3px rgba(102, 126, 234, 0.4);
}

.return-card {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
  box-shadow: 0 10px 25px -3px rgba(17, 153, 142, 0.3);
}

.return-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 20px 40px -3px rgba(17, 153, 142, 0.4);
}

.card-icon {
  margin-bottom: 24px;
  opacity: 0.9;
  transition: all 0.3s ease;
}

.function-card:hover .card-icon {
  opacity: 1;
  transform: scale(1.1);
}

.card-title {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0 0 12px 0;
  letter-spacing: -0.025em;
}

.card-description {
  font-size: 1rem;
  margin: 0;
  opacity: 0.9;
  font-weight: 400;
  line-height: 1.5;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
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
    width: 95vw;
    border-radius: 16px;
  }
  
  .function-card {
    padding: 32px 20px;
  }
}
</style>