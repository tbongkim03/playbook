<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { store, toast } from '../store'
import LogView from '../components/LogView.vue'

const info = ref(null)
const checking = ref(false)
const running = ref(false)
const result = ref(null)
const logs = ref([])
let unsubscribe = null

const history = computed(() => (store.state && store.state.update && store.state.update.history) || [])
const canRun = computed(
  () => info.value && info.value.ok && !info.value.upToDate && !info.value.missingEnv.length && !running.value
)
const excludedTotal = computed(() =>
  info.value && info.value.newMigrations ? info.value.newMigrations.reduce((a, m) => a + m.excluded.length, 0) : 0
)

onMounted(async () => {
  unsubscribe = window.wizard.onUpdateLog((p) => logs.value.push({ line: p.line, stream: p.stream }))
  await check()
})
onBeforeUnmount(() => unsubscribe && unsubscribe())

async function check() {
  checking.value = true
  try {
    info.value = await window.wizard.updateCheck()
  } finally {
    checking.value = false
  }
}

async function runUpdate() {
  const target = info.value.latest
  const ok = window.confirm(
    `${info.value.current || '현재 버전'} → ${target} 로 업데이트합니다.\n\n` +
      '· 진행 중 1~5분간 도서관리 화면이 응답하지 않습니다. 대출·반납이 없는 시간에 진행하세요.\n' +
      '· 시작 전에 DB 를 자동 백업합니다.\n' +
      '· 새 버전이 정상 기동하지 않으면 이전 버전으로 자동으로 되돌립니다.\n\n계속할까요?'
  )
  if (!ok) return
  running.value = true
  result.value = null
  logs.value = []
  try {
    const r = await window.wizard.updateRun(target)
    result.value = r
    if (r.state) store.state = r.state
    if (r.ok) {
      toast(`${target} 업데이트를 마쳤습니다.`, 'success', 8000)
      await check()
    } else {
      toast(r.message || '업데이트에 실패했습니다.', 'error', 10000)
    }
  } finally {
    running.value = false
  }
}

function fmt(iso) {
  try {
    return new Date(iso).toLocaleString('ko-KR')
  } catch {
    return iso
  }
}

async function showFolder(p) {
  if (p) await window.wizard.showInFolder(p)
}
</script>

<template>
  <h1 class="page-title">업데이트</h1>
  <p class="page-lead">새 버전이 배포되었는지 확인하고, 이 PC 의 Playbook 을 새 버전으로 교체합니다.</p>

  <h2 class="section-title">버전</h2>
  <div class="card">
    <div v-if="checking" class="muted">새 버전을 확인하는 중…</div>
    <div v-else-if="info && !info.ok" class="notice danger">
      <strong>확인하지 못했습니다</strong>
      {{ info.message }}
    </div>
    <div v-else-if="info">
      <div class="check-row">
        <div class="grow">
          <div>
            현재 <b>{{ info.current || '알 수 없음' }}</b>
            <span class="faint"> → </span>
            최신 <b>{{ info.latest }}</b>
          </div>
          <div class="check-detail">확인 시각 {{ fmt(info.checkedAt) }}</div>
        </div>
        <button class="small" :disabled="checking || running" @click="check">다시 확인</button>
      </div>

      <div v-if="info.upToDate" class="notice success">
        <strong>최신 버전입니다</strong>
        업데이트할 내용이 없습니다.
      </div>

      <template v-else>
        <div v-if="!info.current" class="notice warn">
          <strong>현재 버전을 알 수 없습니다</strong>
          설치 경로의 .env 에 버전 태그가 없습니다(초기 설치본). 최신 버전으로 맞추면 이후부터는 버전이 표시됩니다.
        </div>

        <div v-if="info.missingEnv.length" class="notice danger">
          <strong>이 버전은 새 설정값이 필요합니다</strong>
          {{ info.missingEnv.join(', ') }} 이(가) 설정 파일에 없어 새 버전 백엔드가 기동하지 못합니다.
          새 버전의 설치 마법사를 받아 설정을 다시 생성한 뒤 업데이트하세요.
        </div>

        <div v-if="info.commits.length" class="mt-12">
          <div style="font-weight: 600">변경 내역</div>
          <ul class="check-detail selectable" style="margin: 6px 0 0; padding-left: 18px">
            <li v-for="(c, i) in info.commits" :key="i">{{ c }}</li>
          </ul>
        </div>

        <div class="mt-12">
          <div style="font-weight: 600">DB 변경</div>
          <div v-if="!info.newMigrations.length" class="check-detail">없음</div>
          <ul v-else class="check-detail" style="margin: 6px 0 0; padding-left: 18px">
            <li v-for="m in info.newMigrations" :key="m.file">
              <code class="inline">{{ m.file }}</code> — {{ m.tables.join(', ') }} ({{ m.statementCount }}개 문장)
            </li>
          </ul>
          <div v-if="excludedTotal" class="notice warn mt-8">
            <strong>삭제 계열 문장 {{ excludedTotal }}개는 실행하지 않습니다</strong>
            DROP·TRUNCATE·DELETE 는 설치 마법사가 자동으로 실행하지 않습니다. 필요하면 관리자에게 문의하세요.
          </div>
        </div>

        <div class="row mt-12">
          <button class="primary" :disabled="!canRun" @click="runUpdate">
            {{ running ? '업데이트 중…' : `${info.latest} 로 업데이트` }}
          </button>
        </div>
      </template>
    </div>
  </div>

  <template v-if="running || logs.length">
    <h2 class="section-title">진행 로그</h2>
    <div v-if="result && !result.ok" class="notice danger">
      <strong>{{ result.rolledBack ? '이전 버전으로 되돌렸습니다' : '업데이트를 마치지 못했습니다' }}</strong>
      <span style="white-space: pre-line">{{ result.message }}</span>
    </div>
    <div v-if="result && result.backupDir" class="check-detail">
      백업 폴더: <a href="#" @click.prevent="showFolder(result.backupDir)">{{ result.backupDir }}</a>
    </div>
    <LogView :lines="logs" height="320px" placeholder="업데이트 로그가 여기에 표시됩니다." />
  </template>

  <template v-if="history.length">
    <h2 class="section-title">업데이트 이력</h2>
    <div class="card">
      <div v-for="(h, i) in history" :key="i" class="check-row">
        <div class="grow">
          <div>
            {{ h.from || '?' }} → {{ h.to }}
            <b :style="{ color: h.ok ? 'var(--pb-success)' : 'var(--pb-danger)' }">
              {{ h.ok ? '완료' : h.rolledBack ? '실패 · 되돌림' : '실패' }}
            </b>
          </div>
          <div class="check-detail">{{ fmt(h.at) }}<span v-if="h.message"> · {{ h.message }}</span></div>
        </div>
      </div>
    </div>
  </template>
</template>
