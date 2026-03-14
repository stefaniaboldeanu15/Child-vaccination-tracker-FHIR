<template>
  <div :class="cardClass">
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

const cardClass = computed(() => {
  return [
    'rounded-xl border border-gray-200 bg-white text-gray-900 shadow-sm',
    props.class,
  ].join(' ')
})
</script>
