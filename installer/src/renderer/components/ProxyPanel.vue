<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { store, toast } from '../store'
import StatusPill from './StatusPill.vue'
import LogView from './LogView.vue'

/**
 * Windows 네이티브 접속 프록시 + IP 차단 스위치.
 * Docker Desktop 은 접속 IP 를 보존하지 않아, 프록시가 실제 IP 를 넘기는지 검사를 통과해야
 * IP 차단을 켤 수 있다. 끄기는 비상 복구용이라 언제든 가능하다.
 */

const status = ref(null)
const verify = ref(null)
const rules = ref(null)
const busy = ref('')
const logs = ref([])
let unsubscribe = null

const state = computed(() => store.state)
const allowlistOn = computed(() => !!(state.value.ipAllowlist && state.value.ipAllowlist.enabled))
const verifyOk = computed(() => !!(state.value.proxy && state.value.proxy.lastVerifyOk))
const service = computed(() => (status.value && status.value.service) || {})

onMounted(async () => {
  unsubscribe = window.wizard.onProxyLog((p) => logs.value.push({ line: p.line, stream: p.stream }))
  await refresh()
  await loadRules()
})
onBeforeUnmount(() => unsubscribe && unsubscribe())

async function refresh() {
  const r = await window.wizard.proxyStatus()
  if (r.ok) status.value = r
}

async function loadRules() {
  rules.value = await window.wizard.allowlistRules()
}

async function run(kind, fn) {
  busy.value = kind
  try {
    const r = await fn()
    if (r.state) store.state = r.state
    return r
  } finally {
    busy.value = ''
  }
}

async function install() {
  logs.value = []
  const r = await run('install', () => window.wizard.proxyInstall())
  await refresh()
  if (r.ok) {
    toast('접속 프록시를 설치했습니다. 이어서 접속 IP 를 검사합니다.', 'success')
    await check()
  } else {
    toast(r.message || '프록시 설치에 실패했습니다.', 'error', 10000)
  }
}

async function check() {
  const r = await run('verify', () => window.wizard.proxyVerify())
  verify.value = r
  if (!r.ok && r.message) toast(r.message, 'error', 8000)
}

async function uninstall() {
  if (!window.confirm('접속 프록시를 제거합니다.\n· IP 차단이 켜져 있으면 먼저 끕니다.\n· Playbook 이 만든 서비스·방화벽 규칙만 지웁니다.\n계속할까요?')) return
  logs.value = []
  const r = await run('uninstall', () => window.wizard.proxyUninstall())
  await refresh()
  verify.value = null
  if (r.ok) toast('접속 프록시를 제거했습니다.', 'success')
  else toast(r.message || '프록시 제거에 실패했습니다.', 'error', 10000)
}

async function setAllowlist(on) {
  if (on && !window.confirm('IP 차단을 켭니다. 허용 규칙에 없는 기기는 접속할 수 없게 됩니다.\n이 PC(localhost)는 항상 허용됩니다. 계속할까요?')) return
  const r = await run('allowlist', () => window.wizard.setAllowlistEnabled(on))
  await loadRules()
  if (r.ok) toast(on ? 'IP 차단을 켰습니다.' : 'IP 차단을 껐습니다.', 'success')
  else toast(r.message || '변경에 실패했습니다.', 'error', 9000)
}
</script>

<template>
  <h2 class="section-title">접속 IP 보존 · IP 차단</h2>
  <div class="card">
    <template v-if="status && status.service.supported">
      <div class="check-row">
        <StatusPill
          :status="service.running ? 'ok' : service.installed ? 'warn' : 'fail'"
          :labels="{ ok: '실행 중', warn: service.state || '중지됨', fail: '미설치' }"
        />
        <div class="grow">
          <b>접속 프록시 (Caddy · PlaybookProxy 서비스)</b>
          <div class="check-detail">
            외부 접속(80)을 받아 실제 접속 IP 를 붙여 넘깁니다. Docker Desktop 은 접속 IP 를 보존하지 않아 이 프록시가
            없으면 IP 차단이 동작하지 않습니다. 설치 시 관리자 권한 확인 창이 뜹니다.
          </div>
        </div>
        <button class="small" :disabled="busy !== '' || !status.bundled" @click="install">
          {{ busy === 'install' ? '설치 중…' : service.installed ? '다시 설치' : '설치' }}
        </button>
        <button v-if="service.installed" class="small" :disabled="busy !== ''" @click="uninstall">
          {{ busy === 'uninstall' ? '제거 중…' : '제거' }}
        </button>
      </div>

      <div class="check-row">
        <StatusPill
          :status="verify ? (verify.ok ? 'ok' : 'fail') : verifyOk ? 'ok' : 'idle'"
          :labels="{ ok: '통과', fail: '실패', idle: '미검사' }"
        />
        <div class="grow">
          <b>접속 IP 검사</b>
          <div class="check-detail">
            이 PC 에서 두 가지 주소로 접속해 서버가 실제 접속 주소를 구분하는지, 그리고 방화벽이 다른 기기의 접속을 허용하는지 확인합니다.
          </div>
          <template v-if="verify && verify.checks">
            <div v-for="c in verify.checks" :key="c.id" class="check-detail">
              {{ c.status === 'ok' ? '✔' : c.status === 'warn' ? '△' : '✘' }} {{ c.label }} — {{ c.detail }}
            </div>
          </template>
          <div v-if="verify && verify.hint" class="check-hint">→ {{ verify.hint }}</div>
          <div class="check-hint">
            → 이 PC 안에서의 검사는 방화벽을 거치지 않습니다. 반드시 <b>같은 Wi-Fi 에 연결한 휴대폰</b>으로
            <span class="mono">http://{{ state.ipAllowlist?.detected?.primary?.address || '이-PC-의-IP' }}</span> 에 접속해,
            관리자 화면 &gt; 접속 허용 IP 의 "현재 접속 IP" 가 휴대폰 주소(예: 192.168.x.x)로 보이는지 확인하세요.
            172.31.240.x 로 보이면 차단을 켜지 마세요.
          </div>
        </div>
        <button class="small" :disabled="busy !== '' || !service.running" @click="check">
          {{ busy === 'verify' ? '검사 중…' : '검사' }}
        </button>
      </div>
    </template>
    <div v-else-if="status" class="check-detail">
      이 운영체제에서는 컨테이너가 접속 IP 를 그대로 보므로 별도 프록시가 필요 없습니다.
    </div>

    <div class="check-row">
      <StatusPill :status="allowlistOn ? 'ok' : 'idle'" :labels="{ ok: '켜짐', idle: '꺼짐' }" />
      <div class="grow">
        <b>IP 차단</b>
        <div class="check-detail">
          켜면 관리자 화면에 등록한 허용 IP 에서만 접속할 수 있습니다. 문제가 생기면 여기서 끄면 됩니다(비상 복구).
        </div>
      </div>
      <button v-if="!allowlistOn" class="small" :disabled="busy !== '' || (status && status.service.supported && !verifyOk)" @click="setAllowlist(true)">
        {{ busy === 'allowlist' ? '적용 중…' : '켜기' }}
      </button>
      <button v-else class="small danger" :disabled="busy !== ''" @click="setAllowlist(false)">
        {{ busy === 'allowlist' ? '적용 중…' : '끄기' }}
      </button>
    </div>

    <div class="check-row">
      <div class="grow">
        <b>지금 적용되는 허용 규칙</b>
        <div v-if="!rules" class="check-detail">불러오는 중…</div>
        <div v-else-if="!rules.ok" class="check-detail">{{ rules.message }}</div>
        <template v-else>
          <div v-if="!rules.rules.length" class="check-detail">규칙 0건 — 차단을 켜도 모두 접속할 수 있습니다.</div>
          <div v-for="(r, i) in rules.rules" :key="i" class="check-detail">
            {{ r.active ? '●' : '○' }} <span class="mono">{{ r.value }}</span>
            <span v-if="r.note"> — <b>{{ r.note }}</b></span>
            <span v-if="r.description" class="faint"> · {{ r.description }}</span>
            <span v-if="!r.active" class="faint"> (비활성)</span>
          </div>
          <div class="check-hint">
            → 규칙은 관리자 화면 &gt; 접속 허용 IP 에서 바꿉니다. 설정 파일의
            <span class="mono">IP_ALLOWLIST_BOOTSTRAP</span>({{ rules.bootstrap || '비어 있음' }})은 처음 설치할 때 한 번만 쓰는 값이라,
            나중에 고쳐도 위 목록은 바뀌지 않습니다.
          </div>
        </template>
      </div>
      <button class="small" :disabled="busy !== ''" @click="loadRules">새로고침</button>
    </div>

    <LogView v-if="logs.length" :lines="logs" height="180px" />
  </div>
</template>
