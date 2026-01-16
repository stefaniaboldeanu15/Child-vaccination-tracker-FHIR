<template>
  <h2 v-bind="attrs" :class="classes">
    <slot />
  </h2>
</template>

<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import { cn } from './utils'

defineOptions({ inheritAttrs: false })
const props = defineProps<{ class?: string }>()
const attrs = useAttrs()

const classes = computed(() => cn('text-xl font-semibold leading-none', String(attrs.class ?? ''), props.class))
</script>
