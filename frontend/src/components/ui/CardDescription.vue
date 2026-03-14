<template>
  <p :class="descriptionClass">
    <slot />
  </p>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  class?: string
}

const props = withDefaults(defineProps<Props>(), {
  class: '',
})

const descriptionClass = computed(() => {
  return ['text-sm text-gray-500', props.class].join(' ')
})
</script>
