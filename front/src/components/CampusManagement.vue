<template>
  <div class="campus-management">
    <div class="section-header">
      <h2 class="section-title">캠퍼스 관리</h2>
      <p class="section-description">캠퍼스를 추가하고 관리할 수 있습니다.</p>
    </div>

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

    <!-- 액션 바 -->
    <div class="action-bar">
      <button class="add-btn" @click="showAddModal = true">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
          <line x1="12" y1="8" x2="12" y2="16" stroke="currentColor" stroke-width="2"/>
          <line x1="8" y1="12" x2="16" y2="12" stroke="currentColor" stroke-width="2"/>
        </svg>
        캠퍼스 추가
      </button>
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
              <th>작업</th>
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
              <td class="campus-actions">
                <button class="edit-btn" @click="openEditModal(campus)">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M11 4H4C3.46957 4 2.96086 4.21071 2.58579 4.58579C2.21071 4.96086 2 5.46957 2 6V20C2 20.5304 2.21071 21.0391 2.58579 21.4142C2.96086 21.7893 3.46957 22 4 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V13" stroke="currentColor" stroke-width="2"/>
                    <path d="M18.5 2.5C18.8978 2.10217 19.4374 1.87868 20 1.87868C20.5626 1.87868 21.1022 2.10217 21.5 2.5C21.8978 2.89782 22.1213 3.43739 22.1213 4C22.1213 4.56261 21.8978 5.10217 21.5 5.5L12 15L8 16L9 12L18.5 2.5Z" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </button>
                <button class="delete-btn" @click="confirmDelete(campus)">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <polyline points="3,6 5,6 21,6" stroke="currentColor" stroke-width="2"/>
                    <path d="M19,6V20C19,20.5304 18.7893,21.0391 18.4142,21.4142C18.0391,21.7893 17.5304,22 17,22H7C6.46957,22 5.96086,21.7893 5.58579,21.4142C5.21071,21.0391 5,20.5304 5,20V6M8,6V4C8,3.46957 8.21071,2.96086 8.58579,2.58579C8.96086,2.21071 9.46957,2 10,2H14C14.5304,2 15.0391,2.21071 15.4142,2.58579C15.7893,2.96086 16,3.46957 16,4V6" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 추가 모달 -->
    <div v-if="showAddModal" class="modal-overlay" @click="closeAddModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>캠퍼스 추가</h3>
          <button class="modal-close" @click="closeAddModal">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none"><line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2"/><line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2"/></svg>
          </button>
        </div>
        <form @submit.prevent="addCampus" class="modal-form">
          <div class="form-group">
            <label>캠퍼스명 <span class="required-mark">*</span></label>
            <input type="text" v-model="newCampus.nameCampus" required placeholder="캠퍼스명을 입력하세요" />
          </div>
          <div class="form-group">
            <label>위치 <span class="required-mark">*</span></label>
            <input type="text" v-model="newCampus.locationCampus" required placeholder="위치를 입력하세요" />
          </div>
          <div class="form-group">
            <label>상태</label>
            <select v-model="newCampus.isActive" class="form-select">
              <option :value="true">활성</option>
              <option :value="false">비활성</option>
            </select>
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="closeAddModal">취소</button>
            <button type="submit" class="submit-btn" :disabled="isLoading">{{ isLoading ? '추가 중...' : '추가' }}</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 수정 모달 -->
    <div v-if="showEditModal" class="modal-overlay" @click="closeEditModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>캠퍼스 수정</h3>
          <button class="modal-close" @click="closeEditModal">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none"><line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2"/><line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2"/></svg>
          </button>
        </div>
        <form @submit.prevent="updateCampus" class="modal-form">
          <div class="form-group">
            <label>캠퍼스명 <span class="required-mark">*</span></label>
            <input type="text" v-model="editingCampus.nameCampus" required placeholder="캠퍼스명을 입력하세요" />
          </div>
          <div class="form-group">
            <label>위치 <span class="required-mark">*</span></label>
            <input type="text" v-model="editingCampus.locationCampus" required placeholder="위치를 입력하세요" />
          </div>
          <div class="form-group">
            <label>상태</label>
            <select v-model="editingCampus.isActive" class="form-select">
              <option :value="true">활성</option>
              <option :value="false">비활성</option>
            </select>
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="closeEditModal">취소</button>
            <button type="submit" class="submit-btn" :disabled="isLoading">{{ isLoading ? '수정 중...' : '수정' }}</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 삭제 확인 모달 -->
    <div v-if="showDeleteModal" class="modal-overlay" @click="closeDeleteModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>캠퍼스 삭제 확인</h3>
          <button class="modal-close" @click="closeDeleteModal">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none"><line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2"/><line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2"/></svg>
          </button>
        </div>
        <div class="delete-warning">
          <div class="warning-icon">
            <svg width="46" height="46" viewBox="0 0 24 24" fill="none"><path d="M12 8V12M12 16H12.01M10.29 3.86L1.82 18C1.64466 18.3024 1.55685 18.6453 1.56455 18.9928C1.57225 19.3403 1.67516 19.6792 1.86244 19.9757C2.04973 20.2723 2.31561 20.5157 2.6289 20.6812C2.9422 20.8467 3.29427 20.9286 3.65 20.92H20.35C20.7057 20.9286 21.0578 20.8467 21.3711 20.6812C21.6844 20.5157 21.9503 20.2723 22.1376 19.9757C22.3248 19.6792 22.4278 19.3403 22.4355 18.9928C22.4432 18.6453 22.3553 18.3024 22.18 18L13.71 3.86C13.5317 3.56611 13.2807 3.32312 12.9812 3.15446C12.6817 2.98581 12.3438 2.89725 12 2.89725C11.6562 2.89725 11.3183 2.98581 11.0188 3.15446C10.7193 3.32312 10.4683 3.56611 10.29 3.86Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </div>
          <div class="warning-content">
            <h4>정말로 캠퍼스를 삭제하시겠습니까?</h4>
            <p>이 작업은 되돌릴 수 없습니다.</p>
            <div class="campus-info">
              <strong>삭제할 캠퍼스: {{ deletingCampus.nameCampus }}</strong>
            </div>
          </div>
        </div>
        <div class="modal-actions delete-modal-actions">
          <button type="button" class="cancel-btn" @click="closeDeleteModal">취소</button>
          <button type="button" class="delete-confirm-btn" @click="deleteCampus" :disabled="isLoading">
            {{ isLoading ? '삭제 중...' : '삭제' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import * as campusApi from '@/api/campus'
import { swAlert } from '@/utils/sweetAlert'
import { handleApiError } from '@/utils/apiErrorHandler'

const campusList = ref([])
const isLoading = ref(false)

const showAddModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)

const newCampus = ref({ nameCampus: '', locationCampus: '', isActive: true })
const editingCampus = ref({ seqCampus: null, nameCampus: '', locationCampus: '', isActive: true })
const deletingCampus = ref({ seqCampus: null, nameCampus: '' })

const activeCampusCount = computed(() => campusList.value.filter(c => c.isActive).length)

const handleKeydown = (e) => {
  if (e.key === 'Escape') {
    if (showAddModal.value) closeAddModal()
    if (showEditModal.value) closeEditModal()
    if (showDeleteModal.value) closeDeleteModal()
  }
}

const fetchCampusList = async () => {
  try {
    isLoading.value = true
    const res = await campusApi.getAllIncludeInactive()
    campusList.value = res.data.data
  } catch (error) {
    await handleApiError(error, '캠퍼스 목록을 불러오는데 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

const addCampus = async () => {
  try {
    isLoading.value = true
    await campusApi.create({
      nameCampus: newCampus.value.nameCampus,
      locationCampus: newCampus.value.locationCampus,
      isActive: newCampus.value.isActive
    })
    await swAlert('캠퍼스가 추가되었습니다.', 'success')
    closeAddModal()
    await fetchCampusList()
  } catch (error) {
    await handleApiError(error, '캠퍼스 추가에 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

const openEditModal = (campus) => {
  editingCampus.value = {
    seqCampus: campus.seqCampus,
    nameCampus: campus.nameCampus,
    locationCampus: campus.locationCampus,
    isActive: campus.isActive
  }
  showEditModal.value = true
}

const updateCampus = async () => {
  try {
    isLoading.value = true
    await campusApi.update(editingCampus.value.seqCampus, {
      nameCampus: editingCampus.value.nameCampus,
      locationCampus: editingCampus.value.locationCampus,
      isActive: editingCampus.value.isActive
    })
    await swAlert('캠퍼스가 수정되었습니다.', 'success')
    closeEditModal()
    await fetchCampusList()
  } catch (error) {
    await handleApiError(error, '캠퍼스 수정에 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

const confirmDelete = (campus) => {
  deletingCampus.value = { seqCampus: campus.seqCampus, nameCampus: campus.nameCampus }
  showDeleteModal.value = true
}

const deleteCampus = async () => {
  try {
    isLoading.value = true
    await campusApi.remove(deletingCampus.value.seqCampus)
    await swAlert('캠퍼스가 삭제되었습니다.', 'success')
    closeDeleteModal()
    await fetchCampusList()
  } catch (error) {
    await handleApiError(error, '캠퍼스 삭제에 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

const closeAddModal = () => {
  showAddModal.value = false
  newCampus.value = { nameCampus: '', locationCampus: '', isActive: true }
}

const closeEditModal = () => {
  showEditModal.value = false
  editingCampus.value = { seqCampus: null, nameCampus: '', locationCampus: '', isActive: true }
}

const closeDeleteModal = () => {
  showDeleteModal.value = false
  deletingCampus.value = { seqCampus: null, nameCampus: '' }
}

onMounted(async () => {
  await fetchCampusList()
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.campus-management { max-width: 100%; font-size: 13px; color: var(--pb-color-text); }

.section-header { margin-bottom: 20px; }
.section-title { font-size: 15px; font-weight: 700; color: var(--pb-color-heading); margin: 0 0 3px; }
.section-description { font-size: 13px; color: var(--pb-color-text-muted); margin: 0; }

.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px; margin-bottom: 20px; }
.stat-card { display: flex; align-items: center; gap: 14px; padding: 16px 18px; background: var(--pb-color-surface); border: 1px solid var(--pb-color-border); border-radius: var(--pb-radius-lg); box-shadow: var(--pb-shadow-xs); }
.stat-icon { display: flex; align-items: center; justify-content: center; width: 36px; height: 36px; background: var(--pb-color-accent-soft); border-radius: var(--pb-radius-md); color: var(--pb-color-accent); flex-shrink: 0; }
.stat-number { font-size: 24px; font-weight: 700; line-height: 1; color: var(--pb-color-heading); }
.stat-label { font-size: 12px; color: var(--pb-color-text-soft); margin-top: 2px; }

.action-bar { display: flex; justify-content: flex-end; margin-bottom: 16px; }

.add-btn { display: flex; align-items: center; gap: 6px; height: 34px; padding: 0 16px; background: var(--pb-color-brand); color: var(--pb-color-surface); border: none; border-radius: var(--pb-radius-sm); font-size: 13px; font-weight: 600; cursor: pointer; transition: background 0.12s; }
.add-btn:hover { background: var(--pb-color-brand-strong); }

.table-container { background: var(--pb-color-surface); border: 1px solid var(--pb-color-border); border-radius: var(--pb-radius-lg); box-shadow: var(--pb-shadow-sm); overflow: hidden; }
.table-header { padding: 13px 20px; border-bottom: 1px solid var(--pb-color-border); background: var(--pb-color-surface-muted); }
.table-header h3 { font-size: 13px; font-weight: 600; color: var(--pb-color-heading); margin: 0; }
.table-wrapper { overflow-x: auto; }

.data-table { width: 100%; border-collapse: collapse; }
.data-table th { text-align: left; padding: 9px 16px; background: var(--pb-color-surface-subtle); color: var(--pb-color-text-soft); font-weight: 600; font-size: 11px; text-transform: uppercase; letter-spacing: 0.05em; border-bottom: 1px solid var(--pb-color-border); }
.data-table td { padding: 10px 16px; border-bottom: 1px solid var(--pb-color-border); color: var(--pb-color-text); font-size: 13px; vertical-align: middle; }
.data-row:hover td { background: var(--pb-color-surface-muted); }
.campus-name { font-weight: 500; color: var(--pb-color-heading); }

.status-badge { display: inline-block; padding: 2px 8px; border-radius: var(--pb-radius-xs); font-size: 11px; font-weight: 600; }
.status-badge.active { background: var(--pb-color-success-soft); color: var(--pb-color-success); }
.status-badge.inactive { background: var(--pb-color-surface-muted); color: var(--pb-color-text-muted); }

.campus-actions { display: flex; gap: 4px; }
.edit-btn, .delete-btn { display: flex; align-items: center; justify-content: center; width: 30px; height: 30px; border: 1px solid var(--pb-color-border); border-radius: var(--pb-radius-sm); cursor: pointer; transition: background 0.12s, border-color 0.12s, color 0.12s; }
.edit-btn { background: var(--pb-color-surface); color: var(--pb-color-text-muted); }
.edit-btn:hover { background: var(--pb-color-surface-muted); color: var(--pb-color-text); border-color: var(--pb-color-border-strong); }
.delete-btn { background: var(--pb-color-danger-soft); color: var(--pb-color-danger); }
.delete-btn:hover:not(:disabled) { background: var(--pb-color-danger); color: var(--pb-color-surface); border-color: var(--pb-color-danger); }
.delete-btn:disabled { opacity: 0.45; cursor: not-allowed; }

.modal-overlay { position: fixed; inset: 0; background: rgba(30, 31, 29, 0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-content { background: var(--pb-color-surface); border: 1px solid var(--pb-color-border); border-radius: var(--pb-radius-lg); width: 90%; max-width: 460px; box-shadow: var(--pb-shadow-popover); max-height: 90vh; overflow-y: auto; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 14px 20px; border-bottom: 1px solid var(--pb-color-border); }
.modal-header h3 { font-size: 14px; font-weight: 600; color: var(--pb-color-heading); margin: 0; }
.modal-close { display: flex; align-items: center; justify-content: center; width: 30px; height: 30px; border: 1px solid var(--pb-color-border); background: var(--pb-color-surface-muted); border-radius: var(--pb-radius-sm); color: var(--pb-color-text-soft); cursor: pointer; transition: background 0.12s; }
.modal-close:hover { background: var(--pb-color-surface-subtle); color: var(--pb-color-text); }
.modal-form { padding: 16px 20px 20px; }
.form-group { margin-bottom: 14px; }
.form-group label { display: block; margin-bottom: 5px; font-size: 12px; font-weight: 500; color: var(--pb-color-text-muted); }
.form-group input, .form-group select { width: 100%; height: 34px; padding: 0 12px; border: 1px solid var(--pb-color-border); border-radius: var(--pb-radius-sm); font-size: 13px; background: var(--pb-color-surface); color: var(--pb-color-text); box-sizing: border-box; transition: border-color 0.15s; }
.form-group input:focus, .form-group select:focus { outline: none; border-color: var(--pb-color-brand); box-shadow: 0 0 0 3px var(--pb-color-brand-muted); }
.form-select { cursor: pointer; }
.required-mark { color: var(--pb-color-danger); font-weight: 600; }
.modal-actions { display: flex; gap: 8px; justify-content: flex-end; padding: 12px 20px 20px; }
.delete-modal-actions { padding: 0 20px 20px; }
.cancel-btn, .submit-btn, .delete-confirm-btn { height: 32px; padding: 0 16px; border: none; border-radius: var(--pb-radius-sm); font-size: 13px; font-weight: 600; cursor: pointer; transition: background 0.12s; }
.cancel-btn { background: var(--pb-color-surface-muted); border: 1px solid var(--pb-color-border); color: var(--pb-color-text); }
.cancel-btn:hover { background: var(--pb-color-border); }
.submit-btn { background: var(--pb-color-brand); color: var(--pb-color-surface); }
.submit-btn:hover:not(:disabled) { background: var(--pb-color-brand-strong); }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.delete-confirm-btn { background: var(--pb-color-danger); color: var(--pb-color-surface); }
.delete-confirm-btn:hover:not(:disabled) { background: var(--pb-color-danger-strong); }
.delete-confirm-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.delete-warning { display: flex; flex-direction: column; align-items: center; padding: 22px 20px; text-align: center; }
.warning-icon { color: var(--pb-color-danger); margin-bottom: 12px; }
.warning-content h4 { font-size: 14px; font-weight: 600; color: var(--pb-color-heading); margin: 0 0 6px; }
.warning-content p { font-size: 13px; color: var(--pb-color-text-muted); margin: 0 0 14px; line-height: 1.5; }
.campus-info { padding: 8px 12px; background: var(--pb-color-danger-soft); border: 1px solid var(--pb-color-danger); border-radius: var(--pb-radius-sm); color: var(--pb-color-danger); font-size: 12px; }

@media (max-width: 768px) {
  .stats-grid { grid-template-columns: 1fr; }
  .data-table th, .data-table td { padding: 9px 12px; }
  .modal-content { width: 95%; }
  .modal-actions { flex-direction: column; }
  .cancel-btn, .submit-btn, .delete-confirm-btn { width: 100%; }
}
</style>
