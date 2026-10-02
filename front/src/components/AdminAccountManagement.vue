<template>
  <div class="admin-account-management">
    <div class="pb-page-head">
      <div>
        <h2>관리자 계정 관리</h2>
        <p>관리자 계정을 추가, 수정, 삭제할 수 있습니다.</p>
      </div>
      <div class="pb-head-actions">
        <button type="button" class="pb-btn pb-btn-primary" @click="showAddModal = true">
          <PhPlusCircle weight="duotone" :size="18" />
          관리자 추가
        </button>
      </div>
    </div>

    <!-- 통계 카드 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">
          <PhUserCheck weight="duotone" :size="24" />
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ adminList.length }}</div>
          <div class="stat-label">총 관리자</div>
        </div>
      </div>
    </div>


    <!-- 필터 영역 -->
    <div class="pb-filter-bar" v-if="showCampusFilter">
      <select v-model="selectedCampus" @change="onCampusChange" class="filter-select">
          <option value="">전체 캠퍼스</option>
          <option
            v-for="campus in campusList"
            :key="campus.seqCampus"
            :value="campus.seqCampus"
          >
            {{ campus.nameCampus }}
          </option>
        </select>
    </div>

    <!-- 관리자 목록 테이블 -->
    <div class="admin-table-container">
      <div class="table-header pb-list-head">
        <h3>관리자 목록</h3>
        <div class="pb-list-actions">
          <button type="button" class="pb-btn pb-btn-outline" @click="exportData">
            <PhDownloadSimple weight="duotone" :size="16" />
            엑셀 내보내기
          </button>
        </div>
      </div>
      
      <div class="table-wrapper">
        <table class="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>이름</th>
              <th>캠퍼스</th>
              <th>디스코드 ID</th>
              <th>생성일</th>
              <th>작업</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!pagedAdminList.length">
              <td colspan="6" class="pb-empty-row">등록된 관리자가 없습니다.</td>
            </tr>
            <tr v-for="admin in pagedAdminList" :key="admin.idAdmin" class="admin-row">
              <td class="admin-id">{{ admin.idAdmin }}</td>
              <td class="admin-name">{{ admin.nameAdmin }}</td>
              <td class="admin-campus">{{ admin.campusName || '전체' }}</td>
              <td class="admin-discord">{{ admin.dcAdmin || '-' }}</td>
              <td class="admin-date">{{ formatDate(admin.createdAt) }}</td>
              <td class="admin-actions">
                <button
                  v-if="canEditAdmin(admin)"
                  class="edit-btn"
                  @click="openEditModal(admin)"
                >
                  <PhNotePencil weight="duotone" :size="16" />
                </button>
                <button class="delete-btn" @click="confirmDeleteAdmin(admin)">
                  <PhTrash weight="duotone" :size="16" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <PbPagination :page="currentPage" :total-pages="totalPages" :info="paginationInfo" @change="changePage" />
    </div>

    <!-- 관리자 추가 모달 -->
    <div v-if="showAddModal" class="modal-overlay" v-modal-backdrop="closeAddModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>새 관리자 추가</h3>
          <button class="modal-close" @click="closeAddModal">
            <PhX weight="duotone" :size="24" />
          </button>
        </div>
        <form @submit.prevent="addAdmin" class="modal-form">
          <div class="form-group">
            <label for="newAdminId">관리자 ID</label>
            <div class="id-input-group">
              <input 
                type="text" 
                id="newAdminId" 
                v-model="newAdmin.idAdmin" 
                required 
                placeholder="관리자 ID를 입력하세요"
                @blur="validateId"
              />
              <button 
                type="button" 
                class="validate-btn" 
                @click="validateId"
                :disabled="!newAdmin.idAdmin"
              >
                중복확인
              </button>
            </div>
            <div v-if="idValidation.message" :class="['validation-message', idValidation.isValid ? 'valid' : 'invalid']">
              {{ idValidation.message }}
            </div>
          </div>
          <div class="form-group">
            <label for="newAdminName">관리자 이름</label>
            <input
              type="text"
              id="newAdminName"
              v-model="newAdmin.nameAdmin"
              required
              placeholder="관리자 이름을 입력하세요"
            />
          </div>
          <div class="form-group">
            <label for="newAdminCampus">캠퍼스</label>
            <select
              id="newAdminCampus"
              v-model="newAdmin.seqCampus"
              class="form-select"
            >
              <option :value="null">전체 관리자</option>
              <option v-for="campus in campusList" :key="campus.seqCampus" :value="campus.seqCampus">
                {{ campus.nameCampus }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label for="newAdminPassword">비밀번호</label>
            <input 
              type="password" 
              id="newAdminPassword" 
              v-model="newAdmin.pwAdmin" 
              required 
              placeholder="비밀번호를 입력하세요"
            />
          </div>
          <div class="form-group">
            <label for="newAdminDiscord">디스코드 ID</label>
            <input 
              type="text" 
              id="newAdminDiscord" 
              v-model="newAdmin.dcAdmin" 
              placeholder="디스코드 ID를 입력하세요"
            />
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="closeAddModal">취소</button>
            <button 
              type="submit" 
              class="submit-btn" 
              :disabled="isLoading || !idValidation.isValid || !newAdmin.idAdmin"
            >
              {{ isLoading ? '추가 중...' : '추가' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 관리자 수정 모달 (비밀번호 검증 포함) -->
    <div v-if="showEditModal" class="modal-overlay" v-modal-backdrop="closeEditModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>관리자 계정 수정</h3>
          <button class="modal-close" @click="closeEditModal">
            <PhX weight="duotone" :size="24" />
          </button>
        </div>
        <form @submit.prevent="updateAdmin" class="modal-form">
          <div class="form-group">
            <label>관리자 ID</label>
            <input 
              type="text" 
              :value="editingAdmin.idAdmin" 
              disabled
              class="disabled-input"
            />
          </div>
          <div class="form-group">
            <label>관리자 이름</label>
            <input 
              type="text" 
              :value="editingAdmin.nameAdmin" 
              disabled
              class="disabled-input"
            />
          </div>
          <div class="form-group">
            <label for="editPassword">현재 비밀번호 확인 <span class="required-mark">*</span></label>
            <input 
              type="password" 
              id="editPassword" 
              v-model="editPassword" 
              placeholder="현재 비밀번호를 입력하세요"
              required
            />
          </div>
          <div class="form-group">
            <label for="editAdminDiscord">새 디스코드 ID</label>
            <input 
              type="text" 
              id="editAdminDiscord" 
              v-model="editingAdmin.dcAdmin" 
              placeholder="디스코드 ID를 입력하세요 (변경하지 않으려면 비워두세요)"
            />
          </div>
          <div class="form-group">
            <label for="editNewPassword">새 비밀번호</label>
            <input 
              type="password" 
              id="editNewPassword" 
              v-model="editNewPassword" 
              placeholder="새 비밀번호를 입력하세요 (변경하지 않으려면 비워두세요)"
            />
            <div class="form-hint">비밀번호를 변경하지 않으려면 비워두세요.</div>
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="closeEditModal">취소</button>
            <button type="submit" class="submit-btn" :disabled="isLoading || !editPassword">
              {{ isLoading ? '수정 중...' : '수정' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 관리자 삭제 확인 모달 (비밀번호 검증 포함) -->
    <div v-if="showDeleteModal" class="modal-overlay" v-modal-backdrop="closeDeleteModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>관리자 삭제 확인</h3>
          <button class="modal-close" @click="closeDeleteModal">
            <PhX weight="duotone" :size="24" />
          </button>
        </div>
        <div class="delete-warning">
          <div class="warning-icon">
            <PhWarning weight="duotone" :size="64" />
          </div>
          <div class="warning-content">
            <h4>정말로 관리자를 삭제하시겠습니까?</h4>
            <p>이 작업은 되돌릴 수 없으며, 해당 관리자 계정이 완전히 삭제됩니다.</p>
            <div class="admin-info">
              <strong>삭제할 관리자: {{ deletingAdmin.nameAdmin }} ({{ deletingAdmin.idAdmin }})</strong>
            </div>
          </div>
        </div>
        <form @submit.prevent="deleteAdmin(deletingAdmin.idAdmin)" class="modal-form">
          <div class="form-group">
            <label for="deletePassword">현재 비밀번호 확인</label>
            <input 
              type="password" 
              id="deletePassword" 
              v-model="deletePassword" 
              placeholder="현재 비밀번호를 입력하세요"
              required
            />
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="closeDeleteModal">취소</button>
            <button 
              type="submit" 
              class="delete-confirm-btn" 
              :disabled="isLoading || !deletePassword"
            >
              {{ isLoading ? '삭제 중...' : '삭제' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import PbPagination from '@/components/PbPagination.vue'
import { vModalBackdrop } from '@/utils/modalBackdrop'
import { PhDownloadSimple, PhNotePencil, PhPlusCircle, PhTrash, PhUserCheck, PhWarning, PhX } from '@phosphor-icons/vue'
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import * as adminApi from '@/api/admin'
import { swAlert } from '@/utils/sweetAlert'
import { handleApiError } from '@/utils/apiErrorHandler'
import { formatDate } from '@/utils/dateFormatter'
import { useAdminCampusFilter } from '@/composables/useAdminCampusFilter'
import { usePagination } from '@/composables/usePagination'
import { exportToXlsx } from '@/utils/exportSheet'

// 반응형 데이터
const adminList = ref([])
const isLoading = ref(false)

// 페이지네이션
const { currentPage, totalPages, pagedList: pagedAdminList,
        paginationInfo, changePage } = usePagination(adminList, 10)

watch(adminList, () => { currentPage.value = 1 })

// 캠퍼스 필터 관련
const {
  showCampusFilter,
  currentUserCampusId,
  selectedCampus,
  campuses: campusList,
  fetchAdminInfo,
  fetchCampuses: fetchCampusList,
  getCampusParam,
} = useAdminCampusFilter()

// 모달 상태
const showAddModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)

// 비밀번호 입력 필드
const editPassword = ref('')
const editNewPassword = ref('')
const deletePassword = ref('')

// 현재 사용자 정보
const currentUser = ref({
  idAdmin: '',
  nameAdmin: '',
  dcAdmin: '',
  seqCampus: null
})

// ID 검증 상태
const idValidation = ref({
  isValid: false,
  message: '',
  checked: false
})

const handleKeydown = (event) => {
  if (event.key === 'Escape' && showAddModal.value) {
    showAddModal.value = false
  }

  if (event.key === 'Escape' && showEditModal.value) {
    showEditModal.value = false
  }

  if (event.key === 'Escape' && showDeleteModal.value) {
    showDeleteModal.value = false
  }
}

// 폼 데이터
const newAdmin = ref({
  idAdmin: '',
  pwAdmin: '',
  nameAdmin: '',
  dcAdmin: '',
  seqCampus: null
})

const editingAdmin = ref({
  idAdmin: '',
  nameAdmin: '',
  dcAdmin: ''
})

const originalAdmin = ref({
  idAdmin: '',
  nameAdmin: '',
  dcAdmin: ''
})

const deletingAdmin = ref({
  idAdmin: '',
  nameAdmin: '',
  dcAdmin: ''
})


// 비밀번호 검증
const validatePassword = async (idAdmin, password) => {
  try {
    const response = await adminApi.validatePassword(idAdmin, password)
    return response.data.data
  } catch (error) {
    throw error
  }
}

// 현재 사용자 정보 조회
const fetchCurrentUser = async () => {
  const data = await fetchAdminInfo()
  if (data) {
    currentUser.value = {
      idAdmin: data.idAdmin,
      nameAdmin: data.nameAdmin,
      dcAdmin: data.dcAdmin || '',
      seqCampus: data.seqCampus
    }
  }
}

// 캠퍼스 변경 핸들러
const onCampusChange = () => {
  fetchAdminList()
}

// 관리자 수정 권한 체크
const canEditAdmin = (admin) => {
  // 전체 관리자 (seqCampus가 null)는 모든 계정 수정 가능
  if (currentUser.value.seqCampus === null) {
    return true
  }
  // 일반 관리자는 본인 계정만 수정 가능
  return currentUser.value.idAdmin === admin.idAdmin
}

// ID 중복 확인
const validateId = async () => {
  if (!newAdmin.value.idAdmin) {
    idValidation.value = {
      isValid: false,
      message: 'ID를 입력해주세요.',
      checked: false
    }
    return
  }

  try {
    const response = await adminApi.validateId(newAdmin.value.idAdmin)

    const data = response.data.data;

    idValidation.value = {
      isValid: !data.flag,
      message: data.flag ? '이미 사용중인 ID입니다.' : '사용 가능한 ID입니다.', 
      checked: true
    }

  } catch (error) {
    idValidation.value = {
      isValid: false,
      message: 'ID 검증에 실패했습니다.',
      checked: false
    }
  }
}

// 관리자 목록 조회
const fetchAdminList = async () => {
  try {
    isLoading.value = true
    const campusId = showCampusFilter.value && selectedCampus.value ? selectedCampus.value : null
    const response = await adminApi.getList(campusId)
    adminList.value = response.data.data.content || response.data.data
  } catch (error) {
    await handleApiError(error, '관리자 목록을 불러오는데 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

// 관리자 추가
const addAdmin = async () => {
  if (!newAdmin.value.idAdmin || !newAdmin.value.nameAdmin || !newAdmin.value.pwAdmin) {
    await swAlert('필수 정보를 모두 입력해주세요.', 'warning')
    return
  }

  if (!idValidation.value.isValid) {
    await swAlert('ID 중복확인을 완료해주세요.', 'warning')
    return
  }

  try {
    isLoading.value = true
    const response = await adminApi.register(newAdmin.value)

    await swAlert('관리자가 성공적으로 추가되었습니다.', 'success')
    closeAddModal()
    await fetchAdminList()
  } catch (error) {
    await handleApiError(error, '관리자 추가에 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

// 관리자 계정 수정 (디스코드 ID 및 비밀번호 수정, 비밀번호 검증 포함)
const updateAdmin = async () => {
  if (!editPassword.value) {
    await swAlert('현재 비밀번호를 입력해주세요.', 'warning')
    return
  }

  // 변경된 값 확인
  const newDiscordValue = editingAdmin.value.dcAdmin ? editingAdmin.value.dcAdmin.trim() : ''
  const originalDiscordValue = originalAdmin.value.dcAdmin || ''
  const discordChanged = newDiscordValue !== originalDiscordValue
  const passwordChanged = editNewPassword.value && editNewPassword.value.trim() !== ''

  // 디스코드 ID와 비밀번호 둘 다 변경하지 않는 경우
  if (!discordChanged && !passwordChanged) {
    await swAlert('디스코드 ID 또는 비밀번호 중 하나는 변경해야 합니다.', 'warning')
    return
  }

  try {
    isLoading.value = true
    
    // 통합 수정 API 호출
    const updateData = {
      idAdmin: editingAdmin.value.idAdmin,
      currentPassword: editPassword.value,
      newPassword: passwordChanged ? editNewPassword.value.trim() : null,
      newDiscord: discordChanged ? newDiscordValue : null
    }
    
    const response = await adminApi.update(updateData)
    
    const updatedFields = []
    if (updateData.newDiscord !== null) updatedFields.push('디스코드 ID')
    if (updateData.newPassword !== null) updatedFields.push('비밀번호')
    
    await swAlert(`${updatedFields.join(' 및 ')}가 성공적으로 수정되었습니다.`, 'success')
    closeEditModal()
    await fetchAdminList()
  } catch (error) {
    await handleApiError(error, '관리자 수정에 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

// 관리자 삭제 확인 모달 열기
const confirmDeleteAdmin = (admin) => {
  deletingAdmin.value = {
    idAdmin: admin.idAdmin,
    nameAdmin: admin.nameAdmin,
    dcAdmin: admin.dcAdmin || ''
  }
  showDeleteModal.value = true
}

// 관리자 삭제 (비밀번호 검증 포함)
const deleteAdmin = async (idAdmin) => {
  if (!deletePassword.value) {
    await swAlert('현재 비밀번호를 입력해주세요.', 'warning')
    return
  }

  try {
    isLoading.value = true

    await validatePassword(idAdmin, deletePassword.value)

    const response = await adminApi.remove(deletingAdmin.value.idAdmin)

    await swAlert('관리자가 성공적으로 삭제되었습니다.', 'success')
    closeDeleteModal()
    await fetchAdminList()

  } catch (error) {
    await handleApiError(error, '관리자 삭제에 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

// 모달 관련 함수들
const closeAddModal = () => {
  showAddModal.value = false
  newAdmin.value = {
    idAdmin: '',
    pwAdmin: '',
    nameAdmin: '',
    dcAdmin: '',
    seqCampus: null
  }
  idValidation.value = {
    isValid: false,
    message: '',
    checked: false
  }
}

const openEditModal = (admin) => {
  editingAdmin.value = {
    idAdmin: admin.idAdmin,
    nameAdmin: admin.nameAdmin,
    dcAdmin: admin.dcAdmin || ''
  }
  // 원본 값 저장 (변경 여부 확인용)
  originalAdmin.value = {
    idAdmin: admin.idAdmin,
    nameAdmin: admin.nameAdmin,
    dcAdmin: admin.dcAdmin || ''
  }
  showEditModal.value = true
}

const closeEditModal = () => {
  showEditModal.value = false
  editPassword.value = ''
  editNewPassword.value = ''
  editingAdmin.value = {
    idAdmin: '',
    nameAdmin: '',
    dcAdmin: ''
  }
  originalAdmin.value = {
    idAdmin: '',
    nameAdmin: '',
    dcAdmin: ''
  }
}

const closeDeleteModal = () => {
  showDeleteModal.value = false
  deletePassword.value = ''
  deletingAdmin.value = {
    idAdmin: '',
    nameAdmin: '',
    dcAdmin: ''
  }
}

// 날짜 포맷팅

const exportData = async () => {
  try {
    const campusId = showCampusFilter.value && selectedCampus.value ? selectedCampus.value : null
    const res = await adminApi.exportExcel(campusId)
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = '관리자계정.xlsx'
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('엑셀 내보내기 실패:', e)
  }
}

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
  await fetchCurrentUser()
  await fetchCampusList()
  await fetchAdminList()
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
/* ─── 기본 ─── */
.admin-account-management {
  max-width: 100%;
  font-size: 13px;
  color: var(--pb-color-text);
}

/* ─── 섹션 헤더 ─── */
.section-header { margin-bottom: 18px; }

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--pb-color-heading);
  margin: 0 0 3px;
}

.section-description {
  font-size: 13px;
  color: var(--pb-color-text-muted);
  margin: 0;
}

/* ─── 통계 카드 ─── */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-md);
  box-shadow: var(--pb-shadow-xs);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: var(--pb-color-brand-soft);
  border-radius: var(--pb-radius-md);
  color: var(--pb-color-brand);
  flex-shrink: 0;
}

.stat-number {
  font-size: 22px;
  font-weight: 700;
  line-height: 1;
  color: var(--pb-color-heading);
}

.stat-label {
  font-size: 12px;
  color: var(--pb-color-text-muted);
  margin-top: 2px;
}

/* ─── 액션 바 ─── */
.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-bottom: 14px;
}

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

.add-admin-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  padding: 0 14px;
  background: var(--pb-color-brand);
  color: #fff;
  border: none;
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
  white-space: nowrap;
}
.add-admin-btn:hover { background: var(--pb-color-brand-strong); }

/* ─── 필터 섹션 ─── */
.filter-section {
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-md);
  padding: 14px 16px;
  margin-bottom: 14px;
  box-shadow: var(--pb-shadow-xs);
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.filter-label {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--pb-color-text-soft);
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
  transition: border-color 0.15s, box-shadow 0.15s;
}
.filter-select:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px var(--pb-color-brand-soft);
}

/* ─── 테이블 컨테이너 ─── */
.admin-table-container {
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-lg);
  overflow: hidden;
  box-shadow: var(--pb-shadow-xs);
}

.table-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface-subtle);
}

.table-header h3 {
  font-size: 13px;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0;
}

.table-wrapper { overflow-x: auto; }

/* ─── 테이블 ─── */
.admin-table {
  width: 100%;
  border-collapse: collapse;
}

.admin-table th {
  text-align: left;
  padding: 9px 16px;
  background: var(--pb-color-surface-muted);
  border-bottom: 1px solid var(--pb-color-border);
  color: var(--pb-color-text-soft);
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  white-space: nowrap;
}

.admin-table td {
  padding: 11px 16px;
  border-bottom: 1px solid var(--pb-color-border);
  color: var(--pb-color-text);
  font-size: 13px;
  vertical-align: middle;
}

.admin-row:hover { background: var(--pb-color-brand-soft); }

.admin-name {
  font-weight: 500;
  color: var(--pb-color-heading);
}

/* ─── 테이블 액션 ─── */
.admin-actions { display: flex; gap: 5px; }

.edit-btn,
.delete-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: var(--pb-radius-sm);
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s, color 0.15s;
}

.edit-btn {
  border: 1px solid var(--pb-color-border);
  background: var(--pb-color-brand-soft);
  color: var(--pb-color-brand);
}
.edit-btn:hover {
  background: var(--pb-color-brand);
  border-color: var(--pb-color-brand);
  color: #fff;
}

.delete-btn {
  border: 1px solid rgba(217, 48, 37, 0.2);
  background: var(--pb-color-danger-soft);
  color: var(--pb-color-danger);
}
.delete-btn:hover {
  background: var(--pb-color-danger);
  border-color: var(--pb-color-danger);
  color: #fff;
}

/* ─── 모달 오버레이 ─── */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(30, 31, 29, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

/* ─── 모달 패널 ─── */
.modal-content {
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-lg);
  width: 90%;
  max-width: 480px;
  box-shadow: 0 8px 32px rgba(30, 31, 29, 0.14);
  overflow: hidden;
}

/* ─── 모달 헤더 ─── */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface-subtle);
}

.modal-header h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0;
}

.modal-close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface);
  border-radius: var(--pb-radius-sm);
  color: var(--pb-color-text-soft);
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}
.modal-close:hover {
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text);
}

/* ─── 모달 폼 ─── */
.modal-form { padding: 16px 20px 20px; }

.form-group { margin-bottom: 14px; }

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-size: 12px;
  font-weight: 600;
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
  transition: border-color 0.15s, box-shadow 0.15s;
}
.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px var(--pb-color-brand-soft);
}

.form-select { cursor: pointer; }

.disabled-input {
  background: var(--pb-color-surface-muted) !important;
  color: var(--pb-color-text-soft) !important;
  cursor: not-allowed !important;
}

/* ID 입력 그룹 */
.id-input-group {
  display: flex;
  gap: 7px;
  align-items: stretch;
}
.id-input-group input { flex: 1; }

.validate-btn {
  height: 34px;
  padding: 0 12px;
  background: var(--pb-color-brand);
  color: #fff;
  border: none;
  border-radius: var(--pb-radius-sm);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
  white-space: nowrap;
}
.validate-btn:hover:not(:disabled) { background: var(--pb-color-brand-strong); }
.validate-btn:disabled { opacity: 0.45; cursor: not-allowed; }

/* 검증 메시지 */
.validation-message {
  margin-top: 5px;
  font-size: 12px;
  font-weight: 500;
}
.validation-message.valid  { color: var(--pb-color-success); }
.validation-message.invalid{ color: var(--pb-color-danger); }

.required-mark { color: var(--pb-color-danger); font-weight: 600; }

.form-hint {
  margin-top: 4px;
  font-size: 12px;
  color: var(--pb-color-text-soft);
}

/* ─── 모달 액션 ─── */
.modal-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px solid var(--pb-color-border);
}

.cancel-btn,
.submit-btn,
.delete-confirm-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 32px;
  padding: 0 16px;
  border-radius: var(--pb-radius-sm);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
  white-space: nowrap;
}

.cancel-btn {
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  color: var(--pb-color-text);
}
.cancel-btn:hover { background: var(--pb-color-surface-muted); }

.submit-btn {
  background: var(--pb-color-brand);
  border: 1px solid var(--pb-color-brand);
  color: #fff;
}
.submit-btn:hover:not(:disabled) { background: var(--pb-color-brand-strong); border-color: var(--pb-color-brand-strong); }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.delete-confirm-btn {
  background: var(--pb-color-danger);
  border: 1px solid var(--pb-color-danger);
  color: #fff;
}
.delete-confirm-btn:hover:not(:disabled) { filter: brightness(0.9); }
.delete-confirm-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* ─── 삭제 경고 ─── */
.delete-warning {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 20px 24px;
  text-align: center;
}

.warning-icon {
  color: var(--pb-color-danger);
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.warning-icon svg { width: 48px; height: 48px; flex-shrink: 0; }

.warning-content h4 {
  font-size: 15px;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0 0 6px;
}

.warning-content p {
  font-size: 13px;
  color: var(--pb-color-text-muted);
  margin: 0 0 14px;
  line-height: 1.55;
}

.admin-info {
  padding: 8px 14px;
  background: var(--pb-color-danger-soft);
  border: 1px solid rgba(217, 48, 37, 0.3);
  border-radius: var(--pb-radius-sm);
  color: var(--pb-color-danger);
  font-size: 13px;
  font-weight: 500;
}


/* ─── 반응형 ─── */
@media (max-width: 768px) {
  .stats-grid { grid-template-columns: 1fr; }
  .action-bar { justify-content: center; flex-wrap: wrap; }

  .admin-table th,
  .admin-table td { padding: 10px 12px; }

  .modal-content { width: 95%; margin: 16px; }
  .modal-form { padding: 14px 16px 18px; }
  .modal-actions { flex-direction: column; padding-top: 12px; }
  .cancel-btn, .submit-btn, .delete-confirm-btn { width: 100%; justify-content: center; }
  .id-input-group { flex-direction: column; }
  .validate-btn { width: 100%; }
  .export-btn, .add-admin-btn { width: 100%; justify-content: center; }
}

@media (max-width: 480px) {
  .admin-table { font-size: 12px; }
  .admin-actions { flex-direction: column; gap: 3px; }
  .edit-btn, .delete-btn { width: 26px; height: 26px; }
}
</style>