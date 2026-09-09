<template>
  <div class="page-container">
    <div v-if="loading" class="pd-state">Loading product…</div>
    <div v-else-if="!product" class="pd-state">Product not found.</div>
    <div class="product-detail-layout" v-else>
      <!-- Gallery -->
      <div class="product-gallery">
        <img v-if="galleryTab === 'gallery'" :src="mainImage" :alt="product.name" class="main-image" />
        <div v-else class="mini-specs">
          <div v-for="(val, key) in productSpecs" :key="key" class="mini-spec-row">
            <span class="mini-spec-key">{{ key }}</span>
            <span class="mini-spec-val">{{ val }}</span>
          </div>
          <p v-if="!Object.keys(productSpecs).length" class="mini-spec-empty">No specifications available</p>
        </div>

        <div class="gallery-tabs">
          <button :class="['gtab', { active: galleryTab === 'gallery' }]" @click="galleryTab = 'gallery'">Gallery</button>
          <button :class="['gtab', { active: galleryTab === 'params' }]" @click="galleryTab = 'params'">Parameters</button>
        </div>

        <div class="thumb-list" v-if="galleryTab === 'gallery' && imageList.length > 1">
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
            <span class="current-price">¥{{ product.price }}</span>
            <span class="original-price" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
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
        <div class="pd-qty">
          <span class="pd-qty-label">Quantity:</span>
          <div class="pd-qty-stepper">
            <button type="button" class="pd-qty-btn" :disabled="quantity <= 1" @click="quantity = Math.max(1, quantity - 1)" aria-label="Decrease">−</button>
            <input type="number" class="pd-qty-input" v-model.number="quantity" :min="1" :max="maxQty" @blur="quantity = Math.min(Math.max(1, quantity || 1), maxQty)" />
            <button type="button" class="pd-qty-btn" :disabled="quantity >= maxQty" @click="quantity = Math.min(maxQty, quantity + 1)" aria-label="Increase">+</button>
          </div>
          <span class="pd-qty-avail">({{ product.stock || 0 }} available)</span>
        </div>

        <!-- Variants -->
        <div v-if="variantGroups.length" class="pd-variants">
          <div v-for="g in variantGroups" :key="g.name" class="pd-vgroup">
            <span class="pd-vname">{{ g.name }}</span>
            <div class="pd-vopts">
              <button
                v-for="opt in g.options"
                :key="opt"
                type="button"
                :class="['pd-vchip', { active: selectedVariants[g.name] === opt }]"
                @click="selectedVariants[g.name] = opt"
              >{{ opt }}</button>
            </div>
          </div>
          <div class="pd-vselected" v-if="variantSummary">Selected: {{ variantSummary }}</div>
        </div>

        <!-- Actions -->
        <div class="product-actions">
          <button class="buy-now-btn" @click="handleBuyNow" :disabled="!product.stock">
            Buy Now
          </button>
          <button class="add-cart-btn" @click="handleAddCart" :disabled="!product.stock">
            🛒 Add to Cart
          </button>
          <div class="action-secondary">
            <button class="msg-btn" @click="openMessageDialog">
              <el-icon><ChatDotRound /></el-icon> Message Seller
            </button>
            <button
              v-if="product?.externalUrl"
              class="msg-btn"
              @click="openTaobao"
              style="background:linear-gradient(135deg,#ff5000,#ff2d00);color:#fff;border:none"
            >
              <el-icon><Link /></el-icon> View on Taobao
            </button>
            <button class="action-pill" @click="handleWishlist">
              <span class="action-pill-icon">{{ isWishlisted ? '❤️' : '🤍' }}</span>
              {{ isWishlisted ? 'Saved' : 'Wishlist' }}
            </button>
            <button class="action-pill" @click="handleShare">
              <span class="action-pill-icon">📤</span>
              Share
            </button>
          </div>
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
          <div class="seller-pills">
            <span class="seller-pill seller-pill--ok">Verified</span>
            <span class="seller-pill">Fast Delivery</span>
          </div>
        </div>

        <!-- Ask AI About This Product (in sidebar, under the store card) -->
        <div class="sidebar-card ask-ai-sidebar-card">
          <div class="ask-ai-head">
            <span class="ask-ai-icon" aria-hidden="true">💬</span>
            <div>
              <div class="ask-ai-title">Ask AI About This Product</div>
              <div class="ask-ai-sub">Get instant answers, powered by AI</div>
            </div>
          </div>
          <button class="buy-now-btn ask-ai-sidebar-btn" @click="showProductChat = !showProductChat">
            {{ showProductChat ? 'Hide Chat' : 'Ask Question' }}
          </button>
        </div>

        <!-- Compact floating AI chat popup (rendered in-place with position:fixed; Teleport was unreliable across builds) -->
        <div v-if="showProductChat" class="ask-ai-popup" role="dialog" aria-label="Ask AI about this product">
          <ChatPanel
            :productId="product?.id"
            :productName="product?.name"
            @close="showProductChat = false"
          />
        </div>

        </div>
    </div>

    <!-- Trust Analysis & Score: combined into one card -->
    <!-- Trust Analysis & Score + Trusted Rating: ONE card, two parts -->
    <div class="ai-card trust-combined-card" v-if="product?.trustScore" style="margin-bottom:24px">
      <h3>🛡️ Trust Analysis & Rating</h3>
      <div class="trust-combined-body">
        <!-- LEFT: Trust Analysis & Score -->
        <div class="trust-combined-left">
          <div class="trust-card-body">
            <div :class="['trust-score-gauge', trustClass]">
              <svg viewBox="0 0 120 120" class="gauge-svg" aria-hidden="true">
                <circle cx="60" cy="60" r="50" class="gauge-track" />
                <circle
                  cx="60" cy="60" r="50"
                  class="gauge-fill"
                  :stroke-dasharray="314.159"
                  :stroke-dashoffset="314.159 * (1 - product.trustScore / 100)"
                />
              </svg>
              <div class="gauge-text">
                <span class="gauge-value">{{ product.trustScore }}</span>
                <span class="gauge-label">/ 100</span>
              </div>
            </div>

            <div class="trust-card-info">
              <div class="trust-card-head">
                <div :class="['trust-score-level', trustClass]">
                  {{ product.trustLevel || 'Not Rated' }}
                </div>
                <p v-if="product.fakeReviewCount > 0" style="color:var(--danger);margin:0 0 8px">
                  ⚠️ {{ product.fakeReviewCount }} suspicious review(s) detected
                </p>
                <p v-else style="color:var(--success);margin:0 0 8px">✅ No suspicious reviews detected</p>
              </div>

              <ul class="trust-score-factors">
                <li>
                  <span class="factor-dot ok"></span>
                  Average rating
                  <strong>{{ product.rating?.toFixed(1) || 'N/A' }}</strong>
                </li>
                <li>
                  <span class="factor-dot ok"></span>
                  Total reviews
                  <strong>{{ product.reviewCount }}</strong>
                </li>
                <li :class="{ warn: product.fakeReviewCount > 0 }">
                  <span class="factor-dot"></span>
                  Suspicious reviews
                  <strong>{{ product.fakeReviewCount || 0 }}</strong>
                </li>
                <li>
                  <span class="factor-dot ok"></span>
                  Verified-purchase protection
                  <strong>Enabled</strong>
                </li>
              </ul>

              <p class="trust-score-note">
                Score is calculated from review authenticity, average rating, and seller history.
              </p>

              <el-button size="small" type="primary" text @click="analyzeProduct" :loading="analyzing">
                🔄 Refresh Analysis
              </el-button>
            </div>
          </div>
        </div>

        <!-- RIGHT: Trusted Rating (raw vs AI-adjusted) -->
        <div class="trust-combined-right" v-if="trustMetrics && trustMetrics.totalReviews > 0">
          <h4 class="trust-combined-right-title">⚖️ Trusted Rating</h4>
          <p class="trust-combined-right-desc">
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
      </div>
    </div>

    <!-- AI Review Summary (full-width, only when present) -->
    <div class="ai-card" v-if="product?.aiSummary" style="margin-bottom:24px">
      <h3>🤖 AI Review Summary</h3>
      <div v-html="renderedSummary" style="line-height:1.8;font-size:var(--font-sm);color:var(--text-secondary)"></div>
    </div>

    <!-- Explainable Trust Score (FR-045) -->
    <div style="margin-bottom:24px">
      <TrustScoreExplainer :factors="trustExplanation" />
    </div>

    <!-- Topic extraction (FR-040) -->
    <div style="margin-bottom:32px">
      <TopicSentimentChart :topics="topics" />
    </div>

    <!-- Ask AI is now in the Trust Analysis row above -->

    <!-- Taobao-style detail tabs -->
    <div class="detail-tabs" v-if="product">
      <div class="detail-tabbar">
        <button :class="['dtab', { active: contentTab === 'reviews' }]" @click="contentTab = 'reviews'">
          Customer Reviews ({{ reviews.length }})
        </button>
        <button :class="['dtab', { active: contentTab === 'specs' }]" @click="contentTab = 'specs'">Specifications</button>
        <button :class="['dtab', { active: contentTab === 'details' }]" @click="contentTab = 'details'">Details</button>
        <button :class="['dtab', { active: contentTab === 'store' }]" @click="contentTab = 'store'">Store Picks</button>
        <button :class="['dtab', { active: contentTab === 'also' }]" @click="contentTab = 'also'">Also Viewed</button>
      </div>

      <!-- Reviews panel -->
      <div v-show="contentTab === 'reviews'" class="dtab-panel">
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
      </div><!-- /reviews panel -->

      <!-- Specifications -->
      <div v-show="contentTab === 'specs'" class="dtab-panel">
        <h3 class="dtab-title">Specifications</h3>
        <div class="spec-table">
          <div v-for="(val, key) in productSpecs" :key="key" class="spec-row">
            <span class="spec-key">{{ key }}</span>
            <span class="spec-val">{{ val }}</span>
          </div>
          <p v-if="!Object.keys(productSpecs).length" class="empty-text">No specifications available</p>
        </div>
      </div>

      <!-- Details -->
      <div v-show="contentTab === 'details'" class="dtab-panel">
        <h3 class="dtab-title">Product Details</h3>
        <div v-if="product.description" class="detail-block">
          <h4>Description</h4>
          <p class="detail-desc">{{ product.description }}</p>
        </div>
        <div v-if="product.aiSummary" class="detail-block">
          <h4>AI Review Summary</h4>
          <div v-html="renderedSummary" class="detail-ai"></div>
        </div>
        <p v-if="!product.description && !product.aiSummary" class="empty-text">No details available</p>
      </div>

      <!-- Store Picks -->
      <div v-show="contentTab === 'store'" class="dtab-panel">
        <h3 class="dtab-title">Store Picks</h3>
        <div class="product-grid">
          <ProductCard v-for="p in storeProducts" :key="p.id" :product="p" />
        </div>
        <p v-if="!storeProducts.length" class="empty-text">No recommendations available</p>
      </div>

      <!-- Also Viewed -->
      <div v-show="contentTab === 'also'" class="dtab-panel">
        <h3 class="dtab-title">Also Viewed</h3>
        <div class="product-grid">
          <ProductCard v-for="p in relatedProducts" :key="p.id" :product="p" />
        </div>
        <p v-if="!relatedProducts.length" class="empty-text">No products found</p>
      </div>
    </div><!-- /detail-tabs -->

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
import { ref, computed, onMounted, watch, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi, reviewApi, aiApi, wishlistApi, messageApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { parseVariants, selectionSummary } from '@/utils/variants'
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
const loading = ref(true)

const variantGroups = computed(() => parseVariants(product.value))
const selectedVariants = reactive({})
watch(() => product.value, (p) => {
  parseVariants(p).forEach(g => {
    if (g.options?.length && !selectedVariants[g.name]) selectedVariants[g.name] = g.options[0]
  })
}, { immediate: true })
const variantSummary = computed(() => selectionSummary(selectedVariants))
const maxQty = computed(() => Math.min(product.value?.stock || 99, 99))
const reviews = ref([])
const categories = ref([])
const relatedProducts = ref([])
const storeProducts = ref([])
const galleryTab = ref('gallery')
const contentTab = ref('reviews')
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

const productSpecs = computed(() => {
  const p = product.value
  if (!p) return {}
  const map = {}
  if (p.brand) map['Brand'] = p.brand
  if (categoryName.value) map['Category'] = categoryName.value
  if (p.price != null) map['Price'] = '¥' + p.price
  if (p.stock != null) map['Stock'] = p.stock
  if (p.rating != null) map['Rating'] = (p.rating?.toFixed?.(1) || p.rating) + ' / 5'
  if (p.reviewCount != null) map['Reviews'] = p.reviewCount
  if (p.trustScore != null) map['Trust Score'] = p.trustScore + '/100 · ' + (p.trustLevel || '')
  if (p.soldCount != null) map['Sold'] = p.soldCount + '+'
  if (p.specs) Object.assign(map, parseSpecs(p.specs))
  return map
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
      const rel = await productApi.list({ categoryId: prod.data.categoryId, limit: 12 })
      const others = (rel.data || []).filter(p => p.id != prod.data.id)
      relatedProducts.value = others.slice(0, 4)
      storeProducts.value = others.slice(0, 8)
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
  finally { loading.value = false }
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

function openTaobao() {
  if (product.value?.externalUrl) window.open(product.value.externalUrl, '_blank', 'noopener')
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
  } catch (e) {
    const isTimeout = e?.code === 'ECONNABORTED' || /timeout/i.test(e?.message || '')
    ElMessage.error(isTimeout
      ? 'AI analysis is taking longer than usual. Please try again — analysis continues in the background.'
      : 'AI analysis failed')
  }
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

/* Product variant / SKU selectors */
.pd-variants {
  margin: 18px 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.pd-vgroup {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}
.pd-vname {
  min-width: 64px;
  font-size: var(--font-sm, 14px);
  font-weight: 600;
  color: var(--text-muted, #64748B);
  padding-top: 6px;
}
.pd-vopts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.pd-vchip {
  border: 1px solid var(--border, #E2E8F0);
  background: #fff;
  color: var(--text, #333);
  border-radius: 6px;
  padding: 6px 14px;
  font-size: var(--font-sm, 14px);
  cursor: pointer;
  transition: all .15s ease;
}
.pd-vchip:hover {
  border-color: var(--primary, #FF4D3D);
  color: var(--primary, #FF4D3D);
}
.pd-vchip.active {
  border-color: var(--primary, #FF4D3D);
  color: var(--primary, #FF4D3D);
  background: var(--primary-light, #FFF1EE);
  font-weight: 700;
}
.pd-vselected {
  font-size: var(--font-sm, 14px);
  color: var(--text-muted, #64748B);
  padding: 8px 12px;
  background: var(--bg-soft, #F8FAFC);
  border-radius: 8px;
}

/* Gallery mini-tabs (Gallery / Parameters) */
.gallery-tabs {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}
.gtab {
  flex: 1;
  padding: 8px 0;
  border: 1px solid var(--border-light, #ebeef5);
  background: #fff;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary, #606266);
  cursor: pointer;
  font-family: inherit;
  transition: all .15s ease;
}
.gtab:hover {
  border-color: var(--primary, #FF4D3D);
  color: var(--primary, #FF4D3D);
}
.gtab.active {
  border-color: var(--primary, #FF4D3D);
  color: #fff;
  background: var(--primary, #FF4D3D);
}
.mini-specs {
  padding: 16px;
  border: 1px solid var(--border-light, #ebeef5);
  border-radius: var(--radius-lg, 12px);
}
.mini-spec-row {
  display: flex;
  font-size: 13px;
  padding: 7px 0;
  border-bottom: 1px dashed var(--border-light, #ebeef5);
}
.mini-spec-key { color: var(--text-muted, #64748B); min-width: 120px; }
.mini-spec-val { color: var(--text, #333); }
.mini-spec-empty { color: var(--text-muted); font-size: 13px; }

/* Detail tab bar (Customer Reviews / Specifications / Details / Store Picks / Also Viewed) */
.detail-tabs { margin-top: 24px; }
.detail-tabbar {
  display: flex;
  gap: 4px;
  border-bottom: 2px solid var(--border-light, #ebeef5);
  position: sticky;
  top: 0;
  background: #fff;
  z-index: 20;
  padding-top: 8px;
  flex-wrap: wrap;
}
.dtab {
  padding: 12px 18px;
  border: none;
  background: none;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-secondary, #606266);
  cursor: pointer;
  border-bottom: 3px solid transparent;
  margin-bottom: -2px;
  font-family: inherit;
}
.dtab:hover { color: var(--primary, #FF4D3D); }
.dtab.active {
  color: var(--primary, #FF4D3D);
  border-bottom-color: var(--primary, #FF4D3D);
}
.dtab-panel { padding-top: 24px; }
.dtab-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 16px;
  color: var(--text, #303133);
}
.spec-table {
  border: 1px solid var(--border-light, #ebeef5);
  border-radius: var(--radius-lg, 12px);
  overflow: hidden;
}
.spec-row {
  display: flex;
  font-size: 14px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-light, #ebeef5);
}
.spec-row:last-child { border-bottom: none; }
.spec-key {
  color: var(--text-muted, #64748B);
  min-width: 160px;
  font-weight: 600;
}
.spec-val { color: var(--text, #333); }
.detail-block { margin-bottom: 24px; }
.detail-block h4 {
  font-size: 15px;
  font-weight: 700;
  margin: 0 0 8px;
  color: var(--text, #303133);
}
.detail-desc {
  line-height: 1.7;
  color: var(--text-secondary, #606266);
  font-size: 14px;
}
.detail-ai { line-height: 1.8; color: var(--text-secondary, #606266); font-size: 14px; }
.empty-text { color: var(--text-muted, #9ca3af); font-size: 14px; padding: 12px 0; }

/* Loading / not-found states */
.pd-state {
  padding: 80px 20px;
  text-align: center;
  font-size: 16px;
  color: var(--text-muted, #9ca3af);
}

/* ============================================================
   Product detail polish — match the example design
   ============================================================ */

/* Stack action buttons vertically, full width (Buy Now / Add to Cart / Message Seller) */
.product-actions {
  /* Keep it a flex row; Buy Now + Add to Cart share one line, Message Seller wraps below. */
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: stretch;
}
.product-actions .buy-now-btn,
.product-actions .add-cart-btn {
  /* Equal-width side-by-side on one row, compact to match the secondary pill row below */
  flex: 1 1 0;
  min-width: 0;
  height: 44px;
  font-size: 14px;
  padding: 0 16px;
}
.product-actions .action-secondary {
  /* Row beneath Buy Now / Add to Cart: Message Seller + Wishlist + Share together */
  flex: 1 1 100%;
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 8px;
}
.product-actions .action-secondary .msg-btn,
.product-actions .action-secondary .action-pill {
  /* Same compact, equal-width pill so all 3 buttons feel consistent and always share one line */
  flex: 1 1 0;
  min-width: 0;
  height: 44px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
  transition: all 0.2s ease;
}
.product-actions .action-secondary .action-pill {
  background: #ffffff;
  color: var(--text, #303133);
  border: 1px solid var(--border-light, #ebeef5);
}
.product-actions .action-secondary .action-pill:hover {
  background: #fff8f5;
  border-color: var(--primary, #FF4D3D);
  color: var(--primary, #FF4D3D);
}
.action-pill-icon {
  font-size: 16px;
  line-height: 1;
}

/* Trust Analysis & Score combined card (two-part: LEFT analysis | RIGHT trusted rating) */
.trust-combined-card {
  padding: 24px 26px;
}
.trust-combined-card h3 {
  font-size: var(--font-xl, 18px);
  margin-bottom: 18px;
}
.trust-combined-body {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
  align-items: stretch;
}
.trust-combined-left {
  flex: 1 1 340px;
  min-width: 300px;
}
.trust-combined-right {
  flex: 1 1 300px;
  min-width: 280px;
  padding-left: 32px;
  border-left: 1px solid var(--border-light, #ebeef5);
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.trust-combined-right-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text, #303133);
  margin: 0 0 6px;
}
.trust-combined-right-desc {
  font-size: var(--font-xs, 12px);
  color: var(--text-muted, #9ca3af);
  margin: 0 0 18px;
}
.trust-card-body {
  display: flex;
  align-items: center;
  gap: 36px;
  flex-wrap: wrap;
}
.trust-card-info {
  flex: 1;
  min-width: 240px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.trust-card-head {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 4px;
}
.trust-card-head .trust-score-level {
  align-self: flex-start;
  margin-bottom: 0;
}
.trust-card-head p { margin: 0; }

/* In the right column the rating compare stacks vertically for clarity */
.trust-combined-right .trusted-rating-row {
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.trust-combined-right .rating-arrow {
  align-self: center;
  transform: rotate(90deg);
  font-size: 18px;
  margin: -2px 0;
}

/* Compact Raw / Trusted rating boxes in the right column */
.trust-combined-right .rating-box {
  flex: 0 0 auto;
  align-self: center;
  width: 100%;
  max-width: 240px;
  padding: 10px 16px;
  border-radius: 10px;
  background: linear-gradient(180deg, #ffffff 0%, #fafafa 100%);
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
  gap: 1px;
}
.trust-combined-right .rating-box.highlight {
  background: linear-gradient(180deg, #f0fdf4 0%, #ecfdf5 100%);
  border-color: #a7f3d0;
  box-shadow: 0 1px 2px rgba(16, 185, 129, 0.08);
}
.trust-combined-right .rating-box-label {
  font-size: 10px;
  letter-spacing: 0.08em;
}
.trust-combined-right .rating-box-value {
  font-size: 24px;
  margin: 2px 0 1px;
  line-height: 1.1;
}
.trust-combined-right .rating-box-sub {
  font-size: 10px;
}

/* Compact verdict pill in right column */
.trust-combined-right .rating-delta {
  font-size: 12px;
  padding: 6px 12px;
  margin-top: 2px;
}

/* Polished card header + nicer background */
.trust-combined-card {
  background: linear-gradient(180deg, #fff8f5 0%, #fff5f0 100%) !important;
  border: 1px solid #fde2d6 !important;
  box-shadow: 0 2px 8px rgba(255, 77, 61, 0.04);
}
.trust-combined-card h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 17px;
  letter-spacing: -0.01em;
}

/* Polished gauge container */
.trust-combined-left .trust-score-gauge {
  width: 132px;
  height: 132px;
  padding: 6px;
  background: linear-gradient(180deg, #ffffff 0%, #fff8f5 100%);
  border-radius: 50%;
  box-shadow: 0 2px 6px rgba(16, 24, 40, 0.05), inset 0 0 0 1px rgba(255, 77, 61, 0.06);
}
.trust-combined-left .trust-score-gauge .gauge-svg { width: 100%; height: 100%; }

/* Ask AI sidebar card (under the Store card) */
.sidebar-card {
  background: white;
  border-radius: var(--radius-lg);
  padding: 20px;
  margin-bottom: 16px;
  border: 1px solid var(--border-light);
}
.ask-ai-head {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}
.ask-ai-icon {
  font-size: 22px;
  line-height: 1;
  width: 40px;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fff7f4, #ffe8e0);
  border-radius: 10px;
  flex-shrink: 0;
}
.ask-ai-title {
  font-weight: 700;
  font-size: 15px;
  color: var(--text);
  line-height: 1.3;
}
.ask-ai-sub {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 3px;
}
.ask-ai-sidebar-btn {
  width: 100%;
  height: 48px;
  font-size: 14px;
}

/* Professional Trust Score card under the AI row */
.trust-card { padding: 24px 26px; }
.trust-card-body {
  display: flex;
  align-items: center;
  gap: 36px;
  flex-wrap: wrap;
}
.trust-score-gauge {
  position: relative;
  width: 140px;
  height: 140px;
  flex-shrink: 0;
}
.gauge-svg {
  transform: rotate(-90deg);
  width: 100%;
  height: 100%;
}
.gauge-track {
  fill: none;
  stroke: #e5e7eb;
  stroke-width: 10;
}
.gauge-fill {
  fill: none;
  stroke-width: 10;
  stroke-linecap: round;
  transition: stroke-dashoffset 0.6s ease;
}
.trust-score-gauge.trust-excellent .gauge-fill { stroke: #10b981; }
.trust-score-gauge.trust-high .gauge-fill { stroke: #84cc16; }
.trust-score-gauge.trust-medium .gauge-fill { stroke: #f59e0b; }
.trust-score-gauge:not(.trust-excellent):not(.trust-high):not(.trust-medium) .gauge-fill { stroke: #ef4444; }
.gauge-text {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}
.gauge-value {
  font-size: 38px;
  font-weight: 800;
  color: var(--text);
  line-height: 1;
}
.gauge-label {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 4px;
}
.trust-score-info {
  flex: 1;
  min-width: 260px;
}
.trust-score-level {
  display: inline-block;
  padding: 4px 14px;
  border-radius: 999px;
  font-weight: 700;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-bottom: 14px;
}
.trust-score-level.trust-excellent { background: #d1fae5; color: #065f46; }
.trust-score-level.trust-high { background: #ecfccb; color: #3f6212; }
.trust-score-level.trust-medium { background: #fef3c7; color: #92400e; }
.trust-score-level:not(.trust-excellent):not(.trust-high):not(.trust-medium) { background: #fee2e2; color: #991b1b; }
.trust-score-factors {
  list-style: none;
  padding: 0;
  margin: 0 0 12px;
}
.trust-score-factors li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px dashed var(--border-light, #eef0f3);
  font-size: var(--font-sm, 14px);
  color: var(--text-secondary);
}
.trust-score-factors li:last-child { border-bottom: none; }
.trust-score-factors li strong {
  margin-left: auto;
  color: var(--text);
  font-weight: 700;
}
.factor-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--success, #10b981);
  flex-shrink: 0;
}
.trust-score-factors li.warn .factor-dot { background: var(--danger, #ef4444); }
.trust-score-factors li.warn strong { color: var(--danger); }
.trust-score-note {
  font-size: 12px;
  color: var(--text-muted);
  margin: 8px 0 0;
}

/* (Old Trust Analysis compact-gauge styles removed — merged into .trust-card above) */

/* Trusted Rating: roomier, clearer verdict */
.trusted-rating-row {
  display: flex;
  align-items: stretch;
  gap: 18px;
  flex-wrap: wrap;
}
.rating-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 18px 28px;
  border-radius: 12px;
  background: #fafafa;
  border: 1px solid var(--border-light, #ebeef5);
  min-width: 160px;
  flex: 1;
}
.rating-box.highlight {
  background: #ecfdf5;
  border-color: #a7f3d0;
}
.rating-box-label {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-muted, #9ca3af);
  font-weight: 700;
}
.rating-box-value {
  font-size: 30px;
  font-weight: 800;
  color: var(--text, #303133);
  line-height: 1.2;
  margin: 4px 0 2px;
}
.rating-box-value.muted {
  color: var(--text-muted, #9ca3af);
}
.rating-box.highlight .rating-box-value {
  color: #047857;
}
.rating-box-sub {
  font-size: 11px;
  color: var(--text-secondary, #606266);
}
.rating-arrow {
  font-size: 24px;
  color: var(--text-muted, #9ca3af);
  align-self: center;
}
.rating-delta {
  font-size: 13px;
  font-weight: 600;
  padding: 8px 14px;
  border-radius: 999px;
  align-self: center;
}

/* Solid border + softer blue for Message Seller (example uses solid outline) */
.msg-btn {
  border-style: solid;
  border-color: #BFDBFE;
}
.msg-btn:hover {
  border-style: solid;
  border-color: #93C5FD;
}

/* Seller card pills (clean, with green "Verified") */

/* Seller card pills (clean, with green "Verified") */
.seller-pills {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.seller-pill {
  font-size: var(--font-xs);
  background: #F1F5F9;
  color: var(--text-secondary, #475569);
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 600;
  border: 1px solid #E2E8F0;
}
.seller-pill--ok {
  background: #ECFDF5;
  color: #067A56;
  border-color: #A7F3D0;
}

/* Clean quantity stepper: [−] [1] [+] (replaces chunky el-input-number) */
.pd-qty {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 20px 0;
  flex-wrap: wrap;
}
.pd-qty-label {
  font-weight: 600;
  font-size: var(--font-sm);
  color: var(--text, #333);
}
.pd-qty-stepper {
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--border, #E2E8F0);
  border-radius: var(--radius-md, 8px);
  overflow: hidden;
  background: #fff;
  box-shadow: var(--shadow-xs, 0 1px 2px rgba(15, 23, 42, 0.04));
}
.pd-qty-btn {
  width: 38px;
  height: 38px;
  border: none;
  background: #fff;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-secondary, #475569);
  cursor: pointer;
  font-family: inherit;
  transition: background .15s, color .15s;
}
.pd-qty-btn:hover:not(:disabled) {
  background: #F8FAFC;
  color: var(--primary, #FF4D3D);
}
.pd-qty-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.pd-qty-input {
  width: 52px;
  height: 38px;
  border: none;
  border-left: 1px solid var(--border-light, #EBEEF5);
  border-right: 1px solid var(--border-light, #EBEEF5);
  text-align: center;
  font-size: 14px;
  font-weight: 700;
  color: var(--text, #333);
  font-family: inherit;
  outline: none;
  background: #fff;
  -moz-appearance: textfield;
}
.pd-qty-input::-webkit-outer-spin-button,
.pd-qty-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
.pd-qty-avail {
  font-size: var(--font-xs);
  color: var(--text-muted, #94A3B8);
}
</style>

<!-- Unscoped styles for the floating Ask AI chat popup.
     Kept global so they apply reliably regardless of <style scoped> data-v attribute behaviour
     (Teleport + scoped CSS was the root cause of the popup being invisible across 3 commits). -->
<style>
.ask-ai-popup {
  position: fixed;
  right: 24px;
  bottom: 96px; /* sits above the global chat bubble (~64-72px) */
  width: 360px;
  max-width: calc(100vw - 32px);
  z-index: 1000;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.18), 0 4px 14px rgba(0, 0, 0, 0.08);
  border: 1px solid #E2E8F0;
  overflow: hidden;
  max-height: calc(100vh - 40px);
  display: flex;
  flex-direction: column;
}
@media (max-width: 640px) {
  .ask-ai-popup {
    right: 12px;
    bottom: 20px;
    width: calc(100vw - 24px);
    max-height: calc(100vh - 40px);
  }
}
</style>
