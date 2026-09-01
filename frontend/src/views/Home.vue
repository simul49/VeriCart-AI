<template>
  <div class="home">
    <!-- ================= Hero ================= -->
    <section class="hero">
      <div class="hero-inner">
        <span class="hero-pill">
          <el-icon><MagicStick /></el-icon> {{ $t('home.heroPill') }}
        </span>
        <h1>
          {{ $t('home.heroTitleA') }}
          <span class="accent">{{ $t('home.heroTitleB') }}</span>
        </h1>
        <p>{{ $t('home.heroSub') }}</p>
        <div class="hero-actions">
          <router-link to="/products" class="btn btn-primary btn-lg">
            <el-icon><Goods /></el-icon> {{ $t('home.startShopping') }}
          </router-link>
          <router-link to="/recommendations" class="btn btn-lg btn-hero-ghost">
            <el-icon><MagicStick /></el-icon> {{ $t('home.getAiPicks') }}
          </router-link>
        </div>
        <div class="hero-stats">
          <div class="hero-stat">
            <div class="value">{{ stats.products }}+</div>
            <div class="label">{{ $t('home.productsVerified') }}</div>
          </div>
          <div class="hero-stat">
            <div class="value">3</div>
            <div class="label">{{ $t('home.modelsAnalysing') }}</div>
          </div>
          <div class="hero-stat">
            <div class="value">{{ stats.avgTrust }}<span class="unit">/100</span></div>
            <div class="label">{{ $t('home.avgTrust') }}</div>
          </div>
          <div class="hero-stat">
            <div class="value">24/7</div>
            <div class="label">{{ $t('home.assistant') }}</div>
          </div>
        </div>
      </div>
    </section>

    <div class="page-container">
      <!-- ================= Why VeriCart ================= -->
      <section class="features">
        <div v-for="f in features" :key="f.titleKey" class="feature-card card card-hover">
          <span class="feature-icon" :style="{ background: f.tint, color: f.color }">
            <el-icon><component :is="f.icon" /></el-icon>
          </span>
          <h3>{{ $t(f.titleKey) }}</h3>
          <p>{{ $t(f.descKey) }}</p>
        </div>
      </section>

      <!-- ================= Categories ================= -->
      <section class="section-head">
        <div>
          <span class="eyebrow"><el-icon><Grid /></el-icon> {{ $t('home.browse') }}</span>
          <h2>{{ $t('home.shopByCategory') }}</h2>
          <p>{{ $t('home.categorySub', { count: categories.length }) }}</p>
        </div>
      </section>
      <div class="category-grid">
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="category-card"
          @click="$router.push(`/products?categoryId=${cat.id}`)"
        >
          <div class="cat-icon">
            <el-icon><component :is="categoryIcon(cat.id)" /></el-icon>
          </div>
          <div class="cat-name">{{ cat.name }}</div>
        </div>
      </div>

      <!-- ================= Flash Deals ================= -->
      <section v-if="flashDeals.length" class="flash-deal-section">
        <div class="flash-deal-header">
          <span class="flash-icon"><el-icon><Lightning /></el-icon></span>
          <span class="flash-title">{{ $t('home.flashDeals') }}</span>
          <div class="countdown">
            <span class="time-block">{{ pad(flashHours) }}</span>
            <span class="time-sep">:</span>
            <span class="time-block">{{ pad(flashMinutes) }}</span>
            <span class="time-sep">:</span>
            <span class="time-block">{{ pad(flashSeconds) }}</span>
          </div>
          <router-link to="/products" class="btn btn-sm btn-ghost see-all">
            {{ $t('common.viewAll') }} <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>
        <div class="flash-deal-products">
          <div v-for="p in flashDeals" :key="p.id" class="product-card" @click="$router.push(`/products/${p.id}`)">
            <div class="img-wrapper">
              <img :src="firstImage(p)" :alt="p.name" loading="lazy" @error="onImgError($event)" />
              <span class="discount-badge">-{{ discountPercent(p) }}%</span>
            </div>
            <div class="info">
              <div class="name">{{ p.name }}</div>
              <div class="price-row">
                <span class="price">${{ p.price }}</span>
                <span class="original-price">${{ p.originalPrice }}</span>
              </div>
              <div class="meta">
                <StarRating :rating="p.rating" :size="12" />
                <span class="sales">{{ $t('home.sold', { count: p.soldCount || 0 }) }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ================= Top Trusted ================= -->
      <section class="section-head">
        <div>
          <span class="eyebrow"><el-icon><CircleCheck /></el-icon> {{ $t('product.trustScore') }}</span>
          <h2>{{ $t('home.topTrusted') }}</h2>
          <p>{{ $t('home.topTrustedSub') }}</p>
        </div>
        <router-link to="/products?sort=trust" class="btn btn-sm btn-outline">
          {{ $t('common.viewAll') }} <el-icon><ArrowRight /></el-icon>
        </router-link>
      </section>
      <div class="product-grid">
        <ProductCard v-for="p in trustedProducts" :key="p.id" :product="p" />
      </div>
      <div v-if="!trustedProducts.length && !loading" class="empty-state">
        <div class="empty-icon"><el-icon><Odometer /></el-icon></div>
        <p style="font-size:17px;font-weight:700;color:var(--ink)">{{ $t('home.noAnalysed') }}</p>
        <p style="margin-top:4px">{{ $t('home.noAnalysedSub') }}</p>
      </div>

      <!-- ================= All Products ================= -->
      <section class="section-head" style="margin-top:48px">
        <div>
          <span class="eyebrow"><el-icon><Goods /></el-icon> {{ $t('home.catalog') }}</span>
          <h2>{{ $t('home.allProducts') }}</h2>
        </div>
        <router-link to="/products" class="btn btn-sm btn-outline">
          {{ $t('home.browseAll') }} <el-icon><ArrowRight /></el-icon>
        </router-link>
      </section>
      <div class="product-grid">
        <ProductCard v-for="p in products" :key="p.id" :product="p" />
      </div>
      <div v-if="!products.length && !loading" class="empty-state">
        <div class="empty-icon"><el-icon><Goods /></el-icon></div>
        <p style="font-size:17px;font-weight:700;color:var(--ink)">{{ $t('home.noProducts') }}</p>
        <p style="margin-top:4px">{{ $t('home.noProductsSub') }}</p>
      </div>

      <!-- ================= CTA band ================= -->
      <section class="cta-band">
        <div class="cta-text">
          <h2>{{ $t('home.ctaTitle') }}</h2>
          <p>{{ $t('home.ctaSub') }}</p>
        </div>
        <div class="cta-actions">
          <router-link to="/recommendations" class="btn btn-primary btn-lg">
            <el-icon><MagicStick /></el-icon> {{ $t('home.askAi') }}
          </router-link>
          <router-link to="/how-it-works" class="btn btn-outline btn-lg">{{ $t('home.howItWorks') }}</router-link>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { productApi } from '@/api'
import ProductCard from '@/components/ProductCard.vue'
import StarRating from '@/components/StarRating.vue'

const products = ref([])
const categories = ref([])
const loading = ref(true)

/* ---------- Hero stats ---------- */
const stats = computed(() => {
  const list = products.value
  const withScore = list.filter(p => p.trustScore)
  const avg = withScore.length
    ? Math.round(withScore.reduce((s, p) => s + p.trustScore, 0) / withScore.length)
    : 0
  return { products: list.length, avgTrust: avg }
})

/* ---------- Value props ---------- */
const features = [
  {
    titleKey: 'home.f1Title', descKey: 'home.f1Desc',
    icon: 'CircleCheck', tint: 'var(--success-light)', color: '#067A56'
  },
  {
    titleKey: 'home.f2Title', descKey: 'home.f2Desc',
    icon: 'Warning', tint: '#FEF4E6', color: '#A96407'
  },
  {
    titleKey: 'home.f3Title', descKey: 'home.f3Desc',
    icon: 'Odometer', tint: '#EAF1FF', color: '#1D4ED8'
  },
  {
    titleKey: 'home.f4Title', descKey: 'home.f4Desc',
    icon: 'Lock', tint: 'var(--primary-light)', color: 'var(--primary-dark)'
  }
]

/* ---------- Flash deals ---------- */
const now = ref(Date.now())
const endOfDay = () => { const d = new Date(); d.setHours(23, 59, 59, 999); return d.getTime() }
const flashRemaining = computed(() => Math.max(0, Math.floor((endOfDay() - now.value) / 1000)))
const flashHours = computed(() => Math.floor(flashRemaining.value / 3600))
const flashMinutes = computed(() => Math.floor((flashRemaining.value % 3600) / 60))
const flashSeconds = computed(() => flashRemaining.value % 60)
const pad = (n) => String(n).padStart(2, '0')

const flashDeals = computed(() =>
  [...products.value]
    .sort((a, b) => (a.price || 9999) - (b.price || 9999))
    .slice(0, 6)
    .map(p => ({ ...p, originalPrice: (p.price * 1.35).toFixed(2) }))
)

/* ---------- Trusted ---------- */
const trustedProducts = computed(() =>
  [...products.value]
    .sort((a, b) => (b.trustScore || 0) - (a.trustScore || 0))
    .slice(0, 8)
)

/* ---------- Helpers ---------- */
const CATEGORY_ICONS = {
  1: 'Monitor', 2: 'Cpu', 3: 'House', 4: 'Reading', 5: 'Football',
  6: 'Iphone', 7: 'Watch', 8: 'Headset', 9: 'Suitcase', 10: 'ShoppingBag'
}
function categoryIcon(id) { return CATEGORY_ICONS[id] || 'Goods' }

function firstImage(p) {
  if (!p?.images) return PLACEHOLDER
  try {
    const imgs = JSON.parse(p.images)
    return Array.isArray(imgs) ? imgs[0] : imgs
  } catch { return p.images }
}
const PLACEHOLDER = 'data:image/svg+xml;utf8,' +
  encodeURIComponent(`<svg xmlns="http://www.w3.org/2000/svg" width="400" height="400">
    <rect width="400" height="400" fill="#F1F3F7"/>
    <text x="50%" y="50%" font-family="sans-serif" font-size="18" fill="#94A3B8"
      text-anchor="middle" dominant-baseline="middle">No image</text></svg>`)

function onImgError(e) { e.target.src = PLACEHOLDER }

function discountPercent(p) {
  if (!p.originalPrice || !p.price) return 0
  return Math.round((1 - p.price / p.originalPrice) * 100)
}

/* ---------- Lifecycle ---------- */
let countdownTimer = null

onMounted(async () => {
  try {
    const [prodRes, catRes] = await Promise.all([productApi.list(), productApi.categories()])
    products.value = prodRes.data || []
    categories.value = (catRes.data || []).slice(0, 12)
  } catch { /* keep empty state */ } finally {
    loading.value = false
  }
  countdownTimer = setInterval(() => { now.value = Date.now() }, 1000)
})

onUnmounted(() => clearInterval(countdownTimer))
</script>

<style scoped>
/* ---------- Hero ---------- */
.hero-inner { max-width: 900px; margin: 0 auto; }

.hero-pill {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 7px 16px;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.18);
  color: rgba(255, 255, 255, 0.9);
  font-size: var(--font-sm);
  font-weight: 600;
  margin-bottom: 26px;
  backdrop-filter: blur(6px);
}
.hero-pill .el-icon { font-size: 15px; color: #FFC15B; }

.hero h1 { margin-bottom: 0; }

.btn-hero-ghost {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.28);
  backdrop-filter: blur(6px);
}
.btn-hero-ghost:hover {
  background: rgba(255, 255, 255, 0.18);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
}
.hero-stat .unit {
  font-size: var(--font-lg);
  font-weight: 600;
  opacity: 0.6;
}

/* ---------- Features ---------- */
.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(230px, 1fr));
  gap: 18px;
  margin-bottom: 48px;
  margin-top: -40px;
  position: relative;
  z-index: 2;
}
.feature-card { padding: 24px 22px; }
.feature-icon {
  width: 46px;
  height: 46px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  margin-bottom: 14px;
}
.feature-card h3 {
  font-size: var(--font-lg);
  font-weight: 700;
  color: var(--ink);
  letter-spacing: -0.01em;
  margin-bottom: 6px;
}
.feature-card p {
  font-size: var(--font-sm);
  color: var(--text-secondary);
  line-height: 1.65;
}

/* ---------- Categories ---------- */
.category-grid { margin-bottom: 48px; }
.category-card { padding: 18px 12px; }
.cat-icon {
  font-size: 24px;
  color: var(--primary);
}
.category-card:hover .cat-icon {
  background: var(--primary);
  color: #fff;
}

/* ---------- Flash deals ---------- */
.see-all { margin-left: auto; }

/* ---------- CTA ---------- */
.cta-band {
  margin-top: 56px;
  padding: 44px 40px;
  border-radius: var(--radius-2xl);
  background:
    radial-gradient(700px 260px at 88% 20%, rgba(255, 77, 61, 0.22), transparent 60%),
    linear-gradient(135deg, #0B1220 0%, #1B2A47 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 28px;
  flex-wrap: wrap;
}
.cta-text h2 {
  font-size: var(--font-3xl);
  font-weight: 800;
  letter-spacing: -0.025em;
  margin-bottom: 8px;
}
.cta-text p {
  color: rgba(255, 255, 255, 0.72);
  font-size: var(--font-md);
  max-width: 460px;
}
.cta-actions { display: flex; gap: 12px; flex-wrap: wrap; }
.cta-actions .btn-outline {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.26);
  color: #fff;
}
.cta-actions .btn-outline:hover {
  background: rgba(255, 255, 255, 0.16);
  border-color: #fff;
  color: #fff;
}

@media (max-width: 768px) {
  .features { margin-top: -28px; gap: 12px; }
  .cta-band { padding: 30px 22px; }
  .cta-text h2 { font-size: var(--font-2xl); }
  .flash-deal-header .see-all { margin-left: 0; }
}
</style>
