<template>
  <div class="course-management">
    <div class="section-header">
      <h2 class="section-title">과정 관리</h2>
      <p class="section-description">과정을 관리하고 캠퍼스 정보를 조회할 수 있습니다.</p>
    </div>

    <!-- 탭 메뉴 -->
    <div class="tab-menu">
      <button 
        class="tab-btn" 
        :class="{ active: activeTab === 'courses' }"
        @click="activeTab = 'courses'"
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2"/>
          <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2"/>
          <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2"/>
        </svg>
        과정 관리
      </button>
      <button 
        class="tab-btn" 
        :class="{ active: activeTab === 'campuses' }"
        @click="switchToCampusTab"
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M21 10C21 17 12 23 12 23C12 23 3 17 3 10C3 7.61305 3.94821 5.32387 5.63604 3.63604C7.32387 1.94821 9.61305 1 12 1C14.3869 1 16.6761 1.94821 18.364 3.63604C20.0518 5.32387 21 7.61305 21 10Z" stroke="currentColor" stroke-width="2"/>
          <circle cx="12" cy="10" r="3" stroke="currentColor" stroke-width="2"/>
        </svg>
        캠퍼스 관리
      </button>
    </div>

    <!-- 과정 관리 탭 -->
    <div v-if="activeTab === 'courses'" class="tab-content">
      <!-- 통계 카드 -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2"/>
              <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2"/>
              <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ courseList.length }}</div>
            <div class="stat-label">총 과정</div>
          </div>
        </div>
      </div>

      <!-- 필터 및 액션 영역 -->
      <div class="action-filter-bar">
        <!-- 캠퍼스 필터 (전체 관리자만 표시) -->
        <div v-if="showCampusFilter" class="filter-group">
          <label class="filter-label">캠퍼스</label>
          <select v-model="selectedCampus" @change="onCampusChange" class="filter-select">
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
        
        <!-- 과정 추가 버튼 -->
        <div class="action-bar">
          <button class="add-btn" @click="showAddCourseModal = true">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <line x1="12" y1="8" x2="12" y2="16" stroke="currentColor" stroke-width="2"/>
              <line x1="8" y1="12" x2="16" y2="12" stroke="currentColor" stroke-width="2"/>
            </svg>
            과정 추가
          </button>
        </div>
      </div>

      <!-- 과정 목록 테이블 -->
      <div class="table-container">
        <div class="table-header">
          <h3>과정 목록</h3>
        </div>
        
        <div class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>과정명</th>
                <th>캠퍼스</th>
                <th>시작일</th>
                <th>종료일</th>
                <th>작업</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="course in courseList" :key="course.seqCourse" class="data-row">
                <td class="course-name">{{ course.nameCourse }}</td>
                <td class="course-campus">{{ course.campusName || '-' }}</td>
                <td class="course-date">{{ formatDate(course.startDtCourse) }}</td>
                <td class="course-date">{{ formatDate(course.finishDtCourse) }}</td>
                <td class="course-actions">
                  <button class="edit-btn" @click="openEditCourseModal(course)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M11 4H4C3.46957 4 2.96086 4.21071 2.58579 4.58579C2.21071 4.96086 2 5.46957 2 6V20C2 20.5304 2.21071 21.0391 2.58579 21.4142C2.96086 21.7893 3.46957 22 4 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V13" stroke="currentColor" stroke-width="2"/>
                      <path d="M18.5 2.5C18.8978 2.10217 19.4374 1.87868 20 1.87868C20.5626 1.87868 21.1022 2.10217 21.5 2.5C21.8978 2.89782 22.1213 3.43739 22.1213 4C22.1213 4.56261 21.8978 5.10217 21.5 5.5L12 15L8 16L9 12L18.5 2.5Z" stroke="currentColor" stroke-width="2"/>
                    </svg>
                  </button>
                  <button class="delete-btn" @click="confirmDeleteCourse(course)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <polyline points="3,6 5,6 21,6" stroke="currentColor" stroke-width="2"/>
                      <path d="M19,6V20C19,20.5304 18.7893,21.0391 18.4142,21.4142C18.0391,21.7893 17.5304,22 17,22H7C6.46957,22 5.96086,21.7893 5.58579,21.4142C5.21071,21.0391 5,20.5304 5,20V6M8,6V4C8,3.46957 8.21071,2.96086 8.58579,2.58579C8.96086,2.21071 9.46957,2 10,2H14C14.5304,2 15.0391,2.21071 15.4142,2.58579C15.7893,2.96086 16,3.46957 16,4V6" stroke="currentColor" stroke-width="2"/>
                      <line x1="10" y1="11" x2="10" y2="17" stroke="currentColor" stroke-width="2"/>
                      <line x1="14" y1="11" x2="14" y2="17" stroke="currentColor" stroke-width="2"/>
                    </svg>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 캠퍼스 관리 탭 -->
    <div v-if="activeTab === 'campuses'" class="tab-content">
      <!-- 통계 카드 -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M21 10C21 17 12 23 12 23C12 23 3 17 3 10C3 7.61305 3.94821 5.32387 5.63604 3.63604C7.32387 1.94821 9.61305 1 12 1C14.3869 1 16.6761 1.94821 18.364 3.63604C20.0518 5.32387 21 7.61305 21 10Z" stroke="currentColor" stroke-width="2"/>
              <circle cx="12" cy="10" r="3" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ campusList.length }}</div>
            <div class="stat-label">총 캠퍼스</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M9 12L11 14L15 10" stroke="currentColor" stroke-width="2"/>
              <path d="M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ activeCampusCount }}</div>
            <div class="stat-label">활성 캠퍼스</div>
          </div>
        </div>
      </div>


      <!-- 캠퍼스 목록 테이블 -->
      <div class="table-container">
        <div class="table-header">
          <h3>캠퍼스 목록</h3>
        </div>
        
        <div class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>캠퍼스명</th>
                <th>위치</th>
                <th>상태</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="campus in campusList" :key="campus.seqCampus" class="data-row">
                <td class="campus-name">{{ campus.nameCampus }}</td>
                <td class="campus-location">{{ campus.locationCampus || '-' }}</td>
                <td class="campus-status">
                  <span :class="['status-badge', campus.isActive ? 'active' : 'inactive']">
                    {{ campus.isActive ? '활성' : '비활성' }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 과정 추가 모달 -->
    <div v-if="showAddCourseModal" class="modal-overlay" @click="closeAddCourseModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>새 과정 추가</h3>
          <button class="modal-close" @click="closeAddCourseModal">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2"/>
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2"/>
            </svg>
          </button>
        </div>
        <form @submit.prevent="addCourse" class="modal-form">
          <div class="form-group">
            <label for="newCourseName">과정명 <span class="required-mark">*</span></label>
            <input 
              type="text" 
              id="newCourseName" 
              v-model="newCourse.nameCourse" 
              required 
              placeholder="과정명을 입력하세요"
            />
          </div>
          <div class="form-group">
            <label for="newCourseCampus">캠퍼스</label>
            <select
              id="newCourseCampus"
              v-model="newCourse.seqCampus"
              class="form-select"
            >
              <option :value="null">캠퍼스 선택</option>
              <option v-for="campus in activeCampusList" :key="campus.seqCampus" :value="campus.seqCampus">
                {{ campus.nameCampus }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label for="newCourseStartDate">시작일 <span class="required-mark">*</span></label>
            <input 
              type="date" 
              id="newCourseStartDate" 
              v-model="newCourse.startDtCourse" 
              required
            />
          </div>
          <div class="form-group">
            <label for="newCourseFinishDate">종료일 <span class="required-mark">*</span></label>
            <input 
              type="date" 
              id="newCourseFinishDate" 
              v-model="newCourse.finishDtCourse" 
              required
            />
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="closeAddCourseModal">취소</button>
            <button type="submit" class="submit-btn" :disabled="isLoading">
              {{ isLoading ? '추가 중...' : '추가' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 과정 수정 모달 -->
    <div v-if="showEditCourseModal" class="modal-overlay" @click="closeEditCourseModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>과정 수정</h3>
          <button class="modal-close" @click="closeEditCourseModal">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2"/>
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2"/>
            </svg>
          </button>
        </div>
        <form @submit.prevent="updateCourse" class="modal-form">
          <div class="form-group">
            <label for="editCourseName">과정명 <span class="required-mark">*</span></label>
            <input 
              type="text" 
              id="editCourseName" 
              v-model="editingCourse.nameCourse" 
              required 
              placeholder="과정명을 입력하세요"
            />
          </div>
          <div class="form-group">
            <label for="editCourseCampus">캠퍼스</label>
            <select
              id="editCourseCampus"
              v-model="editingCourse.seqCampus"
              class="form-select"
            >
              <option :value="null">캠퍼스 선택</option>
              <option v-for="campus in activeCampusList" :key="campus.seqCampus" :value="campus.seqCampus">
                {{ campus.nameCampus }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label for="editCourseStartDate">시작일 <span class="required-mark">*</span></label>
            <input 
              type="date" 
              id="editCourseStartDate" 
              v-model="editingCourse.startDtCourse" 
              required
            />
          </div>
          <div class="form-group">
            <label for="editCourseFinishDate">종료일 <span class="required-mark">*</span></label>
            <input 
              type="date" 
              id="editCourseFinishDate" 
              v-model="editingCourse.finishDtCourse" 
              required
            />
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="closeEditCourseModal">취소</button>
            <button type="submit" class="submit-btn" :disabled="isLoading">
              {{ isLoading ? '수정 중...' : '수정' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 과정 삭제 확인 모달 -->
    <div v-if="showDeleteCourseModal" class="modal-overlay" @click="closeDeleteCourseModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>과정 삭제 확인</h3>
          <button class="modal-close" @click="closeDeleteCourseModal">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2"/>
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2"/>
            </svg>
          </button>
        </div>
        <div class="delete-warning">
          <div class="warning-icon">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 8V12M12 16H12.01M10.29 3.86L1.82 18C1.64466 18.3024 1.55685 18.6453 1.56455 18.9928C1.57225 19.3403 1.67516 19.6792 1.86244 19.9757C2.04973 20.2723 2.31561 20.5157 2.6289 20.6812C2.9422 20.8467 3.29427 20.9286 3.65 20.92H20.35C20.7057 20.9286 21.0578 20.8467 21.3711 20.6812C21.6844 20.5157 21.9503 20.2723 22.1376 19.9757C22.3248 19.6792 22.4278 19.3403 22.4355 18.9928C22.4432 18.6453 22.3553 18.3024 22.18 18L13.71 3.86C13.5317 3.56611 13.2807 3.32312 12.9812 3.15446C12.6817 2.98581 12.3438 2.89725 12 2.89725C11.6562 2.89725 11.3183 2.98581 11.0188 3.15446C10.7193 3.32312 10.4683 3.56611 10.29 3.86Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="warning-content">
            <h4>정말로 과정을 삭제하시겠습니까?</h4>
            <p>이 작업은 되돌릴 수 없으며, 해당 과정이 완전히 삭제됩니다.</p>
            <div class="course-info">
              <strong>삭제할 과정: {{ deletingCourse.nameCourse }}</strong>
            </div>
          </div>
        </div>
        <div class="modal-actions delete-modal-actions">
          <button type="button" class="cancel-btn" @click="closeDeleteCourseModal">취소</button>
          <button 
            type="button" 
            class="delete-confirm-btn" 
            @click="deleteCourse(deletingCourse.seqCourse)"
            :disabled="isLoading"
          >
            {{ isLoading ? '삭제 중...' : '삭제' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import axios from 'axios'
import { swAlert } from '@/utils/sweetAlert'
import { useAdminCampusFilter } from '@/composables/useAdminCampusFilter'

// 반응형 데이터
const activeTab = ref('courses')
const courseList = ref([])
const campusList = ref([])
const isLoading = ref(false)

// 캠퍼스 필터 관련 (과정 관리 탭용)
const {
  showCampusFilter,
  currentUserCampusId,
  selectedCampus,
  campuses,
  fetchAdminInfo,
  fetchCampuses,
  getCampusParam,
} = useAdminCampusFilter()

// 모달 상태
const showAddCourseModal = ref(false)
const showEditCourseModal = ref(false)
const showDeleteCourseModal = ref(false)
// 폼 데이터
const newCourse = ref({
  nameCourse: '',
  seqCampus: null,
  startDtCourse: '',
  finishDtCourse: ''
})

const editingCourse = ref({
  seqCourse: null,
  nameCourse: '',
  seqCampus: null,
  startDtCourse: '',
  finishDtCourse: ''
})

const deletingCourse = ref({
  seqCourse: null,
  nameCourse: ''
})


// 활성 캠퍼스 목록 (별도로 관리)
const activeCampusListForSelect = ref([])

// 계산된 속성
const activeCampusList = computed(() => {
  // 캠퍼스 관리 탭에서는 전체 목록에서 필터링
  if (activeTab.value === 'campuses') {
    return campusList.value.filter(c => c.isActive)
  }
  // 과정 관리 탭에서는 별도로 관리하는 활성 캠퍼스 목록 사용
  return activeCampusListForSelect.value
})

const activeCampusCount = computed(() => {
  return campusList.value.filter(c => c.isActive).length
})

// 키보드 이벤트 핸들러
const handleKeydown = (event) => {
  if (event.key === 'Escape') {
    if (showAddCourseModal.value) closeAddCourseModal()
    if (showEditCourseModal.value) closeEditCourseModal()
    if (showDeleteCourseModal.value) closeDeleteCourseModal()
  }
}


// 캠퍼스 변경 핸들러
const onCampusChange = () => {
  fetchCourseList()
}

// 과정 관련 함수들
const fetchCourseList = async () => {
  try {
    isLoading.value = true
    const response = await axios.get(`/api/courses${getCampusParam()}`)
    courseList.value = response.data.data
  } catch (error) {
    console.error('과정 목록 로드 실패:', error)
    await swAlert('과정 목록을 불러오는데 실패했습니다.', 'error')
  } finally {
    isLoading.value = false
  }
}

const addCourse = async () => {
  if (!newCourse.value.nameCourse || !newCourse.value.startDtCourse || !newCourse.value.finishDtCourse) {
    await swAlert('필수 정보를 모두 입력해주세요.', 'warning')
    return
  }

  try {
    isLoading.value = true
    const courseData = {
      nameCourse: newCourse.value.nameCourse,
      seqCampus: newCourse.value.seqCampus,
      startDtCourse: newCourse.value.startDtCourse,
      finishDtCourse: newCourse.value.finishDtCourse
    }
    await axios.post('/api/courses', courseData)

    await swAlert('과정이 성공적으로 추가되었습니다.', 'success')
    closeAddCourseModal()
    await fetchCourseList()
  } catch (error) {
    await swAlert(error.response?.data?.msg || '과정 추가에 실패했습니다.', 'error')
  } finally {
    isLoading.value = false
  }
}

const openEditCourseModal = (course) => {
  editingCourse.value = {
    seqCourse: course.seqCourse,
    nameCourse: course.nameCourse,
    seqCampus: course.seqCampus,
    startDtCourse: course.startDtCourse,
    finishDtCourse: course.finishDtCourse
  }
  showEditCourseModal.value = true
}

const updateCourse = async () => {
  if (!editingCourse.value.nameCourse || !editingCourse.value.startDtCourse || !editingCourse.value.finishDtCourse) {
    await swAlert('필수 정보를 모두 입력해주세요.', 'warning')
    return
  }

  try {
    isLoading.value = true
    const courseData = {
      nameCourse: editingCourse.value.nameCourse,
      seqCampus: editingCourse.value.seqCampus,
      startDtCourse: editingCourse.value.startDtCourse,
      finishDtCourse: editingCourse.value.finishDtCourse
    }
    await axios.put(`/api/courses/${editingCourse.value.seqCourse}`, courseData)

    await swAlert('과정이 성공적으로 수정되었습니다.', 'success')
    closeEditCourseModal()
    await fetchCourseList()
  } catch (error) {
    await swAlert(error.response?.data?.msg || '과정 수정에 실패했습니다.', 'error')
  } finally {
    isLoading.value = false
  }
}

const confirmDeleteCourse = (course) => {
  deletingCourse.value = {
    seqCourse: course.seqCourse,
    nameCourse: course.nameCourse
  }
  showDeleteCourseModal.value = true
}

const deleteCourse = async (courseId) => {
  try {
    isLoading.value = true
    await axios.delete(`/api/courses/${courseId}`)
    
    await swAlert('과정이 성공적으로 삭제되었습니다.', 'success')
    closeDeleteCourseModal()
    await fetchCourseList()
  } catch (error) {
    await swAlert(error.response?.data?.msg || '과정 삭제에 실패했습니다.', 'error')
  } finally {
    isLoading.value = false
  }
}

const closeAddCourseModal = () => {
  showAddCourseModal.value = false
  newCourse.value = {
    nameCourse: '',
    seqCampus: null,
    startDtCourse: '',
    finishDtCourse: ''
  }
}

const closeEditCourseModal = () => {
  showEditCourseModal.value = false
  editingCourse.value = {
    seqCourse: null,
    nameCourse: '',
    seqCampus: null,
    startDtCourse: '',
    finishDtCourse: ''
  }
}

const closeDeleteCourseModal = () => {
  showDeleteCourseModal.value = false
  deletingCourse.value = {
    seqCourse: null,
    nameCourse: ''
  }
}

// 캠퍼스 관련 함수들
// 활성 캠퍼스 목록 조회 (과정 추가/수정 모달용)
const fetchActiveCampusList = async () => {
  try {
    const response = await axios.get('/api/campus')
    return response.data.data
  } catch (error) {
    console.error('활성 캠퍼스 목록 로드 실패:', error)
    return []
  }
}

// 전체 캠퍼스 목록 조회 (캠퍼스 관리 탭용)
const fetchCampusList = async () => {
  try {
    isLoading.value = true
    const response = await axios.get('/api/campus/all')
    campusList.value = response.data.data
  } catch (error) {
    console.error('캠퍼스 목록 로드 실패:', error)
    if (error.response?.status === 403) {
      // 403 에러 시 활성 캠퍼스만이라도 가져오기
      try {
        const activeResponse = await axios.get('/api/campus')
        campusList.value = activeResponse.data.data
      } catch (fallbackError) {
        console.error('활성 캠퍼스 목록 로드 실패:', fallbackError)
        campusList.value = []
      }
    } else {
      campusList.value = []
    }
  } finally {
    isLoading.value = false
  }
}


// 날짜 포맷팅
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR')
}

// 탭 변경 핸들러
const switchToCampusTab = () => {
  activeTab.value = 'campuses'
  // 캠퍼스 관리 탭으로 전환 시 전체 목록 로드
  if (campusList.value.length === 0) {
    fetchCampusList()
  }
}

// 탭 변경 감지
watch(activeTab, (newTab) => {
  if (newTab === 'campuses' && campusList.value.length === 0) {
    // 캠퍼스 관리 탭으로 전환 시 전체 목록 로드
    fetchCampusList()
  }
})

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
  // 활성 캠퍼스 목록 먼저 로드 (과정 추가/수정 모달용)
  activeCampusListForSelect.value = await fetchActiveCampusList()
  // 사용자 타입 확인 및 캠퍼스 필터 설정
  await fetchAdminInfo()
  // 캠퍼스 목록 가져오기 (과정 관리 탭 필터용)
  await fetchCampuses()
  // 과정 목록 로드
  await fetchCourseList()
  // 현재 탭에 따라 캠퍼스 목록 로드
  if (activeTab.value === 'campuses') {
    await fetchCampusList()
  }
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.course-management {
  max-width: 100%;
}

.section-header {
  margin-bottom: 24px;
}

.section-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--pb-color-heading);
  margin-bottom: 4px;
}

.section-description {
  font-size: 0.9rem;
  color: var(--pb-color-text-muted);
  margin: 0;
}

.tab-menu {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  border-bottom: 1px solid var(--pb-color-border);
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  background: transparent;
  color: var(--pb-color-text-muted);
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.15s ease, border-color 0.15s ease;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
}

.tab-btn:hover {
  color: var(--pb-color-text);
  background: var(--pb-color-surface-muted);
}

.tab-btn.active {
  color: var(--pb-color-brand);
  border-bottom-color: var(--pb-color-brand);
  font-weight: 600;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--pb-color-brand-soft);
  border-radius: var(--pb-radius-md);
  color: var(--pb-color-text);
  border: 1px solid var(--pb-color-border);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: var(--pb-color-surface);
  border-radius: var(--pb-radius-md);
  color: var(--pb-color-brand);
}

.stat-number {
  font-size: 1.75rem;
  font-weight: 700;
  line-height: 1;
  color: var(--pb-color-heading);
}

.stat-label {
  font-size: 0.875rem;
  color: var(--pb-color-text-muted);
}

.action-filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20px;
  gap: 12px;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 200px;
}

.filter-label {
  font-weight: 500;
  color: var(--pb-color-text-muted);
  font-size: 0.875rem;
}

.filter-select {
  padding: 10px 14px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  font-size: 0.9rem;
  transition: border-color 0.15s;
  background: var(--pb-color-surface);
  cursor: pointer;
}

.filter-select:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px rgba(47, 111, 78, 0.12);
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  background: var(--pb-color-brand);
  color: white;
  border: none;
  border-radius: var(--pb-radius-sm);
  font-weight: 600;
  font-size: 0.9rem;
  transition: background 0.15s;
  cursor: pointer;
}

.add-btn:hover {
  background: var(--pb-color-brand-strong);
}

.table-container {
  background: var(--pb-color-surface);
  border-radius: var(--pb-radius-md);
  overflow: hidden;
  box-shadow: var(--pb-shadow-sm);
  border: 1px solid var(--pb-color-border);
}

.table-header {
  padding: 20px 24px;
  border-bottom: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface-muted);
}

.table-header h3 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0;
}

.table-wrapper {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  text-align: left;
  padding: 14px 20px;
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-heading);
  font-weight: 600;
  font-size: 0.875rem;
  border-bottom: 1px solid var(--pb-color-border);
}

.data-table td {
  padding: 14px 20px;
  border-bottom: 1px solid var(--pb-color-border);
  color: var(--pb-color-text);
  font-size: 0.9rem;
}

.data-row:hover {
  background: var(--pb-color-surface-muted);
}

.course-name,
.campus-name {
  font-weight: 500;
  color: var(--pb-color-heading);
}

.course-date {
  color: var(--pb-color-text-muted);
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: var(--pb-radius-xs);
  font-size: 0.8rem;
  font-weight: 600;
}

.status-badge.active {
  background: var(--pb-color-success-soft);
  color: var(--pb-color-success);
}

.status-badge.inactive {
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text-muted);
}

.course-actions,
.campus-actions {
  display: flex;
  gap: 6px;
}

.edit-btn,
.delete-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  cursor: pointer;
  transition: background 0.15s;
}

.edit-btn {
  background: var(--pb-color-brand-soft);
  color: var(--pb-color-brand);
}

.edit-btn:hover {
  background: var(--pb-color-brand);
  color: white;
  border-color: var(--pb-color-brand);
}

.delete-btn {
  background: var(--pb-color-danger-soft);
  color: var(--pb-color-danger);
}

.delete-btn:hover:not(:disabled) {
  background: var(--pb-color-danger);
  color: white;
  border-color: var(--pb-color-danger);
}

.delete-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 모달 스타일 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: var(--pb-color-surface);
  border-radius: var(--pb-radius-md);
  width: 90%;
  max-width: 500px;
  box-shadow: var(--pb-shadow-md);
  border: 1px solid var(--pb-color-border);
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 20px 0 20px;
  margin-bottom: 20px;
}

.modal-header h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0;
}

.modal-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface-muted);
  border-radius: var(--pb-radius-sm);
  color: var(--pb-color-text-muted);
  cursor: pointer;
  transition: background 0.15s;
}

.modal-close:hover {
  background: var(--pb-color-border);
  color: var(--pb-color-text);
}

.modal-form {
  padding: 0 20px 20px 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  font-size: 0.9rem;
  color: var(--pb-color-text);
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  font-size: 0.9rem;
  transition: border-color 0.15s;
  box-sizing: border-box;
  background: var(--pb-color-surface);
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px rgba(47, 111, 78, 0.12);
}

.form-select {
  cursor: pointer;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.checkbox-label input[type="checkbox"] {
  width: auto;
  cursor: pointer;
}

.required-mark {
  color: var(--pb-color-danger);
  font-weight: 600;
}

.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}

.delete-modal-actions {
  padding: 0 20px 20px 20px;
  margin-top: 0;
}

.cancel-btn,
.submit-btn,
.delete-confirm-btn {
  padding: 10px 18px;
  border: none;
  border-radius: var(--pb-radius-sm);
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.15s;
}

.cancel-btn {
  background: var(--pb-color-surface-muted);
  border: 1px solid var(--pb-color-border);
  color: var(--pb-color-text);
}

.cancel-btn:hover {
  background: var(--pb-color-border);
}

.submit-btn {
  background: var(--pb-color-brand);
  color: white;
}

.submit-btn:hover:not(:disabled) {
  background: var(--pb-color-brand-strong);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.delete-confirm-btn {
  background: var(--pb-color-danger);
  color: white;
}

.delete-confirm-btn:hover:not(:disabled) {
  filter: brightness(0.9);
}

.delete-confirm-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.delete-warning {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 20px 28px 20px;
  text-align: center;
}

.warning-icon {
  color: var(--pb-color-danger);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.warning-icon svg {
  width: 56px;
  height: 56px;
  flex-shrink: 0;
}

.warning-content h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0 0 8px 0;
}

.warning-content p {
  color: var(--pb-color-text-muted);
  margin: 0 0 16px 0;
  line-height: 1.5;
  font-size: 0.9rem;
}

.course-info,
.campus-info {
  padding: 10px 14px;
  background: var(--pb-color-danger-soft);
  border: 1px solid var(--pb-color-danger);
  border-radius: var(--pb-radius-sm);
  color: var(--pb-color-danger);
  font-size: 0.875rem;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .action-bar {
    justify-content: center;
  }

  .data-table th,
  .data-table td {
    padding: 12px 14px;
    font-size: 0.875rem;
  }

  .modal-content {
    width: 95%;
    margin: 20px;
  }

  .modal-header,
  .modal-form {
    padding: 16px;
  }

  .modal-actions {
    flex-direction: column;
  }

  .cancel-btn,
  .submit-btn,
  .delete-confirm-btn {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .data-table {
    font-size: 0.8rem;
  }

  .course-actions,
  .campus-actions {
    flex-direction: column;
    gap: 4px;
  }

  .edit-btn,
  .delete-btn {
    width: 28px;
    height: 28px;
  }
}
</style>

