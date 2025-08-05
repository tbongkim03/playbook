<template>
    <div class="book-register-container">
        <div class="register-header">
            <h1 class="page-title">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M4 19.5C4 18.1193 5.11929 17 6.5 17H20" stroke="currentColor" stroke-width="2"/>
                    <path d="M6.5 2H20V22H6.5C5.11929 22 4 20.8807 4 19.5V4.5C4 3.11929 5.11929 2 6.5 2Z" stroke="currentColor" stroke-width="2"/>
                </svg>
                도서 등록
            </h1>
            <p class="page-subtitle">새로운 도서를 등록하고 관리하세요</p>
        </div>

        <div class="register-content">
            <!-- 왼쪽 폼 영역 -->
            <div class="form-section">
                <div class="section-card">
                    <div class="card-header">
                        <h2 class="card-title">도서 정보</h2>
                        <router-link to="/books" custom v-slot="{ navigate }">
                            <button class="back-btn" type="button" @click="navigate">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M19 12H5" stroke="currentColor" stroke-width="2"/>
                                    <path d="M12 19L5 12L12 5" stroke="currentColor" stroke-width="2"/>
                                </svg>
                                뒤로가기
                            </button>
                        </router-link>
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
                                />
                                <button class="search-btn" type="button" @click="searchISBN">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
                                        <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2"/>
                                    </svg>
                                    조회
                                </button>
                            </div>
                        </div>

                        <!-- 도서 제목 -->
                        <div class="form-group">
                            <label class="form-label">도서 제목</label>
                            <input 
                                type="text" 
                                class="form-input" 
                                v-model="book.title" 
                                @keydown="blockJavascriptInput" 
                                @keydown.space.prevent 
                                placeholder="도서 제목을 입력하세요"
                            />
                        </div>

                        <!-- 저자 -->
                        <div class="form-group">
                            <label class="form-label">저자</label>
                            <input 
                                type="text" 
                                class="form-input" 
                                v-model="book.author" 
                                @keydown="blockJavascriptInput" 
                                @keydown.space.prevent 
                                placeholder="저자를 입력하세요"
                            />
                        </div>

                        <!-- 출판사 -->
                        <div class="form-group">
                            <label class="form-label">출판사</label>
                            <input 
                                type="text" 
                                class="form-input" 
                                v-model="book.publisher" 
                                @keydown="blockJavascriptInput" 
                                @keydown.space.prevent 
                                placeholder="출판사를 입력하세요"
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
                                @keydown.space.prevent 
                            />
                        </div>

                        <!-- 등록 버튼 -->
                        <div class="form-actions">
                            <button class="register-btn" @click="submitBook" :disabled="isLoading">
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
import { reactive, ref } from 'vue'

const bookImg = ref(null)
const overLay = ref(null)
const isLoading = ref(false)

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

// JavaScript 입력 차단 함수 (기존 로직 유지)
function blockJavascriptInput(e) {
    // 기존 로직을 여기에 추가하세요
}

async function searchISBN() {
    const apiKey = import.meta.env.VITE_NL_API_KEY

    const isbn = book.isbn

    if (!isbn) {
        alert('ISBN을 입력해주세요.')
        return
    }

    isLoading.value = true

    try {
        const url = `https://www.nl.go.kr/seoji/SearchApi.do?cert_key=${apiKey}&result_style=json&page_no=1&page_size=1&isbn=${isbn}`
        const res = await fetch(url)
        const data = await res.json()

        const doc = data?.docs?.[0] || null
        console.log('조회된 도서 정보:', doc)

        if (!doc) {
            // 이전 정보 초기화
            book.isbn = ''
            book.title = ''
            book.author = ''
            book.publisher = ''
            book.publishDate = ''
            book.title_url = ''
            alert('도서 정보를 찾을 수 없습니다.')
            return
        }

        book.title = doc['TITLE'] || ''
        book.author = doc['AUTHOR'] || ''
        book.publisher = doc['PUBLISHER'] || ''
        book.publishDate = formatDate(doc['PUBLISH_PREDATE'] || '')
        book.title_url = doc['TITLE_URL'] || ''

    } catch (err) {
        console.error('API 조회 실패:', err)
        alert('도서 정보를 조회하는 중 오류가 발생했습니다.')
    } finally {
        isLoading.value = false
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
    if (!book.title || !book.author || !book.publisher) {
        alert('필수 정보를 모두 입력해주세요.')
        return
    }

    const token = localStorage.getItem('jwtToken')
    
    const payload = {
        seqSortSecond: 0,
        isbnBook: book.isbn,
        titleBook: book.title,
        authorBook: book.author,
        publisherBook: book.publisher,
        publishDateBook: book.publishDate,
        imageBook: book.title_url
    }

    try {
        isLoading.value = true
        
        const response = await fetch('http://localhost:8080/books', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${token}`
            },
            body: JSON.stringify(payload)
        })

        const contentType = response.headers.get('content-type')
        
        if (!response.ok) {
            const errorText = await response.text()
            throw new Error(errorText || `서버 오류: ${response.status}`)
        }

        let data
        if (contentType && contentType.includes('application/json')) {
            data = await response.json()
        } else {
            const text = await response.text()
            if (!text) throw new Error('알 수 없는 응답입니다.')
            data = { titleBook: book.title }
        }

        alert(`도서 "${data.titleBook || book.title}"가 성공적으로 등록되었습니다!`)
        
        // 폼 초기화
        Object.keys(book).forEach(key => {
            book[key] = ''
        })
        
    } catch (err) {
        console.error('도서 등록 실패:', err)
        alert(`등록 실패: ${err.message}`)
    } finally {
        isLoading.value = false
    }
}

function useMouse(e) {
    if (!book.title_url || !bookImg.value) return
    
    const rect = bookImg.value.getBoundingClientRect()
    const x = e.clientX - rect.left
    const y = e.clientY - rect.top
    
    const rotateY = (x / rect.width - 0.5) * 20
    const rotateX = (0.5 - y / rect.height) * 20

    if (overLay.value) {
        overLay.value.style.backgroundPosition = `${x/4 + y/4}%`
    }
    
    bookImg.value.style.transform = `perspective(600px) rotateY(${rotateY}deg) rotateX(${rotateX}deg)`
    bookImg.value.style.transition = 'none'
}

function resetTransform() {
    if (!bookImg.value) return
    
    if (overLay.value) {
        overLay.value.style.opacity = '0'
    }
    
    bookImg.value.style.transform = 'perspective(600px) rotateY(0deg) rotateX(0deg)'
    bookImg.value.style.transition = 'transform 0.3s ease'
    
    setTimeout(() => {
        if (overLay.value) {
            overLay.value.style.opacity = ''
        }
    }, 300)
}
</script>

<style scoped>
.book-register-container {
  min-height: 100vh;
  background: #f8f9fa;
  padding: 80px 0 40px;
  margin-top: -5rem;
}

.register-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 0 20px;
}

.page-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-size: 2.5rem;
  font-weight: 700;
  color: #212529;
  margin-bottom: 8px;
}

.page-subtitle {
  font-size: 1.1rem;
  color: #6c757d;
  margin: 0;
}

.register-content {
  width: 98%;
  margin: 0 auto;
  padding: 0.7rem;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  max-width: none;
}

.form-section,
.preview-section {
  min-width: 0;
}

.section-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
  border: 1px solid #dee2e6;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #dee2e6;
  background: #f8f9fa;
}

.card-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #212529;
  margin: 0;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0.5rem 1rem;
  background: transparent;
  color: #007bff;
  border: 1px solid #007bff;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: #007bff;
  color: white;
}

.form-content {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #495057;
}

.form-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 1rem;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
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
  background: #6c757d;
  color: white;
  border: 1px solid #6c757d;
  border-top-right-radius: 6px;
  border-bottom-right-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.search-btn:hover {
  background: #5a6268;
  border-color: #5a6268;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 2rem;
}

.register-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0.75rem 2rem;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.register-btn:hover:not(:disabled) {
  background: #0056b3;
  transform: translateY(-1px);
}

.register-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 미리보기 섹션 */
.preview-content {
  padding: 1.5rem;
}

.book-cover-container {
  margin-bottom: 2rem;
}

.book-cover-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  position: relative;
  cursor: pointer;
}

.book-cover {
  position: relative;
  max-width: 300px;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.cover-image {
  width: 100%;
  height: auto;
  display: block;
  border-radius: 8px;
}

.book-cover-wrapper:hover {
  /* opacity: 1; */
}

.no-image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  color: #6c757d;
  text-align: center;
  border: 2px dashed #dee2e6;
  border-radius: 8px;
  min-height: 400px;
}

.no-image-placeholder svg {
  margin-bottom: 1rem;
  opacity: 0.5;
}

.no-image-placeholder p {
  font-size: 1.1rem;
  font-weight: 500;
  margin-bottom: 0.5rem;
  color: #495057;
}

.no-image-placeholder span {
  font-size: 0.9rem;
  opacity: 0.7;
}

.book-info-summary {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1.5rem;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #e9ecef;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item label {
  font-weight: 500;
  color: #6c757d;
  min-width: 80px;
}

.info-item span {
  color: #495057;
  text-align: right;
  word-break: break-word;
}

/* 반응형 디자인 */
@media (max-width: 1024px) {
  .register-content {
    grid-template-columns: 1fr;
    gap: 24px;
  }
  
  .form-section {
    order: 1;
  }
  
  .preview-section {
    order: 2;
  }
}

@media (max-width: 768px) {
  .register-content {
    width: 100%;
    padding: 0 10px;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .card-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .form-content,
  .preview-content {
    padding: 1rem;
  }
  
  .book-cover-wrapper {
    min-height: 300px;
  }
  
  .no-image-placeholder {
    min-height: 300px;
    padding: 2rem;
  }
}

@media (max-width: 480px) {
  .isbn-input-wrapper {
    flex-direction: column;
  }
  
  .isbn-input-wrapper .form-input {
    border-radius: 6px;
    border-right: 1px solid #ced4da;
    margin-bottom: 0.5rem;
  }
  
  .search-btn {
    border-radius: 6px;
    justify-content: center;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
  
  .info-item span {
    text-align: left;
  }
}
</style>