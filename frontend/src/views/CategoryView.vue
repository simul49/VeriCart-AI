<template>
  <div class="cat-landing">
    <div class="cat-landing-inner" v-if="main">
      <nav class="crumb">
        <router-link to="/">Home</router-link>
        <span class="sep">/</span>
        <span class="cur">{{ main.name }}</span>
      </nav>

      <header class="cat-hero">
        <h1 class="cat-title">{{ main.name }}</h1>
        <p class="cat-meta" v-if="main.subs.length">{{ main.subs.length }} sub-categories</p>
      </header>

      <div class="sub-grid" v-if="main.subs.length">
        <router-link
          v-for="s in main.subs"
          :key="s.id"
          :to="`/products?categoryId=${s.id}`"
          class="sub-card"
        >
          <div class="sub-thumbs">
            <div class="thumb" v-for="(img, i) in thumbnails(s)" :key="i">
              <img :src="img" :alt="s.name" loading="lazy" />
            </div>
            <div class="thumb thumb-empty" v-for="n in (2 - thumbnails(s).length)" :key="'e' + n">
              <span>No image</span>
            </div>
          </div>
          <div class="sub-name">{{ s.name }}</div>
        </router-link>
      </div>

      <p v-else class="empty">No sub-categories under this category yet.</p>
    </div>

    <div class="cat-landing-inner" v-else>
      <p class="empty">Category not found.</p>
      <router-link to="/" class="back">← Back to home</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { productApi } from '@/api'

const route = useRoute()
const all = ref([])

async function load() {
  try {
    const res = await productApi.categories()
    all.value = res.data || []
  } catch (e) {
    all.value = []
  }
}

onMounted(load)
watch(() => route.params.id, load)

// category.images is a JSON string like ["/images/a.jpg","/images/b.jpg"]
function thumbnails(s) {
  if (!s.images) return []
  try {
    const arr = JSON.parse(s.images)
    return Array.isArray(arr) ? arr.slice(0, 2) : [s.images]
  } catch {
    return s.images.includes(',')
      ? s.images.split(',').map(x => x.trim()).slice(0, 2)
      : [s.images]
  }
}

const main = computed(() => {
  const id = Number(route.params.id)
  return all.value.find(c => c.id === id && !c.parentId) || null
})
</script>

<style scoped>
.cat-landing {
  background: #f5f5f5;
  min-height: 72vh;
  padding: 20px 0 40px;
}
.cat-landing-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}
.crumb {
  font-size: 13px;
  color: #999;
  margin-bottom: 14px;
}
.crumb a { color: #999; text-decoration: none; }
.crumb a:hover { color: var(--primary, #ff4400); }
.crumb .sep { margin: 0 6px; }
.crumb .cur { color: #666; }

.cat-hero {
  background: #fff;
  border-radius: 12px;
  padding: 22px 24px;
  margin-bottom: 18px;
  border-left: 4px solid var(--primary, #ff4400);
  box-shadow: 0 1px 4px rgba(0,0,0,.05);
}
.cat-title { font-size: 24px; font-weight: 700; color: #222; margin: 0; }
.cat-meta { color: #999; margin: 6px 0 0; font-size: 14px; }

.sub-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(190px, 1fr));
  gap: 16px;
}
.sub-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  text-decoration: none;
  color: #333;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
  transition: box-shadow .2s, transform .2s;
}
.sub-card:hover {
  box-shadow: 0 6px 18px rgba(255,68,0,.18);
  transform: translateY(-3px);
}
.sub-thumbs {
  display: flex;
  gap: 4px;
  padding: 8px;
}
.thumb {
  flex: 1;
  aspect-ratio: 1 / 1;
  background: #f0f0f0;
  border-radius: 6px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.thumb-empty {
  color: #bbb;
  font-size: 12px;
}
.sub-name {
  padding: 10px 12px 14px;
  font-size: 14px;
  font-weight: 600;
}
.empty { color: #999; padding: 40px 0; text-align: center; }
.back { color: var(--primary, #ff4400); text-decoration: none; }
</style>
