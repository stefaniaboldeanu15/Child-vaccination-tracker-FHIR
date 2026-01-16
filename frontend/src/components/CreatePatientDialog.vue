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
              class="border-input bg-input-background dark:bg-input/30 flex h-9 w-full rounded-md border px-3 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50"
            />
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

        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div class="space-y-2">
            <Label for="idSystem">Identifier system</Label>
            <Input id="idSystem" v-model="identifierSystem" placeholder="http://hospital.example/mrn" />
          </div>
          <div class="space-y-2">
            <Label for="idValue">Identifier value</Label>
            <Input id="idValue" v-model="identifierValue" placeholder="12345" />
          </div>
        </div>

        <div class="space-y-2">
          <Label for="password">Password (for patient login)</Label>
          <Input id="password" v-model="password" type="password" placeholder="Choose a password" />
        </div>

        <div class="space-y-2">
          <Label for="country">Country</Label>
          <Input id="country" v-model="country" placeholder="e.g., AT" />
        </div>

        <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

        <div class="flex items-center justify-end gap-2 pt-1">
          <Button variant="outline" @click="emit('update:open', false)" :disabled="loading">Cancel</Button>
          <Button @click="create" :disabled="loading || !canSubmit">Create</Button>
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

const props = defineProps<{ open: boolean }>()
const emit = defineEmits<{
  (e: 'update:open', v: boolean): void
  (e: 'created', patientId: string): void
}>()

const givenName = ref('')
const familyName = ref('')
const birthDate = ref('')
const gender = ref('')
const identifierSystem = ref('')
const identifierValue = ref('')
const country = ref('')
const password = ref('')

const loading = ref(false)
const error = ref<string | null>(null)

const canSubmit = computed(() => {
  return !!(givenName.value.trim() || familyName.value.trim() || identifierValue.value.trim())
})

watch(
  () => props.open,
  (v) => {
    if (!v) return
    error.value = null
  },
)

async function create() {
  error.value = null
  loading.value = true
  try {
    const res = await backendFetch('/api/physician/patients', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        givenName: givenName.value.trim() || undefined,
        familyName: familyName.value.trim() || undefined,
        birthDate: birthDate.value || undefined,
        gender: gender.value || undefined,
        identifierSystem: identifierSystem.value.trim() || undefined,
        identifierValue: identifierValue.value.trim() || undefined,
        country: country.value.trim() || undefined,
        password: password.value || undefined,
      }),
    })

    const json = (await res.json()) as { patientId?: string }
    if (!json.patientId) throw new Error('Missing patientId')
    emit('created', json.patientId)
    emit('update:open', false)
  } catch (e: any) {
    error.value = String(e?.message ?? e)
  } finally {
    loading.value = false
  }
}
</script>
