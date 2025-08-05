<template>
  <div class="modal-overlay" v-if="isOpen === true" @click="close">
    <div class="modal-container" @click.stop>
      <!-- 모달 헤더 -->
      <div class="modal-header">
        <div class="header-content">
          <div class="header-icon" :class="{ 'error': isD, 'success': !isD && showBarcode }">
            <svg v-if="isD" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2"/>
              <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2"/>
            </svg>
            <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="3" y="4" width="4" height="16" stroke="currentColor" stroke-width="2"/>
              <rect x="9" y="4" width="2" height="16" stroke="currentColor" stroke-width="2"/>
              <rect x="13" y="4" width="2" height="16" stroke="currentColor" stroke-width="2"/>
              <rect x="17" y="4" width="4" height="16" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <div class="header-text">
            <h2 class="modal-title">바코드 출력</h2>
            <p class="book-title">{{ titleBook }}</p>
          </div>
        </div>
        <button class="close-btn" @click="close">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2"/>
            <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2"/>
          </svg>
        </button>
      </div>

      <!-- 상태 메시지 -->
      <div class="status-section">
        <div class="status-message" :class="{ 'error': isD, 'success': !isD && showBarcode, 'loading': !msg }">
          <div class="status-icon">
            <svg v-if="isD" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M10.29 3.86L1.82 18C1.64466 18.3024 1.55298 18.6453 1.55298 18.995C1.55298 19.3447 1.64466 19.6876 1.82 19.99C1.99534 20.2924 2.24708 20.5441 2.55 20.72C2.85292 20.8959 3.19596 20.9896 3.546 20.99H20.454C20.804 20.9896 21.1471 20.8959 21.45 20.72C21.7529 20.5441 22.0047 20.2924 22.18 19.99C22.3553 19.6876 22.447 19.3447 22.447 18.995C22.447 18.6453 22.3553 18.3024 22.18 18L13.71 3.86C13.5347 3.55764 13.2829 3.30596 12.98 3.13C12.6771 2.95404 12.3341 2.86035 11.984 2.86035C11.6339 2.86035 11.2909 2.95404 10.988 3.13C10.6851 3.30596 10.4333 3.55764 10.258 3.86H10.29Z" stroke="currentColor" stroke-width="2"/>
              <line x1="12" y1="9" x2="12" y2="13" stroke="currentColor" stroke-width="2"/>
              <dot cx="12" cy="17" r="1" fill="currentColor"/>
            </svg>
            <svg v-else-if="showBarcode" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <polyline points="20,6 9,17 4,12" stroke="currentColor" stroke-width="2"/>
            </svg>
            <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
              <path d="M12 1V3" stroke="currentColor" stroke-width="2"/>
              <path d="M12 21V23" stroke="currentColor" stroke-width="2"/>
              <path d="M4.22 4.22L5.64 5.64" stroke="currentColor" stroke-width="2"/>
              <path d="M18.36 18.36L19.78 19.78" stroke="currentColor" stroke-width="2"/>
              <path d="M1 12H3" stroke="currentColor" stroke-width="2"/>
              <path d="M21 12H23" stroke="currentColor" stroke-width="2"/>
              <path d="M4.22 19.78L5.64 18.36" stroke="currentColor" stroke-width="2"/>
              <path d="M18.36 5.64L19.78 4.22" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <span class="status-text">{{ msg || '바코드 검증 중...' }}</span>
        </div>
      </div>

      <!-- 바코드 정보 -->
      <div class="barcode-info" v-if="barcodeBook">
        <div class="info-row">
          <span class="info-label">바코드</span>
          <span class="info-value barcode-text">{{ barcodeBook }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">수량</span>
          <span class="info-value">{{ cntBook }}권</span>
        </div>
      </div>

      <!-- 바코드 미리보기 -->
      <div class="barcode-preview" v-if="showBarcode">
        <div class="preview-label">바코드 미리보기</div>
        <div class="barcode-container">
          <svg ref="barcodeSvg" class="barcode-svg"></svg>
        </div>
      </div>

      <!-- 액션 버튼 -->
      <div class="modal-actions">
        <div class="action-buttons" v-if="!isD">
          <button 
            class="action-btn save-btn" 
            @click="saveBook"
            :disabled="buttonsDisabled"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16L21 8V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21Z" stroke="currentColor" stroke-width="2"/>
              <polyline points="17,21 17,13 7,13 7,21" stroke="currentColor" stroke-width="2"/>
              <polyline points="7,3 7,8 15,8" stroke="currentColor" stroke-width="2"/>
            </svg>
            나중에 출력
          </button>
          <button 
            class="action-btn print-btn" 
            @click="printBarcode"
            :disabled="buttonsDisabled"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <polyline points="6,9 6,2 18,2 18,9" stroke="currentColor" stroke-width="2"/>
              <path d="M6,18H4C3.46957,18 2.96086,17.7893 2.58579,17.4142C2.21071,17.0391 2,16.5304 2,16V11C2,10.4696 2.21071,9.96086 2.58579,9.58579C2.96086,9.21071 3.46957,9 4,9H20C20.5304,9 21.0391,9.21071 21.4142,9.58579C21.7893,9.96086 22,10.4696 22,11V16C22,16.5304 21.7893,17.0391 21.4142,17.4142C21.0391,17.7893 20.5304,18 20,18H18" stroke="currentColor" stroke-width="2"/>
              <rect x="6" y="14" width="12" height="8" stroke="currentColor" stroke-width="2"/>
            </svg>
            출력 및 저장
          </button>
        </div>
        
        <button class="action-btn close-btn-bottom" @click="close">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2"/>
            <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2"/>
          </svg>
          닫기
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, nextTick } from 'vue'
import JsBarcode from 'jsbarcode'

const props = defineProps({
  seqBook: Number,
  seqSortSecond: Number,
  cntBook: Number,
  barcodeBook: String,
  titleBook: String,
  isOpen: Boolean
})

const emit = defineEmits(['close'])

const msg = ref('')
const isD = ref(false)
const buttonsDisabled = ref(false)
const showBarcode = ref(false)
const token = localStorage.getItem('jwtToken')

function close() {
  emit('close')
}

const barcodeSvg = ref(null)

// 바코드 생성
const generateBarcode = () => {
  if (barcodeSvg.value && props.barcodeBook) {
    JsBarcode(barcodeSvg.value, props.barcodeBook, {
      format: "CODE128",
      lineColor: "#000",
      width: 2,
      height: 50,
      displayValue: true,
      fontSize: 14,
      text: `${props.barcodeBook}`,
    })
  }
}

const uniqueTest = async () => {
  try {
    const response = await fetch(`http://localhost:8080/books/check/barcode`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify({
        seqBook: props.seqBook,
        barcodeBook: props.barcodeBook
      })
    })

    if (!response.ok) {
      const errorMessage = await response.text()
      throw new Error(errorMessage || `서버 오류: ${response.status}`)
    }

    const result = await response.json()
    
    isD.value = result.duplicated
    msg.value = result.message

    if (isD.value === false) {
      // 사용 가능: 바코드 생성하고 버튼 활성화
      showBarcode.value = true
      buttonsDisabled.value = false

      await nextTick()
      generateBarcode()

    } else if (isD.value === true) {
      // 중복: 바코드 숨기고 버튼 비활성화
      showBarcode.value = false
      buttonsDisabled.value = true
    }

  } catch (error) {
    msg.value = `오류: ${error.message}`
    isD.value = true
    showBarcode.value = false
    buttonsDisabled.value = true
  }
}

// 모달이 열릴 때 렌더 후 생성
watch(() => props.isOpen, async (newVal) => {
  if (newVal) {
    msg.value = ''
    isD.value = false
    buttonsDisabled.value = false
    showBarcode.value = false

    await nextTick()
    await uniqueTest()
  }
})

// 마운트 시 생성
onMounted(async () => {
  if (props.isOpen) {
    await nextTick()
    await uniqueTest()
  }
})

// 나중에 출력(저장만)
const saveBook = () => {
  if (isD.value === true) {
    alert("🚫 중복된 바코드입니다. 저장할 수 없습니다.")
    return
  }
  
  postPrintedBook(false)
}

// 개별 출력 및 저장
const printBarcode = () => {
  if (isD.value === true) {
    alert("🚫 중복된 바코드입니다. 출력할 수 없습니다.")
    return
  }

  if (!barcodeSvg.value) {
    alert("바코드가 아직 생성되지 않았습니다")
    return
  }

  const printWindow = window.open('', '', 'width=1000,height=600')
  if (!printWindow) {
    alert("팝업 차단을 해제해 주세요")
    return
  }

  const doc = printWindow.document
  doc.open()
  doc.write(`
    <!DOCTYPE html>
    <html>
      <head>
        <meta charset="UTF-8">
        <title>바코드 출력 - ${props.titleBook}</title>
        <style>
          body { 
            font-family: sans-serif; 
            margin: 20px;
            text-align: center;
          }
          .barcode-container {
            margin: 20px 0;
          }
          svg { 
            width: auto; 
            height: 60px; 
          }
          .book-info {
            margin-top: 20px;
            font-size: 14px;
            color: #333;
          }
        </style>
      </head>
      <body>
        <div class="barcode-container">
          ${barcodeSvg.value.outerHTML}
        </div>
        <div class="book-info">
          <strong>${props.titleBook}</strong><br>
          수량: ${props.cntBook}권
        </div>
        <script>
          window.onload = function() {
            window.print();
          }
        </` + `script>
      </body>
    </html>
  `)
  doc.close()

  postPrintedBook(true)
}

// POST 함수 (printCheckBook을 매개변수로)
const postPrintedBook = async (printCheckBook) => {
  try {
    const id = props.seqBook

    if (!id) {
      alert('존재하지 않는 책입니다.')
      return
    }

    const bodyData = {
      seqBook: props.seqBook,
      seqSortSecond: props.seqSortSecond,
      barcodeBook: props.barcodeBook,
      cntBook: props.cntBook,
      printCheckBook: printCheckBook
    }

    const response = await fetch(`http://localhost:8080/books/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(bodyData)
    })

    if (!response.ok) {
      throw new Error(`서버 오류: ${response.status}`)
    }

    const result = await response.json()
    alert("✅ 저장하였습니다.")
    close()

  } catch (error) {
    alert(`저장 실패: ${error.message}`)
  }
}
</script>

<style scoped>
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
  max-width: 500px;
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

/* 모달 헤더 */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
  background: #f8f9fa;
}

.header-content {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  flex: 1;
}

.header-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 8px;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.header-icon.success {
  background: #007bff;
  color: white;
}

.header-icon.error {
  background: #dc3545;
  color: white;
}

.header-icon:not(.success):not(.error) {
  background: #6c757d;
  color: white;
}

.header-text {
  flex: 1;
  min-width: 0;
}

.modal-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #212529;
  margin: 0 0 0.5rem 0;
}

.book-title {
  font-size: 0.9rem;
  color: #6c757d;
  margin: 0;
  word-break: break-word;
  line-height: 1.4;
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(108, 117, 125, 0.1);
  color: #6c757d;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.close-btn:hover {
  background: rgba(108, 117, 125, 0.2);
  color: #495057;
}

/* 상태 섹션 */
.status-section {
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
}

.status-message {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.status-message.success {
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
  border: 1px solid rgba(40, 167, 69, 0.2);
}

.status-message.error {
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
  border: 1px solid rgba(220, 53, 69, 0.2);
}

.status-message.loading {
  background: rgba(108, 117, 125, 0.1);
  color: #6c757d;
  border: 1px solid rgba(108, 117, 125, 0.2);
}

.status-icon {
  flex-shrink: 0;
}

.status-text {
  flex: 1;
}

/* 바코드 정보 */
.barcode-info {
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
  background: #f8f9fa;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  font-weight: 500;
  color: #6c757d;
  font-size: 0.9rem;
}

.info-value {
  font-weight: 600;
  color: #495057;
}

.barcode-text {
  font-family: 'Courier New', monospace;
  font-size: 0.85rem;
  background: rgba(0, 123, 255, 0.1);
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  color: #007bff;
}

/* 바코드 미리보기 */
.barcode-preview {
  padding: 1.5rem;
  text-align: center;
  border-bottom: 1px solid #e9ecef;
}

.preview-label {
  font-size: 0.9rem;
  font-weight: 500;
  color: #6c757d;
  margin-bottom: 1rem;
}

.barcode-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  border: 2px dashed #dee2e6;
}

.barcode-svg {
  max-width: 100%;
  height: auto;
}

/* 액션 버튼 */
.modal-actions {
  padding: 1.5rem;
  background: white;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  flex: 1;
}

.save-btn {
  background: #6c757d;
  color: white;
}

.save-btn:hover:not(:disabled) {
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

.close-btn-bottom {
  background: rgba(108, 117, 125, 0.1);
  color: #6c757d;
  width: 100%;
}

.close-btn-bottom:hover {
  background: rgba(108, 117, 125, 0.2);
  color: #495057;
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 반응형 디자인 */
@media (max-width: 480px) {
  .modal-container {
    width: 95%;
    margin: 10px;
  }
  
  .modal-header,
  .status-section,
  .barcode-info,
  .barcode-preview,
  .modal-actions {
    padding: 1rem;
  }
  
  .header-content {
    gap: 0.75rem;
  }
  
  .header-icon {
    width: 40px;
    height: 40px;
  }
  
  .modal-title {
    font-size: 1.1rem;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-btn {
    width: 100%;
  }
}

/* 로딩 애니메이션 */
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.status-message.loading .status-icon svg {
  animation: spin 2s linear infinite;
}
</style>