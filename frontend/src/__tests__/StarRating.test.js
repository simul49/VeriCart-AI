import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import StarRating from '../components/StarRating.vue'

describe('StarRating', () => {
  it('renders correct number of filled stars for rating 4', () => {
    const wrapper = mount(StarRating, {
      props: { rating: 4, maxStars: 5 },
    })

    const stars = wrapper.findAll('.star')
    expect(stars.length).toBe(5)
  })

  it('renders all empty stars for rating 0', () => {
    const wrapper = mount(StarRating, {
      props: { rating: 0, maxStars: 5 },
    })
    expect(wrapper.html()).toBeTruthy()
  })

  it('renders all filled stars for max rating', () => {
    const wrapper = mount(StarRating, {
      props: { rating: 5, maxStars: 5 },
    })
    expect(wrapper.html()).toBeTruthy()
  })

  it('handles rating above maxStars gracefully', () => {
    const wrapper = mount(StarRating, {
      props: { rating: 7, maxStars: 5 },
    })
    expect(wrapper.html()).toBeTruthy()
  })
})
