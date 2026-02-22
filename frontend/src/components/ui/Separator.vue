<template>
  <div v-bind="attrs" :class="classes" />
</template>

<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import { cn } from './utils'

defineOptions({ inheritAttrs: false })
const props = defineProps<{ class?: string }>()
const attrs = useAttrs()

const classes = computed(() => cn('bg-border h-px w-full', String(attrs.class ?? ''), props.class))
</script>
