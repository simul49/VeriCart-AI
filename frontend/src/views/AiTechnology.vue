<template>
  <div class="info-page">
    <div class="info-hero">
      <h1>AI Technology Behind VeriCart</h1>
      <p>A multi-LLM gateway combining three independent AI providers for maximum reliability.</p>
    </div>

    <div class="page-container">
      <div class="pipeline">
        <h2>The analysis pipeline</h2>
        <div class="pipe-row">
          <div v-for="(step, i) in pipeline" :key="i" class="pipe-step">
            <div class="pipe-icon">{{ step.icon }}</div>
            <h4>{{ step.model }}</h4>
            <p>{{ step.role }}</p>
            <div v-if="i < pipeline.length - 1" class="pipe-arrow">→</div>
          </div>
        </div>
      </div>

      <div class="tasks">
        <h2>What the AI does</h2>
        <div class="task-grid">
          <div v-for="t in tasks" :key="t.title" class="task-card">
            <div class="task-icon">{{ t.icon }}</div>
            <h4>{{ t.title }}</h4>
            <p>{{ t.text }}</p>
          </div>
        </div>
      </div>

      <div class="info-section">
        <h2>Why three models?</h2>
        <div class="feature-grid">
          <div v-for="f in features" :key="f.title" class="feature-item">
            <h4>{{ f.title }}</h4>
            <p>{{ f.text }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const pipeline = [
  { icon: '🧠', model: 'DeepSeek', role: 'Sentiment analysis & fake-review detection. The core judge of review authenticity.' },
  { icon: '🌐', model: 'Qwen (Alibaba)', role: 'Topic extraction and positive-ratio scoring across the whole review set.' },
  { icon: '🔄', model: 'Hunyuan (Tencent)', role: 'Fallback and cross-checking. Steps in automatically if another provider fails.' }
]

const tasks = [
  { icon: '🎭', title: 'Fake Review Detection', text: 'Scans for spam patterns, generic praise, keyword stuffing and inconsistencies to flag purchased or fake reviews.' },
  { icon: '💬', title: 'Sentiment Analysis', text: 'Classifies each review as positive, neutral or negative, and detects emotions like anger or satisfaction.' },
  { icon: '📊', title: 'Topic Extraction', text: 'Discovers what buyers actually talk about — battery life, price, comfort, delivery — and how positive each topic is.' },
  { icon: '🏆', title: 'Trust Scoring', text: 'Combines five weighted factors into an explainable 0–100 score with plain-English reasoning.' },
  { icon: '🧾', title: 'AI Summaries', text: 'Generates a concise summary of what real buyers say, so you do not have to read every review.' },
  { icon: '💡', title: 'Recommendations', text: 'Suggests products based on your browsing and purchase history.' }
]

const features = [
  { title: 'Redundancy', text: 'If one LLM provider is unavailable or returns a bad result, another takes over automatically — no downtime for AI features.' },
  { title: 'Cross-checking', text: 'Independent models verify each other, reducing the chance of a single model being fooled by sophisticated fake reviews.' },
  { title: 'Explainability', text: 'Every AI decision is surfaced to the user with reasons — nothing is hidden inside a black box.' },
  { title: 'Mock Mode for Demo', text: 'For development and demos, an offline heuristic engine simulates all three providers, so the full pipeline works without API keys.' }
]
</script>

<style scoped>
.info-page { min-height: 70vh; }
.info-hero {
  background: linear-gradient(135deg, #312e81, #7c3aed);
  color: white; text-align: center; padding: 64px 24px;
}
.info-hero h1 { font-size: 36px; margin-bottom: 12px; }
.info-hero p { font-size: 17px; opacity: 0.92; max-width: 640px; margin: 0 auto; }
.pipeline, .tasks, .info-section { margin: 40px 0; }
.pipeline h2, .tasks h2, .info-section h2 { font-size: 24px; margin-bottom: 20px; text-align: center; }
.pipe-row { display: flex; justify-content: center; gap: 16px; flex-wrap: wrap; }
.pipe-step {
  background: white; border-radius: 14px; padding: 24px; width: 240px;
  text-align: center; box-shadow: 0 2px 12px rgba(0,0,0,0.06); position: relative;
}
.pipe-icon { font-size: 40px; margin-bottom: 10px; }
.pipe-step h4 { color: var(--primary); margin-bottom: 8px; }
.pipe-step p { color: #6B7280; font-size: 13px; line-height: 1.7; }
.pipe-arrow { position: absolute; right: -18px; top: 50%; transform: translateY(-50%); font-size: 22px; color: #9CA3AF; z-index: 2; }
.task-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; }
.task-card { background: #F9FAFB; border: 1px solid #E5E7EB; border-radius: 12px; padding: 22px; }
.task-icon { font-size: 30px; margin-bottom: 8px; }
.task-card h4 { margin-bottom: 6px; }
.task-card p { color: #6B7280; font-size: 14px; line-height: 1.7; }
.feature-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 16px; }
.feature-item { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
.feature-item h4 { color: var(--primary); margin-bottom: 6px; }
.feature-item p { color: #6B7280; font-size: 14px; line-height: 1.7; }
</style>
