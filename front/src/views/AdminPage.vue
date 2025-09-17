<template>
  <div class="admin-dashboard">
    <div class="dashboard-header">
      <h1 class="dashboard-title">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        관리자 대시보드
      </h1>
      <p class="dashboard-subtitle">시스템 관리 및 모니터링</p>
    </div>

    <div class="dashboard-content">
      <!-- 네비게이션 메뉴 -->
      <nav class="admin-nav">
        <div class="nav-section">
          <h3 class="nav-title">관리 메뉴</h3>
          <ul class="nav-list">
            <li>
              <button 
                class="nav-item" 
                :class="{ active: activeTab === 'admin-accounts' }"
                @click="setActiveTab('admin-accounts')"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M16 21V19C16 17.9391 15.5786 16.9217 14.8284 16.1716C14.0783 15.4214 13.0609 15 12 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <circle cx="8.5" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <polyline points="17,11 19,13 23,9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                관리자 계정 관리
              </button>
            </li>
            <li>
              <button 
                class="nav-item" 
                :class="{ active: activeTab === 'user-accounts' }"
                @click="setActiveTab('user-accounts')"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M16 21V19C16 17.9391 15.5786 16.9217 14.8284 16.1716C14.0783 15.4214 13.0609 15 12 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <circle cx="8.5" cy="7" r="4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <polyline points="17,11 19,13 23,9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                학생 계정 관리
              </button>
            </li>
            <li>
              <button 
                class="nav-item" 
                :class="{ active: activeTab === 'books' }"
                @click="setActiveTab('books')"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M4 19.5C4 18.1193 5.11929 17 6.5 17H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M6.5 2H20V22H6.5C5.11929 22 4 20.8807 4 19.5V4.5C4 3.11929 5.11929 2 6.5 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                도서 관리
              </button>
            </li>
            <li>
              <button 
                class="nav-item" 
                :class="{ active: activeTab === 'rental-history' }"
                @click="setActiveTab('rental-history')"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2" stroke="currentColor" stroke-width="2"/>
                  <line x1="16" y1="2" x2="16" y2="6" stroke="currentColor" stroke-width="2"/>
                  <line x1="8" y1="2" x2="8" y2="6" stroke="currentColor" stroke-width="2"/>
                  <line x1="3" y1="10" x2="21" y2="10" stroke="currentColor" stroke-width="2"/>
                  <path d="M8 14L10 16L16 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                대출/반납 히스토리
              </button>
            </li>
            <li>
              <button 
                class="nav-item" 
                :class="{ active: activeTab === 'statistics' }"
                @click="setActiveTab('statistics')"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M3 3V21H21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M9 9L12 6L16 10L20 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                통계 대시보드
              </button>
            </li>
          </ul>
        </div>
      </nav>

      <!-- 메인 컨텐츠 영역 -->
      <main class="admin-main">
        <!-- 관리자 계정 관리 -->
        <div v-if="activeTab === 'admin-accounts'" class="content-section">
          <AdminAccountManagement />
        </div>

        <!-- 사용자 계정 관리 -->
        <div v-if="activeTab === 'user-accounts'" class="content-section">
          <UserAccountManagement />
        </div>

        <!-- 도서 관리 -->
        <div v-if="activeTab === 'books'" class="content-section">
          <BooksTable ref="booksTableRef" @open-register-modal="openRegisterModal" />
        </div>

        <!-- 대출/반납 히스토리 -->
        <div v-if="activeTab === 'rental-history'" class="content-section">
          <RentalHistoryDashboard />
        </div>
        <!-- 통계 대시보드 -->
        <div v-if="activeTab === 'statistics'" class="content-section">
          <StatisticsDashboard />
        </div>
      </main>
    </div>

    <!-- 도서 등록 모달 -->
    <div v-if="showRegisterModal" class="modal-overlay" @click="closeRegisterModal">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <h2 class="modal-title">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M4 19.5C4 18.1193 5.11929 17 6.5 17H20" stroke="currentColor" stroke-width="2"/>
              <path d="M6.5 2H20V22H6.5C5.11929 22 4 20.8807 4 19.5V4.5C4 3.11929 5.11929 2 6.5 2Z" stroke="currentColor" stroke-width="2"/>
            </svg>
            도서 등록
          </h2>
          <button class="close-btn" @click="closeRegisterModal">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2"/>
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2"/>
            </svg>
          </button>
        </div>
        
        <div class="modal-content">
          <BookRegister />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import AdminAccountManagement from '@/components/AdminAccountManagement.vue'
import RentalHistoryDashboard from '@/components/RentalHistoryDashboard.vue'
import StatisticsDashboard from '@/components/StatisticsDashboard.vue'
import BooksTable from '@/components/BooksTable.vue'
import BookRegister from './BookRegister.vue'
import UserAccountManagement from '@/components/UserAccountManagement.vue'

const router = useRouter()
const activeTab = ref('admin-accounts')
const showRegisterModal = ref(false)
const booksTableRef = ref(null)

const handleKeydown = (event) => {
  if (event.key === 'Escape' && showRegisterModal.value) {
    showRegisterModal.value = false
  }
}

const setActiveTab = (tab) => {
  activeTab.value = tab
}

const openRegisterModal = () => {
  showRegisterModal.value = true
}

const closeRegisterModal = () => {
  showRegisterModal.value = false
}

// 관리자 권한 확인
const checkAdminAuth = () => {
  const token = localStorage.getItem('jwtToken')
  const userType = localStorage.getItem('userType')
  
  if (!token || userType !== 'admin') {
    alert('관리자 권한이 필요합니다.')
    router.push('/login')
    return false
  }
  return true
}

onMounted(() => {
  checkAdminAuth()
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  padding: 80px 0 40px;
}

.dashboard-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 0 20px;
}

.dashboard-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-size: 2.5rem;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 8px;
}

.dashboard-subtitle {
  font-size: 1.1rem;
  color: #718096;
  margin: 0;
}

.dashboard-content {
  width: 98%;
  margin: 0 auto;
  padding: 0.7rem;
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 32px;
  max-width: none;
}

.admin-nav {
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.03);
  height: fit-content;
}

.nav-section {
  margin-bottom: 24px;
}

.nav-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f1f5f9;
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 14px 18px;
  border: none;
  background: transparent;
  color: #4a5568;
  font-size: 0.95rem;
  font-weight: 500;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 6px;
  text-align: left;
}

.nav-item:hover {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  color: #2d3748;
  transform: translateX(4px);
}

.nav-item.active {
  background: linear-gradient(135deg, #a8dadc 0%, #b8e6c1 100%);
  color: #2d3748;
  box-shadow: 0 4px 16px rgba(168, 218, 220, 0.3);
  transform: translateX(4px);
}

.admin-main {
  background: white;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.03);
  overflow: hidden;
}

.content-section {
  padding: 24px;
}

/* 모달 스타일 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  backdrop-filter: blur(8px);
}

.modal-container {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  max-width: 1200px;
  width: 100%;
  max-height: 90vh;
  margin-top: 4rem;
  overflow-y: auto;
  animation: modalSlideIn 0.3s ease-out;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  border-bottom: 1px solid #f1f5f9;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 20px 20px 0 0;
}

.modal-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.5rem;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
}

.close-btn {
  background: linear-gradient(135deg, #e2e8f0 0%, #f1f5f9 100%);
  border: none;
  color: #4a5568;
  cursor: pointer;
  padding: 8px;
  border-radius: 12px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  box-shadow: 0 2px 8px rgba(226, 232, 240, 0.3);
}

.close-btn:hover {
  background: linear-gradient(135deg, #cbd5e0 0%, #e2e8f0 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(226, 232, 240, 0.4);
}

.modal-content {
  padding: 32px;
}

/* 반응형 디자인 */
@media (max-width: 1024px) {
  .dashboard-content {
    grid-template-columns: 1fr;
    gap: 24px;
  }
  
  .admin-nav {
    order: 2;
  }
  
  .admin-main {
    order: 1;
  }

  .modal-container {
    max-width: 95%;
    margin: 10px;
  }

  .modal-content {
    padding: 24px;
  }
}

@media (max-width: 768px) {
  .dashboard-title {
    font-size: 2rem;
  }
  
  .content-section {
    padding: 16px;
  }
  
  .dashboard-content {
    width: 100%;
    padding: 0 10px;
  }

  .admin-nav {
    padding: 16px;
    border-radius: 16px;
  }

  .nav-item {
    padding: 12px 16px;
  }

  .modal-header {
    padding: 16px 24px;
  }

  .modal-content {
    padding: 16px;
  }

  .close-btn {
    width: 32px;
    height: 32px;
  }
}

@media (max-width: 480px) {
  .dashboard-header {
    margin-bottom: 24px;
  }

  .dashboard-title {
    font-size: 1.75rem;
  }

  .dashboard-subtitle {
    font-size: 1rem;
  }

  .nav-title {
    font-size: 1rem;
  }

  .nav-item {
    font-size: 0.9rem;
    padding: 10px 14px;
  }

  .modal-header {
    padding: 12px 16px;
  }

  .modal-title {
    font-size: 1.25rem;
  }

  .modal-content {
    padding: 12px;
  }

  .admin-main {
    border-radius: 16px;
  }

  .admin-nav {
    border-radius: 16px;
  }
}</style>