<template>
  <div class="return-container">
    <div class="header">
      <button class="back-btn" @click="goBack">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M19 12H5M12 19l-7-7 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        뒤로가기
      </button>
      <h1 class="title">도서 반납</h1>
    </div>

    <div class="content">
      <div class="scan-area">
        <div class="scan-icon">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M15 7L12 10L9 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h2 class="scan-title">반납할 도서의 바코드를 스캔해주세요</h2>
        <p class="scan-description">바코드 리더기로 반납할 도서의 바코드를 스캔하면 자동으로 반납 처리됩니다.</p>
        
        <!-- 숨겨진 입력 필드 -->
        <input 
          ref="barcodeInput"
          v-model="barcodeBuffer"
          type="text"
          class="barcode-input"
          @input="onBarcodeInput"
          @blur="refocus"
          autocomplete="off"
          readonly
        />
      </div>

      <div v-if="isLoading" class="loading">
        <div class="spinner"></div>
        <p>반납 처리 중...</p>
      </div>

      <div v-if="lastScannedBarcode" class="last-scan">
        <h3>마지막 스캔된 바코드</h3>
        <div class="barcode-info">
          <p class="barcode-text">{{ lastScannedBarcode }}</p>
          <div class="barcode-details">
            <span class="category">{{ parsedBarcode.category }}</span>
            <span class="isbn">{{ parsedBarcode.isbn }}</span>
            <span class="book-num">책번호: {{ parsedBarcode.bookNumber }}</span>
          </div>
        </div>
      </div>

      <div v-if="message" class="message" :class="messageType">
        <div class="message-icon">
          <svg v-if="messageType === 'success'" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M9 12L11 14L15 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
          </svg>
          <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2"/>
            <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        {{ message }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const barcodeInput = ref(null)
const barcodeBuffer = ref('')
const lastScannedBarcode = ref('')
const message = ref('')
const messageType = ref('success')
const isLoading = ref(false)

// 한글-영문 매핑 테이블
const koreanToEnglishMap = {
  'ㅁ': 'A', 'ㅠ': 'B', 'ㅊ': 'C', 'ㅇ': 'D', 'ㄷ': 'E', 'ㄹ': 'F', 'ㅎ': 'G', 'ㅗ': 'H',
  'ㅑ': 'I', 'ㅓ': 'J', 'ㅏ': 'K', 'ㅣ': 'L', 'ㅡ': 'M', 'ㅜ': 'N', 'ㅐ': 'O', 'ㅔ': 'P',
  'ㅂ': 'Q', 'ㄱ': 'R', 'ㄴ': 'S', 'ㅅ': 'T', 'ㅕ': 'U', 'ㅍ': 'V', 'ㅈ': 'W', 'ㅌ': 'X',
  'ㅛ': 'Y', 'ㅋ': 'Z'
}

// 바코드 파싱
const parsedBarcode = computed(() => {
  if (!lastScannedBarcode.value) {
    return { category: '', isbn: '', bookNumber: '' }
  }
  
  const parts = lastScannedBarcode.value.split('-')
  if (parts.length !== 3) {
    return { category: '알 수 없음', isbn: '', bookNumber: '' }
  }

  const categoryCode = parts[0]
  const categoryMap = {
    'A': '일반', 'B': '컴퓨터일반', 'C': '웹/앱', 'D': '데이터베이스/빅데이터/분석/엔지니어링',
    'E': '클라우드/데브옵스', 'F': '인공지능', 'G': '기타', 'H': '엔코아', 'Z': '미지정'
  }
  
  return {
    category: categoryMap[categoryCode[0]] || '알 수 없음',
    isbn: parts[1],
    bookNumber: parts[2]
  }
})

// 한글을 영문으로 변환
const convertKoreanToEnglish = (text) => {
  return text.split('').map(char => koreanToEnglishMap[char] || char).join('')
}

// 바코드 입력 처리
const onBarcodeInput = () => {
  // 바코드 리더기는 빠르게 입력하므로 디바운스 적용
  clearTimeout(window.barcodeTimeout)
  window.barcodeTimeout = setTimeout(() => {
    processBarcodeInput()
  }, 100)
}

const processBarcodeInput = async () => {
  let barcode = barcodeBuffer.value.trim()
  if (!barcode) return

  // 한글이 포함되어 있으면 영문으로 변환
  if (/[ㄱ-ㅎㅏ-ㅣ가-힣]/.test(barcode)) {
    barcode = convertKoreanToEnglish(barcode)
  }

  // 바코드 형식 검증
  const barcodePattern = /^[A-Z]\d{2}-\d{13}-\d+$/
  if (!barcodePattern.test(barcode)) {
    showMessage('올바른 바코드 형식이 아닙니다.', 'error')
    barcodeBuffer.value = ''
    return
  }

  lastScannedBarcode.value = barcode
  await returnBook(barcode)
  barcodeBuffer.value = ''
}

// 도서 반납 API 호출
const returnBook = async (barcode) => {
  isLoading.value = true
  try {
    const response = await axios.put('/api/return', barcode, {
      headers: {
        'Content-Type': 'text/plain'
      }
    })
    
    showMessage(response.data, 'success')
  } catch (error) {
    const errorMessage = error.response?.data || '반납 처리 중 오류가 발생했습니다.'
    showMessage(errorMessage, 'error')
  } finally {
    isLoading.value = false
  }
}

// 메시지 표시
const showMessage = (msg, type = 'success') => {
  message.value = msg
  messageType.value = type
  
  // 3초 후 메시지 자동 삭제
  setTimeout(() => {
    message.value = ''
  }, 3000)
}

// 포커스 유지
const refocus = () => {
  setTimeout(() => {
    if (barcodeInput.value) {
      barcodeInput.value.focus()
    }
  }, 10)
}

// 뒤로가기
const goBack = () => {
  router.push('/')
}

onMounted(() => {
  // 컴포넌트 마운트 시 입력 필드에 포커스
  if (barcodeInput.value) {
    barcodeInput.value.focus()
  }

  // 전역 클릭 이벤트로 포커스 유지
  document.addEventListener('click', refocus)
})

onUnmounted(() => {
  document.removeEventListener('click', refocus)
  if (window.barcodeTimeout) {
    clearTimeout(window.barcodeTimeout)
  }
})
</script>

<style scoped>
.return-container {
  min-height: 100vh;
  background: #f7f6f3;
  color: #37352f;
  padding: 20px;
  margin-top: -1.5rem;
}

.header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 40px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #ffffff;
  border: 1px solid #e9e5e3;
  border-radius: 6px;
  color: #37352f;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
  box-shadow: 0 1px 3px rgba(15, 15, 15, 0.1);
}

.back-btn:hover {
  background: #f1f1ef;
  border-color: #d9d5d2;
}

.title {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0;
  color: #37352f;
}

.content {
  max-width: 800px;
  margin: 0 auto;
}

.scan-area {
  background: #ffffff;
  border: 1px solid #e9e5e3;
  border-radius: 8px;
  padding: 60px 40px;
  text-align: center;
  margin-bottom: 30px;
  position: relative;
  box-shadow: 0 1px 3px rgba(15, 15, 15, 0.1);
}

.scan-icon {
  color: #6b7280;
  margin-bottom: 24px;
}

.scan-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: #37352f;
}

.scan-description {
  font-size: 1rem;
  color: #6b7280;
  margin: 0;
  line-height: 1.6;
}

.barcode-input {
  position: absolute;
  left: -9999px;
  opacity: 0;
  pointer-events: none;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 40px;
  background: #ffffff;
  border: 1px solid #e9e5e3;
  border-radius: 8px;
  margin-bottom: 30px;
  box-shadow: 0 1px 3px rgba(15, 15, 15, 0.1);
}

.spinner {
  width: 24px;
  height: 24px;
  border: 2px solid #e9e5e3;
  border-left: 2px solid #37352f;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.last-scan {
  background: #ffffff;
  border: 1px solid #e9e5e3;
  border-radius: 8px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 1px 3px rgba(15, 15, 15, 0.1);
}

.last-scan h3 {
  margin: 0 0 20px 0;
  font-size: 1.125rem;
  color: #37352f;
  font-weight: 600;
}

.barcode-info {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  padding: 20px;
}

.barcode-text {
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 1.1rem;
  font-weight: 500;
  margin: 0 0 16px 0;
  letter-spacing: 1px;
  color: #37352f;
}

.barcode-details {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.barcode-details span {
  background: #ffffff;
  border: 1px solid #e9e5e3;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.875rem;
  color: #6b7280;
}

.message {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 6px;
  font-weight: 400;
  font-size: 0.875rem;
  animation: slideIn 0.3s ease-out;
}

.message.success {
  background: #d1f2eb;
  border: 1px solid #a7f3d0;
  color: #047857;
}

.message.error {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .title {
    font-size: 2rem;
  }
  
  .scan-area {
    padding: 40px 20px;
  }
  
  .barcode-details {
    flex-direction: column;
    gap: 8px;
  }
}
</style>