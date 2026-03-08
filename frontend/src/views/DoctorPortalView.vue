<template>
  <DoctorView :doctor-id="doctorId" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import DoctorView from '@/components/DoctorView.vue'
import { useAuth } from '@/auth/auth'

const { state } = useAuth()
const doctorId = computed(() => state.practitioner?.practitionerId ?? '')
</script>
