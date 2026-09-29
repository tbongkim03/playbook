<template>
  <div class="course-management">
    <div class="section-header">
      <h2 class="section-title">과정 관리</h2>
      <p class="section-description">과정을 관리하고 캠퍼스 정보를 조회할 수 있습니다.</p>
    </div>

    <!-- 과정 관리 탭 -->
    <div class="tab-content">
      <!-- 통계 카드 -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">
            <PhGraduationCap weight="duotone" :size="24" />
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
          <button class="export-btn" @click="exportData">
            <PhDownloadSimple weight="duotone" :size="16" />
            엑셀로 내보내기
          </button>
          <button class="add-btn" @click="showAddCourseModal = true">
            <PhPlusCircle weight="duotone" :size="20" />
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
              <tr v-for="course in pagedCourseList" :key="course.seqCourse" class="data-row">
                <td class="course-name">{{ course.nameCourse }}</td>
                <td class="course-campus">{{ course.campusName || '-' }}</td>
                <td class="course-date">{{ formatDate(course.startDtCourse) }}</td>
                <td class="course-date">{{ formatDate(course.finishDtCourse) }}</td>
                <td class="course-actions">
                  <button class="edit-btn" @click="openEditCourseModal(course)">
                    <PhNotePencil weight="duotone" :size="16" />
                  </button>
                  <button class="delete-btn" @click="confirmDeleteCourse(course)">
                    <PhTrash weight="duotone" :size="16" />
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 페이지네이션 -->
        <div class="gl-pagination" v-if="totalPages > 1">
          <span class="gl-pagination-info">{{ paginationInfo }}</span>
          <nav class="gl-pagination-nav">
            <button class="gl-page-btn prev-btn" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">
              <PhCaretLeft weight="duotone" :size="14" />
              이전
            </button>
            <template v-for="item in paginationItems" :key="String(item) + '-cm'">
              <span v-if="item === '...'" class="gl-page-ellipsis">…</span>
              <button v-else class="gl-page-btn" :class="{ active: item === currentPage }" @click="changePage(item)">{{ item }}</button>
            </template>
            <button class="gl-page-btn next-btn" :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">
              다음
              <PhCaretRight weight="duotone" :size="14" />
            </button>
          </nav>
        </div>
      </div>
    </div>

    <!-- 과정 추가 모달 -->
    <div v-if="showAddCourseModal" class="modal-overlay" v-modal-backdrop="closeAddCourseModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>새 과정 추가</h3>
          <button class="modal-close" @click="closeAddCourseModal">
            <PhX weight="duotone" :size="24" />
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
    <div v-if="showEditCourseModal" class="modal-overlay" v-modal-backdrop="closeEditCourseModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>과정 수정</h3>
          <button class="modal-close" @click="closeEditCourseModal">
            <PhX weight="duotone" :size="24" />
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
    <div v-if="showDeleteCourseModal" class="modal-overlay" v-modal-backdrop="closeDeleteCourseModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>과정 삭제 확인</h3>
          <button class="modal-close" @click="closeDeleteCourseModal">
            <PhX weight="duotone" :size="24" />
          </button>
        </div>
        <div class="delete-warning">
          <div class="warning-icon">
            <PhWarning weight="duotone" :size="64" />
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
import { vModalBackdrop } from '@/utils/modalBackdrop'
import { PhCaretLeft, PhGraduationCap, PhCaretRight, PhDownloadSimple, PhNotePencil, PhPlusCircle, PhTrash, PhWarning, PhX } from '@phosphor-icons/vue'
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import * as courseApi from '@/api/course'
import * as campusApi from '@/api/campus'
import { swAlert } from '@/utils/sweetAlert'
import { handleApiError } from '@/utils/apiErrorHandler'
import { formatDate } from '@/utils/dateFormatter'
import { useAdminCampusFilter } from '@/composables/useAdminCampusFilter'
import { usePagination } from '@/composables/usePagination'
import { exportToXlsx } from '@/utils/exportSheet'

// 반응형 데이터
const courseList = ref([])
const isLoading = ref(false)

// 페이지네이션
const { currentPage, totalPages, pagedList: pagedCourseList,
        paginationItems, paginationInfo, changePage } = usePagination(courseList, 20)

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
const activeCampusList = computed(() => activeCampusListForSelect.value)

const handleKeydown = (event) => {
  if (event.key === 'Escape') {
    if (showAddCourseModal.value) closeAddCourseModal()
    if (showEditCourseModal.value) closeEditCourseModal()
    if (showDeleteCourseModal.value) closeDeleteCourseModal()
  }
}


// 캠퍼스 변경 핸들러
const onCampusChange = () => {
  currentPage.value = 1
  fetchCourseList()
}

// 과정 관련 함수들
const fetchCourseList = async () => {
  try {
    isLoading.value = true
    const campusId = showCampusFilter.value && selectedCampus.value ? selectedCampus.value : null
    const response = await courseApi.getAll(campusId)
    courseList.value = response.data.data
  } catch (error) {
    await handleApiError(error, '과정 목록을 불러오는데 실패했습니다.')
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
    await courseApi.create(courseData)

    await swAlert('과정이 성공적으로 추가되었습니다.', 'success')
    closeAddCourseModal()
    await fetchCourseList()
  } catch (error) {
    await handleApiError(error, '과정 추가에 실패했습니다.')
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
    await courseApi.update(editingCourse.value.seqCourse, courseData)

    await swAlert('과정이 성공적으로 수정되었습니다.', 'success')
    closeEditCourseModal()
    await fetchCourseList()
  } catch (error) {
    await handleApiError(error, '과정 수정에 실패했습니다.')
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
    await courseApi.remove(courseId)
    
    await swAlert('과정이 성공적으로 삭제되었습니다.', 'success')
    closeDeleteCourseModal()
    await fetchCourseList()
  } catch (error) {
    await handleApiError(error, '과정 삭제에 실패했습니다.')
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
    const response = await campusApi.getAll()
    return response.data.data
  } catch (error) {
    console.error('활성 캠퍼스 목록 로드 실패:', error)
    return []
  }
}

// 날짜 포맷팅

const exportData = async () => {
  try {
    const campusId = showCampusFilter.value && selectedCampus.value ? selectedCampus.value : null
    const res = await courseApi.exportExcel(campusId)
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = '과정목록.xlsx'
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('엑셀 내보내기 실패:', e)
  }
}

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
  activeCampusListForSelect.value = await fetchActiveCampusList()
  await fetchAdminInfo()
  await fetchCampuses()
  await fetchCourseList()
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
/* ── 루트 ── */
.course-management {
  max-width: 100%;
  font-size: 13px;
  color: var(--pb-color-text);
}

/* ── 섹션 헤더 ── */
.section-header { margin-bottom: 20px; }

.section-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--pb-color-heading);
  margin: 0 0 3px;
}

.section-description {
  font-size: 13px;
  color: var(--pb-color-text-muted);
  margin: 0;
}

/* ── 탭 메뉴 ── */
.tab-menu {
  display: flex;
  gap: 0;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--pb-color-border);
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 9px 18px;
  border: none;
  background: transparent;
  color: var(--pb-color-text-muted);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: color 0.12s, border-color 0.12s;
}

.tab-btn:hover {
  color: var(--pb-color-text);
  background: var(--pb-color-surface-subtle);
}

.tab-btn.active {
  color: var(--pb-color-brand);
  border-bottom-color: var(--pb-color-brand);
  font-weight: 600;
}

/* ── 통계 카드 ── */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-lg);
  box-shadow: var(--pb-shadow-xs);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: var(--pb-color-accent-soft);
  border-radius: var(--pb-radius-md);
  color: var(--pb-color-accent);
  flex-shrink: 0;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  line-height: 1;
  color: var(--pb-color-heading);
}

.stat-label {
  font-size: 12px;
  color: var(--pb-color-text-soft);
  margin-top: 2px;
}

/* ── 액션/필터 바 ── */
.action-filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 16px;
  gap: 10px;
  flex-wrap: wrap;
}

.action-bar { display: flex; justify-content: flex-end; gap: 8px; }

.export-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  padding: 0 14px;
  background: #fff;
  color: var(--pb-color-text-primary);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
  white-space: nowrap;
}
.export-btn:hover { background: var(--pb-color-bg-subtle); }

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 190px;
}

.filter-label {
  font-size: 11px;
  font-weight: 500;
  color: var(--pb-color-text-soft);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.filter-select {
  height: 34px;
  padding: 0 10px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  cursor: pointer;
  transition: border-color 0.15s;
}

.filter-select:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px var(--pb-color-brand-muted);
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 16px;
  background: var(--pb-color-brand);
  color: var(--pb-color-surface);
  border: none;
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.12s;
}

.add-btn:hover { background: var(--pb-color-brand-strong); }

/* ── 테이블 컨테이너 ── */
.table-container {
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-lg);
  box-shadow: var(--pb-shadow-sm);
  overflow: hidden;
}

.table-header {
  padding: 13px 20px;
  border-bottom: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface-muted);
}

.table-header h3 {
  font-size: 13px;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0;
}

.table-wrapper { overflow-x: auto; }

/* ── 테이블 ── */
.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  text-align: left;
  padding: 9px 16px;
  background: var(--pb-color-surface-subtle);
  color: var(--pb-color-text-soft);
  font-weight: 600;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 1px solid var(--pb-color-border);
}

.data-table td {
  padding: 10px 16px;
  border-bottom: 1px solid var(--pb-color-border);
  color: var(--pb-color-text);
  font-size: 13px;
  vertical-align: middle;
}

.data-row:hover td { background: var(--pb-color-surface-muted); }

.course-name,
.campus-name {
  font-weight: 500;
  color: var(--pb-color-heading);
}

.course-date { color: var(--pb-color-text-soft); font-size: 12px; }

/* ── 상태 배지 ── */
.status-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: var(--pb-radius-xs);
  font-size: 11px;
  font-weight: 600;
}

.status-badge.active  { background: var(--pb-color-success-soft); color: var(--pb-color-success); }
.status-badge.inactive { background: var(--pb-color-surface-muted); color: var(--pb-color-text-muted); }

/* ── 액션 버튼 ── */
.course-actions,
.campus-actions {
  display: flex;
  gap: 4px;
}

.edit-btn,
.delete-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  cursor: pointer;
  transition: background 0.12s, border-color 0.12s, color 0.12s;
}

.edit-btn {
  background: var(--pb-color-surface);
  color: var(--pb-color-text-muted);
}

.edit-btn:hover {
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text);
  border-color: var(--pb-color-border-strong);
}

.delete-btn {
  background: var(--pb-color-danger-soft);
  color: var(--pb-color-danger);
}

.delete-btn:hover:not(:disabled) {
  background: var(--pb-color-danger);
  color: var(--pb-color-surface);
  border-color: var(--pb-color-danger);
}

.delete-btn:disabled { opacity: 0.45; cursor: not-allowed; }

/* ── 모달 ── */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(30, 31, 29, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-lg);
  width: 90%;
  max-width: 500px;
  box-shadow: var(--pb-shadow-popover);
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid var(--pb-color-border);
}

.modal-header h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0;
}

.modal-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface-muted);
  border-radius: var(--pb-radius-sm);
  color: var(--pb-color-text-soft);
  cursor: pointer;
  transition: background 0.12s;
}

.modal-close:hover {
  background: var(--pb-color-surface-subtle);
  color: var(--pb-color-text);
}

.modal-form { padding: 16px 20px 20px; }

.form-group { margin-bottom: 14px; }

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-size: 12px;
  font-weight: 500;
  color: var(--pb-color-text-muted);
}

.form-group input,
.form-group select {
  width: 100%;
  height: 34px;
  padding: 0 12px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  box-sizing: border-box;
  transition: border-color 0.15s;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px var(--pb-color-brand-muted);
}

.form-select { cursor: pointer; }

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
  gap: 8px;
  justify-content: flex-end;
  padding: 12px 20px 20px;
}

.delete-modal-actions {
  padding: 0 20px 20px;
  margin-top: 0;
}

.cancel-btn,
.submit-btn,
.delete-confirm-btn {
  height: 32px;
  padding: 0 16px;
  border: none;
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.12s;
}

.cancel-btn {
  background: var(--pb-color-surface-muted);
  border: 1px solid var(--pb-color-border);
  color: var(--pb-color-text);
}

.cancel-btn:hover { background: var(--pb-color-border); }

.submit-btn {
  background: var(--pb-color-brand);
  color: var(--pb-color-surface);
}

.submit-btn:hover:not(:disabled) { background: var(--pb-color-brand-strong); }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.delete-confirm-btn {
  background: var(--pb-color-danger);
  color: var(--pb-color-surface);
}

.delete-confirm-btn:hover:not(:disabled) { background: var(--pb-color-danger-strong); }
.delete-confirm-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* ── 삭제 경고 ── */
.delete-warning {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 22px 20px;
  text-align: center;
}

.warning-icon {
  color: var(--pb-color-danger);
  margin-bottom: 12px;
}

.warning-icon svg { width: 46px; height: 46px; }

.warning-content h4 {
  font-size: 14px;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0 0 6px;
}

.warning-content p {
  font-size: 13px;
  color: var(--pb-color-text-muted);
  margin: 0 0 14px;
  line-height: 1.5;
}

.course-info,
.campus-info {
  padding: 8px 12px;
  background: var(--pb-color-danger-soft);
  border: 1px solid var(--pb-color-danger);
  border-radius: var(--pb-radius-sm);
  color: var(--pb-color-danger);
  font-size: 12px;
}

/* ── 페이지네이션 ── */
.gl-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-top: 1px solid var(--pb-color-border);
}
.gl-pagination-info { font-size: 13px; color: var(--pb-color-text-muted); }
.gl-pagination-nav { display: flex; align-items: center; gap: 2px; }
.gl-page-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-width: 32px;
  height: 32px;
  padding: 0 8px;
  border: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  border-radius: var(--pb-radius-sm);
  cursor: pointer;
  font-size: 13px;
  transition: background 0.12s, color 0.12s, border-color 0.12s;
  white-space: nowrap;
}
.gl-page-btn:hover:not(:disabled):not(.active) {
  background: var(--pb-color-surface-muted);
  border-color: var(--pb-color-border-strong);
}
.gl-page-btn.active {
  background: var(--pb-color-brand);
  color: #fff;
  border-color: var(--pb-color-brand);
  font-weight: 600;
}
.gl-page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.gl-page-btn.prev-btn, .gl-page-btn.next-btn { padding: 0 10px; }
.gl-page-ellipsis {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  font-size: 13px;
  color: var(--pb-color-text-soft);
}

/* ── 반응형 ── */
@media (max-width: 768px) {
  .stats-grid { grid-template-columns: 1fr; }
  .action-bar { justify-content: center; flex-wrap: wrap; }
  .action-filter-bar { flex-direction: column; align-items: stretch; }
  .filter-group { min-width: unset; }
  .data-table th, .data-table td { padding: 9px 12px; }
  .modal-content { width: 95%; }
  .modal-actions { flex-direction: column; }
  .cancel-btn, .submit-btn, .delete-confirm-btn { width: 100%; }
  .export-btn { width: 100%; justify-content: center; }
}

@media (max-width: 480px) {
  .course-actions, .campus-actions { flex-direction: column; gap: 3px; }
  .edit-btn, .delete-btn { width: 28px; height: 28px; }
}
</style>

