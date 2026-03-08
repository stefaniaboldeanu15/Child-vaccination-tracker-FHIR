<template>
  <div v-bind="attrs" :class="classes">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import { cn } from './utils'

defineOptions({ inheritAttrs: false })
const props = defineProps<{ class?: string }>()
const attrs = useAttrs()

const classes = computed(() =>
  cn('rounded-xl border bg-card text-card-foreground shadow-md', String(attrs.class ?? ''), props.class),
)
</script>
