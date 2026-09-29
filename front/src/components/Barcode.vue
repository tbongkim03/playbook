<template>
  <div class="modal-overlay" v-if="isOpen === true" v-modal-backdrop="close">
    <div class="modal-container" @click.stop>
      <!-- 모달 헤더 -->
      <div class="modal-header">
        <div class="header-content">
          <div class="header-icon" :class="{ 'error': isD, 'success': !isD && showBarcode }">
            <PhXCircle weight="duotone" :size="24" v-if="isD" />
            <PhBarcode weight="duotone" :size="24" v-else />
          </div>
          <div class="header-text">
            <h2 class="modal-title">바코드 출력</h2>
            <p class="book-title">{{ titleBook }}</p>
          </div>
        </div>
        <button class="close-btn" @click="close">
          <PhX weight="duotone" :size="20" />
        </button>
      </div>

      <!-- 상태 메시지 -->
      <div class="status-section">
        <div class="status-message" :class="{ 'error': isD, 'success': !isD && showBarcode, 'loading': !msg }">
          <div class="status-icon">
            <PhWarning weight="duotone" :size="20" v-if="isD" />
            <PhCheck weight="duotone" :size="20" v-else-if="showBarcode" />
            <PhCircleNotch weight="duotone" :size="20" v-else />
          </div>
          <span class="status-text">{{ msg || '바코드 검증 중...' }}</span>
        </div>
      </div>

      <!-- 출력 설정 (출력 모드일 때만 표시) -->
      <div class="print-settings" v-if="!isD && showBarcode">
        <div class="setting-group">
          <label for="startPosition" class="setting-label">
            <PhCrosshair weight="duotone" :size="16" />
            라벨 시작 위치
          </label>
          <select id="startPosition" v-model="startPosition" class="setting-select">
            <option v-for="n in 65" :key="n" :value="n - 1">{{ n }}번째</option>
          </select>
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
            <PhFloppyDisk weight="duotone" :size="16" />
            나중에 출력
          </button>
          <button 
            class="action-btn print-btn" 
            @click="printBarcode"
            :disabled="buttonsDisabled"
          >
            <PhPrinter weight="duotone" :size="16" />
            출력 및 저장
          </button>
        </div>
        
        <button class="action-btn close-btn-bottom" @click="close">
          닫기
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { vModalBackdrop } from '@/utils/modalBackdrop'
import { PhBarcode, PhCheck, PhCircleNotch, PhCrosshair, PhFloppyDisk, PhPrinter, PhWarning, PhX, PhXCircle } from '@phosphor-icons/vue'
import { ref, watch, onMounted, nextTick } from 'vue'
import JsBarcode from 'jsbarcode'
import { swAlert } from '@/utils/sweetAlert'
import * as bookApi from '@/api/book'

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
const startPosition = ref(0)  // 시작 위치 (0-64)

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
    const res = await bookApi.checkBarcode({
      seqBook: props.seqBook,
      barcodeBook: props.barcodeBook
    })

    isD.value = res.data.data.duplicated
    msg.value = res.data.data.message

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
const saveBook = async () => {
  if (isD.value === true) {
    await swAlert('중복된 바코드입니다. 저장할 수 없습니다.', 'warning')
    return
  }
  
  postPrintedBook(false)
}

// 개별 출력 및 저장
const printBarcode = async () => {
  if (isD.value === true) {
    await swAlert('중복된 바코드입니다. 출력할 수 없습니다.', 'warning')
    return
  }

  if (!barcodeSvg.value) {
    await swAlert('바코드가 아직 생성되지 않았습니다.', 'info')
    return
  }

  const printWindow = window.open('', '', 'width=1000,height=600')
  if (!printWindow) {
    await swAlert('팝업 차단을 해제해 주세요.', 'warning')
    return
  }

  // 13행 × 5열 = 65개의 박스 생성
  const totalBoxes = 65
  const boxes = []

  // 시작 위치만큼 빈 박스 추가
  for (let i = 0; i < startPosition.value; i++) {
    boxes.push('<div class="barcode-cell"></div>')
  }

  // 해당 단일 바코드 1개 추가
  const tempSvg = document.createElementNS("http://www.w3.org/2000/svg", "svg")
  JsBarcode(tempSvg, props.barcodeBook, {
    format: "CODE128",
    lineColor: "#000",
    width: 1,
    height: 40,
    displayValue: false,
  })

  boxes.push(`
    <div class="barcode-cell">
      <div class="barcode-content">
        ${tempSvg.outerHTML}
        <div class="barcode-label">${props.barcodeBook}</div>
        <div class="book-title">${props.titleBook || ''}</div>
      </div>
    </div>
  `)

  // 나머지 빈 박스로 채우기
  while (boxes.length < totalBoxes) {
    boxes.push('<div class="barcode-cell"></div>')
  }

  // 13행으로 나누기
  const rows = []
  for (let i = 0; i < 13; i++) {
    const rowBoxes = boxes.slice(i * 5, (i + 1) * 5)
    rows.push(`<div class="row">${rowBoxes.join('')}</div>`)
  }

  const doc = printWindow.document
  doc.open()
  doc.write(`
    <!DOCTYPE html>
    <html lang="ko">
    <head>
      <meta charset="UTF-8">
      <title>바코드 출력 - ${props.titleBook}</title>
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
      <div class="grid">
        ${rows.join('')}
      </div>
      <script>
        window.onload = function() {
          window.print();
        };
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
      await swAlert('존재하지 않는 책입니다.', 'error')
      return
    }

    const bodyData = {
      seqBook: props.seqBook,
      seqSortSecond: props.seqSortSecond,
      barcodeBook: props.barcodeBook,
      cntBook: props.cntBook,
      printCheckBook: printCheckBook
    }

    await bookApi.update(id, bodyData)
    await swAlert('저장하였습니다.', 'success')
    close()

  } catch (error) {
    await swAlert(`저장 실패: ${error.message}`, 'error')
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
  border-bottom: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface-subtle);
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
  border-radius: var(--pb-radius-sm);
  flex-shrink: 0;
}

.header-icon.success {
  background: var(--pb-color-brand);
  color: white;
}

.header-icon.error {
  background: var(--pb-color-danger);
  color: white;
}

.header-icon:not(.success):not(.error) {
  background: var(--pb-color-text-muted);
  color: white;
}

.header-text {
  flex: 1;
  min-width: 0;
}

.modal-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--pb-color-heading);
  margin: 0 0 0.5rem 0;
}

.book-title {
  font-size: 0.9rem;
  color: var(--pb-color-text-muted);
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
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text-muted);
  border-radius: var(--pb-radius-xs);
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
  flex-shrink: 0;
}

.close-btn:hover {
  background: var(--pb-color-border);
  color: var(--pb-color-text);
}

/* 상태 섹션 */
.status-section {
  padding: 1.5rem;
  border-bottom: 1px solid var(--pb-color-border);
}

.status-message {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  border-radius: var(--pb-radius-sm);
  font-weight: 500;
}

.status-message.success {
  background: var(--pb-color-success-soft);
  color: var(--pb-color-success);
  border: 1px solid var(--pb-color-success);
}

.status-message.error {
  background: var(--pb-color-danger-soft);
  color: var(--pb-color-danger);
  border: 1px solid var(--pb-color-danger);
}

.status-message.loading {
  background: var(--pb-color-surface-subtle);
  color: var(--pb-color-text-muted);
  border: 1px solid var(--pb-color-border);
}

.status-icon {
  flex-shrink: 0;
}

.status-text {
  flex: 1;
}

/* 출력 설정 섹션 */
.print-settings {
  padding: 1.5rem;
  border-bottom: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface-subtle);
}

.setting-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-width: 300px;
}

.setting-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: var(--pb-color-text);
  font-size: 0.9rem;
}

.setting-select {
  padding: 0.75rem 1rem;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-xs);
  font-size: 0.9rem;
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  transition: border-color 0.15s;
}

.setting-select:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px var(--pb-color-brand-soft);
}

/* 바코드 정보 */
.barcode-info {
  padding: 1.5rem;
  border-bottom: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface);
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
  color: var(--pb-color-text-muted);
  font-size: 0.9rem;
}

.info-value {
  font-weight: 600;
  color: var(--pb-color-text);
}

.barcode-text {
  font-family: 'Courier New', monospace;
  font-size: 0.85rem;
  background: var(--pb-color-brand-soft);
  padding: 0.25rem 0.5rem;
  border-radius: var(--pb-radius-xs);
  color: var(--pb-color-brand);
}

/* 바코드 미리보기 */
.barcode-preview {
  padding: 1.5rem;
  text-align: center;
  border-bottom: 1px solid var(--pb-color-border);
}

.preview-label {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--pb-color-text-muted);
  margin-bottom: 1rem;
}

.barcode-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 1rem;
  background: var(--pb-color-surface-subtle);
  border-radius: var(--pb-radius-sm);
  border: 2px dashed var(--pb-color-border);
}

.barcode-svg {
  max-width: 100%;
  height: auto;
}

/* 액션 버튼 */
.modal-actions {
 padding: 1.5rem;
 background: var(--pb-color-surface);
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
 border-radius: var(--pb-radius-xs);
 font-weight: 600;
 cursor: pointer;
 transition: background 0.15s;
 font-size: 0.9rem;
 flex: 1;
}

.save-btn {
 background: var(--pb-color-text-muted);
 color: white;
}

.save-btn:hover:not(:disabled) {
 background: var(--pb-color-text);
}

.print-btn {
 background: var(--pb-color-brand);
 color: white;
}

.print-btn:hover:not(:disabled) {
 background: var(--pb-color-brand-strong);
}

.close-btn-bottom {
 background: var(--pb-color-surface-subtle);
 color: var(--pb-color-text-muted);
 width: 100%;
}

.close-btn-bottom:hover {
 background: var(--pb-color-surface-muted);
 color: var(--pb-color-text);
}

.action-btn:disabled {
 opacity: 0.6;
 cursor: not-allowed;
}

/* 반응형 디자인 */
@media (max-width: 480px) {
 .modal-container {
   width: 95%;
   margin: 10px;
 }
 
 .modal-header,
 .status-section,
 .print-settings,
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
 
 .settings-grid {
   grid-template-columns: 1fr;
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