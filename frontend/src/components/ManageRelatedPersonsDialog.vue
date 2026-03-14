<template>
  <Dialog :open="open" @update:open="(v) => emit('update:open', v)">
    <DialogContent v-if="open" class="max-w-2xl max-h-[85vh] overflow-y-auto">
      <DialogHeader>
        <DialogTitle>Related Persons</DialogTitle>
        <DialogDescription>Manage guardians and related contacts for this patient.</DialogDescription>
      </DialogHeader>

      <div class="space-y-4">
        <!-- Existing related persons -->
        <div v-if="localList.length > 0" class="space-y-3">
          <div
            v-for="rp in localList"
            :key="rp.relatedPersonId"
            class="rounded-lg border p-4 space-y-3"
          >
            <template v-if="editingId === rp.relatedPersonId">
              <!-- Inline edit form -->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
                <div class="space-y-1">
                  <Label>Full name</Label>
                  <Input v-model="editForm.fullName" placeholder="Full name" />
                </div>
                <div class="space-y-1">
                  <Label>Relationship</Label>
                  <Input v-model="editForm.relationship" placeholder="e.g., parent, guardian" />
                </div>
                <div class="space-y-1">
                  <Label>Phone</Label>
                  <Input v-model="editForm.phone" placeholder="+43 ..." />
                </div>
                <div class="space-y-1">
                  <Label>Email</Label>
                  <Input v-model="editForm.email" placeholder="email@example.com" />
                </div>
                <div class="space-y-1 md:col-span-2">
                  <Label>Address</Label>
                  <Input v-model="editForm.address" placeholder="Street, City" />
                </div>
              </div>
              <p v-if="editError" class="text-sm text-red-600">{{ editError }}</p>
              <div class="flex gap-2 justify-end">
                <Button variant="outline" size="sm" @click="cancelEdit" :disabled="editLoading">Cancel</Button>
                <Button size="sm" @click="saveEdit(rp.relatedPersonId)" :disabled="editLoading">
                  {{ editLoading ? 'Saving…' : 'Save' }}
                </Button>
              </div>
            </template>

            <template v-else>
              <!-- Read view -->
              <div class="flex items-start justify-between gap-3">
                <div class="space-y-1 min-w-0">
                  <div class="text-gray-900">{{ rp.fullName || '(no name)' }}</div>
                  <div class="text-sm text-gray-600 flex flex-wrap gap-x-3 gap-y-1">
                    <span v-if="rp.relationship">{{ rp.relationship }}</span>
                    <span v-if="rp.phone">{{ rp.phone }}</span>
                    <span v-if="rp.email">{{ rp.email }}</span>
                  </div>
                  <div v-if="rp.address" class="text-xs text-gray-500">{{ rp.address }}</div>
                  <div class="text-xs text-gray-400">ID: {{ rp.relatedPersonId }}</div>
                </div>
                <Button variant="outline" size="sm" @click="startEdit(rp)">Edit</Button>
              </div>
            </template>
          </div>
        </div>

        <div v-else-if="!showCreateForm" class="text-gray-500 text-sm">No related persons linked to this patient.</div>

        <!-- Create form -->
        <div v-if="showCreateForm" class="rounded-lg border border-blue-200 bg-blue-50 p-4 space-y-3">
          <div class="text-gray-900">New Related Person</div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div class="space-y-1">
              <Label>Full name</Label>
              <Input v-model="createForm.fullName" placeholder="Full name" />
            </div>
            <div class="space-y-1">
              <Label>Relationship</Label>
              <Input v-model="createForm.relationship" placeholder="e.g., parent, guardian" />
            </div>
            <div class="space-y-1">
              <Label>Identifier (SVNR)</Label>
              <Input v-model="createForm.relatedPersonIdentifier" placeholder="Optional SVNR" />
            </div>
            <div class="space-y-1">
              <Label>Phone</Label>
              <Input v-model="createForm.phone" placeholder="+43 ..." />
            </div>
            <div class="space-y-1">
              <Label>Email</Label>
              <Input v-model="createForm.email" placeholder="email@example.com" />
            </div>
            <div class="space-y-1">
              <Label>Address</Label>
              <Input v-model="createForm.address" placeholder="Street, City" />
            </div>
          </div>
          <p v-if="createError" class="text-sm text-red-600">{{ createError }}</p>
          <div class="flex gap-2 justify-end">
            <Button variant="outline" size="sm" @click="showCreateForm = false; createError = null" :disabled="createLoading">Cancel</Button>
            <Button size="sm" @click="create" :disabled="createLoading || !createForm.fullName.trim()">
              {{ createLoading ? 'Creating…' : 'Create' }}
            </Button>
          </div>
        </div>

        <div class="flex justify-between pt-2">
          <Button variant="outline" @click="showCreateForm = true" v-if="!showCreateForm">
            + Add Related Person
          </Button>
          <div v-else />
          <Button variant="outline" @click="emit('update:open', false)">Close</Button>
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

type RelatedPersonDTO = {
  relatedPersonId: string
  relatedPersonIdentifier?: string
  relationship?: string
  fullName?: string
  phone?: string
  email?: string
  address?: string
}

const props = defineProps<{
  open: boolean
  patientId: string
  relatedPersons: RelatedPersonDTO[]
}>()

const emit = defineEmits<{
  (e: 'update:open', v: boolean): void
  (e: 'changed'): void
}>()

const localList = ref<RelatedPersonDTO[]>([])
watch(() => props.relatedPersons, (v) => { localList.value = [...v] }, { immediate: true })
watch(() => props.open, (v) => {
  if (v) {
    localList.value = [...props.relatedPersons]
    cancelEdit()
    showCreateForm.value = false
    createError.value = null
  }
})

// Edit
const editingId = ref<string | null>(null)
const editForm = ref({ fullName: '', relationship: '', phone: '', email: '', address: '' })
const editLoading = ref(false)
const editError = ref<string | null>(null)

function startEdit(rp: RelatedPersonDTO) {
  editingId.value = rp.relatedPersonId
  editForm.value = {
    fullName: rp.fullName ?? '',
    relationship: rp.relationship ?? '',
    phone: rp.phone ?? '',
    email: rp.email ?? '',
    address: rp.address ?? '',
  }
  editError.value = null
}

function cancelEdit() {
  editingId.value = null
  editError.value = null
}

async function saveEdit(relatedPersonId: string) {
  editError.value = null
  editLoading.value = true
  try {
    await backendFetch(`/api/practitioner/dashboard/related-persons/${encodeURIComponent(relatedPersonId)}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        fullName: editForm.value.fullName.trim() || undefined,
        relationship: editForm.value.relationship.trim() || undefined,
        phone: editForm.value.phone.trim() || undefined,
        email: editForm.value.email.trim() || undefined,
        address: editForm.value.address.trim() || undefined,
      }),
    })
    // Update local list
    const idx = localList.value.findIndex((r) => r.relatedPersonId === relatedPersonId)
    if (idx !== -1) {
      localList.value[idx] = { ...localList.value[idx], ...editForm.value }
    }
    editingId.value = null
    emit('changed')
  } catch (e: any) {
    editError.value = String(e?.message ?? e)
  } finally {
    editLoading.value = false
  }
}

// Create
const showCreateForm = ref(false)
const createForm = ref({
  fullName: '',
  relationship: '',
  relatedPersonIdentifier: '',
  phone: '',
  email: '',
  address: '',
})
const createLoading = ref(false)
const createError = ref<string | null>(null)

async function create() {
  createError.value = null
  createLoading.value = true
  try {
    const res = await backendFetch('/api/practitioner/dashboard/related-persons', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        patientId: props.patientId,
        relatedPersonIdentifier: createForm.value.relatedPersonIdentifier.trim() || undefined,
        relationship: createForm.value.relationship.trim() || undefined,
        fullName: createForm.value.fullName.trim() || undefined,
        phone: createForm.value.phone.trim() || undefined,
        email: createForm.value.email.trim() || undefined,
        address: createForm.value.address.trim() || undefined,
      }),
    })
    const newId = (await res.text()).trim()
    localList.value.push({
      relatedPersonId: newId || `new-${Date.now()}`,
      fullName: createForm.value.fullName,
      relationship: createForm.value.relationship,
      relatedPersonIdentifier: createForm.value.relatedPersonIdentifier || undefined,
      phone: createForm.value.phone || undefined,
      email: createForm.value.email || undefined,
      address: createForm.value.address || undefined,
    })
    createForm.value = { fullName: '', relationship: '', relatedPersonIdentifier: '', phone: '', email: '', address: '' }
    showCreateForm.value = false
    emit('changed')
  } catch (e: any) {
    createError.value = String(e?.message ?? e)
  } finally {
    createLoading.value = false
  }
}
</script>
