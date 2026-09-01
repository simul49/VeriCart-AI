<template>
  <span class="brand-mark" :style="{ width: size + 'px', height: size + 'px' }">
    <svg viewBox="0 0 32 32" fill="none" aria-hidden="true">
      <path
        d="M16 2.5 4.5 7.2v9.1c0 7.1 4.8 12.2 11.5 14.2 6.7-2 11.5-7.1 11.5-14.2V7.2L16 2.5Z"
        fill="url(#bmGrad)"
      />
      <path
        d="m11.2 16.2 3.4 3.4 6.4-6.6"
        stroke="#fff" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"
      />
      <defs>
        <linearGradient id="bmGrad" x1="4.5" y1="2.5" x2="27.5" y2="30.5">
          <stop stop-color="#FF7A5A" />
          <stop offset="1" stop-color="#F03524" />
        </linearGradient>
      </defs>
    </svg>
  </span>
</template>

<script setup>
defineProps({
  size: { type: [Number, String], default: 32 }
})
</script>

<style scoped>
.brand-mark {
  display: inline-flex;
  flex-shrink: 0;
}
.brand-mark svg { width: 100%; height: 100%; }
</style>
