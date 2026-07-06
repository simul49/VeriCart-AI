import { defineStore } from 'pinia'
import { ref } from 'vue'
import { cartApi } from '@/api'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const count = ref(0)
  const loading = ref(false)

  async function fetchCart() {
    loading.value = true
    try {
      const res = await cartApi.get()
      items.value = res.data || []
      count.value = items.value.reduce((s, i) => s + i.quantity, 0)
    } finally {
      loading.value = false
    }
  }

  async function addToCart(productId, quantity = 1) {
    await cartApi.add({ productId, quantity })
    await fetchCart()
  }

  async function removeFromCart(id) {
    await cartApi.remove(id)
    await fetchCart()
  }

  async function updateQuantity(id, quantity) {
    await cartApi.update(id, { quantity })
    await fetchCart()
  }

  async function clearCart() {
    await cartApi.clear()
    items.value = []
    count.value = 0
  }

  return { items, count, loading, fetchCart, addToCart, removeFromCart, updateQuantity, clearCart }
})
