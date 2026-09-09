<template>
  <div class="ts-page">
    <!-- Hero -->
    <header class="ts-hero">
      <div class="ts-hero-inner">
        <div class="ts-hero-copy">
          <span class="eyebrow">AI Transparency Engine</span>
          <h1>The AI Trust Score</h1>
          <p>A single 0&ndash;100 score that tells you how much a product&rsquo;s reviews can really be believed &mdash; so you shop with confidence, not guesswork.</p>
          <div class="ts-hero-stats">
            <div class="ts-stat"><span class="ts-stat-num">5</span><span class="ts-stat-label">weighted factors</span></div>
            <div class="ts-stat"><span class="ts-stat-num">0&ndash;100</span><span class="ts-stat-label">clear scale</span></div>
            <div class="ts-stat"><span class="ts-stat-num">100%</span><span class="ts-stat-label">AI-verified</span></div>
          </div>
        </div>
        <div class="ts-gauge" aria-hidden="true">
          <div class="ts-gauge-ring" :style="{ '--val': sampleScore }">
            <div class="ts-gauge-inner">
              <span class="ts-gauge-num">{{ sampleScore }}</span>
              <span class="ts-gauge-label">Trust Score</span>
            </div>
          </div>
          <span class="ts-gauge-cap">{{ sampleLevel }} &mdash; example</span>
        </div>
      </div>
    </header>

    <div class="ts-container">
      <!-- How it works -->
      <section class="section-head">
        <span class="eyebrow">How it works</span>
        <h2>From raw reviews to a score you can trust</h2>
        <p class="section-subtitle">Three steps run automatically for every product, every time new reviews arrive.</p>
      </section>
      <div class="ts-steps">
        <div class="ts-step">
          <span class="ts-step-num">1</span>
          <h3>Analyze every review</h3>
          <p>Our AI reads each review twice &mdash; <b>DeepSeek</b> flags fake or paid reviews, while <b>Kimi</b> reads the sentiment behind the words.</p>
        </div>
        <div class="ts-step">
          <span class="ts-step-num">2</span>
          <h3>Weight the signals</h3>
          <p>Five factors are scored and blended using the weights below into one honest 0&ndash;100 number.</p>
        </div>
        <div class="ts-step">
          <span class="ts-step-num">3</span>
          <h3>Show the real rating</h3>
          <p>Reviews the AI flags as suspicious are removed before your visible rating is calculated.</p>
        </div>
      </div>

      <!-- Factors -->
      <section class="section-head ts-mt">
        <span class="eyebrow">The formula</span>
        <h2>How the score is calculated</h2>
        <p class="section-subtitle">Five weighted factors are scored by the AI, then combined into a single 0&ndash;100 score.</p>
      </section>
      <div class="ts-factors">
        <div v-for="f in factors" :key="f.name" class="ts-factor">
          <div class="ts-factor-top">
            <span class="ts-factor-name">{{ f.name }}</span>
            <span class="ts-weight">{{ f.weight }}%</span>
          </div>
          <div class="ts-bar"><div class="ts-bar-fill" :style="{ width: f.weight + '%' }"></div></div>
          <p class="ts-factor-desc">{{ f.desc }}</p>
        </div>
      </div>

      <!-- Levels -->
      <section class="section-head ts-mt">
        <span class="eyebrow">Reading the score</span>
        <h2>What the levels mean</h2>
        <p class="section-subtitle">Every product lands in one of four bands.</p>
      </section>
      <div class="ts-levels">
        <div v-for="l in levels" :key="l.range" class="ts-level" :style="{ '--lvl': l.color }">
          <span class="ts-level-bar"></span>
          <div class="ts-level-body">
            <span class="ts-level-range">{{ l.range }}</span>
            <p>{{ l.text }}</p>
          </div>
        </div>
      </div>

      <!-- Raw vs Trusted -->
      <section class="section-head ts-mt">
        <span class="eyebrow">Why it matters</span>
        <h2>Trusted Rating vs Raw Rating</h2>
        <p class="section-subtitle">The same product, before and after the AI removes suspicious reviews.</p>
      </section>
      <div class="ts-compare">
        <div class="ts-rating ts-rating--raw">
          <span class="ts-rating-tag">Raw rating</span>
          <div class="ts-rating-num">4.33</div>
          <p>The simple average of all review stars &mdash; including any fake or paid reviews that slipped through.</p>
        </div>
        <div class="ts-arrow" aria-hidden="true"><el-icon><Right /></el-icon></div>
        <div class="ts-rating ts-rating--trusted">
          <span class="ts-rating-tag"><el-icon><Check /></el-icon> Trusted rating</span>
          <div class="ts-rating-num">{{ trustedExample }}</div>
          <p>The average <b>after the AI removes {{ removedExample }} flagged suspicious reviews</b>. This is the honest number you see.</p>
        </div>
      </div>

      <p class="ts-foot">Every Trust Score is recalculated automatically whenever new reviews arrive, so the number you see is always current.</p>
    </div>
  </div>
</template>

<script setup>
const sampleScore = 87
const sampleLevel = 'High'
const trustedExample = '4.11'
const removedExample = 2

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
.ts-page { min-height: 70vh; }

/* ---------- Hero ---------- */
.ts-hero {
  background: var(--primary-grad);
  color: #fff;
  padding: 76px 24px;
}
.ts-hero-inner {
  max-width: 1080px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 48px;
  flex-wrap: wrap;
}
.ts-hero-copy { flex: 1; min-width: 280px; }
.ts-hero .eyebrow {
  color: #fff;
  background: rgba(255, 255, 255, 0.16);
  padding: 5px 12px;
  border-radius: var(--radius-full);
  display: inline-block;
}
.ts-hero h1 {
  font-size: var(--font-4xl);
  font-weight: 800;
  margin: 14px 0 14px;
  letter-spacing: -0.02em;
}
.ts-hero p {
  font-size: var(--font-lg);
  color: rgba(255, 255, 255, 0.92);
  max-width: 520px;
  line-height: 1.7;
}
.ts-hero-stats { display: flex; gap: 34px; margin-top: 28px; flex-wrap: wrap; }
.ts-stat { display: flex; flex-direction: column; }
.ts-stat-num { font-size: var(--font-2xl); font-weight: 800; line-height: 1.1; }
.ts-stat-label {
  font-size: var(--font-xs);
  color: rgba(255, 255, 255, 0.82);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-top: 4px;
}

/* ---------- Gauge ---------- */
.ts-gauge { text-align: center; }
.ts-gauge-ring {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: conic-gradient(#ffffff calc(var(--val) * 1%), rgba(255, 255, 255, 0.16) 0);
  display: flex;
  align-items: center;
  justify-content: center;
}
.ts-gauge-inner {
  width: 158px;
  height: 158px;
  border-radius: 50%;
  background: rgba(15, 23, 42, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.ts-gauge-num { font-size: 52px; font-weight: 800; line-height: 1; letter-spacing: -0.02em; }
.ts-gauge-label {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: rgba(255, 255, 255, 0.78);
  margin-top: 4px;
}
.ts-gauge-cap {
  display: block;
  margin-top: 14px;
  font-size: var(--font-sm);
  color: rgba(255, 255, 255, 0.88);
  font-weight: 600;
}

/* ---------- Layout ---------- */
.ts-container {
  max-width: 1080px;
  margin: 0 auto;
  padding: 56px 24px 80px;
}
.ts-mt { margin-top: 60px; }

/* ---------- Steps ---------- */
.ts-steps {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 20px;
}
.ts-step {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 26px;
  box-shadow: var(--shadow-xs);
}
.ts-step-num {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--primary-grad);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 18px;
  margin-bottom: 14px;
  box-shadow: 0 6px 16px var(--primary-glow);
}
.ts-step h3 { font-size: var(--font-lg); font-weight: 700; margin-bottom: 8px; }
.ts-step p { color: var(--text-secondary); font-size: var(--font-sm); line-height: 1.65; }

/* ---------- Factors ---------- */
.ts-factors {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 18px;
}
.ts-factor {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 22px;
  box-shadow: var(--shadow-xs);
}
.ts-factor-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}
.ts-factor-name { font-weight: 700; font-size: var(--font-md); color: var(--text); }
.ts-weight {
  font-size: var(--font-xs);
  font-weight: 700;
  color: var(--primary-dark);
  background: var(--primary-light);
  padding: 4px 11px;
  border-radius: var(--radius-full);
  white-space: nowrap;
}
.ts-bar {
  height: 8px;
  background: var(--surface-muted);
  border-radius: var(--radius-full);
  overflow: hidden;
  margin: 12px 0 14px;
}
.ts-bar-fill {
  height: 100%;
  background: var(--primary-grad);
  border-radius: var(--radius-full);
}
.ts-factor-desc { color: var(--text-secondary); font-size: var(--font-sm); line-height: 1.6; margin: 0; }

/* ---------- Levels ---------- */
.ts-levels {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}
.ts-level {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
  display: flex;
}
.ts-level-bar { width: 6px; background: var(--lvl); flex-shrink: 0; }
.ts-level-body { padding: 18px 20px; }
.ts-level-range { font-size: 18px; font-weight: 800; color: var(--lvl); }
.ts-level-body p { color: var(--text-secondary); font-size: var(--font-sm); line-height: 1.6; margin: 6px 0 0; }

/* ---------- Compare ---------- */
.ts-compare {
  display: flex;
  align-items: stretch;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}
.ts-rating {
  width: 320px;
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  padding: 28px;
  box-shadow: var(--shadow-sm);
}
.ts-rating--trusted {
  border-color: var(--primary-soft);
  box-shadow: 0 14px 34px var(--primary-glow);
}
.ts-rating-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: var(--font-sm);
  font-weight: 700;
  color: var(--text-secondary);
}
.ts-rating-num { font-size: 48px; font-weight: 800; margin: 10px 0; letter-spacing: -0.02em; }
.ts-rating--raw .ts-rating-num { color: var(--text-muted); }
.ts-rating--trusted .ts-rating-num { color: var(--primary); }
.ts-rating p { color: var(--text-secondary); font-size: var(--font-sm); line-height: 1.65; }
.ts-arrow {
  display: flex;
  align-items: center;
  font-size: 28px;
  color: var(--primary);
}

.ts-foot {
  text-align: center;
  color: var(--text-muted);
  font-size: var(--font-sm);
  margin-top: 56px;
  padding-top: 24px;
  border-top: 1px solid var(--border-light);
}

@media (max-width: 640px) {
  .ts-hero { padding: 48px 18px; }
  .ts-hero h1 { font-size: 30px; }
  .ts-gauge { margin: 8px auto 0; }
  .ts-rating { width: 100%; }
}
</style>
