<template>
  <footer class="site-footer">
    <div class="footer-main">
      <div class="footer-inner">
        <!-- Brand -->
        <div class="footer-brand">
          <router-link to="/" class="footer-logo">🔍 VeriCart AI</router-link>
          <p class="footer-tagline">
            AI-powered trust-driven e-commerce platform. Shop with confidence backed by multi-LLM review analysis, fake detection, and intelligent recommendations.
          </p>
          <div class="footer-social">
            <a href="#" title="Twitter"><el-icon><Share /></el-icon></a>
            <a href="#" title="GitHub"><el-icon><Connection /></el-icon></a>
            <a href="#" title="LinkedIn"><el-icon><Link /></el-icon></a>
          </div>
        </div>

        <!-- Quick Links -->
        <div class="footer-col">
          <h4>Shop</h4>
          <router-link to="/products">All Products</router-link>
          <router-link to="/products?sort=trust">Top Trusted</router-link>
          <router-link to="/recommendations">AI Recommendations</router-link>
          <router-link to="/cart">Shopping Cart</router-link>
        </div>

        <!-- Account -->
        <div class="footer-col">
          <h4>Account</h4>
          <router-link to="/orders">My Orders</router-link>
          <router-link to="/wishlist">Wishlist</router-link>
          <router-link to="/profile">Profile Settings</router-link>
          <router-link to="/login">Sign In</router-link>
        </div>

        <!-- About -->
        <div class="footer-col">
          <h4>About</h4>
          <a href="#">How It Works</a>
          <a href="#">Trust Score</a>
          <a href="#">AI Technology</a>
          <a href="#">Privacy Policy</a>
        </div>

        <!-- Contact -->
        <div class="footer-col">
          <h4>Contact</h4>
          <p><el-icon><Message /></el-icon> support@vericart.ai</p>
          <p><el-icon><Location /></el-icon> Beijing, China</p>
          <p class="footer-badge">🛡️ AI Trust Verified</p>
        </div>
      </div>
    </div>

    <!-- Bottom bar -->
    <div class="footer-bottom">
      <div class="footer-inner">
        <span>&copy; {{ year }} VeriCart AI — Team Algorists. All rights reserved.</span>
        <div class="footer-bottom-links">
          <span>Powered by DeepSeek · Qwen · Tencent Hunyuan</span>
          <span class="footer-separator">|</span>
          <span>Spring Boot + Vue.js 3</span>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
const year = new Date().getFullYear()
</script>

<style scoped>
.site-footer {
  background: #1F2937;
  color: #E5E7EB;
  margin-top: auto;
}

.footer-main {
  padding: 48px 24px 32px;
  border-bottom: 1px solid #374151;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  gap: 48px;
  flex-wrap: wrap;
}

.footer-brand {
  flex: 1 1 280px;
  min-width: 240px;
}

.footer-logo {
  font-size: 22px;
  font-weight: 800;
  color: white;
  text-decoration: none;
}

.footer-tagline {
  color: #9CA3AF;
  font-size: 14px;
  line-height: 1.7;
  margin-top: 12px;
  max-width: 340px;
}

.footer-social {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.footer-social a {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #374151;
  color: #D1D5DB;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s, color 0.2s;
  text-decoration: none;
}

.footer-social a:hover {
  background: var(--primary);
  color: white;
}

.footer-col {
  flex: 0 0 160px;
}

.footer-col h4 {
  color: white;
  font-size: 14px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 16px;
}

.footer-col a {
  display: block;
  color: #9CA3AF;
  font-size: 14px;
  padding: 4px 0;
  text-decoration: none;
  transition: color 0.2s;
}

.footer-col a:hover {
  color: white;
}

.footer-col p {
  color: #9CA3AF;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 0;
}

.footer-badge {
  margin-top: 8px;
  padding: 6px 12px;
  background: #374151;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #10B981;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.footer-bottom {
  padding: 16px 24px;
  background: #111827;
}

.footer-bottom .footer-inner {
  font-size: 13px;
  color: #6B7280;
  justify-content: space-between;
  align-items: center;
}

.footer-bottom-links {
  display: flex;
  gap: 8px;
  align-items: center;
}

.footer-separator {
  color: #374151;
}

@media (max-width: 768px) {
  .footer-inner {
    gap: 24px;
  }
  .footer-col {
    flex: 1 1 140px;
  }
  .footer-bottom .footer-inner {
    flex-direction: column;
    gap: 4px;
    text-align: center;
  }
  .footer-bottom-links {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
