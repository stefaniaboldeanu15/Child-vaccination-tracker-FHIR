<template>
  <div v-show="isActive" :class="contentClass" role="tabpanel">
    <slot />
  </div>
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

const isActive = computed(() => tabsValue?.value === props.value)

const contentClass = computed(() => ['mt-2', props.class].join(' '))
</script>
