<template>
  <Dialog :open="open" @update:open="(v) => emit('update:open', v)">
    <DialogContent class="max-w-xl">
      <DialogHeader>
        <DialogTitle>Add Recommendation</DialogTitle>
        <DialogDescription>Create an immunization recommendation for Patient/{{ patientId }}</DialogDescription>
      </DialogHeader>

      <div class="space-y-4">
        <!-- Catalog picker -->
        <div class="space-y-2">
          <Label>Vaccine (from catalog)</Label>
          <select
            v-model="selectedCatalog"
            class="border-input bg-input-background dark:bg-input/30 flex h-9 w-full rounded-md border px-3 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50"
          >
            <option value="">Select or enter manually below</option>
            <option v-for="o in catalogOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
          </select>
        </div>

        <div class="grid grid-cols-2 gap-3">
          <div class="space-y-2">
            <Label for="rec-code">Vaccine code</Label>
            <Input id="rec-code" v-model="vaccineCode" placeholder="e.g., 20" />
          </div>
          <div class="space-y-2">
            <Label for="rec-display">Vaccine display</Label>
            <Input id="rec-display" v-model="vaccineDisplay" placeholder="e.g., DTaP" />
          </div>
        </div>

        <div class="grid grid-cols-2 gap-3">
          <div class="space-y-2">
            <Label for="rec-due">Due date</Label>
            <input
              id="rec-due"
              type="date"
              v-model="dueDate"
              class="border-input bg-input-background dark:bg-input/30 flex h-9 w-full rounded-md border px-3 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50"
            />
          </div>
          <div class="space-y-2">
            <Label for="rec-dose">Dose number</Label>
            <Input id="rec-dose" v-model="doseNumber" type="number" min="1" placeholder="e.g., 1" />
          </div>
        </div>

        <div class="space-y-2">
          <Label for="rec-series">Series</Label>
          <Input id="rec-series" v-model="series" placeholder="e.g., primary, booster" />
        </div>

        <div class="space-y-2">
          <Label for="rec-notes">Notes</Label>
          <Input id="rec-notes" v-model="notes" placeholder="Optional notes" />
        </div>

        <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

        <div class="flex justify-end gap-2 pt-2">
          <Button variant="outline" @click="emit('update:open', false)" :disabled="loading">Cancel</Button>
          <Button @click="submit" :disabled="loading || !vaccineCode.trim()">{{ loading ? 'Saving…' : 'Save' }}</Button>
        </div>
      </div>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { backendFetch } from '@/api/backend'
import { vaccineCatalog } from '@/vaccines/vaccineCatalog'

import Dialog from '@/components/ui/Dialog.vue'
import DialogContent from '@/components/ui/DialogContent.vue'
import DialogHeader from '@/components/ui/DialogHeader.vue'
import DialogTitle from '@/components/ui/DialogTitle.vue'
import DialogDescription from '@/components/ui/DialogDescription.vue'
import Label from '@/components/ui/Label.vue'
import Input from '@/components/ui/Input.vue'
import Button from '@/components/ui/Button.vue'

const props = defineProps<{ patientId: string; open: boolean }>()
const emit = defineEmits<{
  (e: 'update:open', v: boolean): void
  (e: 'saved'): void
}>()

const selectedCatalog = ref('')
const vaccineCode = ref('')
const vaccineDisplay = ref('')
const dueDate = ref('')
const doseNumber = ref<string>('')
const series = ref('')
const notes = ref('')
const loading = ref(false)
const error = ref<string | null>(null)

const catalogOptions = computed(() => {
  const map = new Map<string, { value: string; label: string; code: string; display: string }>()
  for (const v of vaccineCatalog) {
    for (const c of v.codings ?? []) {
      if (map.has(c.code)) continue
      const label = c.display ? `${v.label} — ${c.display} (${c.code})` : `${v.label} (${c.code})`
      map.set(c.code, { value: c.code, label, code: c.code, display: c.display ?? v.label })
    }
  }
  return Array.from(map.values()).sort((a, b) => a.label.localeCompare(b.label))
})

watch(selectedCatalog, (v) => {
  if (!v) return
  const opt = catalogOptions.value.find((o) => o.value === v)
  if (!opt) return
  vaccineCode.value = opt.code
  vaccineDisplay.value = opt.display
})

watch(() => props.open, (v) => {
  if (v) {
    selectedCatalog.value = ''
    vaccineCode.value = ''
    vaccineDisplay.value = ''
    dueDate.value = ''
    doseNumber.value = ''
    series.value = ''
    notes.value = ''
    error.value = null
  }
})

async function submit() {
  error.value = null
  loading.value = true
  try {
    await backendFetch(`/api/practitioner/patients/${encodeURIComponent(props.patientId)}/create-recommendation`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        vaccineCode: vaccineCode.value.trim(),
        vaccineDisplay: vaccineDisplay.value.trim() || undefined,
        dueDate: dueDate.value || undefined,
        doseNumber: doseNumber.value ? parseInt(doseNumber.value) : undefined,
        series: series.value.trim() || undefined,
        notes: notes.value.trim() || undefined,
      }),
    })
    emit('saved')
    emit('update:open', false)
  } catch (e: any) {
    error.value = String(e?.message ?? e)
  } finally {
    loading.value = false
  }
}
</script>
