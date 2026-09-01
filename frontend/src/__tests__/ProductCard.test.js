import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import ProductCard from '../components/ProductCard.vue'

// ProductCard uses useRouter + the cart/auth Pinia stores, so mount it with
// a real router and a fresh Pinia instance.
const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/', component: { template: '<div />' } }],
})

function mountProduct(product) {
  return mount(ProductCard, {
    props: { product },
    global: {
      plugins: [createPinia(), router],
    },
  })
}

describe('ProductCard', () => {
  const mockProduct = {
    id: 1,
    name: 'Test Phone',
    price: 999.99,
    rating: 4.5,
    reviewCount: 42,
    trustScore: 88,
    trustLevel: 'High',
    images: JSON.stringify(['/img/phone.jpg']),
  }

  it('renders product name', () => {
    const wrapper = mountProduct(mockProduct)
    expect(wrapper.text()).toContain('Test Phone')
  })

  it('renders product price', () => {
    const wrapper = mountProduct(mockProduct)
    expect(wrapper.text()).toContain('999.99')
  })

  it('renders trust score badge', () => {
    const wrapper = mountProduct(mockProduct)
    expect(wrapper.text()).toContain('88')
  })

  it('renders review count', () => {
    const wrapper = mountProduct(mockProduct)
    expect(wrapper.text()).toContain('42')
  })

  it('handles product without trust score', () => {
    const product = { ...mockProduct, trustScore: null, trustLevel: null }
    const wrapper = mountProduct(product)
    // Should not crash
    expect(wrapper.html()).toBeTruthy()
  })
})
