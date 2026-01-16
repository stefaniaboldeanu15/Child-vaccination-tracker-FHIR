<template>
  <button v-bind="attrs" :type="type" :class="classes">
    <slot />
  </button>
</template>

<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import { cn } from './utils'

defineOptions({ inheritAttrs: false })
const props = defineProps<{
  variant?: 'default' | 'outline' | 'secondary'
  size?: 'default' | 'sm'
  type?: 'button' | 'submit' | 'reset'
  class?: string
}>()
const attrs = useAttrs()

const classes = computed(() => {
  const base = 'inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium transition-[color,box-shadow] outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50'

  const variant = props.variant === 'outline'
    ? 'border border-input bg-transparent hover:bg-accent hover:text-accent-foreground'
    : props.variant === 'secondary'
      ? 'bg-secondary text-secondary-foreground hover:bg-secondary/80'
      : 'bg-primary text-primary-foreground hover:bg-primary/90'

  const size = props.size === 'sm' ? 'h-8 px-3' : 'h-9 px-4'

  return cn(base, variant, size, String(attrs.class ?? ''), props.class)
})

const type = computed(() => props.type ?? 'button')
</script>
