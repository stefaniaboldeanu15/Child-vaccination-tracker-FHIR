<template>
  <div v-bind="attrs" :class="classes">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed, provide, ref, useAttrs, watch } from 'vue'
import { cn } from './utils'
import { TABS_KEY, type TabsContext } from './tabsContext'

defineOptions({ inheritAttrs: false })

const props = defineProps<{
  modelValue?: string
  defaultValue?: string
  class?: string
}>()

const emit = defineEmits<{ (e: 'update:modelValue', v: string): void }>()

const attrs = useAttrs()

const internal = ref(props.defaultValue ?? '')

watch(
  () => props.modelValue,
  (v) => {
    if (v !== undefined) internal.value = v
  },
  { immediate: true },
)

function setValue(v: string) {
  if (props.modelValue === undefined) {
    internal.value = v
  }
  emit('update:modelValue', v)
}

const value = computed(() => internal.value)

const ctx: TabsContext = { value, setValue }
provide(TABS_KEY, ctx)

const classes = computed(() => cn(String(attrs.class ?? ''), props.class))
</script>
