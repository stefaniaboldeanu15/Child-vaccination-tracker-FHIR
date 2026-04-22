<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { LocalizedFieldConfig } from '@/config/resources'

const props = defineProps<{
  modelValue: boolean
  title: string
  fields: LocalizedFieldConfig[]
  onSave: (payload: Record<string, any>) => Promise<void> | void
  initialValue?: Record<string, any>
  kickerLabel?: string
  cancelLabel?: string
  saveLabel?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [boolean]
}>()

const draft = reactive<Record<string, any>>({})
const submitAttempted = ref(false)
const isSaving = ref(false)
const submitError = ref<string | null>(null)

function applyInitialValue() {
  props.fields.forEach((field) => {
    const nextValue = props.initialValue?.[field.key]
    draft[field.key] =
      nextValue === undefined || nextValue === null ? '' : formatValueForField(nextValue, field.type)
  })
}

watch(
  () => [props.modelValue, props.initialValue],
  () => {
    if (props.modelValue) {
      applyInitialValue()
      submitAttempted.value = false
      submitError.value = null
      isSaving.value = false
    }
  },
  { immediate: true, deep: true },
)

const isOpen = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value),
})

function formatValueForField(value: unknown, type: LocalizedFieldConfig['type']) {
  if (value == null) return ''
  if (type === 'datetime') return String(value).slice(0, 16)
  return value
}

function normalizeFieldValue(value: unknown, field: LocalizedFieldConfig) {
  if (value === '' || value === undefined || value === null) return null
  if (field.type === 'number') return Number(value)
  return value
}

function isValidDateValue(value: string) {
  return /^\d{4}-\d{2}-\d{2}$/.test(value) && !Number.isNaN(Date.parse(`${value}T00:00:00`))
}

function isValidDateTimeValue(value: string) {
  return /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(value) && !Number.isNaN(Date.parse(value))
}

function getFieldError(field: LocalizedFieldConfig) {
  const rawValue = draft[field.key]
  const normalizedValue = normalizeFieldValue(rawValue, field)

  if (field.required && normalizedValue === null) {
    return field.validation?.requiredMessage ?? 'This field is required.'
  }

  if (normalizedValue === null) {
    return ''
  }

  if (field.type === 'number' && !Number.isFinite(normalizedValue)) {
    return field.validation?.invalidMessage ?? 'Enter a valid number.'
  }

  if (field.type === 'date' && typeof normalizedValue === 'string' && !isValidDateValue(normalizedValue)) {
    return field.validation?.invalidMessage ?? 'Enter a valid date.'
  }

  if (field.type === 'datetime' && typeof normalizedValue === 'string' && !isValidDateTimeValue(normalizedValue)) {
    return field.validation?.invalidMessage ?? 'Enter a valid date and time.'
  }

  if (field.type === 'select' && field.options?.length) {
    const allowedValues = new Set(field.options.map((option) => option.value))
    if (typeof normalizedValue !== 'string' || !allowedValues.has(normalizedValue)) {
      return field.validation?.invalidMessage ?? 'Choose a valid option.'
    }
  }

  return ''
}

const fieldErrors = computed<Record<string, string>>(() =>
  Object.fromEntries(props.fields.map((field) => [field.key, getFieldError(field)])),
)

const hasErrors = computed(() => Object.values(fieldErrors.value).some(Boolean))

async function submit() {
  submitAttempted.value = true
  if (hasErrors.value) return

  const payload = Object.fromEntries(
    props.fields
      .map((field) => [field.key, normalizeFieldValue(draft[field.key], field)])
      .filter(([, value]) => value !== null),
  )

  isSaving.value = true
  submitError.value = null

  try {
    await props.onSave(payload)
    isOpen.value = false
  } catch (error) {
    submitError.value = error instanceof Error ? error.message : String(error)
  } finally {
    isSaving.value = false
  }
}
</script>

<template>
  <va-modal v-model="isOpen" hide-default-actions>
    <div class="modal-shell">
      <div class="section-header">
        <svg class="hero-mark" viewBox="0 0 64 64" aria-hidden="true">
          <defs>
            <linearGradient id="modalMarkGreen" x1="12" y1="10" x2="52" y2="54" gradientUnits="userSpaceOnUse">
              <stop offset="0" stop-color="#22c55e" />
              <stop offset="1" stop-color="#16a34a" />
            </linearGradient>
          </defs>
          <path class="hero-mark-shield" d="M32 8c-10 0-18 8.2-18 18.3C14 40.2 32 56 32 56s18-15.8 18-29.7C50 16.2 42 8 32 8z" />
          <path class="hero-mark-plus" d="M25 31h14" />
          <path class="hero-mark-plus" d="M32 24v14" />
        </svg>
        <div class="header-copy">
          <div class="kicker">{{ props.kickerLabel ?? 'Edit payload' }}</div>
          <h3>{{ title }}</h3>
        </div>
      </div>

      <va-alert v-if="submitError" color="danger" outline class="modal-error">
        {{ submitError }}
      </va-alert>
      <va-alert v-else-if="isSaving" color="info" outline class="modal-error">
        Saving and refreshing data…
      </va-alert>

      <div class="form-grid">
        <div v-for="field in fields" :key="field.key" :class="{ full: field.full }">
          <va-input
            v-if="field.type === 'text' || field.type === 'date' || field.type === 'datetime' || field.type === 'number'"
            v-model="draft[field.key]"
            :label="field.label"
            :type="field.type === 'datetime' ? 'datetime-local' : field.type"
            :disabled="isSaving"
            :error="submitAttempted && Boolean(fieldErrors[field.key])"
            :error-messages="submitAttempted && fieldErrors[field.key] ? [fieldErrors[field.key]] : []"
          />
          <va-select
            v-else-if="field.type === 'select'"
            v-model="draft[field.key]"
            :label="field.label"
            :options="field.options ?? []"
            text-by="label"
            value-by="value"
            :disabled="isSaving"
            :error="submitAttempted && Boolean(fieldErrors[field.key])"
            :error-messages="submitAttempted && fieldErrors[field.key] ? [fieldErrors[field.key]] : []"
          />
          <va-textarea
            v-else
            v-model="draft[field.key]"
            :label="field.label"
            :min-rows="field.full ? 4 : 2"
            :disabled="isSaving"
            :error="submitAttempted && Boolean(fieldErrors[field.key])"
            :error-messages="submitAttempted && fieldErrors[field.key] ? [fieldErrors[field.key]] : []"
          />
        </div>
      </div>

      <div class="toolbar modal-actions">
        <va-button preset="secondary" :disabled="isSaving" @click="isOpen = false">{{ props.cancelLabel ?? 'Cancel' }}</va-button>
        <va-button :loading="isSaving" :disabled="isSaving" @click="submit">{{ props.saveLabel ?? 'Save' }}</va-button>
      </div>
    </div>
  </va-modal>
</template>

<style scoped>
.modal-shell {
  width: 100%;
  max-width: none;
  margin: 0 auto;
  padding: 0;
  box-sizing: border-box;
  overflow-x: hidden;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  margin-bottom: 10px;
}

.header-copy h3 {
  margin: 6px 0 0;
  color: #0b2d72;
  font-size: 1.65rem;
  line-height: 1.12;
}

.kicker {
  color: #1d4ed8;
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-mark {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  filter: drop-shadow(0 6px 12px rgba(22, 163, 74, 0.24));
}

.hero-mark-shield {
  fill: url(#modalMarkGreen);
  stroke: #15803d;
  stroke-width: 1.8;
}

.hero-mark-plus {
  fill: none;
  stroke: #ffffff;
  stroke-width: 3.2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 14px;
}

.form-grid > div {
  min-width: 0;
}

.form-grid .full {
  grid-column: 1 / -1;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
  gap: 10px;
}

.modal-error {
  margin-bottom: 12px;
}

:deep(.va-modal__dialog) {
  width: min(760px, calc(100vw - 64px));
  max-width: calc(100vw - 64px);
  padding: 0 !important;
  margin: 0 auto;
  background: transparent !important;
  box-shadow: none !important;
}

:deep(.va-modal__inner) {
  width: 100%;
  max-width: 100%;
  overflow-x: hidden;
  border-radius: 20px;
  border: 1px solid rgba(11, 45, 114, 0.14);
  box-shadow: 0 18px 42px rgba(11, 45, 114, 0.14);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.99) 0%, rgba(239, 247, 255, 0.92) 100%);
}

:deep(.va-modal__content) {
  padding: 16px;
  box-sizing: border-box;
  overflow-x: hidden;
  overflow-y: hidden;
}

:deep(.va-input-wrapper),
:deep(.va-input__container),
:deep(.va-select-content),
:deep(.va-textarea-wrapper) {
  border-radius: 12px;
}

:deep(.va-input-label),
:deep(.va-select-label),
:deep(.va-textarea-label) {
  color: #1d4ed8;
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

:deep(.va-button) {
  min-height: 40px;
  border-radius: 12px;
  font-weight: 700;
}

:deep(.va-button--secondary) {
  background: rgba(9, 146, 194, 0.08) !important;
  border-color: rgba(9, 146, 194, 0.22) !important;
  color: #0b2d72 !important;
}

@media (max-width: 840px) {
  .modal-shell {
    max-width: 100%;
  }

  :deep(.va-modal__dialog) {
    width: calc(100vw - 24px);
    max-width: calc(100vw - 24px);
  }

  :deep(.va-modal__inner) {
    width: 100%;
    max-width: 100%;
  }

  .form-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}
</style>
