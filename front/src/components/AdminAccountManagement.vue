<template>
  <div class="admin-account-management">
    <div class="section-header">
      <h2 class="section-title">관리자 계정 관리</h2>
      <p class="section-description">관리자 계정을 추가, 수정, 삭제할 수 있습니다.</p>
    </div>

    <!-- 통계 카드 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M16 21V19C16 17.9391 15.5786 16.9217 14.8284 16.1716C14.0783 15.4214 13.0609 15 12 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2"/>
            <circle cx="8.5" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
            <polyline points="17,11 19,13 23,9" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ adminList.length }}</div>
          <div class="stat-label">총 관리자</div>
        </div>
      </div>
    </div>

    <!-- 관리자 추가 버튼 -->
    <div class="action-bar">
      <button class="add-admin-btn" @click="showAddModal = true">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
          <line x1="12" y1="8" x2="12" y2="16" stroke="currentColor" stroke-width="2"/>
          <line x1="8" y1="12" x2="16" y2="12" stroke="currentColor" stroke-width="2"/>
        </svg>
        관리자 추가
      </button>
    </div>

    <!-- 관리자 목록 테이블 -->
    <div class="admin-table-container">
      <div class="table-header">
        <h3>관리자 목록</h3>
      </div>
      
      <div class="table-wrapper">
        <table class="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>이름</th>
              <th>디스코드 ID</th>
              <th>생성일</th>
              <th>작업</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="admin in adminList" :key="admin.idAdmin" class="admin-row">
              <td class="admin-id">{{ admin.idAdmin }}</td>
              <td class="admin-name">{{ admin.nameAdmin }}</td>
              <td class="admin-discord">{{ admin.dcAdmin || '-' }}</td>
              <td class="admin-date">{{ formatDate(admin.createdAt) }}</td>
              <td class="admin-actions">
                <button class="edit-btn" @click="openEditModal(admin)">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M11 4H4C3.46957 4 2.96086 4.21071 2.58579 4.58579C2.21071 4.96086 2 5.46957 2 6V20C2 20.5304 2.21071 21.0391 2.58579 21.4142C2.96086 21.7893 3.46957 22 4 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V13" stroke="currentColor" stroke-width="2"/>
                    <path d="M18.5 2.5C18.8978 2.10217 19.4374 1.87868 20 1.87868C20.5626 1.87868 21.1022 2.10217 21.5 2.5C21.8978 2.89782 22.1213 3.43739 22.1213 4C22.1213 4.56261 21.8978 5.10217 21.5 5.5L12 15L8 16L9 12L18.5 2.5Z" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </button>
                <button class="delete-btn" @click="confirmDeleteAdmin(admin)">
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

    <!-- 관리자 추가 모달 -->
    <div v-if="showAddModal" class="modal-overlay" @click="closeAddModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>새 관리자 추가</h3>
          <button class="modal-close" @click="closeAddModal">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2"/>
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2"/>
            </svg>
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
    <div v-if="showEditModal" class="modal-overlay" @click="closeEditModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>디스코드 ID 수정</h3>
          <button class="modal-close" @click="closeEditModal">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2"/>
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2"/>
            </svg>
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
            <label for="editPassword">현재 비밀번호 확인</label>
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
              placeholder="디스코드 ID를 입력하세요"
            />
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
    <div v-if="showDeleteModal" class="modal-overlay" @click="closeDeleteModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>관리자 삭제 확인</h3>
          <button class="modal-close" @click="closeDeleteModal">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2"/>
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2"/>
            </svg>
          </button>
        </div>
        <div class="delete-warning">
          <div class="warning-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 9V13" stroke="currentColor" stroke-width="2"/>
              <path d="M12 17.02L12.01 16.991" stroke="currentColor" stroke-width="2"/>
              <path d="M10.29 3.86L1.82 18C1.64466 18.3024 1.55685 18.6453 1.56455 18.9928C1.57225 19.3403 1.67516 19.6792 1.86244 19.9757C2.04973 20.2723 2.31561 20.5157 2.6289 20.6812C2.9422 20.8467 3.29427 20.9286 3.65 20.92H20.35C20.7057 20.9286 21.0578 20.8467 21.3711 20.6812C21.6844 20.5157 21.9503 20.2723 22.1376 19.9757C22.3248 19.6792 22.4278 19.3403 22.4355 18.9928C22.4432 18.6453 22.3553 18.3024 22.18 18L13.71 3.86C13.5317 3.56611 13.2807 3.32312 12.9812 3.15446C12.6817 2.98581 12.3438 2.89725 12 2.89725C11.6562 2.89725 11.3183 2.98581 11.0188 3.15446C10.7193 3.32312 10.4683 3.56611 10.29 3.86Z" stroke="currentColor" stroke-width="2"/>
            </svg>
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
import { ref, onMounted, onBeforeUnmount } from 'vue'
import axios from 'axios'

// 반응형 데이터
const adminList = ref([])
const isLoading = ref(false)

// 모달 상태
const showAddModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)

// 비밀번호 입력 필드
const editPassword = ref('')
const deletePassword = ref('')

// 현재 사용자 정보
const currentUser = ref({
  idAdmin: '',
  nameAdmin: '',
  dcAdmin: ''
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
  dcAdmin: ''
})

const editingAdmin = ref({
  idAdmin: '',
  nameAdmin: '',
  dcAdmin: ''
})

const deletingAdmin = ref({
  idAdmin: '',
  nameAdmin: '',
  dcAdmin: ''
})

// API 헤더 설정
const getAuthHeaders = () => {
  const token = localStorage.getItem('jwtToken')
  return {
    Authorization: `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
}

// 비밀번호 검증
const validatePassword = async (idAdmin, password) => {
  try {
    const response = await axios.post(
      `http://localhost:8080/admin/validate?id=${idAdmin}`,
      password,
      {
        headers: getAuthHeaders()
      }
    )
    return response.data
  } catch (error) {
    throw error
  }
}

// 현재 사용자 정보 조회
const fetchCurrentUser = async () => {
  try {
    const response = await axios.get('http://localhost:8080/admin/me', {
      headers: getAuthHeaders()
    })
    currentUser.value = response.data
  } catch (error) {
  }
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
    const response = await axios.get(`http://localhost:8080/admin/register/validate`, {
      params: { id: newAdmin.value.idAdmin },
      headers: getAuthHeaders()
    })

    const data = await response.data;

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
    const response = await axios.get('http://localhost:8080/admin/list', {
      headers: getAuthHeaders()
    })
    // 백엔드의 AdminListResponseDto 구조에 맞게 수정
    adminList.value = response.data.content || response.data.adminList || response.data
  } catch (error) {
    if (error.response?.status === 403) {
      alert('관리자 권한이 필요합니다.')
    } else if (error.response?.status === 401) {
      alert('로그인이 필요합니다.')
    } else {
      alert('관리자 목록을 불러오는데 실패했습니다.')
    }
  } finally {
    isLoading.value = false
  }
}

// 관리자 추가
const addAdmin = async () => {
  if (!newAdmin.value.idAdmin || !newAdmin.value.nameAdmin || !newAdmin.value.pwAdmin) {
    alert('필수 정보를 모두 입력해주세요.')
    return
  }

  if (!idValidation.value.isValid) {
    alert('ID 중복확인을 완료해주세요.')
    return
  }

  try {
    isLoading.value = true
    const response = await axios.post('http://localhost:8080/admin/register', newAdmin.value, {
      headers: getAuthHeaders()
    })
    
    alert('관리자가 성공적으로 추가되었습니다.')
    closeAddModal()
    await fetchAdminList()
  } catch (error) {
    if (error.response?.status === 403) {
      alert('관리자만 접근 가능합니다.')
    } else if (error.response?.status === 401) {
      alert('인증에 실패했습니다.')
    } else {
      alert(error.response?.data || '관리자 추가에 실패했습니다.')
    }
  } finally {
    isLoading.value = false
  }
}

// 관리자 디스코드 ID 수정 (비밀번호 검증 포함)
const updateAdmin = async () => {
  if (!editPassword.value) {
    alert('현재 비밀번호를 입력해주세요.')
    return
  }

  try {
    isLoading.value = true
    
    // 먼저 비밀번호 검증
    await validatePassword(editingAdmin.value.idAdmin, editPassword.value)
    
    // 비밀번호가 맞으면 디스코드 ID 업데이트
    const response = await axios.put('http://localhost:8080/admin/discord', 
      editingAdmin.value.dcAdmin, {
      headers: getAuthHeaders()
    })
    
    alert('디스코드 ID가 성공적으로 수정되었습니다.')
    closeEditModal()
    await fetchAdminList()
  } catch (error) {
    console.error('관리자 수정 실패:', error)
    if (error.response?.status === 403) {
      alert('관리자만 접근 가능합니다.')
    } else if (error.response?.status === 401) {
      alert('비밀번호가 일치하지 않습니다.')
    } else {
      alert(error.response?.data || '관리자 수정에 실패했습니다.')
    }
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
    alert('현재 비밀번호를 입력해주세요.')
    return
  }

  try {
    isLoading.value = true
    
    // 먼저 비밀번호 검증
    await validatePassword(idAdmin, deletePassword.value)
    
    // 비밀번호가 맞으면 관리자 삭제
    const response = await axios.delete('http://localhost:8080/admin', {
      headers: getAuthHeaders(),
      data: deletingAdmin.value.idAdmin
    })
    
    alert('관리자가 성공적으로 삭제되었습니다.')
    closeDeleteModal()
    await fetchAdminList()
    
  } catch (error) {
    if (error.response?.status === 403) {
      alert('관리자 권한이 필요합니다.')
    } else if (error.response?.status === 401) {
      alert('비밀번호가 일치하지 않습니다.')
    } else {
      alert(error.response?.data || '관리자 삭제에 실패했습니다.')
    }
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
    dcAdmin: ''
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
  showEditModal.value = true
}

const closeEditModal = () => {
  showEditModal.value = false
  editPassword.value = ''
  editingAdmin.value = {
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
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR')
}

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
  await fetchCurrentUser()
  await fetchAdminList()
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.admin-account-management {
  max-width: 100%;
}

.section-header {
  margin-bottom: 32px;
}

.section-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 8px;
}

.section-description {
  font-size: 1rem;
  color: #718096;
  margin: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: linear-gradient(135deg, #ddbff0 0%, #e6ccf7 100%);
  border-radius: 20px;
  color: #2d3748;
  box-shadow: 0 8px 32px rgba(221, 191, 240, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 16px;
}

.stat-number {
  font-size: 2rem;
  font-weight: 700;
  line-height: 1;
}

.stat-label {
  font-size: 0.9rem;
  opacity: 0.9;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 24px;
}

.add-admin-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: linear-gradient(135deg, #b8e6c1 0%, #d4f1d4 100%);
  color: #2d3748;
  border: none;
  border-radius: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 6px 20px rgba(184, 230, 193, 0.3);
}

.add-admin-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(184, 230, 193, 0.4);
}

.admin-table-container {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.03);
}

.table-header {
  padding: 24px;
  border-bottom: 1px solid #f1f5f9;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.table-header h3 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
}

.table-wrapper {
  overflow-x: auto;
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
}

.admin-table th {
  text-align: left;
  padding: 18px 24px;
  background: #fafafa;
  color: #2d3748;
  font-weight: 600;
  font-size: 0.9rem;
  border-bottom: 1px solid #e2e8f0;
}

.admin-table td {
  padding: 18px 24px;
  border-bottom: 1px solid #f7fafc;
  color: #4a5568;
}

.admin-row:hover {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.admin-name {
  font-weight: 500;
  color: #2d3748;
}

.admin-actions {
  display: flex;
  gap: 8px;
}

.edit-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #a8dadc 0%, #b8e6c1 100%);
  color: #2d3748;
  box-shadow: 0 2px 8px rgba(168, 218, 220, 0.3);
}

.edit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(168, 218, 220, 0.4);
}

.delete-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #feb2b2 0%, #fbb6ce 100%);
  color: #2d3748;
  box-shadow: 0 2px 8px rgba(254, 178, 178, 0.3);
}

.delete-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(254, 178, 178, 0.4);
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
  backdrop-filter: blur(8px);
}

.modal-content {
  background: white;
  border-radius: 20px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 24px 0 24px;
  margin-bottom: 24px;
}

.modal-header h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
}

.modal-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: linear-gradient(135deg, #e2e8f0 0%, #f1f5f9 100%);
  border-radius: 12px;
  color: #4a5568;
  cursor: pointer;
  transition: all 0.3s ease;
}

.modal-close:hover {
  background: linear-gradient(135deg, #cbd5e0 0%, #e2e8f0 100%);
  transform: translateY(-1px);
}

.modal-form {
  padding: 0 24px 24px 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #2d3748;
}

.form-group input {
  width: 100%;
  padding: 14px 18px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s ease;
  box-sizing: border-box;
  background: #fafafa;
}

.form-group input:focus {
  outline: none;
  border-color: #a8dadc;
  box-shadow: 0 0 0 3px rgba(168, 218, 220, 0.15);
  background: white;
}

.disabled-input {
  background: #f1f5f9 !important;
  color: #718096 !important;
  cursor: not-allowed !important;
}

.id-input-group {
  display: flex;
  gap: 8px;
  align-items: stretch;
}

.id-input-group input {
  flex: 1;
}

.validate-btn {
  padding: 14px 16px;
  background: linear-gradient(135deg, #a8dadc 0%, #b8e6c1 100%);
  color: #2d3748;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.validate-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(168, 218, 220, 0.3);
}

.validate-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.validation-message {
  margin-top: 8px;
  font-size: 0.9rem;
  font-weight: 500;
}

.validation-message.valid {
  color: #28a745;
}

.validation-message.invalid {
  color: #dc3545;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
}

.cancel-btn,
.submit-btn {
  padding: 12px 20px;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn {
  background: linear-gradient(135deg, #e2e8f0 0%, #f1f5f9 100%);
  color: #4a5568;
  box-shadow: 0 4px 16px rgba(226, 232, 240, 0.3);
}

.cancel-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(226, 232, 240, 0.4);
}

.submit-btn {
  background: linear-gradient(135deg, #ddbff0 0%, #e6ccf7 100%);
  color: #2d3748;
  box-shadow: 0 4px 16px rgba(221, 191, 240, 0.3);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(221, 191, 240, 0.4);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.delete-warning {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 24px 24px 24px;
  text-align: center;
}

.warning-icon {
  color: #f56565;
  margin-bottom: 16px;
}

.warning-content h4 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #2d3748;
  margin: 0 0 8px 0;
}

.warning-content p {
  color: #718096;
  margin: 0 0 16px 0;
  line-height: 1.5;
}

.admin-info {
  padding: 12px 16px;
  background: linear-gradient(135deg, #fed7d7 0%, #feb2b2 100%);
  border-radius: 12px;
  color: #2d3748;
  font-size: 0.9rem;
}

.delete-confirm-btn {
  padding: 12px 20px;
  background: linear-gradient(135deg, #feb2b2 0%, #fbb6ce 100%);
  color: #2d3748;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(254, 178, 178, 0.3);
}

.delete-confirm-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(254, 178, 178, 0.4);
}

.delete-confirm-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .action-bar {
    justify-content: center;
  }
  
  .admin-table th,
  .admin-table td {
    padding: 14px 16px;
    font-size: 0.9rem;
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
  .submit-btn {
    width: 100%;
  }

  .id-input-group {
    flex-direction: column;
  }

  .validate-btn {
    align-self: stretch;
  }
}

@media (max-width: 480px) {
  .admin-table {
    font-size: 0.8rem;
  }
  
  .admin-actions {
    flex-direction: column;
    gap: 6px;
  }
  
  .edit-btn,
  .delete-btn {
    width: 32px;
    height: 32px;
  }
  
  .modal-content {
    border-radius: 16px;
  }
  
  .form-group input {
    padding: 12px 16px;
  }
}
</style>