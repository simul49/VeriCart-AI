import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import StarRating from '../components/StarRating.vue'

describe('StarRating', () => {
  it('renders 5 base stars with 80% fill for rating 4', () => {
    const wrapper = mount(StarRating, {
      props: { rating: 4, maxStars: 5 },
    })

    const overlay = wrapper.find('.stars-overlay')
    expect(overlay.exists()).toBe(true)
    expect(overlay.attributes('style')).toContain('width: 80%')
  })

  it('renders empty stars for rating 0', () => {
    const wrapper = mount(StarRating, {
      props: { rating: 0, maxStars: 5 },
    })

    expect(wrapper.find('.stars-overlay').attributes('style')).toContain('width: 0%')
  })

  it('renders all filled stars for max rating', () => {
    const wrapper = mount(StarRating, {
      props: { rating: 5, maxStars: 5 },
    })

    expect(wrapper.find('.stars-overlay').attributes('style')).toContain('width: 100%')
  })

  it('clamps rating above maxStars', () => {
    const wrapper = mount(StarRating, {
      props: { rating: 7, maxStars: 5 },
    })

    expect(wrapper.find('.stars-overlay').attributes('style')).toContain('width: 100%')
  })

  it('renders 5 interactive star buttons in editable mode', () => {
    const wrapper = mount(StarRating, {
      props: { modelValue: 3, editable: true },
    })

    expect(wrapper.findAll('.star-btn').length).toBe(5)
  })

  it('emits updated value on star click in editable mode', async () => {
    const wrapper = mount(StarRating, {
      props: { modelValue: 1, editable: true },
    })

    await wrapper.findAll('.star-btn')[3].trigger('click')
    expect(wrapper.emitted('update:modelValue')[0]).toEqual([4])
  })
})
