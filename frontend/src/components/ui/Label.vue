<template>
  <label v-bind="attrs" :for="forAttr" :class="classes">
    <slot />
  </label>
</template>

<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import { cn } from './utils'

defineOptions({ inheritAttrs: false })
const props = defineProps<{ forId?: string; class?: string }>()
const attrs = useAttrs()

const forAttr = computed(() => props.forId)
const classes = computed(() => cn('text-sm font-medium leading-none', String(attrs.class ?? ''), props.class))
</script>
