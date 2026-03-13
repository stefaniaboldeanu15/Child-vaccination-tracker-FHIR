<template>
  <Dialog :open="open" @update:open="(v) => emit('update:open', v)">
    <DialogContent v-if="open" class="max-w-xl">
      <DialogHeader>
        <DialogTitle>Create new patient</DialogTitle>
        <DialogDescription>Creates a Patient resource in the connected FHIR server.</DialogDescription>
      </DialogHeader>

      <div class="space-y-5">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div class="space-y-2">
            <Label for="given">Given name</Label>
            <Input id="given" v-model="givenName" placeholder="e.g., John" />
          </div>
          <div class="space-y-2">
            <Label for="family">Family name</Label>
            <Input id="family" v-model="familyName" placeholder="e.g., Doe" />
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div class="space-y-2">
            <Label for="birth">Birth date</Label>
            <input
              id="birth"
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
            <Label for="gender">Gender</Label>
            <select
              id="gender"
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
          <Button @click="create" :disabled="loading || !canSubmit">{{ loading ? 'Creating…' : 'Create' }}</Button>
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

const props = defineProps<{ open: boolean }>()
const emit = defineEmits<{
  (e: 'update:open', v: boolean): void
  (e: 'created', patientId: string): void
}>()

const givenName = ref('')
const familyName = ref('')
const birthDate = ref('')
const gender = ref('')

const loading = ref(false)
const error = ref<string | null>(null)
const submitAttempted = ref(false)

const birthDateError = computed(() => {
  if (!birthDate.value.trim()) return ''
  if (!isValidDateInput(birthDate.value)) return 'Enter a valid birth date.'
  if (isFutureDate(birthDate.value)) return 'Birth dates cannot be in the future.'
  return ''
})

const canSubmit = computed(() => !!(givenName.value.trim() || familyName.value.trim()) && !birthDateError.value)
const showValidation = computed(() => submitAttempted.value)

watch(() => props.open, (v) => {
  if (!v) return
  error.value = null
  givenName.value = ''
  familyName.value = ''
  birthDate.value = ''
  gender.value = ''
  submitAttempted.value = false
})

async function create() {
  submitAttempted.value = true
  error.value = null

  if (!canSubmit.value) {
    error.value = birthDateError.value || 'Enter at least a given name or family name.'
    return
  }

  loading.value = true
  try {
    const res = await backendFetch('/api/practitioner/dashboard/patients', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        firstName: givenName.value.trim() || undefined,
        lastName: familyName.value.trim() || undefined,
        birthDate: birthDate.value || undefined,
        gender: gender.value || undefined,
      }),
    })

    const patientId = (await res.text()).trim()
    if (!patientId) throw new Error('Missing patient id in response')
    emit('created', patientId)
    emit('update:open', false)
  } catch (e: any) {
    error.value = toFriendlyFormError(e, 'Creating the patient')
  } finally {
    loading.value = false
  }
}
</script>
