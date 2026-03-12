<template>
  <Dialog :open="open" @update:open="(v) => emit('update:open', v)">
    <DialogContent class="max-w-2xl max-h-[85vh] overflow-y-auto">
      <DialogHeader>
        <DialogTitle>Create Encounter</DialogTitle>
        <DialogDescription>Record a full encounter for Patient/{{ patientId }}</DialogDescription>
      </DialogHeader>

      <div class="space-y-5">
        <!-- Encounter details -->
        <div class="grid grid-cols-2 gap-3">
          <div class="space-y-2">
            <Label for="enc-date">Encounter date</Label>
            <input
              id="enc-date"
              type="date"
              v-model="encounterDate"
              class="border-input bg-input-background dark:bg-input/30 flex h-9 w-full rounded-md border px-3 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50"
            />
          </div>
          <div class="space-y-2">
            <Label for="enc-org">Organization ID</Label>
            <Input id="enc-org" v-model="organizationId" placeholder="Optional FHIR org ID" />
          </div>
        </div>

        <div class="space-y-2">
          <Label for="enc-loc">Location ID</Label>
          <Input id="enc-loc" v-model="locationId" placeholder="Optional FHIR location ID" />
        </div>

        <!-- Immunizations -->
        <div class="space-y-3">
          <div class="flex items-center justify-between">
            <div class="text-gray-900">Immunizations</div>
            <Button variant="outline" size="sm" @click="addImmunization">+ Add</Button>
          </div>

          <div v-if="immunizations.length === 0" class="text-sm text-gray-500">No immunizations added yet.</div>

          <div
            v-for="(imm, idx) in immunizations"
            :key="idx"
            class="rounded-lg border p-3 space-y-2 bg-gray-50"
          >
            <div class="flex items-center justify-between">
              <div class="text-sm text-gray-700">Immunization {{ idx + 1 }}</div>
              <button class="text-red-500 text-xs hover:underline" @click="removeImmunization(idx)">Remove</button>
            </div>
            <div class="grid grid-cols-2 gap-2">
              <div class="space-y-1">
                <Label>Vaccine code</Label>
                <Input v-model="imm.vaccineCode" placeholder="e.g., 20" />
              </div>
              <div class="space-y-1">
                <Label>Vaccine display</Label>
                <Input v-model="imm.vaccineDisplay" placeholder="e.g., DTaP" />
              </div>
              <div class="space-y-1 col-span-2">
                <Label>Occurrence date &amp; time</Label>
                <input
                  type="datetime-local"
                  v-model="imm.occurrenceDateTime"
                  class="border-input bg-input-background dark:bg-input/30 flex h-9 w-full rounded-md border px-3 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50"
                />
              </div>
            </div>
          </div>
        </div>

        <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

        <div class="flex justify-end gap-2 pt-2">
          <Button variant="outline" @click="emit('update:open', false)" :disabled="loading">Cancel</Button>
          <Button @click="submit" :disabled="loading || !encounterDate.trim()">{{ loading ? 'Saving…' : 'Save' }}</Button>
        </div>
      </div>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { backendFetch } from '@/api/backend'

import Dialog from '@/components/ui/Dialog.vue'
import DialogContent from '@/components/ui/DialogContent.vue'
import DialogHeader from '@/components/ui/DialogHeader.vue'
import DialogTitle from '@/components/ui/DialogTitle.vue'
import DialogDescription from '@/components/ui/DialogDescription.vue'
import Label from '@/components/ui/Label.vue'
import Input from '@/components/ui/Input.vue'
import Button from '@/components/ui/Button.vue'

type ImmunizationInput = {
  vaccineCode: string
  vaccineDisplay: string
  occurrenceDateTime: string
}

const props = defineProps<{ patientId: string; open: boolean }>()
const emit = defineEmits<{
  (e: 'update:open', v: boolean): void
  (e: 'saved'): void
}>()

const encounterDate = ref(new Date().toISOString().slice(0, 10))
const organizationId = ref('')
const locationId = ref('')
const immunizations = ref<ImmunizationInput[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

watch(() => props.open, (v) => {
  if (v) {
    encounterDate.value = new Date().toISOString().slice(0, 10)
    organizationId.value = ''
    locationId.value = ''
    immunizations.value = []
    error.value = null
  }
})

function addImmunization() {
  immunizations.value.push({ vaccineCode: '', vaccineDisplay: '', occurrenceDateTime: '' })
}

function removeImmunization(idx: number) {
  immunizations.value.splice(idx, 1)
}

async function submit() {
  error.value = null
  loading.value = true
  try {
    await backendFetch(`/api/practitioner/${encodeURIComponent(props.patientId)}/create-full-encounters`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        encounterDate: encounterDate.value,
        organizationId: organizationId.value.trim() || undefined,
        locationId: locationId.value.trim() || undefined,
        immunizations: immunizations.value
          .filter((i) => i.vaccineCode.trim())
          .map((i) => ({
            vaccineCode: i.vaccineCode.trim(),
            vaccineDisplay: i.vaccineDisplay.trim() || undefined,
            occurrenceDateTime: i.occurrenceDateTime
              ? new Date(i.occurrenceDateTime).toISOString()
              : undefined,
          })),
        observations: [],
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
