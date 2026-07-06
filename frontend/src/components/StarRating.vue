<template>
  <span class="stars">
    <span v-for="i in 5" :key="i"
      :class="i <= rating ? 'star-filled' : 'star-empty'"
      :style="{ fontSize: size + 'px' }">★</span>
  </span>
</template>

<script setup>
defineProps({
  rating: { type: Number, default: 0 },
  size: { type: Number, default: 16 }
})
</script>
