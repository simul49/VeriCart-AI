<template>
  <footer class="site-footer">
    <div class="footer-inner">
      <!-- Brand + newsletter -->
      <div class="footer-brand">
        <router-link to="/" class="footer-logo">
          <BrandMark :size="28" />
          VeriCart<span class="brand-ai">AI</span>
        </router-link>

        <p class="footer-tagline">{{ $t('footer.tagline') }}</p>

        <form class="newsletter" @submit.prevent="subscribe">
          <el-icon><Message /></el-icon>
          <input
            v-model="email"
            type="email"
            :placeholder="$t('footer.newsletterPlaceholder')"
            :aria-label="$t('auth.email')"
          />
          <button type="submit" :disabled="!email">{{ $t('footer.join') }}</button>
        </form>
        <p v-if="subscribed" class="newsletter-ok">
          <el-icon><CircleCheckFilled /></el-icon> {{ $t('footer.newsletterOk') }}
        </p>

        <div class="footer-social">
          <a href="#" aria-label="Share"><el-icon><Share /></el-icon></a>
          <a href="#" aria-label="GitHub"><el-icon><Link /></el-icon></a>
          <a href="#" aria-label="Community"><el-icon><Connection /></el-icon></a>
        </div>
      </div>

      <!-- Link columns -->
      <div class="footer-cols">
        <div class="footer-col">
          <h4>{{ $t('footer.shop') }}</h4>
          <router-link to="/products">{{ $t('footer.allProducts') }}</router-link>
          <router-link to="/products?sort=trust">{{ $t('footer.topTrusted') }}</router-link>
          <router-link to="/recommendations">{{ $t('footer.aiRecommendations') }}</router-link>
          <router-link to="/compare">{{ $t('footer.compareProducts') }}</router-link>
          <router-link to="/cart">{{ $t('footer.shoppingCart') }}</router-link>
        </div>

        <div class="footer-col">
          <h4>{{ $t('footer.account') }}</h4>
          <router-link to="/orders">{{ $t('footer.myOrders') }}</router-link>
          <router-link to="/wishlist">{{ $t('footer.wishlist') }}</router-link>
          <router-link to="/messages">{{ $t('footer.messages') }}</router-link>
          <router-link to="/notifications">{{ $t('footer.alerts') }}</router-link>
          <router-link to="/profile">{{ $t('footer.profileSettings') }}</router-link>
        </div>

        <div class="footer-col">
          <h4>{{ $t('footer.about') }}</h4>
          <router-link to="/how-it-works">{{ $t('footer.howItWorks') }}</router-link>
          <router-link to="/trust-score">{{ $t('footer.trustScore') }}</router-link>
          <router-link to="/ai-technology">{{ $t('footer.aiTechnology') }}</router-link>
          <router-link to="/seller">{{ $t('footer.sellOnVericart') }}</router-link>
        </div>

        <div class="footer-col">
          <h4>{{ $t('footer.support') }}</h4>
          <p><el-icon><Message /></el-icon> {{ $t('footer.contactEmail') }}</p>
          <p><el-icon><Location /></el-icon> {{ $t('footer.contactLocation') }}</p>
          <p><el-icon><Clock /></el-icon> {{ $t('footer.contactHours') }}</p>
          <p class="footer-badge">
            <el-icon><Lock /></el-icon> {{ $t('footer.trustVerified') }}
          </p>
        </div>
      </div>
    </div>

    <!-- Bottom bar -->
    <div class="footer-bottom">
      <div class="footer-bottom-inner">
        <span>{{ $t('footer.rights', { year }) }}</span>
        <div class="footer-bottom-links">
          <span>{{ $t('footer.poweredBy') }}</span>
          <span class="footer-separator">·</span>
          <span>{{ $t('footer.builtWith') }}</span>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import BrandMark from './BrandMark.vue'

const { t } = useI18n()
const year = new Date().getFullYear()
const email = ref('')
const subscribed = ref(false)

function subscribe() {
  if (!email.value.includes('@')) {
    ElMessage.warning(t('footer.newsletterInvalid'))
    return
  }
  subscribed.value = true
  email.value = ''
}
</script>

<style scoped>
.site-footer {
  background: var(--ink);
  color: #CBD5E1;
  margin-top: auto;
}

.footer-inner {
  max-width: var(--maxw);
  margin: 0 auto;
  padding: 56px 24px 40px;
  display: flex;
  gap: 56px;
  flex-wrap: wrap;
}

/* ---- Brand ---- */
.footer-brand { flex: 1 1 300px; min-width: 260px; }
.footer-logo {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  font-size: 20px;
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.03em;
}
.brand-ai { color: var(--primary); margin-left: 2px; }

.footer-tagline {
  color: #94A3B8;
  font-size: var(--font-sm);
  line-height: 1.75;
  margin-top: 14px;
  max-width: 360px;
}

/* ---- Newsletter ---- */
.newsletter {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 20px;
  max-width: 340px;
  height: 44px;
  padding: 0 5px 0 14px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: var(--radius-full);
  transition: border-color var(--transition);
}
.newsletter:focus-within { border-color: var(--primary); }
.newsletter .el-icon { color: #64748B; }
.newsletter input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  background: transparent;
  color: #fff;
  font-size: var(--font-sm);
  font-family: inherit;
}
.newsletter input::placeholder { color: #64748B; }
.newsletter button {
  height: 34px;
  padding: 0 18px;
  border: none;
  border-radius: var(--radius-full);
  background: var(--primary);
  color: #fff;
  font-size: var(--font-sm);
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
  transition: background var(--transition);
}
.newsletter button:hover:not(:disabled) { background: var(--primary-hover); }
.newsletter button:disabled { opacity: 0.5; cursor: not-allowed; }
.newsletter-ok {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
  font-size: var(--font-xs);
  color: var(--success);
}

/* ---- Social ---- */
.footer-social { display: flex; gap: 10px; margin-top: 22px; }
.footer-social a {
  width: 38px;
  height: 38px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #94A3B8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  transition: all var(--transition);
}
.footer-social a:hover {
  background: var(--primary);
  border-color: var(--primary);
  color: #fff;
  transform: translateY(-2px);
}

/* ---- Columns ---- */
.footer-cols {
  flex: 2 1 560px;
  display: flex;
  gap: 40px;
  flex-wrap: wrap;
  justify-content: space-between;
}
.footer-col { min-width: 140px; }
.footer-col h4 {
  color: #fff;
  font-size: var(--font-xs);
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.09em;
  margin-bottom: 16px;
}
.footer-col a,
.footer-col p {
  display: flex;
  align-items: center;
  gap: 7px;
  color: #94A3B8;
  font-size: var(--font-sm);
  padding: 6px 0;
  transition: color var(--transition);
}
.footer-col a:hover { color: #fff; }
.footer-col .el-icon { font-size: 15px; }

.footer-badge {
  margin-top: 12px;
  padding: 7px 13px;
  background: rgba(16, 185, 129, 0.12);
  border: 1px solid rgba(16, 185, 129, 0.28);
  border-radius: var(--radius-sm);
  font-size: var(--font-xs);
  font-weight: 700;
  color: #34D399;
  display: inline-flex;
}

/* ---- Bottom ---- */
.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.09);
  background: #070C16;
}
.footer-bottom-inner {
  max-width: var(--maxw);
  margin: 0 auto;
  padding: 18px 24px;
  font-size: var(--font-xs);
  color: #64748B;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.footer-bottom-links { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.footer-separator { color: #334155; }

@media (max-width: 768px) {
  .footer-inner { padding: 40px 20px 28px; gap: 32px; }
  .footer-cols { gap: 24px; }
  .footer-col { flex: 1 1 130px; }
  .footer-bottom-inner { flex-direction: column; text-align: center; }
  .footer-bottom-links { justify-content: center; }
}
</style>
