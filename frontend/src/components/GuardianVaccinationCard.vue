<script setup lang="ts">
import { computed } from 'vue'
import { Calendar, Syringe } from 'lucide-vue-next'

import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Badge from '@/components/ui/Badge.vue'

export type Immunization = {
  immunizationId: string
  vaccineDisplay?: string | null
  vaccineCode?: string | null
  occurrenceDateTime?: string | null
  status?: string | null
}

const props = defineProps<{ immunization: Immunization }>()

const isCompleted = computed(() => String(props.immunization.status || '').toLowerCase() === 'completed')

function formatDate(v?: string | null) {
  if (!v) return ''
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return v
  return d.toLocaleDateString('en-GB')
}

const title = computed(() => props.immunization.vaccineDisplay || props.immunization.vaccineCode || 'Vaccine')
</script>

<template>
  <Card :class="isCompleted ? 'bg-green-50 border-green-200' : 'bg-white'">
    <CardContent class="pt-5">
      <div class="flex items-start justify-between gap-3">
        <div class="flex items-start gap-3">
          <div :class="isCompleted ? 'p-2 rounded-lg bg-green-100' : 'p-2 rounded-lg bg-gray-100'">
            <Syringe :class="isCompleted ? 'w-5 h-5 text-green-700' : 'w-5 h-5 text-gray-700'" />
          </div>
          <div class="min-w-0">
            <div class="font-medium text-gray-900 break-words">{{ title }}</div>
            <div class="mt-1 flex items-center gap-2 text-xs text-gray-600">
              <Calendar class="w-3.5 h-3.5 text-gray-500" />
              <span>
                Given:
                <span class="font-medium text-gray-700">{{ formatDate(immunization.occurrenceDateTime) || 'Unknown' }}</span>
              </span>
            </div>
          </div>
        </div>

        <Badge :variant="isCompleted ? 'default' : 'outline'" :class="isCompleted ? 'bg-green-600' : ''">
          {{ isCompleted ? 'Completed' : (immunization.status || 'Unknown') }}
        </Badge>
      </div>
    </CardContent>
  </Card>
</template>
