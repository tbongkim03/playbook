<template>
  <div class="modal-overlay" @click="close">
    <div class="modal-container" @click.stop>
      <!-- 모달 헤더 -->
      <div class="modal-header">
        <div class="header-content">
          <div class="header-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="3" y="4" width="4" height="16" stroke="currentColor" stroke-width="2"/>
              <rect x="9" y="4" width="2" height="16" stroke="currentColor" stroke-width="2"/>
              <rect x="13" y="4" width="2" height="16" stroke="currentColor" stroke-width="2"/>
              <rect x="17" y="4" width="4" height="16" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <div class="header-text">
            <h2 class="modal-title">바코드 출력</h2>
            <p class="modal-subtitle">
              미출력 도서의 바코드를 출력합니다
            </p>
          </div>
        </div>
        <button class="close-btn" @click="close">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2"/>
            <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2"/>
          </svg>
        </button>
      </div>
      <!-- 설정 영역 -->
      <div class="settings-section">
        <div class="settings-grid">
          <div class="setting-group">
            <label for="countSelect" class="setting-label">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M16 4H18C18.5304 4 19.0391 4.21071 19.4142 4.58579C19.7893 4.96086 20 5.46957 20 6V18C20 18.5304 19.7893 19.0391 19.4142 19.4142C19.0391 19.7893 18.5304 20 18 20H6C5.46957 20 4.96086 19.7893 3.58579 19.4142C3.21071 19.0391 3 18.5304 3 18V6C3 5.46957 3.21071 4.96086 3.58579 4.58579C3.96086 4.21071 4.46957 4 6 4H8" stroke="currentColor" stroke-width="2"/>
                <rect x="8" y="2" width="8" height="4" rx="1" ry="1" stroke="currentColor" stroke-width="2"/>
              </svg>
              출력 수량
            </label>
            <select id="countSelect" v-model="selectedCountPerPage" class="setting-select">
              <option v-for="option in options" :key="option" :value="option">
                {{ option }}개
              </option>
            </select>
          </div>

          <div class="setting-group">
            <label for="startPosition" class="setting-label">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M21 10C21 16.0751 16.0751 21 10 21C4.44772 21 0 16.5523 0 11C0 5.44772 4.44772 1 10 1C15.5228 1 20 5.44772 20 11" stroke="currentColor" stroke-width="2"/>
                <circle cx="10" cy="11" r="3" stroke="currentColor" stroke-width="2"/>
              </svg>
              시작 위치
            </label>
            <select id="startPosition" v-model="startPosition" class="setting-select">
              <option v-for="n in 65" :key="n" :value="n">{{ n }}번째</option>
            </select>
          </div>
        </div>

        <!-- 통계 정보 -->
        <div class="stats-info">
          <div class="stat-item">
            <span class="stat-label">전체 미출력</span>
            <span class="stat-value">{{ props.books.length }}개</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">필터링 후</span>
            <span class="stat-value">{{ props.books.length }}개</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">출력 예정</span>
            <span class="stat-value">{{ displayedBooks.length }}개</span>
          </div>
        </div>
      </div>

      <!-- 미리보기 영역 -->
      <div class="preview-section">
        <div class="preview-header">
          <h3 class="preview-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M1 12S5 4 12 4S23 12 23 12S19 20 12 20S1 12 1 12Z" stroke="currentColor" stroke-width="2"/>
              <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
            </svg>
            미리보기
          </h3>
          <span class="preview-count">{{ displayedBooks.length }}개 항목</span>
        </div>

        <div class="preview-content">
          <div class="barcode-grid" v-if="displayedBooks.length > 0">
            <div 
              class="barcode-card" 
              v-for="book in displayedBooks" 
              :key="book.seqBook"
            >
              <div class="barcode-container">
                <svg ref="barcodeSvgs" :data-code="book.barcodeBook" class="barcode-svg"></svg>
              </div>
              <div class="barcode-info">
                <div class="barcode-code">{{ book.barcodeBook }}</div>
                <div class="barcode-title">{{ book.titleBook }}</div>
              </div>
            </div>
          </div>

          <!-- 빈 상태 -->
          <div v-else class="empty-state">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="3" y="4" width="4" height="16" stroke="currentColor" stroke-width="2"/>
              <rect x="9" y="4" width="2" height="16" stroke="currentColor" stroke-width="2"/>
              <rect x="13" y="4" width="2" height="16" stroke="currentColor" stroke-width="2"/>
              <rect x="17" y="4" width="4" height="16" stroke="currentColor" stroke-width="2"/>
            </svg>
            <h4>출력할 바코드가 없습니다</h4>
            <p>
              미출력 도서가 없습니다.
            </p>
          </div>
        </div>
      </div>

      <!-- 액션 버튼 -->
      <div class="modal-actions">
        <button class="action-btn cancel-btn" @click="close">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2"/>
            <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2"/>
          </svg>
          취소
        </button>
        <button 
          class="action-btn print-btn" 
          @click="printAll"
          :disabled="displayedBooks.length === 0"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <polyline points="6,9 6,2 18,2 18,9" stroke="currentColor" stroke-width="2"/>
            <path d="M6,18H4C3.46957,18 2.96086,17.7893 2.58579,17.4142C2.21071,17.0391 2,16.5304 2,16V11C2,10.4696 2.21071,9.96086 2.58579,9.58579C2.96086,9.21071 3.46957,9 4,9H20C20.5304,9 21.0391,9.21071 21.4142,9.58579C21.7893,9.96086 22,10.4696 22,11V16C22,16.5304 21.7893,17.0391 21.4142,17.4142C21.0391,17.7893 20.5304,18 20,18H18" stroke="currentColor" stroke-width="2"/>
            <rect x="6" y="14" width="12" height="8" stroke="currentColor" stroke-width="2"/>
          </svg>
          출력하기
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted, nextTick } from 'vue'
import JsBarcode from 'jsbarcode'

const props = defineProps({
  books: {
    type: Array,
    default: () => []
  }
})

const startPosition = ref(1)

const emit = defineEmits(['close', 'refresh'])
function close() {
  emit('close')
}

// 출력 시 사용자가 선택하는 바코드 수
const options = ref([])
const selectedCountPerPage = ref(1)

// 보여줄 책 슬라이스 - props.books에서 직접 슬라이스 (최대 65개)
const displayedBooks = computed(() => {
  const maxCount = Math.min(selectedCountPerPage.value, 65)
  return props.books.slice(0, maxCount)
})

// 옵션 업데이트 함수
const updateOptions = () => {
  options.value = []
  const maxCount = Math.min(props.books.length, 65) // 최대 65개
  for (let i = 1; i <= maxCount; i++) {
    options.value.push(i)
  }
  selectedCountPerPage.value = maxCount > 0 ? maxCount : 1
}

// 바코드 생성
const barcodeSvgs = ref([])

const generateBarcodes = () => {
  nextTick(() => {
    barcodeSvgs.value.forEach(svg => {
      const code = svg.dataset.code
      JsBarcode(svg, code, {
        format: "CODE128",
        lineColor: "#000",
        width: 2,
        height: 40,
        displayValue: false,
        fontSize: 12
      })
    })
  })
}

// props.books가 변경되면 옵션 업데이트
watch(() => props.books, () => {
  updateOptions()
  generateBarcodes()
}, { immediate: true })

// 데이터가 변경되거나 선택 수가 바뀌면 바코드 다시 생성
watch([() => selectedCountPerPage.value], () => {
  generateBarcodes()
})

// 컴포넌트 마운트 시 바코드 생성
onMounted(() => {
  generateBarcodes()
})

// 바코드 SVG 생성 헬퍼 함수
function createBarcodeSVG(book) {
  const tempSvg = document.createElementNS("http://www.w3.org/2000/svg", "svg")
  JsBarcode(tempSvg, book.barcodeBook, {
    format: "CODE128",
    lineColor: "#000",
    width: 1,
    height: 40,
    displayValue: false,
  })
  
  return `
    <div class="barcode-cell">
      <div class="barcode-content">
        ${tempSvg.outerHTML}
        <div class="barcode-label">${book.barcodeBook}</div>
        <div class="book-title">${book.titleBook}</div>
      </div>
    </div>
  `
}

// 한 페이지의 박스들을 행으로 나누는 함수
function createPageRows(boxes) {
  const rows = []
  for (let i = 0; i < 13; i++) {
    const rowBoxes = boxes.slice(i * 5, (i + 1) * 5)
    rows.push(`<div class="row">${rowBoxes.join('')}</div>`)
  }
  return rows.join('')
}

// 출력 함수
const printAll = async () => {
  if (!displayedBooks.value.length) {
    alert('출력할 바코드가 없습니다.')
    return
  }

  const printWindow = window.open('', '', 'width=1000,height=600') 

  if (!printWindow) {
    alert("팝업 차단을 해제해 주세요")
    return
  }

  // 13행 × 5열 = 65개의 박스
  const totalBoxesPerPage = 65
  const pages = []
  
  let startPos = startPosition.value // 1~65 (1~65번째 위치)
  let bookIndex = 0
  const totalBooks = displayedBooks.value.length

  // 현재 페이지의 박스들
  let currentPageBoxes = []

  // 첫 페이지에 시작 위치 전까지 빈 박스 추가 (1번째부터 시작하므로 startPos - 1개)
  for (let i = 0; i < startPos - 1; i++) {
    currentPageBoxes.push('<div class="barcode-cell"></div>')
  }

  // 바코드 추가
  while (bookIndex < totalBooks) {
    // 현재 페이지가 가득 찼으면 새 페이지 시작
    if (currentPageBoxes.length >= totalBoxesPerPage) {
      // 현재 페이지를 완성하고 저장
      while (currentPageBoxes.length < totalBoxesPerPage) {
        currentPageBoxes.push('<div class="barcode-cell"></div>')
      }
      pages.push([...currentPageBoxes])
      currentPageBoxes = []
    }

    // 바코드 추가
    const book = displayedBooks.value[bookIndex]
    currentPageBoxes.push(createBarcodeSVG(book))
    bookIndex++
  }

  // 마지막 페이지 완성
  if (currentPageBoxes.length > 0) {
    while (currentPageBoxes.length < totalBoxesPerPage) {
      currentPageBoxes.push('<div class="barcode-cell"></div>')
    }
    pages.push(currentPageBoxes)
  }

  // 페이지 HTML 생성
  const pageHTMLs = pages.map((pageBoxes, index) => {
    const rows = createPageRows(pageBoxes)
    return `
      <div class="page-break">
        <div class="grid">
          ${rows}
        </div>
      </div>
    `
  })

  const doc = printWindow.document
  doc.open()
  doc.write(`
    <!DOCTYPE html>
    <html lang="ko">
    <head>
      <meta charset="UTF-8">
      <title>바코드 출력</title>
      <style>
        @page {
          size: A4;
          margin: 0;
        }
        body {
          margin: 0;
          padding: 0;
          font-family: Arial, sans-serif;
        }
        .page-break {
          page-break-after: always;
        }
        .page-break:last-child {
          page-break-after: auto;
        }
        .grid {
          position: relative;
          width: calc(210mm - 1cm);
          height: calc(297mm - 1.9cm);
          margin: 1cm 0.5cm 0.9cm 0.4cm;
          box-sizing: border-box;
          display: flex;
          flex-direction: column;
          justify-content: space-between;
        }
        .row {
          display: flex;
          justify-content: space-between;
        }
        .barcode-cell {
          width: 38.1mm;
          height: 21.2mm;
          // border: 1px solid #ddd;
          box-sizing: border-box;
          margin: 0 0.1335cm;
          display: flex;
          align-items: center;
          justify-content: center;
          overflow: hidden;
        }
        .row .barcode-cell:first-child {
          margin-left: 0;
        }
        .row .barcode-cell:last-child {
          margin-right: 0;
        }
        .barcode-content {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          width: 100%;
          height: 100%;
          padding: 1mm;
        }
        svg {
          width: 32mm;
          height: auto;
          margin-bottom: 0.5mm;
        }
        .barcode-label {
          font-size: 6px;
          font-weight: bold;
          text-align: center;
          margin-bottom: 0.5mm;
          font-family: 'Courier New', monospace;
        }
        .book-title {
          font-size: 4px;
          text-align: center;
          line-height: 1.1;
          word-break: break-word;
          overflow: hidden;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }
      </style>
    </head>
    <body>
      ${pageHTMLs.join('')}
      <script>
        window.onload = function() {
          window.print();
        };
      </` + `script>
    </body>
    </html>
  `)
  doc.close()

  try {
    const ids = displayedBooks.value.map(book => book.seqBook)

    const res = await fetch('/api/books/batch/print', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(ids)
    })

    if (!res.ok) {
      const errorMessage = await res.text()
      throw new Error(errorMessage || `서버 오류: ${res.status}`)
    }

    alert('인쇄 완료 상태로 저장되었습니다.')

    // 부모 컴포넌트에 새로고침 이벤트 발생
    emit('refresh')

    // 바코드 다시 생성
    generateBarcodes()
  } catch (error) {
    alert('저장에 실패했습니다.', error)
  }
}
</script>

<style scoped>
/* 기존 스타일 유지 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-container {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from { 
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to { 
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 필터 정보 섹션 추가 */
.filter-info {
  padding: 1rem 1.5rem;
  background: linear-gradient(135deg, #e3f2fd 0%, #f1f8e9 100%);
  border-bottom: 1px solid #e9ecef;
}

.filter-badge-container {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.filter-label {
  font-weight: 600;
  color: #1976d2;
  font-size: 0.9rem;
  white-space: nowrap;
}

.filter-badges {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.filter-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  white-space: nowrap;
}

.filter-badge.search {
  background: linear-gradient(135deg, #bbdefb 0%, #e1f5fe 100%);
  color: #0d47a1;
  border: 1px solid #90caf9;
}

.filter-badge.category {
  background: linear-gradient(135deg, #c8e6c9 0%, #e8f5e8 100%);
  color: #2e7d32;
  border: 1px solid #a5d6a7;
}

/* 모달 헤더 */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
  background: #f8f9fa;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.header-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: #007bff;
  color: white;
  border-radius: 8px;
}

.header-text {
  flex: 1;
}

.modal-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #212529;
  margin: 0 0 0.25rem 0;
}

.modal-subtitle {
  font-size: 0.9rem;
  color: #6c757d;
  margin: 0;
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: none;
  background: rgba(108, 117, 125, 0.1);
  color: #6c757d;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(108, 117, 125, 0.2);
  color: #495057;
}

/* 설정 섹션 */
.settings-section {
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
  background: white;
}

.settings-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.setting-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.setting-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: #495057;
  font-size: 0.9rem;
}

.setting-select {
  padding: 0.75rem 1rem;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 0.9rem;
  background: white;
  transition: all 0.3s ease;
}

.setting-select:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.stats-info {
  display: flex;
  gap: 2rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
}

.stat-label {
  font-size: 0.8rem;
  color: #6c757d;
  font-weight: 500;
}

.stat-value {
  font-size: 1.25rem;
  font-weight: 700;
  color: #007bff;
}

/* 미리보기 섹션 */
.preview-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #e9ecef;
  background: white;
}

.preview-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.1rem;
  font-weight: 600;
  color: #495057;
  margin: 0;
}

.preview-count {
  font-size: 0.9rem;
  color: #6c757d;
}

.preview-content {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem;
  background: #f8f9fa;
}

.barcode-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1rem;
}

.barcode-card {
  background: white;
  border-radius: 8px;
  padding: 1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e9ecef;
  transition: all 0.3s ease;
}

.barcode-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.barcode-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0.5rem;
  background: #f8f9fa;
  border-radius: 6px;
  margin-bottom: 0.75rem;
}

.barcode-svg {
  max-width: 100%;
  height: auto;
}

.barcode-info {
  text-align: center;
}

.barcode-code {
  font-size: 0.85rem;
  font-weight: 600;
  color: #495057;
  font-family: 'Courier New', monospace;
  margin-bottom: 0.25rem;
}

.barcode-title {
  font-size: 0.8rem;
  color: #6c757d;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 빈 상태 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  text-align: center;
  color: #6c757d;
}

.empty-state svg {
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-state h4 {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #495057;
}

.empty-state p {
  margin: 0;
  opacity: 0.8;
}

/* 액션 버튼 */
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid #e9ecef;
  background: white;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.cancel-btn {
  background: #6c757d;
  color: white;
}

.cancel-btn:hover {
  background: #5a6268;
  transform: translateY(-1px);
}

.print-btn {
  background: #007bff;
  color: white;
}

.print-btn:hover:not(:disabled) {
  background: #0056b3;
  transform: translateY(-1px);
}

.print-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .modal-container {
    width: 95%;
    max-height: 95vh;
  }
  
  .modal-header {
    padding: 1rem;
  }
  
  .header-icon {
    width: 40px;
    height: 40px;
  }
  
  .modal-title {
    font-size: 1.25rem;
  }

  .filter-info {
    padding: 0.75rem 1rem;
  }

  .filter-badge-container {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .filter-badges {
    width: 100%;
  }
  
  .settings-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .stats-info {
    gap: 1rem;
  }
  
  .barcode-grid {
    grid-template-columns: 1fr;
  }
  
  .modal-actions {
    flex-direction: column;
  }
  
  .action-btn {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .modal-container {
    width: 100%;
    height: 100vh;
    max-height: none;
    border-radius: 0;
  }
  
  .header-content {
    gap: 0.75rem;
  }

  .filter-badges {
    flex-direction: column;
  }

  .filter-badge {
    align-self: flex-start;
  }
  
  .settings-section,
  .preview-header,
  .preview-content,
  .modal-actions {
    padding: 1rem;
  }
}
</style>