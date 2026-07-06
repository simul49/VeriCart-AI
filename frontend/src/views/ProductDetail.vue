<template>
  <div class="page-container" v-if="product">
    <!-- Product Info -->
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:40px;margin-bottom:40px">
      <!-- Images -->
      <div>
        <img :src="mainImage" :alt="product.name"
          style="width:100%;border-radius:16px;max-height:400px;object-fit:cover;background:#F1F5F9" />
        <div style="display:flex;gap:8px;margin-top:12px" v-if="imageList.length > 1">
          <img v-for="(img, i) in imageList" :key="i" :src="img" @click="selectedImage = i"
            style="width:64px;height:64px;border-radius:8px;object-fit:cover;cursor:pointer"
            :style="{ border: selectedImage === i ? '2px solid var(--primary)' : '2px solid transparent' }" />
        </div>
      </div>

      <!-- Details -->
      <div>
        <h1 style="font-size:28px;font-weight:700">{{ product.name }}</h1>
        <p style="color:#6B7280;margin-top:4px">{{ product.brand }}</p>

        <div style="font-size:32px;font-weight:800;color:var(--primary);margin:16px 0">
          ${{ product.price }}
        </div>

        <div style="display:flex;align-items:center;gap:12px;margin-bottom:16px">
          <StarRating :rating="product.rating" />
          <span style="color:#6B7280">({{ product.reviewCount }} reviews)</span>
          <span style="color:#059669" v-if="product.stock > 0">In Stock ({{ product.stock }})</span>
          <span style="color:#DC2626" v-else>Out of Stock</span>
        </div>

        <p style="color:#4B5563;line-height:1.8">{{ product.description }}</p>

        <div style="display:flex;gap:12px;margin-top:24px">
          <el-button type="primary" size="large" @click="handleAddCart" :disabled="product.stock === 0">
            <el-icon><ShoppingCart /></el-icon> Add to Cart
          </el-button>
          <el-button size="large" @click="handleWishlist">
            <el-icon><Star /></el-icon> Wishlist
          </el-button>
        </div>
      </div>
    </div>

    <!-- Trust Score Card -->
    <div class="ai-card" style="margin-bottom:32px" v-if="product.trustScore">
      <h3><span>🛡️</span> AI Trust Score</h3>
      <div style="display:flex;align-items:center;gap:24px;flex-wrap:wrap">
        <div :class="['trust-badge-large', trustClass]">
          <span class="score">{{ product.trustScore }}</span>
          <span class="label">/ 100 — {{ product.trustLevel || 'Not Rated' }}</span>
        </div>
        <div style="flex:1;min-width:200px">
          <p v-if="product.fakeReviewCount > 0" style="color:#DC2626;margin-bottom:4px">
            ⚠️ {{ product.fakeReviewCount }} suspicious review(s) detected
          </p>
          <p v-else style="color:#059669;margin-bottom:4px">✅ No suspicious reviews detected</p>
          <el-button size="small" type="primary" text @click="analyzeProduct">
            <el-icon><Refresh /></el-icon> Refresh Analysis
          </el-button>
        </div>
      </div>
    </div>

    <div v-else-if="product.reviewCount > 0" class="ai-card" style="margin-bottom:32px">
      <h3>🛡️ AI Trust Score</h3>
      <p style="color:#6B7280">AI analysis not yet generated for this product.</p>
      <el-button type="primary" @click="analyzeProduct" :loading="analyzing">
        🔬 Analyze Now
      </el-button>
    </div>

    <!-- AI Summary -->
    <div class="ai-card" style="margin-bottom:32px" v-if="product.aiSummary">
      <h3><span>🤖</span> AI Review Summary</h3>
      <div v-html="renderedSummary" style="line-height:1.8"></div>
    </div>

    <!-- Ask AI About This Product -->
    <div class="ai-card" style="margin-bottom:32px">
      <div style="display:flex;align-items:center;justify-content:space-between">
        <h3 style="margin:0"><span>💬</span> Ask AI About This Product</h3>
        <el-button type="primary" size="small" @click="showProductChat = !showProductChat">
          {{ showProductChat ? 'Hide Chat' : 'Open Chat' }}
        </el-button>
      </div>
      <ChatPanel
        v-if="showProductChat"
        :productId="product.id"
        style="margin-top:16px;border:1px solid var(--border);border-radius:12px;overflow:hidden"
      />
    </div>

    <!-- Customer Reviews -->
    <h3 style="font-size:20px;font-weight:700;margin-bottom:16px">
      Customer Reviews ({{ reviews.length }})
    </h3>

    <div v-if="reviews.length">
      <div v-for="r in reviews" :key="r.id"
        :class="['review-card', { 'review-flagged': r.isFlagged }]">
        <div class="review-header">
          <el-avatar :size="32">{{ r.username?.[0] }}</el-avatar>
          <span class="name">{{ r.username }}</span>
          <StarRating :rating="r.rating" :size="14" />
          <span v-if="r.sentiment" :style="{ color: sentimentColor(r.sentiment), fontSize:'12px', fontWeight:600 }">
            {{ r.sentiment }}
          </span>
          <span v-if="r.isFlagged" style="color:#DC2626;font-size:12px">⚠️ Suspicious</span>
        </div>
        <p style="margin-top:4px;color:#4B5563">{{ r.content }}</p>
        <div v-if="r.fakeReason" style="margin-top:8px;font-size:12px;color:#DC2626">
          AI: {{ r.fakeReason }}
        </div>
        <div style="font-size:12px;color:#9CA3AF;margin-top:8px">
          {{ new Date(r.createdAt).toLocaleDateString() }}
        </div>
      </div>
    </div>

    <div v-else style="text-align:center;padding:40px;color:#9CA3AF">
      No reviews yet. Be the first to review this product!
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { productApi, reviewApi, aiApi, cartApi, wishlistApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { marked } from 'marked'
import StarRating from '@/components/StarRating.vue'
import ChatPanel from '@/components/ChatPanel.vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const auth = useAuthStore()
const cartStore = useCartStore()

const product = ref(null)
const reviews = ref([])
const categories = ref([])
const selectedImage = ref(0)
const analyzing = ref(false)
const showProductChat = ref(false)

const imageList = computed(() => {
  if (!product.value?.images) return []
  try {
    const imgs = JSON.parse(product.value.images)
    return Array.isArray(imgs) ? imgs : [imgs]
  } catch { return [product.value.images] }
})

const mainImage = computed(() =>
  imageList.value[selectedImage.value] || 'https://placehold.co/600x400/F1F5F9/9CA3AF?text=No+Image'
)

const trustClass = computed(() => {
  const s = product.value?.trustScore
  if (!s) return ''
  if (s >= 85) return 'trust-excellent'
  if (s >= 70) return 'trust-high'
  if (s >= 50) return 'trust-medium'
  return 'trust-low'
})

const renderedSummary = computed(() => {
  if (!product.value?.aiSummary) return ''
  return marked(product.value.aiSummary)
})

onMounted(async () => {
  try {
    const [prod, rev] = await Promise.all([
      productApi.detail(route.params.id),
      reviewApi.byProduct(route.params.id)
    ])
    product.value = prod.data
    reviews.value = rev.data || []
  } catch (e) { /* handled */ }
})

async function handleAddCart() {
  if (!auth.isLoggedIn) return ElMessage.warning('Please login first')
  await cartStore.addToCart(product.value.id)
  ElMessage.success('Added to cart!')
}

async function handleWishlist() {
  if (!auth.isLoggedIn) return ElMessage.warning('Please login first')
  try {
    const check = await wishlistApi.check(product.value.id)
    if (check.data?.inWishlist) {
      await wishlistApi.remove(product.value.id)
      ElMessage.success('Removed from wishlist')
    } else {
      await wishlistApi.add(product.value.id)
      ElMessage.success('Added to wishlist!')
    }
  } catch (e) {
    ElMessage.error('Failed to update wishlist')
  }
}

async function analyzeProduct() {
  analyzing.value = true
  try {
    const res = await aiApi.analyze(product.value.id)
    ElMessage.success('AI analysis complete!')
    // Refresh product data
    const prod = await productApi.detail(route.params.id)
    product.value = prod.data
  } catch (e) {
    ElMessage.error('AI analysis failed. Try again later.')
  } finally {
    analyzing.value = false
  }
}

function sentimentColor(s) {
  return s === 'POSITIVE' ? '#059669' : s === 'NEGATIVE' ? '#DC2626' : '#6B7280'
}
</script>
