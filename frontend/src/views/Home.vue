<template>
  <div class="home">
    <!-- ================= Hero ================= -->
    <section class="hero hero-v2">
      <div class="hero-v2-grid">
        <!-- Left: copy -->
        <div class="hero-v2-text">
          <span class="hero-pill">
            <el-icon><MagicStick /></el-icon> {{ $t('home.heroPill') }}
          </span>
          <h1>
            {{ $t('home.heroTitleA') }}<br />
            <span class="accent">{{ $t('home.heroTitleB') }}</span>
          </h1>
          <p class="hero-v2-sub">{{ $t('home.heroSub') }}</p>
          <div class="hero-actions">
            <router-link to="/products" class="btn btn-primary btn-lg">
              {{ $t('home.startShopping') }} <el-icon><CaretRight /></el-icon>
            </router-link>
            <router-link to="/recommendations" class="btn btn-lg btn-hero-ghost">
              {{ $t('home.getAiPicks') }}
            </router-link>
          </div>
        </div>

        <!-- Right: AI / multi-LLM visual -->
        <div class="hero-v2-visual" aria-hidden="true">
          <!-- Far left: model chips -->
          <div class="model-stack">
            <div class="model-chip">
              <span class="model-ic ic-purple"><el-icon><Check /></el-icon></span>
              DeepSeek
            </div>
            <div class="model-chip">
              <span class="model-ic ic-blue"><el-icon><Setting /></el-icon></span>
              Kimi
            </div>
            <div class="model-chip">
              <span class="model-ic ic-teal"><el-icon><Document /></el-icon></span>
              Hunyuan
            </div>
          </div>

          <!-- Center: glowing orbits + headphones panel + floating trust card -->
          <div class="center-stage">
            <div class="orbits" aria-hidden="true">
              <span class="orbit orbit-blue"></span>
              <span class="orbit orbit-purple"></span>
              <span class="orbit orbit-orange"></span>
            </div>
            <div class="headphones-panel">
              <div class="headphones-box">
                <svg viewBox="0 0 240 240" xmlns="http://www.w3.org/2000/svg">
                  <defs>
                    <linearGradient id="hpRing" x1="0" y1="0" x2="1" y2="1">
                      <stop offset="0%" stop-color="#A78BFA"/>
                      <stop offset="100%" stop-color="#2DD4BF"/>
                    </linearGradient>
                    <linearGradient id="hpCup" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stop-color="#1E293B"/>
                      <stop offset="100%" stop-color="#0B1220"/>
                    </linearGradient>
                    <linearGradient id="hpBand" x1="0" y1="0" x2="1" y2="0">
                      <stop offset="0%" stop-color="#7C3AED"/>
                      <stop offset="100%" stop-color="#2DD4BF"/>
                    </linearGradient>
                  </defs>
                  <circle cx="120" cy="120" r="98" fill="url(#hpRing)" opacity="0.18"/>
                  <circle cx="120" cy="120" r="72" fill="url(#hpRing)" opacity="0.10"/>
                  <!-- headband -->
                  <path d="M52 130 Q52 52 120 52 Q188 52 188 130"
                        stroke="url(#hpBand)" stroke-width="11" fill="none" stroke-linecap="round"/>
                  <!-- left ear cup -->
                  <rect x="34" y="118" width="40" height="66" rx="15"
                        fill="url(#hpCup)" stroke="url(#hpRing)" stroke-width="3.5"/>
                  <circle cx="54" cy="151" r="9" fill="url(#hpRing)" opacity="0.55"/>
                  <!-- right ear cup -->
                  <rect x="166" y="118" width="40" height="66" rx="15"
                        fill="url(#hpCup)" stroke="url(#hpRing)" stroke-width="3.5"/>
                  <circle cx="186" cy="151" r="9" fill="url(#hpRing)" opacity="0.55"/>
                </svg>
              </div>
            </div>
            <div class="trust-card">
              <div class="tc-shield">
                <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                  <path d="M12 2 L4 5 v6 c0 5 3.5 9 8 11 4.5-2 8-6 8-11 V5 L12 2z" fill="currentColor"/>
                  <polyline points="9 12 11 14 15 10" stroke="#ffffff" stroke-width="2.6" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              <div class="tc-body">
                <div class="tc-label">Trust Score</div>
                <div class="tc-value">92<span class="tc-unit">/100</span></div>
                <div class="tc-pill">Excellent</div>
              </div>
            </div>
          </div>

          <!-- Multi-LLM Verification floating card (upper-right) -->
          <div class="multi-llm-float">
            <div class="ml-head">
              <div class="ml-title">
                <span class="ml-ic"><el-icon><Connection /></el-icon></span>
                Multi-LLM Verification
              </div>
              <div class="ml-status">
                <span class="ml-dot"></span> Analyzing<span class="ml-dots"><i></i><i></i><i></i></span>
              </div>
            </div>
            <div class="ml-row">
              <span class="ml-label">AI Consensus</span>
              <span class="ml-pct">94%</span>
            </div>
            <div class="ml-bar"><div class="ml-fill"></div></div>
          </div>

          <!-- Far right: feature pills -->
          <div class="fpill-stack">
            <div class="fpill">
              <span class="fpill-ic ic-green"><el-icon><CircleCheck /></el-icon></span>
              Real Reviews
            </div>
            <div class="fpill">
              <span class="fpill-ic ic-red"><el-icon><CircleClose /></el-icon></span>
              No Fake Feedback
            </div>
            <div class="fpill">
              <span class="fpill-ic ic-blue"><el-icon><Document /></el-icon></span>
              Verified Purchases
            </div>
            <div class="fpill">
              <span class="fpill-ic ic-purple"><el-icon><DataLine /></el-icon></span>
              Transparent Scores
            </div>
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

        <!-- AI Trust Score at a Glance -->
        <div class="feature-card trust-glance card">
          <div class="tg-header">
            <h3>{{ $t('home.tagTitle') }}</h3>
          </div>
          <div class="tg-body">
            <div class="tg-gauge">
              <svg viewBox="0 0 100 100" class="gauge-svg">
                <circle cx="50" cy="50" r="42" fill="none" stroke="rgba(16, 185, 129, 0.12)" stroke-width="8"/>
                <circle cx="50" cy="50" r="42" fill="none" stroke="#10B981" stroke-width="8"
                  stroke-linecap="round" stroke-dasharray="263.9" stroke-dashoffset="29"
                  transform="rotate(-90 50 50)"/>
              </svg>
              <div class="tg-gauge-text">
                <div class="tg-gauge-value">89</div>
                <div class="tg-gauge-unit">/100</div>
                <div class="tg-gauge-pill">{{ $t('home.tagHighTrust') }}</div>
              </div>
            </div>
            <ul class="tg-metrics">
              <li><span class="m-ic"><el-icon><CircleCheck /></el-icon></span><span class="m-label">{{ $t('home.tagAuthenticity') }}</span><span class="m-score">96/100</span></li>
              <li><span class="m-ic"><el-icon><CircleCheck /></el-icon></span><span class="m-label">{{ $t('home.tagSentiment') }}</span><span class="m-score">88/100</span></li>
              <li><span class="m-ic"><el-icon><CircleCheck /></el-icon></span><span class="m-label">{{ $t('home.tagVerifiedPurchases') }}</span><span class="m-score">87/100</span></li>
              <li><span class="m-ic"><el-icon><CircleCheck /></el-icon></span><span class="m-label">{{ $t('home.tagReviewVolume') }}</span><span class="m-score">90/100</span></li>
              <li><span class="m-ic"><el-icon><CircleCheck /></el-icon></span><span class="m-label">{{ $t('home.tagConsistency') }}</span><span class="m-score">85/100</span></li>
            </ul>
          </div>
          <div class="tg-footer">
            <span class="tg-powered">{{ $t('home.tagPoweredBy') }}</span>
            <div class="tg-models">
              <span class="tg-model"><el-icon><Cpu /></el-icon> {{ $t('home.tagDeepSeek') }}</span>
              <span class="tg-model"><el-icon><ChatLineRound /></el-icon> {{ $t('home.tagKimi') }}</span>
              <span class="tg-model"><el-icon><DataAnalysis /></el-icon> {{ $t('home.tagHunyuan') }}</span>
            </div>
          </div>
        </div>
      </section>

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
        <div class="product-grid">
          <ProductCard
            v-for="p in flashDeals"
            :key="p.id"
            :product="p"
            :show-image="true"
            :clickable="true"
          />
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
      <div v-if="topTrusted.length" class="product-grid">
        <ProductCard
          v-for="p in topTrusted"
          :key="p.id"
          :product="p"
          :show-image="true"
          :clickable="true"
        />
      </div>
      <div v-else class="empty-state">
        <div class="empty-icon"><el-icon><Odometer /></el-icon></div>
        <p style="font-size:17px;font-weight:700;color:var(--ink)">{{ $t('home.noAnalysed') }}</p>
        <p style="margin-top:4px">{{ $t('home.noAnalysedSub') }}</p>
      </div>

      <!-- ================= All Products ================= -->
      <section class="section-head" style="margin-top:48px">
        <div>
          <span class="eyebrow"><el-icon><Goods /></el-icon> {{ $t('home.catalog') }}</span>
          <h2>{{ $t('home.allProducts') }}</h2>
          <p class="section-sub">{{ $t('home.allProductsSub') }}</p>
        </div>
      </section>
      <div class="all-products-grid">
        <div class="product-grid">
          <ProductCard
            v-for="(p, i) in subProducts"
            :key="p.id || i"
            :product="p"
            :show-image="true"
            :clickable="true"
          />
        </div>
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
import subProductData from '../subcategoryProducts.json'

const products = ref([])

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

/* ---------- All Products: real product cards (one per subcategory) ---------- */
const subProducts = computed(() => {
  const byId = new Map(products.value.map(p => [String(p.id), p]))
  const seen = new Set()
  const out = []
  for (const e of (subProductData || [])) {
    if (e.productId == null) continue
    const real = byId.get(String(e.productId))
    if (real && !seen.has(real.id)) {
      seen.add(real.id)
      out.push(real)
    }
  }
  return out
})

/* ---------- Image-backed product pools (from subcategoryProducts.json) ---------- */
// Only products that actually have a local picture are eligible for the promo grids.
const imageIds = new Set((subProductData || []).map(e => String(e.productId)).filter(Boolean))

const imageProducts = computed(() =>
  products.value.filter(p => imageIds.has(String(p.id)))
)

/* ---------- Flash deals (10 cheapest that have a picture) ---------- */
const now = ref(Date.now())
const endOfDay = () => { const d = new Date(); d.setHours(23, 59, 59, 999); return d.getTime() }
const flashRemaining = computed(() => Math.max(0, Math.floor((endOfDay() - now.value) / 1000)))
const flashHours = computed(() => Math.floor(flashRemaining.value / 3600))
const flashMinutes = computed(() => Math.floor((flashRemaining.value % 3600) / 60))
const flashSeconds = computed(() => flashRemaining.value % 60)
const pad = (n) => String(n).padStart(2, '0')

const flashDeals = computed(() =>
  [...imageProducts.value]
    .filter(p => p.price != null)
    .sort((a, b) => a.price - b.price)
    .slice(0, 10)
    .map(p => ({ ...p, originalPrice: (p.price * 1.35).toFixed(2) }))
)

/* ---------- Top trusted (10 highest trust score that have a picture) ---------- */
const topTrusted = computed(() =>
  [...imageProducts.value]
    .sort((a, b) => (b.trustScore || 0) - (a.trustScore || 0))
    .slice(0, 10)
)

/* ---------- Lifecycle ---------- */
let countdownTimer = null
onMounted(async () => {
  try {
    const prodRes = await productApi.list()
    products.value = prodRes.data || []
  } catch { /* keep empty state */ }
  countdownTimer = setInterval(() => { now.value = Date.now() }, 1000)
})
onUnmounted(() => clearInterval(countdownTimer))
</script>

<style scoped>
/* ---------- Hero (v2 — two-column with AI visual) ---------- */
.hero.hero-v2 {
  text-align: left;
  padding: 76px 24px 88px;
}
.hero-v2-grid {
  max-width: var(--maxw);
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1.35fr;
  gap: 48px;
  align-items: center;
}
.hero-v2-text { max-width: 560px; }
.hero-v2-text h1 {
  margin: 0;
  font-size: 52px;
  line-height: 1.12;
  letter-spacing: -0.03em;
  max-width: none;
  text-align: left;
}
.hero-v2-text .accent {
  background: linear-gradient(100deg, #2DD4BF 0%, #10B981 55%, #34D399 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.hero-v2-sub {
  margin: 18px 0 0;
  max-width: 520px;
  font-size: 15px;
  line-height: 1.75;
  color: rgba(255, 255, 255, 0.74);
}
.hero.hero-v2 .hero-actions { justify-content: flex-start; margin-top: 30px; }
.hero.hero-v2 .hero-pill { margin-bottom: 22px; }

/* ---- Hero CTA buttons (dark hero) ---- */
.hero.hero-v2 .btn-primary {
  background: var(--primary-grad);
  color: #fff;
  font-weight: 700;
  letter-spacing: 0.01em;
  padding: 0 32px;
  border: none;
  box-shadow:
    0 10px 28px rgba(255, 90, 31, 0.42),
    0 0 0 1px rgba(255, 255, 255, 0.08) inset,
    0 0 24px rgba(255, 90, 31, 0.28);
}
.hero.hero-v2 .btn-primary:hover:not(:disabled) {
  background: var(--primary-grad);
  transform: translateY(-2px);
  filter: brightness(1.05);
  box-shadow:
    0 16px 36px rgba(255, 90, 31, 0.55),
    0 0 0 1px rgba(255, 255, 255, 0.10) inset,
    0 0 32px rgba(255, 90, 31, 0.38);
}
.hero.hero-v2 .btn-primary .el-icon {
  font-size: 18px;
  margin-left: 4px;
}

.hero.hero-v2 .btn-hero-ghost {
  background: rgba(255, 255, 255, 0.04);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.22);
  font-weight: 600;
  letter-spacing: 0.01em;
  padding: 0 30px;
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  transition: all var(--transition);
}
.hero.hero-v2 .btn-hero-ghost:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.10);
  border-color: rgba(255, 255, 255, 0.40);
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 8px 22px rgba(0, 0, 0, 0.28);
}

/* ---- Right-side visual (model chips + center + feature pills) ---- */
.hero-v2-visual {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 18px;
  min-height: 360px;
}
.model-stack,
.fpill-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 0 0 auto;
}
.model-chip,
.fpill {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 14px;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  backdrop-filter: blur(8px);
  white-space: nowrap;
}
.model-ic,
.fpill-ic {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
  background: transparent;
  border: 1.5px solid;
}
.model-ic.ic-purple { color: #A78BFA; border-color: rgba(167, 139, 250, 0.55); }
.model-ic.ic-blue   { color: #60A5FA; border-color: rgba(96, 165, 250, 0.55); }
.model-ic.ic-teal   { color: #2DD4BF; border-color: rgba(45, 212, 191, 0.55); }

.fpill-ic.ic-green  { color: #10B981; border-color: rgba(16, 185, 129, 0.55); }
.fpill-ic.ic-red    { color: #F87171; border-color: rgba(239, 68, 68, 0.55); }
.fpill-ic.ic-blue   { color: #60A5FA; border-color: rgba(96, 165, 250, 0.55); }
.fpill-ic.ic-purple { color: #A78BFA; border-color: rgba(167, 139, 250, 0.55); }

.center-stage {
  position: relative;
  width: 260px;
  height: 260px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
}
.center-stage::before {
  content: '';
  position: absolute;
  top: 50%; left: 50%;
  width: 70%; height: 70%;
  margin: -35% 0 0 -35%;
  background: radial-gradient(circle, rgba(124, 58, 237, 0.20) 0%, rgba(45, 212, 191, 0.10) 40%, transparent 70%);
  border-radius: 50%;
  filter: blur(22px);
  z-index: -1;
}
.orbits {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}
.orbit {
  position: absolute;
  top: 50%; left: 50%;
  width: 88%;
  height: 88%;
  margin: -44% 0 0 -44%;
  border-radius: 50%;
  border: 1.5px solid;
  filter: blur(0.4px);
}
.orbit-blue   { border-color: rgba(96, 165, 250, 0.55); transform: rotate(-18deg) scaleY(0.30); }
.orbit-purple { border-color: rgba(167, 139, 250, 0.55); transform: rotate(28deg)  scaleY(0.40); }
.orbit-orange { border-color: rgba(255, 138, 76, 0.55);  transform: rotate(-38deg) scaleY(0.28); }

.headphones-panel {
  position: relative;
  width: 200px;
  height: 200px;
  border-radius: 28px;
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.10) 0%, rgba(255, 255, 255, 0.02) 100%);
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow:
    0 14px 40px rgba(0, 0, 0, 0.35),
    inset 0 0 0 1px rgba(255, 255, 255, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: floaty 6s ease-in-out infinite;
  z-index: 1;
}
.headphones-box {
  position: relative;
  width: 180px;
  height: 180px;
}
.headphones-box svg { width: 100%; height: 100%; display: block; }

.multi-llm-float {
  width: 180px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(11, 18, 32, 0.82);
  border: 1px solid rgba(255, 255, 255, 0.14);
  box-shadow: 0 14px 40px rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(14px);
  flex: 0 0 auto;
}

/* ---- Trust Score card (floating, green glass with shield) ---- */
.trust-card {
  position: absolute;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 4;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px 10px 10px;
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  box-shadow:
    0 14px 30px rgba(16, 185, 129, 0.45),
    0 0 0 5px rgba(16, 185, 129, 0.10);
  color: #fff;
  white-space: nowrap;
  animation: floaty 6s ease-in-out infinite;
  animation-delay: -3s;
}
.tc-shield {
  width: 38px;
  height: 38px;
  background: #fff;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #10B981;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.18);
}
.tc-shield svg { width: 22px; height: 22px; display: block; }
.tc-body { display: flex; flex-direction: column; gap: 2px; align-items: flex-start; }
.tc-label { font-size: 9px; font-weight: 700; opacity: 0.85; text-transform: uppercase; letter-spacing: 0.08em; }
.tc-value { font-size: 18px; font-weight: 800; line-height: 1.05; letter-spacing: -0.01em; }
.tc-unit  { font-size: 11px; font-weight: 600; opacity: 0.7; margin-left: 1px; }
.tc-pill {
  margin-top: 4px;
  padding: 2px 10px;
  border: 1px solid rgba(255, 255, 255, 0.55);
  border-radius: 999px;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}
.ml-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.ml-title {
  display: flex;
  align-items: center;
  gap: 7px;
  color: #fff;
  font-size: 12.5px;
  font-weight: 700;
}
.ml-ic {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: rgba(45, 212, 191, 0.18);
  color: #2DD4BF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
}
.ml-status {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #2DD4BF;
  font-size: 11px;
  font-weight: 600;
}
.ml-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #2DD4BF;
  box-shadow: 0 0 0 0 rgba(45, 212, 191, 0.6);
  animation: pulse-dot 1.5s ease-in-out infinite;
}
.ml-dots {
  display: inline-flex;
  gap: 1px;
  margin-left: 1px;
}
.ml-dots i {
  display: inline-block;
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: currentColor;
  opacity: 0;
  animation: ml-dots-blink 1.4s infinite;
}
.ml-dots i:nth-child(2) { animation-delay: 0.2s; }
.ml-dots i:nth-child(3) { animation-delay: 0.4s; }
@keyframes ml-dots-blink {
  0%, 20% { opacity: 0; }
  50%     { opacity: 1; }
  100%    { opacity: 0; }
}
@keyframes pulse-dot {
  0%, 100% { box-shadow: 0 0 0 0 rgba(45, 212, 191, 0.6); opacity: 1; }
  50%      { box-shadow: 0 0 0 6px rgba(45, 212, 191, 0);  opacity: 0.5; }
}
@keyframes floaty {
  0%, 100% { transform: translateY(0); }
  50%      { transform: translateY(-6px); }
}
.ml-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 8px;
}
.ml-label { color: rgba(255, 255, 255, 0.7); font-size: 12px; }
.ml-pct   { color: #2DD4BF; font-size: 18px; font-weight: 800; letter-spacing: -0.01em; }
.ml-bar {
  height: 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.10);
  overflow: hidden;
}
.ml-fill {
  display: block;
  height: 100%;
  width: 94%;
  border-radius: 999px;
  background: linear-gradient(90deg, #2DD4BF 0%, #10B981 100%);
  box-shadow: 0 0 12px rgba(45, 212, 191, 0.55);
  animation: grow-bar 1.2s ease-out both;
}
@keyframes grow-bar {
  from { width: 0; }
  to   { width: 94%; }
}

/* ---- Hero responsive ---- */
@media (max-width: 960px) {
  .hero-v2-grid { grid-template-columns: 1fr; gap: 40px; }
  .hero-v2-text h1 { font-size: 38px; }
  .hero-v2-visual { flex-wrap: wrap; gap: 14px; }
  .center-stage { width: 240px; height: 240px; }
  .headphones-panel { width: 180px; height: 180px; }
  .headphones-box { width: 160px; height: 160px; }
  .multi-llm-float { width: 170px; }
  .trust-card { padding: 8px 12px 8px 8px; gap: 8px; }
  .tc-shield { width: 32px; height: 32px; }
  .tc-shield svg { width: 18px; height: 18px; }
  .tc-value { font-size: 16px; }
}
@media (max-width: 560px) {
  .hero.hero-v2 { padding: 56px 18px 64px; }
  .hero-v2-text h1 { font-size: 30px; }
  .model-stack, .fpill-stack { flex-direction: row; flex-wrap: wrap; justify-content: center; }
  .model-chip, .fpill { padding: 8px 12px; font-size: 12px; }
  .center-stage { width: 240px; height: 240px; }
  .headphones-panel { width: 180px; height: 180px; }
  .headphones-box { width: 160px; height: 160px; }
  .multi-llm-float { width: 100%; max-width: 240px; }
  .trust-card { bottom: 18px; }
}

/* ---------- Features ---------- */
.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
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

/* ---- AI Trust Score at a Glance ---- */
.trust-glance {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px 20px 16px;
}
.tg-header h3 {
  font-size: 14px;
  font-weight: 700;
  color: var(--ink);
  margin: 0 0 2px;
  letter-spacing: -0.01em;
}
.tg-body {
  display: flex;
  align-items: center;
  gap: 14px;
}
.tg-gauge {
  position: relative;
  width: 88px;
  height: 88px;
  flex-shrink: 0;
}
.gauge-svg { width: 100%; height: 100%; display: block; }
.tg-gauge-text {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  line-height: 1;
}
.tg-gauge-value { font-size: 24px; font-weight: 800; color: var(--ink); }
.tg-gauge-unit  { font-size: 9px; color: var(--text-muted); font-weight: 600; margin-top: 1px; }
.tg-gauge-pill {
  margin-top: 4px;
  padding: 1px 7px;
  background: rgba(16, 185, 129, 0.12);
  color: #059669;
  border-radius: 999px;
  font-size: 8px;
  font-weight: 700;
  letter-spacing: 0.02em;
}
.tg-metrics {
  list-style: none;
  padding: 0;
  margin: 0;
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1px;
}
.tg-metrics li {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  color: var(--text-secondary);
}
.m-ic {
  color: #10B981;
  font-size: 11px;
  display: flex;
  align-items: center;
  flex-shrink: 0;
}
.m-label {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.m-score {
  font-weight: 700;
  color: var(--text-muted);
  font-size: 10px;
  flex-shrink: 0;
}
.tg-footer {
  border-top: 1px solid var(--border);
  padding-top: 8px;
  margin-top: 4px;
}
.tg-powered {
  font-size: 9px;
  color: var(--text-muted);
  font-weight: 600;
  letter-spacing: 0.02em;
  text-transform: uppercase;
}
.tg-models {
  display: flex;
  gap: 5px;
  margin-top: 6px;
  flex-wrap: wrap;
}
.tg-model {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 2px 7px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 999px;
  font-size: 9px;
  font-weight: 600;
  color: var(--text-secondary);
}
.tg-model .el-icon { font-size: 9px; color: var(--primary); }

@media (max-width: 560px) {
  .tg-body { flex-direction: column; align-items: flex-start; }
  .tg-gauge { width: 72px; height: 72px; }
}

/* ---------- Flash deals ---------- */
.see-all { margin-left: auto; }
.product-card--static { cursor: default; }

/* ---------- All-products pager ---------- */
.all-pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 28px;
}
.all-pager button {
  font-family: inherit;
}
.all-pager .page-pill,
.all-pager .page-num {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 36px;
  padding: 0 10px;
  border: 1px solid var(--border);
  border-radius: var(--radius-full);
  background: #fff;
  color: var(--text-secondary);
  font-size: var(--font-sm);
  font-weight: 700;
  cursor: pointer;
  box-shadow: var(--shadow-xs);
  transition: all var(--transition);
}
.all-pager .page-pill { width: 36px; padding: 0; }
.all-pager .page-pill .el-icon { font-size: 15px; }
.all-pager .page-gap {
  min-width: 22px;
  text-align: center;
  color: var(--text-muted);
  font-size: var(--font-sm);
  font-weight: 700;
}
.all-pager button:hover:not(:disabled) {
  color: #fff;
  background: var(--primary-grad);
  border-color: transparent;
  box-shadow: var(--shadow-brand);
  transform: translateY(-1px);
}
.all-pager button:disabled {
  opacity: 0.35;
  cursor: default;
  background: #fff;
  color: var(--text-secondary);
  transform: none;
  box-shadow: none;
}
.all-pager .page-num.active {
  background: var(--primary-grad);
  color: #fff;
  border-color: transparent;
  box-shadow: var(--shadow-brand);
}

/* ---------- All Products: product grid ---------- */
.all-products-grid { margin-top: 28px; }

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

/* ---------- Flash deals ---------- */
.flash-deal-section { margin-top: 56px; }
.flash-deal-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.flash-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px; height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #FF5000, #FF2D00);
  color: #fff;
  font-size: 22px;
}
.flash-title {
  font-size: var(--font-2xl);
  font-weight: 800;
  color: var(--ink);
  letter-spacing: -0.02em;
}
.countdown {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-left: 4px;
}
.time-block {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 34px;
  padding: 4px 8px;
  border-radius: 8px;
  background: #1B1B1B;
  color: #fff;
  font-weight: 800;
  font-size: 15px;
  font-variant-numeric: tabular-nums;
}
.time-sep { color: #ff5000; font-weight: 800; }
.see-all { margin-left: auto; }

@media (max-width: 768px) {
  .features { margin-top: -28px; gap: 12px; }
  .cta-band { padding: 30px 22px; }
  .cta-text h2 { font-size: var(--font-2xl); }
  .flash-deal-header .see-all { margin-left: 0; }
}
</style>
