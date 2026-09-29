<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { store, toast } from '../store'
import StepNav from '../components/StepNav.vue'
import LogView from '../components/LogView.vue'
import StatusPill from '../components/StatusPill.vue'

const scan = ref(null)
const scanError = ref('')
const selection = ref({}) // file -> Set-like object { [groupKey]: true }
const logs = ref([])
const busy = ref('')
const applied = ref(false)
const servicesStarted = ref(false)
const services = ref([])

let unsubscribe = null
let unsubscribeDeploy = null

onMounted(async () => {
  unsubscribe = window.wizard.onMigrationLog((p) => logs.value.push({ line: p.line, stream: p.stream }))
  unsubscribeDeploy = window.wizard.onDeployLog((p) => logs.value.push({ line: p.line, stream: p.stream }))
  servicesStarted.value = !!(store.state.deploy && store.state.deploy.uppedAt)
  await runScan()
})

onUnmounted(() => {
  unsubscribe && unsubscribe()
  unsubscribeDeploy && unsubscribeDeploy()
})

async function runScan() {
  busy.value = 'scan'
  scanError.value = ''
  try {
    const r = await window.wizard.migrationScan()
    if (!r.ok) {
      scanError.value = r.message || '스캔에 실패했습니다.'
      scan.value = null
      return
    }
    scan.value = r
    // 안전 기본값: 아직 없는 테이블만 미리 체크
    const next = {}
    for (const f of r.files) {
      next[f.file] = {}
      for (const g of f.groups) {
        next[f.file][g.key] = f.status === 'applied' ? false : !!g.recommended
      }
    }
    selection.value = next
  } finally {
    busy.value = ''
  }
}

function toggle(file, key) {
  selection.value[file][key] = !selection.value[file][key]
}

function toggleFile(file, on) {
  const f = scan.value.files.find((x) => x.file === file)
  for (const g of f.groups) selection.value[file][g.key] = on
}

const selectedCount = computed(() => {
  let n = 0
  for (const file of Object.keys(selection.value)) {
    for (const k of Object.keys(selection.value[file])) if (selection.value[file][k]) n += 1
  }
  return n
})

const riskyChecked = computed(() => {
  if (!scan.value) return []
  const out = []
  for (const f of scan.value.files) {
    for (const g of f.groups) {
      if (selection.value[f.file] && selection.value[f.file][g.key] && g.tableExists === true) {
        out.push(`${f.file} → ${g.label}`)
      }
    }
  }
  return out
})

async function apply() {
  const selections = []
  for (const f of scan.value.files) {
    const keys = f.groups.filter((g) => selection.value[f.file] && selection.value[f.file][g.key]).map((g) => g.key)
    if (keys.length) selections.push({ file: f.file, groupKeys: keys })
  }
  if (!selections.length) {
    toast('적용할 항목을 하나 이상 선택하세요.', 'warn')
    return
  }
  busy.value = 'apply'
  try {
    const r = await window.wizard.migrationApply(selections)
    if (r.state) store.state = r.state
    if (r.ok) {
      applied.value = true
      toast('선택한 마이그레이션을 적용했습니다.', 'success')
    } else {
      const failed = (r.results || []).filter((x) => !x.ok)
      toast(failed.length ? `${failed[0].file}: ${failed[0].message}` : r.message || '적용 실패', 'error', 9000)
    }
    await runScan()
  } finally {
    busy.value = ''
  }
}

async function startServices() {
  busy.value = 'start'
  try {
    const r = await window.wizard.deployStartServices()
    if (r.state) store.state = r.state
    if (r.ok) {
      servicesStarted.value = true
      services.value = r.services || []
      if (r.proxyWarning) toast(r.proxyWarning, 'error', 12000)
      else toast('서비스를 기동했습니다.', 'success')
    } else {
      toast(r.message || '서비스 기동에 실패했습니다.', 'error', 9000)
    }
  } finally {
    busy.value = ''
  }
}

const pendingFiles = computed(() =>
  scan.value ? scan.value.files.filter((f) => f.status === 'pending' || f.status === 'changed') : []
)
const excludedTotal = computed(() => (scan.value ? scan.value.excludedTotal : 0))

// ── 초기 도서 데이터 (선택) ────────────────────────────────────────
const seed = computed(() => store.state && store.state.bookSeed)
const seedFileName = computed(() => {
  const p = seed.value && seed.value.filePath
  return p ? p.split(/[\\/]/).pop() : ''
})
const bookSeedStatus = computed(() => {
  if (!seed.value || !seed.value.importedAt) return 'idle'
  return seed.value.errorCount ? 'fail' : 'ok'
})

async function pickBookFile() {
  const r = await window.wizard.pickBookFile()
  if (r.state) store.state = r.state
  if (!r.ok && !r.canceled) toast(r.message || '파일을 선택하지 못했습니다.', 'error')
}

async function importBooks() {
  busy.value = 'books'
  logs.value.push({ line: '───────────── 초기 도서 데이터 등록 ─────────────' })
  try {
    const r = await window.wizard.importBooks()
    if (r.state) store.state = r.state
    if (r.ok) {
      const msg = `신규 ${r.inserted}권 · 갱신 ${r.updated}권 등록 완료`
      toast(r.errors && r.errors.length ? `${msg} (오류 ${r.errors.length}건)` : msg,
        r.errors && r.errors.length ? 'warn' : 'success', 8000)
    } else {
      toast(r.message || '도서 등록에 실패했습니다.', 'error', 9000)
      logs.value.push({ line: `실패: ${r.message}`, stream: 'stderr' })
    }
  } finally {
    busy.value = ''
  }
}
</script>

<template>
  <h1 class="page-title">9. DB 마이그레이션 <span class="faint" style="font-size: 15px">(배포 2/2)</span></h1>
  <p class="page-lead">
    데이터베이스 스키마를 최신 상태로 맞춘 뒤 나머지 서비스를 기동합니다.
    <b>테이블 단위로 확인을 받고</b> 선택한 것만 실행합니다.
  </p>

  <div class="notice danger">
    <strong>기존 데이터는 지우지 않습니다</strong>
    <code class="inline">DROP</code> · <code class="inline">TRUNCATE</code> ·
    <code class="inline">DELETE FROM</code> 이 들어간 문장은 <b>실행 대상에서 완전히 제외</b>되며,
    체크해도 실행되지 않습니다. 아래 &quot;제외된 문장&quot; 에 무엇이 걸렸는지 표시됩니다.
    (SQL 주석 안의 롤백문은 판정 대상이 아닙니다)
  </div>

  <div v-if="scanError" class="notice danger">
    <strong>스캔 실패</strong>
    <div style="white-space: pre-wrap">{{ scanError }}</div>
  </div>

  <div class="card" v-if="scan">
    <div class="card-head">
      <span class="card-title">대상 데이터베이스</span>
      <span class="mono faint">{{ scan.database }}</span>
      <span class="grow"></span>
      <button class="small" :disabled="busy === 'scan'" @click="runScan">
        {{ busy === 'scan' ? '스캔 중…' : '다시 스캔' }}
      </button>
    </div>
    <div class="check-detail">
      현재 테이블 {{ scan.existingTables.length }}개 · 마이그레이션 파일 {{ scan.files.length }}개 ·
      미적용 {{ pendingFiles.length }}개
      <span v-if="excludedTotal"> · 제외된 파괴적 문장 {{ excludedTotal }}개</span>
    </div>
  </div>

  <template v-if="scan">
    <div v-for="f in scan.files" :key="f.file" class="card">
      <div class="card-head">
        <StatusPill
          :status="f.status === 'pending' || f.status === 'changed' ? 'warn' : 'ok'"
          :labels="{ ok: f.statusLabel, warn: f.statusLabel }"
        />
        <span class="card-title mono">{{ f.file }}</span>
        <span class="grow"></span>
        <button class="small ghost" @click="toggleFile(f.file, true)">전체 선택</button>
        <button class="small ghost" @click="toggleFile(f.file, false)">전체 해제</button>
      </div>

      <div v-if="f.appliedAt" class="field-hint mb-8">
        마지막 적용: {{ new Date(f.appliedAt).toLocaleString('ko-KR') }}
      </div>

      <div v-for="g in f.groups" :key="g.key" class="check-row">
        <label class="check" style="flex: 1">
          <input
            type="checkbox"
            :checked="selection[f.file] && selection[f.file][g.key]"
            @change="toggle(f.file, g.key)"
          />
          <span class="grow">
            <b class="mono">{{ g.label }}</b>
            <span v-if="g.tableExists === true" class="pill warn" style="margin-left: 6px">이미 있음</span>
            <span v-else-if="g.tableExists === false" class="pill idle" style="margin-left: 6px">없음</span>
            <div class="check-detail">
              {{ g.kinds.join(', ') }} · 문장 {{ g.statementCount }}개
              <span v-if="g.rowCount !== null && g.rowCount !== undefined"> · 현재 {{ g.rowCount }}행</span>
            </div>
            <div class="check-detail faint">{{ g.note }}</div>
          </span>
        </label>
      </div>

      <div v-if="f.excluded.length" class="notice warn mt-12">
        <strong>제외된 문장 {{ f.excluded.length }}개 (실행하지 않음)</strong>
        <div v-for="(e, i) in f.excluded" :key="i" class="mono" style="font-size: 11.5px">
          · [{{ e.reason }}] {{ e.preview }}
        </div>
      </div>
    </div>
  </template>

  <div v-if="riskyChecked.length" class="notice warn">
    <strong>이미 존재하는 테이블이 선택되어 있습니다</strong>
    <div v-for="(r, i) in riskyChecked" :key="i">· {{ r }}</div>
    <div class="mt-8">
      중복 적용은 대개 &quot;컬럼이 이미 있음&quot; 오류로 끝나지만, UPDATE 문이 포함된 경우 데이터가 바뀔 수
      있습니다. 확실하지 않으면 체크를 해제하세요.
    </div>
  </div>

  <div class="card">
    <div class="row mb-8">
      <button class="primary" :disabled="busy !== '' || !selectedCount" @click="apply">
        {{ busy === 'apply' ? '적용 중…' : `선택한 ${selectedCount}개 항목 적용` }}
      </button>
      <span class="faint" style="font-size: 12px">선택하지 않은 항목은 건드리지 않습니다.</span>
    </div>
    <LogView :lines="logs" height="240px" placeholder="적용 로그가 여기에 표시됩니다." />
  </div>

  <h2 class="section-title">서비스 기동</h2>
  <div class="notice info">
    <strong>마이그레이션이 끝나야 백엔드가 뜹니다</strong>
    운영 백엔드는 스키마 검증 모드로 동작하므로, 필요한 테이블이 없으면 기동에 실패합니다.
    위에서 미적용 항목을 모두 처리한 뒤 아래 버튼을 누르세요.
  </div>

  <div class="card">
    <div class="row">
      <button class="primary" :disabled="busy !== ''" @click="startServices">
        {{ busy === 'start' ? '기동 중…' : servicesStarted ? '다시 기동' : '백엔드 · 프론트 기동' }}
      </button>
      <span v-if="store.state.deploy && store.state.deploy.uppedAt" class="faint" style="font-size: 12px">
        마지막 기동: {{ new Date(store.state.deploy.uppedAt).toLocaleString('ko-KR') }}
      </span>
    </div>

    <div v-if="services.length" class="mt-12">
      <div v-for="s in services" :key="s.name" class="check-row">
        <StatusPill :status="/running|up/i.test(s.state || s.status || '') ? 'ok' : 'fail'" />
        <div class="grow">
          <b>{{ s.service }}</b> <span class="faint mono">{{ s.name }}</span>
          <div class="check-detail">{{ s.status || s.state }}</div>
        </div>
      </div>
    </div>
  </div>

  <!-- ── 초기 도서 데이터 (선택) ─────────────────────────────────── -->
  <h2 class="section-title">초기 도서 데이터 <span class="faint">(선택)</span></h2>
  <div class="card">
    <p class="mt-0 muted">
      이미 정리해 둔 장서 목록이 있으면 지금 한 번에 넣을 수 있습니다.
      <b>관리자 화면의 [엑셀로 내보내기] 로 받은 양식</b>을 그대로 쓰면 됩니다.
      건너뛰어도 되고, 설치 후 관리자 화면에서 언제든 올릴 수 있습니다.
    </p>

    <div class="notice info">
      <strong>양식</strong>
      <code class="inline">도서번호 · 제목 · ISBN · 저자 · 출판사 · 출판일 · 대분류 · 중분류 · 수량 · 대출상태 · 바코드 · 표지URL · 라벨출력</code>
      <div class="mt-8">
        · <b>도서번호가 비었거나 이 서버에 없는 번호면 신규 등록</b>됩니다(번호는 새로 매겨집니다).
        이 서버에 있는 번호면 그 도서를 수정하되, ISBN 이 다르면 다른 책으로 보고 건너뜁니다.<br />
        · <b>라벨출력</b> 은 실물 라벨을 붙였다는 기록입니다. <code class="inline">출력됨</code> 으로 두면 그대로 이어집니다.<br />
        · <b>대출상태</b> 는 신규 등록 시 무시하고 항상 “대출가능”으로 넣습니다 —
        대여이력 없이 대출중이면 반납할 수 없는 상태가 되기 때문입니다.<br />
        · <b>중분류</b> 는 이름으로 찾습니다. 없는 이름이면 그 행만 오류로 건너뜁니다.
      </div>
    </div>

    <div class="check-row">
      <StatusPill
        :status="bookSeedStatus"
        :labels="{ ok: '등록됨', fail: '오류 있음', idle: '미선택' }"
      />
      <div class="grow">
        <b>{{ seedFileName || '엑셀 파일을 선택하세요' }}</b>
        <div v-if="seed && seed.importedAt" class="check-detail">
          신규 {{ seed.inserted }}권 · 갱신 {{ seed.updated }}권 · 건너뜀 {{ seed.skipped }}행
          <span v-if="seed.errorCount"> · 오류 {{ seed.errorCount }}건</span>
        </div>
        <div v-else class="check-detail">10MB 이하 .xlsx 파일</div>
      </div>
      <button class="small" :disabled="busy !== ''" @click="pickBookFile">파일 선택</button>
      <button
        class="small primary"
        :disabled="busy !== '' || !seedFileName || !servicesStarted"
        @click="importBooks"
      >
        {{ busy === 'books' ? '등록 중…' : '도서 등록' }}
      </button>
    </div>

    <div v-if="!servicesStarted" class="field-hint">
      위에서 서비스를 먼저 기동해야 합니다. 도서 등록은 백엔드 API 를 통해 이루어집니다.
    </div>
    <div v-if="seed && seed.errorCount" class="notice warn">
      <strong>일부 행이 등록되지 않았습니다 ({{ seed.errorCount }}건)</strong>
      아래 진행 로그에 행 번호와 사유가 있습니다. <b>실패한 행만 남긴 파일</b>로 고쳐 다시 올리세요
      (파일 전체를 다시 올리면 이미 등록된 도서가 <b>중복 등록</b>됩니다).
    </div>
  </div>

  <StepNav
    :next-disabled="!servicesStarted"
    next-reason="서비스를 기동한 뒤 완료 단계로 넘어가세요."
    next-label="완료 확인"
  />
</template>
