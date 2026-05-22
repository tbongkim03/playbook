<!-- BookRegister.vue - 모달용 -->
<template>
  <div class="book-register-modal">
    <div class="register-content">
      <!-- 왼쪽 폼 영역 -->
      <div class="form-section">
        <div class="section-card">
          <div class="card-header">
            <h2 class="card-title">도서 정보</h2>
          </div>

          <div class="form-content">
            <!-- ISBN 검색 -->
            <div class="form-group isbn-group">
              <label class="form-label">ISBN 검색</label>
              <div class="isbn-input-wrapper">
                <input 
                  type="number" 
                  class="form-input" 
                  placeholder="ISBN을 입력하세요" 
                  v-model="book.isbn" 
                  @keydown="blockJavascriptInput" 
                  @keydown.space.prevent
                  @keydown.enter="handleIsbnEnter"
                />
                <button class="search-btn" type="button" @click="searchISBN" :disabled="isSearching">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
                    <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2"/>
                  </svg>
                  {{ isSearching ? '조회중...' : '조회' }}
                </button>
              </div>
            </div>

            <!-- 도서 제목 -->
            <div class="form-group">
              <label class="form-label">도서 제목 <span class="required">*</span></label>
              <input 
                type="text" 
                class="form-input" 
                v-model="book.title" 
                @keydown="blockJavascriptInput" 
                @keydown.enter="handleFormEnter"
                placeholder="도서 제목을 입력하세요"
                required
              />
            </div>

            <!-- 저자 -->
            <div class="form-group">
              <label class="form-label">저자 <span class="required">*</span></label>
              <input 
                type="text" 
                class="form-input" 
                v-model="book.author" 
                @keydown="blockJavascriptInput" 
                @keydown.enter="handleFormEnter"
                placeholder="저자를 입력하세요"
                required
              />
            </div>

            <!-- 출판사 -->
            <div class="form-group">
              <label class="form-label">출판사 <span class="required">*</span></label>
              <input 
                type="text" 
                class="form-input" 
                v-model="book.publisher" 
                @keydown="blockJavascriptInput" 
                @keydown.enter="handleFormEnter"
                placeholder="출판사를 입력하세요"
                required
              />
            </div>

            <!-- 출판일 -->
            <div class="form-group">
              <label class="form-label">출판일</label>
              <input 
                type="date" 
                class="form-input" 
                v-model="book.publishDate" 
                @keydown="blockJavascriptInput" 
                @keydown.enter="handleFormEnter"
              />
            </div>

            <!-- 등록 버튼 -->
            <div class="form-actions">
              <button 
                class="cancel-btn" 
                @click="$emit('cancel')" 
                type="button"
                :disabled="isLoading"
              >
                취소
              </button>
              <button 
                class="register-btn" 
                @click="submitBook" 
                :disabled="isLoading || !isFormValid"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2"/>
                  <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2"/>
                  <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2"/>
                </svg>
                {{ isLoading ? '등록 중...' : '등록' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 오른쪽 미리보기 영역 -->
      <div class="preview-section">
        <div class="section-card">
          <div class="card-header">
            <h2 class="card-title">도서 미리보기</h2>
          </div>
          
          <div class="preview-content">
            <div class="book-cover-container" :class="{'has-image': book.title_url}">
              <div 
                class="book-cover-wrapper" 
                ref="bookImg" 
                @mousemove="useMouse" 
                @mouseleave="resetTransform"
              >
                <div class="book-cover" v-if="book.title_url">
                  <img :src="book.title_url" alt="도서 표지" class="cover-image" />
                </div>
                <div class="no-image-placeholder" v-else>
                  <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M4 19.5C4 18.1193 5.11929 17 6.5 17H20" stroke="currentColor" stroke-width="2"/>
                    <path d="M6.5 2H20V22H6.5C5.11929 22 4 20.8807 4 19.5V4.5C4 3.11929 5.11929 2 6.5 2Z" stroke="currentColor" stroke-width="2"/>
                  </svg>
                  <p>도서 표지</p>
                  <span>ISBN을 조회하면 표지가 표시됩니다</span>
                </div>
              </div>
            </div>

            <!-- 도서 정보 요약 -->
            <div class="book-info-summary" v-if="book.title || book.author || book.publisher">
              <div class="info-item" v-if="book.title">
                <label>제목</label>
                <span>{{ book.title }}</span>
              </div>
              <div class="info-item" v-if="book.author">
                <label>저자</label>
                <span>{{ book.author }}</span>
              </div>
              <div class="info-item" v-if="book.publisher">
                <label>출판사</label>
                <span>{{ book.publisher }}</span>
              </div>
              <div class="info-item" v-if="book.publishDate">
                <label>출판일</label>
                <span>{{ formatDisplayDate(book.publishDate) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import axios from 'axios'
import { swAlert } from '@/utils/sweetAlert'

// Props와 Emits
const emit = defineEmits(['book-registered', 'cancel'])

const bookImg = ref(null)
const isLoading = ref(false)
const isSearching = ref(false)
const hasSearched = ref(false) // 조회 완료 상태 추적

const book = reactive({
  isbn: '',
  title: '',
  author: '',
  publisher: '',
  publishDate: '',
  categoryLarge: '',
  categoryMedium: '',
  title_url: ''
})

// 폼 유효성 검사
const isFormValid = computed(() => {
  return book.title.trim() && book.author.trim() && book.publisher.trim()
})

// JavaScript 입력 차단 함수
function blockJavascriptInput(e) {
  const prohibitedPatterns = [
    /javascript:/i,
    /<script/i,
    /on\w+=/i,
    /eval\(/i,
    /setTimeout/i,
    /setInterval/i
  ]
  
  const value = e.target.value + e.key
  
  for (const pattern of prohibitedPatterns) {
    if (pattern.test(value)) {
      e.preventDefault()
      return
    }
  }
}

// ISBN 입력 필드에서 엔터 키 처리
function handleIsbnEnter(e) {
  if (!isSearching.value && book.isbn && String(book.isbn).trim()) {
    searchISBN()
  }
}

// 다른 입력 필드에서 엔터 키 처리
function handleFormEnter(e) {
  if (hasSearched.value && isFormValid.value && !isLoading.value) {
    submitBook()
  }
}

async function searchISBN() {
  // const apiKey = import.meta.env.VITE_NL_API_KEY

  const isbn = String(book.isbn || '').trim()

  if (!isbn) {
    await swAlert('ISBN을 입력해주세요.', 'warning')
    return
  }

  isSearching.value = true

  try {
    const res = await axios.post('/api/national-library/isbn', isbn)
    const data = res.data.data

    const doc = data?.docs?.[0] || null
    // console.log('조회된 도서 정보:', doc)

    if (!doc) {
      await swAlert('도서 정보를 찾을 수 없습니다.', 'error')
      return
    }

    book.title = doc['TITLE'] || ''
    book.author = doc['AUTHOR'] || ''
    book.publisher = doc['PUBLISHER'] || ''
    book.publishDate = formatDate(doc['PUBLISH_PREDATE'] || '')
    book.title_url = doc['TITLE_URL'] || ''
    
    // console.log('국립중앙도서관 API에서 가져온 TITLE_URL:', book.title_url)
    
    // TITLE_URL이 없는 경우 네이버 검색 API로 이미지 검색
    if (!book.title_url || book.title_url.trim() === '') {
      // console.log('TITLE_URL이 비어있어 네이버 API로 이미지를 검색합니다...')
      try {
        const naverImageUrl = await searchBookImageFromNaver()
        book.title_url = naverImageUrl
        // console.log('네이버 API에서 가져온 이미지로 설정:', book.title_url)
      } catch (error) {
        // console.warn('네이버 API 이미지 검색 실패:', error)
        // 실패해도 계속 진행 (이미지 없이)
        book.title_url = ''
      }
    }
    
    hasSearched.value = true // 조회 완료 상태 설정

  } catch (err) {
    await swAlert('도서 정보를 조회하는 중 오류가 발생했습니다.', 'error')
  } finally {
    isSearching.value = false
  }
}

function formatDate(dateStr) {
  // YYYYMMDD 형식인 경우 처리
  if (/^\d{8}$/.test(dateStr)) {
    const year = dateStr.substring(0, 4)
    const month = dateStr.substring(4, 6)
    const day = dateStr.substring(6, 8)
    return `${year}-${month}-${day}`
  }

  // YYYY 형식만 있는 경우
  if (/^\d{4}$/.test(dateStr)) {
    return `${dateStr}-01-01`
  }

  return ''
}

function formatDisplayDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('ko-KR')
}

async function submitBook() {
  if (!isFormValid.value) {
    await swAlert('필수 정보를 모두 입력해주세요.', 'warning')
    return
  }
  
  const payload = {
    seqSortSecond: 0,
    isbnBook: book.isbn,
    titleBook: book.title,
    authorBook: book.author,
    publisherBook: book.publisher,
    publishDateBook: book.publishDate,
    imageBook: book.title_url || ''
  }

  try {
    isLoading.value = true

    const response = await axios.post('/api/books', payload)
    const data = response.data.data

    await swAlert(`도서 "${data?.titleBook || book.title}"가 성공적으로 등록되었습니다!`, 'success')
    
    // 폼 초기화
    resetForm()
    
  } catch (err) {
    await swAlert(`등록 실패: ${err.response?.data?.msg || err.message}`, 'error')
  } finally {
    isLoading.value = false
  }
}

async function searchBookImageFromNaver() {
  if (book.isbn && String(book.isbn).trim()) {
    try {
      const response = await axios.post('/api/naver/book-search', {
        isbn: String(book.isbn).trim(),
        display: 10
      })
      const data = response.data.data

      if (data.items && data.items.length > 0) {
        const imageUrl = data.items[0].image
        if (imageUrl) {
          return imageUrl
        }
      }
    } catch (error) {
      console.warn('ISBN 검색 중 오류:', error.response?.data)
    }
  }
}

function resetForm() {
  Object.keys(book).forEach(key => {
    book[key] = ''
  })
  hasSearched.value = false // 조회 상태도 초기화
}

function useMouse(e) {
  if (!book.title_url || !bookImg.value) return
  
  const rect = bookImg.value.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  
  const rotateY = (x / rect.width - 0.5) * 20
  const rotateX = (0.5 - y / rect.height) * 20
  
  bookImg.value.style.transform = `perspective(600px) rotateY(${rotateY}deg) rotateX(${rotateX}deg)`
  bookImg.value.style.transition = 'none'
}

function resetTransform() {
  if (!bookImg.value) return
  
  bookImg.value.style.transform = 'perspective(600px) rotateY(0deg) rotateX(0deg)'
  bookImg.value.style.transition = 'transform 0.3s ease'
}
</script>

<style scoped>
.book-register-modal {
  width: 100%;
  height: 100%;
}

.register-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  height: 100%;
}

.form-section,
.preview-section {
  min-width: 0;
}

.section-card {
  background: var(--pb-color-surface-subtle);
  border-radius: var(--pb-radius-sm);
  border: 1px solid var(--pb-color-border);
  overflow: hidden;
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface-muted);
}

.card-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--pb-color-heading);
  margin: 0;
}

.form-content {
  padding: 1.5rem;
  height: calc(100% - 80px);
  overflow-y: auto;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: var(--pb-color-text-muted);
}

.required {
  color: var(--pb-color-danger);
}

.form-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-xs);
  font-size: 1rem;
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  transition: border-color 0.15s, box-shadow 0.15s;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px var(--pb-color-brand-soft);
}

.isbn-group {
  position: relative;
}

.isbn-input-wrapper {
  display: flex;
  gap: 0;
}

.isbn-input-wrapper .form-input {
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
  border-right: none;
}

.search-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0.75rem 1rem;
  background: var(--pb-color-text-muted);
  color: white;
  border: 1px solid var(--pb-color-text-muted);
  border-top-right-radius: var(--pb-radius-xs);
  border-bottom-right-radius: var(--pb-radius-xs);
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
  white-space: nowrap;
}

.search-btn:hover:not(:disabled) {
  background: var(--pb-color-text);
  border-color: var(--pb-color-text);
}

.search-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
}

.cancel-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0.75rem 2rem;
  background: transparent;
  color: var(--pb-color-text-muted);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-xs);
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}

.cancel-btn:hover:not(:disabled) {
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text);
}

.register-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0.75rem 2rem;
  background: var(--pb-color-brand);
  color: white;
  border: none;
  border-radius: var(--pb-radius-xs);
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.15s;
}

.register-btn:hover:not(:disabled) {
  background: var(--pb-color-brand-strong);
}

.register-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.preview-content {
  padding: 1.5rem;
  height: calc(100% - 80px);
  overflow-y: auto;
}

.book-cover-container {
  margin-bottom: 2rem;
}

.book-cover-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
  position: relative;
  cursor: pointer;
}

.book-cover {
  position: relative;
  max-width: 250px;
  width: 100%;
  border-radius: var(--pb-radius-sm);
  overflow: hidden;
  box-shadow: var(--pb-shadow-md);
}

.cover-image {
  width: 100%;
  height: auto;
  display: block;
  border-radius: var(--pb-radius-sm);
}

.no-image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  color: var(--pb-color-text-muted);
  text-align: center;
  border: 2px dashed var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  min-height: 300px;
}

.no-image-placeholder svg {
  margin-bottom: 1rem;
  opacity: 0.5;
}

.no-image-placeholder p {
  font-size: 1.1rem;
  font-weight: 500;
  margin-bottom: 0.5rem;
  color: var(--pb-color-text);
}

.no-image-placeholder span {
  font-size: 0.9rem;
  opacity: 0.7;
}

.book-info-summary {
  background: var(--pb-color-surface);
  border-radius: var(--pb-radius-sm);
  padding: 1.5rem;
  border: 1px solid var(--pb-color-border);
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid var(--pb-color-surface-subtle);
}

.info-item:last-child {
  border-bottom: none;
}

.info-item label {
  font-weight: 500;
  color: var(--pb-color-text-muted);
  min-width: 80px;
}

.info-item span {
  color: var(--pb-color-text);
  text-align: right;
  word-break: break-word;
  flex: 1;
  margin-left: 1rem;
}

@media (max-width: 1024px) {
  .register-content { grid-template-columns: 1fr; gap: 1.5rem; }
  .form-section { order: 1; }
  .preview-section { order: 2; }
  .section-card { height: auto; }
  .form-content, .preview-content { height: auto; }
}

@media (max-width: 768px) {
  .card-header { padding: 1rem; }
  .form-content, .preview-content { padding: 1rem; }
  .book-cover-wrapper { min-height: 250px; }
  .no-image-placeholder { min-height: 250px; padding: 1.5rem; }
  .form-actions { flex-direction: column; }
  .cancel-btn, .register-btn { width: 100%; justify-content: center; }
}

@media (max-width: 480px) {
  .isbn-input-wrapper { flex-direction: column; }
  .isbn-input-wrapper .form-input { border-radius: var(--pb-radius-xs); border-right: 1px solid var(--pb-color-border); margin-bottom: 0.5rem; }
  .search-btn { border-radius: var(--pb-radius-xs); justify-content: center; }
  .info-item { flex-direction: column; align-items: flex-start; gap: 0.25rem; }
  .info-item span { text-align: left; margin-left: 0; }
}
</style>