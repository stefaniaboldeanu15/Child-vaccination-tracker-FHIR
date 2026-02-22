<template>
  <p v-bind="attrs" :class="classes">
    <slot />
  </p>
</template>

<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import { cn } from './utils'

defineOptions({ inheritAttrs: false })
const props = defineProps<{ class?: string }>()
const attrs = useAttrs()

const classes = computed(() => cn('text-sm text-muted-foreground', String(attrs.class ?? ''), props.class))
</script>
