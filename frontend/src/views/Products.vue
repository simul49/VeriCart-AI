<template>
  <div class="page-container">
    <!-- Page header -->
    <div class="shop-head">
      <div>
        <h1 class="shop-title">{{ $t('products.title') }}</h1>
        <p class="shop-sub">
          <span v-if="loading || groupedLoading">{{ $t('products.searching') }}</span>
          <span v-else-if="isGrouped">
            {{ groupedTotal }} {{ groupedTotal === 1 ? 'product' : 'products' }}
            across {{ groupedData.length }} {{ groupedData.length === 1 ? 'category' : 'categories' }}
          </span>
          <span v-else>{{ $t('products.countPlural', { count: total }) }}</span>
        </p>
      </div>
    </div>

    <!-- Filter toolbar -->
    <div class="toolbar">
      <div class="toolbar-row">
        <el-input
          v-model="searchKeyword"
          :placeholder="$t('products.searchPlaceholder')"
          clearable
          class="toolbar-search"
          @clear="resetAndFetch"
          @keyup.enter="resetAndFetch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>

        <el-select
          v-model="filterCategory"
          :placeholder="$t('common.allCategories')"
          clearable
          class="toolbar-select"
          @change="resetAndFetch"
        >
          <el-option :label="$t('common.allCategories')" value="" />
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>

        <el-select v-model="sortBy" class="toolbar-select" @change="resetAndFetch">
          <el-option :label="$t('products.newest')" value="newest" />
          <el-option :label="$t('products.priceAsc')" value="price_asc" />
          <el-option :label="$t('products.priceDesc')" value="price_desc" />
          <el-option :label="$t('products.byTrust')" value="trust" />
        </el-select>

        <el-button type="primary" @click="resetAndFetch">{{ $t('common.search') }}</el-button>
      </div>

      <!-- Active filters -->
      <div v-if="activeFilters.length" class="active-filters">
        <span class="filters-label">{{ $t('common.filters') }}:</span>
        <span v-for="f in activeFilters" :key="f.key" class="filter-chip">
          {{ f.label }}
          <button @click="f.clear" aria-label="Remove filter"><el-icon><Close /></el-icon></button>
        </span>
        <button class="clear-all" @click="clearAll">{{ $t('common.clearAll') }}</button>
      </div>
    </div>

    <!-- ============ GROUPED BY SUB-CATEGORY (All Products overview) ============ -->
    <div v-if="isGrouped">
      <div v-if="groupedLoading" class="product-grid">
        <div v-for="n in 8" :key="n" class="skeleton-card">
          <div class="loading-skeleton" style="padding-top:100%;border-radius:16px 16px 0 0" />
          <div class="skeleton-body">
            <div class="loading-skeleton" style="height:14px;width:85%" />
            <div class="loading-skeleton" style="height:14px;width:55%" />
            <div class="loading-skeleton" style="height:20px;width:40%;margin-top:8px" />
          </div>
        </div>
      </div>

      <div v-else-if="groupedData.length" class="category-sections">
        <section v-for="sec in groupedData" :key="sec.id" class="cat-section">
          <h2 class="cat-section-title">{{ sec.name }}</h2>

          <div v-for="sub in sec.subcategories" :key="sub.id" class="sub-section">
            <div class="sub-header">
              <h3 class="sub-title">{{ sub.name }}</h3>
              <button class="view-all" @click="goToSub(sub.id)">View all →</button>
            </div>
            <div class="product-grid">
              <ProductCard
                v-for="p in sub.products"
                :key="p.id"
                :product="p"
                :show-image="true"
                :clickable="true"
              />
            </div>
          </div>
        </section>
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon"><el-icon><Search /></el-icon></div>
        <p style="font-size:17px;font-weight:700;color:var(--ink)">{{ $t('products.noResults') }}</p>
      </div>
    </div>

    <!-- ============ FLAT (search / category filter / sort) ============ -->
    <div v-else>
      <div v-if="loading" class="product-grid">
        <div v-for="n in 8" :key="n" class="skeleton-card">
          <div class="loading-skeleton" style="padding-top:100%;border-radius:16px 16px 0 0" />
          <div class="skeleton-body">
            <div class="loading-skeleton" style="height:14px;width:85%" />
            <div class="loading-skeleton" style="height:14px;width:55%" />
            <div class="loading-skeleton" style="height:20px;width:40%;margin-top:8px" />
          </div>
        </div>
      </div>

      <div v-else-if="displayProducts.length" class="product-grid">
        <ProductCard
          v-for="p in displayProducts"
          :key="p.id"
          :product="p"
          :show-image="true"
          :clickable="true"
        />
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon"><el-icon><Search /></el-icon></div>
        <p style="font-size:17px;font-weight:700;color:var(--ink)">{{ $t('products.noResults') }}</p>
        <p style="margin-top:4px">{{ $t('products.noResultsSub') }}</p>
        <button class="btn btn-outline btn-sm" style="margin-top:18px" @click="clearAll">
          {{ $t('products.resetFilters') }}
        </button>
      </div>

      <!-- Pagination -->
      <div v-if="total > 0" class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[12, 24, 48]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="onSizeChange"
          @current-change="fetchProducts"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { productApi } from '@/api'
import ProductCard from '@/components/ProductCard.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const products = ref([])
const categories = ref([])
const searchKeyword = ref('')
const filterCategory = ref('')
const sortBy = ref('newest')
const loading = ref(false)

// Grouped-by-sub-category state (All Products overview)
const groupedData = ref([])
const groupedLoading = ref(false)

// Server-paginated products for the flat view (used by the grid).
const displayProducts = computed(() => products.value)

// All Products overview (no filter, no search) → grouped by sub-category.
const isGrouped = computed(() => !filterCategory.value && !searchKeyword.value)

// Total products shown across the grouped sections.
const groupedTotal = computed(() =>
  groupedData.value.reduce(
    (sum, sec) => sum + sec.subcategories.reduce((a, sub) => a + sub.products.length, 0),
    0
  )
)

const page = ref(1)
const size = ref(12)
const total = ref(0)

onMounted(async () => {
  const catRes = await productApi.categories()
  categories.value = catRes.data || []

  if (route.query.categoryId) filterCategory.value = Number(route.query.categoryId)
  if (route.query.keyword) searchKeyword.value = route.query.keyword
  await fetchAll()
})

const activeFilters = computed(() => {
  const list = []
  if (searchKeyword.value) {
    list.push({
      key: 'kw',
      label: `"${searchKeyword.value}"`,
      clear: () => { searchKeyword.value = ''; resetAndFetch() }
    })
  }
  if (filterCategory.value) {
    const cat = categories.value.find(c => c.id === filterCategory.value)
    list.push({
      key: 'cat',
      label: cat?.name || t('common.category'),
      clear: () => { filterCategory.value = ''; resetAndFetch() }
    })
  }
  if (sortBy.value !== 'newest') {
    const labels = {
      price_asc: t('products.priceAsc'),
      price_desc: t('products.priceDesc'),
      trust: t('products.byTrust')
    }
    list.push({
      key: 'sort',
      label: labels[sortBy.value] || sortBy.value,
      clear: () => { sortBy.value = 'newest'; resetAndFetch() }
    })
  }
  return list
})

function clearAll() {
  searchKeyword.value = ''
  filterCategory.value = ''
  sortBy.value = 'newest'
  resetAndFetch()
}

function resetAndFetch() {
  page.value = 1
  fetchAll()
}

function onSizeChange() {
  page.value = 1
  fetchProducts()
}

function goToSub(subId) {
  router.push({ path: '/products', query: { categoryId: subId } })
}

// Route to the correct data source based on the current mode.
async function fetchAll() {
  if (isGrouped.value) await fetchGrouped()
  else await fetchProducts()
}

async function fetchGrouped() {
  groupedLoading.value = true
  try {
    const res = await productApi.grouped()
    groupedData.value = res.data || []
  } catch {
    groupedData.value = []
  } finally {
    groupedLoading.value = false
  }
}

async function fetchProducts() {
  loading.value = true

  const params = { page: page.value, size: size.value }
  if (searchKeyword.value) params.keyword = searchKeyword.value
  if (filterCategory.value) params.categoryId = filterCategory.value
  if (sortBy.value && sortBy.value !== 'newest') params.sort = sortBy.value

  try {
    const res = await productApi.page(params)
    products.value = res.data?.items || []
    total.value = res.data?.total || 0
  } catch {
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.shop-head { margin-bottom: 20px; }
.shop-title {
  font-size: var(--font-3xl);
  font-weight: 800;
  letter-spacing: -0.025em;
  color: var(--ink);
}
.shop-sub { color: var(--text-secondary); font-size: var(--font-md); margin-top: 4px; }

/* Toolbar */
.toolbar {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 16px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-xs);
}
.toolbar-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}
.toolbar-search { flex: 1 1 260px; max-width: 380px; }
.toolbar-select { width: 170px; }

.active-filters {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed var(--border);
}
.filters-label { font-size: var(--font-sm); font-weight: 600; color: var(--text-muted); }
.filter-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 6px 4px 12px;
  border-radius: var(--radius-full);
  background: var(--primary-light);
  color: var(--primary-dark);
  font-size: var(--font-xs);
  font-weight: 700;
}
.filter-chip button {
  width: 18px;
  height: 18px;
  border: none;
  border-radius: var(--radius-full);
  background: rgba(217, 43, 28, 0.14);
  color: var(--primary-dark);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 10px;
  transition: background var(--transition);
}
.filter-chip button:hover { background: var(--primary); color: #fff; }
.clear-all {
  border: none;
  background: none;
  color: var(--text-muted);
  font-size: var(--font-xs);
  font-weight: 600;
  text-decoration: underline;
  cursor: pointer;
  font-family: inherit;
}
.clear-all:hover { color: var(--ink); }

/* Grouped by sub-category */
.category-sections { display: flex; flex-direction: column; gap: 36px; }
.cat-section-title {
  font-size: var(--font-2xl);
  font-weight: 800;
  color: var(--ink);
  margin-bottom: 18px;
  padding-bottom: 10px;
  border-bottom: 2px solid var(--border-light);
}
.sub-section { margin-bottom: 28px; }
.sub-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}
.sub-title {
  font-size: var(--font-lg);
  font-weight: 700;
  color: var(--ink);
}
.view-all {
  border: none;
  background: none;
  color: var(--primary);
  font-size: var(--font-sm);
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  white-space: nowrap;
}
.view-all:hover { text-decoration: underline; }

/* Skeleton cards */
.skeleton-card {
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
}
.skeleton-body { padding: 14px 16px 18px; display: flex; flex-direction: column; gap: 8px; }

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--border-light);
}

@media (max-width: 768px) {
  .toolbar-select { width: 100%; }
  .toolbar-search { max-width: none; }
  .shop-title { font-size: var(--font-2xl); }
  .sub-header { flex-direction: column; align-items: flex-start; gap: 4px; }
}
</style>
