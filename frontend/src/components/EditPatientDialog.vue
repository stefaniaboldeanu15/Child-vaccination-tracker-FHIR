<template>
  <Dialog :open="open" @update:open="(v) => emit('update:open', v)">
    <DialogContent v-if="open" class="max-w-xl">
      <DialogHeader>
        <DialogTitle>Edit Patient</DialogTitle>
        <DialogDescription>Update details for FHIR Patient/{{ patientId }}</DialogDescription>
      </DialogHeader>

      <div class="space-y-5">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div class="space-y-2">
            <Label for="ep-given">Given name</Label>
            <Input id="ep-given" v-model="givenName" placeholder="e.g., John" />
          </div>
          <div class="space-y-2">
            <Label for="ep-family">Family name</Label>
            <Input id="ep-family" v-model="familyName" placeholder="e.g., Doe" />
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div class="space-y-2">
            <Label for="ep-birth">Birth date</Label>
            <input
              id="ep-birth"
              type="date"
              v-model="birthDate"
              :class="[
                'border-input bg-input-background dark:bg-input/30 flex h-9 w-full rounded-md border px-3 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50',
                invalidFieldClass(showValidation && !!birthDateError),
              ]"
            />
            <p v-if="showValidation && birthDateError" class="text-xs text-red-600">{{ birthDateError }}</p>
          </div>
          <div class="space-y-2">
            <Label for="ep-gender">Gender</Label>
            <select
              id="ep-gender"
              v-model="gender"
              class="border-input bg-input-background dark:bg-input/30 flex h-9 w-full rounded-md border px-3 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50"
            >
              <option value="">(unset)</option>
              <option value="male">male</option>
              <option value="female">female</option>
              <option value="other">other</option>
              <option value="unknown">unknown</option>
            </select>
          </div>
        </div>

        <div v-if="error" class="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {{ error }}
        </div>

        <div class="flex items-center justify-end gap-2 pt-1">
          <Button variant="outline" @click="emit('update:open', false)" :disabled="loading">Cancel</Button>
          <Button @click="save" :disabled="loading || !canSubmit">{{ loading ? 'Saving…' : 'Save' }}</Button>
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
import { invalidFieldClass, isFutureDate, isValidDateInput, toFriendlyFormError } from '@/utils/formFeedback'

const props = defineProps<{
  open: boolean
  patientId: string
  initialFirstName?: string
  initialLastName?: string
  initialBirthDate?: string
  initialGender?: string
}>()

const emit = defineEmits<{
  (e: 'update:open', v: boolean): void
  (e: 'saved'): void
}>()

const givenName = ref('')
const familyName = ref('')
const birthDate = ref('')
const gender = ref('')
const loading = ref(false)
const error = ref<string | null>(null)
const submitAttempted = ref(false)

function normalizeDateForDateInput(value?: string) {
  if (!value) return ''

  const raw = String(value).trim()
  if (!raw) return ''

  const isoMatch = raw.match(/^(\d{4})-(\d{2})-(\d{2})/)
  if (isoMatch) {
    const [, year, month, day] = isoMatch
    return `${year}-${month}-${day}`
  }

  const dotMatch = raw.match(/^(\d{2})\.(\d{2})\.(\d{4})$/)
  if (dotMatch) {
    const [, day, month, year] = dotMatch
    return `${year}-${month}-${day}`
  }

  const javaDateMatch = raw.match(/^[A-Za-z]{3} ([A-Za-z]{3}) (\d{1,2}) .* (\d{4})$/)
  if (javaDateMatch) {
    const [, monthName, day, year] = javaDateMatch
    const months: Record<string, string> = {
      Jan: '01', Feb: '02', Mar: '03', Apr: '04',
      May: '05', Jun: '06', Jul: '07', Aug: '08',
      Sep: '09', Oct: '10', Nov: '11', Dec: '12',
    }
    const month = months[monthName]
    if (month) return `${year}-${month}-${String(day).padStart(2, '0')}`
  }

  const parsed = new Date(raw)
  if (!Number.isNaN(parsed.getTime())) {
    const year = parsed.getFullYear()
    const month = String(parsed.getMonth() + 1).padStart(2, '0')
    const day = String(parsed.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  return raw
}

function syncFormFromProps() {
  error.value = null
  givenName.value = props.initialFirstName ?? ''
  familyName.value = props.initialLastName ?? ''
  birthDate.value = normalizeDateForDateInput(props.initialBirthDate)
  gender.value = props.initialGender ?? ''
  submitAttempted.value = false
}

const birthDateError = computed(() => {
  if (!birthDate.value.trim()) return ''
  if (!isValidDateInput(birthDate.value)) return 'Enter a valid birth date.'
  if (isFutureDate(birthDate.value)) return 'Birth dates cannot be in the future.'
  return ''
})

const canSubmit = computed(() => !!(givenName.value.trim() || familyName.value.trim()) && !birthDateError.value)
const showValidation = computed(() => submitAttempted.value)

watch(
  () => [
    props.open,
    props.patientId,
    props.initialFirstName,
    props.initialLastName,
    props.initialBirthDate,
    props.initialGender,
  ],
  ([open]) => {
    if (!open) return
    syncFormFromProps()
  },
  { immediate: true },
)

async function save() {
  submitAttempted.value = true
  error.value = null

  if (!canSubmit.value) {
    error.value = birthDateError.value || 'Enter at least a given name or family name.'
    return
  }

  loading.value = true
  try {
    await backendFetch(`/api/practitioner/dashboard/patients/${encodeURIComponent(props.patientId)}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        patientId: props.patientId,
        firstName: givenName.value.trim() || undefined,
        lastName: familyName.value.trim() || undefined,
        birthDate: birthDate.value || undefined,
        gender: gender.value || undefined,
      }),
    })
    emit('saved')
    emit('update:open', false)
  } catch (e: any) {
    error.value = toFriendlyFormError(e, 'Saving the patient')
  } finally {
    loading.value = false
  }
}
</script>
