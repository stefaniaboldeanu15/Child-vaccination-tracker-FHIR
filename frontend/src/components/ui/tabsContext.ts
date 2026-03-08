import type { ComputedRef } from 'vue'

export type TabsContext = {
  value: ComputedRef<string>
  setValue: (v: string) => void
}

export const TABS_KEY = Symbol('tabs')
