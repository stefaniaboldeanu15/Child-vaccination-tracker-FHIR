<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import type { FieldConfig } from '@/config/resources'

const props = defineProps<{
  modelValue: boolean
  title: string
  fields: FieldConfig[]
  initialValue?: Record<string, any>
}>()

const emit = defineEmits<{
  'update:modelValue': [boolean]
  save: [Record<string, any>]
}>()

const draft = reactive<Record<string, any>>({})

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
    if (props.modelValue) applyInitialValue()
  },
  { immediate: true, deep: true },
)

const isOpen = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value),
})

function formatValueForField(value: unknown, type: FieldConfig['type']) {
  if (value == null) return ''
  if (type === 'datetime') return String(value).slice(0, 16)
  return value
}

function normalizeFieldValue(value: unknown, field: FieldConfig) {
  if (value === '' || value === undefined || value === null) return null
  if (field.type === 'number') return Number(value)
  return value
}

function submit() {
  const payload = Object.fromEntries(
    props.fields
      .map((field) => [field.key, normalizeFieldValue(draft[field.key], field)])
      .filter(([, value]) => value !== null),
  )

  emit('save', payload)
  isOpen.value = false
}
</script>

<template>
  <va-modal v-model="isOpen" hide-default-actions>
    <div style="min-width: min(720px, 92vw)">
      <div class="section-header">
        <div>
          <div class="kicker">Edit payload</div>
          <h3 style="margin: 6px 0 0">{{ title }}</h3>
        </div>
      </div>

      <div class="form-grid">
        <div v-for="field in fields" :key="field.key" :class="{ full: field.full }">
          <va-input
            v-if="field.type === 'text' || field.type === 'date' || field.type === 'datetime' || field.type === 'number'"
            v-model="draft[field.key]"
            :label="field.label"
            :type="field.type === 'datetime' ? 'datetime-local' : field.type"
          />
          <va-select
            v-else-if="field.type === 'select'"
            v-model="draft[field.key]"
            :label="field.label"
            :options="field.options ?? []"
            text-by="label"
            value-by="value"
          />
          <va-textarea
            v-else
            v-model="draft[field.key]"
            :label="field.label"
            :min-rows="field.full ? 4 : 2"
          />
        </div>
      </div>

      <div class="toolbar" style="justify-content: flex-end; margin-top: 16px">
        <va-button preset="secondary" @click="isOpen = false">Cancel</va-button>
        <va-button @click="submit">Save</va-button>
      </div>
    </div>
  </va-modal>
</template>
