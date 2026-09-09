<template>
  <div class="cat-landing">
    <div class="cat-landing-inner">
      <nav class="crumb">
        <router-link to="/">Home</router-link>
        <span class="sep">/</span>
        <span class="cur">{{ cat ? cat.name : 'Category' }}</span>
      </nav>

      <!-- Hero + sub-categories only render when the category was found in the directory -->
      <template v-if="cat">
        <header class="cat-hero">
          <h1 class="cat-title">{{ cat.name }}</h1>
          <p class="cat-meta" v-if="subs.length">{{ subs.length }} sub-categories</p>
        </header>

        <div class="sub-list" v-if="subs.length">
          <button
            type="button"
            class="sub-link"
            :class="{ 'is-active': activeSubId == null }"
            @click="selectSub(null)"
          >All</button>
          <button
            v-for="s in subs"
            :key="s.id"
            type="button"
            class="sub-link"
            :class="{ 'is-active': activeSubId === s.id }"
            @click="selectSub(s.id)"
          >{{ s.name }}</button>
        </div>

        <p v-else-if="isMain" class="empty">No sub-categories under this category yet.</p>
      </template>

      <!-- ============ Products in this category (always shown) ============ -->
      <section class="cat-products">
        <div v-if="loadingProducts" class="product-grid">
          <div v-for="n in 8" :key="n" class="skeleton-card">
            <div class="loading-skeleton" style="padding-top:100%;border-radius:16px 16px 0 0" />
            <div class="skeleton-body">
              <div class="loading-skeleton" style="height:14px;width:85%" />
              <div class="loading-skeleton" style="height:14px;width:55%;margin-top:8px" />
              <div class="loading-skeleton" style="height:20px;width:40%;margin-top:8px" />
            </div>
          </div>
        </div>

        <div v-else-if="products.length" class="product-grid">
          <ProductCard
            v-for="p in products"
            :key="p.id"
            :product="p"
            :show-image="true"
            :clickable="true"
          />
        </div>

        <p v-else class="empty">No products in this category yet.</p>

        <div v-if="!cat" style="margin-top:16px">
          <router-link to="/" class="back">← Back to home</router-link>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { productApi } from '@/api'
import ProductCard from '@/components/ProductCard.vue'

const route = useRoute()
const all = ref([])

// Products belonging to the current category (main OR sub — backend resolves both)
const products = ref([])
const productTotal = ref(0)
const loadingProducts = ref(false)

// Currently-selected sub-category filter (null = show all products in the main category)
const activeSubId = ref(null)

// Numeric id of the route param (kept as a number for reliable comparisons)
const routeId = computed(() => Number(route.params.id) || 0)

async function load() {
  // Load the full category directory (best-effort — never blocks the products view)
  try {
    const res = await productApi.categories()
    all.value = Array.isArray(res?.data) ? res.data : []
  } catch (e) {
    all.value = []
  }
  await loadProducts()
}

// Pick a sub-category (or clear the filter by passing null)
function selectSub(id) {
  activeSubId.value = id
  loadProducts()
}

// A product is "shown" only when it actually carries a picture (local file or URL).
const hasPicture = (p) => !!p.images && p.images !== '[]' && p.images !== ''

// Fetch products for the currently active filter (sub if selected, else the
// main category).
//  • On a MAIN category with "All" selected → one picture-backed product per
//    sub-category (so the page shows N cards, not the whole catalogue).
//  • Otherwise (specific sub picked, or direct-sub route) → all products in
//    that sub-category, up to 60.
async function loadProducts() {
  const id = activeSubId.value ?? routeId.value
  if (!id) {
    products.value = []
    productTotal.value = 0
    return
  }

  // "All" view on a main category → one card per sub-category.
  if (activeSubId.value == null && isMain.value) {
    loadingProducts.value = true
    try {
      const res = await productApi.grouped({ categoryId: id })
      const sections = res?.data || []
      const list = []
      for (const sec of sections) {
        for (const sub of sec.subcategories || []) {
          const pic = (sub.products || []).find(hasPicture)
          if (pic) list.push(pic)
        }
      }
      products.value = list
      productTotal.value = list.length
    } catch {
      products.value = []
      productTotal.value = 0
    } finally {
      loadingProducts.value = false
    }
    return
  }

  loadingProducts.value = true
  try {
    const res = await productApi.page({ categoryId: id, page: 1, size: 60 })
    products.value = res?.data?.items || []
    productTotal.value = res?.data?.total || 0
  } catch (e) {
    products.value = []
    productTotal.value = 0
  } finally {
    loadingProducts.value = false
  }
}

onMounted(load)
watch(() => route.params.id, () => {
  activeSubId.value = null
  load()
})

// The category object for this route id (lenient — matched by id, regardless of
// parentId, so a transient lookup miss can't blank the whole page).
const cat = computed(() => {
  const id = routeId.value
  if (!id) return null
  return all.value.find(c => c && c.id === id) || null
})

const isMain = computed(() => !!cat.value && !cat.value.parentId)
const subs = computed(() => {
  const id = routeId.value
  if (!id) return []
  return all.value
    .filter(s => s && s.parentId === id)
    .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
})

// category.images is unused now — sub-categories are plain text links
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

.sub-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 18px;
  padding: 6px 0 2px;
}
.sub-link {
  background: transparent;
  border: 0;
  cursor: pointer;
  color: #333;
  text-decoration: none;
  font-size: 14px;
  padding: 2px 0;
}
.sub-link:hover {
  color: var(--primary, #ff4400);
  text-decoration: underline;
}
.sub-link.is-active {
  color: var(--primary, #ff4400);
  font-weight: 700;
  text-decoration: underline;
}
.empty { color: #999; padding: 40px 0; text-align: center; }
.back { color: var(--primary, #ff4400); text-decoration: none; }

/* ---- Products within the category ---- */
.cat-products { margin-top: 8px; }

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}
.skeleton-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.skeleton-body { padding: 12px 14px 16px; }
.loading-skeleton {
  background: linear-gradient(90deg, #f0f0f0 25%, #f7f7f7 37%, #f0f0f0 63%);
  background-size: 400% 100%;
  animation: shimmer 1.4s ease infinite;
  border-radius: 6px;
}
@keyframes shimmer {
  0% { background-position: 100% 0; }
  100% { background-position: 0 0; }
}
</style>
