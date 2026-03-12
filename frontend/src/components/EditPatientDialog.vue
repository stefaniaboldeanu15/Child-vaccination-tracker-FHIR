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
              class="border-input bg-input-background dark:bg-input/30 flex h-9 w-full rounded-md border px-3 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50"
            />
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

        <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

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

const canSubmit = computed(() => !!(givenName.value.trim() || familyName.value.trim()))

watch(() => props.open, (v) => {
  if (!v) return
  error.value = null
  givenName.value = props.initialFirstName ?? ''
  familyName.value = props.initialLastName ?? ''
  birthDate.value = props.initialBirthDate ?? ''
  gender.value = props.initialGender ?? ''
})

async function save() {
  error.value = null
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
    error.value = String(e?.message ?? e)
  } finally {
    loading.value = false
  }
}
</script>
