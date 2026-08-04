<template>
  <div class="allowed-ip-management">
    <header class="aip-header">
      <h2>허용 IP 관리</h2>
      <p class="aip-sub">
        서비스 접속을 허용할 IP·대역을 관리합니다. 활성 규칙에 해당하지 않는 위치에서는 화면과 API 모두 차단됩니다.
      </p>
    </header>

    <!-- 상태 배너 -->
    <section v-if="myIpLoaded" class="aip-banner" :class="`is-${bannerTone}`">
      <span class="aip-banner-icon" aria-hidden="true">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2" />
          <path d="M12 8V13" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
          <circle cx="12" cy="16.2" r="1.1" fill="currentColor" />
        </svg>
      </span>
      <div class="aip-banner-body">
        <p class="aip-banner-text">{{ bannerText }}</p>
        <p v-if="myIp.bypassActive" class="aip-banner-note">
          비상 우회(IP_ALLOWLIST_BYPASS) 환경변수가 설정되어 있습니다.
        </p>
      </div>
      <button class="btn btn-ghost btn-sm" @click="loadMyIp" :disabled="loading">상태 새로고침</button>
    </section>

    <!-- 툴바 -->
    <section class="aip-toolbar">
      <div class="aip-filters">
        <label v-if="isSuperAdmin" class="aip-field">
          <span class="aip-field-label">캠퍼스</span>
          <select v-model="filterCampus" class="form-input" @change="loadList">
            <option :value="ALL_CAMPUS">전체</option>
            <option v-for="c in campuses" :key="c.seqCampus" :value="c.seqCampus">{{ c.nameCampus }}</option>
          </select>
        </label>
        <label class="aip-check">
          <input type="checkbox" v-model="includeInactive" @change="loadList" />
          <span>비활성 규칙 포함</span>
        </label>
      </div>
      <div class="aip-actions">
        <button class="btn btn-ghost" @click="reloadAll" :disabled="loading">새로고침</button>
        <button class="btn" @click="openCreate" :disabled="loading">IP 등록</button>
      </div>
    </section>

    <!-- 목록 -->
    <section class="aip-card">
      <div class="aip-table-wrap">
        <table class="aip-table">
          <thead>
            <tr>
              <th>IP / 대역</th>
              <th>유형</th>
              <th>캠퍼스</th>
              <th>설명</th>
              <th>활성</th>
              <th>등록일</th>
              <th>작업</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="7" class="aip-empty">불러오는 중…</td>
            </tr>
            <tr v-else-if="!rules.length">
              <td colspan="7" class="aip-empty">등록된 허용 IP가 없습니다.</td>
            </tr>
            <tr
              v-for="rule in rules"
              :key="rule.seqAllowedIp"
              :class="{ 'is-mine': rule.matchedByRequester }"
            >
              <td>
                <span class="aip-ip">{{ rule.ipValue }}</span>
                <span v-if="rule.matchedByRequester" class="aip-badge badge-mine">현재 내 접속</span>
                <span v-if="rule.isSystem" class="aip-badge badge-system">기본 규칙</span>
              </td>
              <td>{{ ipTypeLabel(rule.ipType) }}</td>
              <td>{{ rule.seqCampus == null ? '전역' : (rule.campusName || `#${rule.seqCampus}`) }}</td>
              <td class="aip-desc">{{ rule.description || '-' }}</td>
              <td>
                <button
                  class="aip-toggle"
                  :class="rule.isActive ? 'on' : 'off'"
                  @click="toggleActive(rule)"
                  :disabled="loading"
                >
                  {{ rule.isActive ? '활성' : '비활성' }}
                </button>
              </td>
              <td>{{ formatDate(rule.createdAt) }}</td>
              <td>
                <div class="aip-row-actions">
                  <button class="btn btn-sm btn-ghost" @click="openEdit(rule)" :disabled="loading">수정</button>
                  <button
                    class="btn btn-sm btn-danger"
                    :disabled="rule.isSystem || loading"
                    :title="rule.isSystem ? SYSTEM_DELETE_HINT : '규칙 삭제'"
                    @click="removeRule(rule)"
                  >
                    삭제
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- 등록/수정 모달 -->
    <div v-if="showModal" class="aip-modal-overlay" @click="closeModal">
      <div class="aip-modal" @click.stop>
        <div class="aip-modal-head">
          <h3>{{ isEdit ? '허용 IP 수정' : '허용 IP 등록' }}</h3>
          <button class="aip-close" @click="closeModal" aria-label="닫기">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="2" />
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="2" />
            </svg>
          </button>
        </div>

        <div class="aip-modal-body">
          <p v-if="!isEdit && myIp.clientIp" class="aip-hint">
            현재 접속 IP는 <strong>{{ myIp.clientIp }}</strong> 입니다. 아래 빠른 선택으로 채울 수 있습니다.
          </p>
          <div v-if="!isEdit" class="aip-quick">
            <button
              class="btn btn-sm btn-ghost"
              :disabled="!myIp.clientIp"
              @click="form.ipValue = myIp.clientIp"
            >
              내 IP만 허용 ({{ myIp.clientIp || '-' }})
            </button>
            <button
              class="btn btn-sm btn-ghost"
              :disabled="!myIp.suggestedCidr"
              @click="form.ipValue = myIp.suggestedCidr"
            >
              내 대역 허용 ({{ myIp.suggestedCidr || '-' }})
            </button>
          </div>

          <label class="aip-field">
            <span class="aip-field-label">IP / 대역 <em>*</em></span>
            <input
              v-model.trim="form.ipValue"
              class="form-input"
              maxlength="64"
              placeholder="예) 192.168.0.15 또는 192.168.0.0/24"
            />
          </label>

          <label v-if="isSuperAdmin" class="aip-field">
            <span class="aip-field-label">캠퍼스</span>
            <select v-model="form.seqCampus" class="form-input">
              <option :value="null">전역 (모든 캠퍼스)</option>
              <option v-for="c in campuses" :key="c.seqCampus" :value="c.seqCampus">{{ c.nameCampus }}</option>
            </select>
          </label>

          <label class="aip-field">
            <span class="aip-field-label">설명</span>
            <input
              v-model.trim="form.description"
              class="form-input"
              maxlength="200"
              placeholder="예) 서초 라운지 LAN 대역"
            />
          </label>

          <label class="aip-check">
            <input type="checkbox" v-model="form.isActive" />
            <span>활성 상태로 저장</span>
          </label>
        </div>

        <div class="aip-modal-foot">
          <button class="btn btn-ghost" @click="closeModal" :disabled="saving">취소</button>
          <button class="btn" @click="submitForm" :disabled="saving">{{ saving ? '저장 중…' : '저장' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import * as allowedIpApi from '@/api/allowedIp'
import * as campusApi from '@/api/campus'
import * as adminApi from '@/api/admin'
import { swAlert, swConfirm } from '@/utils/sweetAlert'
import { handleApiError } from '@/utils/apiErrorHandler'
import { formatDate } from '@/utils/dateFormatter'

const ALL_CAMPUS = '__ALL__'
const SYSTEM_DELETE_HINT = '설치 마법사가 등록한 기본 규칙은 삭제할 수 없습니다. 비활성만 가능합니다.'
const FIRST_RULE_WARNING =
  '이 규칙을 등록하는 순간 여기에 해당하지 않는 모든 접속이 차단됩니다. 등록할 IP가 지금 접속 중인 위치를 포함하는지 확인하세요.'

const rules = ref([])
const campuses = ref([])
const isSuperAdmin = ref(false)
const loading = ref(false)
const saving = ref(false)
const myIpLoaded = ref(false)
const filterCampus = ref(ALL_CAMPUS)
const includeInactive = ref(true)

const myIp = reactive({
  clientIp: '',
  allowed: false,
  matchedRuleSeq: null,
  matchedRuleValue: '',
  filterEnabled: false,
  activeRuleCount: 0,
  bypassActive: false,
  suggestedCidr: '',
})

const showModal = ref(false)
const isEdit = ref(false)
const form = reactive({
  seqAllowedIp: null,
  ipValue: '',
  seqCampus: null,
  description: '',
  isActive: true,
})

const bannerTone = computed(() => {
  if (!myIp.filterEnabled) return 'muted'
  if (myIp.activeRuleCount === 0) return 'warning'
  if (!myIp.allowed) return 'danger'
  return 'ok'
})

const bannerText = computed(() => {
  if (!myIp.filterEnabled) return 'IP 차단이 비활성 상태입니다 (개발 환경)'
  if (myIp.activeRuleCount === 0) {
    return '활성 규칙이 0건이라 현재 모든 IP가 접속 가능합니다. 첫 규칙을 등록하면 즉시 차단이 시작됩니다.'
  }
  if (!myIp.allowed) {
    return `현재 접속 IP: ${myIp.clientIp} — 일치하는 활성 규칙이 없습니다. 규칙을 지우거나 바꾸기 전에 반드시 확인하세요.`
  }
  return `현재 접속 IP: ${myIp.clientIp} (규칙 #${myIp.matchedRuleSeq} 로 허용됨)`
})

const ipTypeLabel = (type) => (type === 'CIDR' ? '대역(CIDR)' : type === 'SINGLE' ? '단일 IP' : type || '-')

// 자기 차단 거부(4001/4002)는 서버 msg 를 그대로 노출한다
async function reportError(e, defaultMessage) {
  const code = e.response?.data?.code
  const msg = e.response?.data?.msg
  if ((code === '4001' || code === '4002') && msg) {
    await swAlert(msg, 'error')
    return
  }
  await handleApiError(e, defaultMessage)
}

async function loadMe() {
  try {
    const res = await adminApi.getMe()
    isSuperAdmin.value = !res.data.data?.seqCampus
  } catch (e) {
    isSuperAdmin.value = false
  }
}

async function loadCampuses() {
  if (!isSuperAdmin.value) return
  try {
    const res = await campusApi.getAll()
    campuses.value = res.data.data || []
  } catch (e) {
    campuses.value = []
  }
}

async function loadMyIp() {
  try {
    const res = await allowedIpApi.getMyIp()
    Object.assign(myIp, res.data.data || {})
    myIpLoaded.value = true
  } catch (e) {
    await reportError(e, '현재 접속 IP 정보를 불러오지 못했습니다.')
  }
}

async function loadList() {
  loading.value = true
  try {
    const params = { includeInactive: includeInactive.value }
    if (isSuperAdmin.value && filterCampus.value !== ALL_CAMPUS) {
      params.seqCampus = filterCampus.value
    }
    const res = await allowedIpApi.getList(params)
    rules.value = res.data.data || []
  } catch (e) {
    await reportError(e, '허용 IP 목록을 불러오지 못했습니다.')
  } finally {
    loading.value = false
  }
}

async function reloadAll() {
  await Promise.all([loadMyIp(), loadList()])
}

function openCreate() {
  isEdit.value = false
  form.seqAllowedIp = null
  form.ipValue = myIp.suggestedCidr || ''
  form.seqCampus = null
  form.description = ''
  form.isActive = true
  showModal.value = true
}

function openEdit(rule) {
  isEdit.value = true
  form.seqAllowedIp = rule.seqAllowedIp
  form.ipValue = rule.ipValue
  form.seqCampus = rule.seqCampus ?? null
  form.description = rule.description || ''
  form.isActive = rule.isActive
  showModal.value = true
}

function closeModal() {
  if (saving.value) return
  showModal.value = false
}

function buildPayload() {
  const payload = {
    ipValue: form.ipValue,
    description: form.description || null,
    isActive: form.isActive,
  }
  // 캠퍼스 관리자는 서버가 자기 캠퍼스로 강제하므로 보내지 않는다
  if (isSuperAdmin.value) payload.seqCampus = form.seqCampus
  return payload
}

async function submitForm() {
  if (!form.ipValue) {
    await swAlert('IP 또는 대역을 입력해주세요.', 'warning')
    return
  }

  // 활성 규칙 0건에서 첫 활성 규칙을 만들면 즉시 차단이 시작된다
  const startsBlocking = myIp.filterEnabled && myIp.activeRuleCount === 0 && form.isActive
  if (startsBlocking) {
    const ok = await swConfirm('첫 허용 규칙을 등록합니다', FIRST_RULE_WARNING, { isDangerous: true, icon: 'warning' })
    if (!ok) return
  }

  saving.value = true
  try {
    if (isEdit.value) {
      await allowedIpApi.update(form.seqAllowedIp, buildPayload())
      await swAlert('허용 IP가 수정되었습니다.', 'success')
    } else {
      await allowedIpApi.create(buildPayload())
      await swAlert('허용 IP가 등록되었습니다.', 'success')
    }
    showModal.value = false
    await reloadAll()
  } catch (e) {
    await reportError(e, isEdit.value ? '허용 IP 수정에 실패했습니다.' : '허용 IP 등록에 실패했습니다.')
  } finally {
    saving.value = false
  }
}

async function toggleActive(rule) {
  const next = !rule.isActive
  if (!next && rule.matchedByRequester) {
    const ok = await swConfirm(
      '현재 내 접속을 허용하는 규칙입니다',
      '비활성하면 지금 접속 중인 위치가 차단될 수 있습니다. 계속할까요?',
      { isDangerous: true, icon: 'warning' },
    )
    if (!ok) return
  }

  loading.value = true
  try {
    await allowedIpApi.setActive(rule.seqAllowedIp, next)
    await reloadAll()
  } catch (e) {
    await reportError(e, '활성 상태 변경에 실패했습니다.')
  } finally {
    loading.value = false
  }
}

async function removeRule(rule) {
  if (rule.isSystem) {
    await swAlert(SYSTEM_DELETE_HINT, 'warning')
    return
  }
  const ok = await swConfirm(
    '허용 IP를 삭제할까요?',
    rule.matchedByRequester
      ? `${rule.ipValue} 는 현재 접속 중인 위치를 허용하는 규칙입니다. 삭제하면 접속이 차단될 수 있습니다.`
      : `${rule.ipValue} 규칙을 삭제합니다.`,
    { isDangerous: true, icon: 'warning' },
  )
  if (!ok) return

  loading.value = true
  try {
    await allowedIpApi.remove(rule.seqAllowedIp)
    await swAlert('허용 IP가 삭제되었습니다.', 'success')
    await reloadAll()
  } catch (e) {
    await reportError(e, '허용 IP 삭제에 실패했습니다.')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadMe()
  await loadCampuses()
  await reloadAll()
})
</script>

<style scoped>
.allowed-ip-management {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.aip-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--pb-color-heading);
}

.aip-sub {
  margin: 0.25rem 0 0;
  font-size: 0.9rem;
  color: var(--pb-color-text-muted);
}

/* ── 상태 배너 ── */
.aip-banner {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.875rem 1rem;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-lg);
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text-muted);
}

.aip-banner.is-warning {
  background: var(--pb-color-warning-soft);
  border-color: var(--pb-color-warning);
  color: var(--pb-color-warning);
}

.aip-banner.is-danger {
  background: var(--pb-color-danger-soft);
  border-color: var(--pb-color-danger);
  color: var(--pb-color-danger);
}

.aip-banner.is-ok {
  background: var(--pb-color-success-soft);
  border-color: var(--pb-color-success);
  color: var(--pb-color-success);
}

.aip-banner-icon {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  margin-top: 2px;
}

.aip-banner-body {
  flex: 1;
}

.aip-banner-text {
  margin: 0;
  font-size: 0.875rem;
  font-weight: 500;
}

.aip-banner-note {
  margin: 0.25rem 0 0;
  font-size: 0.8rem;
  opacity: 0.85;
}

/* ── 툴바 ── */
.aip-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  justify-content: space-between;
  gap: 0.75rem;
}

.aip-filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.75rem;
}

.aip-actions {
  display: flex;
  gap: 0.5rem;
}

.aip-field {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.aip-field-label {
  font-size: 0.82rem;
  font-weight: 500;
  color: var(--pb-color-text-muted);
}

.aip-field-label em {
  font-style: normal;
  color: var(--pb-color-danger);
}

.aip-check {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  font-size: 0.85rem;
  color: var(--pb-color-text);
  cursor: pointer;
}

/* ── 카드·테이블 ── */
.aip-card {
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-lg);
  padding: 0.5rem;
}

.aip-table-wrap {
  overflow-x: auto;
}

.aip-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}

.aip-table th,
.aip-table td {
  padding: 9px 10px;
  border-bottom: 1px solid var(--pb-color-border);
  text-align: left;
  vertical-align: middle;
}

.aip-table th {
  font-weight: 600;
  color: var(--pb-color-text-muted);
  white-space: nowrap;
}

.aip-table tr.is-mine td {
  background: var(--pb-color-brand-soft);
}

.aip-empty {
  text-align: center;
  color: var(--pb-color-text-soft);
  padding: 1.5rem 0;
}

.aip-ip {
  font-weight: 600;
  color: var(--pb-color-heading);
}

.aip-desc {
  max-width: 320px;
  color: var(--pb-color-text-muted);
}

.aip-badge {
  display: inline-block;
  margin-left: 6px;
  padding: 1px 7px;
  font-size: 0.7rem;
  font-weight: 600;
  border-radius: var(--pb-radius-sm);
  white-space: nowrap;
}

.badge-mine {
  background: var(--pb-color-brand);
  color: var(--pb-color-surface);
}

.badge-system {
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text-muted);
}

.aip-toggle {
  padding: 3px 11px;
  font-size: 0.78rem;
  font-weight: 600;
  border-radius: var(--pb-radius-sm);
  cursor: pointer;
  border: 1px solid transparent;
}

.aip-toggle.on {
  background: var(--pb-color-success-soft);
  color: var(--pb-color-success);
  border-color: var(--pb-color-success);
}

.aip-toggle.off {
  background: var(--pb-color-surface-muted);
  color: var(--pb-color-text-soft);
  border-color: var(--pb-color-border);
}

.aip-toggle:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.aip-row-actions {
  display: flex;
  gap: 0.375rem;
}

/* ── 버튼 ── */
.btn {
  padding: 7px 14px;
  border: 1px solid var(--pb-color-brand);
  background: var(--pb-color-brand);
  color: var(--pb-color-surface);
  border-radius: var(--pb-radius-md);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
}

.btn:hover:not(:disabled) {
  background: var(--pb-color-brand-strong);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-sm {
  padding: 5px 11px;
  font-size: 0.82rem;
}

.btn-ghost {
  background: transparent;
  color: var(--pb-color-brand);
}

.btn-ghost:hover:not(:disabled) {
  background: var(--pb-color-brand-soft);
}

.btn-danger {
  background: transparent;
  border-color: var(--pb-color-danger);
  color: var(--pb-color-danger);
}

.btn-danger:hover:not(:disabled) {
  background: var(--pb-color-danger-soft);
}

/* ── 입력 ── */
.form-input {
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

/* ── 모달 ── */
.aip-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.aip-modal {
  background: var(--pb-color-surface);
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-md);
  box-shadow: var(--pb-shadow-md);
  width: 100%;
  max-width: 520px;
  max-height: 90vh;
  overflow-y: auto;
}

.aip-modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--pb-color-border);
  background: var(--pb-color-surface-muted);
}

.aip-modal-head h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--pb-color-heading);
}

.aip-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: 1px solid var(--pb-color-border);
  border-radius: var(--pb-radius-sm);
  background: var(--pb-color-surface);
  color: var(--pb-color-text-muted);
  cursor: pointer;
}

.aip-close:hover {
  background: var(--pb-color-border);
  color: var(--pb-color-text);
}

.aip-modal-body {
  display: flex;
  flex-direction: column;
  gap: 0.875rem;
  padding: 20px;
}

.aip-hint {
  margin: 0;
  font-size: 0.85rem;
  color: var(--pb-color-text-muted);
}

.aip-quick {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.aip-modal-foot {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  padding: 14px 20px;
  border-top: 1px solid var(--pb-color-border);
}

@media (max-width: 768px) {
  .aip-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .aip-actions {
    justify-content: flex-end;
  }

  .aip-desc {
    max-width: 180px;
  }
}
</style>
