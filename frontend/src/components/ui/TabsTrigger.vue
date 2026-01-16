<template>
  <button
    type="button"
    role="tab"
    :aria-selected="isActive"
    v-bind="attrs"
    :class="classes"
    @click="onClick"
  >
    <slot />
  </button>
</template>

<script setup lang="ts">
import { computed, inject, useAttrs } from 'vue'
import { cn } from './utils'
import { TABS_KEY, type TabsContext } from './tabsContext'

defineOptions({ inheritAttrs: false })
const props = defineProps<{ value: string; class?: string }>()
const attrs = useAttrs()

const ctx = inject<TabsContext>(TABS_KEY)
if (!ctx) throw new Error('TabsTrigger must be used inside <Tabs>')

// vue-tsc sometimes fails to narrow `inject()` results across SFC compilation.
// The runtime check above guarantees `ctx` exists.
const isActive = computed(() => ctx!.value.value === props.value)

function onClick() {
  ctx!.setValue(props.value)
}

const classes = computed(() =>
  cn(
    'inline-flex items-center justify-center whitespace-nowrap rounded-md px-3 py-1.5 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50',
    isActive.value ? 'bg-background text-foreground shadow-sm' : 'text-muted-foreground hover:text-foreground',
    String(attrs.class ?? ''),
    props.class,
  ),
)
</script>
