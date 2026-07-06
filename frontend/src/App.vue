<template>
  <div id="app-root">
    <nav class="navbar" v-if="!hideNavbar">
      <div class="nav-inner">
        <router-link to="/" class="logo">🔍 VeriCart AI</router-link>
        <div class="nav-links">
          <router-link to="/products">Products</router-link>
          <router-link to="/recommendations" v-if="auth.isLoggedIn">🤖 AI Picks</router-link>
          <router-link to="/wishlist" v-if="auth.isLoggedIn">❤️ Wishlist</router-link>
          <router-link to="/cart" v-if="auth.isLoggedIn">
            Cart <el-badge v-if="cart.count > 0" :value="cart.count" :max="99" />
          </router-link>
          <router-link to="/orders" v-if="auth.isLoggedIn">Orders</router-link>
          <router-link to="/admin" v-if="auth.isAdmin">Admin</router-link>

          <template v-if="auth.isLoggedIn">
            <router-link to="/profile">{{ auth.username }}</router-link>
            <el-button type="danger" size="small" text @click="handleLogout">Logout</el-button>
          </template>
          <template v-else>
            <el-button type="primary" size="small" @click="$router.push('/login')">Login</el-button>
            <el-button size="small" @click="$router.push('/register')">Register</el-button>
          </template>
        </div>
      </div>
    </nav>

    <main class="main-content">
      <router-view />
    </main>

    <Footer />

    <!-- AI Shopping Assistant FAB -->
    <div class="ai-chat" v-if="auth.isLoggedIn">
      <button class="ai-chat-btn" @click="showChat = !showChat" title="AI Shopping Assistant">
        <span v-if="!showChat">💬</span>
        <span v-else>✕</span>
      </button>
      <ChatPanel v-if="showChat" @close="showChat = false" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import ChatPanel from '@/components/ChatPanel.vue'
import Footer from '@/components/Footer.vue'

const route = useRoute()
const auth = useAuthStore()
const cart = useCartStore()
const showChat = ref(false)

const hideNavbar = computed(() => ['Login', 'Register'].includes(route.name))

onMounted(() => {
  if (auth.isLoggedIn) cart.fetchCart()
})

watch(() => auth.isLoggedIn, (val) => {
  if (val) cart.fetchCart()
})

function handleLogout() {
  auth.logout()
  cart.clearCart()
  location.href = '/'
}
</script>
