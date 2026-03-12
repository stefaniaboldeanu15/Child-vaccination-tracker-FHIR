<template>
  <Dialog :open="open" @update:open="(v) => emit('update:open', v)">
    <DialogContent class="max-w-xl">
      <DialogHeader>
        <DialogTitle>Report Adverse Event</DialogTitle>
        <DialogDescription>Record an adverse event for Patient/{{ patientId }}</DialogDescription>
      </DialogHeader>

      <div class="space-y-4">
        <div class="grid grid-cols-2 gap-3">
          <div class="space-y-2">
            <Label for="ae-display">Event name</Label>
            <Input id="ae-display" v-model="display" placeholder="e.g., Injection site reaction" />
          </div>
          <div class="space-y-2">
            <Label for="ae-code">Event code</Label>
            <Input id="ae-code" v-model="code" placeholder="Optional code" />
          </div>
        </div>

        <div class="grid grid-cols-2 gap-3">
          <div class="space-y-2">
            <Label for="ae-severity">Severity</Label>
            <select
              id="ae-severity"
              v-model="severity"
              class="border-input bg-input-background dark:bg-input/30 flex h-9 w-full rounded-md border px-3 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50"
            >
              <option value="">(unset)</option>
              <option value="mild">mild</option>
              <option value="moderate">moderate</option>
              <option value="severe">severe</option>
            </select>
          </div>
          <div class="space-y-2">
            <Label for="ae-outcome">Outcome</Label>
            <select
              id="ae-outcome"
              v-model="outcome"
              class="border-input bg-input-background dark:bg-input/30 flex h-9 w-full rounded-md border px-3 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50"
            >
              <option value="">(unset)</option>
              <option value="resolved">resolved</option>
              <option value="recovering">recovering</option>
              <option value="ongoing">ongoing</option>
              <option value="fatal">fatal</option>
              <option value="unknown">unknown</option>
            </select>
          </div>
        </div>

        <div class="space-y-2">
          <Label for="ae-category">Category</Label>
          <Input id="ae-category" v-model="category" placeholder="e.g., product-problem, wrong-technique" />
        </div>

        <div class="space-y-2">
          <Label for="ae-date">Event date &amp; time</Label>
          <input
            id="ae-date"
            type="datetime-local"
            v-model="date"
            class="border-input bg-input-background dark:bg-input/30 flex h-9 w-full rounded-md border px-3 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50"
          />
        </div>

        <div class="space-y-2">
          <Label for="ae-description">Description</Label>
          <textarea
            id="ae-description"
            v-model="description"
            rows="3"
            placeholder="Describe the adverse event in detail…"
            class="border-input bg-input-background dark:bg-input/30 flex w-full rounded-md border px-3 py-2 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50"
          />
        </div>

        <div class="grid grid-cols-2 gap-3">
          <div class="space-y-2">
            <Label for="ae-immid">Linked immunization ID</Label>
            <Input id="ae-immid" v-model="immunizationId" placeholder="Optional" />
          </div>
          <div class="space-y-2">
            <Label for="ae-encid">Linked encounter ID</Label>
            <Input id="ae-encid" v-model="encounterId" placeholder="Optional" />
          </div>
        </div>

        <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

        <div class="flex justify-end gap-2 pt-2">
          <Button variant="outline" @click="emit('update:open', false)" :disabled="loading">Cancel</Button>
          <Button @click="submit" :disabled="loading || !display.trim()">{{ loading ? 'Saving…' : 'Save' }}</Button>
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

const props = defineProps<{ patientId: string; open: boolean }>()
const emit = defineEmits<{
  (e: 'update:open', v: boolean): void
  (e: 'saved'): void
}>()

const display = ref('')
const code = ref('')
const severity = ref('')
const outcome = ref('')
const category = ref('')
const date = ref('')
const description = ref('')
const immunizationId = ref('')
const encounterId = ref('')
const loading = ref(false)
const error = ref<string | null>(null)

watch(() => props.open, (v) => {
  if (v) {
    display.value = ''
    code.value = ''
    severity.value = ''
    outcome.value = ''
    category.value = ''
    date.value = ''
    description.value = ''
    immunizationId.value = ''
    encounterId.value = ''
    error.value = null
  }
})

async function submit() {
  error.value = null
  loading.value = true
  try {
    await backendFetch(`/api/practitioner/patients/${encodeURIComponent(props.patientId)}/adverse-events`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        display: display.value.trim(),
        code: code.value.trim() || undefined,
        severity: severity.value || undefined,
        outcome: outcome.value || undefined,
        category: category.value.trim() || undefined,
        date: date.value ? new Date(date.value).toISOString() : undefined,
        description: description.value.trim() || undefined,
        immunizationId: immunizationId.value.trim() || undefined,
        encounterId: encounterId.value.trim() || undefined,
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
