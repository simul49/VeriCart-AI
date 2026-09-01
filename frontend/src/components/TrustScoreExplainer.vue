<template>
  <div v-if="factors.length" class="explainer">
    <div class="explainer-head" @click="open = !open">
      <div>
        <span class="explainer-title">Why this Trust Score?</span>
        <span class="explainer-sub">AI breaks the score into weighted factors</span>
      </div>
      <span class="explainer-toggle">{{ open ? 'Hide' : 'Show' }} breakdown</span>
    </div>

    <div v-if="open" class="explainer-body">
      <div v-for="f in factors" :key="f.factor" class="factor">
        <div class="factor-top">
          <span class="factor-name">{{ f.factor }}</span>
          <span class="factor-weight">{{ f.weight }}% weight</span>
        </div>
        <div class="factor-bar-track">
          <div
            class="factor-bar-fill"
            :style="{ width: f.score + '%', background: barColor(f.score) }"
          ></div>
        </div>
        <div class="factor-meta">
          <span class="factor-detail">{{ f.detail }}</span>
          <span class="factor-score">
            {{ f.score }}/100 <span class="factor-contrib">→ +{{ f.contribution }}</span>
          </span>
        </div>
      </div>

      <p class="explainer-note">
        Trust Score is recalculated whenever new reviews arrive. Suspicious reviews detected by AI
        are excluded from the rating shown to you.
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  factors: { type: Array, default: () => [] }
})

const open = ref(true)

function barColor(score) {
  if (score >= 85) return '#16a34a'
  if (score >= 70) return '#65a30d'
  if (score >= 50) return '#f59e0b'
  return '#ef4444'
}
</script>

<style scoped>
.explainer {
  background: #fff;
  border: 1px solid var(--border-light, #ebeef5);
  border-radius: var(--radius-lg, 12px);
  overflow: hidden;
}

.explainer-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 18px;
  cursor: pointer;
  background: #fafafa;
}

.explainer-head:hover {
  background: #f5f7fa;
}

.explainer-title {
  display: block;
  font-weight: 700;
  font-size: var(--font-sm, 14px);
  color: var(--text, #303133);
}

.explainer-sub {
  display: block;
  font-size: var(--font-xs, 12px);
  color: var(--text-muted, #9ca3af);
  margin-top: 2px;
}

.explainer-toggle {
  font-size: var(--font-xs, 12px);
  color: var(--primary, #ff6b35);
  font-weight: 600;
  white-space: nowrap;
}

.explainer-body {
  padding: 16px 18px;
}

.factor {
  margin-bottom: 14px;
}

.factor-top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 5px;
}

.factor-name {
  font-weight: 600;
  font-size: var(--font-sm, 14px);
  color: var(--text, #303133);
}

.factor-weight {
  font-size: var(--font-xs, 12px);
  color: var(--text-muted, #9ca3af);
}

.factor-bar-track {
  height: 8px;
  background: #eef0f3;
  border-radius: 4px;
  overflow: hidden;
}

.factor-bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.5s ease;
}

.factor-meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-top: 5px;
}

.factor-detail {
  font-size: var(--font-xs, 12px);
  color: var(--text-secondary, #606266);
}

.factor-score {
  font-size: var(--font-xs, 12px);
  font-weight: 700;
  color: var(--text, #303133);
  white-space: nowrap;
}

.factor-contrib {
  color: var(--primary, #ff6b35);
}

.explainer-note {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px dashed var(--border-light, #ebeef5);
  font-size: var(--font-xs, 12px);
  color: var(--text-muted, #9ca3af);
  line-height: 1.6;
}
</style>
