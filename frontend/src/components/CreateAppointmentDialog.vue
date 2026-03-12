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
              class="border-input bg-input-background dark:bg-input/30 flex h-9 w-full rounded-md border px-3 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50"
            />
          </div>
          <div class="space-y-2">
            <Label for="appt-end">End date &amp; time</Label>
            <input
              id="appt-end"
              type="datetime-local"
              v-model="end"
              class="border-input bg-input-background dark:bg-input/30 flex h-9 w-full rounded-md border px-3 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50"
            />
          </div>
        </div>

        <div class="space-y-2">
          <Label for="appt-location">Location</Label>
          <Input id="appt-location" v-model="location" placeholder="e.g., Clinic Room 1" />
        </div>

        <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

        <div class="flex justify-end gap-2 pt-2">
          <Button variant="outline" @click="emit('update:open', false)" :disabled="loading">Cancel</Button>
          <Button @click="submit" :disabled="loading || !start.trim()">{{ loading ? 'Saving…' : 'Save' }}</Button>
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

const reason = ref('')
const start = ref('')
const end = ref('')
const location = ref('')
const loading = ref(false)
const error = ref<string | null>(null)

watch(() => props.open, (v) => {
  if (v) {
    reason.value = ''
    start.value = ''
    end.value = ''
    location.value = ''
    error.value = null
  }
})

async function submit() {
  error.value = null
  loading.value = true
  try {
    await backendFetch(`/api/practitioner/patients/${encodeURIComponent(props.patientId)}/create-appointments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        patientId: props.patientId,
        start: start.value ? new Date(start.value).toISOString() : undefined,
        end: end.value ? new Date(end.value).toISOString() : undefined,
        location: location.value.trim() || undefined,
        reason: reason.value.trim() || undefined,
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
