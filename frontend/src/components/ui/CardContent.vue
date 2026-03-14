<template>
  <div :class="contentClass">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  class?: string
}

const props = withDefaults(defineProps<Props>(), {
  class: '',
})

const contentClass = computed(() => {
  return ['p-6 pt-0', props.class].join(' ')
})
</script>
