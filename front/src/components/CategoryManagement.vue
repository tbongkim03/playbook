<template>
  <div class="category-management">
    <div class="pb-page-head">
      <div>
        <h2>카테고리 관리</h2>
        <p>도서 대분류·중분류를 추가하고 관리할 수 있습니다.</p>
      </div>
    </div>

    <!-- 통계 카드 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">
          <PhTextAlignLeft weight="duotone" :size="24" />
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ firstList.length }}</div>
          <div class="stat-label">총 대분류</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <PhClipboardText weight="duotone" :size="24" />
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ secondList.length }}</div>
          <div class="stat-label">총 중분류</div>
        </div>
      </div>
    </div>

    <!-- 대분류 / 중분류 섹션 탭 -->
    <div class="sub-tabs">
      <button :class="['sub-tab', { active: activeSection === 'first' }]" @click="activeSection = 'first'">대분류</button>
      <button :class="['sub-tab', { active: activeSection === 'second' }]" @click="activeSection = 'second'">중분류</button>
    </div>

    <!-- 대분류 섹션 -->
    <template v-if="activeSection === 'first'">
      <div class="table-container">
        <div class="table-header pb-list-head">
        <h3>대분류 목록</h3>
        <div class="pb-list-actions">
          <button type="button" class="pb-btn pb-btn-primary" @click="showAddFirstModal = true">
            <PhPlusCircle weight="duotone" :size="18" />
            대분류 추가
          </button>
        </div>
      </div>
        <div class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>한글명</th>
                <th>영문명</th>
                <th>작업</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in firstList" :key="item.seqSortFirst" class="data-row">
                <td class="item-name">{{ item.korSortFirst }}</td>
                <td class="item-eng">{{ item.nameSortFirst }}</td>
                <td class="item-actions">
                  <button class="edit-btn" @click="openEditFirstModal(item)">
                    <PhNotePencil weight="duotone" :size="16" />
                  </button>
                  <button class="delete-btn" @click="confirmDeleteFirst(item)">
                    <PhTrash weight="duotone" :size="16" />
                  </button>
                </td>
              </tr>
              <tr v-if="firstList.length === 0">
                <td colspan="3" class="empty-row">등록된 대분류가 없습니다.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>

    <!-- 중분류 섹션 -->
    <template v-if="activeSection === 'second'">
      <div class="table-container">
        <div class="table-header pb-list-head">
        <h3>중분류 목록</h3>
        <div class="pb-list-actions">
          <button type="button" class="pb-btn pb-btn-primary" @click="showAddSecondModal = true">
            <PhPlusCircle weight="duotone" :size="18" />
            중분류 추가
          </button>
        </div>
      </div>
        <div class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>대분류</th>
                <th>한글명</th>
                <th>영문명</th>
                <th>작업</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in secondList" :key="item.seqSortSecond" class="data-row">
                <td class="item-parent">{{ getFirstName(item.seqSortFirst) }}</td>
                <td class="item-name">{{ item.korSortSecond }}</td>
                <td class="item-eng">{{ item.nameSortSecond }}</td>
                <td class="item-actions">
                  <button class="edit-btn" @click="openEditSecondModal(item)">
                    <PhNotePencil weight="duotone" :size="16" />
                  </button>
                  <button class="delete-btn" @click="confirmDeleteSecond(item)">
                    <PhTrash weight="duotone" :size="16" />
                  </button>
                </td>
              </tr>
              <tr v-if="secondList.length === 0">
                <td colspan="4" class="empty-row">등록된 중분류가 없습니다.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>

    <!-- 대분류 추가 모달 -->
    <div v-if="showAddFirstModal" class="modal-overlay" v-modal-backdrop="closeAddFirstModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>대분류 추가</h3>
          <button class="modal-close" @click="closeAddFirstModal">
            <PhX weight="duotone" :size="24" />
          </button>
        </div>
        <form @submit.prevent="addFirst" class="modal-form">
          <div class="form-group">
            <label>한글명 <span class="required-mark">*</span></label>
            <input type="text" v-model="newFirst.korSortFirst" required placeholder="예) 컴퓨터·IT" />
          </div>
          <div class="form-group">
            <label>영문명 <span class="required-mark">*</span></label>
            <input type="text" v-model="newFirst.nameSortFirst" required placeholder="예) computer-it" />
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="closeAddFirstModal">취소</button>
            <button type="submit" class="submit-btn" :disabled="isLoading">{{ isLoading ? '추가 중...' : '추가' }}</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 대분류 수정 모달 -->
    <div v-if="showEditFirstModal" class="modal-overlay" v-modal-backdrop="closeEditFirstModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>대분류 수정</h3>
          <button class="modal-close" @click="closeEditFirstModal">
            <PhX weight="duotone" :size="24" />
          </button>
        </div>
        <form @submit.prevent="updateFirst" class="modal-form">
          <div class="form-group">
            <label>한글명 <span class="required-mark">*</span></label>
            <input type="text" v-model="editingFirst.korSortFirst" required />
          </div>
          <div class="form-group">
            <label>영문명 <span class="required-mark">*</span></label>
            <input type="text" v-model="editingFirst.nameSortFirst" required />
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="closeEditFirstModal">취소</button>
            <button type="submit" class="submit-btn" :disabled="isLoading">{{ isLoading ? '수정 중...' : '수정' }}</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 중분류 추가 모달 -->
    <div v-if="showAddSecondModal" class="modal-overlay" v-modal-backdrop="closeAddSecondModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>중분류 추가</h3>
          <button class="modal-close" @click="closeAddSecondModal">
            <PhX weight="duotone" :size="24" />
          </button>
        </div>
        <form @submit.prevent="addSecond" class="modal-form">
          <div class="form-group">
            <label>대분류 <span class="required-mark">*</span></label>
            <select v-model="newSecond.seqSortFirst" required class="form-select">
              <option value="" disabled>대분류를 선택하세요</option>
              <option v-for="f in firstList" :key="f.seqSortFirst" :value="f.seqSortFirst">{{ f.korSortFirst }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>한글명 <span class="required-mark">*</span></label>
            <input type="text" v-model="newSecond.korSortSecond" required placeholder="예) 프로그래밍 언어" />
          </div>
          <div class="form-group">
            <label>영문명 <span class="required-mark">*</span></label>
            <input type="text" v-model="newSecond.nameSortSecond" required placeholder="예) programming-language" />
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="closeAddSecondModal">취소</button>
            <button type="submit" class="submit-btn" :disabled="isLoading">{{ isLoading ? '추가 중...' : '추가' }}</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 중분류 수정 모달 -->
    <div v-if="showEditSecondModal" class="modal-overlay" v-modal-backdrop="closeEditSecondModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>중분류 수정</h3>
          <button class="modal-close" @click="closeEditSecondModal">
            <PhX weight="duotone" :size="24" />
          </button>
        </div>
        <form @submit.prevent="updateSecond" class="modal-form">
          <div class="form-group">
            <label>대분류 <span class="required-mark">*</span></label>
            <select v-model="editingSecond.seqSortFirst" required class="form-select">
              <option v-for="f in firstList" :key="f.seqSortFirst" :value="f.seqSortFirst">{{ f.korSortFirst }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>한글명 <span class="required-mark">*</span></label>
            <input type="text" v-model="editingSecond.korSortSecond" required />
          </div>
          <div class="form-group">
            <label>영문명 <span class="required-mark">*</span></label>
            <input type="text" v-model="editingSecond.nameSortSecond" required />
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="closeEditSecondModal">취소</button>
            <button type="submit" class="submit-btn" :disabled="isLoading">{{ isLoading ? '수정 중...' : '수정' }}</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 삭제 확인 모달 (공통) -->
    <div v-if="showDeleteModal" class="modal-overlay" v-modal-backdrop="closeDeleteModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ deletingItem.type === 'first' ? '대분류' : '중분류' }} 삭제 확인</h3>
          <button class="modal-close" @click="closeDeleteModal">
            <PhX weight="duotone" :size="24" />
          </button>
        </div>
        <div class="delete-warning">
          <div class="warning-icon">
            <PhWarning weight="duotone" :size="46" />
          </div>
          <div class="warning-content">
            <h4>정말로 삭제하시겠습니까?</h4>
            <p>이 작업은 되돌릴 수 없습니다.</p>
            <div class="item-info">
              <strong>삭제할 항목: {{ deletingItem.name }}</strong>
            </div>
          </div>
        </div>
        <div class="modal-actions delete-modal-actions">
          <button type="button" class="cancel-btn" @click="closeDeleteModal">취소</button>
          <button type="button" class="delete-confirm-btn" @click="executeDelete" :disabled="isLoading">
            {{ isLoading ? '삭제 중...' : '삭제' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { vModalBackdrop } from '@/utils/modalBackdrop'
import { PhClipboardText, PhNotePencil, PhPlusCircle, PhTextAlignLeft, PhTrash, PhWarning, PhX } from '@phosphor-icons/vue'
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as sortApi from '@/api/sort'
import { swAlert } from '@/utils/sweetAlert'
import { handleApiError } from '@/utils/apiErrorHandler'

const firstList = ref([])
const secondList = ref([])
const isLoading = ref(false)
const activeSection = ref('first')

const showAddFirstModal = ref(false)
const showEditFirstModal = ref(false)
const showAddSecondModal = ref(false)
const showEditSecondModal = ref(false)
const showDeleteModal = ref(false)

const newFirst = ref({ korSortFirst: '', nameSortFirst: '' })
const editingFirst = ref({ seqSortFirst: null, korSortFirst: '', nameSortFirst: '' })

const newSecond = ref({ seqSortFirst: '', korSortSecond: '', nameSortSecond: '' })
const editingSecond = ref({ seqSortSecond: null, seqSortFirst: null, korSortSecond: '', nameSortSecond: '' })

const deletingItem = ref({ type: '', id: null, name: '' })

const getFirstName = (seqSortFirst) => {
  const found = firstList.value.find(f => f.seqSortFirst === seqSortFirst)
  return found ? found.korSortFirst : '-'
}

const handleKeydown = (e) => {
  if (e.key !== 'Escape') return
  if (showAddFirstModal.value) closeAddFirstModal()
  else if (showEditFirstModal.value) closeEditFirstModal()
  else if (showAddSecondModal.value) closeAddSecondModal()
  else if (showEditSecondModal.value) closeEditSecondModal()
  else if (showDeleteModal.value) closeDeleteModal()
}

const fetchAll = async () => {
  try {
    isLoading.value = true
    const [r1, r2] = await Promise.all([sortApi.getFirstCategories(), sortApi.getSecondCategories()])
    firstList.value = r1.data.data
    secondList.value = r2.data.data
  } catch (error) {
    await handleApiError(error, '카테고리 목록을 불러오는데 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

const addFirst = async () => {
  try {
    isLoading.value = true
    await sortApi.createFirstCategory({ korSortFirst: newFirst.value.korSortFirst, nameSortFirst: newFirst.value.nameSortFirst })
    await swAlert('대분류가 추가되었습니다.', 'success')
    closeAddFirstModal()
    await fetchAll()
  } catch (error) {
    await handleApiError(error, '대분류 추가에 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

const openEditFirstModal = (item) => {
  editingFirst.value = { seqSortFirst: item.seqSortFirst, korSortFirst: item.korSortFirst, nameSortFirst: item.nameSortFirst }
  showEditFirstModal.value = true
}

const updateFirst = async () => {
  try {
    isLoading.value = true
    await sortApi.updateFirstCategory(editingFirst.value.seqSortFirst, { korSortFirst: editingFirst.value.korSortFirst, nameSortFirst: editingFirst.value.nameSortFirst })
    await swAlert('대분류가 수정되었습니다.', 'success')
    closeEditFirstModal()
    await fetchAll()
  } catch (error) {
    await handleApiError(error, '대분류 수정에 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

const confirmDeleteFirst = (item) => {
  deletingItem.value = { type: 'first', id: item.seqSortFirst, name: item.korSortFirst }
  showDeleteModal.value = true
}

const addSecond = async () => {
  try {
    isLoading.value = true
    await sortApi.createSecondCategory({ seqSortFirst: newSecond.value.seqSortFirst, korSortSecond: newSecond.value.korSortSecond, nameSortSecond: newSecond.value.nameSortSecond })
    await swAlert('중분류가 추가되었습니다.', 'success')
    closeAddSecondModal()
    await fetchAll()
  } catch (error) {
    await handleApiError(error, '중분류 추가에 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

const openEditSecondModal = (item) => {
  editingSecond.value = { seqSortSecond: item.seqSortSecond, seqSortFirst: item.seqSortFirst, korSortSecond: item.korSortSecond, nameSortSecond: item.nameSortSecond }
  showEditSecondModal.value = true
}

const updateSecond = async () => {
  try {
    isLoading.value = true
    await sortApi.updateSecondCategory(editingSecond.value.seqSortSecond, { seqSortFirst: editingSecond.value.seqSortFirst, korSortSecond: editingSecond.value.korSortSecond, nameSortSecond: editingSecond.value.nameSortSecond })
    await swAlert('중분류가 수정되었습니다.', 'success')
    closeEditSecondModal()
    await fetchAll()
  } catch (error) {
    await handleApiError(error, '중분류 수정에 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

const confirmDeleteSecond = (item) => {
  deletingItem.value = { type: 'second', id: item.seqSortSecond, name: item.korSortSecond }
  showDeleteModal.value = true
}

const executeDelete = async () => {
  try {
    isLoading.value = true
    if (deletingItem.value.type === 'first') {
      await sortApi.deleteFirstCategory(deletingItem.value.id)
      await swAlert('대분류가 삭제되었습니다.', 'success')
    } else {
      await sortApi.deleteSecondCategory(deletingItem.value.id)
      await swAlert('중분류가 삭제되었습니다.', 'success')
    }
    closeDeleteModal()
    await fetchAll()
  } catch (error) {
    await handleApiError(error, '삭제에 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

const closeAddFirstModal = () => { showAddFirstModal.value = false; newFirst.value = { korSortFirst: '', nameSortFirst: '' } }
const closeEditFirstModal = () => { showEditFirstModal.value = false; editingFirst.value = { seqSortFirst: null, korSortFirst: '', nameSortFirst: '' } }
const closeAddSecondModal = () => { showAddSecondModal.value = false; newSecond.value = { seqSortFirst: '', korSortSecond: '', nameSortSecond: '' } }
const closeEditSecondModal = () => { showEditSecondModal.value = false; editingSecond.value = { seqSortSecond: null, seqSortFirst: null, korSortSecond: '', nameSortSecond: '' } }
const closeDeleteModal = () => { showDeleteModal.value = false; deletingItem.value = { type: '', id: null, name: '' } }

onMounted(async () => {
  await fetchAll()
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.category-management { max-width: 100%; font-size: 13px; color: var(--pb-color-text); }

.section-header { margin-bottom: 20px; }
.section-title { font-size: 15px; font-weight: 700; color: var(--pb-color-heading); margin: 0 0 3px; }
.section-description { font-size: 13px; color: var(--pb-color-text-muted); margin: 0; }

.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px; margin-bottom: 20px; }
.stat-card { display: flex; align-items: center; gap: 14px; padding: 16px 18px; background: var(--pb-color-surface); border: 1px solid var(--pb-color-border); border-radius: var(--pb-radius-lg); box-shadow: var(--pb-shadow-xs); }
.stat-icon { display: flex; align-items: center; justify-content: center; width: 36px; height: 36px; background: var(--pb-color-accent-soft); border-radius: var(--pb-radius-md); color: var(--pb-color-accent); flex-shrink: 0; }
.stat-number { font-size: 24px; font-weight: 700; line-height: 1; color: var(--pb-color-heading); }
.stat-label { font-size: 12px; color: var(--pb-color-text-soft); margin-top: 2px; }

.sub-tabs { display: flex; gap: 4px; margin-bottom: 16px; border-bottom: 1px solid var(--pb-color-border); }
.sub-tab { height: 34px; padding: 0 16px; background: transparent; border: none; border-bottom: 2px solid transparent; font-size: 13px; font-weight: 500; color: var(--pb-color-text-muted); cursor: pointer; transition: color 0.12s, border-color 0.12s; margin-bottom: -1px; }
.sub-tab.active { color: var(--pb-color-brand); border-bottom-color: var(--pb-color-brand); font-weight: 600; }
.sub-tab:hover:not(.active) { color: var(--pb-color-text); }

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
.item-name { font-weight: 500; color: var(--pb-color-heading); }
.item-parent { color: var(--pb-color-text-muted); font-size: 12px; }
.empty-row { text-align: center; color: var(--pb-color-text-muted); padding: 24px 16px !important; }

.item-actions { display: flex; gap: 4px; }
.edit-btn, .delete-btn { display: flex; align-items: center; justify-content: center; width: 30px; height: 30px; border: 1px solid var(--pb-color-border); border-radius: var(--pb-radius-sm); cursor: pointer; transition: background 0.12s, border-color 0.12s, color 0.12s; }
.edit-btn { background: var(--pb-color-surface); color: var(--pb-color-text-muted); }
.edit-btn:hover { background: var(--pb-color-surface-muted); color: var(--pb-color-text); border-color: var(--pb-color-border-strong); }
.delete-btn { background: var(--pb-color-danger-soft); color: var(--pb-color-danger); }
.delete-btn:hover:not(:disabled) { background: var(--pb-color-danger); color: var(--pb-color-surface); border-color: var(--pb-color-danger); }

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
.item-info { padding: 8px 12px; background: var(--pb-color-danger-soft); border: 1px solid var(--pb-color-danger); border-radius: var(--pb-radius-sm); color: var(--pb-color-danger); font-size: 12px; }

@media (max-width: 768px) {
  .stats-grid { grid-template-columns: 1fr; }
  .data-table th, .data-table td { padding: 9px 12px; }
  .modal-content { width: 95%; }
  .modal-actions { flex-direction: column; }
  .cancel-btn, .submit-btn, .delete-confirm-btn { width: 100%; }
}
</style>
