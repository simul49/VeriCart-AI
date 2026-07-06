<template>
  <div class="page-container">
    <h2 class="section-title">Products</h2>

    <!-- Filters -->
    <div style="display:flex;gap:12px;flex-wrap:wrap;align-items:center;margin-bottom:24px">
      <el-input v-model="searchKeyword" placeholder="Search products..." style="width:280px" clearable
        @clear="fetchProducts" @keyup.enter="fetchProducts">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>

      <el-select v-model="filterCategory" placeholder="All Categories" clearable style="width:180px"
        @change="fetchProducts">
        <el-option label="All Categories" :value="null" />
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>

      <el-select v-model="sortBy" placeholder="Sort by" style="width:180px" @change="fetchProducts">
        <el-option label="Newest" value="newest" />
        <el-option label="Price: Low to High" value="price_asc" />
        <el-option label="Price: High to Low" value="price_desc" />
        <el-option label="Trust Score" value="trust" />
      </el-select>

      <el-button type="primary" @click="fetchProducts">Search</el-button>
    </div>

    <!-- Results -->
    <div class="product-grid">
      <ProductCard v-for="p in products" :key="p.id" :product="p" />
    </div>

    <div v-if="!products.length" style="text-align:center;padding:60px;color:#9CA3AF">
      No products found.
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { productApi } from '@/api'
import ProductCard from '@/components/ProductCard.vue'

const route = useRoute()
const products = ref([])
const categories = ref([])
const searchKeyword = ref('')
const filterCategory = ref(null)
const sortBy = ref('newest')

onMounted(async () => {
  const catRes = await productApi.categories()
  categories.value = catRes.data || []

  if (route.query.categoryId) {
    filterCategory.value = Number(route.query.categoryId)
  }
  await fetchProducts()
})

async function fetchProducts() {
  const params = {}
  if (searchKeyword.value) params.keyword = searchKeyword.value
  if (filterCategory.value) params.categoryId = filterCategory.value
  if (sortBy.value && sortBy.value !== 'newest') params.sort = sortBy.value

  const res = await productApi.list(params)
  products.value = res.data || []
}
</script>
