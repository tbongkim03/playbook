<script setup>
import { computed, onMounted, ref } from 'vue'
import { store, patch, toast } from '../store'
import StepNav from '../components/StepNav.vue'

const detected = ref(null)
const entries = ref([])
const draft = ref('')
const draftError = ref('')
const acknowledged = ref(false)

onMounted(async () => {
  entries.value = [...((store.state.ipAllowlist && store.state.ipAllowlist.entries) || [])]
  acknowledged.value = !!(store.state.ipAllowlist && store.state.ipAllowlist.acknowledgedEmpty)
  await detect()
  if (entries.value.length === 0 && detected.value && detected.value.defaultEntries.length) {
    entries.value = [...detected.value.defaultEntries]
    await save()
  }
})

async function detect() {
  const r = await window.wizard.detectIp()
  if (r.ok) detected.value = r.detected
}

async function save() {
  await patch({ ipAllowlist: { entries: entries.value, acknowledgedEmpty: acknowledged.value } })
}

async function addEntry(value) {
  const raw = value !== undefined ? value : draft.value
  draftError.value = ''
  const r = await window.wizard.validateIpEntry(raw)
  if (!r.ok || !r.result.ok) {
    draftError.value = (r.result && r.result.message) || r.message || '올바르지 않은 값입니다.'
    return
  }
  const normalized = r.result.normalized
  if (entries.value.includes(normalized)) {
    draftError.value = '이미 등록된 값입니다.'
    return
  }
  entries.value.push(normalized)
  if (value === undefined) draft.value = ''
  if (r.result.note) toast(r.result.note, 'info', 6000)
  await save()
}

async function removeEntry(i) {
  entries.value.splice(i, 1)
  await save()
}

async function toggleAck() {
  acknowledged.value = !acknowledged.value
  await save()
}

const isEmpty = computed(() => entries.value.length === 0)
const myIp = computed(() => (detected.value && detected.value.primary ? detected.value.primary.address : null))

const coversMe = computed(() => {
  if (!myIp.value) return false
  const ip = myIp.value
  return entries.value.some((e) => {
    if (e === ip || e === `${ip}/32`) return true
    if (!e.includes('/') || e.includes(':')) return false
    const [net, prefixStr] = e.split('/')
    const prefix = Number(prefixStr)
    const toInt = (s) => s.split('.').reduce((a, o) => (a << 8) + Number(o), 0) >>> 0
    const mask = prefix === 0 ? 0 : (0xffffffff << (32 - prefix)) >>> 0
    return (toInt(ip) & mask) === (toInt(net) & mask)
  })
})

const canProceed = computed(() => !isEmpty.value || acknowledged.value)
</script>

<template>
  <h1 class="page-title">6. 접속 허용 IP 초기 등록</h1>
  <p class="page-lead">
    Playbook 은 여기 등록된 위치에서만 접속할 수 있습니다. 화면과 <code class="inline">/api</code> 요청
    <b>전부</b>에 적용됩니다. 설치 후에는 관리자 화면에서 언제든 추가·수정할 수 있습니다.
  </p>

  <div class="notice warn">
    <strong>현재 버전에서는 차단 기능이 꺼진 상태로 설치됩니다</strong>
    Windows 의 Docker Desktop 은 접속한 PC 의 IP 를 서비스에 전달하지 않아, 모든 접속이 같은 내부 IP 로
    보입니다. 이 상태로 차단을 켜면 이 PC(localhost)를 포함해 <b>모두 차단</b>됩니다. 여기서 등록한 대역은
    저장만 되고, 차단 방식이 보완된 뒤 적용됩니다.
  </div>

  <div class="notice danger">
    <strong>여기 없는 위치에서는 접속이 차단됩니다</strong>
    라운지 PC 와 관리자 PC 가 쓰는 대역을 빠뜨리면, 설치가 끝난 뒤 아무도 화면에 들어갈 수 없습니다.
    잘 모르겠으면 아래에서 자동 감지된 <b>/24 대역</b>을 그대로 두세요.
  </div>

  <h2 class="section-title">이 PC 에서 감지한 네트워크</h2>
  <div class="card">
    <div v-if="!detected || !detected.primary" class="muted">
      네트워크 주소를 찾지 못했습니다. 유선/무선 연결 상태를 확인한 뒤 [다시 감지] 를 누르세요.
    </div>
    <template v-else>
      <table class="kv">
        <tbody>
          <tr>
            <th>이 PC 의 IP</th>
            <td class="mono selectable">{{ detected.primary.address }} <span class="faint">({{ detected.primary.interfaceName }})</span></td>
          </tr>
          <tr>
            <th>서브넷 마스크</th>
            <td class="mono">{{ detected.primary.netmask }} (/{{ detected.primary.prefix }})</td>
          </tr>
        </tbody>
      </table>

      <div class="mt-12">
        <div class="field-label">추천 항목 — 눌러서 추가</div>
        <div class="row">
          <button v-for="s in detected.suggestions" :key="s.value" class="small" @click="addEntry(s.value)">
            {{ s.label }}
          </button>
        </div>
      </div>

      <div v-if="detected.addresses.length > 1" class="field-hint mt-12">
        다른 어댑터:
        <span v-for="(a, i) in detected.addresses.filter((x) => x !== detected.primary)" :key="i" class="mono">
          {{ a.interfaceName }} {{ a.address }}{{ a.virtual ? '(가상)' : '' }}&nbsp;
        </span>
      </div>
    </template>
  </div>

  <h2 class="section-title">등록할 규칙</h2>
  <div class="card">
    <div class="input-row mb-8">
      <input
        v-model="draft"
        type="text"
        class="mono"
        placeholder="192.168.0.0/24 또는 192.168.0.15"
        @keyup.enter="addEntry()"
      />
      <button class="small primary" @click="addEntry()">추가</button>
    </div>
    <div v-if="draftError" class="field-error mb-8">{{ draftError }}</div>

    <div v-if="isEmpty" class="muted">등록된 규칙이 없습니다.</div>
    <div v-else>
      <div v-for="(e, i) in entries" :key="e" class="check-row">
        <span class="pill" :class="e.includes('/') ? 'idle' : 'ok'">{{ e.includes('/') ? '대역' : '단일' }}</span>
        <div class="grow mono selectable">{{ e }}</div>
        <span v-if="myIp && (e === myIp || e === myIp + '/32')" class="pill ok">내 IP</span>
        <button class="small danger" @click="removeEntry(i)">삭제</button>
      </div>
    </div>

    <div v-if="!isEmpty && myIp && !coversMe" class="notice warn mt-12">
      <strong>이 PC({{ myIp }}) 가 어떤 규칙에도 포함되지 않습니다</strong>
      지금 설정대로면 설치를 마친 뒤 이 PC 에서 화면에 접속할 수 없습니다.
      (localhost 로는 항상 접속되지만, 다른 기기에서는 막힙니다)
    </div>
  </div>

  <div v-if="isEmpty" class="notice warn">
    <strong>규칙 0건으로 진행하면 &quot;전면 허용&quot; 상태가 됩니다</strong>
    차단 필터는 활성 규칙이 하나도 없을 때 <b>모든 접속을 통과시킵니다</b>(잠김 사고 방지 장치).
    즉 캠퍼스 네트워크에서 IP 주소만 알면 누구나 들어올 수 있습니다.
    설치 후 관리자 화면에서 반드시 규칙을 등록하세요.
    <div class="mt-8">
      <label class="check">
        <input type="checkbox" :checked="acknowledged" @change="toggleAck" />
        위 내용을 이해했고, 규칙 없이 진행합니다.
      </label>
    </div>
  </div>

  <div class="notice info">
    <strong>잠겨도 복구할 수 있습니다</strong>
    · <code class="inline">127.0.0.1</code> 로컬 접속과 Docker 내부망은 항상 허용됩니다<br />
    · 설치 경로의 <code class="inline">back/.env.prod</code> 에서
    <code class="inline">IP_ALLOWLIST_BYPASS</code> 에 대역을 넣고 컨테이너를 재시작하면 우회됩니다<br />
    · 여기서 정한 값은 <code class="inline">IP_ALLOWLIST_BOOTSTRAP</code> 환경변수로 전달되어
    최초 기동 시 DB 규칙으로 등록됩니다
  </div>

  <StepNav
    :next-disabled="!canProceed"
    next-reason="규칙 0건으로 진행하려면 위 확인란을 체크하세요."
  >
    <template #extra>
      <button class="small" @click="detect">다시 감지</button>
    </template>
  </StepNav>
</template>
