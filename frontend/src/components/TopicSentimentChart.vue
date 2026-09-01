<template>
  <div v-if="topics.length" class="topics">
    <div class="topics-head">
      <h4>🗂️ What customers talk about</h4>
      <span class="topics-sub">AI-extracted topics (FR-040)</span>
    </div>

    <div v-for="t in topics" :key="t.topic" class="topic-row">
      <div class="topic-label">
        <span class="topic-name">{{ t.topic }}</span>
        <span class="topic-mentions">{{ t.mentions }} mention{{ t.mentions > 1 ? 's' : '' }}</span>
      </div>

      <div class="topic-track">
        <div
          class="topic-fill"
          :style="{ width: t.positiveRatio + '%', background: ratioColor(t.positiveRatio) }"
        ></div>
      </div>

      <span class="topic-ratio" :style="{ color: ratioColor(t.positiveRatio) }">
        {{ t.positiveRatio }}% positive
      </span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  topics: { type: Array, default: () => [] }
})

function ratioColor(ratio) {
  if (ratio >= 70) return '#16a34a'
  if (ratio >= 40) return '#f59e0b'
  return '#ef4444'
}
</script>

<style scoped>
.topics {
  background: #fff;
  border: 1px solid var(--border-light, #ebeef5);
  border-radius: var(--radius-lg, 12px);
  padding: 18px;
}

.topics-head {
  margin-bottom: 16px;
}

.topics-head h4 {
  font-weight: 700;
  font-size: var(--font-sm, 14px);
  color: var(--text, #303133);
  margin: 0;
}

.topics-sub {
  font-size: var(--font-xs, 12px);
  color: var(--text-muted, #9ca3af);
}

.topic-row {
  display: grid;
  grid-template-columns: 150px 1fr 100px;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.topic-label {
  display: flex;
  flex-direction: column;
}

.topic-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text, #303133);
}

.topic-mentions {
  font-size: 11px;
  color: var(--text-muted, #9ca3af);
}

.topic-track {
  height: 8px;
  background: #eef0f3;
  border-radius: 4px;
  overflow: hidden;
}

.topic-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.6s ease;
}

.topic-ratio {
  font-size: 12px;
  font-weight: 700;
  text-align: right;
}

@media (max-width: 576px) {
  .topic-row {
    grid-template-columns: 1fr;
    gap: 4px;
  }
  .topic-ratio {
    text-align: left;
  }
}
</style>
