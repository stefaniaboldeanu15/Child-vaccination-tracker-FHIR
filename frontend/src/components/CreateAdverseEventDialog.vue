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
            <Input
              id="ae-display"
              v-model="display"
              placeholder="e.g., Injection site reaction"
              :class="invalidFieldClass(showValidation && !!displayError)"
            />
            <p v-if="showValidation && displayError" class="text-xs text-red-600">{{ displayError }}</p>
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
            :class="[
              'border-input bg-input-background dark:bg-input/30 flex h-9 w-full rounded-md border px-3 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50',
              invalidFieldClass(showValidation && !!dateError),
            ]"
          />
          <p v-if="showValidation && dateError" class="text-xs text-red-600">{{ dateError }}</p>
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

        <div v-if="error" class="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {{ error }}
        </div>

        <div class="flex justify-end gap-2 pt-2">
          <Button variant="outline" @click="emit('update:open', false)" :disabled="loading">Cancel</Button>
          <Button @click="submit" :disabled="loading || !canSubmit">{{ loading ? 'Saving…' : 'Save' }}</Button>
        </div>
      </div>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { backendFetch } from '@/api/backend'

import Dialog from '@/components/ui/Dialog.vue'
import DialogContent from '@/components/ui/DialogContent.vue'
import DialogHeader from '@/components/ui/DialogHeader.vue'
import DialogTitle from '@/components/ui/DialogTitle.vue'
import DialogDescription from '@/components/ui/DialogDescription.vue'
import Label from '@/components/ui/Label.vue'
import Input from '@/components/ui/Input.vue'
import Button from '@/components/ui/Button.vue'
import { invalidFieldClass, isBlank, isFutureDateTime, isValidDateTimeLocal, toFriendlyFormError } from '@/utils/formFeedback'

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
const submitAttempted = ref(false)

const displayError = computed(() => {
  if (isBlank(display.value)) return 'Enter a short name for the adverse event.'
  return ''
})

const dateError = computed(() => {
  if (!date.value.trim()) return ''
  if (!isValidDateTimeLocal(date.value)) return 'Enter a valid event date and time.'
  if (isFutureDateTime(date.value)) return 'Adverse events cannot be recorded in the future.'
  return ''
})

const canSubmit = computed(() => !displayError.value && !dateError.value)
const showValidation = computed(() => submitAttempted.value)

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
    submitAttempted.value = false
  }
})

async function submit() {
  submitAttempted.value = true
  error.value = null

  if (!canSubmit.value) {
    error.value = 'Please review the adverse event details before saving.'
    return
  }

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
    error.value = toFriendlyFormError(e, 'Saving the adverse event')
  } finally {
    loading.value = false
  }
}
</script>
