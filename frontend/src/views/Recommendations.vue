<template>
  <div class="page-container ai-picks">
    <!-- Hero header -->
    <section class="ai-hero">
      <span class="ai-hero-badge">
        <el-icon><MagicStick /></el-icon>
        AI-Powered
      </span>
      <h1 class="ai-hero-title">AI Picks For You</h1>
      <p class="ai-hero-sub">
        Tell us what you need in plain words &mdash; our AI scans the whole catalogue and ranks the
        best matches by relevance, rating &amp; trust.
      </p>
    </section>

    <!-- Preferences -->
    <section class="pref-card">
      <label class="pref-label">What are you looking for?</label>
      <div class="pref-row">
        <el-input
          v-model="preferences"
          size="large"
          placeholder="e.g. wireless headphones under $100 with good bass"
          clearable
          class="pref-input"
          @keyup.enter="getRecommendations"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" size="large" class="pref-btn" @click="getRecommendations" :loading="loading">
          <el-icon><MagicStick /></el-icon>
          Get Recommendations
        </el-button>
      </div>
    </section>

    <!-- Results -->
    <template v-if="recommendationDetails.length">
      <header class="result-head">
        <div>
          <h2 class="result-title">Recommended for You</h2>
          <p class="result-sub">{{ recommendationDetails.length }} picks ranked by relevance, rating &amp; trust</p>
        </div>
        <el-tag type="primary" effect="light" round>{{ recommendationDetails.length }} results</el-tag>
      </header>

      <div class="recommendation-grid">
        <article v-for="(rec, idx) in recommendationDetails" :key="rec.productId || idx" class="recommendation-card">
          <div class="rec-media">
            <span class="rec-rank">#{{ idx + 1 }}</span>
            <ProductCard :product="rec.product" />
          </div>
          <div class="rec-reason">
            <div class="rec-reason-head">
              <el-icon class="rec-spark"><MagicStick /></el-icon>
              <span class="rec-text">{{ rec.reason || 'Highly rated match' }}</span>
            </div>
            <div class="rec-highlights" v-if="rec.highlights?.length">
              <ElTag v-for="h in rec.highlights" :key="h" size="small" type="success" effect="plain">
                {{ h }}
              </ElTag>
            </div>
          </div>
        </article>
      </div>
    </template>

    <!-- AI Explanation -->
    <section class="ai-explain" v-if="aiExplanation">
      <div class="ai-explain-head">
        <el-icon class="ai-explain-icon"><Cpu /></el-icon>
        <h3>Why These Recommendations?</h3>
      </div>
      <div v-html="renderedExplanation" class="explanation-body"></div>
    </section>

    <!-- Empty: searched but no results -->
    <section v-if="!loading && !recommended.length && hasSearched" class="empty-state">
      <el-icon class="empty-icon"><Search /></el-icon>
      <h3 class="empty-title">No matches found</h3>
      <p class="empty-text">Try broadening your search or using different keywords.</p>
    </section>

    <!-- Trending fallback -->
    <section v-if="!hasSearched && trending.length" class="trending-section">
      <header class="result-head">
        <div>
          <h2 class="result-title">Trending Now</h2>
          <p class="result-sub">Popular picks loved by the community</p>
        </div>
      </header>
      <div class="product-grid">
        <ProductCard v-for="p in trending" :key="p.id" :product="p" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { aiApi, productApi } from '@/api'
import { marked } from 'marked'
import ProductCard from '@/components/ProductCard.vue'
import { ElMessage, ElTag } from 'element-plus'

const preferences = ref('')
const recommended = ref([])
const recommendations = ref([])
const trending = ref([])
const aiExplanation = ref('')
const loading = ref(false)
const hasSearched = ref(false)

const recommendationDetails = computed(() => {
  if (!recommendations.value?.length) return recommended.value.map(p => ({ product: p }))
  return recommendations.value.map(rec => {
    const product = recommended.value.find(p => p.id === rec.productId)
    return { ...rec, product }
  }).filter(r => r.product)
})

const renderedExplanation = computed(() => {
  if (!aiExplanation.value) return ''
  return marked(aiExplanation.value)
})

onMounted(async () => {
  try {
    const res = await productApi.list({ sort: 'trust', limit: 8 })
    trending.value = res.data || []
  } catch (e) { /* handled */ }
})

async function getRecommendations() {
  if (!preferences.value.trim()) {
    ElMessage.warning('Please describe what you\'re looking for')
    return
  }

  loading.value = true
  hasSearched.value = true

  try {
    const res = await aiApi.recommend({ preferences: preferences.value })
    recommended.value = res.data?.products || []
    recommendations.value = res.data?.recommendations || []
    aiExplanation.value = res.data?.explanation || ''
  } catch (e) {
    ElMessage.error('Failed to get recommendations. Please try again.')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.ai-picks {
  max-width: var(--maxw, 1180px);
}

/* ============ Hero header ============ */
.ai-hero {
  position: relative;
  text-align: center;
  padding: 48px 24px;
  margin-bottom: 28px;
  border-radius: var(--radius-lg);
  background: var(--primary-grad);
  color: #fff;
  box-shadow: var(--shadow-brand);
  overflow: hidden;
}
.ai-hero::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 15% 20%, rgba(255,255,255,0.18), transparent 38%),
    radial-gradient(circle at 85% 80%, rgba(255,255,255,0.12), transparent 42%);
  pointer-events: none;
}
.ai-hero-badge {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 14px;
  margin-bottom: 16px;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.35);
  font-size: var(--font-xs);
  font-weight: 800;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}
.ai-hero-badge .el-icon { font-size: 14px; }
.ai-hero-title {
  position: relative;
  z-index: 1;
  font-size: var(--font-3xl);
  font-weight: 800;
  letter-spacing: -0.03em;
  margin: 0;
}
.ai-hero-sub {
  position: relative;
  z-index: 1;
  max-width: 620px;
  margin: 12px auto 0;
  font-size: var(--font-md);
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.92);
}

/* ============ Preferences card ============ */
.pref-card {
  background: #fff;
  border: 1px solid var(--border-light, #E2E8F0);
  border-radius: var(--radius-lg);
  padding: 24px;
  margin-bottom: 28px;
  box-shadow: var(--shadow-xs, 0 1px 3px rgba(15,23,42,0.06));
}
.pref-label {
  display: block;
  font-size: var(--font-md);
  font-weight: 700;
  color: var(--ink, #0B1220);
  margin-bottom: 12px;
}
.pref-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.pref-input { flex: 1 1 300px; }
.pref-btn { flex-shrink: 0; }

/* ============ Result header ============ */
.result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 28px 0 18px;
}
.result-title {
  font-size: var(--font-2xl);
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--ink, #0B1220);
  margin: 0;
}
.result-sub {
  margin: 4px 0 0;
  font-size: var(--font-sm);
  color: var(--text-secondary, #475569);
}

/* ============ Recommendation cards ============ */
.recommendation-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}
.recommendation-card {
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid var(--border-light, #E2E8F0);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-xs, 0 1px 3px rgba(15,23,42,0.06));
  transition: transform var(--transition, 0.2s), box-shadow var(--transition, 0.2s);
}
.recommendation-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.12);
}
.recommendation-card :deep(.product-card) {
  border: none;
  border-radius: 0;
  box-shadow: none;
}
.rec-media {
  position: relative;
}
.rec-rank {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 2;
  min-width: 28px;
  height: 28px;
  padding: 0 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-grad);
  color: #fff;
  border-radius: var(--radius-full);
  font-weight: 800;
  font-size: var(--font-xs);
  box-shadow: 0 4px 10px rgba(255, 90, 31, 0.4);
}
.rec-reason {
  background: var(--surface-muted, #F1F3F7);
  border-top: 1px solid var(--border-light, #E2E8F0);
  padding: 14px 16px;
  font-size: var(--font-sm);
  color: var(--text, #334155);
}
.rec-reason-head {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}
.rec-spark {
  color: var(--primary);
  font-size: 16px;
  margin-top: 1px;
  flex-shrink: 0;
}
.rec-text {
  line-height: 1.5;
  font-weight: 600;
  color: var(--ink, #0B1220);
}
.rec-highlights {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

/* ============ AI explanation ============ */
.ai-explain {
  margin-top: 32px;
  padding: 24px;
  background: #fff;
  border: 1px solid var(--border-light, #E2E8F0);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xs, 0 1px 3px rgba(15,23,42,0.06));
}
.ai-explain-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}
.ai-explain-head h3 {
  margin: 0;
  font-size: var(--font-lg);
  font-weight: 800;
  color: var(--ink, #0B1220);
}
.ai-explain-icon {
  font-size: 22px;
  color: var(--primary);
}
.explanation-body {
  line-height: 1.8;
  color: var(--text-secondary, #475569);
}
.explanation-body :deep(strong) { color: var(--ink, #0B1220); }
.explanation-body :deep(ul) { padding-left: 20px; margin: 8px 0; }
.explanation-body :deep(li) { margin-bottom: 6px; }
.explanation-body :deep(p) { margin-bottom: 12px; }

/* ============ Empty states ============ */
.empty-state {
  text-align: center;
  padding: 56px 24px;
  margin-top: 28px;
  background: #fff;
  border: 1px dashed var(--border, #E2E8F0);
  border-radius: var(--radius-lg);
}
.empty-icon {
  font-size: 40px;
  color: var(--primary, #FF5A1F);
  margin-bottom: 12px;
}
.empty-title {
  margin: 0 0 6px;
  font-size: var(--font-lg);
  font-weight: 700;
  color: var(--ink, #0B1220);
}
.empty-text {
  margin: 0;
  font-size: var(--font-sm);
  color: var(--text-muted, #94A3B8);
}

/* ============ Trending ============ */
.trending-section { margin-top: 32px; }
</style>
