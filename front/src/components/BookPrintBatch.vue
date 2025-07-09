<template>
  <div class="black-bg" @click="close">
    <div class="white-bg" @click.stop>
      <h2>바코드 출력</h2>
      <div class="controls">
        <label for="countSelect">바코드 수:</label>
        <select id="countSelect" v-model="selectedCountPerPage">
          <option v-for="option in options" :key="option" :value="option">
            {{ option }}
          </option>
        </select>
      </div>

      <div class="preview">
        <div class="barcode-grid" :style="gridStyle">
          <div class="barcode-cell" v-for="book in displayedBooks" :key="book.seqBook">
            <svg ref="barcodeSvgs" :data-code="book.barcodeBook"></svg>
            <div class="barcode-text">
              {{ book.barcodeBook }} - {{ book.titleBook }}
            </div>
          </div>
        </div>
      </div>

      <div class="buttons">
        <button class="btn btn-secondary" @click="printAll">🖨️ 출력하기</button>
        <button class="btn btn-primary" @click="close">닫기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted, nextTick } from 'vue'
import JsBarcode from 'jsbarcode'

const emit = defineEmits(['close'])
function close() {
  emit('close')
}

// 1. 바코드 책 리스트 (fetch로 받아옴)
const books = ref([])

// 2. 출력 시 사용자가 선택하는 바코드 수
const options = ref([])
const selectedCountPerPage = ref(1)

// 3. fetch 사용해서 조건에 맞는 바코드 책 리스트 가져오기
const fetchUnprintedBarcodes = async () => {
  try {
    const res = await fetch('http://localhost:8080/books/unprinted')
    if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`)
    const data = await res.json()
    books.value = data

    // 옵션 초기화 (1 ~ books.length)
    options.value = []
    for (let i = 1; i <= books.value.length; i++) {
      options.value.push(i)
    }
    selectedCountPerPage.value = books.value.length > 0 ? books.value.length : 1
  } catch (error) {
    console.error('바코드 리스트 가져오기 실패:', error)
  }
}

// 4. 보여줄 책 슬라이스
const displayedBooks = computed(() => {
  return books.value.slice(0, selectedCountPerPage.value)
})

// 5. grid 스타일
const gridStyle = 'grid-template-columns: repeat(3, 1fr); gap: 10px;'

// 6. 바코드 생성
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
        displayValue: true,
        fontSize: 12
      })
    })
  })
}

// 7. 데이터가 변경되거나 선택 수가 바뀌면 바코드 다시 생성
watch([() => selectedCountPerPage.value, books], () => {
  generateBarcodes()
})

// 8. 컴포넌트 마운트 시 데이터 불러오기 및 바코드 생성
onMounted(async () => {
  await fetchUnprintedBarcodes()
  generateBarcodes()
})
</script>

<style>
.controls {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.preview {
  max-height: 60vh;
  overflow-y: auto;
}
.barcode-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.barcode-cell {
  border: 1px solid #ccc;
  padding: 10px;
  text-align: center;
}
.barcode-text {
  margin-top: 5px;
  font-size: 12px;
}
.buttons {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
}
</style>
