<template>
  <div class="borrow-container">
    <div class="header">
      <button class="back-btn" @click="goBack">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M19 12H5M12 19l-7-7 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        뒤로가기
      </button>
      <h1 class="title">도서 대여</h1>
    </div>

    <div class="content">
      <div class="scan-area">
        <div class="scan-icon">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M9 12L11 14L15 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2"/>
          </svg>
        </div>
        <h2 class="scan-title">바코드를 스캔해주세요</h2>
        <p class="scan-description">바코드 리더기로 도서의 바코드를 스캔하면 자동으로 대여 처리됩니다.</p>
        
        <!-- 숨겨진 입력 필드 -->
        <input 
          ref="barcodeInput"
          v-model="barcodeBuffer"
          type="text"
          class="barcode-input"
          @input="onBarcodeInput"
          @blur="refocus"
          autocomplete="off"
        />
      </div>

      <div v-if="isLoading" class="loading">
        <div class="spinner"></div>
        <p>대여 처리 중...</p>
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
  console.log('onBarcodeInput 호출됨:', barcodeBuffer.value)
  // 바코드 리더기는 빠르게 입력하므로 디바운스 적용
  clearTimeout(window.barcodeTimeout)
  window.barcodeTimeout = setTimeout(() => {
    processBarcodeInput()
  }, 100)
}

const processBarcodeInput = async () => {
  let barcode = barcodeBuffer.value.trim()
  console.log('input에서 바코드 처리 - 원본:', barcodeBuffer.value)
  console.log('input에서 바코드 처리 - 트림 후:', barcode)
  
  if (!barcode) {
    console.log('바코드가 비어있음')
    return
  }

  // 한글이 포함되어 있으면 영문으로 변환
  if (/[ㄱ-ㅎㅏ-ㅣ가-힣]/.test(barcode)) {
    console.log('한글 감지됨:', barcode)
    barcode = convertKoreanToEnglish(barcode)
    console.log('영문 변환 후:', barcode)
  }

  // 바코드가 숫자로 시작하면 대분류가 누락된 것으로 판단
  if (/^\d{2}-/.test(barcode)) {
    console.log('대분류 누락 감지, 원본:', barcode)
    showMessage('바코드의 첫 번째 문자가 누락되었습니다. 다시 스캔해주세요.', 'error')
    barcodeBuffer.value = ''
    return
  }

  // 바코드 형식 검증
  const barcodePattern = /^[A-Z]\d{2}-\d{13}-\d+$/
  console.log('바코드 패턴 검증:', barcodePattern.test(barcode))
  
  if (!barcodePattern.test(barcode)) {
    console.log('바코드 형식 오류:', barcode)
    showMessage('올바른 바코드 형식이 아닙니다. (형식: A00-0000000000000-0)', 'error')
    barcodeBuffer.value = ''
    return
  }

  console.log('바코드 처리 시작:', barcode)
  lastScannedBarcode.value = barcode
  await borrowBook(barcode)
  barcodeBuffer.value = ''
}

const processBarcodeInputFromKeyboard = async (keyboardInput) => {
  let barcode = keyboardInput.trim()
  console.log('키보드에서 바코드 처리 - 원본:', keyboardInput)
  console.log('키보드에서 바코드 처리 - 트림 후:', barcode)
  
  if (!barcode) {
    console.log('바코드가 비어있음')
    return
  }

  // 한글이 포함되어 있으면 영문으로 변환
  if (/[ㄱ-ㅎㅏ-ㅣ가-힣]/.test(barcode)) {
    console.log('한글 감지됨:', barcode)
    barcode = convertKoreanToEnglish(barcode)
    console.log('영문 변환 후:', barcode)
  }

  // 바코드가 숫자로 시작하면 대분류가 누락된 것으로 판단
  if (/^\d{2}-/.test(barcode)) {
    console.log('대분류 누락 감지, 원본:', barcode)
    // 일단 에러로 처리하고 사용자에게 다시 스캔하라고 안내
    showMessage('바코드의 첫 번째 문자가 누락되었습니다. 다시 스캔해주세요.', 'error')
    return
  }

  // 바코드 형식 검증
  const barcodePattern = /^[A-Z]\d{2}-\d{13}-\d+$/
  console.log('바코드 패턴 검증:', barcodePattern.test(barcode))
  
  if (!barcodePattern.test(barcode)) {
    console.log('바코드 형식 오류:', barcode)
    showMessage('올바른 바코드 형식이 아닙니다. (형식: A00-0000000000000-0)', 'error')
    return
  }

  console.log('바코드 처리 시작:', barcode)
  lastScannedBarcode.value = barcode
  await borrowBook(barcode)
}

// 도서 대여 API 호출
const borrowBook = async (barcode) => {
  console.log('borrowBook 호출됨:', barcode)
  isLoading.value = true
  
  const token = localStorage.getItem('jwtToken')
  console.log('사용할 토큰:', token ? `${token.substring(0, 20)}...` : 'null')
  
  if (!token) {
    showMessage('로그인이 필요합니다.', 'error')
    isLoading.value = false
    return
  }
  
  try {
    console.log('API 요청 시작')
    
    const response = await axios({
      method: 'post',  // 백엔드가 POST로 변경됨
      url: 'http://localhost:8080/api/borrow',
      data: barcode,
      headers: {
        'Content-Type': 'text/plain',
        'Authorization': `Bearer ${token}`
      },
      withCredentials: false  // JWT 토큰 사용 시 false
    })
    
    console.log('API 응답 성공:', response)
    showMessage(response.data, 'success')
    
  } catch (error) {
    console.error('API 요청 실패:', error)
    
    if (error.response) {
      // 서버 응답이 있는 경우
      console.error('응답 상태:', error.response.status)
      console.error('응답 데이터:', error.response.data)
      const errorMessage = error.response.data || `서버 오류: ${error.response.status}`
      showMessage(errorMessage, 'error')
    } else if (error.request) {
      // 요청은 보냈지만 응답이 없는 경우
      console.error('네트워크 오류:', error.message)
      showMessage('네트워크 오류가 발생했습니다.', 'error')
    } else {
      // 요청 설정 중 오류
      console.error('요청 오류:', error.message)
      showMessage('요청 중 오류가 발생했습니다.', 'error')
    }
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
  console.log('refocus 호출됨')
  setTimeout(() => {
    if (barcodeInput.value) {
      barcodeInput.value.focus()
      console.log('입력 필드에 포커스 설정됨')
    }
  }, 10)
}

// 뒤로가기
const goBack = () => {
  router.push('/')
}

onMounted(() => {
  console.log('컴포넌트 마운트됨')
  
  // axios 기본 설정으로 withCredentials 비활성화
  axios.defaults.withCredentials = false
  
  // 컴포넌트 마운트 시 입력 필드에 포커스
  if (barcodeInput.value) {
    barcodeInput.value.focus()
    console.log('마운트 시 포커스 설정됨')
  } else {
    console.log('barcodeInput이 null입니다')
  }

  // 전역 클릭 이벤트로 포커스 유지
  document.addEventListener('click', refocus)
  console.log('클릭 이벤트 리스너 등록됨')
  
  // 바코드 리더기를 위한 키보드 이벤트 처리
  let keyBuffer = ''
  let keyTimeout = null
  let isProcessingBarcode = false
  
  document.addEventListener('keydown', (e) => {
    console.log('키 입력 감지:', e.key, e.code)
    
    // 바코드 처리 중이면 무시
    if (isProcessingBarcode) {
      e.preventDefault()
      return
    }
    
    // 특수키 무시 (Shift, Ctrl, Alt 등) - 단, Process는 제외
    if (e.key.length > 1 && !['Backspace', 'Delete', 'Tab', 'Enter', 'Process'].includes(e.key)) {
      return
    }
    
    // Process 키는 특별 처리 (바코드 리더기에서 A-Z를 Process로 전송할 수 있음)
    if (e.key === 'Process') {
      // e.code로 실제 키 확인하여 해당 문자 추가
      const keyMap = {
        'KeyA': 'A', 'KeyB': 'B', 'KeyC': 'C', 'KeyD': 'D', 'KeyE': 'E',
        'KeyF': 'F', 'KeyG': 'G', 'KeyH': 'H', 'KeyZ': 'Z'
      }
      
      if (keyMap[e.code]) {
        keyBuffer += keyMap[e.code]
        console.log(`Process ${e.code} 감지, ${keyMap[e.code]} 추가됨. 현재 버퍼:`, keyBuffer)
      }
      e.preventDefault()
      return
    }
    
    // Tab이나 Enter는 바코드 끝으로 간주
    if (e.key === 'Tab' || e.key === 'Enter') {
      e.preventDefault()
      if (keyBuffer.trim() && !isProcessingBarcode) {
        console.log('바코드 입력 완료:', keyBuffer)
        isProcessingBarcode = true
        barcodeBuffer.value = keyBuffer
        processBarcodeInputFromKeyboard(keyBuffer)
        keyBuffer = ''
        setTimeout(() => { isProcessingBarcode = false }, 1000)
      }
      return
    }
    
    // 백스페이스 처리
    if (e.key === 'Backspace') {
      keyBuffer = keyBuffer.slice(0, -1)
      e.preventDefault()
      return
    }
    
    // 일반 문자 추가
    if (e.key.length === 1) {
      keyBuffer += e.key
      console.log('현재 버퍼:', keyBuffer)
      
      // input 필드 업데이트 방지
      e.preventDefault()
    }
    
    // 타이머 리셋 (500ms 후 버퍼 클리어)
    clearTimeout(keyTimeout)
    keyTimeout = setTimeout(() => {
      if (keyBuffer && !isProcessingBarcode) {
        console.log('타임아웃으로 바코드 처리:', keyBuffer)
        isProcessingBarcode = true
        barcodeBuffer.value = keyBuffer
        processBarcodeInputFromKeyboard(keyBuffer)
        keyBuffer = ''
        setTimeout(() => { isProcessingBarcode = false }, 1000)
      }
    }, 500)
  })
})

onUnmounted(() => {
  document.removeEventListener('click', refocus)
  if (window.barcodeTimeout) {
    clearTimeout(window.barcodeTimeout)
  }
})
</script>

<style scoped>
.borrow-container {
  min-height: 100vh;
  background: #f7f6f3;
  color: #37352f;
  padding: 20px;
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
  background: #f7f6f3;
  border: 1px solid #e9e5e3;
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