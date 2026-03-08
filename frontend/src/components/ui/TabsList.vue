<template>
  <div v-bind="attrs" :class="classes" role="tablist">
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
  cn(
    'inline-grid items-center justify-center rounded-md bg-muted p-1 text-muted-foreground',
    String(attrs.class ?? ''),
    props.class,
  ),
)
</script>
