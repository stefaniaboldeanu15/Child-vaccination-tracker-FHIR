<template>
  <Dialog :open="open" @update:open="(v) => emit('update:open', v)">
    <DialogContent class="max-w-xl">
      <DialogHeader>
        <DialogTitle>Create Appointment</DialogTitle>
        <DialogDescription>Schedule an appointment for Patient/{{ patientId }}</DialogDescription>
      </DialogHeader>

      <div class="space-y-4">
        <div class="space-y-2">
          <Label for="appt-reason">Reason</Label>
          <Input id="appt-reason" v-model="reason" placeholder="e.g., Vaccination follow-up" />
        </div>

        <div class="grid grid-cols-2 gap-3">
          <div class="space-y-2">
            <Label for="appt-start">Start date &amp; time</Label>
            <input
              id="appt-start"
              type="datetime-local"
              v-model="start"
              :class="[
                'border-input bg-input-background dark:bg-input/30 flex h-9 w-full rounded-md border px-3 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50',
                invalidFieldClass(showValidation && !!startError),
              ]"
            />
            <p v-if="showValidation && startError" class="text-xs text-red-600">{{ startError }}</p>
          </div>
          <div class="space-y-2">
            <Label for="appt-end">End date &amp; time</Label>
            <input
              id="appt-end"
              type="datetime-local"
              v-model="end"
              :class="[
                'border-input bg-input-background dark:bg-input/30 flex h-9 w-full rounded-md border px-3 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50',
                invalidFieldClass(showValidation && !!endError),
              ]"
            />
            <p v-if="showValidation && endError" class="text-xs text-red-600">{{ endError }}</p>
          </div>
        </div>

        <div class="space-y-2">
          <Label for="appt-location">Location</Label>
          <Input id="appt-location" v-model="location" placeholder="e.g., Clinic Room 1" />
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
import { invalidFieldClass, isBlank, isValidDateTimeLocal, toFriendlyFormError } from '@/utils/formFeedback'

const props = defineProps<{ patientId: string; open: boolean }>()
const emit = defineEmits<{
  (e: 'update:open', v: boolean): void
  (e: 'saved'): void
}>()

const reason = ref('')
const start = ref('')
const end = ref('')
const location = ref('')
const loading = ref(false)
const error = ref<string | null>(null)
const submitAttempted = ref(false)

const startError = computed(() => {
  if (isBlank(start.value)) return 'Choose when the appointment starts.'
  if (!isValidDateTimeLocal(start.value)) return 'Enter a valid start date and time.'
  return ''
})

const endError = computed(() => {
  if (isBlank(end.value)) return 'Choose when the appointment ends.'
  if (!isValidDateTimeLocal(end.value)) return 'Enter a valid end date and time.'
  if (!startError.value && new Date(end.value).getTime() <= new Date(start.value).getTime()) {
    return 'End time must be after the start time.'
  }
  return ''
})

const canSubmit = computed(() => !startError.value && !endError.value)
const showValidation = computed(() => submitAttempted.value)

watch(() => props.open, (v) => {
  if (v) {
    reason.value = ''
    start.value = ''
    end.value = ''
    location.value = ''
    error.value = null
    submitAttempted.value = false
  }
})

async function submit() {
  submitAttempted.value = true
  error.value = null

  if (!canSubmit.value) {
    error.value = 'Please fix the appointment time before saving.'
    return
  }

  loading.value = true
  try {
    await backendFetch(`/api/practitioner/patients/${encodeURIComponent(props.patientId)}/create-appointments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        patientId: props.patientId,
        start: new Date(start.value).toISOString(),
        end: new Date(end.value).toISOString(),
        location: location.value.trim() || undefined,
        reason: reason.value.trim() || undefined,
      }),
    })
    emit('saved')
    emit('update:open', false)
  } catch (e: any) {
    error.value = toFriendlyFormError(e, 'Creating the appointment')
  } finally {
    loading.value = false
  }
}
</script>
