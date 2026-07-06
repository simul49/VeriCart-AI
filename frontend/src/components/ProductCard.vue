<template>
  <div class="product-card" @click="$router.push(`/products/${product.id}`)">
    <img :src="imageUrl" :alt="product.name" loading="lazy" />
    <div class="info">
      <div class="name">{{ product.name }}</div>
      <div class="price">${{ product.price }}</div>
      <div class="meta">
        <StarRating :rating="product.rating" :size="12" />
        <span>({{ product.reviewCount || 0 }})</span>
        <div v-if="product.trustScore" :class="['trust-badge', trustLevelClass]">
          {{ product.trustScore }}
        </div>
      </div>
      <slot name="actions"></slot>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import StarRating from './StarRating.vue'

const props = defineProps({ product: Object })

const imageUrl = computed(() => {
  if (!props.product?.images) return 'https://placehold.co/280x200/F1F5F9/9CA3AF?text=No+Image'
  try {
    const imgs = JSON.parse(props.product.images)
    return Array.isArray(imgs) ? imgs[0] : imgs
  } catch { return props.product.images }
})

const trustLevelClass = computed(() => {
  const s = props.product?.trustScore
  if (!s) return ''
  if (s >= 85) return 'trust-excellent'
  if (s >= 70) return 'trust-high'
  if (s >= 50) return 'trust-medium'
  return 'trust-low'
})
</script>
