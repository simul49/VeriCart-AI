<template>
  <div class="page-container">
    <h2 class="section-title">⚖️ Compare Products</h2>
    <p style="color:var(--text-muted);margin-bottom:24px">
      AI compares trust score, rating and price to recommend the most trustworthy option (FR-057).
    </p>

    <div v-if="compareStore.count === 0" class="empty-state">
      <p style="font-size:40px;margin-bottom:8px">⚖️</p>
      <p style="font-weight:600;color:var(--text)">No products selected</p>
      <p style="margin-top:4px">Open a product and tap “Compare” to add it here (max 4).</p>
      <router-link to="/products" class="browse-link">Browse products →</router-link>
    </div>

    <template v-else>
      <div class="compare-grid">
        <div v-for="p in compareStore.items" :key="p.id" class="compare-card">
          <button class="remove-btn" @click="compareStore.remove(p.id)" title="Remove">×</button>

          <img :src="firstImage(p)" :alt="p.name" class="compare-img" />

          <h4 class="compare-name">{{ p.name }}</h4>

          <div class="compare-price">${{ p.price }}</div>

          <div class="compare-metric">
            <span class="metric-label">Trust Score</span>
            <span :class="['metric-value', trustClass(p.trustScore)]">
              {{ p.trustScore ?? '—' }}<small v-if="p.trustScore">/100</small>
            </span>
          </div>

          <div class="compare-metric">
            <span class="metric-label">Rating</span>
            <span class="metric-value">
              {{ p.rating ? Number(p.rating).toFixed(1) : '—' }}<small>/5</small>
            </span>
          </div>

          <div class="compare-metric">
            <span class="metric-label">Reviews</span>
            <span class="metric-value">{{ p.reviewCount ?? 0 }}</span>
          </div>

          <div v-if="p.id === winnerId" class="winner-flag">🏆 AI's pick</div>
        </div>
      </div>

      <div class="compare-actions">
        <el-button type="primary" :loading="loading" @click="runComparison">
          🤖 Run AI Comparison
        </el-button>
        <el-button @click="compareStore.clear()">Clear all</el-button>
      </div>

      <!-- Explainable AI verdict -->
      <div v-if="verdict" class="ai-card verdict-card">
        <h3>🤖 AI Verdict</h3>
        <div class="verdict-text" v-html="renderedVerdict"></div>
      </div>

      <div v-if="comparedProducts.length" class="ai-card">
        <h3>📊 Side-by-side</h3>
        <div class="table-wrap">
          <table class="compare-table">
            <thead>
              <tr>
                <th>Attribute</th>
                <th v-for="p in comparedProducts" :key="p.id">{{ p.name }}</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>Price</td>
                <td v-for="p in comparedProducts" :key="p.id">${{ p.price }}</td>
              </tr>
              <tr>
                <td>Brand</td>
                <td v-for="p in comparedProducts" :key="p.id">{{ p.brand || '—' }}</td>
              </tr>
              <tr>
                <td>Stock</td>
                <td v-for="p in comparedProducts" :key="p.id">{{ p.stock ?? '—' }}</td>
              </tr>
              <tr>
                <td>Rating</td>
                <td v-for="p in comparedProducts" :key="p.id">
                  {{ p.rating ? Number(p.rating).toFixed(1) : '—' }}/5
                </td>
              </tr>
              <tr>
                <td>Trust Score</td>
                <td v-for="p in comparedProducts" :key="p.id">
                  <span :class="trustClass(p.trustScore)">{{ p.trustScore ?? '—' }}</span>
                </td>
              </tr>
              <tr>
                <td>Suspicious reviews</td>
                <td v-for="p in comparedProducts" :key="p.id">
                  {{ p.fakeReviewCount ?? 0 }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { productApi } from '@/api'
import { useCompareStore } from '@/stores/compare'
import { marked } from 'marked'
import { ElMessage } from 'element-plus'

const compareStore = useCompareStore()

const loading = ref(false)
const verdict = ref('')
const winnerId = ref(null)
const comparedProducts = ref([])

const renderedVerdict = computed(() => (verdict.value ? marked(verdict.value) : ''))

function firstImage(p) {
  if (!p.images) return 'https://placehold.co/200x200/F8F8F8/CCC?text=No+Image'
  try {
    const arr = JSON.parse(p.images)
    return Array.isArray(arr) ? arr[0] : p.images
  } catch {
    return p.images
  }
}

function trustClass(score) {
  if (!score) return ''
  if (score >= 85) return 'trust-excellent'
  if (score >= 70) return 'trust-high'
  if (score >= 50) return 'trust-medium'
  return 'trust-low'
}

async function runComparison() {
  if (compareStore.count < 2) {
    ElMessage.warning('Select at least 2 products to compare')
    return
  }
  loading.value = true
  try {
    const res = await productApi.compare(compareStore.ids.join(','))
    comparedProducts.value = res.data?.products || []
    verdict.value = res.data?.verdict || ''
    winnerId.value = res.data?.winnerId ?? null
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || 'Comparison failed')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.compare-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.compare-card {
  position: relative;
  background: #fff;
  border: 1px solid var(--border-light, #ebeef5);
  border-radius: var(--radius-lg, 12px);
  padding: 16px;
  text-align: center;
}

.remove-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 1px solid var(--border-light, #ebeef5);
  background: #fff;
  color: var(--text-muted, #9ca3af);
  cursor: pointer;
  line-height: 1;
}

.remove-btn:hover {
  background: #fef2f2;
  color: var(--danger, #ef4444);
  border-color: #fecaca;
}

.compare-img {
  width: 100%;
  height: 140px;
  object-fit: contain;
  margin-bottom: 10px;
}

.compare-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--text, #303133);
  margin-bottom: 6px;
  line-height: 1.4;
}

.compare-price {
  font-size: 18px;
  font-weight: 800;
  color: var(--primary, #ff6b35);
  margin-bottom: 12px;
}

.compare-metric {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  border-top: 1px dashed var(--border-light, #ebeef5);
  font-size: 13px;
}

.metric-label {
  color: var(--text-muted, #9ca3af);
}

.metric-value {
  font-weight: 700;
  color: var(--text, #303133);
}

.metric-value small {
  font-size: 11px;
  color: var(--text-muted, #9ca3af);
}

.winner-flag {
  margin-top: 12px;
  background: #ecfdf5;
  color: #16a34a;
  font-weight: 700;
  font-size: 12px;
  padding: 6px;
  border-radius: 6px;
}

.compare-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 24px;
}

.verdict-card {
  margin-bottom: 24px;
}

.verdict-text {
  line-height: 1.8;
  font-size: var(--font-sm, 14px);
  color: var(--text-secondary, #606266);
}

.table-wrap {
  overflow-x: auto;
}

.compare-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.compare-table th,
.compare-table td {
  padding: 10px 12px;
  text-align: left;
  border-bottom: 1px solid var(--border-light, #ebeef5);
}

.compare-table th {
  font-weight: 700;
  color: var(--text, #303133);
  background: #fafafa;
}

.browse-link {
  display: inline-block;
  margin-top: 12px;
  color: var(--primary, #ff6b35);
  font-weight: 600;
}

.trust-excellent { color: #16a34a; font-weight: 700; }
.trust-high { color: #65a30d; font-weight: 700; }
.trust-medium { color: #f59e0b; font-weight: 700; }
.trust-low { color: #ef4444; font-weight: 700; }
</style>
