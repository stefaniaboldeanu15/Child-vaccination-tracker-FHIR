<template>
  <button
    type="button"
    role="tab"
    :class="triggerClass"
    :aria-selected="isActive"
    @click="activate"
  >
    <slot />
  </button>
</template>

<script setup lang="ts">
import { computed, inject, type Ref } from 'vue'

const props = withDefaults(
  defineProps<{
    value: string
    class?: string
  }>(),
  {
    class: '',
  },
)

const tabsValue = inject<Ref<string>>('tabsValue')
const setTabsValue = inject<(value: string) => void>('setTabsValue')

const isActive = computed(() => tabsValue?.value === props.value)

function activate(): void {
  setTabsValue?.(props.value)
}

const triggerClass = computed(() => {
  return [
    'inline-flex items-center justify-center whitespace-nowrap rounded-sm px-3 py-1.5 text-sm font-medium transition-all',
    isActive.value ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-600 hover:text-gray-900',
    props.class,
  ].join(' ')
})
</script>
