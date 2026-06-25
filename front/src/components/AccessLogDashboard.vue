<template>
  <div class="access-log-dashboard">
    <div class="section-header">
      <div>
        <h2 class="section-title">접속 이력</h2>
        <p class="section-description">관리자·학생 로그인 접속 이력을 확인합니다.</p>
      </div>
    </div>

    <!-- 필터 -->
    <div class="filter-bar">
      <select v-model="filters.actorType" @change="fetchLogs">
        <option value="">전체 유형</option>
        <option value="ADMIN">관리자</option>
        <option value="USER">학생</option>
      </select>
      <select v-model="filters.result" @change="fetchLogs">
        <option value="">전체 결과</option>
        <option value="SUCCESS">성공</option>
        <option value="FAIL">실패</option>
      </select>
    </div>

    <!-- 테이블 -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>#</th>
            <th>유형</th>
            <th>아이디</th>
            <th>IP 주소</th>
            <th>결과</th>
            <th>실패 사유</th>
            <th>접속 일시</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="empty-cell">불러오는 중...</td>
          </tr>
          <tr v-else-if="logs.length === 0">
            <td colspan="7" class="empty-cell">접속 이력이 없습니다.</td>
          </tr>
          <tr v-for="log in logs" :key="log.seqAccessLog" class="data-row">
            <td>{{ log.seqAccessLog }}</td>
            <td>
              <span class="type-badge" :class="log.actorType === 'ADMIN' ? 'admin' : 'user'">
                {{ log.actorType === 'ADMIN' ? '관리자' : '학생' }}
              </span>
            </td>
            <td>{{ log.actorName }}</td>
            <td class="mono">{{ log.ipAddress }}</td>
            <td>
              <span class="result-badge" :class="log.result === 'SUCCESS' ? 'success' : 'fail'">
                {{ log.result === 'SUCCESS' ? '성공' : '실패' }}
              </span>
            </td>
            <td class="fail-reason">{{ log.failReason || '-' }}</td>
            <td class="mono">{{ formatDate(log.accessedAt) }}</td>
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
import { getAccessLogs } from '@/api/admin'
import { swAlert } from '@/utils/sweetAlert'

const logs = ref([])
const loading = ref(false)
const currentPage = ref(1)
const totalPages = ref(1)
const pageSize = 20

const filters = ref({ actorType: '', result: '' })

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

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
      ...(filters.value.actorType && { actorType: filters.value.actorType }),
      ...(filters.value.result && { result: filters.value.result }),
    }
    const res = await getAccessLogs(params)
    const page = res.data.data
    logs.value = page.content
    totalPages.value = page.totalPages
  } catch {
    swAlert('error', '접속 이력을 불러오지 못했습니다.')
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
.access-log-dashboard { max-width: 100%; font-size: 13px; color: var(--pb-color-text); }
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
.fail-reason { max-width: 200px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; color: var(--pb-color-text-muted); }

.type-badge, .result-badge { display: inline-block; padding: 2px 8px; border-radius: var(--pb-radius-xs); font-size: 11px; font-weight: 600; }
.type-badge.admin { background: var(--pb-color-accent-soft); color: var(--pb-color-accent); }
.type-badge.user { background: var(--pb-color-surface-muted); color: var(--pb-color-text-soft); }
.result-badge.success { background: var(--pb-color-success-soft); color: var(--pb-color-success); }
.result-badge.fail { background: var(--pb-color-error-soft, #fff0f0); color: var(--pb-color-error, #e53e3e); }

.pagination { display: flex; justify-content: center; gap: 4px; margin-top: 16px; }
.page-btn { height: 32px; min-width: 32px; padding: 0 8px; border: 1px solid var(--pb-color-border); border-radius: var(--pb-radius-sm); background: var(--pb-color-surface); color: var(--pb-color-text); font-size: 13px; cursor: pointer; transition: background 0.12s, color 0.12s; }
.page-btn:hover:not(:disabled) { background: var(--pb-color-surface-muted); }
.page-btn.active { background: var(--pb-color-brand); color: var(--pb-color-surface); border-color: var(--pb-color-brand); font-weight: 600; }
.page-btn:disabled { opacity: 0.4; cursor: default; }
</style>
