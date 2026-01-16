<template>
  <div v-bind="attrs" :class="classes" @click.stop>
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
    // Constrain dialog height so it never runs off-screen; allow internal scrolling.
    // Keep to utilities that exist in the precompiled CSS bundle.
    'mx-auto max-w-lg rounded-xl border bg-card text-card-foreground shadow-lg p-6 max-h-[90vh] overflow-y-auto',
    String(attrs.class ?? ''),
    props.class,
  ),
)
</script>
