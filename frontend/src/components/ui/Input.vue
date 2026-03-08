<template>
  <input
    v-bind="$attrs"
    :value="modelValue"
    @input="onInput"
    :class="classes"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { cn } from './utils'

defineOptions({ inheritAttrs: false })

const props = defineProps<{
  modelValue?: string
  class?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: string): void
}>()

function onInput(e: Event) {
  emit('update:modelValue', (e.target as HTMLInputElement).value)
}

const classes = computed(() =>
  cn(
    'border-input bg-input-background dark:bg-input/30 flex h-9 w-full rounded-md border px-3 py-2 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-50',
    props.class,
  ),
)
</script>
