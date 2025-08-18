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
          <option v-for="course in courses" :key="course.id" :value="course.id">
            {{ course.name }}
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
                  <td>{{ item.nameSortFirst }}</td>
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
                  <td>{{ item.nameSortFirst }}</td>
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

const title_url = "http://localhost:8080"

// Chart.js 등록
Chart.register(...registerables)

// 반응형 데이터
const loading = ref(false)
const error = ref(null)
const selectedCourse = ref('')

// 통계 데이터
const popularFirstSort = ref([])
const popularSecondSort = ref([])
const userReadingRank = ref([])

// 과정 목록 (실제로는 API에서 가져와야 함)
const courses = ref([
  { id: 1, name: 'Java 개발자 과정' },
  { id: 2, name: 'Python 데이터 사이언스 과정' },
  { id: 3, name: 'React 프론트엔드 과정' },
  { id: 4, name: 'DevOps 과정' }
])

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
    const token = localStorage.getItem('jwtToken')
    const url = selectedCourse.value 
      ? `${title_url}/history/popular/first/${selectedCourse.value}`
      : `${title_url}/history/popular/first`
    
    const response = await fetch(url, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error('인기 대분류 데이터를 불러올 수 없습니다')
    }

    const data = await response.json()
    popularFirstSort.value = data
  } catch (err) {
    console.error('Popular first sort fetch error:', err)
    throw err
  }
}

const fetchPopularSecondSort = async () => {
  try {
    const token = localStorage.getItem('jwtToken')
    const url = selectedCourse.value 
      ? `${title_url}/history/popular/second/${selectedCourse.value}`
      : `${title_url}/history/popular/second`
    
    const response = await fetch(url, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error('인기 중분류 데이터를 불러올 수 없습니다')
    }

    const data = await response.json()
    popularSecondSort.value = data
  } catch (err) {
    console.error('Popular second sort fetch error:', err)
    throw err
  }
}

const fetchUserReadingRank = async () => {
  try {
    const token = localStorage.getItem('jwtToken')
    const url = selectedCourse.value 
      ? `${title_url}/history/rank/${selectedCourse.value}`
      : `${title_url}/history/rank`
    
    const response = await fetch(url, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      throw new Error('사용자 랭킹 데이터를 불러올 수 없습니다')
    }

    const data = await response.json()
    userReadingRank.value = data
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
      fetchUserReadingRank()
    ])
    
    // 차트 업데이트
    await nextTick()
    updateCharts()
  } catch (err) {
    error.value = err.message || '데이터를 불러오는 중 오류가 발생했습니다'
  } finally {
    loading.value = false
  }
}

// 차트 생성/업데이트 함수들
const createFirstSortChart = () => {
  if (!firstSortChart.value || popularFirstSort.value.length === 0) return

  const ctx = firstSortChart.value.getContext('2d')
  
  if (firstSortChartInstance) {
    firstSortChartInstance.destroy()
  }

  const data = popularFirstSort.value.slice(0, 10) // 상위 10개만

  firstSortChartInstance = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: data.map(item => item.nameSortFirst),
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

const createSecondSortChart = () => {
  if (!secondSortChart.value || popularSecondSort.value.length === 0) return

  const ctx = secondSortChart.value.getContext('2d')
  
  if (secondSortChartInstance) {
    secondSortChartInstance.destroy()
  }

  const data = popularSecondSort.value.slice(0, 8) // 상위 8개만

  secondSortChartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: data.map(item => item.nameSortFirst),
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

const createUserRankChart = () => {
  if (!userRankChart.value || userReadingRank.value.length === 0) return

  const ctx = userRankChart.value.getContext('2d')
  
  if (userRankChartInstance) {
    userRankChartInstance.destroy()
  }

  const data = userReadingRank.value.slice(0, 15) // 상위 15명만

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
      indexAxis: 'y', // 수평 막대 차트
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

const updateCharts = () => {
  if (!showFirstSortTable.value) createFirstSortChart()
  if (!showSecondSortTable.value) createSecondSortChart()
  if (!showRankTable.value) createUserRankChart()
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
onMounted(() => {
  fetchData()
})

onBeforeUnmount(() => {
  if (firstSortChartInstance) firstSortChartInstance.destroy()
  if (secondSortChartInstance) secondSortChartInstance.destroy()
  if (userRankChartInstance) userRankChartInstance.destroy()
})
</script>

<style scoped>
.statistics-dashboard {
  padding: 24px;
}

.dashboard-header {
  margin-bottom: 32px;
  text-align: center;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-size: 2rem;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 8px;
}

.section-subtitle {
  color: #718096;
  font-size: 1.1rem;
  margin: 0;
}

.filter-controls {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
  padding: 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.03);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-group label {
  font-weight: 500;
  color: #4a5568;
  white-space: nowrap;
}

.filter-select {
  padding: 8px 12px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.95rem;
  background: white;
  transition: border-color 0.3s ease;
  min-width: 200px;
}

.filter-select:focus {
  outline: none;
  border-color: #a8dadc;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #a8dadc 0%, #b8e6c1 100%);
  color: #2d3748;
  border: none;
  border-radius: 10px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(168, 218, 220, 0.3);
}

.refresh-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(168, 218, 220, 0.4);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f1f5f9;
  border-top: 4px solid #a8dadc;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.statistics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.03);
  overflow: hidden;
}

.stat-card.full-width {
  grid-column: 1 / -1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 1px solid #e2e8f0;
}

.card-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.toggle-btn {
  padding: 6px 12px;
  background: linear-gradient(135deg, #a8dadc 0%, #b8e6c1 100%);
  color: #2d3748;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.toggle-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(168, 218, 220, 0.3);
}

.card-content {
  padding: 24px;
}

.chart-container {
  position: relative;
  height: 300px;
}

.table-container {
  max-height: 300px;
  overflow-y: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
}

.data-table th {
  background: #f8fafc;
  font-weight: 600;
  color: #2d3748;
  position: sticky;
  top: 0;
}

.data-table tr:hover {
  background: #f8fafc;
}

.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  text-align: center;
}

.error-icon {
  font-size: 3rem;
  margin-bottom: 16px;
}

.error-state h3 {
  color: #e53e3e;
  margin-bottom: 8px;
}

.error-state p {
  color: #718096;
  margin-bottom: 24px;
}

.retry-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #fed7d7 0%, #feb2b2 100%);
  color: #c53030;
  border: none;
  border-radius: 10px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.retry-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(254, 178, 178, 0.4);
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .statistics-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-controls {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-group {
    justify-content: space-between;
  }
  
  .filter-select {
    min-width: unset;
    flex: 1;
  }
  
  .chart-container {
    height: 250px;
  }
  
  .section-title {
    font-size: 1.5rem;
  }
}
</style>