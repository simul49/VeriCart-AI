import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ProductCard from '../components/ProductCard.vue'

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
    const wrapper = mount(ProductCard, {
      props: { product: mockProduct },
    })
    expect(wrapper.text()).toContain('Test Phone')
  })

  it('renders product price', () => {
    const wrapper = mount(ProductCard, {
      props: { product: mockProduct },
    })
    expect(wrapper.text()).toContain('999.99')
  })

  it('renders trust level badge', () => {
    const wrapper = mount(ProductCard, {
      props: { product: mockProduct },
    })
    expect(wrapper.text()).toContain('High')
  })

  it('renders review count', () => {
    const wrapper = mount(ProductCard, {
      props: { product: mockProduct },
    })
    expect(wrapper.text()).toContain('42')
  })

  it('handles product without trust score', () => {
    const product = { ...mockProduct, trustScore: null, trustLevel: null }
    const wrapper = mount(ProductCard, {
      props: { product },
    })
    // Should not crash
    expect(wrapper.html()).toBeTruthy()
  })
})
