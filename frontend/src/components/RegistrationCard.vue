<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { startSmartLogin } from '@/utils/smart'
import type { SessionRole } from '@/auth/session'
import {
  registerPractitioner,
  registerRelatedPerson,
  type AuthSessionProfile,
  type PractitionerRegistrationRequest,
  type RelatedPersonRegistrationRequest,
} from '@/api/auth'

const props = defineProps<{
  role: SessionRole
}>()

const router = useRouter()
const submitting = ref(false)
const error = ref<string | null>(null)
const success = ref<AuthSessionProfile | null>(null)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  firstName: '',
  lastName: '',
  identifier: '',
  specialization: '',
  phone: '',
  email: '',
  organizationId: '',
  patientId: '',
  relationship: '',
  address: '',
})

const isPractitioner = computed(() => props.role === 'practitioner')
const title = computed(() => (isPractitioner.value ? 'Create practitioner account' : 'Create related person account'))
const subtitle = computed(() =>
  isPractitioner.value
    ? 'Register a new practitioner.'
    : 'Create a parent or guardian account linked to one child patient id.'
)
const submitLabel = computed(() => (submitting.value ? 'Creating account...' : 'Create account'))
const signInLabel = computed(() =>
  isPractitioner.value ? 'Continue to practitioner sign in' : 'Continue to parent sign in'
)

function trimmed(value: string) {
  const normalized = value.trim()
  return normalized ? normalized : undefined
}

function validate() {
  if (!form.username.trim()) return 'Username is required.'
  if (!form.password) return 'Password is required.'
  if (form.password.length < 8) return 'Password must be at least 8 characters.'
  if (form.password !== form.confirmPassword) return 'Passwords do not match.'
  if (!form.firstName.trim()) return 'First name is required.'
  if (!form.lastName.trim()) return 'Last name is required.'

  if (isPractitioner.value) {
    if (!form.identifier.trim()) return 'License or practitioner identifier is required.'
    if (!form.specialization.trim()) return 'Specialization is required.'
    return null
  }

  if (!form.patientId.trim()) return 'Linked child patient id is required.'
  if (!form.relationship.trim()) return 'Relationship is required.'
  return null
}

function toPractitionerPayload(): PractitionerRegistrationRequest {
  return {
    username: form.username.trim(),
    password: form.password,
    firstName: trimmed(form.firstName),
    lastName: trimmed(form.lastName),
    identifier: trimmed(form.identifier),
    specialization: trimmed(form.specialization),
    phone: trimmed(form.phone),
    email: trimmed(form.email),
    organizationId: trimmed(form.organizationId),
  }
}

function toRelatedPersonPayload(): RelatedPersonRegistrationRequest {
  return {
    username: form.username.trim(),
    password: form.password,
    patientId: trimmed(form.patientId),
    firstName: trimmed(form.firstName),
    lastName: trimmed(form.lastName),
    relationship: trimmed(form.relationship),
    phone: trimmed(form.phone),
    email: trimmed(form.email),
    address: trimmed(form.address),
  }
}

function friendlyError(reason: unknown) {
  if (!(reason instanceof Error)) return 'The registration request failed.'

  const raw = reason.message
  const jsonStart = raw.indexOf('{')
  if (jsonStart >= 0) {
    try {
      const parsed = JSON.parse(raw.slice(jsonStart)) as { detail?: string; message?: string; error?: string }
      return parsed.detail || parsed.message || parsed.error || raw
    } catch {
      return raw
    }
  }

  return raw
}

async function submit() {
  error.value = null

  const validationError = validate()
  if (validationError) {
    error.value = validationError
    return
  }

  submitting.value = true
  try {
    success.value = isPractitioner.value
      ? await registerPractitioner(toPractitionerPayload())
      : await registerRelatedPerson(toRelatedPersonPayload())
  } catch (reason) {
    error.value = friendlyError(reason)
  } finally {
    submitting.value = false
  }
}

async function continueToLogin() {
  await startSmartLogin(props.role)
}

async function goHome() {
  await router.push('/')
}

function reset() {
  success.value = null
  error.value = null
  form.username = ''
  form.password = ''
  form.confirmPassword = ''
  form.firstName = ''
  form.lastName = ''
  form.identifier = ''
  form.specialization = ''
  form.phone = ''
  form.email = ''
  form.organizationId = ''
  form.patientId = ''
  form.relationship = ''
  form.address = ''
}
</script>

<template>
  <va-card class="surface-card hero-card auth-card">
    <div class="kicker">{{ isPractitioner ? 'Practitioner registration' : 'Related person registration' }}</div>
    <h1 style="margin: 8px 0 12px">{{ title }}</h1>
    <p class="muted" style="margin: 0 0 18px">{{ subtitle }}</p>

    <va-alert v-if="error" color="danger" outline style="margin-bottom: 18px">
      {{ error }}
    </va-alert>

    <template v-if="success">
      <va-alert color="success" outline style="margin-bottom: 18px">
        The account was created successfully. Use the existing SMART sign-in flow to start an authenticated session.
      </va-alert>

      <div class="record-card section-stack" style="margin-bottom: 18px">
        <div class="record-grid">
          <div class="record-item">
            <strong>Username</strong>
            <span>{{ success.username }}</span>
          </div>
          <div class="record-item">
            <strong>Role</strong>
            <span>{{ success.role }}</span>
          </div>
          <div class="record-item">
            <strong>Display name</strong>
            <span>{{ success.displayName }}</span>
          </div>
          <div class="record-item">
            <strong>FHIR user</strong>
            <span>{{ success.fhirUser }}</span>
          </div>
        </div>
      </div>

      <div class="toolbar">
        <va-button @click="continueToLogin">{{ signInLabel }}</va-button>
        <va-button preset="secondary" @click="reset">Create another account</va-button>
        <va-button preset="secondary" color="secondary" @click="goHome">Back to home</va-button>
      </div>
    </template>

    <form v-else class="section-stack" @submit.prevent="submit">
      <div class="form-grid">
        <va-input v-model="form.firstName" label="First name" placeholder="Enter first name" />
        <va-input v-model="form.lastName" label="Last name" placeholder="Enter last name" />

        <va-input v-model="form.username" label="Username" placeholder="Choose a username" autocomplete="username" />
        <va-input v-model="form.password" label="Password" type="password" placeholder="Choose a password" autocomplete="new-password" />

        <va-input
          v-model="form.confirmPassword"
          label="Confirm password"
          type="password"
          placeholder="Repeat password"
          autocomplete="new-password"
        />
        <va-input v-model="form.phone" label="Phone" placeholder="Optional phone number" />

        <va-input v-model="form.email" label="Email" placeholder="Optional email address" class="full" />

        <template v-if="isPractitioner">
          <va-input v-model="form.identifier" label="License / identifier" placeholder="e.g. LIC-DR-SCHMIDT-01" />
          <va-input v-model="form.specialization" label="Specialization" placeholder="e.g. Pediatrics" />
          <va-input v-model="form.organizationId" label="Organization id" placeholder="Optional organization id" class="full" />
        </template>

        <template v-else>
          <va-input v-model="form.patientId" label="Linked child patient id" placeholder="e.g. patient-001" />
          <va-input v-model="form.relationship" label="Relationship" placeholder="e.g. mother, father, guardian" />
          <va-input v-model="form.address" label="Address" placeholder="Optional address" class="full" />
        </template>
      </div>

      <div class="toolbar" style="margin-top: 6px">
        <va-button type="submit" :loading="submitting">{{ submitLabel }}</va-button>
        <va-button preset="secondary" color="secondary" @click.prevent="goHome">Cancel</va-button>
      </div>
    </form>
  </va-card>
</template>
