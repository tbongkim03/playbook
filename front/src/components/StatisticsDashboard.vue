<template>
  <div class="statistics-dashboard">
    <!-- 헤더 -->
    <div class="dashboard-header">
      <h2 class="section-title">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M3 3V21H21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M9 9L12 6L16 10L20 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        통계 대시보드
      </h2>
      <p class="section-subtitle">도서 대여 및 사용자 통계를 확인하세요</p>
    </div>

    <!-- 필터 컨트롤 -->
    <div class="filter-controls">
      <div class="filter-group">
        <label for="courseSelect">과정 선택:</label>
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
        <label for="campusSelect">캠퍼스 선택:</label>
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
      
      <button @click="refreshData" class="refresh-btn" :disabled="loading">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M1 4V10H7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M23 20V14H17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10M23 14L18.36 18.36A9 9 0 0 1 3.51 15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        새로고침
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
          <div v-if="!showFirstSortTable" class="chart-container">
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
          <div v-if="!showSecondSortTable" class="chart-container">
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
          <div v-if="!showRankTable" class="chart-container">
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
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { Chart, registerables } from 'chart.js'
import axios from 'axios'
import { useAdminCampusFilter } from '@/composables/useAdminCampusFilter'
import { API_BASE } from '@/utils/constants'

// Chart.js 등록
Chart.register(...registerables)

// 반응형 데이터
const loading = ref(false)
const error = ref(null)
const selectedCourse = ref('')

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

async function getCourseList() {
  // const apiKey = import.meta.env.VITE_WORK24_API_KEY
  // const today = new Date()
  // const sixMonthsAgo = new Date()
  // sixMonthsAgo.setMonth(sixMonthsAgo.getMonth() - 6)

  // function formatDateToYYYYMMDD(date) {
  //   const yyyy = date.getFullYear()
  //   const mm = String(date.getMonth() + 1).padStart(2, '0')
  //   const dd = String(date.getDate()).padStart(2, '0')
  //   return `${yyyy}${mm}${dd}`
  // }

  // const srchTraStDt = formatDateToYYYYMMDD(sixMonthsAgo)
  // const srchTraEndDt = formatDateToYYYYMMDD(today)

  // const url =
  //   `https://www.work24.go.kr/cm/openApi/call/hr/callOpenApiSvcInfo310L01.do?authKey=${apiKey}` +
  //   `&returnType=JSON&outType=1&pageNum=1&pageSize=100` +
  //   `&srchTraStDt=${srchTraStDt}&srchTraEndDt=${srchTraEndDt}` +
  //   `&srchTraArea1=11&srchNcs1=20&crseTracseSe=C0104&srchTraGbn=M1001&srchTraOrganNm=플레이데이터평생교육원` +
  //   `&sort=ASC&sortCol=2`

  try {
    const res = await axios.get('/api/work24/course')
    const data = res.data.data
    const apiCoursesRaw = data?.srchList || []

    if (apiCoursesRaw.length === 0) return

    const apiCourses = apiCoursesRaw.map(item => {
      const title = item.title.includes(' - ') ? item.title.split(' - ')[0] : item.title
      const fullName = `${title} ${item.trprDegr}기`
      return {
        nameCourse: fullName,
        startDtCourse: item.traStartDate,
        finishDtCourse: item.traEndDate,
        trprDegr: item.trprDegr,
        seqCourse: item.trprId
      }
    })

    const dbRes = await axios.get(`${API_BASE}/courses`)
    const dbCourses = dbRes.data.data

    for (const apiItem of apiCourses) {
      const exists = dbCourses.find(dbItem => dbItem.nameCourse === apiItem.nameCourse)
      if (!exists) {
        await axios.post(`${API_BASE}/courses`, {
          nameCourse: apiItem.nameCourse,
          startDtCourse: apiItem.startDtCourse,
          finishDtCourse: apiItem.finishDtCourse
        })
      }
    }

    for (const dbItem of dbCourses) {
      const exists = apiCourses.find(apiItem => apiItem.nameCourse === dbItem.nameCourse)
      if (!exists) {
        await axios.delete(`${API_BASE}/courses/${dbItem.seqCourse}`)
      }
    }

    for (const apiItem of apiCourses) {
      const dbItem = dbCourses.find(db => db.nameCourse === apiItem.nameCourse)
      if (dbItem) {
        const isDifferent =
          dbItem.startDtCourse !== apiItem.startDtCourse ||
          dbItem.finishDtCourse !== apiItem.finishDtCourse

        if (isDifferent) {
          await axios.put(`${API_BASE}/courses/${dbItem.seqCourse}`, {
            nameCourse: apiItem.nameCourse,
            startDtCourse: apiItem.startDtCourse,
            finishDtCourse: apiItem.finishDtCourse
          })
        }
      }
    }

    const finalDbRes = await axios.get(`${API_BASE}/courses`)
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
    let url = selectedCourse.value
      ? `${API_BASE}/history/popular/first/${selectedCourse.value}`
      : `${API_BASE}/history/popular/first`
    
    url += getCampusParam()

    const response = await axios.get(url)
    popularFirstSort.value = response.data.data
  } catch (err) {
    console.error('Popular first sort fetch error:', err)
    throw err
  }
}

const fetchPopularSecondSort = async () => {
  try {
    let url = selectedCourse.value
      ? `${API_BASE}/history/popular/second/${selectedCourse.value}`
      : `${API_BASE}/history/popular/second`
    
    url += getCampusParam()

    const response = await axios.get(url)
    popularSecondSort.value = response.data.data
  } catch (err) {
    console.error('Popular second sort fetch error:', err)
    throw err
  }
}

const fetchUserReadingRank = async () => {
  try {
    let url = selectedCourse.value
      ? `${API_BASE}/history/rank/${selectedCourse.value}`
      : `${API_BASE}/history/rank`
    
    url += getCampusParam()

    const response = await axios.get(url)
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

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 14px;
  background: var(--pb-color-surface-muted);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  font-weight: 500;
  color: var(--pb-color-text);
  cursor: pointer;
  transition: background 0.12s;
  margin-top: auto;
}

.refresh-btn:hover:not(:disabled) { background: var(--pb-color-border); }
.refresh-btn:disabled { opacity: 0.5; cursor: not-allowed; }

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
  .filter-select { min-width: unset; }
  .chart-container { height: 240px; }
}
</style>