<template>
  <div class="page-container">
    <h2 class="section-title">❤️ My Wishlist</h2>

    <template v-if="wishlistItems.length">
      <div class="product-grid">
        <ProductCard
          v-for="item in wishlistItems"
          :key="item.id"
          :product="item.product"
        >
          <template #actions>
            <el-button
              type="danger"
              size="small"
              plain
              @click="handleRemove(item.productId)"
              style="margin-top:8px;width:100%"
            >
              <el-icon><Delete /></el-icon> Remove
            </el-button>
          </template>
        </ProductCard>
      </div>
    </template>

    <div v-else style="text-align:center;padding:80px 0;color:#9CA3AF">
      <el-empty description="Your wishlist is empty">
        <p style="margin-top:8px">Save items you love to your wishlist</p>
        <el-button type="primary" @click="$router.push('/products')" style="margin-top:16px">
          Browse Products
        </el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { wishlistApi } from '@/api'
import ProductCard from '@/components/ProductCard.vue'
import { ElMessage } from 'element-plus'

const wishlistItems = ref([])

onMounted(async () => {
  await fetchWishlist()
})

async function fetchWishlist() {
  try {
    const res = await wishlistApi.list()
    wishlistItems.value = res.data || []
  } catch (e) { /* handled */ }
}

async function handleRemove(productId) {
  try {
    await wishlistApi.remove(productId)
    ElMessage.success('Removed from wishlist')
    await fetchWishlist()
  } catch (e) {
    ElMessage.error('Failed to remove item')
  }
}
</script>
