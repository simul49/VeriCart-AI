<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="section-title">🤖 AI Recommendations For You</h2>
      <p style="color:#6B7280;margin-top:4px">
        Personalized product picks based on your browsing history and preferences
      </p>
    </div>

    <!-- Preferences -->
    <div class="pref-card">
      <h3 style="margin-bottom:12px">What are you looking for?</h3>
      <div style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:12px">
        <el-input
          v-model="preferences"
          placeholder="e.g., 'wireless headphones under $100 with good bass'"
          style="flex:1;min-width:280px"
          clearable
          @keyup.enter="getRecommendations"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="getRecommendations" :loading="loading">
          Get Recommendations
        </el-button>
      </div>
      <p style="font-size:12px;color:#9CA3AF">
        Powered by AI — describe what you need and we'll find the best matches
      </p>
    </div>

    <!-- Results -->
    <template v-if="recommendationDetails.length">
      <div style="display:flex;align-items:center;justify-content:space-between;margin:24px 0 16px">
        <h3 style="font-size:18px;font-weight:700;margin:0">
          Recommended for You ({{ recommendationDetails.length }})
        </h3>
        <span style="font-size:13px;color:#6B7280">Ranked by relevance, rating & trust</span>
      </div>
      <div class="recommendation-grid">
        <div v-for="(rec, idx) in recommendationDetails" :key="rec.productId || idx" class="recommendation-card">
          <ProductCard :product="rec.product" />
          <div class="rec-reason">
            <div style="display:flex;align-items:center;gap:8px;margin-bottom:8px">
              <div class="rec-rank">#{{ idx + 1 }}</div>
              <div class="rec-text">{{ rec.reason || 'Highly rated match' }}</div>
            </div>
            <div class="rec-highlights" v-if="rec.highlights?.length">
              <ElTag v-for="h in rec.highlights" :key="h" size="small" type="success" effect="plain">
                {{ h }}
              </ElTag>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- AI Explanation -->
    <div class="ai-card" v-if="aiExplanation" style="margin-top:24px">
      <h3>🧠 Why These Recommendations?</h3>
      <div v-html="renderedExplanation" class="explanation-body"></div>
    </div>

    <!-- Empty State -->
    <div v-if="!loading && !recommended.length && hasSearched" style="text-align:center;padding:60px;color:#9CA3AF">
      <el-empty description="No recommendations found">
        <p style="margin-top:8px">Try broadening your search or using different keywords</p>
      </el-empty>
    </div>

    <div v-if="!hasSearched" style="text-align:center;padding:60px;color:#9CA3AF">
      <p style="font-size:48px;margin-bottom:16px">🔍</p>
      <p style="font-size:18px;font-weight:600;color:#4B5563">Describe what you're looking for above</p>
      <p style="margin-top:8px">Our AI will analyze all products and find the perfect match</p>
    </div>

    <!-- Trending Products (fallback) -->
    <div v-if="!hasSearched && trending.length" style="margin-top:48px">
      <h3 class="section-title">🔥 Trending Now</h3>
      <div class="product-grid">
        <ProductCard v-for="p in trending" :key="p.id" :product="p" />
      </div>
    </div>
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
.page-header {
  margin-bottom: 24px;
}

.pref-card {
  background: linear-gradient(135deg, #EEF2FF 0%, #E0E7FF 100%);
  border: 1px solid #C7D2FE;
  border-radius: 16px;
  padding: 24px;
}

.recommendation-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.recommendation-card {
  display: flex;
  flex-direction: column;
}

.recommendation-card :deep(.product-card) {
  flex: 1;
  border-radius: 16px 16px 0 0;
}

.rec-reason {
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-top: none;
  border-radius: 0 0 16px 16px;
  padding: 14px 16px;
  font-size: 13px;
  color: #374151;
}

.rec-rank {
  width: 24px; height: 24px;
  display: flex; align-items: center; justify-content: center;
  background: var(--primary); color: white;
  border-radius: 50%; font-weight: 700; font-size: 12px;
  flex-shrink: 0;
}

.rec-text {
  line-height: 1.5;
  font-weight: 500;
}

.rec-highlights {
  display: flex; flex-wrap: wrap; gap: 6px;
  margin-top: 4px;
}

.explanation-body {
  line-height: 1.8;
  color: #4B5563;
}

.explanation-body :deep(strong) { color: #1F2937; }
.explanation-body :deep(ul) { padding-left: 20px; margin: 8px 0; }
.explanation-body :deep(li) { margin-bottom: 6px; }
.explanation-body :deep(p) { margin-bottom: 12px; }
</style>
