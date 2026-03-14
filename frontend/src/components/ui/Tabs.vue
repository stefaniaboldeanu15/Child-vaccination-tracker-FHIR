<template>
  <div :class="tabsClass">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed, provide, ref, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    defaultValue?: string
    modelValue?: string
    class?: string
  }>(),
  {
    defaultValue: '',
    modelValue: undefined,
    class: '',
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const currentValue = ref(props.modelValue ?? props.defaultValue)

watch(
  () => props.modelValue,
  (value) => {
    if (value !== undefined) {
      currentValue.value = value
    }
  },
)

function setValue(value: string): void {
  currentValue.value = value
  emit('update:modelValue', value)
}

provide('tabsValue', currentValue)
provide('setTabsValue', setValue)

const tabsClass = computed(() => ['w-full', props.class].join(' '))
</script>
