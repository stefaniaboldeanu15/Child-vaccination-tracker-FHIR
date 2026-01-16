<template>
  <span v-bind="attrs" :class="classes">
    <slot />
  </span>
</template>

<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import { cn } from './utils'

defineOptions({ inheritAttrs: false })
const props = defineProps<{ variant?: 'default' | 'outline'; class?: string }>()
const attrs = useAttrs()

const classes = computed(() => {
  const base = 'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium transition-colors'
  const variant = props.variant === 'outline'
    ? 'border border-border bg-transparent text-foreground'
    : 'bg-primary text-primary-foreground'
  return cn(base, variant, String(attrs.class ?? ''), props.class)
})
</script>
