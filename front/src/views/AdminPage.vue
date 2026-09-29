<template>
  <div class="admin-dashboard">
    <div class="dashboard-content">
      <!-- 네비게이션 메뉴 -->
      <nav class="admin-nav" :class="{ 'is-open': navOpen }">
        <!-- 태블릿·모바일: 헤더 아래 고정 바. 누르면 메뉴판이 펼쳐진다 -->
        <button type="button" class="nav-toggle" :aria-expanded="navOpen" @click="navOpen = !navOpen">
          <span class="nav-toggle-label">
            <span class="nav-toggle-kicker">관리 메뉴</span>
            <span class="nav-toggle-current">{{ TAB_LABELS[activeTab] }}</span>
          </span>
          <PhCaretDown weight="duotone" :size="18" class="nav-toggle-chevron" aria-hidden="true" />
        </button>
        <div class="sidebar-header">
          <div class="sidebar-icon">
            <PhSquaresFour weight="fill" :size="16" />
          </div>
          <div>
            <div class="sidebar-title">관리자 대시보드</div>
            <div class="sidebar-subtitle">시스템 관리 및 모니터링</div>
          </div>
        </div>
        <div class="nav-section">
          <ul class="nav-list">
            <li class="nav-group-title">계정</li>
            <li>
              <button 
                class="nav-item" 
                :class="{ active: activeTab === 'admin-accounts' }"
                @click="setActiveTab('admin-accounts')"
              >
                <PhUserGear weight="fill" :size="20" />
                관리자 계정 관리
              </button>
            </li>
            <li>
              <button 
                class="nav-item" 
                :class="{ active: activeTab === 'user-accounts' }"
                @click="setActiveTab('user-accounts')"
              >
                <PhStudent weight="fill" :size="20" />
                학생 계정 관리
              </button>
            </li>
            <li class="nav-group-title">도서 · 대출</li>
            <li>
              <button 
                class="nav-item" 
                :class="{ active: activeTab === 'books' }"
                @click="setActiveTab('books')"
              >
                <PhBooks weight="fill" :size="20" />
                도서 관리
              </button>
            </li>
            <li>
              <button 
                class="nav-item" 
                :class="{ active: activeTab === 'rental-history' }"
                @click="setActiveTab('rental-history')"
              >
                <PhClockCounterClockwise weight="fill" :size="20" />
                대출/반납 히스토리
              </button>
            </li>
            <li>
              <button 
                class="nav-item" 
                :class="{ active: activeTab === 'statistics' }"
                @click="setActiveTab('statistics')"
              >
                <PhChartBar weight="fill" :size="20" />
                통계 대시보드
              </button>
            </li>
            <li class="nav-group-title">기준 정보</li>
            <li>
              <button
                class="nav-item"
                :class="{ active: activeTab === 'course-management' }"
                @click="setActiveTab('course-management')"
              >
                <PhGraduationCap weight="fill" :size="20" />
                과정 관리
              </button>
            </li>
            <li>
              <button
                class="nav-item"
                :class="{ active: activeTab === 'campus-management' }"
                @click="setActiveTab('campus-management')"
              >
                <PhMapPin weight="fill" :size="20" />
                캠퍼스 관리
              </button>
            </li>
            <li>
              <button
                class="nav-item"
                :class="{ active: activeTab === 'category-management' }"
                @click="setActiveTab('category-management')"
              >
                <PhFolders weight="fill" :size="20" />
                카테고리 관리
              </button>
            </li>
            <li>
              <button
                class="nav-item"
                :class="{ active: activeTab === 'terms-management' }"
                @click="setActiveTab('terms-management')"
              >
                <PhFileText weight="fill" :size="20" />
                약관 관리
              </button>
            </li>
            <li class="nav-group-title">보안 · 기록</li>
            <li>
              <button
                class="nav-item"
                :class="{ active: activeTab === 'access-log' }"
                @click="setActiveTab('access-log')"
              >
                <PhSignIn weight="fill" :size="20" />
                접속 이력
              </button>
            </li>
            <li>
              <button
                class="nav-item"
                :class="{ active: activeTab === 'audit-log' }"
                @click="setActiveTab('audit-log')"
              >
                <PhListChecks weight="fill" :size="20" />
                감사 로그
              </button>
            </li>
            <!-- 허용 IP 관리 (전체관리자·캠퍼스관리자 공통) -->
            <li>
              <button
                class="nav-item"
                :class="{ active: activeTab === 'allowed-ip' }"
                @click="setActiveTab('allowed-ip')"
              >
                <PhShieldCheck weight="fill" :size="20" />
                허용 IP 관리
              </button>
            </li>
            <!-- 연동 관리 (전체관리자 전용) -->
            <li v-if="isSuperAdmin">
              <button
                class="nav-item"
                :class="{ active: activeTab === 'integration' }"
                @click="setActiveTab('integration')"
              >
                <PhPlugsConnected weight="fill" :size="20" />
                연동 관리
              </button>
            </li>
          </ul>
        </div>
      </nav>

      <!-- 메인 컨텐츠 영역 -->
      <main ref="adminMainRef" class="admin-main">
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
        <!-- 카테고리 관리 -->
        <div v-if="activeTab === 'category-management'" class="content-section">
          <CategoryManagement />
        </div>
        <!-- 약관 관리 -->
        <div v-if="activeTab === 'terms-management'" class="content-section">
          <TermsEditor />
        </div>
        <!-- 접속 이력 -->
        <div v-if="activeTab === 'access-log'" class="content-section">
          <AccessLogDashboard />
        </div>
        <!-- 감사 로그 -->
        <div v-if="activeTab === 'audit-log'" class="content-section">
          <AuditLogDashboard />
        </div>
        <!-- 허용 IP 관리 (전체관리자·캠퍼스관리자 공통) -->
        <div v-if="activeTab === 'allowed-ip'" class="content-section">
          <AllowedIpManagement />
        </div>
        <!-- 연동 관리 (전체관리자 전용) -->
        <div v-if="activeTab === 'integration' && isSuperAdmin" class="content-section">
          <IntegrationManagement />
        </div>
      </main>
    </div>

    <!-- 도서 등록 모달 -->
    <div v-if="showRegisterModal" class="modal-overlay" v-modal-backdrop="closeRegisterModal">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <h2 class="modal-title">
            <PhBook weight="duotone" :size="24" />
            도서 등록
          </h2>
          <button class="close-btn" @click="closeRegisterModal">
            <PhX weight="duotone" :size="24" />
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
import { vModalBackdrop } from '@/utils/modalBackdrop'
import { PhBook, PhBooks, PhCaretDown, PhChartBar, PhClockCounterClockwise, PhFileText, PhFolders, PhGraduationCap, PhListChecks, PhMapPin, PhPlugsConnected, PhShieldCheck, PhSignIn, PhSquaresFour, PhStudent, PhUserGear, PhX } from '@phosphor-icons/vue'
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
import CategoryManagement from '@/components/CategoryManagement.vue'
import TermsEditor from '@/components/TermsEditor.vue'
import AccessLogDashboard from '@/components/AccessLogDashboard.vue'
import AuditLogDashboard from '@/components/AuditLogDashboard.vue'
import AllowedIpManagement from '@/components/AllowedIpManagement.vue'
import IntegrationManagement from '@/components/IntegrationManagement.vue'
import * as adminApi from '@/api/admin'
import { useTableCardLabels } from '@/composables/useTableCardLabels'

const router = useRouter()
const activeTab = ref('admin-accounts')
const showRegisterModal = ref(false)
const booksTableRef = ref(null)
const isSuperAdmin = ref(false)

const handleKeydown = (event) => {
  if (event.key === 'Escape' && showRegisterModal.value) {
    showRegisterModal.value = false
  }
}

const navOpen = ref(false)
const adminMainRef = ref(null)
useTableCardLabels(adminMainRef)

// 모바일 메뉴 바에 현재 위치를 보여주기 위한 이름표
const TAB_LABELS = {
  'admin-accounts': '관리자 계정 관리',
  'user-accounts': '학생 계정 관리',
  books: '도서 관리',
  'rental-history': '대출/반납 히스토리',
  statistics: '통계 대시보드',
  'course-management': '과정 관리',
  'campus-management': '캠퍼스 관리',
  'category-management': '카테고리 관리',
  'terms-management': '약관 관리',
  'access-log': '접속 이력',
  'audit-log': '감사 로그',
  'allowed-ip': '허용 IP 관리',
  integration: '연동 관리'
}

const setActiveTab = (tab) => {
  activeTab.value = tab
  navOpen.value = false
  window.scrollTo({ top: 0 })
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

// 전체관리자(캠퍼스 미지정) 여부 확인 → 연동 관리 탭 노출 제어
const checkSuperAdmin = async () => {
  try {
    const res = await adminApi.getMe()
    isSuperAdmin.value = !res.data.data?.seqCampus
  } catch (e) {
    isSuperAdmin.value = false
  }
}

onMounted(async () => {
  const authorized = await checkAdminAuth()
  if (authorized) {
    await checkSuperAdmin()
  }
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
  padding: 24px 0 48px;
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
  position: sticky;
  top: calc(var(--pb-header-height) + 24px);
}

.nav-toggle {
  display: none;
}

.nav-group-title {
  padding: 12px 10px 4px;
  font-size: 11px;
  font-weight: 600;
  color: var(--pb-color-text-soft);
  letter-spacing: 0.02em;
}

.nav-group-title:first-child {
  padding-top: 4px;
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

/* 반응형 디자인 — 1024px 이하는 사이드바 대신 헤더 아래 고정 메뉴 바 */
@media (max-width: 1024px) {
  .admin-dashboard {
    padding-top: 0;
  }

  .dashboard-content {
    grid-template-columns: 1fr;
    gap: 16px;
    width: 100%;
  }

  .admin-nav {
    position: sticky;
    top: var(--pb-header-height);
    z-index: 1020;
    border-radius: 0;
    border-width: 0 0 1px;
    box-shadow: none;
    overflow: visible;
  }

  .nav-toggle {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    width: 100%;
    min-height: 52px;
    padding: 8px 16px;
    border: 0;
    background: var(--pb-color-surface);
    color: var(--pb-color-heading);
    text-align: left;
    cursor: pointer;
  }

  .nav-toggle-label {
    display: flex;
    flex-direction: column;
    line-height: 1.25;
  }

  .nav-toggle-kicker {
    font-size: 11px;
    color: var(--pb-color-text-soft);
  }

  .nav-toggle-current {
    font-size: 15px;
    font-weight: 600;
  }

  .nav-toggle-chevron {
    flex-shrink: 0;
    color: var(--pb-color-text-muted);
    transition: transform 0.18s ease;
  }

  .admin-nav.is-open .nav-toggle-chevron {
    transform: rotate(180deg);
  }

  .sidebar-header {
    display: none;
  }

  /* 메뉴판: 바 아래로 펼쳐져 본문 위에 뜬다 */
  .nav-section {
    display: none;
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    max-height: calc(100vh - var(--pb-header-height) - 52px);
    overflow-y: auto;
    padding: 8px 12px 16px;
    background: var(--pb-color-surface);
    border-bottom: 1px solid var(--pb-color-border);
    box-shadow: var(--pb-shadow-popover);
  }

  .admin-nav.is-open .nav-section {
    display: block;
  }

  .nav-list {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 2px 8px;
  }

  .nav-group-title {
    grid-column: 1 / -1;
  }

  .nav-item {
    min-height: 44px;
    margin-bottom: 0;
  }

  .admin-main {
    border-radius: 0;
    border-left: none;
    border-right: none;
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

  /* 요약 숫자 카드: 한 줄에 하나씩 세로로 길게 쌓이지 않게 2열로. 홀수 개면 마지막 카드가 한 줄을 다 쓴다 */
  .admin-main :deep(:is(.stats-grid, .stats-section)) {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
  }

  .admin-main :deep(:is(.stats-grid, .stats-section) > :last-child:nth-child(odd)) {
    grid-column: 1 / -1;
  }

  .admin-main :deep(:is(.stats-grid, .stats-section) > *) {
    min-width: 0;
    padding: 10px 12px;
    gap: 10px;
  }

  .admin-main :deep(:is(.stats-grid, .stats-section) > * :is(.stat-icon, [class*='-icon'])) {
    width: 32px;
    height: 32px;
    flex-shrink: 0;
  }

  /* 모바일 표 → 카드. 가로 스크롤 표는 한 행을 보려고 좌우로 계속 밀어야 해서 읽기 힘들다.
     탭마다 표 틀 이름이 달라 여기서 한 번에 잡고, 항목명은 useTableCardLabels 가 td 에 붙인 data-label 을 쓴다. */
  .admin-main :deep(:is(.table-wrapper, .table-container, .aip-table-wrap)) {
    overflow: visible;
    border: 0;
    background: transparent;
    box-shadow: none;
  }

  .admin-main :deep(:is(.table-wrapper, .table-container, .aip-table-wrap) table) {
    display: block;
    width: 100%;
    min-width: 0;
    border: 0;
    background: transparent;
  }

  .admin-main :deep(:is(.table-wrapper, .table-container, .aip-table-wrap) thead) {
    display: none;
  }

  .admin-main :deep(:is(.table-wrapper, .table-container, .aip-table-wrap) tbody) {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .admin-main :deep(:is(.table-wrapper, .table-container, .aip-table-wrap) tbody tr) {
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 12px 14px;
    background: var(--pb-color-surface);
    border: 1px solid var(--pb-color-border);
    border-radius: var(--pb-radius-md);
    height: auto;
  }

  .admin-main :deep(:is(.table-wrapper, .table-container, .aip-table-wrap) tbody td) {
    display: flex;
    align-items: baseline;
    gap: 12px;
    width: auto;
    max-width: none;
    min-width: 0;
    padding: 0;
    border: 0;
    background: transparent;
    white-space: normal;
    overflow: visible;
    text-overflow: clip;
    text-align: left;
    font-size: 14px;
    word-break: break-word;
  }

  .admin-main :deep(:is(.table-wrapper, .table-container, .aip-table-wrap) tbody td) {
    height: auto;
    min-height: 0;
  }

  .admin-main :deep(:is(.table-wrapper, .table-container, .aip-table-wrap) tbody td > *) {
    max-width: 100%;
  }

  .admin-main :deep(:is(.table-wrapper, .table-container, .aip-table-wrap) tbody td::before) {
    content: attr(data-label);
    flex: 0 0 76px;
    font-size: 12px;
    color: var(--pb-color-text-soft);
  }

  /* 첫 칸은 카드 제목 */
  .admin-main :deep(:is(.table-wrapper, .table-container, .aip-table-wrap) tbody td:first-child) {
    font-size: 15px;
    font-weight: 600;
    color: var(--pb-color-heading);
    padding-bottom: 4px;
    margin-bottom: 2px;
    border-bottom: 1px solid var(--pb-color-border);
  }

  .admin-main :deep(:is(.table-wrapper, .table-container, .aip-table-wrap) tbody td:first-child::before),
  .admin-main :deep(:is(.table-wrapper, .table-container, .aip-table-wrap) tbody td[data-label='']::before) {
    display: none;
  }

  /* 작업 칸은 카드 맨 아래 동작 줄 — 버튼을 가로로 오른쪽에 모은다 */
  .admin-main :deep(:is(.table-wrapper, .table-container, .aip-table-wrap) tbody td:is([data-label='작업'], [data-label='관리'], [data-label='액션'])) {
    flex-direction: row;
    flex-wrap: wrap;
    align-items: center;
    justify-content: flex-end;
    padding-top: 8px;
    margin-top: 2px;
    border-top: 1px solid var(--pb-color-border);
  }

  .admin-main :deep(:is(.table-wrapper, .table-container, .aip-table-wrap) tbody td:is([data-label='작업'], [data-label='관리'], [data-label='액션'])::before) {
    display: none;
  }

  .admin-main :deep(:is(.table-wrapper, .table-container, .aip-table-wrap) tbody td:is([data-label='작업'], [data-label='관리'], [data-label='액션']) > :is(div, span)) {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: flex-end;
    gap: 8px;
    height: auto;
    min-height: 0;
  }

  /* 카드 안 버튼은 손가락으로 누를 수 있는 크기로 */
  .admin-main :deep(:is(.table-wrapper, .table-container, .aip-table-wrap) tbody td:is([data-label='작업'], [data-label='관리'], [data-label='액션']) button) {
    min-height: 36px;
    min-width: 36px;
    padding-inline: 12px;
  }

  /* 빈 값 칸은 줄을 차지하지 않는다 */
  .admin-main :deep(:is(.table-wrapper, .table-container, .aip-table-wrap) tbody td:empty) {
    display: none;
  }

  .dashboard-content {
    gap: 0;
  }

  .nav-item {
    font-size: 0.875rem;
    padding: 9px 10px;
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