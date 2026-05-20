<template>
  <div class="terms-editor">
    <div class="editor-header">
      <h2 class="section-title">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <polyline points="14,2 14,8 20,8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <line x1="16" y1="13" x2="8" y2="13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <line x1="16" y1="17" x2="8" y2="17" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <polyline points="10,9 9,9 8,9" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        약관 관리
      </h2>
      <p class="section-description">이용약관, 개인정보처리방침, 디스코드 알림 동의서를 수정합니다.</p>
    </div>

    <!-- 약관 탭 -->
    <div class="terms-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.type"
        class="tab-btn"
        :class="{ active: activeType === tab.type }"
        @click="switchTab(tab.type)"
      >
        {{ tab.label }}
      </button>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <span>약관 내용 불러오는 중...</span>
    </div>

    <div v-else class="editor-body">
      <!-- 에디터 영역 -->
      <div class="editor-section">
        <div class="editor-toolbar">
          <span class="editor-label">HTML 편집</span>
          <span class="char-count">{{ currentContent.length }}자</span>
        </div>
        <textarea
          v-model="currentContent"
          class="content-textarea"
          placeholder="약관 내용을 HTML 형식으로 입력하세요..."
          rows="20"
        ></textarea>
        <div class="editor-actions">
          <button class="btn-reset" @click="resetContent" :disabled="saving">
            초기화
          </button>
          <button class="btn-save" @click="saveTerms" :disabled="saving || !currentContent.trim()">
            <span v-if="saving">저장 중...</span>
            <span v-else>저장</span>
          </button>
        </div>
      </div>

      <!-- 미리보기 영역 -->
      <div class="preview-section">
        <div class="preview-label">미리보기</div>
        <div class="preview-content" v-html="previewHtml"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { swAlert, swToast } from '@/utils/sweetAlert'

const tabs = [
  { type: 'SERVICE', label: '서비스 이용약관' },
  { type: 'PRIVACY', label: '개인정보처리방침' },
  { type: 'DISCORD', label: '디스코드 알림 동의' },
]

const activeType = ref('SERVICE')
const currentContent = ref('')
const originalContent = ref('')
const loading = ref(false)
const saving = ref(false)

const previewHtml = computed(() =>
  currentContent.value || '<p style="color:#aaa">내용을 입력하면 여기에 미리보기가 표시됩니다.</p>'
)

const fetchTerms = async (type) => {
  loading.value = true
  try {
    const res = await axios.get(`/api/terms/${type}`)
    currentContent.value = res.data.data?.content || ''
    originalContent.value = currentContent.value
  } catch (error) {
    await swAlert('약관 내용을 불러오지 못했습니다.', 'error')
    currentContent.value = ''
    originalContent.value = ''
  } finally {
    loading.value = false
  }
}

const switchTab = async (type) => {
  if (activeType.value === type) return
  activeType.value = type
  await fetchTerms(type)
}

const saveTerms = async () => {
  if (!currentContent.value.trim()) return
  saving.value = true
  try {
    await axios.put(`/api/terms/${activeType.value}`, { content: currentContent.value })
    originalContent.value = currentContent.value
    swToast('약관이 저장되었습니다.', 'success')
  } catch (error) {
    await swAlert('저장에 실패했습니다.', 'error')
  } finally {
    saving.value = false
  }
}

const resetContent = async () => {
  currentContent.value = originalContent.value
}

onMounted(() => fetchTerms('SERVICE'))
</script>

<style scoped>
.terms-editor {
  padding: 8px 0;
}

.editor-header {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.4rem;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 6px;
}

.section-description {
  color: #718096;
  font-size: 0.9rem;
  margin: 0;
}

.terms-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  border-bottom: 2px solid #e2e8f0;
  padding-bottom: 0;
}

.tab-btn {
  padding: 10px 20px;
  border: none;
  background: transparent;
  color: #718096;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  margin-bottom: -2px;
  transition: all 0.2s ease;
  border-radius: 4px 4px 0 0;
}

.tab-btn:hover {
  color: #4a5568;
  background: #f7fafc;
}

.tab-btn.active {
  color: #3182ce;
  border-bottom-color: #3182ce;
  font-weight: 600;
}

.loading-state {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 40px;
  justify-content: center;
  color: #718096;
}

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #e2e8f0;
  border-top-color: #3182ce;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.editor-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.editor-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.editor-label,
.preview-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #4a5568;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.char-count {
  font-size: 0.8rem;
  color: #a0aec0;
}

.content-textarea {
  width: 100%;
  min-height: 400px;
  padding: 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-family: 'Fira Code', 'Courier New', monospace;
  font-size: 0.85rem;
  line-height: 1.6;
  color: #2d3748;
  resize: vertical;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.content-textarea:focus {
  outline: none;
  border-color: #3182ce;
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.1);
}

.editor-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.btn-reset {
  padding: 10px 20px;
  border: 1px solid #e2e8f0;
  background: white;
  color: #4a5568;
  font-size: 0.9rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-reset:hover:not(:disabled) {
  background: #f7fafc;
  border-color: #cbd5e0;
}

.btn-save {
  padding: 10px 24px;
  border: none;
  background: linear-gradient(135deg, #3182ce, #2b6cb0);
  color: white;
  font-size: 0.9rem;
  font-weight: 600;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(49, 130, 206, 0.3);
}

.btn-save:hover:not(:disabled) {
  background: linear-gradient(135deg, #2b6cb0, #2c5282);
  box-shadow: 0 4px 12px rgba(49, 130, 206, 0.4);
  transform: translateY(-1px);
}

.btn-save:disabled,
.btn-reset:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.preview-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-content {
  min-height: 400px;
  padding: 20px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
  overflow-y: auto;
  font-size: 0.9rem;
  line-height: 1.7;
  color: #2d3748;
}

@media (max-width: 1100px) {
  .editor-body {
    grid-template-columns: 1fr;
  }
}
</style>
