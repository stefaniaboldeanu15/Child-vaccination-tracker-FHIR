<template>
  <Card :class="cardClass">
    <CardContent class="pt-6">
      <div class="flex items-start justify-between mb-4">
        <div class="flex items-start gap-3">
          <div :class="iconWrapperClass">
            <Syringe :class="iconClass" />
          </div>
          <div>
            <h4 class="text-gray-900">{{ vaccination.vaccineName }}</h4>
            <p class="text-gray-600">{{ vaccination.vaccineType }}</p>
          </div>
        </div>
        <Badge :variant="isCompleted ? 'default' : 'outline'" :class="badgeClass">
          {{ isCompleted ? 'Completed' : 'Scheduled' }}
        </Badge>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-3 text-gray-600">
        <div class="flex items-center gap-2">
          <Calendar class="w-4 h-4 text-gray-400" />
          <span>Date: {{ formatDate(vaccination.date) }}</span>
        </div>

        <div class="flex items-center gap-2">
          <Shield class="w-4 h-4 text-gray-400" />
          <span>Dose: {{ vaccination.doseNumber }} of {{ vaccination.totalDoses }}</span>
        </div>

        <template v-if="isCompleted">
          <div class="flex items-center gap-2">
            <User class="w-4 h-4 text-gray-400" />
            <span>Dr. {{ vaccination.administeredBy }}</span>
          </div>

          <div class="flex items-center gap-2">
            <MapPin class="w-4 h-4 text-gray-400" />
            <span>{{ vaccination.location }}</span>
          </div>
        </template>
      </div>

      <div v-if="isCompleted" class="mt-3 pt-3 border-t text-gray-600 grid grid-cols-2 gap-2">
        <div>
          <span class="text-gray-500">Manufacturer:</span> {{ vaccination.manufacturer }}
        </div>
        <div>
          <span class="text-gray-500">Batch:</span> {{ vaccination.batchNumber }}
        </div>
      </div>

      <div v-if="vaccination.nextDoseDate" class="mt-3 pt-3 border-t">
        <div class="flex items-center gap-2 text-orange-600">
          <Calendar class="w-4 h-4" />
          <span>Next dose scheduled: {{ formatDate(vaccination.nextDoseDate) }}</span>
        </div>
      </div>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Vaccination } from '@/mockData'
import { Calendar, MapPin, User, Shield, Syringe } from 'lucide-vue-next'

import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Badge from '@/components/ui/Badge.vue'

const props = defineProps<{ vaccination: Vaccination }>()

const isCompleted = computed(() => props.vaccination.status === 'completed')

const cardClass = computed(() =>
  `${isCompleted.value ? 'bg-white' : 'bg-blue-50 border-blue-200'} hover:shadow-md transition-shadow`,
)

const iconWrapperClass = computed(() =>
  `p-3 rounded-lg ${isCompleted.value ? 'bg-green-100' : 'bg-blue-100'}`,
)

const iconClass = computed(() =>
  `w-5 h-5 ${isCompleted.value ? 'text-green-600' : 'text-blue-600'}`,
)

const badgeClass = computed(() =>
  isCompleted.value ? 'bg-green-600' : 'bg-blue-600 text-white',
)

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-GB')
}
</script>
