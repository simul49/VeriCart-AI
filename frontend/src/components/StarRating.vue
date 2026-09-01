<template>
  <!-- Editable mode: interactive stars with hover preview (used by the review form) -->
  <span v-if="editable" class="stars stars-editable" :style="{ fontSize: size + 'px' }">
    <span
      v-for="i in 5"
      :key="i"
      :class="['star-btn', i <= (hoverValue || modelValue) ? 'star-filled' : 'star-empty']"
      @click="select(i)"
      @mouseenter="hoverValue = i"
      @mouseleave="hoverValue = 0"
      role="button"
      :aria-label="`${i} star${i > 1 ? 's' : ''}`"
    >★</span>
    <span v-if="showLabel" class="star-label">{{ labelText }}</span>
  </span>

  <!-- Read-only mode: supports fractional ratings via a clipped overlay -->
  <span v-else class="stars" :style="{ fontSize: size + 'px' }" :title="`${rating} out of 5`">
    <span class="stars-base">★★★★★</span>
    <span class="stars-overlay" :style="{ width: fillPercent + '%' }">★★★★★</span>
  </span>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  rating: { type: Number, default: 0 },
  size: { type: Number, default: 16 },
  editable: { type: Boolean, default: false },
  modelValue: { type: Number, default: 0 },
  showLabel: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue'])

const hoverValue = ref(0)

const fillPercent = computed(() => {
  const r = Math.max(0, Math.min(5, props.rating || 0))
  return (r / 5) * 100
})

const LABELS = ['', 'Poor', 'Fair', 'Good', 'Very Good', 'Excellent']
const labelText = computed(() => LABELS[hoverValue.value || props.modelValue] || '')

function select(i) {
  emit('update:modelValue', i)
}
</script>

<style scoped>
.stars {
  position: relative;
  display: inline-block;
  line-height: 1;
  white-space: nowrap;
  color: var(--border, #dcdfe6);
}

.stars-base {
  color: #dcdfe6;
}

.stars-overlay {
  position: absolute;
  top: 0;
  left: 0;
  overflow: hidden;
  color: #ffa726;
  pointer-events: none;
}

.stars-editable .star-btn {
  cursor: pointer;
  transition: color 0.15s, transform 0.1s;
  padding: 0 1px;
}

.stars-editable .star-btn:hover {
  transform: scale(1.15);
}

.star-filled {
  color: #ffa726;
}

.star-empty {
  color: #dcdfe6;
}

.star-label {
  font-size: 13px;
  margin-left: 8px;
  color: var(--text-secondary, #606266);
}
</style>
