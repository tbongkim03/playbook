<template>
    <div class="black-bg" v-if="isOpen == true">
        <div class="white-bg">
            <h4>바코드 생성</h4>
            <hr>
            <!-- <p>{{ barcodeBook }}</p> -->
            <svg ref="barcodeSvg"></svg>
            <!-- <div class="title">{{ titleBook }}</div> -->
            <div class="prints">
                <button class="btn btn-secondary">나중에 출력</button>
                <button class="btn btn-secondary" @click="printBarcode">개별 출력🖨️</button>
            </div>
            
            <button class="btn btn-primary" @click="$emit('close')">닫기</button>
        </div>

    </div>
</template>

<script setup>
import { ref, watch, onMounted, nextTick } from 'vue'
import JsBarcode from 'jsbarcode'

const props = defineProps({
  barcodeBook: String,
  titleBook: String,
  isOpen: Boolean
})

const emit = defineEmits(['close'])

const barcodeSvg = ref(null)

// 실제 바코드 생성
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

// 모달이 열릴 때만 렌더 후 생성
watch(() => props.isOpen, async (newVal) => {
  if (newVal) {
    // 렌더 완료 기다렸다가 실행
    await nextTick()
    generateBarcode()
  }
})

// 컴포넌트 처음 마운트 시 열려 있으면 생성
onMounted(async () => {
  if (props.isOpen) {
    await nextTick()
    generateBarcode()
  }
})

const printBarcode = () => {
  if (!barcodeSvg.value) {
    alert("바코드가 아직 생성되지 않았습니다")
    return
  }

  const printWindow = window.open('', '', 'width=400,height=600')
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
            window.close();
          }
        </` + `script>
      </body>
    </html>
  `)
  doc.close()
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
  z-index: 9999; /* 매우 높게 설정해서 헤더 포함 모든 요소 위로 */
  display: flex;
  justify-content: center;
  align-items: center;
}

.white-bg {
  background: white;
  border-radius: 8px;
  padding: 20px;
  min-width: 300px;
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
    display: flex;
    margin-top: 30px;
    margin-bottom: 10px;
    column-gap: 10px;
}
.prints button {
    width: 50%;
    border: 1px solid black;
}
</style>