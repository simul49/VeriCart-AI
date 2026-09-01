<template>
  <div class="info-page">
    <div class="info-hero">
      <h1>The AI Trust Score</h1>
      <p>A 0–100 score that tells you how much a product&rsquo;s reviews can be believed.</p>
    </div>

    <div class="page-container">
      <div class="score-formula">
        <h2>How the score is calculated</h2>
        <p class="formula-sub">Five weighted factors are scored by the AI, then combined into a single 0–100 score.</p>

        <div class="factors">
          <div v-for="f in factors" :key="f.name" class="factor-row">
            <div class="factor-head">
              <span class="factor-name">{{ f.name }}</span>
              <el-tag size="small" type="primary">{{ f.weight }}% weight</el-tag>
            </div>
            <el-progress :percentage="f.weight" :show-text="false" :stroke-width="10" />
            <p class="factor-desc">{{ f.desc }}</p>
          </div>
        </div>
      </div>

      <div class="levels">
        <h2>What the levels mean</h2>
        <div class="level-grid">
          <div v-for="l in levels" :key="l.range" class="level-card" :style="{ borderColor: l.color }">
            <h3 :style="{ color: l.color }">{{ l.range }}</h3>
            <p>{{ l.text }}</p>
          </div>
        </div>
      </div>

      <div class="info-section">
        <h2>Trusted Rating vs Raw Rating</h2>
        <div class="rating-compare">
          <div class="rating-box raw">
            <h4>Raw Rating</h4>
            <div class="big-rating">4.33</div>
            <p>The simple average of all review stars — including any fake or paid reviews.</p>
          </div>
          <div class="arrow">→</div>
          <div class="rating-box trusted">
            <h4>Trusted Rating</h4>
            <div class="big-rating">4.11</div>
            <p>The average <b>after the AI removes flagged suspicious reviews</b>. This is the honest number.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const factors = [
  { name: 'Review Authenticity', weight: 30, desc: 'What share of reviews pass AI fake-detection. Too many suspicious reviews collapses the score.' },
  { name: 'Customer Sentiment', weight: 30, desc: 'The overall tone of genuine reviews — how positive buyers really are.' },
  { name: 'Verified Purchases', weight: 20, desc: 'The proportion of reviews linked to confirmed orders. Verified feedback counts more.' },
  { name: 'Review Volume', weight: 10, desc: 'How many reviews exist. A handful of reviews is less statistically reliable than hundreds.' },
  { name: 'Rating Consistency', weight: 10, desc: 'Are ratings consistent? Wildly mixed opinions can indicate manipulation.' }
]

const levels = [
  { range: '80 – 100', text: 'Excellent — highly authentic reviews, strongly positive sentiment.', color: '#059669' },
  { range: '60 – 79', text: 'High — generally trustworthy, with a few flagged reviews.', color: '#2563eb' },
  { range: '40 – 59', text: 'Medium — mixed signals; read individual reviews carefully.', color: '#d97706' },
  { range: '0 – 39', text: 'Low — many suspicious reviews or poor sentiment; proceed with caution.', color: '#dc2626' }
]
</script>

<style scoped>
.info-page { min-height: 70vh; }
.info-hero {
  background: linear-gradient(135deg, #0f172a, #475569);
  color: white; text-align: center; padding: 64px 24px;
}
.info-hero h1 { font-size: 36px; margin-bottom: 12px; }
.info-hero p { font-size: 17px; opacity: 0.92; max-width: 600px; margin: 0 auto; }
.score-formula { margin: 40px 0; }
.score-formula h2, .levels h2, .info-section h2 { font-size: 24px; margin-bottom: 8px; }
.formula-sub { color: #6B7280; margin-bottom: 24px; }
.factors { display: flex; flex-direction: column; gap: 18px; }
.factor-row { background: white; border-radius: 12px; padding: 16px 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
.factor-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.factor-name { font-weight: 600; font-size: 15px; }
.factor-desc { color: #6B7280; font-size: 13px; margin-top: 8px; }
.levels { margin: 40px 0; }
.level-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 14px; margin-top: 16px; }
.level-card { background: white; border-radius: 12px; padding: 20px; border: 2px solid; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
.level-card h3 { font-size: 22px; margin-bottom: 6px; }
.level-card p { color: #6B7280; font-size: 14px; line-height: 1.6; }
.info-section { margin: 40px 0 56px; }
.info-section h2 { text-align: center; }
.rating-compare { display: flex; align-items: center; justify-content: center; gap: 24px; margin-top: 20px; flex-wrap: wrap; }
.rating-box { background: white; border-radius: 14px; padding: 28px 32px; text-align: center; box-shadow: 0 2px 12px rgba(0,0,0,0.07); width: 240px; }
.rating-box h4 { margin-bottom: 10px; color: #374151; }
.big-rating { font-size: 40px; font-weight: 800; margin-bottom: 8px; }
.raw .big-rating { color: #9CA3AF; }
.trusted .big-rating { color: var(--primary); }
.rating-box p { color: #6B7280; font-size: 13px; line-height: 1.6; }
.arrow { font-size: 32px; color: #9CA3AF; }
</style>
