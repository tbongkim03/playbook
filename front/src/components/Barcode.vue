<template>
  <div class="black-bg" v-if="isOpen === true" @click="close">
    <div class="white-bg" @click.stop>
      <h4>바코드 출력</h4>
      <hr>
      <svg ref="barcodeSvg"></svg>
      <div class="prints">
        <button class="btn btn-secondary" @click="saveBook">나중에 출력</button>
        <button class="btn btn-secondary" @click="printBarcode">출력 및 저장 🖨️</button>
      </div>
      <button class="btn btn-primary" @click="close">닫기</button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, nextTick, onUnmounted } from 'vue'
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
      height: 40,
      displayValue: true,
      fontSize: 14,
      text: `${props.barcodeBook} ${props.titleBook}`,
    })
  }
}

// 모달이 열릴 때 렌더 후 생성
watch(() => props.isOpen, async (newVal) => {
  if (newVal) {
    await nextTick()
    generateBarcode()
    console.log('props.seqSortSecond:', props.seqSortSecond)
  }
})

// 마운트 시 생성
onMounted(async () => {
  if (props.isOpen) {
    await nextTick()
    generateBarcode()
  }
})

// 나중에 출력(저장만)
const saveBook = () => {
  postPrintedBook(false)
}

// 개별 출력 및 저장
const printBarcode = () => {
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
        <style>
          body { font-family: sans-serif; }
          svg { width: auto; height: 35px; }
        </style>
      </head>
      <body>
        ${barcodeSvg.value.outerHTML}
        <script>
          window.onload = function() {
            window.print();
          }
          window.onafterprint = function() {
            window.opener.postMessage('print-done', '*');
            window.close();
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
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(bodyData)
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const result = await response.json()
    console.log('PUT 성공:', result)
    alert("✅ 저장하였습니다.");

  } catch (error) {
    console.error('PUT 오류:', error)
  }
}
</script>

<style>
body {
  margin: 0px;
}
div {
  box-sizing: border-box;
}
.black-bg {
  position: fixed;
  top: 0; left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
}

.white-bg {
  background: white;
  border-radius: 8px;
  padding: 20px;
  min-width: 400px;
  min-height: 300px;
  box-shadow: 0 0 10px rgba(0,0,0,0.3);
  position: relative;
  z-index: 10000;
  display: flex;
  flex-direction: column;
}
.title {
  margin-top: 10px;
  font-size: 16px;
}
.prints {
  width: 100%;
  height: 50px;
  display: flex;
  margin-top: 50px;
  margin-bottom: 20px;
  column-gap: 20px;
}
.prints button {
  width: 50%;
  border: 1px solid black;
}
</style>
