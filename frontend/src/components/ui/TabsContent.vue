<template>
  <div v-if="isActive" v-bind="attrs" :class="classes" role="tabpanel">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed, inject, useAttrs } from 'vue'
import { cn } from './utils'
import { TABS_KEY, type TabsContext } from './tabsContext'

defineOptions({ inheritAttrs: false })
const props = defineProps<{ value: string; class?: string }>()
const attrs = useAttrs()

const ctx = inject<TabsContext>(TABS_KEY)
if (!ctx) throw new Error('TabsContent must be used inside <Tabs>')

const isActive = computed(() => ctx.value.value === props.value)
const classes = computed(() => cn(String(attrs.class ?? ''), props.class))
</script>
