<template>
  <PatientView :patient-id="patientId" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuth } from '@/auth/auth'
import PatientView from '@/components/PatientView.vue'

const { state } = useAuth()
const patientId = computed(() => state.patientId || '')
</script>
