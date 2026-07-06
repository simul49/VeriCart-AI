<template>
  <div class="page-container">
    <h2 class="section-title">Shopping Cart</h2>

    <template v-if="cart.items.length">
      <div class="cart-item" v-for="item in cart.items" :key="item.id">
        <img :src="item.productImage || 'https://placehold.co/80x80/F1F5F9/9CA3AF?text=No+Img'"
          style="width:80px;height:80px;border-radius:8px;object-fit:cover" />
        <div style="flex:1">
          <router-link :to="`/products/${item.productId}`" style="font-weight:600">
            {{ item.productName }}
          </router-link>
          <div style="color:var(--primary);font-weight:700;margin-top:4px">
            ${{ item.productPrice }}
          </div>
        </div>
        <el-input-number v-model="item.quantity" :min="1" :max="99" size="small"
          @change="cart.updateQuantity(item.id, item.quantity)" />
        <div style="font-weight:700;min-width:80px;text-align:right">
          ${{ (item.productPrice * item.quantity).toFixed(2) }}
        </div>
        <el-button type="danger" text @click="cart.removeFromCart(item.id)">
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>

      <div style="text-align:right;padding:24px 0;border-top:2px solid var(--border);margin-top:16px">
        <div style="font-size:24px;font-weight:800;margin-bottom:16px">
          Total: ${{ total.toFixed(2) }}
        </div>
        <el-button size="large" type="primary" @click="$router.push('/checkout')">
          Proceed to Checkout
        </el-button>
      </div>
    </template>

    <div v-else style="text-align:center;padding:80px 0;color:#9CA3AF">
      <el-empty description="Your cart is empty">
        <el-button type="primary" @click="$router.push('/products')">Browse Products</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useCartStore } from '@/stores/cart'

const cart = useCartStore()
const total = computed(() => cart.items.reduce((s, i) => s + i.productPrice * i.quantity, 0))

onMounted(() => cart.fetchCart())
</script>
