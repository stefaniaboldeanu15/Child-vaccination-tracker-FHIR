<template>
  <h3 :class="titleClass">
    <slot />
  </h3>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  class?: string
}

const props = withDefaults(defineProps<Props>(), {
  class: '',
})

const titleClass = computed(() => {
  return ['text-2xl font-semibold leading-none tracking-tight', props.class].join(' ')
})
</script>
