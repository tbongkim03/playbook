<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { store, patch, toast, withBusy } from '../store'
import StepNav from '../components/StepNav.vue'
import SecretField from '../components/SecretField.vue'
import StatusPill from '../components/StatusPill.vue'
import LogView from '../components/LogView.vue'

const portal = ref({ url: 'https://discord.com/developers/applications', permissions: '' })
const guilds = ref([])
const selectedGuild = ref('')
const linkChannelName = ref('플북-연동안내')
const logs = ref([])
const busy = ref('')
const provisionResult = ref(null)
const permResult = ref(null)

let unsubscribe = null

const d = computed(() => (store.state ? store.state.discord : null))
const campusName = computed(() => (store.state && store.state.campus ? store.state.campus.name : ''))
const tokenVerified = computed(() => !!(d.value && d.value.tokenVerifiedAt && d.value.applicationId))
const provisioned = computed(() => !!(d.value && d.value.provisionedAt))
const skipped = computed(() => !!(d.value && d.value.skipped))

onMounted(async () => {
  const p = await window.wizard.discordPortal()
  if (p.ok) portal.value = p
  if (d.value && d.value.guildId) selectedGuild.value = d.value.guildId
  unsubscribe = window.wizard.onDiscordLog((payload) => {
    logs.value.push({ line: payload.line, stream: 'stdout' })
  })
})

onUnmounted(() => unsubscribe && unsubscribe())

async function refreshState() {
  const r = await window.wizard.getState()
  if (r.ok) store.state = r.state
}

async function open(url) {
  const r = await window.wizard.openExternal(url)
  if (!r.ok) toast(r.message, 'error')
}

async function verifyToken() {
  busy.value = 'token'
  try {
    const r = await window.wizard.discordVerifyToken()
    if (r.state) store.state = r.state
    toast(r.message, r.ok ? 'success' : 'error', 7000)
    if (r.ok) logs.value.push({ line: `봇 확인: ${r.botName} (application id ${r.applicationId})` })
  } finally {
    busy.value = ''
  }
}

async function openInvite() {
  if (!d.value || !d.value.inviteUrl) {
    toast('먼저 봇 토큰을 검증하세요.', 'warn')
    return
  }
  await open(d.value.inviteUrl)
}

async function loadGuilds() {
  busy.value = 'guilds'
  try {
    const r = await window.wizard.discordListGuilds()
    if (!r.ok) {
      toast(r.message, 'error', 8000)
      return
    }
    guilds.value = r.guilds
    if (r.guilds.length === 1) selectedGuild.value = r.guilds[0].id
    toast(r.message, r.guilds.length ? 'success' : 'warn', 7000)
  } finally {
    busy.value = ''
  }
}

/**
 * 초대 승인 != 권한 부여. 실제 값을 디스코드에 물어 확인한다.
 * 여기서 잡지 않으면 ④ 에서 403 을 만나거나, 더 나쁘게는 설치가 끝난 뒤
 * 계정 연동 시 역할 부여가 조용히 실패한다.
 */
async function checkPermissions() {
  if (!selectedGuild.value) {
    toast('대상 서버를 선택하세요.', 'warn')
    return
  }
  busy.value = 'perm'
  try {
    const r = await window.wizard.discordCheckPermissions(selectedGuild.value)
    permResult.value = r
    if (r.ok && r.hasAll) toast(r.message, 'success')
    else toast(r.message, r.ok ? 'error' : 'warn', 9000)
    logs.value.push({ line: `권한 확인: ${r.message}`, stream: r.ok && r.hasAll ? 'stdout' : 'stderr' })
  } finally {
    busy.value = ''
  }
}

async function provision() {
  if (!selectedGuild.value) {
    toast('대상 서버를 선택하세요.', 'warn')
    return
  }
  busy.value = 'provision'
  provisionResult.value = null
  logs.value.push({ line: '───────────────────────────────' })
  try {
    const guild = guilds.value.find((g) => g.id === selectedGuild.value)
    const r = await window.wizard.discordProvision({
      guildId: selectedGuild.value,
      guildName: guild ? guild.name : '',
      linkChannelName: linkChannelName.value
    })
    provisionResult.value = r
    if (r.state) store.state = r.state
    if (r.ok) toast('디스코드 채널·역할 준비가 끝났습니다.', 'success')
    else {
      toast(r.message || '디스코드 설정에 실패했습니다.', 'error', 9000)
      logs.value.push({ line: `실패: ${r.message}`, stream: 'stderr' })
    }
  } finally {
    busy.value = ''
  }
}

async function toggleSkip() {
  const next = !skipped.value
  await window.wizard.discordSkip(next)
  await refreshState()
  if (next) toast('디스코드 설정을 건너뜁니다. 설치 후 연동 탭에서 등록할 수 있습니다.', 'warn', 7000)
}

const canProceed = computed(() => skipped.value || provisioned.value)

// 서버를 바꾸면 이전 서버의 권한 결과가 그대로 남아 오해를 부른다.
// 목록 조회 때 이미 권한을 함께 받아오므로 그 값으로 즉시 채워 준다
// (사용자가 [권한 확인] 을 누르지 않아도 부족한 서버를 고르면 바로 보인다).
watch(selectedGuild, (id) => {
  const g = guilds.value.find((x) => x.id === id)
  if (!g) {
    permResult.value = null
    return
  }
  permResult.value = {
    ok: true,
    guildId: g.id,
    guildName: g.name,
    isAdmin: g.isAdmin,
    hasAll: g.hasAllPermissions,
    have: [],
    missing: (g.missingPermissions || []).map((label) => ({ label })),
    message: g.hasAllPermissions
      ? '필요한 권한을 모두 가지고 있습니다.'
      : `권한이 부족합니다 — ${(g.missingPermissions || []).join(', ')}`
  }
})

/** 권한 확인 결과 → StatusPill 상태. 미확인이면 idle. */
const permStatus = computed(() => {
  if (!permResult.value) return 'idle'
  if (!permResult.value.ok) return 'idle'
  return permResult.value.hasAll ? 'ok' : 'fail'
})
</script>

<template>
  <h1 class="page-title">4. 디스코드 봇 연결</h1>
  <p class="page-lead">
    디스코드 봇은 대여·반납 알림과 계정 연동에 쓰입니다. 앱 생성과 토큰 발급은 디스코드가 공식 API 를 제공하지
    않아 직접 하셔야 하고, <b>그 다음 단계(초대 · 채널 · 역할 생성)는 마법사가 자동으로 처리</b>합니다.
  </p>

  <div v-if="skipped" class="notice warn">
    <strong>디스코드 설정을 건너뛴 상태입니다</strong>
    도서 대여·반납 알림과 디스코드 계정 연동 기능이 동작하지 않습니다. 설치 후
    <b>관리자 화면 &gt; 연동 탭</b>에서 봇 토큰과 채널 ID 를 등록하면 활성화됩니다.
    <div class="mt-8"><button class="small" @click="toggleSkip">건너뛰기 취소하고 지금 설정</button></div>
  </div>

  <template v-if="!skipped">
    <!-- 1) 포털 안내 -->
    <h2 class="section-title">① 디스코드 개발자 포털에서 봇 만들기</h2>
    <div class="card">
      <ol class="guide">
        <li>
          <b>Developer Portal 을 엽니다.</b>
          <div class="check-detail">아래 버튼을 누르면 기본 브라우저에서 열립니다. 디스코드 계정으로 로그인하세요.</div>
          <div class="mt-8"><button class="small primary" @click="open(portal.url)">Developer Portal 열기 ↗</button></div>
        </li>
        <li>
          <b>오른쪽 위 [New Application] 을 누르고 이름을 정합니다.</b>
          <div class="check-detail">예) <code class="inline">Playbook {{ campusName || '캠퍼스' }}</code> — 서버에 표시될 봇 이름입니다.</div>
        </li>
        <li>
          <b>왼쪽 메뉴에서 [Bot] 탭으로 들어갑니다.</b>
          <div class="check-detail">앱을 만들면 봇은 자동으로 생성되어 있습니다.</div>
        </li>
        <li>
          <b>[Reset Token] 을 눌러 토큰을 발급받고 [Copy] 로 복사합니다.</b>
          <div class="check-detail">
            토큰은 <b>이때 딱 한 번만</b> 전체가 보입니다. 창을 닫으면 다시 볼 수 없고 재발급해야 합니다.
          </div>
        </li>
        <li>
          <b>같은 [Bot] 화면 아래 &quot;Privileged Gateway Intents&quot; 에서
            <span style="color: var(--pb-warn)">SERVER MEMBERS INTENT</span> 를 켭니다.</b>
          <div class="check-detail">
            서버에 새로 들어온 사람에게 캠퍼스 역할을 자동으로 붙여 주는 기능(PlaybookListener)이 이 권한을 씁니다.
            켜지 않으면 봇이 아예 연결되지 않거나 연동 시 역할이 부여되지 않습니다.
            <b>이 토글은 API 로 바꿀 수 없어 직접 켜셔야 합니다.</b>
          </div>
        </li>
        <li>
          <b>[Save Changes] 를 눌러 저장합니다.</b>
        </li>
      </ol>
    </div>

    <!-- 2) 토큰 검증 -->
    <h2 class="section-title">② 봇 토큰 붙여넣고 검증</h2>
    <div class="card">
      <div class="card-head">
        <StatusPill :status="tokenVerified ? 'ok' : 'idle'" :labels="{ ok: '검증 완료', idle: '미검증' }" />
        <span v-if="tokenVerified" class="check-detail">
          봇 <b>{{ d.botName }}</b> · Application ID <span class="mono selectable">{{ d.applicationId }}</span>
        </span>
      </div>

      <SecretField
        path="discord.botToken"
        label="봇 토큰 (DISCORD_BOT_TOKEN)"
        required
        :secret="d ? d.botToken : {}"
        placeholder="복사한 토큰을 붙여넣으세요"
        hint="'Bot ' 접두사나 앞뒤 공백이 있어도 자동으로 정리합니다."
      />

      <button class="small primary" :disabled="busy === 'token'" @click="verifyToken">
        {{ busy === 'token' ? '검증 중…' : '토큰 검증' }}
      </button>
      <div class="field-hint mt-8">
        디스코드 <code class="inline">GET /users/@me</code> 로 실제 호출해 확인합니다.
      </div>
    </div>

    <!-- 3) 초대 -->
    <h2 class="section-title">③ 서버에 봇 초대</h2>
    <div class="card">
      <p class="mt-0 muted">
        아래 버튼은 필요한 권한만 담은 초대 링크를 자동으로 만들어 브라우저로 엽니다. 링크가 열리면 봇을 넣을
        <b>디스코드 서버를 고르고 [승인]</b> 을 누르세요.
      </p>

      <div class="notice info">
        <strong>이 봇이 요구하는 권한</strong>
        채널 관리 · 역할 관리 · 채널 보기 · 메시지 보내기 · 링크 첨부 · 메시지 기록 보기
        <span class="faint">(permissions = {{ portal.permissions }})</span><br />
        마법사가 채널과 역할을 만들고, 서비스가 알림을 보내고 연동 시 역할을 부여하는 데 쓰입니다.
      </div>

      <div class="row">
        <button class="small primary" :disabled="!tokenVerified" @click="openInvite">초대 링크 열기 ↗</button>
        <button class="small" :disabled="!tokenVerified || busy === 'guilds'" @click="loadGuilds">
          {{ busy === 'guilds' ? '조회 중…' : '초대 완료 — 서버 목록 가져오기' }}
        </button>
      </div>

      <div v-if="d && d.inviteUrl" class="field-hint mt-8" style="word-break: break-all">
        <span class="mono selectable">{{ d.inviteUrl }}</span>
      </div>
    </div>

    <!-- 4) 서버 선택 + 프로비저닝 -->
    <h2 class="section-title">④ 대상 서버 선택 · 채널과 역할 자동 생성</h2>
    <div class="card">
      <div class="field">
        <label class="field-label">봇이 들어가 있는 서버<span class="req">*</span></label>
        <select v-model="selectedGuild" :disabled="!guilds.length">
          <option value="">
            {{ guilds.length ? '선택하세요' : '먼저 [서버 목록 가져오기] 를 누르세요' }}
          </option>
          <option v-for="g in guilds" :key="g.id" :value="g.id">
            {{ g.hasAllPermissions ? '' : '⚠ ' }}{{ g.name }} ({{ g.id }})
          </option>
        </select>
        <div v-if="d && d.guildId && !guilds.length" class="field-hint">
          이전에 선택한 서버: <b>{{ d.guildName || d.guildId }}</b>
        </div>
      </div>

      <!-- 권한 확인 -->
      <div class="field">
        <div class="card-head">
          <StatusPill
            :status="permStatus"
            :labels="{ ok: '권한 확인됨', fail: '권한 부족', idle: '미확인' }"
          />
          <button class="small" :disabled="!selectedGuild || busy === 'perm'" @click="checkPermissions">
            {{ busy === 'perm' ? '확인 중…' : '권한 확인' }}
          </button>
        </div>
        <div class="field-hint">
          초대를 승인해도 권한이 함께 들어왔다는 보장은 없습니다. 승인 화면에서 권한 체크를 끄거나,
          <b>이미 서버에 있던 봇을 재사용하면 예전 권한이 그대로 유지</b>됩니다.
          실제 값을 디스코드에 물어봐 확인합니다.
        </div>
      </div>

      <div v-if="permResult && permResult.ok && !permResult.hasAll" class="notice danger">
        <strong>권한이 부족합니다 — 지금 상태로는 채널·역할을 만들 수 없습니다</strong>
        없는 권한: <b>{{ permResult.missing.map((m) => m.label).join(', ') }}</b>
        <div class="mt-8">
          해결 방법은 둘 중 하나입니다.<br />
          · <b>초대 링크로 다시 승인</b> — 위 ③ 의 [초대 링크 열기] 를 다시 누르고 권한 체크를 모두 켠 채 승인<br />
          · <b>서버 설정 &gt; 역할</b> 에서 봇 역할에 해당 권한을 직접 켜기
        </div>
        <div class="mt-8">
          ⚠️ <b>역할 관리</b> 가 없으면 설치 후 <b>계정 연동 시 캠퍼스 역할 부여가 조용히 실패</b>합니다.
          채널을 손으로 만들어 넘어가도 이 기능은 동작하지 않습니다.
        </div>
      </div>

      <div v-else-if="permResult && permResult.ok && permResult.isAdmin" class="notice warn">
        <strong>봇이 관리자(ADMINISTRATOR) 권한을 가지고 있습니다</strong>
        필요한 권한은 모두 충족하지만, 관리자 권한은 이 서비스가 요구하는 범위를 크게 넘습니다.
        운영 서버에서는 필요한 6종만 주는 편이 안전합니다.
      </div>

      <div v-else-if="permResult && permResult.ok" class="notice success">
        <strong>필요한 권한 6종을 모두 확인했습니다</strong>
        {{
          permResult.have && permResult.have.length
            ? permResult.have.map((h) => h.label).join(' · ')
            : '채널 관리 · 채널 보기 · 메시지 보내기 · 링크 첨부 · 메시지 기록 보기 · 역할 관리'
        }}
      </div>

      <div v-else-if="permResult && !permResult.ok" class="notice warn">
        <strong>권한을 확인하지 못했습니다</strong>
        {{ permResult.message }}
      </div>

      <div class="field">
        <label class="field-label">연동 안내 채널 이름</label>
        <input v-model="linkChannelName" type="text" placeholder="플북-연동안내" />
        <div class="field-hint">
          이미 같은 이름의 채널이 있으면 새로 만들지 않고 그대로 재사용합니다.
        </div>
      </div>

      <div class="notice info">
        <strong>마법사가 만드는 것</strong>
        · 연동 안내 채널 <code class="inline">#{{ linkChannelName }}</code> → 환경변수 <code class="inline">DISCORD_LINK_CHANNEL_ID</code><br />
        · 캠퍼스 채널 <code class="inline">#{{ (campusName || '캠퍼스') + '-라운지' }}</code> → 대여·반납 알림 발송 대상<br />
        · 캠퍼스 역할 <code class="inline">플북-{{ campusName || '캠퍼스' }}</code> → 계정 연동 시 부여되어 캠퍼스 채널을 열어 줍니다<br />
        캠퍼스 채널은 <b>@everyone 은 볼 수 없고 역할을 받은 사람만</b> 보이도록 권한이 설정됩니다.
      </div>

      <button class="primary" :disabled="!tokenVerified || !selectedGuild || busy === 'provision'" @click="provision">
        {{ busy === 'provision' ? '생성 중…' : provisioned ? '다시 실행 (이미 있으면 재사용)' : '채널·역할 자동 생성' }}
      </button>

      <div class="mt-12">
        <LogView :lines="logs" height="190px" placeholder="생성 로그가 여기에 표시됩니다." />
      </div>

      <div v-if="provisionResult && provisionResult.ok" class="notice success mt-12">
        <strong>준비 완료</strong>
        <div v-if="provisionResult.created?.length">생성: {{ provisionResult.created.join(' / ') }}</div>
        <div v-if="provisionResult.reused?.length">재사용: {{ provisionResult.reused.join(' / ') }}</div>
      </div>

      <div v-if="provisionResult && provisionResult.warnings?.length" class="notice warn mt-8">
        <strong>확인이 필요한 사항</strong>
        <div v-for="(w, i) in provisionResult.warnings" :key="i">· {{ w }}</div>
      </div>

      <div v-if="provisionResult && !provisionResult.ok" class="notice danger mt-12">
        <strong>실패했습니다</strong>
        {{ provisionResult.message }}
        <div class="mt-8">위 원인을 해결한 뒤 [채널·역할 자동 생성] 을 다시 누르세요. 이미 만들어진 것은 재사용됩니다.</div>
      </div>
    </div>

    <div v-if="provisioned" class="notice success">
      <strong>채널·역할 생성 완료 — 추가 등록은 필요 없습니다</strong>
      아래 값은 <code class="inline">.env</code> 로 전달되어, 백엔드가 처음 기동할 때
      <code class="inline">tb_campus_channel</code> 에 자동으로 등록됩니다.
      나중에 바꾸려면 <b>관리자 화면 &gt; 연동 탭 &gt; 캠퍼스 채널 매핑</b>에서 수정하세요
      (화면에서 고친 값을 재기동이 덮어쓰지 않습니다). 10단계 백업 파일에도 기록됩니다.
      <div class="mono selectable mt-8">역할 ID: {{ d.campusRoleId }} · 채널 ID: {{ d.campusChannelId }}</div>
    </div>
  </template>

  <StepNav
    :next-disabled="!canProceed"
    next-reason="채널·역할 생성을 완료하거나 이 단계를 건너뛰어야 합니다."
  >
    <template #extra>
      <button v-if="!skipped" class="ghost" @click="toggleSkip">디스코드 없이 진행</button>
    </template>
  </StepNav>
</template>
