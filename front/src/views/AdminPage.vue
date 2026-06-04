<template>
  <div class="admin-dashboard">
    <div class="dashboard-content">
      <!-- 네비게이션 메뉴 -->
      <nav class="admin-nav">
        <div class="sidebar-header">
          <div class="sidebar-icon">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div>
            <div class="sidebar-title">관리자 대시보드</div>
            <div class="sidebar-subtitle">시스템 관리 및 모니터링</div>
          </div>
        </div>
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
            <li>
              <button
                class="nav-item"
                :class="{ active: activeTab === 'course-management' }"
                @click="setActiveTab('course-management')"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                과정 관리
              </button>
            </li>
            <li>
              <button
                class="nav-item"
                :class="{ active: activeTab === 'campus-management' }"
                @click="setActiveTab('campus-management')"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M21 10C21 17 12 23 12 23C12 23 3 17 3 10C3 7.61305 3.94821 5.32387 5.63604 3.63604C7.32387 1.94821 9.61305 1 12 1C14.3869 1 16.6761 1.94821 18.364 3.63604C20.0518 5.32387 21 7.61305 21 10Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <circle cx="12" cy="10" r="3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                캠퍼스 관리
              </button>
            </li>
            <li>
              <button
                class="nav-item"
                :class="{ active: activeTab === 'terms-management' }"
                @click="setActiveTab('terms-management')"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <polyline points="14,2 14,8 20,8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <line x1="16" y1="13" x2="8" y2="13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <line x1="16" y1="17" x2="8" y2="17" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                약관 관리
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
        <!-- 과정 관리 -->
        <div v-if="activeTab === 'course-management'" class="content-section">
          <CourseManagement />
        </div>
        <!-- 캠퍼스 관리 -->
        <div v-if="activeTab === 'campus-management'" class="content-section">
          <CampusManagement />
        </div>
        <!-- 약관 관리 -->
        <div v-if="activeTab === 'terms-management'" class="content-section">
          <TermsEditor />
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
import { swAlert } from '@/utils/sweetAlert'
import AdminAccountManagement from '@/components/AdminAccountManagement.vue'
import RentalHistoryDashboard from '@/components/RentalHistoryDashboard.vue'
import StatisticsDashboard from '@/components/StatisticsDashboard.vue'
import BooksTable from '@/components/BooksTable.vue'
import BookRegister from './BookRegister.vue'
import UserAccountManagement from '@/components/UserAccountManagement.vue'
import CourseManagement from '@/components/CourseManagement.vue'
import CampusManagement from '@/components/CampusManagement.vue'
import TermsEditor from '@/components/TermsEditor.vue'

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
const checkAdminAuth = async () => {
  const userType = sessionStorage.getItem('userType')

  if (userType !== 'admin') {
    await swAlert('관리자 권한이 필요합니다.', 'warning')
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
  background: var(--pb-color-canvas);
  padding: 72px 0 48px;
}

.dashboard-content {
  width: min(100% - 48px, var(--pb-content-max));
  margin: 0 auto;
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 20px;
  align-items: start;
}

.admin-nav {
  background: var(--pb-color-surface);
  border-radius: var(--pb-radius-md);
  padding: 0;
  box-shadow: var(--pb-shadow-xs);
  border: 1px solid var(--pb-color-border);
  height: fit-content;
  overflow: hidden;
}

/* 사이드바 헤더 */
.sidebar-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--pb-color-border);
}

.sidebar-icon {
  width: 30px;
  height: 30px;
  border-radius: var(--pb-radius-sm);
  background: var(--pb-color-accent-soft);
  color: var(--pb-color-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.sidebar-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--pb-color-heading);
  line-height: 1.3;
}

.sidebar-subtitle {
  font-size: 11px;
  color: var(--pb-color-text-soft);
  line-height: 1.3;
}

.nav-section {
  padding: 8px;
}

.nav-title {
  font-size: 10px;
  font-weight: 600;
  color: var(--pb-color-text-soft);
  margin-bottom: 4px;
  padding: 4px 6px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 14px;
  border: none;
  background: transparent;
  color: var(--pb-color-text);
  font-size: 0.9rem;
  font-weight: 500;
  border-radius: var(--pb-radius-sm);
  cursor: pointer;
  transition: background 0.15s ease, color 0.15s ease;
  margin-bottom: 2px;
  text-align: left;
}

.nav-item:hover {
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text);
}

.nav-item.active {
  background: var(--pb-color-brand-soft);
  color: var(--pb-color-brand);
  font-weight: 600;
}

.admin-main {
  background: var(--pb-color-surface);
  border-radius: var(--pb-radius-md);
  box-shadow: var(--pb-shadow-sm);
  border: 1px solid var(--pb-color-border);
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
}

.modal-container {
  background: var(--pb-color-surface);
  border-radius: var(--pb-radius-md);
  box-shadow: var(--pb-shadow-md);
  max-width: 1200px;
  width: 100%;
  max-height: 90vh;
  margin-top: 4rem;
  overflow-y: auto;
  border: 1px solid var(--pb-color-border);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 28px;
  border-bottom: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface-muted);
}

.modal-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0;
}

.close-btn {
  background: var(--pb-color-surface-muted);
  border: 1px solid var(--pb-color-border);
  color: var(--pb-color-text-muted);
  cursor: pointer;
  padding: 6px;
  border-radius: var(--pb-radius-sm);
  transition: background 0.15s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
}

.close-btn:hover {
  background: var(--pb-color-border);
  color: var(--pb-color-text);
}

.modal-content {
  padding: 28px;
}

/* 반응형 디자인 */
@media (max-width: 1024px) {
  .dashboard-content {
    grid-template-columns: 1fr;
    gap: 20px;
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
    padding: 20px;
  }
}

@media (max-width: 768px) {
  .content-section {
    padding: 16px;
  }

  .dashboard-content {
    width: 100%;
    padding: 0;
  }

  /* 모바일: 사이드바 → 상단 가로 탭 전환 */
  .admin-nav {
    order: 0;
    border-radius: 0;
    border-left: none;
    border-right: none;
    border-top: none;
    box-shadow: none;
    padding: 0;
    position: sticky;
    top: var(--pb-header-height);
    z-index: 100;
  }

  .admin-main {
    order: 1;
    border-radius: 0;
    border-left: none;
    border-right: none;
  }

  .sidebar-header,
  .nav-title {
    display: none;
  }

  .nav-section {
    padding: 0;
  }

  .nav-list {
    display: flex;
    flex-direction: row;
    overflow-x: auto;
    scrollbar-width: none;
    -ms-overflow-style: none;
    gap: 2px;
    padding: 6px 8px;
    white-space: nowrap;
  }

  .nav-list::-webkit-scrollbar {
    display: none;
  }

  .nav-item {
    flex-direction: column;
    flex-shrink: 0;
    width: auto;
    padding: 8px 12px;
    font-size: 0.78rem;
    gap: 4px;
    min-height: 56px;
    text-align: center;
    border-radius: var(--pb-radius-sm);
    margin-bottom: 0;
    white-space: nowrap;
  }

  .nav-item svg {
    width: 18px;
    height: 18px;
  }

  .modal-header {
    padding: 16px 20px;
  }

  .modal-content {
    padding: 16px;
  }

  .close-btn {
    width: 44px;
    height: 44px;
  }
}

@media (max-width: 480px) {
  .dashboard-header {
    margin-bottom: 16px;
  }

  .nav-item {
    font-size: 0.875rem;
    padding: 9px 12px;
  }

  .modal-header {
    padding: 12px 16px;
  }

  .modal-content {
    padding: 12px;
  }
}</style>