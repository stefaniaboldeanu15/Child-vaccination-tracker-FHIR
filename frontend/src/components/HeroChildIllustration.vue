<script setup lang="ts">
// Hero illustration – child + specialist symbol
defineProps<{
  class?: string
}>()
</script>

<template>
  <div class="hero-illustration" :class="$props.class" aria-hidden="true">
    <svg viewBox="0 0 200 200" fill="none" xmlns="http://www.w3.org/2000/svg">
      <!-- Soft background circle -->
      <circle cx="100" cy="100" r="88" fill="currentColor" fill-opacity="0.05" />
      <!-- Pointed shield – Spitze / specialist symbol -->
      <path
        d="M100 28 L168 54 L168 86 C168 128 100 168 100 168 C100 168 32 128 32 86 L32 54 Z"
        fill="currentColor"
        fill-opacity="0.1"
        stroke="currentColor"
        stroke-width="2.2"
        stroke-linejoin="miter"
      />
      <!-- Cartoon child -->
      <path d="M100 52 L100 112 M76 82 L124 82" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" />
      <circle cx="100" cy="82" r="3.5" fill="currentColor" />
      <circle cx="100" cy="142" r="26" fill="currentColor" fill-opacity="0.08" stroke="currentColor" stroke-width="2" />
      <circle cx="92" cy="136" r="3" fill="currentColor" fill-opacity="0.8" />
      <circle cx="108" cy="136" r="3" fill="currentColor" fill-opacity="0.8" />
      <path d="M92 148 Q100 154 108 148" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" fill="none" opacity="0.9" />
      <ellipse cx="100" cy="172" rx="18" ry="12" fill="currentColor" fill-opacity="0.06" stroke="currentColor" stroke-width="1.5" opacity="0.9" />
    </svg>
  </div>
</template>

<style scoped>
.hero-illustration {
  width: 100%;
  height: 100%;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  color: var(--hero-illustration-color, #0d9488);
}
.hero-illustration svg {
  width: 100%;
  height: 100%;
  max-width: 280px;
  max-height: 280px;
  object-fit: contain;
}
</style>
