<template>
  <div class="page-container">
    <div class="product-detail-layout" v-if="product">
      <!-- Gallery -->
      <div class="product-gallery">
        <img :src="mainImage" :alt="product.name" class="main-image" />
        <div class="thumb-list" v-if="imageList.length > 1">
          <img
            v-for="(img, i) in imageList"
            :key="i"
            :src="img"
            :class="['thumb', { active: selectedImage === i }]"
            @click="selectedImage = i"
          />
        </div>
      </div>

      <!-- Product Info -->
      <div>
        <div>
          <p style="color:var(--text-muted);font-size:var(--font-sm)">{{ product.brand || categoryName }}</p>
          <h1 style="font-size:24px;font-weight:700;line-height:1.4;margin:8px 0">{{ product.name }}</h1>
        </div>

        <!-- Price Section -->
        <div class="product-price-section">
          <div style="display:flex;align-items:baseline;flex-wrap:wrap;gap:8px">
            <span class="current-price">${{ product.price }}</span>
            <span class="original-price" v-if="product.originalPrice">${{ product.originalPrice }}</span>
            <span class="discount-tag" v-if="product.originalPrice && product.originalPrice > product.price">
              -{{ Math.round((1 - product.price / product.originalPrice) * 100) }}% OFF
            </span>
          </div>
        </div>

        <!-- Rating & Trust -->
        <div style="display:flex;align-items:center;gap:16px;flex-wrap:wrap;margin:16px 0">
          <div style="display:flex;align-items:center;gap:6px">
            <StarRating :rating="product.rating" />
            <span style="font-size:var(--font-sm);color:var(--text-secondary)">
              {{ product.rating?.toFixed(1) || 'N/A' }} ({{ product.reviewCount }} reviews)
            </span>
          </div>
          <span style="color:var(--success);font-weight:600;font-size:var(--font-sm)" v-if="product.soldCount">
            {{ product.soldCount }}+ sold
          </span>
          <span style="color:var(--success);font-weight:600;font-size:var(--font-sm)" v-if="product.stock > 0">
            In Stock
          </span>
          <span style="color:var(--danger);font-weight:600;font-size:var(--font-sm)" v-else>Out of Stock</span>
        </div>

        <!-- Specs -->
        <div style="margin:16px 0" v-if="product.specs">
          <div v-for="(val, key) in parseSpecs(product.specs)" :key="key"
            style="display:flex;font-size:var(--font-sm);padding:6px 0;border-bottom:1px dashed var(--border-light)">
            <span style="color:var(--text-muted);min-width:120px">{{ key }}</span>
            <span style="color:var(--text)">{{ val }}</span>
          </div>
        </div>

        <!-- Quantity -->
        <div style="display:flex;align-items:center;gap:12px;margin:20px 0">
          <span style="font-weight:600;font-size:var(--font-sm)">Quantity:</span>
          <el-input-number v-model="quantity" :min="1" :max="Math.min(product.stock || 99, 99)" size="large" />
          <span style="font-size:var(--font-xs);color:var(--text-muted)">({{ product.stock || 0 }} available)</span>
        </div>

        <!-- Actions -->
        <div class="product-actions">
          <button class="buy-now-btn" @click="handleBuyNow" :disabled="!product.stock">
            Buy Now
          </button>
          <button class="add-cart-btn" @click="handleAddCart" :disabled="!product.stock">
            🛒 Add to Cart
          </button>
          <button class="msg-btn" @click="openMessageDialog">
            <el-icon><ChatDotRound /></el-icon> Message Seller
          </button>
        </div>

        <!-- Wishlist & Share -->
        <div style="display:flex;gap:16px;margin-top:16px">
          <el-button text @click="handleWishlist">
            {{ isWishlisted ? '❤️ Saved' : '🤍 Wishlist' }}
          </el-button>
          <el-button text @click="handleShare">📤 Share</el-button>
        </div>
      </div>

      <!-- Sidebar -->
      <div>
        <!-- Store/Seller Info -->
        <div style="background:white;border-radius:var(--radius-lg);padding:20px;margin-bottom:16px;border:1px solid var(--border-light)">
          <div style="display:flex;align-items:center;gap:12px;margin-bottom:12px">
            <el-avatar :size="44">🏪</el-avatar>
            <div>
              <div style="font-weight:600">{{ product.brand || 'VeriCart Store' }}</div>
              <div style="font-size:var(--font-xs);color:var(--text-muted)">Official Store</div>
            </div>
          </div>
          <div style="display:flex;gap:8px;flex-wrap:wrap">
            <span style="font-size:var(--font-xs);background:#F5F5F5;padding:4px 10px;border-radius:12px">Verified</span>
            <span style="font-size:var(--font-xs);background:#F5F5F5;padding:4px 10px;border-radius:12px">Fast Delivery</span>
          </div>
        </div>

        <!-- Trust Score Sidebar -->
        <div style="background:white;border-radius:var(--radius-lg);padding:20px;border:1px solid var(--border-light);margin-bottom:16px" v-if="product.trustScore">
          <h4 style="font-weight:700;margin-bottom:12px;font-size:var(--font-sm);text-transform:uppercase;color:var(--text-muted)">Trust Score</h4>
          <div :class="['trust-badge-large', trustClass]" style="padding:20px">
            <span class="score">{{ product.trustScore }}</span>
            <span class="label">/ 100 — {{ product.trustLevel || 'Not Rated' }}</span>
          </div>
          <div style="margin-top:12px;font-size:var(--font-xs);color:var(--text-secondary)">
            <p v-if="product.fakeReviewCount > 0" style="color:var(--danger)">
              ⚠️ {{ product.fakeReviewCount }} suspicious review(s)
            </p>
            <p v-else style="color:var(--success)">✅ Clean review history</p>
          </div>
        </div>
      </div>
    </div>

    <!-- AI Analysis Section -->
    <div style="margin:32px 0;display:grid;grid-template-columns:1fr 1fr;gap:24px">
      <div class="ai-card" v-if="product?.trustScore">
        <h3>🛡️ Trust Analysis</h3>
        <div style="display:flex;align-items:center;gap:24px;flex-wrap:wrap">
          <div :class="['trust-badge-large', trustClass]" style="padding:20px 32px">
            <span class="score">{{ product.trustScore }}</span>
            <span class="label">/ 100 — {{ product.trustLevel || 'Not Rated' }}</span>
          </div>
          <div style="flex:1;min-width:200px">
            <p v-if="product.fakeReviewCount > 0" style="color:var(--danger);margin-bottom:6px">
              ⚠️ {{ product.fakeReviewCount }} suspicious review(s) detected
            </p>
            <p v-else style="color:var(--success);margin-bottom:6px">✅ No suspicious reviews detected</p>
            <el-button size="small" type="primary" text @click="analyzeProduct" :loading="analyzing">
              🔄 Refresh Analysis
            </el-button>
          </div>
        </div>
      </div>

      <div class="ai-card" v-if="product?.aiSummary">
        <h3>🤖 AI Review Summary</h3>
        <div v-html="renderedSummary" style="line-height:1.8;font-size:var(--font-sm);color:var(--text-secondary)"></div>
      </div>
    </div>

    <!-- Trusted Rating: raw vs AI-adjusted -->
    <div class="ai-card" v-if="trustMetrics && trustMetrics.totalReviews > 0" style="margin-bottom:24px">
      <h3>⚖️ Trusted Rating</h3>
      <p style="font-size:var(--font-xs);color:var(--text-muted);margin-bottom:16px">
        AI removes suspicious reviews, then recalculates the honest score.
      </p>
      <div class="trusted-rating-row">
        <div class="rating-box">
          <span class="rating-box-label">Raw rating</span>
          <span class="rating-box-value muted">{{ trustMetrics.rawRating?.toFixed(1) }}</span>
          <span class="rating-box-sub">{{ trustMetrics.totalReviews }} reviews</span>
        </div>
        <span class="rating-arrow">→</span>
        <div class="rating-box highlight">
          <span class="rating-box-label">Trusted rating</span>
          <span class="rating-box-value">{{ trustMetrics.trustedRating?.toFixed(1) }}</span>
          <span class="rating-box-sub">
            {{ trustMetrics.flaggedReviews }} suspicious removed
          </span>
        </div>
        <div v-if="trustMetrics.ratingDelta > 0.05" class="rating-delta warn">
          ↓ {{ trustMetrics.ratingDelta?.toFixed(2) }} after removing fake reviews
        </div>
        <div v-else class="rating-delta ok">✓ No inflation detected</div>
      </div>
    </div>

    <!-- Explainable Trust Score (FR-045) -->
    <div style="margin-bottom:24px">
      <TrustScoreExplainer :factors="trustExplanation" />
    </div>

    <!-- Topic extraction (FR-040) -->
    <div style="margin-bottom:32px">
      <TopicSentimentChart :topics="topics" />
    </div>

    <!-- Ask AI -->
    <div class="ai-card" style="margin-bottom:32px">
      <div style="display:flex;align-items:center;justify-content:space-between">
        <h3 style="margin:0">💬 Ask AI About This Product</h3>
        <el-button type="primary" size="small" @click="showProductChat = !showProductChat">
          {{ showProductChat ? 'Hide Chat' : 'Ask Question' }}
        </el-button>
      </div>
      <ChatPanel
        v-if="showProductChat"
        :productId="product?.id"
        style="margin-top:16px;border:1px solid var(--border);border-radius:12px;overflow:hidden"
      />
    </div>

    <!-- Customer Reviews -->
    <div class="reviews-head">
      <h3>Customer Reviews ({{ reviews.length }})</h3>
      <div class="reviews-head-actions">
        <el-button size="small" @click="toggleCompare">
          {{ compareSelected ? '✓ Added to compare' : '⚖️ Compare' }}
        </el-button>
        <el-button
          v-if="auth.isLoggedIn && !myReview && !showReviewForm"
          type="primary"
          size="small"
          @click="showReviewForm = true"
        >
          ✍️ Write a Review
        </el-button>
      </div>
    </div>

    <!-- Review submission / edit -->
    <div v-if="auth.isLoggedIn && showReviewForm" style="margin-bottom:24px">
      <ReviewForm
        :product-id="route.params.id"
        :existing="myReview"
        :show-cancel="true"
        @submitted="onReviewSubmitted"
        @cancel="showReviewForm = false"
      />
    </div>
    <div v-else-if="!auth.isLoggedIn" class="login-prompt">
      Please <router-link to="/login">log in</router-link> to write a review.
    </div>
    <div v-else-if="myReview && !showReviewForm" class="login-prompt">
      ✅ You reviewed this product.
      <el-button text type="primary" size="small" @click="showReviewForm = true">Edit</el-button>
    </div>

    <div v-if="reviews.length">
      <div v-for="r in reviews" :key="r.id"
        :class="['review-card', { 'review-flagged': r.isFlagged }]">
        <div class="review-header">
          <el-avatar :size="36">{{ r.username?.[0]?.toUpperCase() }}</el-avatar>
          <span class="name">{{ r.username }}</span>
          <span v-if="r.verified" class="verified-badge" title="Linked to a confirmed order">
            ✓ Verified Purchase
          </span>
          <StarRating :rating="r.rating" :size="14" />
          <span v-if="r.sentiment" :style="{ color: sentimentColor(r.sentiment), fontSize:'12px', fontWeight:600 }">
            {{ r.sentiment }}
          </span>
          <span v-if="r.emotion" class="emotion-chip">{{ r.emotion }}</span>

          <!-- Per-review AI verdict (explainable AI) -->
          <span :class="['ai-verdict', r.isFlagged ? 'verdict-bad' : 'verdict-good']" :title="aiVerdictTitle(r)">
            {{ aiVerdictLabel(r) }}
          </span>

          <span style="margin-left:auto;display:flex;gap:8px;align-items:center">
            <el-button
              v-if="auth.isLoggedIn && r.userId !== auth.userId"
              text
              size="small"
              @click="reportReview(r.id)"
            >🚩 Report</el-button>
            <el-button
              v-if="auth.isLoggedIn && r.userId === auth.userId"
              text
              size="small"
              type="danger"
              @click="deleteReview(r.id)"
            >Delete</el-button>
          </span>
        </div>

        <p style="margin-top:8px;color:var(--text);line-height:1.6">{{ r.content }}</p>

        <div v-if="r.fakeReason" class="fake-reason">
          <strong>AI Detection:</strong> {{ r.fakeReason }}
        </div>

        <div class="review-foot">
          <span>{{ new Date(r.createdAt).toLocaleDateString() }}</span>
          <span v-if="r.fakeProbability != null">
            Fake probability: {{ Math.round(r.fakeProbability) }}%
          </span>
        </div>
      </div>
    </div>
    <div v-else class="empty-state">
      <p style="font-size:36px;margin-bottom:8px">📝</p>
      <p style="font-weight:600;color:var(--text)">No reviews yet</p>
      <p style="margin-top:4px">Be the first to review this product</p>
    </div>

    <!-- Related Products -->
    <div v-if="relatedProducts.length" style="margin-top:48px">
      <h3 style="font-size:20px;font-weight:700;margin-bottom:16px">You May Also Like</h3>
      <div class="product-grid">
        <ProductCard v-for="p in relatedProducts" :key="p.id" :product="p" />
      </div>
    </div>

    <!-- Message Seller dialog -->
    <el-dialog v-model="msgDialogVisible" width="540px" class="msg-dialog" align-center>
      <template #header>
        <div class="msg-dialog-head">
          <span class="msg-dialog-avatar"><el-icon><MagicStick /></el-icon></span>
          <div class="msg-dialog-titles">
            <h3>Message the store</h3>
            <p>The AI assistant replies instantly — the owner can follow up personally.</p>
          </div>
        </div>
      </template>

      <p class="msg-dialog-about">
        Asking about <strong>{{ product?.name }}</strong>
      </p>

      <!-- Quick question templates -->
      <p class="msg-dialog-label">Quick questions</p>
      <div class="msg-quick-list">
        <button
          v-for="q in quickQuestions"
          :key="q"
          class="msg-quick-chip"
          :class="{ active: message === q }"
          @click="message = message === q ? '' : q"
        >{{ q }}</button>
      </div>

      <el-input
        v-model="message"
        type="textarea"
        :rows="4"
        maxlength="2000"
        show-word-limit
        placeholder="Ask about stock, shipping, price, authenticity, returns…"
      />
      <p class="msg-dialog-note">
        <el-icon><InfoFilled /></el-icon>
        You'll be taken to the chat so you can watch the AI answer right away.
      </p>

      <template #footer>
        <el-button @click="msgDialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="sendingMsg" :disabled="!message.trim()" @click="sendMessage">
          <el-icon style="margin-right:6px"><Promotion /></el-icon> Send
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi, reviewApi, aiApi, wishlistApi, messageApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { marked } from 'marked'
import StarRating from '@/components/StarRating.vue'
import ChatPanel from '@/components/ChatPanel.vue'
import ReviewForm from '@/components/ReviewForm.vue'
import TrustScoreExplainer from '@/components/TrustScoreExplainer.vue'
import TopicSentimentChart from '@/components/TopicSentimentChart.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCompareStore } from '@/stores/compare'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const cartStore = useCartStore()

const product = ref(null)
const reviews = ref([])
const categories = ref([])
const relatedProducts = ref([])
const selectedImage = ref(0)
const quantity = ref(1)
const analyzing = ref(false)
const showProductChat = ref(false)
const isWishlisted = ref(false)

// Message Seller state
const msgDialogVisible = ref(false)
const message = ref('')
const sendingMsg = ref(false)
const quickQuestions = [
  'Is this in stock?',
  'How long is shipping?',
  'Can you negotiate the price?',
  'Is this 100% authentic?',
  'Do you offer bulk discounts?'
]

// Explainable-AI state
const trustMetrics = ref(null)
const trustExplanation = ref([])
const topics = ref([])
const myReview = ref(null)
const showReviewForm = ref(false)

const compareStore = useCompareStore()
const compareSelected = computed(() => compareStore.isSelected(Number(route.params.id)))

const imageList = computed(() => {
  if (!product.value?.images) return []
  try {
    const imgs = JSON.parse(product.value.images)
    return Array.isArray(imgs) ? imgs : [imgs]
  } catch { return [product.value.images] }
})

const mainImage = computed(() =>
  imageList.value[selectedImage.value] || 'https://placehold.co/600x600/F8F8F8/CCC?text=No+Image'
)

const categoryName = computed(() => {
  const cat = categories.value.find(c => c.id === product.value?.categoryId)
  return cat?.name || ''
})

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
    const [prod, rev, cat] = await Promise.all([
      productApi.detail(route.params.id),
      reviewApi.byProduct(route.params.id),
      productApi.categories()
    ])
    product.value = prod.data
    reviews.value = rev.data || []
    categories.value = cat.data || []

    // Fetch related products
    if (prod.data?.categoryId) {
      const rel = await productApi.list({ categoryId: prod.data.categoryId, limit: 6 })
      relatedProducts.value = (rel.data || []).filter(p => p.id != prod.data.id).slice(0, 4)
    }

    // Check wishlist
    if (auth.isLoggedIn) {
      try {
        const check = await wishlistApi.check(prod.data.id)
        isWishlisted.value = check.data?.inWishlist || false
      } catch (e) { /* ok */ }
    }

    await loadTrustData()
  } catch (e) { /* handled */ }
})

/** Loads trusted-rating metrics + the signed-in user's own review for this product. */
async function loadTrustData() {
  const pid = route.params.id
  try {
    const tm = await reviewApi.trustMetrics(pid)
    trustMetrics.value = tm.data || null
  } catch (e) { trustMetrics.value = null }

  if (auth.isLoggedIn) {
    try {
      const mine = await reviewApi.hasReviewed(pid)
      myReview.value = mine.data?.review || null
    } catch (e) { myReview.value = null }
  }
}

function parseSpecs(specs) {
  try {
    return typeof specs === 'string' ? JSON.parse(specs) : specs
  } catch { return {} }
}

async function handleAddCart() {
  if (!auth.isLoggedIn) return ElMessage.warning('Please login first')
  try {
    await cartStore.addToCart(product.value.id, quantity.value)
    ElMessage.success('Added to cart!')
  } catch (e) { ElMessage.error('Failed to add to cart') }
}

async function handleBuyNow() {
  if (!auth.isLoggedIn) return ElMessage.warning('Please login first')
  await handleAddCart()
  router.push('/cart')
}

function openMessageDialog() {
  if (!auth.isLoggedIn) return ElMessage.warning('Please login first to message the seller')
  message.value = ''
  msgDialogVisible.value = true
}

async function sendMessage() {
  if (!message.value.trim()) return
  if (!product.value) return
  sendingMsg.value = true
  try {
    const res = await messageApi.send({ productId: product.value.id, message: message.value.trim() })
    msgDialogVisible.value = false
    message.value = ''
    ElMessage.success('Message sent — the AI assistant is answering now.')
    // Jump into the chat so the customer watches the reply arrive live.
    const newId = res?.data?.id
    router.push(newId ? `/messages?thread=${newId}` : '/messages')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || 'Could not send message')
  } finally {
    sendingMsg.value = false
  }
}

async function handleWishlist() {
  if (!auth.isLoggedIn) return ElMessage.warning('Please login first')
  try {
    if (isWishlisted.value) {
      await wishlistApi.remove(product.value.id)
      isWishlisted.value = false
      ElMessage.success('Removed from wishlist')
    } else {
      await wishlistApi.add(product.value.id)
      isWishlisted.value = true
      ElMessage.success('Added to wishlist!')
    }
  } catch (e) { ElMessage.error('Failed to update wishlist') }
}

function handleShare() {
  const url = window.location.href
  navigator.clipboard?.writeText(url)
  ElMessage.success('Product link copied!')
}

async function analyzeProduct() {
  analyzing.value = true
  try {
    const res = await aiApi.analyze(product.value.id)
    // Capture the explainable-AI breakdown returned by the backend
    if (res.data) {
      trustExplanation.value = res.data.explanation || []
      topics.value = res.data.topics || []
    }
    ElMessage.success('AI analysis complete!')
    const prod = await productApi.detail(route.params.id)
    product.value = prod.data
    await loadTrustData()
  } catch (e) { ElMessage.error('AI analysis failed') }
  finally { analyzing.value = false }
}

/** Refresh reviews + trust data after a review is created/edited. */
async function onReviewSubmitted() {
  showReviewForm.value = false
  const rev = await reviewApi.byProduct(route.params.id)
  reviews.value = rev.data || []
  await loadTrustData()
}

async function reportReview(id) {
  try {
    await ElMessageBox.confirm('Report this review as suspicious?', 'Report Review', {
      confirmButtonText: 'Report',
      cancelButtonText: 'Cancel',
      type: 'warning'
    })
    await reviewApi.report(id)
    ElMessage.success('Review reported for moderation')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('Could not report review')
  }
}

async function deleteReview(id) {
  try {
    await ElMessageBox.confirm('Delete your review?', 'Delete Review', {
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
      type: 'warning'
    })
    await reviewApi.delete(id)
    ElMessage.success('Review deleted')
    reviews.value = reviews.value.filter(r => r.id !== id)
    await loadTrustData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('Could not delete review')
  }
}

function toggleCompare() {
  if (!product.value) return
  if (compareSelected.value) {
    compareStore.remove(product.value.id)
    ElMessage.success('Removed from comparison')
  } else {
    const ok = compareStore.add(product.value)
    if (ok) {
      ElMessage.success(
        `Added to comparison (${compareStore.count}/4). Visit Compare to see the AI verdict.`
      )
    } else {
      ElMessage.warning('You can compare up to 4 products')
    }
  }
}

/** Per-review explainable AI verdict badge. */
function aiVerdictLabel(r) {
  if (r.isFlagged) return '⚠️ Suspicious'
  const p = r.fakeProbability != null ? Number(r.fakeProbability) : null
  if (p == null) return '⏳ Pending AI'
  if (p >= 30) return '🟡 Uncertain'
  return '✓ Likely genuine'
}

function aiVerdictTitle(r) {
  if (r.fakeReason) return r.fakeReason
  const p = r.fakeProbability != null ? Number(r.fakeProbability) : null
  if (p == null) return 'This review has not been analysed by AI yet'
  return `AI estimates a ${Math.round(p)}% probability that this review is fake`
}

function sentimentColor(s) {
  return s === 'POSITIVE' ? 'var(--success)' : s === 'NEGATIVE' ? 'var(--danger)' : 'var(--text-secondary)'
}
</script>

<style scoped>
.reviews-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.reviews-head h3 {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
  color: var(--text, #303133);
}

.reviews-head-actions {
  display: flex;
  gap: 8px;
}

.login-prompt {
  background: #f5f7fa;
  border: 1px dashed var(--border, #dcdfe6);
  border-radius: var(--radius-lg, 12px);
  padding: 14px 18px;
  font-size: 14px;
  color: var(--text-secondary, #606266);
  margin-bottom: 24px;
}

.verified-badge {
  font-size: 11px;
  font-weight: 700;
  color: #16a34a;
  background: #ecfdf5;
  border: 1px solid #a7f3d0;
  padding: 2px 8px;
  border-radius: 10px;
  white-space: nowrap;
}

.emotion-chip {
  font-size: 11px;
  color: var(--text-secondary, #606266);
  background: #f5f7fa;
  padding: 2px 8px;
  border-radius: 10px;
}

.ai-verdict {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 10px;
  cursor: help;
  white-space: nowrap;
}

.verdict-good {
  color: #16a34a;
  background: #ecfdf5;
}

.verdict-bad {
  color: #dc2626;
  background: #fef2f2;
}

.fake-reason {
  margin-top: 8px;
  font-size: 12px;
  color: var(--danger, #ef4444);
  background: #fff0f0;
  padding: 6px 12px;
  border-radius: 6px;
}

.review-foot {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  font-size: 12px;
  color: var(--text-muted, #9ca3af);
  margin-top: 8px;
}

/* Trusted rating comparison */
.trusted-rating-row {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.rating-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 20px;
  border-radius: 10px;
  background: #fafafa;
  border: 1px solid var(--border-light, #ebeef5);
  min-width: 120px;
}

.rating-box.highlight {
  background: #ecfdf5;
  border-color: #a7f3d0;
}

.rating-box-label {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-muted, #9ca3af);
  font-weight: 600;
}

.rating-box-value {
  font-size: 26px;
  font-weight: 800;
  color: var(--text, #303133);
  line-height: 1.2;
}

.rating-box-value.muted {
  color: var(--text-muted, #9ca3af);
}

.rating-box-sub {
  font-size: 11px;
  color: var(--text-secondary, #606266);
}

.rating-arrow {
  font-size: 20px;
  color: var(--text-muted, #9ca3af);
}

.rating-delta {
  font-size: 13px;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 8px;
}

.rating-delta.warn {
  color: #b45309;
  background: #fffbeb;
}

.rating-delta.ok {
  color: #16a34a;
  background: #ecfdf5;
}

/* Message Seller dialog */
.msg-dialog-head {
  display: flex;
  align-items: center;
  gap: 12px;
}
.msg-dialog-avatar {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: 12px;
  background: linear-gradient(135deg, #FF7A5A, #F03524);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}
.msg-dialog-titles h3 {
  font-size: 17px;
  font-weight: 700;
  color: var(--ink, #0F172A);
  letter-spacing: -0.02em;
}
.msg-dialog-titles p {
  font-size: 12px;
  color: var(--text-muted, #94A3B8);
  margin-top: 2px;
}

.msg-dialog-about {
  font-size: 13px;
  color: var(--text-secondary, #475569);
  margin-bottom: 18px;
}
.msg-dialog-label {
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-muted, #94A3B8);
  margin-bottom: 9px;
}

.msg-quick-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;
}

.msg-quick-chip {
  font-size: 12px;
  font-weight: 600;
  padding: 7px 14px;
  border-radius: 999px;
  border: 1px solid var(--border, #E5E8EF);
  background: #fff;
  color: var(--text-secondary, #475569);
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}

.msg-quick-chip:hover {
  border-color: var(--primary, #FF4D3D);
  background: var(--primary-light, #FFF1EE);
  color: var(--primary-dark, #D92B1C);
}

.msg-quick-chip.active {
  border-color: var(--primary, #FF4D3D);
  background: var(--primary-light, #FFF1EE);
  color: var(--primary-dark, #D92B1C);
  font-weight: 700;
}

.msg-dialog-note {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
  font-size: 11px;
  color: var(--text-muted, #94A3B8);
}
</style>
