<script setup>
import { computed, onMounted, ref } from 'vue'
import { store, toast } from '../store'
import StepNav from '../components/StepNav.vue'
import StatusPill from '../components/StatusPill.vue'
import ProxyPanel from '../components/ProxyPanel.vue'

const health = ref(null)
const checking = ref(false)
const shortcut = ref(null)

const state = computed(() => store.state)
const backupPath = computed(() => (state.value.finish && state.value.finish.backupPath) || (state.value.generated && state.value.generated.backupSavedTo))
const d = computed(() => state.value.discord)

onMounted(async () => {
  shortcut.value = state.value.finish ? state.value.finish.shortcutPath : null
  await check()
})

async function check() {
  checking.value = true
  try {
    const r = await window.wizard.healthCheck()
    if (r.ok) health.value = r.result
  } finally {
    checking.value = false
  }
}

async function makeShortcut() {
  const r = await window.wizard.createShortcut()
  if (r.ok) {
    shortcut.value = r.path
    toast('바탕화면에 바로가기를 만들었습니다.', 'success')
  } else {
    toast(r.message || '바로가기 생성에 실패했습니다.', 'error')
  }
}

async function saveBackup() {
  const r = await window.wizard.saveBackup()
  if (r.ok) {
    const s = await window.wizard.getState()
    if (s.ok) store.state = s.state
    toast(`백업 파일을 저장했습니다: ${r.path}`, 'success', 8000)
  } else if (!r.canceled) {
    toast(r.message || '백업 저장에 실패했습니다.', 'error')
  }
}

async function open(url) {
  const r = await window.wizard.openExternal(url)
  if (!r.ok) toast(r.message, 'error')
}

async function showFolder(p) {
  if (p) await window.wizard.showInFolder(p)
}

const healthy = computed(() => !!(health.value && health.value.healthy))
const needsRoleMapping = computed(() => !!(d.value && !d.value.skipped && d.value.campusRoleId))
</script>

<template>
  <h1 class="page-title">10. 설치 완료</h1>
  <p class="page-lead">서비스가 정상적으로 떴는지 확인하고, 마무리 작업을 진행합니다.</p>

  <h2 class="section-title">헬스체크</h2>
  <div class="card">
    <div v-if="!health" class="muted">확인 중…</div>
    <div v-else>
      <div v-for="c in health.checks" :key="c.id" class="check-row">
        <StatusPill :status="c.status" />
        <div class="grow">
          <div style="font-weight: 600">{{ c.label }}</div>
          <div class="check-detail">{{ c.detail }}</div>
          <div v-if="c.hint" class="check-hint">→ {{ c.hint }}</div>
        </div>
      </div>
    </div>
    <div class="row mt-12">
      <button class="small" :disabled="checking" @click="check">{{ checking ? '확인 중…' : '다시 확인' }}</button>
      <span class="faint" style="font-size: 12px">
        방금 기동했다면 백엔드 초기화에 1~3분이 걸립니다. 실패로 나와도 잠시 후 다시 확인해 보세요.
      </span>
    </div>
  </div>

  <div v-if="healthy" class="notice success">
    <strong>서비스가 정상 동작 중입니다</strong>
    아래 주소로 접속하세요.
  </div>
  <div v-else-if="health" class="notice warn">
    <strong>아직 일부 항목이 준비되지 않았습니다</strong>
    설치 경로에서 <code class="inline">docker compose -f docker-compose.prod.yml logs back</code> 으로
    백엔드 로그를 확인하면 원인을 알 수 있습니다. 스키마 검증 실패라면 9단계로 돌아가 미적용
    마이그레이션이 남아 있는지 확인하세요.
  </div>

  <h2 class="section-title">접속 정보</h2>
  <div class="card">
    <table class="kv">
      <tbody>
        <tr>
          <th>서비스 주소</th>
          <td>
            <span class="mono selectable">http://localhost</span>
            <button class="small link" style="margin-left: 8px" @click="open('http://localhost/')">열기 ↗</button>
          </td>
        </tr>
        <tr>
          <th>같은 네트워크의 다른 PC</th>
          <td class="mono selectable">
            http://{{ state.ipAllowlist?.detected?.primary?.address || '이-PC-의-IP' }}
          </td>
        </tr>
        <tr>
          <th>관리자 아이디</th>
          <td class="mono selectable">{{ state.master?.id }}</td>
        </tr>
        <tr>
          <th>설치 경로</th>
          <td>
            <span class="mono selectable">{{ state.installDir }}</span>
            <button class="small link" style="margin-left: 8px" @click="showFolder(state.installDir)">폴더 열기</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>

  <ProxyPanel />

  <h2 class="section-title">마무리</h2>
  <div class="card">
    <div class="check-row">
      <StatusPill :status="shortcut ? 'ok' : 'idle'" :labels="{ ok: '생성됨', idle: '미생성' }" />
      <div class="grow">
        <b>바탕화면 바로가기</b>
        <div class="check-detail">
          {{ shortcut || '바탕화면에 "Playbook 도서관리" 바로가기를 만듭니다.' }}
        </div>
      </div>
      <button class="small" @click="makeShortcut">{{ shortcut ? '다시 만들기' : '만들기' }}</button>
    </div>

    <div class="check-row">
      <StatusPill :status="backupPath ? 'ok' : 'fail'" :labels="{ ok: '저장됨', fail: '미저장' }" />
      <div class="grow">
        <b>설정 백업 파일</b>
        <div class="check-detail">
          {{ backupPath || '비밀번호·API 키·디스코드 ID 를 한 파일로 저장합니다. 반드시 보관하세요.' }}
        </div>
        <div v-if="!backupPath" class="check-hint">
          INTEGRATION_SECRET_KEY 를 잃으면 DB 에 저장된 연동값을 복호화할 수 없습니다.
        </div>
      </div>
      <button class="small primary" @click="saveBackup">{{ backupPath ? '다시 저장' : '저장' }}</button>
      <button v-if="backupPath" class="small" @click="showFolder(backupPath)">위치 열기</button>
    </div>

    <div class="check-row">
      <StatusPill status="warn" :labels="{ warn: '수동' }" />
      <div class="grow">
        <b>설정 파일 접근 권한 확인</b>
        <div class="check-detail">
          <span class="mono">{{ state.deploy?.backEnvPath }}</span> 에 비밀번호가 평문으로 들어 있습니다.
          해당 폴더 속성 &gt; 보안 탭에서 다른 사용자 계정의 읽기 권한을 제거하세요.
        </div>
      </div>
      <button class="small" @click="showFolder(state.deploy?.backEnvPath)">위치 열기</button>
    </div>
  </div>

  <div v-if="needsRoleMapping" class="notice success">
    <strong>디스코드 캠퍼스 매핑 — 자동 등록됨</strong>
    아래 값이 <code class="inline">.env</code> 를 거쳐 <code class="inline">tb_campus_channel</code> 에
    자동으로 등록되었습니다. 계정 연동 시 캠퍼스 채널이 열립니다.
    확인·수정은 <b>관리자 화면 &gt; 연동 탭 &gt; 캠퍼스 채널 매핑</b>에서 하세요.
    <div class="mono selectable mt-8">
      역할 ID: {{ d.campusRoleId }}<br />
      채널 ID: {{ d.campusChannelId }}
    </div>
  </div>

  <div v-if="state.ipAllowlist && state.ipAllowlist.entries.length === 0" class="notice danger">
    <strong>접속 허용 IP 규칙이 0건입니다</strong>
    지금은 캠퍼스 네트워크의 누구나 접속할 수 있는 상태입니다.
    관리자로 로그인해 <b>관리자 화면 &gt; 접속 허용 IP</b> 에서 규칙을 등록하세요.
  </div>

  <div v-if="state.monitoring && !state.monitoring.enabled" class="notice info">
    <strong>중앙 모니터링을 사용하지 않는 설정입니다</strong>
    서버가 멈춰도 중앙에서 알 수 없습니다. 나중에 켜려면
    <code class="inline">back/.env.prod</code> 의 <code class="inline">MONITORING_REFRESH_ENABLED</code> 를
    <code class="inline">true</code> 로 바꾸고 관련 값을 채운 뒤 컨테이너를 재시작하세요.
  </div>

  <StepNav hide-next>
    <template #extra>
      <button class="primary" @click="open('http://localhost/')">Playbook 열기 ↗</button>
    </template>
  </StepNav>
</template>
