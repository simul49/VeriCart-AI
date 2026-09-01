import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * Holds products selected for side-by-side AI comparison (FR-057).
 * Max 4 products, persisted so the selection survives navigation.
 */
export const useCompareStore = defineStore('compare', () => {
  const STORAGE_KEY = 'vericart_compare'
  const MAX = 4

  const items = ref(load())

  function load() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY)
      return raw ? JSON.parse(raw) : []
    } catch {
      return []
    }
  }

  function persist() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(items.value))
  }

  const count = computed(() => items.value.length)
  const ids = computed(() => items.value.map(p => p.id))

  function isSelected(productId) {
    return items.value.some(p => p.id === productId)
  }

  function add(product) {
    if (!product) return false
    if (isSelected(product.id)) return false
    if (items.value.length >= MAX) return false
    items.value.push({
      id: product.id,
      name: product.name,
      price: product.price,
      images: product.images,
      trustScore: product.trustScore,
      rating: product.rating,
      reviewCount: product.reviewCount
    })
    persist()
    return true
  }

  function remove(productId) {
    items.value = items.value.filter(p => p.id !== productId)
    persist()
  }

  function toggle(product) {
    if (isSelected(product.id)) {
      remove(product.id)
      return false
    }
    return add(product)
  }

  function clear() {
    items.value = []
    persist()
  }

  return { items, count, ids, isSelected, add, remove, toggle, clear }
})
