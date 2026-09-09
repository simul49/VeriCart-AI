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
          <!-- Primary nav (Image 2) — moved up into the header, icon-only -->
          <nav class="header-nav">
            <router-link to="/" class="nav-link" :title="$t('nav.home')" :aria-label="$t('nav.home')">
              <el-icon class="nav-icon"><HomeFilled /></el-icon>
            </router-link>
            <router-link to="/products" class="nav-link" :title="$t('nav.shop')" :aria-label="$t('nav.shop')">
              <el-icon class="nav-icon"><Goods /></el-icon>
            </router-link>
            <router-link to="/recommendations" v-if="auth.isLoggedIn" class="nav-link" :title="$t('nav.aiPicks')" :aria-label="$t('nav.aiPicks')">
              <el-icon class="nav-icon"><MagicStick /></el-icon>
            </router-link>
            <router-link to="/trust-score" class="nav-link" :title="$t('nav.trustScore')" :aria-label="$t('nav.trustScore')">
              <el-icon class="nav-icon"><Odometer /></el-icon>
            </router-link>
          </nav>

          <router-link to="/cart" v-if="auth.isLoggedIn" class="action-btn" :title="$t('nav.cart')" :aria-label="$t('nav.cart')">
            <span class="icon-wrap">
              <el-icon><ShoppingCart /></el-icon>
              <span v-if="cart.count > 0" class="badge">{{ cart.count > 99 ? '99+' : cart.count }}</span>
            </span>
          </router-link>

          <router-link to="/notifications" v-if="auth.isLoggedIn" class="action-btn" :title="$t('nav.alerts')" :aria-label="$t('nav.alerts')">
            <span class="icon-wrap">
              <el-icon><Bell /></el-icon>
              <span v-if="unreadCount > 0" class="badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
            </span>
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
              <button class="account-btn" :title="auth.username" :aria-label="$t('nav.myAccount')">
                <span class="avatar">{{ initial }}</span>
              </button>
              <template #dropdown>
                <el-dropdown-menu class="profile-menu">
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon> {{ $t('nav.profile') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="wishlist">
                    <el-icon><Star /></el-icon> {{ $t('nav.wishlist') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="orders">
                    <el-icon><Tickets /></el-icon> {{ $t('nav.orders') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="messages">
                    <el-icon><Message /></el-icon> {{ $t('nav.messages') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="notifications">
                    <span class="dd-icon">
                      <el-icon><Bell /></el-icon>
                      <span v-if="unreadCount > 0" class="dd-badge">{{ unreadCount > 9 ? '9+' : unreadCount }}</span>
                    </span>
                    {{ $t('nav.alerts') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="compare">
                    <span class="dd-icon">
                      <el-icon><ScaleToOriginal /></el-icon>
                      <span v-if="compare.count > 0" class="dd-badge">{{ compare.count }}</span>
                    </span>
                    {{ $t('nav.compare') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="cart">
                    <el-icon><ShoppingCart /></el-icon> {{ $t('nav.cart') }}
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

      <!-- ============ Category bar ============ -->
      <div class="category-wrap" v-if="categoryTree.length">
        <div class="category-strip">
          <button
            type="button"
            class="cat-pager cat-pager-prev"
            :disabled="!canScrollLeft"
            aria-label="Previous categories"
            @click="scrollBar(-1)"
          ><el-icon><ArrowLeft /></el-icon></button>

          <nav
            class="category-bar"
            ref="catBar"
            @scroll="onBarScroll"
            @wheel="onBarWheel"
          >
            <div
              class="cat-item"
              v-for="m in categoryTree"
              :key="m.id"
            >
              <router-link
                :to="`/category/${m.id}`"
                class="cat-link"
                :class="{ 'is-active': isMainActive(m) }"
              >{{ catName(m) }}</router-link>
            </div>
          </nav>

          <button
            type="button"
            class="cat-pager cat-pager-next"
            :disabled="!canScrollRight"
            aria-label="Next categories"
            @click="scrollBar(1)"
          ><el-icon><ArrowRight /></el-icon></button>
        </div>

        <!-- Mega-menu removed: sub-category browsing happens on the category page -->
      </div>

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
          <div class="drawer-cat-group" v-for="m in categoryTree" :key="m.id">
            <router-link :to="`/products?categoryId=${m.id}`" class="drawer-cat drawer-cat-main">
              {{ catName(m) }}
            </router-link>
            <div class="drawer-sub-list" v-if="m.subs.length">
              <router-link
                v-for="s in m.subs"
                :key="s.id"
                :to="`/products?categoryId=${s.id}`"
                class="drawer-cat drawer-cat-sub"
              >{{ s.name }}</router-link>
            </div>
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
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
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
  { id: 2, name: 'Apparel & Fashion', sortOrder: 1 },
  { id: 34, name: 'Fresh Produce & Grocery', sortOrder: 2 },
  { id: 3, name: 'Home & Kitchen', sortOrder: 3 },
  { id: 1, name: 'Electronics & Digital', sortOrder: 4 },
  { id: 42, name: 'Home Appliances', sortOrder: 5 },
  { id: 26, name: 'Beauty & Personal Care', sortOrder: 6 },
  { id: 30, name: 'Mother & Baby', sortOrder: 7 },
  { id: 5, name: 'Sports & Outdoors', sortOrder: 8 },
  { id: 46, name: 'Car & Auto Accessories', sortOrder: 9 },
  { id: 50, name: 'Pet Supplies', sortOrder: 10 },
  { id: 4, name: 'Stationery & Office', sortOrder: 11 },
])

// Chinese names for the main categories (subcategories stay English)
const mainZh = {
  1: '数码3C', 2: '服装鞋帽', 34: '农产品与生鲜食品', 3: '家居百货',
  42: '家用电器', 26: '美妆个护', 30: '母婴用品', 5: '运动户外',
  46: '汽车用品', 50: '宠物用品', 4: '文具办公',
}

// Build a main → subcategory tree from the flat category list
const categoryTree = computed(() => {
  const all = quickCategories.value
  const mains = all
    .filter(c => !c.parentId)
    .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
  return mains.map(m => ({
    ...m,
    subs: all
      .filter(s => s.parentId === m.id)
      .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0)),
  }))
})

// ---- Category bar pager (prev / next page through the mains + wheel) ----
const catBar = ref(null)
const canScrollLeft = ref(false)
const canScrollRight = ref(false)

function updateBarArrows() {
  const el = catBar.value
  if (!el) {
    canScrollLeft.value = false
    canScrollRight.value = false
    return
  }
  const max = el.scrollWidth - el.clientWidth
  canScrollLeft.value = el.scrollLeft > 2
  canScrollRight.value = max > 2 && el.scrollLeft < max - 2
}
function onBarScroll() { updateBarArrows() }
function scrollBar(dir) {
  const el = catBar.value
  if (!el) return
  // Pager behaviour: each click flips one full visible "page" of tabs
  const step = Math.max(el.clientWidth, 200)
  el.scrollBy({ left: dir * step, behavior: 'smooth' })
}
function onBarWheel(e) {
  const el = catBar.value
  if (!el || el.scrollWidth <= el.clientWidth) return
  const delta = Math.abs(e.deltaX) > Math.abs(e.deltaY) ? e.deltaX : e.deltaY
  if (!delta) return
  e.preventDefault()
  el.scrollLeft += delta
}

function catName(c) {
  if (c && c.id != null && mainZh[c.id] && locale.value === 'zh') return mainZh[c.id]
  return c?.name || ''
}
function isMainActive(m) {
  const q = route.query.categoryId
  if (!q) return false
  return String(q) === String(m.id) || m.subs.some(s => String(s.id) === String(q))
}

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
  window.addEventListener('resize', updateBarArrows)
  await nextTick()
  updateBarArrows()
})

// Refresh the scroll arrows whenever the category list (re)builds the strip
watch(categoryTree, async () => {
  await nextTick()
  updateBarArrows()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateBarArrows)
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
  background: #fff;
  border-bottom: 1px solid var(--border);
  box-shadow: 0 1px 0 rgba(15, 23, 42, 0.02);
}
.site-header::after {
  content: '';
  position: absolute;
  left: 0; right: 0; bottom: -1px;
  height: 2px;
  background: var(--primary-grad);
  opacity: 0.85;
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

/* Category bar (Pinduoduo-style tab strip — single horizontal line, scrollable) */
.category-wrap {
  position: relative;
  background: #fff;
}
.category-strip {
  position: relative;
  display: flex;
  align-items: center;
  gap: 2px;
  max-width: var(--maxw);
  margin: 0 auto;
}
.category-bar {
  flex: 1 1 auto;
  min-width: 0;
  height: 48px;
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 0;
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: none;
  -webkit-overflow-scrolling: touch;
  padding: 0 4px;
}
.category-bar::-webkit-scrollbar { display: none; }

/* Pager buttons (prev / next) flanking the strip — page through the mains */
.cat-pager {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  border: 1px solid var(--border);
  border-radius: var(--radius-full);
  background: #fff;
  color: var(--text-secondary);
  cursor: pointer;
  box-shadow: var(--shadow-xs);
  transition: color 0.2s, background-color 0.2s, border-color 0.2s,
              box-shadow 0.2s, transform 0.2s;
}
.cat-pager .el-icon { font-size: 14px; }
.cat-pager:hover:not(:disabled) {
  color: #fff;
  background: var(--primary-grad);
  border-color: transparent;
  box-shadow: var(--shadow-brand);
  transform: translateY(-1px);
}
.cat-pager:disabled {
  opacity: 0.35;
  cursor: default;
  background: #fff;
}
.cat-link {
  position: relative;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  height: 48px;
  padding: 0 14px;
  font-size: var(--font-md);
  font-weight: 600;
  color: var(--text-secondary);
  white-space: nowrap;
  transition: color var(--transition);
}
.cat-link:hover { color: var(--primary); }
.cat-link.is-active {
  color: var(--primary);
  font-weight: 800;
}
.cat-link.is-active::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 6px;
  transform: translateX(-50%);
  width: 22px;
  height: 3px;
  border-radius: 3px;
  background: var(--primary-grad);
  box-shadow: 0 2px 6px var(--primary-glow);
}

/* Category wrapper (relative anchor for the mega-menu) */
.category-wrap { position: relative; }
.cat-item { display: flex; align-items: center; }

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
  max-width: 580px;
  display: flex;
  align-items: center;
  gap: 8px;
  height: 44px;
  padding: 0 4px 0 16px;
  background: var(--surface-muted);
  border: 1px solid transparent;
  border-radius: var(--radius-full);
  transition: all var(--transition);
}
.search-box:focus-within {
  background: #fff;
  border-color: var(--primary);
  box-shadow: 0 0 0 4px var(--primary-glow);
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
  height: 36px;
  padding: 0 22px;
  border: none;
  border-radius: var(--radius-full);
  background: var(--primary-grad);
  color: #fff;
  font-size: var(--font-sm);
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
  letter-spacing: 0.01em;
  box-shadow: 0 4px 12px var(--primary-glow);
  transition: transform var(--transition), box-shadow var(--transition), filter var(--transition);
  flex-shrink: 0;
}
.search-submit:hover {
  transform: translateY(-1px);
  filter: brightness(1.05);
  box-shadow: 0 8px 20px var(--primary-glow);
}
.search-submit:active { transform: translateY(0); }

/* Actions */
.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: auto;
}
.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
  padding: 6px 10px;
  border-radius: var(--radius-md);
  color: var(--ink);
  transition: all var(--transition);
  position: relative;
}
.action-btn .el-icon { font-size: 19px; }
.action-btn:hover { color: var(--primary); background: var(--primary-light); }

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
  justify-content: center;
  padding: 2px;
  border: 1px solid var(--border);
  border-radius: var(--radius-full);
  background: #fff;
  cursor: pointer;
  transition: all var(--transition);
  font-family: inherit;
}
.account-btn:hover { border-color: var(--primary); box-shadow: var(--shadow-sm); }
.avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  background: var(--primary-grad);
  color: #fff;
  font-weight: 800;
  font-size: var(--font-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 6px var(--primary-glow);
}
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

/* ============ Primary nav (now inside the header, icon-only) ============ */
.header-nav {
  display: flex;
  align-items: center;
  gap: 4px;
}
.nav-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 12px;
  border-radius: var(--radius-full);
  color: var(--ink);
  white-space: nowrap;
  transition: all var(--transition);
}
.nav-link .nav-icon { font-size: 22px; }
.nav-link:hover {
  color: var(--primary);
  background: var(--primary-light);
}
.nav-link.router-link-exact-active {
  color: var(--primary);
  background: var(--primary-soft);
}

/* ---- Profile dropdown extras (badges inside dropdown items) ---- */
.profile-menu .dd-icon {
  display: inline-flex;
  align-items: center;
  position: relative;
  margin-right: 4px;
}
.profile-menu .dd-badge {
  position: absolute;
  top: -7px;
  right: -10px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: var(--radius-full);
  background: var(--primary);
  color: #fff;
  font-size: 10px;
  font-weight: 800;
  line-height: 16px;
  text-align: center;
  border: 1.5px solid #fff;
}
.profile-menu .el-dropdown-menu__item {
  display: flex;
  align-items: center;
}

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
.drawer-cat-group { margin-bottom: 14px; }
.drawer-cat-main { width: 100%; text-align: left; font-weight: 700; color: var(--ink); background: var(--primary-soft); }
.drawer-cat-main:hover { background: var(--primary-light); color: var(--primary-dark); }
.drawer-sub-list { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 6px; padding-left: 4px; }
.drawer-cat-sub { font-size: var(--font-xs); padding: 5px 10px; }
.drawer-lang { margin-top: 22px; }
.drawer-foot { margin-top: auto; padding-top: 20px; }

/* ============ Responsive ============ */
@media (max-width: 1100px) {
  .action-btn { min-width: 42px; }
  .account-btn { padding: 3px; }
  .caret { display: none; }
}
@media (max-width: 900px) {
  .announce-bar { display: none; }
  .search-box { display: none; }
  .header-nav { display: none; }
  .category-wrap { display: none; }
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
