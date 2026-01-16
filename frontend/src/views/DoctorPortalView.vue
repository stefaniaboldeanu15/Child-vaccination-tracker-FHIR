<template>
  <DoctorView :doctor-id="doctorId" />
</template>

<script setup lang="ts">
import DoctorView from '@/components/DoctorView.vue'

import { computed } from 'vue'
import { useAuth } from '@/auth/auth'

const { state } = useAuth()
const doctorId = computed(() => state.doctorId ?? '')
</script>
