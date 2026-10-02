<template>
  <div class="statistics-dashboard">
    <!-- 헤더 -->
    <div class="pb-page-head">
      <div>
        <h2>통계 대시보드</h2>
        <p>도서 대여 및 사용자 통계를 확인하세요</p>
      </div>
    </div>

    <!-- 필터 컨트롤 -->
    <div class="filter-controls">
      <div class="filter-group">
        <label for="courseSelect">과정</label>
        <select id="courseSelect" v-model="selectedCourse" @change="fetchData" class="filter-select">
          <option value="">전체 과정</option>
          <option
            v-for="(course, index) in courses"
            :key="index"
            :value="course.seqCourse"
          >
            {{ course.title }} ({{ course.trprDegr }}기)
          </option>
        </select>
      </div>

      <!-- 캠퍼스 필터 (전체 관리자만 표시) -->
      <div v-if="showCampusFilter" class="filter-group">
        <label for="campusSelect">캠퍼스</label>
        <select id="campusSelect" v-model="selectedCampus" @change="fetchData" class="filter-select">
          <option value="">전체 캠퍼스</option>
          <option
            v-for="campus in campuses"
            :key="campus.seqCampus"
            :value="campus.seqCampus"
          >
            {{ campus.nameCampus }}
          </option>
        </select>
      </div>

      <!-- 기간 필터 -->
      <div class="filter-group">
        <label>기간</label>
        <DateRangePicker ref="datePickerRef" @change="onDateRangeChange" />
      </div>

      <button type="button" class="pb-icon-btn stats-refresh" :disabled="loading" aria-label="통계 새로고침" title="통계 새로고침" @click="refreshData">
        <PhArrowsClockwise weight="duotone" :size="18" :class="{ 'is-spinning': loading }" />
      </button>
    </div>

    <!-- 로딩 상태 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>통계 데이터를 로딩중입니다...</p>
    </div>

    <!-- 통계 카드들 -->
    <div v-if="!loading" class="statistics-grid">
      <!-- 인기 대분류 차트 -->
      <div class="stat-card">
        <div class="card-header">
          <h3 class="card-title">인기 도서 대분류</h3>
          <div class="card-actions">
            <button @click="toggleFirstSortView" class="toggle-btn">
              {{ showFirstSortTable ? '차트' : '테이블' }}
            </button>
          </div>
        </div>
        <div class="card-content">
          <div v-if="!popularFirstSort.length" class="chart-empty">
            <PhChartBar weight="duotone" :size="32" />
            <b>이 조건의 대출 기록이 아직 없어요</b>
            <span>기간이나 과정을 바꿔 보세요</span>
          </div>
          <div v-else-if="!showFirstSortTable" class="chart-container">
            <canvas ref="firstSortChart"></canvas>
          </div>
          <div v-else class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>순위</th>
                  <th>대분류</th>
                  <th>대여 횟수</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, index) in popularFirstSort" :key="index">
                  <td>{{ index + 1 }}</td>
                  <td>{{ item.korSortLabel }}</td>
                  <td>{{ item.rentalCount }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- 인기 중분류 차트 -->
      <div class="stat-card">
        <div class="card-header">
          <h3 class="card-title">인기 도서 중분류</h3>
          <div class="card-actions">
            <button @click="toggleSecondSortView" class="toggle-btn">
              {{ showSecondSortTable ? '차트' : '테이블' }}
            </button>
          </div>
        </div>
        <div class="card-content">
          <div v-if="!popularSecondSort.length" class="chart-empty">
            <PhChartBar weight="duotone" :size="32" />
            <b>이 조건의 대출 기록이 아직 없어요</b>
            <span>기간이나 과정을 바꿔 보세요</span>
          </div>
          <div v-else-if="!showSecondSortTable" class="chart-container">
            <canvas ref="secondSortChart"></canvas>
          </div>
          <div v-else class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>순위</th>
                  <th>중분류</th>
                  <th>대여 횟수</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, index) in popularSecondSort" :key="index">
                  <td>{{ index + 1 }}</td>
                  <td>{{ item.korSortLabel }}</td>
                  <td>{{ item.rentalCount }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- 사용자 독서 랭킹 -->
      <div class="stat-card full-width">
        <div class="card-header">
          <h3 class="card-title">사용자 독서 랭킹</h3>
          <div class="card-actions">
            <button @click="toggleRankView" class="toggle-btn">
              {{ showRankTable ? '차트' : '테이블' }}
            </button>
          </div>
        </div>
        <div class="card-content">
          <div v-if="!userReadingRank.length" class="chart-empty">
            <PhChartBar weight="duotone" :size="32" />
            <b>이 조건의 대출 기록이 아직 없어요</b>
            <span>기간이나 과정을 바꿔 보세요</span>
          </div>
          <div v-else-if="!showRankTable" class="chart-container">
            <canvas ref="userRankChart"></canvas>
          </div>
          <div v-else class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>순위</th>
                  <th>사용자명</th>
                  <th>도서 대여 수</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, index) in userReadingRank" :key="index">
                  <td>{{ index + 1 }}</td>
                  <td>{{ item.userName }}</td>
                  <td>{{ item.bookCount }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- 에러 상태 -->
    <div v-if="error" class="error-state">
      <div class="error-icon">⚠️</div>
      <h3>데이터를 불러올 수 없습니다</h3>
      <p>{{ error }}</p>
      <button @click="fetchData" class="retry-btn">다시 시도</button>
    </div>
  </div>
</template>

<script setup>
import { PhArrowsClockwise, PhChartBar } from '@phosphor-icons/vue'
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { Chart, registerables } from 'chart.js'
import * as courseApi from '@/api/course'
import * as historyApi from '@/api/history'
import { useAdminCampusFilter } from '@/composables/useAdminCampusFilter'
import DateRangePicker from './DateRangePicker.vue'

// Chart.js 등록
Chart.register(...registerables)

// 반응형 데이터
const loading = ref(false)
const error = ref(null)
const selectedCourse = ref('')

// 기간 필터
const datePickerRef = ref(null)
const activeDateRange = ref({ startDate: null, endDate: null })

const onDateRangeChange = (range) => {
  activeDateRange.value = range
  fetchData()
}

// 캠퍼스 필터 관련 (캠퍼스 관리자는 자기 캠퍼스 고정, 필터 숨김)
const {
  showCampusFilter,
  currentUserCampusId,
  selectedCampus,
  campuses,
  fetchAdminInfo,
  fetchCampuses,
  getCampusParam,
} = useAdminCampusFilter({ showFilterForCampusAdmin: false })

// 통계 데이터
const popularFirstSort = ref([])
const popularSecondSort = ref([])
const userReadingRank = ref([])

// 과정 목록
const courses = ref([])

// 과정 목록은 읽기만 한다.
// 예전에는 화면을 열 때마다 여기서 Work24 목록으로 과정을 만들고 지웠다 — 서버 CourseSyncScheduler 와 역할이 겹치고,
// 캠퍼스 구분 없이 "API 에 없는 과정"을 지워 다른 캠퍼스 과정까지 지울 수 있는 구조였다.
async function getCourseList() {
  try {
    const campusId = showCampusFilter.value && selectedCampus.value ? selectedCampus.value : undefined
    const finalDbRes = await courseApi.getAll(campusId)
    const finalDbCourses = finalDbRes.data.data

    courses.value = finalDbCourses
      .map(item => {
        const parts = item.nameCourse.split(' ')
        const isLastPartGeneration = parts[parts.length - 1].endsWith('기')
        const title = isLastPartGeneration
          ? parts.slice(0, -1).join(' ')
          : item.nameCourse
        const generation = isLastPartGeneration 
          ? parts[parts.length - 1].replace('기', '') 
          : '1'
        
        return {
          title,
          trprDegr: generation,
          seqCourse: item.seqCourse,
          nameCourse: item.nameCourse,
          startDtCourse: item.startDtCourse,
          finishDtCourse: item.finishDtCourse
        }
      })
      .sort((a, b) => a.title.localeCompare(b.title, 'ko'))

  } catch (err) {
    console.error('API 조회 실패:', err)
  }
}

// 뷰 토글 상태
const showFirstSortTable = ref(false)
const showSecondSortTable = ref(false)
const showRankTable = ref(false)

// 차트 레퍼런스
const firstSortChart = ref(null)
const secondSortChart = ref(null)
const userRankChart = ref(null)

// 차트 인스턴스
let firstSortChartInstance = null
let secondSortChartInstance = null
let userRankChartInstance = null

// API 호출 함수들
const fetchPopularFirstSort = async () => {
  try {
    const campusId = showCampusFilter.value && selectedCampus.value ? selectedCampus.value : null
    const { startDate, endDate } = activeDateRange.value
    const response = await historyApi.getPopularFirst(selectedCourse.value || null, campusId, startDate, endDate)
    popularFirstSort.value = response.data.data
  } catch (err) {
    console.error('Popular first sort fetch error:', err)
    throw err
  }
}

const fetchPopularSecondSort = async () => {
  try {
    const campusId = showCampusFilter.value && selectedCampus.value ? selectedCampus.value : null
    const { startDate, endDate } = activeDateRange.value
    const response = await historyApi.getPopularSecond(selectedCourse.value || null, campusId, startDate, endDate)
    popularSecondSort.value = response.data.data
  } catch (err) {
    console.error('Popular second sort fetch error:', err)
    throw err
  }
}

const fetchUserReadingRank = async () => {
  try {
    const campusId = showCampusFilter.value && selectedCampus.value ? selectedCampus.value : null
    const { startDate, endDate } = activeDateRange.value
    const response = await historyApi.getUserRank(selectedCourse.value || null, campusId, startDate, endDate)
    userReadingRank.value = response.data.data
  } catch (err) {
    console.error('User reading rank fetch error:', err)
    throw err
  }
}

// 모든 데이터 가져오기
const fetchData = async () => {
  loading.value = true
  error.value = null

  try {
    await Promise.all([
      fetchPopularFirstSort(),
      fetchPopularSecondSort(),
      fetchUserReadingRank(),
      getCourseList()
    ])
    
    // DOM 업데이트 완료 후 차트 생성
    await nextTick()
    // 추가 대기 시간으로 확실한 렌더링 보장
    setTimeout(updateCharts, 100)
  } catch (err) {
    error.value = err.message || '데이터를 불러오는 중 오류가 발생했습니다'
  } finally {
    loading.value = false
  }
}


// 차트 생성/업데이트 함수들
const createFirstSortChart = async () => {
  await nextTick()
  if (!firstSortChart.value || popularFirstSort.value.length === 0) return

  const ctx = firstSortChart.value.getContext('2d')
  
  if (firstSortChartInstance) {
    firstSortChartInstance.destroy()
  }

  // 차트 생성 전 짧은 대기
  await new Promise(resolve => setTimeout(resolve, 50))

  const data = popularFirstSort.value.slice(0, 10)

  firstSortChartInstance = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: data.map(item => item.korSortLabel),
      datasets: [{
        data: data.map(item => item.rentalCount),
        backgroundColor: [
          '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF',
          '#FF9F40', '#FF6384', '#C9CBCF', '#4BC0C0', '#36A2EB'
        ],
        borderWidth: 2,
        borderColor: '#ffffff'
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            padding: 20,
            usePointStyle: true
          }
        }
      }
    }
  })
}


const createSecondSortChart = async () => {
  await nextTick()
  if (!secondSortChart.value || popularSecondSort.value.length === 0) return

  const ctx = secondSortChart.value.getContext('2d')
  
  if (secondSortChartInstance) {
    secondSortChartInstance.destroy()
  }

  await new Promise(resolve => setTimeout(resolve, 50))

  const data = popularSecondSort.value.slice(0, 8)

  secondSortChartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: data.map(item => item.korSortLabel),
      datasets: [{
        label: '대여 횟수',
        data: data.map(item => item.rentalCount),
        backgroundColor: 'rgba(54, 162, 235, 0.8)',
        borderColor: 'rgba(54, 162, 235, 1)',
        borderWidth: 1
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            stepSize: 1
          }
        }
      }
    }
  })
}

const createUserRankChart = async () => {
  await nextTick()
  if (!userRankChart.value || userReadingRank.value.length === 0) return

  const ctx = userRankChart.value.getContext('2d')
  
  if (userRankChartInstance) {
    userRankChartInstance.destroy()
  }

  await new Promise(resolve => setTimeout(resolve, 50))

  const data = userReadingRank.value.slice(0, 15)

  userRankChartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: data.map(item => item.userName),
      datasets: [{
        label: '도서 대여 수',
        data: data.map(item => item.bookCount),
        backgroundColor: 'rgba(255, 99, 132, 0.8)',
        borderColor: 'rgba(255, 99, 132, 1)',
        borderWidth: 1
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      indexAxis: 'y',
      plugins: {
        legend: {
          display: false
        }
      },
      scales: {
        x: {
          beginAtZero: true,
          ticks: {
            stepSize: 1
          }
        }
      }
    }
  })
}

const updateCharts = async () => {
  await nextTick()
  if (!showFirstSortTable.value) await createFirstSortChart()
  if (!showSecondSortTable.value) await createSecondSortChart()
  if (!showRankTable.value) await createUserRankChart()
}

// 뷰 토글 함수들
const toggleFirstSortView = async () => {
  showFirstSortTable.value = !showFirstSortTable.value
  if (!showFirstSortTable.value) {
    await nextTick()
    createFirstSortChart()
  }
}

const toggleSecondSortView = async () => {
  showSecondSortTable.value = !showSecondSortTable.value
  if (!showSecondSortTable.value) {
    await nextTick()
    createSecondSortChart()
  }
}

const toggleRankView = async () => {
  showRankTable.value = !showRankTable.value
  if (!showRankTable.value) {
    await nextTick()
    createUserRankChart()
  }
}

// 데이터 새로고침
const refreshData = () => {
  fetchData()
}


// 라이프사이클
onMounted(async () => {
  await fetchCampuses()
  await fetchAdminInfo()
  await fetchData()
})

onBeforeUnmount(() => {
  if (firstSortChartInstance) firstSortChartInstance.destroy()
  if (secondSortChartInstance) secondSortChartInstance.destroy()
  if (userRankChartInstance) userRankChartInstance.destroy()
})
</script>

<style scoped>
/* ── 루트 ── */
.statistics-dashboard {
  max-width: 100%;
  font-size: 13px;
  color: var(--pb-color-text);
}

/* ── 헤더 ── */
.dashboard-header {
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 700;
  color: var(--pb-color-heading);
  margin: 0 0 3px;
}

.section-subtitle {
  font-size: 13px;
  color: var(--pb-color-text-muted);
  margin: 0;
}

/* ── 필터 컨트롤 ── */
.filter-controls {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 20px;
  padding: 14px 16px;
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-lg);
  box-shadow: var(--pb-shadow-xs);
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.filter-group label {
  font-size: 11px;
  font-weight: 500;
  color: var(--pb-color-text-soft);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  white-space: nowrap;
}

.filter-select {
  height: 34px;
  padding: 0 10px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  min-width: 190px;
  cursor: pointer;
  transition: border-color 0.15s;
}

.filter-select:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px var(--pb-color-brand-muted);
}



/* ── 로딩 상태 ── */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 56px 20px;
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-lg);
  box-shadow: var(--pb-shadow-xs);
  color: var(--pb-color-text-soft);
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--pb-color-border);
  border-left-color: var(--pb-color-brand);
  border-radius: 50%;
  animation: spin 0.9s linear infinite;
  margin-bottom: 14px;
}

@keyframes spin { to { rotate: 360deg; } }

/* ── 통계 그리드 ── */
.statistics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

/* ── 카드 ── */
.stat-card {
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-lg);
  box-shadow: var(--pb-shadow-sm);
  overflow: hidden;
}

.stat-card.full-width { grid-column: 1 / -1; }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 13px 18px;
  background: var(--pb-color-surface-muted);
  border-bottom: 1px solid var(--pb-color-border);
}

.card-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0;
}

.card-actions { display: flex; gap: 6px; }

.toggle-btn {
  height: 28px;
  padding: 0 10px;
  background: var(--pb-color-surface);
  color: var(--pb-color-text-muted);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.12s, color 0.12s;
}

.toggle-btn:hover {
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text);
  border-color: var(--pb-color-border-strong);
}

.card-content { padding: 20px; }

/* ── 차트/테이블 ── */
.chart-container {
  position: relative;
  height: 280px;
}

.table-container {
  max-height: 280px;
  overflow-y: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  text-align: left;
  padding: 8px 12px;
  background: var(--pb-color-surface-subtle);
  color: var(--pb-color-text-soft);
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 1px solid var(--pb-color-border);
  position: sticky;
  top: 0;
}

.data-table td {
  padding: 9px 12px;
  border-bottom: 1px solid var(--pb-color-border);
  font-size: 13px;
  color: var(--pb-color-text);
}

.data-table tr:hover td { background: var(--pb-color-surface-muted); }

/* ── 에러 상태 ── */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 56px 20px;
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-lg);
  box-shadow: var(--pb-shadow-xs);
  text-align: center;
}

.error-icon {
  font-size: 36px;
  margin-bottom: 14px;
}

.error-state h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--pb-color-danger);
  margin: 0 0 6px;
}

.error-state p {
  font-size: 13px;
  color: var(--pb-color-text-muted);
  margin: 0 0 20px;
}

.retry-btn {
  height: 32px;
  padding: 0 18px;
  background: var(--pb-color-danger-soft);
  border: 1px solid var(--pb-color-danger);
  border-radius: var(--pb-radius-sm);
  color: var(--pb-color-danger);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.12s;
}

.retry-btn:hover { background: var(--pb-color-danger); color: var(--pb-color-surface); }

/* ── 반응형 ── */
@media (max-width: 768px) {
  .statistics-grid { grid-template-columns: 1fr; }
  .filter-controls { flex-direction: column; align-items: stretch; }
  .stats-refresh { align-self: flex-end; }
  .filter-select { min-width: unset; }
  .chart-container { height: 240px; }
}

/* 데이터가 없을 때 빈 캔버스 대신 안내 */
.chart-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-height: 220px;
  color: var(--pb-color-text-soft);
  text-align: center;
}

.chart-empty b {
  color: var(--pb-color-heading);
  font-size: 14px;
  font-weight: 600;
}

.chart-empty span {
  font-size: 12px;
}

/* 필터 줄 맨 끝의 새로고침 아이콘 */
.stats-refresh {
  margin-left: auto;
}
</style>