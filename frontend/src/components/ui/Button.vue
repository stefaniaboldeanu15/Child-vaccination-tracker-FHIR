<template>
  <button
    :type="type"
    :disabled="disabled"
    :class="buttonClass"
  >
    <slot />
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type Variant = 'default' | 'secondary' | 'outline' | 'ghost' | 'destructive'
type Size = 'sm' | 'md' | 'lg'

interface Props {
  type?: 'button' | 'submit' | 'reset'
  disabled?: boolean
  variant?: Variant
  size?: Size
  class?: string
}

const props = withDefaults(defineProps<Props>(), {
  type: 'button',
  disabled: false,
  variant: 'default',
  size: 'md',
  class: '',
})

const variantClasses: Record<Variant, string> = {
  default: 'bg-blue-600 text-white hover:bg-blue-700',
  secondary: 'bg-gray-600 text-white hover:bg-gray-700',
  outline: 'border border-gray-300 bg-white text-gray-900 hover:bg-gray-100',
  ghost: 'bg-transparent text-gray-900 hover:bg-gray-100',
  destructive: 'bg-red-600 text-white hover:bg-red-700',
}

const sizeClasses: Record<Size, string> = {
  sm: 'px-3 py-1.5 text-sm',
  md: 'px-4 py-2 text-sm',
  lg: 'px-5 py-2.5 text-base',
}

const buttonClass = computed(() => {
  return [
    'inline-flex items-center justify-center rounded-md font-medium transition-colors',
    'focus:outline-none focus:ring-2 focus:ring-blue-400 focus:ring-offset-2',
    'disabled:cursor-not-allowed disabled:opacity-50',
    variantClasses[props.variant],
    sizeClasses[props.size],
    props.class,
  ].join(' ')
})
</script>
