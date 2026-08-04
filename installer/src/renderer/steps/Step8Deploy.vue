<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { store, toast } from '../store'
import StepNav from '../components/StepNav.vue'
import LogView from '../components/LogView.vue'
import StatusPill from '../components/StatusPill.vue'

const summary = ref(null)
const logs = ref([])
const running = ref(false)
const finished = ref(false)
const services = ref([])

let unsubscribe = null

onMounted(async () => {
  await loadSummary()
  unsubscribe = window.wizard.onDeployLog((p) => logs.value.push({ line: p.line, stream: p.stream }))
  const ps = await window.wizard.deployPs()
  if (ps.ok) services.value = ps.services
  if (store.state.deploy && store.state.deploy.dbUppedAt) finished.value = true
})

onUnmounted(() => unsubscribe && unsubscribe())

async function loadSummary() {
  const r = await window.wizard.deploySummary()
  if (r.ok) summary.value = r.summary
}

async function run() {
  running.value = true
  finished.value = false
  logs.value = []
  try {
    const r = await window.wizard.deployRun()
    if (r.state) store.state = r.state
    if (r.ok) {
      finished.value = true
      services.value = r.services || []
      toast('설정 파일 생성과 데이터베이스 기동이 끝났습니다. 9단계로 진행하세요.', 'success', 7000)
    } else {
      toast(r.message || '배포에 실패했습니다.', 'error', 9000)
    }
  } finally {
    running.value = false
  }
}

async function refreshPs() {
  const r = await window.wizard.deployPs()
  if (r.ok) services.value = r.services
}

async function showFolder(p) {
  if (!p) return
  await window.wizard.showInFolder(p)
}

const verifiedLabel = (v) => (v === true ? '검증 완료' : v === false ? '검증 실패' : '미검증')
const verifiedStatus = (v) => (v === true ? 'ok' : v === false ? 'fail' : 'idle')
</script>

<template>
  <h1 class="page-title">8. 설정 확인 · 배포 <span class="faint" style="font-size: 15px">(1/2 — 데이터베이스)</span></h1>
  <p class="page-lead">
    지금까지 입력한 내용을 확인하고, 설정 파일을 만든 뒤 이미지를 내려받아
    <b>데이터베이스 컨테이너만 먼저</b> 기동합니다. 비밀번호와 키는 아래 요약에서 가려져 표시됩니다.
  </p>

  <div class="notice warn">
    <strong>왜 데이터베이스만 먼저 올리나요?</strong>
    운영 백엔드는 스키마 검증 모드(<code class="inline">ddl-auto=validate</code>)로 동작합니다.
    마이그레이션이 적용되지 않은 상태에서 백엔드를 올리면 <b>기동 자체가 실패</b>합니다.
    그래서 순서가 <b>DB 기동 → 9단계 마이그레이션 → 나머지 서비스 기동</b> 입니다.
  </div>

  <template v-if="summary">
    <h2 class="section-title">설정 요약</h2>
    <div class="card">
      <table class="kv">
        <tbody>
          <tr>
            <th>설치 경로</th>
            <td class="mono selectable">{{ store.state.installDir }}</td>
          </tr>
          <tr>
            <th>캠퍼스</th>
            <td>
              {{ summary.campus.name }}
              <span v-if="summary.campus.campusId" class="faint">(seq_campus = {{ summary.campus.campusId }})</span>
              <div v-if="summary.campus.customCampusWarning" class="check-hint">
                기본 캠퍼스가 아니어서 디스코드 채널 환경변수 슬롯이 없습니다. 설치 후 연동 탭에서 등록하세요.
              </div>
            </td>
          </tr>
          <tr>
            <th>DB 비밀번호</th>
            <td class="mono">{{ summary.generated.dbPassword }}</td>
          </tr>
          <tr>
            <th>연동 암호화 키</th>
            <td class="mono">{{ summary.generated.integrationSecretKey }}</td>
          </tr>
          <tr>
            <th>네이버 API</th>
            <td>
              <StatusPill :status="verifiedStatus(summary.apiKeys.verified.naver)" :labels="{ ok: '검증 완료', fail: '검증 실패', idle: '미검증' }" />
              <span class="mono faint">&nbsp;{{ summary.apiKeys.naverClientId }} / {{ summary.apiKeys.naverClientSecret }}</span>
            </td>
          </tr>
          <tr>
            <th>국립중앙도서관</th>
            <td>
              <StatusPill :status="verifiedStatus(summary.apiKeys.verified.nl)" :labels="{ ok: '검증 완료', fail: '검증 실패', idle: '미검증' }" />
              <span class="mono faint">&nbsp;{{ summary.apiKeys.nlApiKey }}</span>
            </td>
          </tr>
          <tr>
            <th>Work24</th>
            <td>
              <StatusPill :status="verifiedStatus(summary.apiKeys.verified.work24)" :labels="{ ok: '검증 완료', fail: '검증 실패', idle: '미검증' }" />
              <span class="mono faint">&nbsp;{{ summary.apiKeys.work24ApiKey }}</span>
            </td>
          </tr>
          <tr>
            <th>디스코드</th>
            <td>
              <span v-if="summary.discord.skipped" class="muted">건너뜀 — 설치 후 연동 탭에서 등록</span>
              <span v-else>
                봇 <b>{{ summary.discord.botName }}</b> · 서버 {{ summary.discord.guildName || summary.discord.guildId }}<br />
                <span class="faint mono">
                  연동채널 {{ summary.discord.linkChannelId }} · 캠퍼스채널 {{ summary.discord.campusChannelId }} ·
                  역할 {{ summary.discord.campusRoleId }}
                </span>
              </span>
            </td>
          </tr>
          <tr>
            <th>마스터 관리자</th>
            <td>{{ summary.master.id }} / {{ summary.master.pw }} ({{ summary.master.name }}, @{{ summary.master.discord }})</td>
          </tr>
          <tr>
            <th>접속 허용 IP</th>
            <td>
              <span v-if="summary.ipAllowlist.empty" class="check-hint">규칙 없음 — 전면 허용 상태로 기동합니다</span>
              <span v-else class="mono selectable">{{ summary.ipAllowlist.entries.join(', ') }}</span>
            </td>
          </tr>
          <tr>
            <th>중앙 모니터링</th>
            <td>
              <span v-if="!summary.monitoring.enabled" class="muted">사용 안 함</span>
              <span v-else>
                {{ summary.monitoring.endpoint }} · 라벨 <b>{{ summary.monitoring.campusLabel }}</b> ·
                시크릿 <span class="mono">{{ summary.monitoring.bootstrapSecret }}</span>
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </template>

  <div class="notice info">
    <strong>이제 하는 일</strong>
    ① 배포 파일을 설치 경로로 복사 → ② <code class="inline">back/.env.prod</code>,
    <code class="inline">db/.env.prod</code> 생성 → ③ compose 파일 검증 →
    ④ <code class="inline">docker compose pull</code> →
    ⑤ <code class="inline">docker compose up -d --build db</code> 후 접속 대기
    <div class="mt-8 faint">
      회선 상태에 따라 이미지 내려받기에 5~20분이 걸릴 수 있습니다. 창을 닫지 마세요.
      기존 <code class="inline">.env.prod</code> 파일이 있으면 덮어쓰지 않고 보존합니다.
    </div>
  </div>

  <div class="card">
    <div class="row mb-8">
      <button class="primary" :disabled="running" @click="run">
        {{ running ? '진행 중…' : finished ? '다시 실행' : '배포 시작 (설정 생성 + DB 기동)' }}
      </button>
      <button class="small" :disabled="running" @click="refreshPs">컨테이너 상태 새로고침</button>
      <span v-if="store.state.deploy && store.state.deploy.dbUppedAt" class="faint" style="font-size: 12px">
        DB 기동: {{ new Date(store.state.deploy.dbUppedAt).toLocaleString('ko-KR') }}
      </span>
    </div>

    <LogView :lines="logs" height="330px" placeholder="[배포 시작] 을 누르면 진행 로그가 실시간으로 표시됩니다." />
  </div>

  <div v-if="services.length" class="card">
    <div class="card-head"><span class="card-title">컨테이너 상태</span></div>
    <div v-for="s in services" :key="s.name" class="check-row">
      <StatusPill :status="/running|up/i.test(s.state || s.status || '') ? 'ok' : 'fail'" />
      <div class="grow">
        <b>{{ s.service }}</b> <span class="faint mono">{{ s.name }}</span>
        <div class="check-detail">{{ s.status || s.state }}</div>
      </div>
    </div>
  </div>

  <div v-if="finished" class="notice success">
    <strong>1/2 단계 완료 — 설정 파일 생성 · 데이터베이스 기동</strong>
    <button class="small link" @click="showFolder(store.state.deploy.backEnvPath)">back/.env.prod 위치 열기</button><br />
    다음 단계에서 마이그레이션을 적용한 뒤, 백엔드·프론트를 기동합니다.
  </div>

  <StepNav :next-disabled="!finished" next-reason="설정 생성과 DB 기동을 먼저 완료하세요." next-label="DB 마이그레이션으로" />
</template>
