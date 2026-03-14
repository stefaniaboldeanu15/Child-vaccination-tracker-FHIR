<template>
  <div :class="listClass" role="tablist">
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

const listClass = computed(() => {
  return [
    'inline-flex h-10 items-center justify-center rounded-md bg-gray-100 p-1 text-gray-500',
    props.class,
  ].join(' ')
})
</script>
