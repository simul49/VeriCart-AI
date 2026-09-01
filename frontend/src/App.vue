<template>
  <div id="app-root">
    <!-- ============ Announcement bar ============ -->
    <div class="announce-bar" v-if="!hideNavbar">
      <div class="announce-inner">
        <span class="announce-item">
          <el-icon><CircleCheckFilled /></el-icon> {{ $t('nav.aiVerified') }}
        </span>
        <span class="announce-sep" />
        <span class="announce-item">
          <el-icon><Van /></el-icon> {{ $t('nav.freeShipping') }}
        </span>
        <span class="announce-sep" />
        <span class="announce-item">
          <el-icon><Headset /></el-icon> {{ $t('nav.assistant247') }}
        </span>
        <span class="announce-right">
          <router-link to="/how-it-works">{{ $t('nav.howTrustWorks') }}</router-link>
        </span>
      </div>
    </div>

    <!-- ============ Main header ============ -->
    <header class="site-header" v-if="!hideNavbar">
      <div class="header-inner">
        <!-- Logo -->
        <router-link to="/" class="brand">
          <BrandMark :size="34" />
          <span class="brand-text">VeriCart<span class="brand-ai">AI</span></span>
        </router-link>

        <!-- Search -->
        <form class="search-box" @submit.prevent="handleSearch">
          <el-icon class="search-icon"><Search /></el-icon>
          <input
            v-model="searchQuery"
            type="text"
            :placeholder="$t('products.searchPlaceholder')"
            :aria-label="$t('common.search')"
          />
          <button type="submit" class="search-submit">{{ $t('common.search') }}</button>
        </form>

        <!-- Actions -->
        <div class="header-actions">
          <router-link to="/wishlist" v-if="auth.isLoggedIn" class="action-btn" :title="$t('nav.wishlist')">
            <el-icon><Star /></el-icon>
            <span class="action-label">{{ $t('nav.wishlist') }}</span>
          </router-link>

          <router-link to="/orders" v-if="auth.isLoggedIn" class="action-btn" :title="$t('nav.orders')">
            <el-icon><Tickets /></el-icon>
            <span class="action-label">{{ $t('nav.orders') }}</span>
          </router-link>

          <router-link to="/messages" v-if="auth.isLoggedIn" class="action-btn" :title="$t('nav.messages')">
            <el-icon><Message /></el-icon>
            <span class="action-label">{{ $t('nav.messages') }}</span>
          </router-link>

          <router-link to="/notifications" v-if="auth.isLoggedIn" class="action-btn" :title="$t('nav.alerts')">
            <span class="icon-wrap">
              <el-icon><Bell /></el-icon>
              <span v-if="unreadCount > 0" class="badge">{{ unreadCount > 9 ? '9+' : unreadCount }}</span>
            </span>
            <span class="action-label">{{ $t('nav.alerts') }}</span>
          </router-link>

          <router-link to="/compare" class="action-btn" :title="$t('nav.compare')">
            <span class="icon-wrap">
              <el-icon><ScaleToOriginal /></el-icon>
              <span v-if="compare.count > 0" class="badge">{{ compare.count }}</span>
            </span>
            <span class="action-label">{{ $t('nav.compare') }}</span>
          </router-link>

          <router-link to="/cart" v-if="auth.isLoggedIn" class="action-btn" :title="$t('nav.cart')">
            <span class="icon-wrap">
              <el-icon><ShoppingCart /></el-icon>
              <span v-if="cart.count > 0" class="badge">{{ cart.count > 99 ? '99+' : cart.count }}</span>
            </span>
            <span class="action-label">{{ $t('nav.cart') }}</span>
          </router-link>

          <!-- Account -->
          <!-- Language -->
          <el-dropdown trigger="click" @command="switchLocale">
            <button class="lang-btn" :title="$t('nav.language')">
              {{ locale === 'en' ? 'EN' : '中' }}
              <el-icon class="caret"><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="en" :disabled="locale === 'en'">
                  {{ $t('lang.en') }}
                </el-dropdown-item>
                <el-dropdown-item command="zh" :disabled="locale === 'zh'">
                  {{ $t('lang.zh') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <template v-if="auth.isLoggedIn">
            <el-dropdown trigger="click" @command="onAccountCommand">
              <button class="account-btn">
                <span class="avatar">{{ initial }}</span>
                <span class="account-meta">
                  <span class="account-name">{{ auth.username }}</span>
                  <span class="account-role">{{ auth.isSeller ? $t('nav.sellerRole') : $t('nav.member') }}</span>
                </span>
                <el-icon class="caret"><ArrowDown /></el-icon>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon> {{ $t('nav.profile') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="orders">
                    <el-icon><Tickets /></el-icon> {{ $t('nav.orders') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="messages">
                    <el-icon><Message /></el-icon> {{ $t('nav.messages') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="seller" v-if="auth.isSeller" divided>
                    <el-icon><Shop /></el-icon> {{ $t('seller.dashboard') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided class="logout-item">
                    <el-icon><SwitchButton /></el-icon> {{ $t('nav.signOut') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="btn btn-primary btn-sm">{{ $t('nav.signIn') }}</router-link>
          </template>

          <!-- Mobile menu toggle -->
          <button class="menu-toggle" @click="mobileOpen = true" aria-label="Open menu">
            <el-icon><Menu /></el-icon>
          </button>
        </div>
      </div>

      <!-- ============ Navigation row ============ -->
      <nav class="site-nav">
        <div class="nav-inner">
          <router-link to="/" class="nav-link">
            <el-icon><HomeFilled /></el-icon> {{ $t('nav.home') }}
          </router-link>
          <router-link to="/products" class="nav-link">
            <el-icon><Goods /></el-icon> {{ $t('nav.shop') }}
          </router-link>
          <router-link to="/recommendations" v-if="auth.isLoggedIn" class="nav-link">
            <el-icon><MagicStick /></el-icon> {{ $t('nav.aiPicks') }}
          </router-link>
          <router-link to="/trust-score" class="nav-link">
            <el-icon><Odometer /></el-icon> {{ $t('nav.trustScore') }}
          </router-link>
          <router-link to="/seller" v-if="auth.isSeller" class="nav-link">
            <el-icon><Shop /></el-icon> {{ $t('nav.seller') }}
          </router-link>

          <span class="nav-divider" />

          <div class="cat-scroll">
            <router-link
              v-for="cat in quickCategories"
              :key="cat.id"
              :to="`/products?categoryId=${cat.id}`"
              class="cat-link"
            >
              {{ cat.name }}
            </router-link>
          </div>
        </div>
      </nav>
    </header>

    <!-- ============ Mobile drawer ============ -->
    <el-drawer v-model="mobileOpen" direction="rtl" size="82%" :with-header="false">
      <div class="drawer">
        <div class="drawer-head">
          <span class="brand-text">VeriCart<span class="brand-ai">AI</span></span>
          <button class="icon-btn" @click="mobileOpen = false"><el-icon><Close /></el-icon></button>
        </div>

        <div class="drawer-search">
          <el-icon><Search /></el-icon>
          <input
            v-model="searchQuery"
            :placeholder="$t('products.searchPlaceholder')"
            @keyup.enter="mobileSearch"
          />
        </div>

        <div class="drawer-links">
          <router-link to="/" class="drawer-link"><el-icon><HomeFilled /></el-icon> {{ $t('nav.home') }}</router-link>
          <router-link to="/products" class="drawer-link"><el-icon><Goods /></el-icon> {{ $t('nav.shop') }}</router-link>
          <router-link to="/recommendations" v-if="auth.isLoggedIn" class="drawer-link">
            <el-icon><MagicStick /></el-icon> {{ $t('nav.aiPicks') }}
          </router-link>
          <router-link to="/compare" class="drawer-link">
            <el-icon><ScaleToOriginal /></el-icon> {{ $t('nav.compare') }}
            <span v-if="compare.count" class="drawer-count">{{ compare.count }}</span>
          </router-link>
          <router-link to="/wishlist" v-if="auth.isLoggedIn" class="drawer-link">
            <el-icon><Star /></el-icon> {{ $t('nav.wishlist') }}
          </router-link>
          <router-link to="/cart" v-if="auth.isLoggedIn" class="drawer-link">
            <el-icon><ShoppingCart /></el-icon> {{ $t('nav.cart') }}
            <span v-if="cart.count" class="drawer-count">{{ cart.count }}</span>
          </router-link>
          <router-link to="/orders" v-if="auth.isLoggedIn" class="drawer-link">
            <el-icon><Tickets /></el-icon> {{ $t('nav.orders') }}
          </router-link>
          <router-link to="/messages" v-if="auth.isLoggedIn" class="drawer-link">
            <el-icon><Message /></el-icon> {{ $t('nav.messages') }}
          </router-link>
          <router-link to="/notifications" v-if="auth.isLoggedIn" class="drawer-link">
            <el-icon><Bell /></el-icon> {{ $t('nav.alerts') }}
            <span v-if="unreadCount" class="drawer-count">{{ unreadCount }}</span>
          </router-link>
          <router-link to="/seller" v-if="auth.isSeller" class="drawer-link">
            <el-icon><Shop /></el-icon> {{ $t('seller.dashboard') }}
          </router-link>
          <router-link to="/profile" v-if="auth.isLoggedIn" class="drawer-link">
            <el-icon><User /></el-icon> {{ $t('nav.profile') }}
          </router-link>
        </div>

        <!-- Language in drawer -->
        <div class="drawer-lang">
          <p class="drawer-cats-title">{{ $t('nav.language') }}</p>
          <div class="drawer-cat-grid">
            <button class="drawer-cat" :class="{ active: locale === 'en' }" @click="switchLocale('en')">
              {{ $t('lang.en') }}
            </button>
            <button class="drawer-cat" :class="{ active: locale === 'zh' }" @click="switchLocale('zh')">
              {{ $t('lang.zh') }}
            </button>
          </div>
        </div>

        <div class="drawer-cats">
          <p class="drawer-cats-title">{{ $t('common.category') }}</p>
          <div class="drawer-cat-grid">
            <router-link
              v-for="cat in quickCategories"
              :key="cat.id"
              :to="`/products?categoryId=${cat.id}`"
              class="drawer-cat"
            >
              {{ cat.name }}
            </router-link>
          </div>
        </div>

        <div class="drawer-foot">
          <router-link v-if="!auth.isLoggedIn" to="/login" class="btn btn-primary btn-block">
            {{ $t('nav.signIn') }}
          </router-link>
          <button v-else class="btn btn-outline btn-block" @click="handleLogout">
            {{ $t('nav.signOut') }}
          </button>
        </div>
      </div>
    </el-drawer>

    <!-- ============ Page content ============ -->
    <main class="main-content">
      <router-view />
    </main>

    <Footer />

    <!-- ============ Floating AI assistant ============ -->
    <div class="ai-chat" v-if="auth.isLoggedIn">
      <button class="ai-chat-btn" @click="showChat = !showChat" :title="showChat ? 'Close' : 'AI Assistant'">
        <el-icon v-if="!showChat"><ChatDotRound /></el-icon>
        <el-icon v-else><Close /></el-icon>
      </button>
      <ChatPanel v-if="showChat" @close="showChat = false" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { useCompareStore } from '@/stores/compare'
import { productApi, notificationApi } from '@/api'
import { setLocale, currentLocale, SUPPORTED } from '@/i18n'
import BrandMark from '@/components/BrandMark.vue'
import ChatPanel from '@/components/ChatPanel.vue'
import Footer from '@/components/Footer.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const compare = useCompareStore()

const showChat = ref(false)
const searchQuery = ref('')
const unreadCount = ref(0)
const mobileOpen = ref(false)

const hideNavbar = computed(() => ['Login', 'Register'].includes(route.name))
const initial = computed(() => (auth.username || 'U').charAt(0).toUpperCase())

/* ---- Language ---- */
const locale = ref(currentLocale())
const languages = SUPPORTED
function switchLocale(code) {
  setLocale(code)
  locale.value = code
}
const otherLocale = computed(() => (locale.value === 'en' ? 'zh' : 'en'))

const quickCategories = ref([
  { id: 1, name: 'Electronics' },
  { id: 3, name: 'Home & Garden' },
  { id: 4, name: 'Books' },
  { id: 5, name: 'Sports' },
  { id: 6, name: 'Phones' },
  { id: 8, name: 'Audio' },
  { id: 9, name: "Men's Wear" },
  { id: 10, name: "Women's Wear" },
])

async function fetchUnread() {
  if (!auth.isLoggedIn) {
    unreadCount.value = 0
    return
  }
  try {
    const res = await notificationApi.unreadCount()
    unreadCount.value = res.data || 0
  } catch {
    unreadCount.value = 0
  }
}

onMounted(async () => {
  if (auth.isLoggedIn) cart.fetchCart()
  await fetchUnread()
  setInterval(fetchUnread, 60000)
  try {
    const res = await productApi.categories()
    if (res.data?.length) quickCategories.value = res.data
  } catch { /* keep defaults */ }
})

watch(() => auth.isLoggedIn, (val) => {
  if (val) {
    cart.fetchCart()
    fetchUnread()
  } else {
    unreadCount.value = 0
  }
})

function handleSearch() {
  const q = searchQuery.value.trim()
  if (!q) return
  mobileOpen.value = false
  router.push(`/products?keyword=${encodeURIComponent(q)}`)
}

function mobileSearch() {
  handleSearch()
}

function onAccountCommand(cmd) {
  if (cmd === 'logout') { handleLogout(); return }
  router.push({ path: '/' + cmd })
}

function handleLogout() {
  auth.logout()
  cart.clearCart()
  location.href = '/'
}
</script>

<style scoped>
/* ============ Announcement bar ============ */
.announce-bar {
  background: var(--ink);
  color: rgba(255, 255, 255, 0.82);
  font-size: var(--font-xs);
}
.announce-inner {
  max-width: var(--maxw);
  margin: 0 auto;
  padding: 0 24px;
  height: 34px;
  display: flex;
  align-items: center;
  gap: 14px;
}
.announce-item { display: inline-flex; align-items: center; gap: 5px; }
.announce-sep {
  width: 1px; height: 12px; background: rgba(255, 255, 255, 0.18);
}
.announce-right { margin-left: auto; }
.announce-right a { color: rgba(255, 255, 255, 0.7); transition: color var(--transition); }
.announce-right a:hover { color: #fff; }

/* ============ Header ============ */
.site-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: saturate(180%) blur(14px);
  border-bottom: 1px solid var(--border);
}
.header-inner {
  max-width: var(--maxw);
  margin: 0 auto;
  padding: 0 24px;
  height: 68px;
  display: flex;
  align-items: center;
  gap: 20px;
}

/* Brand */
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
.brand-text {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--ink);
}
.brand-ai {
  color: var(--primary);
  margin-left: 2px;
}

/* Search */
.search-box {
  flex: 1;
  max-width: 540px;
  display: flex;
  align-items: center;
  gap: 8px;
  height: 42px;
  padding: 0 4px 0 14px;
  background: var(--surface-muted);
  border: 1px solid transparent;
  border-radius: var(--radius-full);
  transition: all var(--transition);
}
.search-box:focus-within {
  background: #fff;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(255, 77, 61, 0.12);
}
.search-icon { font-size: 17px; color: var(--text-muted); flex-shrink: 0; }
.search-box input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  background: transparent;
  font-size: var(--font-base);
  font-family: inherit;
  color: var(--text);
}
.search-box input::placeholder { color: var(--text-muted); }
.search-submit {
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
  flex-shrink: 0;
}
.search-submit:hover { background: var(--primary-hover); }

/* Actions */
.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: auto;
}
.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1px;
  min-width: 58px;
  padding: 6px 8px;
  border-radius: var(--radius-md);
  font-size: 11px;
  font-weight: 600;
  color: var(--text-secondary);
  transition: all var(--transition);
  position: relative;
}
.action-btn .el-icon { font-size: 19px; }
.action-btn:hover { color: var(--primary); background: var(--primary-light); }
.action-label { line-height: 1.2; }

.icon-wrap { position: relative; display: flex; }
.badge {
  position: absolute;
  top: -5px;
  right: -8px;
  min-width: 17px;
  height: 17px;
  padding: 0 4px;
  border-radius: var(--radius-full);
  background: var(--primary);
  color: #fff;
  font-size: 10px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #fff;
  line-height: 1;
}

/* Language switcher */
.lang-btn {
  display: flex;
  align-items: center;
  gap: 3px;
  height: 34px;
  padding: 0 9px;
  border: 1px solid var(--border);
  border-radius: var(--radius-full);
  background: #fff;
  color: var(--text-secondary);
  font-size: var(--font-xs);
  font-weight: 800;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition);
  flex-shrink: 0;
}
.lang-btn:hover { border-color: var(--ink-400); color: var(--ink); }
.lang-btn .caret { font-size: 11px; }

/* Account */
.account-btn {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 5px 10px 5px 5px;
  border: 1px solid var(--border);
  border-radius: var(--radius-full);
  background: #fff;
  cursor: pointer;
  transition: all var(--transition);
  font-family: inherit;
}
.account-btn:hover { border-color: var(--ink-400); box-shadow: var(--shadow-sm); }
.avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, #FF7A5A, #F03524);
  color: #fff;
  font-weight: 800;
  font-size: var(--font-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.account-meta { display: flex; flex-direction: column; align-items: flex-start; line-height: 1.25; }
.account-name { font-size: var(--font-sm); font-weight: 700; color: var(--ink); max-width: 92px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.account-role { font-size: 10px; color: var(--text-muted); }
.caret { font-size: 12px; color: var(--text-muted); }
.logout-item { color: var(--danger); }

.menu-toggle {
  display: none;
  width: 40px;
  height: 40px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: #fff;
  cursor: pointer;
  align-items: center;
  justify-content: center;
  font-size: 19px;
  color: var(--ink);
}

/* ============ Navigation row ============ */
.site-nav {
  border-top: 1px solid var(--border-light);
  background: #fff;
}
.nav-inner {
  max-width: var(--maxw);
  margin: 0 auto;
  padding: 0 24px;
  height: 46px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.nav-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  font-size: var(--font-sm);
  font-weight: 600;
  color: var(--text-secondary);
  white-space: nowrap;
  transition: all var(--transition);
}
.nav-link .el-icon { font-size: 15px; }
.nav-link:hover { color: var(--primary); background: var(--primary-light); }
.nav-link.router-link-exact-active { color: var(--primary); background: var(--primary-light); }

.nav-divider {
  width: 1px;
  height: 20px;
  background: var(--border);
  margin: 0 10px;
  flex-shrink: 0;
}
.cat-scroll {
  display: flex;
  align-items: center;
  gap: 2px;
  overflow-x: auto;
  scrollbar-width: none;
  min-width: 0;
}
.cat-scroll::-webkit-scrollbar { display: none; }
.cat-link {
  padding: 6px 11px;
  border-radius: var(--radius-full);
  font-size: var(--font-sm);
  color: var(--text-secondary);
  white-space: nowrap;
  transition: all var(--transition);
}
.cat-link:hover { color: var(--ink); background: var(--surface-muted); }

/* ============ Mobile drawer ============ */
.drawer { display: flex; flex-direction: column; height: 100%; padding: 18px; }
.drawer-head {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 18px;
}
.icon-btn {
  width: 36px; height: 36px; border: 1px solid var(--border);
  border-radius: var(--radius-md); background: #fff; cursor: pointer;
  display: flex; align-items: center; justify-content: center; font-size: 18px;
}
.drawer-search {
  display: flex; align-items: center; gap: 8px;
  height: 44px; padding: 0 14px;
  background: var(--surface-muted);
  border-radius: var(--radius-full);
  border: 1px solid transparent;
}
.drawer-search:focus-within { background: #fff; border-color: var(--primary); }
.drawer-search .el-icon { color: var(--text-muted); }
.drawer-search input {
  flex: 1; border: none; outline: none; background: transparent;
  font-size: var(--font-base); font-family: inherit;
}
.drawer-links { display: flex; flex-direction: column; gap: 2px; margin-top: 18px; }
.drawer-link {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 14px; border-radius: var(--radius-md);
  font-size: var(--font-md); font-weight: 600; color: var(--text);
  transition: background var(--transition);
}
.drawer-link .el-icon { font-size: 18px; color: var(--text-secondary); }
.drawer-link:hover { background: var(--surface-muted); }
.drawer-link.router-link-exact-active { background: var(--primary-light); color: var(--primary-dark); }
.drawer-count {
  margin-left: auto; min-width: 20px; height: 20px; padding: 0 6px;
  border-radius: var(--radius-full); background: var(--primary); color: #fff;
  font-size: 11px; font-weight: 800;
  display: flex; align-items: center; justify-content: center;
}
.drawer-cats { margin-top: 22px; }
.drawer-cats-title {
  font-size: var(--font-xs); font-weight: 800; letter-spacing: 0.08em;
  text-transform: uppercase; color: var(--text-muted); margin-bottom: 12px;
}
.drawer-cat-grid { display: flex; flex-wrap: wrap; gap: 8px; }
.drawer-cat {
  padding: 7px 14px; border-radius: var(--radius-full);
  background: var(--surface-muted); font-size: var(--font-sm);
  font-weight: 600; color: var(--text-secondary);
  border: none; font-family: inherit; cursor: pointer;
  transition: all var(--transition);
}
.drawer-cat:hover { background: var(--primary-light); color: var(--primary-dark); }
.drawer-cat.active { background: var(--primary); color: #fff; }
.drawer-lang { margin-top: 22px; }
.drawer-foot { margin-top: auto; padding-top: 20px; }

/* ============ Responsive ============ */
@media (max-width: 1100px) {
  .action-label { display: none; }
  .action-btn { min-width: 42px; }
  .account-meta { display: none; }
  .account-btn { padding: 4px; }
  .caret { display: none; }
}
@media (max-width: 900px) {
  .announce-bar { display: none; }
  .search-box { display: none; }
  .site-nav { display: none; }
  .menu-toggle { display: flex; }
  .action-btn { display: none; }
  .header-actions .account-btn { display: none; }
  .header-actions > .btn { display: none; }
}
@media (max-width: 520px) {
  .brand-text { font-size: 17px; }
  .header-inner { gap: 12px; padding: 0 16px; }
}
</style>
