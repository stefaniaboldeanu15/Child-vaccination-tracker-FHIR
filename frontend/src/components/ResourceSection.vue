<script setup lang="ts">
import { computed, ref } from 'vue'
import RecordFormModal from '@/components/RecordFormModal.vue'
import type { Language, LocalizedResourceConfig } from '@/config/resources'
import { titleCase } from '@/config/resources'

const props = defineProps<{
  config: LocalizedResourceConfig
  items: Record<string, any>[]
  loading: boolean
  error: string | null
  canCreate: boolean
  canEdit: boolean
  language?: Language
}>()

const emit = defineEmits<{
  create: [LocalizedResourceConfig, Record<string, any>]
  update: [LocalizedResourceConfig, string, Record<string, any>]
}>()

const i18n = {
  en: {
    add: 'Add',
    create: 'Create',
    update: 'Update',
    loading: 'Loading',
    noneFound: 'No',
    foundForChild: 'found for this child.',
    dtoFallback: 'FHIR-backed DTO',
  },
  de: {
    add: 'Hinzufügen',
    create: 'Erstellen',
    update: 'Aktualisieren',
    loading: 'Lade',
    noneFound: 'Keine',
    foundForChild: 'für dieses Kind gefunden.',
    dtoFallback: 'FHIR-basiertes DTO',
  },
} as const

const t = computed(() => i18n[props.language ?? 'en'])

const createOpen = ref(false)
const editOpen = ref(false)
const editingRecord = ref<Record<string, any> | null>(null)

const visibleKeys = computed(() => {
  const keys = new Set<string>()

  props.items.forEach((item) => {
    Object.entries(item).forEach(([key, value]) => {
      if (value !== null && value !== '') keys.add(key)
    })
  })

  return Array.from(keys)
})

function startEdit(item: Record<string, any>) {
  editingRecord.value = item
  editOpen.value = true
}

function renderValue(value: unknown) {
  if (value === null || value === undefined || value === '') return '—'
  if (Array.isArray(value)) return value.join(', ')
  if (typeof value === 'object') return JSON.stringify(value)
  return String(value)
}
</script>

<template>
  <section class="section-stack">
    <div class="section-header">
      <div>
        <div class="kicker">{{ config.label }}</div>
        <h2 style="margin: 4px 0 6px">{{ config.label }}</h2>
        <div class="muted">{{ config.description }}</div>
      </div>
      <va-button v-if="canCreate && config.canCreate && config.createFields?.length" @click="createOpen = true">
        {{ t.add }} {{ config.singularLabel ?? (config.label.slice(0, -1) || config.label) }}
      </va-button>
    </div>

    <va-alert v-if="error" color="danger" outline>{{ error }}</va-alert>
    <div v-else-if="loading" class="empty-state">{{ t.loading }} {{ config.label.toLowerCase() }}…</div>
    <div v-else-if="items.length === 0" class="empty-state">{{ t.noneFound }} {{ config.label.toLowerCase() }} {{ t.foundForChild }}</div>
    <div v-else class="section-stack">
      <article v-for="item in items" :key="item.id ?? JSON.stringify(item)" class="record-card">
        <div class="section-header" style="margin-bottom: 12px">
          <div>
            <h3 style="margin: 0">{{ item.id || config.label }}</h3>
            <div class="muted">{{ Object.values(item).find((value) => typeof value === 'string' && value && value !== item.id) || t.dtoFallback }}</div>
          </div>
          <va-button
            v-if="canEdit && config.canEdit && config.updateFields?.length && item.id"
            preset="secondary"
            size="small"
            @click="startEdit(item)"
          >
            {{ t.update }}
          </va-button>
        </div>

        <div class="record-grid">
          <div v-for="key in visibleKeys" :key="key" class="record-item">
            <strong>{{ titleCase(key) }}</strong>
            <span>{{ renderValue(item[key]) }}</span>
          </div>
        </div>
      </article>
    </div>

    <RecordFormModal
      v-if="config.createFields?.length"
      v-model="createOpen"
      :title="`${t.create} ${config.singularLabel ?? config.label}`"
      :fields="config.createFields"
      @save="emit('create', config, $event)"
    />

    <RecordFormModal
      v-if="config.updateFields?.length && editingRecord"
      v-model="editOpen"
      :title="`${t.update} ${config.singularLabel ?? config.label}`"
      :fields="config.updateFields"
      :initial-value="editingRecord"
      @save="emit('update', config, String(editingRecord?.id), $event)"
    />
  </section>
</template>
