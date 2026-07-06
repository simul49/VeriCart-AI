<template>
  <div>
    <!-- Hero -->
    <div class="hero">
      <h1>Shop With Trust, Powered by AI</h1>
      <p>
        VeriCart AI analyzes reviews, detects fake feedback, and generates Trust Scores —
        so you can make confident purchasing decisions.
      </p>
      <div style="display:flex;gap:12px;justify-content:center;margin-top:24px">
        <el-button size="large" type="default" @click="$router.push('/products')">
          Browse Products
        </el-button>
        <el-button size="large" type="primary" @click="$router.push('/recommendations')">
          🤖 AI Recommendations
        </el-button>
      </div>
    </div>

    <!-- Categories -->
    <div class="page-container" style="margin-top:40px">
      <h2 class="section-title">Shop by Category</h2>
      <div style="display:flex;gap:12px;flex-wrap:wrap;margin-bottom:32px">
        <el-tag v-for="cat in categories" :key="cat.id" size="large"
          type="primary" effect="plain" style="cursor:pointer;padding:8px 16px"
          @click="$router.push(`/products?categoryId=${cat.id}`)">
          {{ cat.name }}
        </el-tag>
      </div>

      <!-- Top Trusted Products -->
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px">
        <h2 class="section-title" style="margin-bottom:0">🛡️ Top Trusted Products</h2>
        <el-button text type="primary" @click="$router.push('/products?sort=trust')">View All →</el-button>
      </div>
      <div class="product-grid">
        <ProductCard v-for="p in trustedProducts" :key="p.id" :product="p" />
      </div>
      <div v-if="!trustedProducts.length" style="text-align:center;padding:40px;color:#9CA3AF">
        No trusted products yet.
      </div>

      <!-- All Products -->
      <h2 class="section-title" style="margin-top:32px">All Products</h2>
      <div class="product-grid">
        <ProductCard v-for="p in products" :key="p.id" :product="p" />
      </div>
      <div v-if="!products.length" style="text-align:center;padding:60px;color:#9CA3AF">
        No products yet. Be the first to add one!
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { productApi } from '@/api'
import ProductCard from '@/components/ProductCard.vue'

const products = ref([])
const trustedProducts = ref([])
const categories = ref([])

onMounted(async () => {
  try {
    const [prodRes, catRes] = await Promise.all([
      productApi.list(),
      productApi.categories()
    ])
    products.value = prodRes.data || []
    categories.value = catRes.data || []
    // Derive top trusted from existing data — no extra API call
    trustedProducts.value = [...(prodRes.data || [])]
      .sort((a, b) => (b.trustScore || 0) - (a.trustScore || 0))
      .slice(0, 4)
  } catch (e) { /* handled by interceptor */ }
})
</script>
