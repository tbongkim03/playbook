<template>
  <div class="terms-editor">
    <div class="pb-page-head">
      <div>
        <h2>약관 관리</h2>
        <p>이용약관, 개인정보처리방침, 디스코드 알림 동의서를 수정합니다.</p>
      </div>
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
import * as termsApi from '@/api/terms'
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
    const res = await termsApi.getByType(type)
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
    await termsApi.update(activeType.value, currentContent.value)
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
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin-bottom: 6px;
}

.section-description {
  color: var(--pb-color-text-muted);
  font-size: 0.9rem;
  margin: 0;
}

.terms-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--pb-color-border);
  padding-bottom: 0;
}

.tab-btn {
  padding: 10px 20px;
  border: none;
  background: transparent;
  color: var(--pb-color-text-muted);
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: color 0.15s ease, border-color 0.15s ease;
  border-radius: var(--pb-radius-xs) var(--pb-radius-xs) 0 0;
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

.loading-state {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 40px;
  justify-content: center;
  color: var(--pb-color-text-muted);
}

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid var(--pb-color-border);
  border-top-color: var(--pb-color-brand);
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
  color: var(--pb-color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.char-count {
  font-size: 0.8rem;
  color: var(--pb-color-text-soft);
}

.content-textarea {
  width: 100%;
  min-height: 400px;
  padding: 16px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-md);
  font-family: 'Fira Code', 'Courier New', monospace;
  font-size: 0.85rem;
  line-height: 1.6;
  color: var(--pb-color-text);
  resize: vertical;
  transition: border-color 0.15s;
  box-sizing: border-box;
}

.content-textarea:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px rgba(47, 111, 78, 0.12);
}

.editor-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.btn-reset {
  padding: 10px 20px;
  border: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  font-size: 0.9rem;
  border-radius: var(--pb-radius-md);
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}

.btn-reset:hover:not(:disabled) {
  background: var(--pb-color-surface-muted);
  border-color: var(--pb-color-border-strong);
}

.btn-save {
  padding: 10px 24px;
  border: none;
  background: var(--pb-color-brand);
  color: white;
  font-size: 0.9rem;
  font-weight: 600;
  border-radius: var(--pb-radius-md);
  cursor: pointer;
  transition: background 0.15s;
}

.btn-save:hover:not(:disabled) {
  background: var(--pb-color-brand-strong);
}

.btn-save:disabled,
.btn-reset:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.preview-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-content {
  min-height: 400px;
  padding: 20px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-md);
  background: var(--pb-color-surface-muted);
  overflow-y: auto;
  font-size: 0.9rem;
  line-height: 1.7;
  color: var(--pb-color-text);
}

@media (max-width: 768px) {
  .editor-body {
    grid-template-columns: 1fr;
  }
  .content-textarea { min-height: 280px; }
  .preview-content { min-height: 200px; }
}
</style>
