<template>
  <div class="info-page">
    <div class="info-hero">
      <h1>How VeriCart AI Works</h1>
      <p>From product search to verified purchase — every step is backed by AI-powered trust checks.</p>
    </div>

    <div class="page-container">
      <div class="steps">
        <div v-for="(step, i) in steps" :key="i" class="step-card">
          <div class="step-number">{{ i + 1 }}</div>
          <div class="step-icon">{{ step.icon }}</div>
          <h3>{{ step.title }}</h3>
          <p>{{ step.text }}</p>
        </div>
      </div>

      <div class="info-section">
        <h2>What makes us different</h2>
        <div class="feature-grid">
          <div v-for="f in features" :key="f.title" class="feature-item">
            <h4>{{ f.title }}</h4>
            <p>{{ f.text }}</p>
          </div>
        </div>
      </div>

      <div class="cta-box">
        <h3>Ready to shop with confidence?</h3>
        <el-button type="primary" size="large" @click="$router.push('/products')">Browse Products</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
const steps = [
  { icon: '🔍', title: 'Search & Discover', text: 'Browse a curated catalogue of 20+ products. Each listing shows its live AI Trust Score, so you can compare items on trustworthiness as well as price.' },
  { icon: '🤖', title: 'AI Review Analysis', text: 'When you open a product, our multi-LLM pipeline (DeepSeek + Qwen + Hunyuan) analyses every review — detecting fake or paid reviews, extracting key topics like battery life or build quality, and summarising what real buyers say.' },
  { icon: '🛡️', title: 'Trusted Rating', text: 'Fake reviews are removed from the calculation. You see both the raw average rating and the AI-trusted rating, so a product can never inflate its score with purchased reviews.' },
  { icon: '🛒', title: 'Shop Securely', text: 'Add to cart, check out with simulated payment, and track your order status through fulfilment. Verified purchases get a special badge on their reviews.' },
  { icon: '⭐', title: 'Review with Impact', text: 'After your order is delivered, submit your own review. The AI analyses it in seconds, and if you bought the product, your review is marked as a verified purchase.' },
  { icon: '🔔', title: 'Stay Informed', text: 'Get notified when an order changes status, when your product receives a new review, or when the AI flags suspicious activity on products you care about.' }
]

const features = [
  { title: 'Explainable AI', text: 'Every trust score comes with a breakdown — five weighted factors with plain-English reasons, not a black box.' },
  { title: 'Real-time Analysis', text: 'Reviews are analysed on demand, so new feedback is reflected in trust scores immediately.' },
  { title: 'Multi-LLM Redundancy', text: 'Three independent AI providers cross-check each other, with automatic fallback if one is unavailable.' },
  { title: 'Transparency', text: 'You can see exactly which reviews were flagged as suspicious and why the AI thought so.' }
]
</script>

<style scoped>
.info-page { min-height: 70vh; }
.info-hero {
  background: linear-gradient(135deg, #1e3a8a, #0ea5e9);
  color: white; text-align: center;
  padding: 64px 24px;
}
.info-hero h1 { font-size: 36px; margin-bottom: 12px; }
.info-hero p { font-size: 17px; opacity: 0.92; max-width: 620px; margin: 0 auto; }
.steps { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 20px; margin: 40px 0; }
.step-card {
  background: white; border-radius: 14px; padding: 28px 22px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06); position: relative;
  border-top: 4px solid var(--primary);
}
.step-number {
  position: absolute; top: 14px; right: 18px;
  font-size: 44px; font-weight: 800; color: #E5E7EB;
}
.step-icon { font-size: 36px; margin-bottom: 10px; }
.step-card h3 { font-size: 17px; margin-bottom: 8px; }
.step-card p { color: #6B7280; font-size: 14px; line-height: 1.7; }
.info-section { margin: 32px 0 16px; }
.info-section h2 { font-size: 26px; text-align: center; margin-bottom: 24px; }
.feature-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 16px; }
.feature-item { background: #F9FAFB; border-radius: 12px; padding: 20px; border: 1px solid #E5E7EB; }
.feature-item h4 { color: var(--primary); margin-bottom: 6px; }
.feature-item p { color: #6B7280; font-size: 14px; line-height: 1.7; }
.cta-box {
  margin: 40px 0 56px; text-align: center; padding: 40px;
  background: linear-gradient(135deg, #0F172A, #1e3a8a);
  border-radius: 16px; color: white;
}
.cta-box h3 { font-size: 22px; margin-bottom: 18px; }
</style>
