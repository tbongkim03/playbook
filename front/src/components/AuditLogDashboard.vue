<template>
  <div class="audit-log-dashboard">
    <div class="section-header">
      <div>
        <h2 class="section-title">감사 로그</h2>
        <p class="section-description">관리자 주요 작업(도서·계정·캠퍼스·과정 CRUD) 이력을 확인합니다.</p>
      </div>
    </div>

    <!-- 필터 -->
    <div class="filter-bar">
      <select v-model="filters.targetType" @change="fetchLogs">
        <option value="">전체 대상</option>
        <option value="BOOK">도서</option>
        <option value="ADMIN">관리자</option>
        <option value="USER">학생</option>
        <option value="CAMPUS">캠퍼스</option>
        <option value="COURSE">과정</option>
      </select>
      <select v-model="filters.action" @change="fetchLogs">
        <option value="">전체 작업</option>
        <option value="BOOK_CREATE">도서 등록</option>
        <option value="BOOK_UPDATE">도서 수정</option>
        <option value="BOOK_DELETE">도서 삭제</option>
        <option value="ADMIN_CREATE">관리자 등록</option>
        <option value="ADMIN_UPDATE">관리자 수정</option>
        <option value="ADMIN_DELETE">관리자 삭제</option>
        <option value="USER_RESET_PW">비밀번호 초기화</option>
        <option value="USER_DELETE">학생 삭제</option>
        <option value="CAMPUS_CREATE">캠퍼스 등록</option>
        <option value="CAMPUS_UPDATE">캠퍼스 수정</option>
        <option value="CAMPUS_DELETE">캠퍼스 삭제</option>
        <option value="COURSE_CREATE">과정 등록</option>
        <option value="COURSE_UPDATE">과정 수정</option>
        <option value="COURSE_DELETE">과정 삭제</option>
      </select>
    </div>

    <!-- 테이블 -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>#</th>
            <th>작업자</th>
            <th>작업 유형</th>
            <th>대상</th>
            <th>대상 ID</th>
            <th>결과</th>
            <th>실패 사유</th>
            <th>일시</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="8" class="empty-cell">불러오는 중...</td>
          </tr>
          <tr v-else-if="logs.length === 0">
            <td colspan="8" class="empty-cell">감사 로그가 없습니다.</td>
          </tr>
          <tr v-for="log in logs" :key="log.seqAuditLog" class="data-row">
            <td>{{ log.seqAuditLog }}</td>
            <td>{{ log.actorName }}</td>
            <td>
              <span class="action-badge" :class="actionClass(log.action)">
                {{ actionLabel(log.action) }}
              </span>
            </td>
            <td>{{ targetLabel(log.targetType) }}</td>
            <td class="mono">{{ log.targetId || '-' }}</td>
            <td>
              <span class="result-badge" :class="log.result === 'SUCCESS' ? 'success' : 'fail'">
                {{ log.result === 'SUCCESS' ? '성공' : '실패' }}
              </span>
            </td>
            <td class="fail-reason">{{ log.failReason || '-' }}</td>
            <td class="mono">{{ formatDate(log.auditedAt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 페이지네이션 -->
    <div class="pagination" v-if="totalPages > 1">
      <button class="page-btn" :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">‹</button>
      <button
        v-for="p in visiblePages"
        :key="p"
        class="page-btn"
        :class="{ active: p === currentPage }"
        @click="changePage(p)"
      >{{ p }}</button>
      <button class="page-btn" :disabled="currentPage >= totalPages" @click="changePage(currentPage + 1)">›</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getAuditLogs } from '@/api/admin'
import { swAlert } from '@/utils/sweetAlert'

const logs = ref([])
const loading = ref(false)
const currentPage = ref(1)
const totalPages = ref(1)
const pageSize = 20

const filters = ref({ targetType: '', action: '' })

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

const ACTION_LABELS = {
  BOOK_CREATE: '도서 등록', BOOK_UPDATE: '도서 수정', BOOK_DELETE: '도서 삭제',
  ADMIN_CREATE: '관리자 등록', ADMIN_UPDATE: '관리자 수정', ADMIN_DELETE: '관리자 삭제',
  USER_RESET_PW: '비밀번호 초기화', USER_DELETE: '학생 삭제',
  CAMPUS_CREATE: '캠퍼스 등록', CAMPUS_UPDATE: '캠퍼스 수정', CAMPUS_DELETE: '캠퍼스 삭제',
  COURSE_CREATE: '과정 등록', COURSE_UPDATE: '과정 수정', COURSE_DELETE: '과정 삭제',
}
const TARGET_LABELS = {
  BOOK: '도서', ADMIN: '관리자', USER: '학생', CAMPUS: '캠퍼스', COURSE: '과정',
}

function actionLabel(action) { return ACTION_LABELS[action] || action }
function targetLabel(type) { return TARGET_LABELS[type] || type }
function actionClass(action) {
  if (action?.endsWith('_DELETE')) return 'delete'
  if (action?.endsWith('_CREATE')) return 'create'
  return 'update'
}
function formatDate(dt) {
  if (!dt) return '-'
  return dt.replace('T', ' ').substring(0, 19)
}

async function fetchLogs() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize,
      ...(filters.value.targetType && { targetType: filters.value.targetType }),
      ...(filters.value.action && { action: filters.value.action }),
    }
    const res = await getAuditLogs(params)
    const page = res.data.data
    logs.value = page.content
    totalPages.value = page.totalPages
  } catch {
    swAlert('error', '감사 로그를 불러오지 못했습니다.')
  } finally {
    loading.value = false
  }
}

function changePage(p) {
  currentPage.value = p
  fetchLogs()
}

onMounted(fetchLogs)
</script>

<style scoped>
.audit-log-dashboard { max-width: 100%; font-size: 13px; color: var(--pb-color-text); }
.section-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 20px; }
.section-title { font-size: 15px; font-weight: 700; color: var(--pb-color-heading); margin: 0 0 3px; }
.section-description { font-size: 13px; color: var(--pb-color-text-muted); margin: 0; }

.filter-bar { display: flex; gap: 10px; margin-bottom: 14px; flex-wrap: wrap; }
.filter-bar select { height: 34px; padding: 0 10px; border: 1px solid var(--pb-color-border); border-radius: var(--pb-radius-sm); font-size: 13px; background: var(--pb-color-surface); color: var(--pb-color-text); cursor: pointer; }

.table-container { background: var(--pb-color-surface); border: 1px solid var(--pb-color-border); border-radius: var(--pb-radius-lg); box-shadow: var(--pb-shadow-sm); overflow: auto; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { text-align: left; padding: 9px 16px; background: var(--pb-color-surface-subtle); color: var(--pb-color-text-soft); font-weight: 600; font-size: 11px; text-transform: uppercase; letter-spacing: 0.05em; border-bottom: 1px solid var(--pb-color-border); white-space: nowrap; }
.data-table td { padding: 10px 16px; border-bottom: 1px solid var(--pb-color-border); font-size: 13px; vertical-align: middle; }
.data-row:hover td { background: var(--pb-color-surface-muted); }
.empty-cell { text-align: center; padding: 40px; color: var(--pb-color-text-muted); }
.mono { font-family: monospace; font-size: 12px; }
.fail-reason { max-width: 160px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; color: var(--pb-color-text-muted); }

.action-badge, .result-badge { display: inline-block; padding: 2px 8px; border-radius: var(--pb-radius-xs); font-size: 11px; font-weight: 600; }
.action-badge.create { background: var(--pb-color-success-soft); color: var(--pb-color-success); }
.action-badge.update { background: var(--pb-color-accent-soft); color: var(--pb-color-accent); }
.action-badge.delete { background: var(--pb-color-error-soft, #fff0f0); color: var(--pb-color-error, #e53e3e); }
.result-badge.success { background: var(--pb-color-success-soft); color: var(--pb-color-success); }
.result-badge.fail { background: var(--pb-color-error-soft, #fff0f0); color: var(--pb-color-error, #e53e3e); }

.pagination { display: flex; justify-content: center; gap: 4px; margin-top: 16px; }
.page-btn { height: 32px; min-width: 32px; padding: 0 8px; border: 1px solid var(--pb-color-border); border-radius: var(--pb-radius-sm); background: var(--pb-color-surface); color: var(--pb-color-text); font-size: 13px; cursor: pointer; transition: background 0.12s, color 0.12s; }
.page-btn:hover:not(:disabled) { background: var(--pb-color-surface-muted); }
.page-btn.active { background: var(--pb-color-brand); color: var(--pb-color-surface); border-color: var(--pb-color-brand); font-weight: 600; }
.page-btn:disabled { opacity: 0.4; cursor: default; }
</style>
