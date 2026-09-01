<template>
  <div v-if="active" class="pipeline">
    <p class="pipeline-title">🤖 AI is analysing this review…</p>

    <div class="pipeline-steps">
      <div
        v-for="(step, idx) in steps"
        :key="step.model"
        :class="['pipeline-step', stepState(idx)]"
      >
        <span class="pipeline-dot">{{ stepState(idx) === 'done' ? '✓' : idx + 1 }}</span>
        <div class="pipeline-text">
          <span class="pipeline-model">{{ step.model }}</span>
          <span class="pipeline-task">{{ step.task }}</span>
        </div>
      </div>

      <div class="pipeline-connector"></div>
    </div>

    <p class="pipeline-foot">{{ statusText }}</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  active: { type: Boolean, default: false },
  currentStep: { type: Number, default: 0 }
})

const steps = [
  { model: 'DeepSeek', task: 'Fake-review detection & authenticity' },
  { model: 'Qwen', task: 'Sentiment & emotion analysis' },
  { model: 'Hunyuan', task: 'Review summarisation' }
]

function stepState(idx) {
  if (idx < props.currentStep) return 'done'
  if (idx === props.currentStep) return 'active'
  return 'pending'
}

const statusText = computed(() => {
  if (props.currentStep >= steps.length) return 'Analysis complete — Trust Score updated'
  return `Running ${steps[props.currentStep].model}…`
})
</script>

<style scoped>
.pipeline {
  background: linear-gradient(135deg, #fff7ed 0%, #fff 100%);
  border: 1px solid var(--border-light, #ebeef5);
  border-radius: var(--radius-lg, 12px);
  padding: 18px;
}

.pipeline-title {
  font-weight: 700;
  font-size: var(--font-sm, 14px);
  color: var(--text, #303133);
  margin-bottom: 14px;
}

.pipeline-steps {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex-wrap: wrap;
}

.pipeline-step {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid var(--border-light, #ebeef5);
  opacity: 0.45;
  transition: opacity 0.3s, transform 0.3s, border-color 0.3s;
  flex: 1;
  min-width: 150px;
}

.pipeline-step.active {
  opacity: 1;
  border-color: var(--primary, #ff6b35);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.15);
}

.pipeline-step.done {
  opacity: 1;
  border-color: #16a34a;
}

.pipeline-dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
  background: #eef0f3;
  color: var(--text-secondary, #606266);
  flex-shrink: 0;
}

.pipeline-step.active .pipeline-dot {
  background: var(--primary, #ff6b35);
  color: #fff;
  animation: pulse 1.2s infinite;
}

.pipeline-step.done .pipeline-dot {
  background: #16a34a;
  color: #fff;
}

.pipeline-text {
  display: flex;
  flex-direction: column;
}

.pipeline-model {
  font-weight: 700;
  font-size: 13px;
  color: var(--text, #303133);
}

.pipeline-task {
  font-size: 11px;
  color: var(--text-muted, #9ca3af);
}

.pipeline-foot {
  margin-top: 14px;
  font-size: var(--font-xs, 12px);
  color: var(--text-secondary, #606266);
  font-weight: 600;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.15); }
}
</style>
