<template>
  <div class="integration-management">
    <header class="im-header">
      <h2>연동 관리</h2>
      <p class="im-sub">디스코드 봇·채널/역할, 외부 API(Work24·네이버) 설정을 관리하고 연동 상태를 테스트합니다.</p>
    </header>

    <!-- 디스코드 봇 상태 -->
    <section class="im-card">
      <div class="im-card-head">
        <h3>디스코드 봇</h3>
        <span class="status-dot" :class="discord.connected ? 'on' : 'off'"></span>
        <span class="status-text">{{ discord.checked ? (discord.connected ? '연결됨' : '연결 안 됨') : '미확인' }}</span>
      </div>
      <div class="im-actions">
        <button class="btn" @click="runDiscordTest" :disabled="discord.loading">연결 테스트</button>
        <button class="btn btn-ghost" @click="runReconnect" :disabled="discord.loading">봇 재연결</button>
        <a v-if="discord.settingsUrl" class="btn btn-link" :href="discord.settingsUrl" target="_blank" rel="noopener">
          봇 권한·설정 변경 ↗
        </a>
      </div>
      <p v-if="discord.message" class="im-msg" :class="discord.connected ? 'ok' : 'err'">{{ discord.message }}</p>
      <ul v-if="discord.guilds.length" class="guild-list">
        <li v-for="g in discord.guilds" :key="g.id">
          <strong>{{ g.name }}</strong>
          <span class="perm">{{ (g.permissions || []).join(', ') }}</span>
        </li>
      </ul>
    </section>

    <!-- 설정 값 (카테고리별) -->
    <section v-for="cat in categories" :key="cat.key" class="im-card">
      <div class="im-card-head">
        <h3>{{ cat.label }}</h3>
        <template v-if="cat.key === 'WORK24'">
          <button class="btn btn-sm" @click="runWork24Test" :disabled="work24.loading">호출 테스트</button>
          <button class="btn btn-sm btn-ghost" @click="runWork24Sync" :disabled="work24.loading">지금 동기화</button>
        </template>
        <template v-else-if="cat.key === 'NAVER'">
          <button class="btn btn-sm" @click="runNaverTest" :disabled="naver.loading">호출 테스트</button>
        </template>
        <template v-else-if="cat.key === 'NL'">
          <button class="btn btn-sm" @click="runNlTest" :disabled="nl.loading">호출 테스트</button>
        </template>
      </div>

      <div v-for="cfg in configsByCategory(cat.key)" :key="cfg.configKey" class="cfg-row">
        <label :for="`cfg-${cfg.configKey}`">
          {{ cfg.description || cfg.configKey }}
          <span v-if="cfg.isSecret" class="badge-secret">비밀</span>
        </label>
        <div class="cfg-input">
          <input
            :id="`cfg-${cfg.configKey}`"
            v-model="edits[cfg.configKey]"
            :type="cfg.isSecret ? 'password' : 'text'"
            :placeholder="cfg.isSecret ? (cfg.configured ? '설정됨 — 변경하려면 새 값 입력' : '미설정') : ''"
            class="form-input"
          />
          <button class="btn btn-sm" @click="saveConfig(cfg)">저장</button>
        </div>
      </div>

      <p v-if="cat.key === 'WORK24' && work24.message" class="im-msg" :class="work24.ok ? 'ok' : 'err'">{{ work24.message }}</p>
      <p v-if="cat.key === 'NAVER' && naver.message" class="im-msg" :class="naver.ok ? 'ok' : 'err'">{{ naver.message }}</p>
      <p v-if="cat.key === 'NL' && nl.message" class="im-msg" :class="nl.ok ? 'ok' : 'err'">{{ nl.message }}</p>
    </section>

    <!-- 캠퍼스별 채널/역할 매핑 -->
    <section class="im-card">
      <div class="im-card-head">
        <h3>캠퍼스별 디스코드 채널·역할</h3>
      </div>
      <p class="im-hint">
        역할(Role) ID를 지정하면 플북 계정 연동 시 해당 캠퍼스 채널이 자동 해금됩니다.
        채널(Channel) ID는 캠퍼스 알림 발송 대상입니다.
      </p>
      <table class="im-table">
        <thead>
          <tr><th>캠퍼스</th><th>채널 ID</th><th>역할 ID</th><th></th></tr>
        </thead>
        <tbody>
          <tr v-for="row in campusRows" :key="row.seqCampus">
            <td>{{ row.campusName }}</td>
            <td><input v-model="row.discordChannelId" class="form-input" placeholder="채널 ID" /></td>
            <td><input v-model="row.discordRoleId" class="form-input" placeholder="역할 ID" /></td>
            <td><button class="btn btn-sm" @click="saveCampusRow(row)">저장</button></td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import * as integrationApi from '@/api/integration'
import * as campusApi from '@/api/campus'
import { swAlert } from '@/utils/sweetAlert'

const categories = [
  { key: 'DISCORD', label: '디스코드 설정' },
  { key: 'WORK24', label: 'Work24 (훈련과정)' },
  { key: 'NAVER', label: '네이버 책 검색' },
  { key: 'NL', label: '국립중앙도서관 (ISBN)' },
]

const configs = ref([])
const edits = reactive({})
const campusRows = ref([])

const discord = reactive({ checked: false, connected: false, message: '', settingsUrl: '', guilds: [], loading: false })
const work24 = reactive({ message: '', ok: false, loading: false })
const naver = reactive({ message: '', ok: false, loading: false })
const nl = reactive({ message: '', ok: false, loading: false })

const configsByCategory = (cat) => configs.value.filter(c => c.category === cat)

async function loadConfigs() {
  const res = await integrationApi.getConfigs()
  configs.value = res.data.data || []
  configs.value.forEach(c => {
    // 시크릿은 빈 값으로 시작(미입력 시 변경하지 않음), 일반 값은 현재 값 노출
    edits[c.configKey] = c.isSecret ? '' : (c.configValue || '')
  })
}

async function loadCampusChannels() {
  const [chRes, campusRes] = await Promise.all([
    integrationApi.getCampusChannels(),
    campusApi.getAll(),
  ])
  const mappings = chRes.data.data || []
  const campuses = campusRes.data.data || []
  const byId = new Map(mappings.map(m => [m.seqCampus, m]))
  // 모든 활성 캠퍼스를 행으로 보여주고, 기존 매핑이 있으면 채움
  campusRows.value = campuses.map(c => {
    const m = byId.get(c.seqCampus)
    return {
      seqCampus: c.seqCampus,
      campusName: c.nameCampus,
      discordChannelId: m?.discordChannelId || '',
      discordRoleId: m?.discordRoleId || '',
    }
  })
}

async function saveConfig(cfg) {
  const value = edits[cfg.configKey]
  if (cfg.isSecret && (value === '' || value == null)) {
    await swAlert('변경할 값을 입력해주세요.', 'warning')
    return
  }
  try {
    await integrationApi.updateConfig(cfg.configKey, value)
    await swAlert('저장되었습니다.', 'success')
    await loadConfigs()
  } catch (e) {
    await swAlert('저장 실패: ' + (e.response?.data?.msg || e.message), 'error')
  }
}

async function saveCampusRow(row) {
  try {
    await integrationApi.updateCampusChannel(row.seqCampus, {
      discordChannelId: row.discordChannelId || null,
      discordRoleId: row.discordRoleId || null,
    })
    await swAlert(`${row.campusName} 매핑이 저장되었습니다.`, 'success')
  } catch (e) {
    await swAlert('저장 실패: ' + (e.response?.data?.msg || e.message), 'error')
  }
}

async function runDiscordTest() {
  discord.loading = true
  try {
    const res = await integrationApi.testDiscord()
    const d = res.data.data
    discord.checked = true
    discord.connected = d.success
    discord.message = d.message
    discord.settingsUrl = d.settingsUrl || ''
    discord.guilds = d.detail?.guilds || []
  } catch (e) {
    discord.checked = true
    discord.connected = false
    discord.message = '테스트 실패: ' + (e.response?.data?.msg || e.message)
  } finally {
    discord.loading = false
  }
}

async function runReconnect() {
  discord.loading = true
  try {
    const res = await integrationApi.reconnectDiscord()
    const d = res.data.data
    discord.checked = true
    discord.connected = d.success
    discord.message = d.message
  } catch (e) {
    discord.message = '재연결 실패: ' + (e.response?.data?.msg || e.message)
  } finally {
    discord.loading = false
  }
}

async function runWork24Test() {
  work24.loading = true
  try {
    const res = await integrationApi.testWork24()
    const d = res.data.data
    work24.ok = d.success
    work24.message = d.message
  } catch (e) {
    work24.ok = false
    work24.message = '테스트 실패: ' + (e.response?.data?.msg || e.message)
  } finally {
    work24.loading = false
  }
}

async function runWork24Sync() {
  work24.loading = true
  try {
    const res = await integrationApi.syncWork24()
    const d = res.data.data
    work24.ok = d.success
    work24.message = d.message
    await swAlert(d.message, d.success ? 'success' : 'error')
  } catch (e) {
    work24.ok = false
    work24.message = '동기화 실패: ' + (e.response?.data?.msg || e.message)
  } finally {
    work24.loading = false
  }
}

async function runNaverTest() {
  naver.loading = true
  try {
    const res = await integrationApi.testNaver()
    const d = res.data.data
    naver.ok = d.success
    naver.message = d.message
  } catch (e) {
    naver.ok = false
    naver.message = '테스트 실패: ' + (e.response?.data?.msg || e.message)
  } finally {
    naver.loading = false
  }
}

async function runNlTest() {
  nl.loading = true
  try {
    const res = await integrationApi.testNl()
    const d = res.data.data
    nl.ok = d.success
    nl.message = d.message
  } catch (e) {
    nl.ok = false
    nl.message = '테스트 실패: ' + (e.response?.data?.msg || e.message)
  } finally {
    nl.loading = false
  }
}

onMounted(async () => {
  try {
    await Promise.all([loadConfigs(), loadCampusChannels()])
  } catch (e) {
    await swAlert('연동 설정을 불러오지 못했습니다: ' + (e.response?.data?.msg || e.message), 'error')
  }
})
</script>

<style scoped>
.integration-management {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.im-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--pb-color-heading);
}

.im-sub {
  margin: 0.25rem 0 0;
  font-size: 0.9rem;
  color: var(--pb-color-text-muted);
}

.im-card {
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-lg);
  padding: 1.25rem;
}

.im-card-head {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  margin-bottom: 1rem;
}

.im-card-head h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--pb-color-heading);
  flex: 1;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
}

.status-dot.on { background: var(--pb-color-success); box-shadow: 0 0 0 3px var(--pb-color-success-soft); }
.status-dot.off { background: var(--pb-color-danger); box-shadow: 0 0 0 3px var(--pb-color-danger-soft); }

.status-text { font-size: 0.875rem; color: var(--pb-color-text-muted); }

.im-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.btn {
  padding: 7px 14px;
  border: 1px solid var(--pb-color-brand);
  background: var(--pb-color-brand);
  color: #fff;
  border-radius: var(--pb-radius-md);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
}

.btn:hover:not(:disabled) { background: var(--pb-color-brand-strong); }
.btn:disabled { opacity: 0.6; cursor: not-allowed; }

.btn-sm { padding: 5px 11px; font-size: 0.82rem; }

.btn-ghost {
  background: transparent;
  color: var(--pb-color-brand);
}
.btn-ghost:hover:not(:disabled) { background: var(--pb-color-brand-soft); }

.btn-link {
  background: transparent;
  border-color: transparent;
  color: var(--pb-color-brand);
}

.im-msg { margin: 0.5rem 0 0; font-size: 0.875rem; }
.im-msg.ok { color: var(--pb-color-success); }
.im-msg.err { color: var(--pb-color-danger); }

.im-hint {
  margin: 0 0 0.75rem;
  font-size: 0.85rem;
  color: var(--pb-color-text-muted);
}

.guild-list {
  list-style: none;
  margin: 0.75rem 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.guild-list li {
  font-size: 0.82rem;
  color: var(--pb-color-text);
}

.guild-list .perm {
  display: block;
  color: var(--pb-color-text-soft);
  font-size: 0.75rem;
}

.cfg-row {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
  margin-bottom: 0.875rem;
}

.cfg-row label {
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--pb-color-text);
}

.badge-secret {
  display: inline-block;
  margin-left: 6px;
  padding: 1px 7px;
  font-size: 0.7rem;
  border-radius: var(--pb-radius-sm);
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text-muted);
}

.cfg-input {
  display: flex;
  gap: 0.5rem;
}

.form-input {
  flex: 1;
  width: 100%;
  padding: 8px 11px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-md);
  font-size: 0.875rem;
  background: var(--pb-color-surface);
  color: var(--pb-color-text);
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: var(--pb-color-brand);
  box-shadow: 0 0 0 3px var(--pb-color-brand-soft);
}

.im-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}

.im-table th, .im-table td {
  padding: 8px 10px;
  border-bottom: 1px solid var(--pb-color-border);
  text-align: left;
}

.im-table th {
  font-weight: 600;
  color: var(--pb-color-text-muted);
}

.im-table td .form-input { padding: 6px 9px; }
</style>
