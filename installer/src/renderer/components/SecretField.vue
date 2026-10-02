<script setup>
import { ref, computed } from 'vue'
import { setSecret } from '../store'

/**
 * 시크릿 입력 필드.
 *
 * 동작 원칙
 *  - 저장된 값은 렌더러로 내려오지 않는다. 표시되는 건 마스킹 문자열뿐.
 *  - 사용자가 새로 입력한 값(draft)만 잠깐 컴포넌트 안에 있고,
 *    [저장] 또는 blur 시점에 setSecret 으로 메인에 올린 뒤 즉시 비운다.
 */

const props = defineProps({
  path: { type: String, required: true }, // 예: 'apiKeys.nlApiKey'
  label: { type: String, default: '' },
  secret: { type: Object, default: () => ({ set: false, masked: '' }) },
  placeholder: { type: String, default: '' },
  hint: { type: String, default: '' },
  required: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false }
})

const emit = defineEmits(['saved'])

const draft = ref('')
const editing = ref(false)
const saving = ref(false)

const hasValue = computed(() => !!(props.secret && props.secret.set))

function startEdit() {
  editing.value = true
  draft.value = ''
}

async function commit() {
  if (!editing.value) return
  const v = draft.value
  if (v === '') {
    editing.value = false
    return
  }
  saving.value = true
  try {
    await setSecret(props.path, v)
    draft.value = '' // 평문을 렌더러 메모리에 남기지 않는다
    editing.value = false
    emit('saved')
  } finally {
    saving.value = false
  }
}

async function clearValue() {
  await setSecret(props.path, '')
  draft.value = ''
  editing.value = false
  emit('saved')
}
</script>

<template>
  <div class="field">
    <label v-if="label" class="field-label">
      {{ label }}<span v-if="required" class="req">*</span>
    </label>

    <div v-if="!editing" class="input-row">
      <input
        type="text"
        class="mono"
        readonly
        :disabled="disabled"
        :value="hasValue ? secret.masked : ''"
        :placeholder="hasValue ? '' : placeholder || '입력되지 않음'"
      />
      <button class="small" :disabled="disabled" @click="startEdit">
        {{ hasValue ? '변경' : '입력' }}
      </button>
      <button v-if="hasValue" class="small danger" :disabled="disabled" @click="clearValue">지움</button>
    </div>

    <div v-else class="input-row">
      <input
        v-model="draft"
        type="password"
        class="mono"
        autocomplete="off"
        spellcheck="false"
        :placeholder="placeholder"
        :disabled="saving"
        @keyup.enter="commit"
      />
      <button class="small primary" :disabled="saving || !draft" @click="commit">저장</button>
      <button class="small ghost" :disabled="saving" @click="editing = false">취소</button>
    </div>

    <div v-if="hint" class="field-hint">{{ hint }}</div>
    <div v-if="editing" class="field-hint">
      입력한 값은 저장 즉시 마법사 내부(메인 프로세스)로 옮겨지고 화면에서는 지워집니다.
    </div>
  </div>
</template>
