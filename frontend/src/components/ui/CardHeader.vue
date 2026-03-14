<template>
  <div :class="headerClass">
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

const headerClass = computed(() => {
  return ['flex flex-col space-y-1.5 p-6', props.class].join(' ')
})
</script>
