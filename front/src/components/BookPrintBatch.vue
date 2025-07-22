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

      <div class="controls">
        <label for="startPosition">시작 위치:</label>
        <select id="startPosition" v-model="startPosition">
          <option v-for="n in 21" :key="n" :value="n - 1">{{ n }}</option>
           <!-- <option v-for="n in 40" :key="n" :value="n - 1">{{ n }}</option> -->
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

const startPosition = ref(0)

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

// 9. 출력 함수
const printAll = async () => {
  if (!displayedBooks.value.length) {
    alert('출력할 바코드가 없습니다.')
    return
  }

  const printWindow = window.open('', '', 'width=1000,height=600') 

  const emptyCells = Array(startPosition.value).fill('<div class="barcode-cell"></div>')

  const content = [
    ...emptyCells,
    ...displayedBooks.value.map(book => {
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
          ${tempSvg.outerHTML}
          <div class="barcode-label">${book.barcodeBook} - ${book.titleBook}</div>
        </div>
      `
    })
  ].join('')

  
  if (!printWindow) {
    alert("팝업 차단을 해제해 주세요")
        return
  }

  const doc = printWindow.document;
  doc.open()
  doc.write(`
  <!DOCTYPE html>
    <html>
      <head>
        <meta charset="UTF-8">
        <title>바코드 출력</title>
        <style>
          @page { size: A4; margin: 0; }
          body {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
          }

          .print-area {
            display: grid;
            grid-template-columns: repeat(5, 38.1mm);
            grid-auto-rows: 21.2mm;
            gap: 0mm 2.5mm;
            padding: 15mm 6.5mm; /* 상단, 좌우 여백 */
          }

          .barcode-cell {
            width: 38.1mm;
            height: 21.2mm;
            border: 1px solid #ccc; /* 가이드용으로 보이게 하려면 #ccc */
            border-radius: 5px;
            box-sizing: border-box;
            overflow: hidden;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            font-size: 10pt;
          }

          svg {
            width: 35mm;
            /* min-width: 100%; */
          }

          .barcode-label {
            font-size: 5px;
            margin-top: 1mm;
            text-align: center;
            word-break: break-word;
          }
      </style>
      </head>
      <body>
        <div class="print-area">
          ${content}
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

  // try {
  //   const ids = displayedBooks.value.map(book => book.seqBook)

  //   const res = await fetch('http://localhost:8080/books/batch/print', {
  //     method: 'PUT',
  //     headers: {
  //       'Content-Type': 'application/json'
  //     },
  //     body: JSON.stringify(ids)
  //   })

  //   if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`)

  //   alert('인쇄 완료 상태로 저장되었습니다.')

  //   // 다시 목록 갱신
  //   await fetchUnprintedBarcodes()
  //   generateBarcodes()
  // } catch (error) {
  //   alert('저장에 실패했습니다.', error)
  // }
}
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
  /* grid-template-columns: repeat(4, 1fr); */
  gap: 10px;
  /* gap: 0px; */
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
