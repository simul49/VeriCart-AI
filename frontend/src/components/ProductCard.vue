<template>
  <div class="product-card" @click="$router.push(`/products/${product.id}`)">
    <div class="img-wrapper">
      <img v-if="!imgFailed" :src="imageUrl" :alt="product.name" loading="lazy" @error="imgFailed = true" />
      <div v-else class="img-fallback">
        <el-icon class="fallback-icon"><component :is="categoryIcon" /></el-icon>
      </div>

      <!-- Discount badge -->
      <span class="discount-badge" v-if="product.originalPrice && product.originalPrice > product.price">
        -{{ discountPercent }}%
      </span>

      <!-- Trust badge over image -->
      <span v-if="product.trustScore" :class="['img-trust', trustLevelClass]" :title="`AI trust score ${product.trustScore}/100`">
        <el-icon><CircleCheck /></el-icon> {{ product.trustScore }}
      </span>

      <!-- Quick add -->
      <button class="quick-add" @click.stop="handleQuickAdd" v-if="showQuickAdd">
        <el-icon><ShoppingCart /></el-icon> Add to Cart
      </button>
    </div>

    <div class="info">
      <div class="brand" v-if="product.brand">{{ product.brand }}</div>
      <div class="name">{{ product.name }}</div>
      <div class="price-row">
        <span class="price">${{ product.price }}</span>
        <span class="original-price" v-if="product.originalPrice && product.originalPrice > product.price">
          ${{ product.originalPrice }}
        </span>
      </div>
      <div class="meta">
        <StarRating :rating="product.rating" :size="12" />
        <span>({{ product.reviewCount || 0 }})</span>
        <span class="sales">{{ product.soldCount ? product.soldCount + ' sold' : '' }}</span>
      </div>
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import {
  Monitor, Cpu, House, Reading, Football, Iphone, Watch, Headset,
  Suitcase, ShoppingBag, Goods, ShoppingCart, CircleCheck
} from '@element-plus/icons-vue'
import StarRating from './StarRating.vue'

const props = defineProps({
  product: { type: Object, required: true },
  showQuickAdd: { type: Boolean, default: true }
})

const router = useRouter()
const cartStore = useCartStore()
const auth = useAuthStore()
const imgFailed = ref(false)

const CATEGORY_ICONS = {
  1: Monitor, 2: Cpu, 3: House, 4: Reading, 5: Football,
  6: Iphone, 7: Watch, 8: Headset, 9: Suitcase, 10: ShoppingBag
}

const imageUrl = computed(() => {
  const raw = props.product?.images
  if (!raw) return ''
  try {
    const imgs = JSON.parse(raw)
    return Array.isArray(imgs) ? imgs[0] : String(imgs)
  } catch {
    return String(raw)
  }
})

const discountPercent = computed(() => {
  if (!props.product?.originalPrice || !props.product?.price) return 0
  return Math.round((1 - props.product.price / props.product.originalPrice) * 100)
})

const categoryIcon = computed(() => CATEGORY_ICONS[props.product?.categoryId] || Goods)

const trustLevelClass = computed(() => {
  const s = props.product?.trustScore
  if (!s) return ''
  if (s >= 85) return 'trust-excellent'
  if (s >= 70) return 'trust-high'
  if (s >= 50) return 'trust-medium'
  return 'trust-low'
})

async function handleQuickAdd() {
  if (!auth.isLoggedIn) {
    ElMessage.warning('Please sign in to add items to your cart')
    router.push('/login')
    return
  }
  try {
    await cartStore.addToCart(props.product.id)
    ElMessage.success('Added to cart')
  } catch {
    ElMessage.error('Failed to add to cart')
  }
}
</script>

<style scoped>
.img-fallback {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FFEFEC 0%, #FFE2DD 100%);
  color: var(--primary);
}
.fallback-icon { font-size: 40px; }

/* Trust score chip over the image */
.img-trust {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 9px;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 800;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(6px);
  box-shadow: var(--shadow-sm);
}
.img-trust .el-icon { font-size: 12px; }

.brand {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--text-muted);
  margin-bottom: 3px;
}
</style>
