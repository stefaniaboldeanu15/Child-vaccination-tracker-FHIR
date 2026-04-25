<script setup lang="ts">
// Logo component – shield + medical cross + kids
defineProps<{
  size?: number
  variant?: 'full' | 'icon'
}>()
</script>

<template>
  <div class="vaccination-logo" :class="[variant]" :style="{ width: size ? `${size}px` : undefined, height: size ? `${size}px` : undefined }">
    <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true" role="img" aria-label="VaxCare Junior - care for children">
      <!-- Shield -->
      <path
        d="M24 1 L45 12 L45 22 C45 34 24 47 24 47 C24 47 3 34 3 22 L3 12 Z"
        fill="currentColor"
        fill-opacity="0.08"
        stroke="currentColor"
        stroke-width="2"
        stroke-linejoin="miter"
      />
      <!-- Medical plus -->
      <path d="M24 14 L24 34 M16 24 L32 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
      <circle cx="24" cy="24" r="2.5" fill="currentColor" />
      <!-- Kids-->
      <path
        d="M24 44 C22 40 18 39 18 42 C18 44 21 46 24 46 C27 46 30 44 30 42 C30 39 26 40 24 44 Z"
        fill="currentColor"
        fill-opacity="0.9"
        stroke="currentColor"
        stroke-width="1"
        stroke-linejoin="round"
      />
    </svg>
    <span v-if="variant === 'full'" class="logo-text">VaxCare Junior</span>
  </div>
</template>

<style scoped>
.vaccination-logo {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  color: var(--logo-color, #0d9488);
}

.vaccination-logo svg {
  width: 100%;
  height: 100%;
  min-width: 28px;
  min-height: 28px;
  max-width: 100%;
  max-height: 100%;
  flex-shrink: 0;
}

.vaccination-logo.icon {
  gap: 0;
}

.logo-text {
  font-weight: 700;
  font-size: 1.25rem;
  letter-spacing: -0.02em;
  color: inherit;
}
</style>
