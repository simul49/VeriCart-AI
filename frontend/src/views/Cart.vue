<template>
  <div class="page-container">
    <h2 class="section-title">{{ $t('cart.title') }}</h2>

    <template v-if="cart.items.length">
      <div class="cart-layout">
        <!-- Cart Items -->
        <div class="cart-items-section">
          <!-- Header (hidden on mobile — items become cards) -->
          <div class="cart-head">
            <el-checkbox v-model="selectAll" size="large" @change="handleSelectAll" />
            <span class="col-product">{{ $t('common.product') }}</span>
            <span class="col-price">{{ $t('cart.unitPrice') }}</span>
            <span class="col-qty">{{ $t('common.quantity') }}</span>
            <span class="col-subtotal">{{ $t('cart.subtotal') }}</span>
            <span class="col-action" />
          </div>

          <div class="cart-item" v-for="item in cart.items" :key="item.id">
            <el-checkbox v-model="item.selected" size="large" class="col-check" />
            <img
              class="col-img"
              :src="item.productImage || 'https://placehold.co/96x96/F8F8F8/CCC?text=No+Img'"
              :alt="item.productName"
            />
            <div class="col-product">
              <router-link :to="`/products/${item.productId}`" class="cart-name">
                {{ item.productName }}
              </router-link>
              <div class="cart-id">ID: {{ item.productId }}</div>
            </div>
            <div class="col-price">
              <span class="m-label">{{ $t('cart.unitPrice') }}</span>
              <span class="m-value">${{ item.productPrice }}</span>
            </div>
            <div class="col-qty">
              <el-input-number
                v-model="item.quantity"
                :min="1"
                :max="99"
                size="small"
                @change="cart.updateQuantity(item.id, item.quantity)"
              />
            </div>
            <div class="col-subtotal">
              <span class="m-label">{{ $t('cart.subtotal') }}</span>
              <span class="m-value">${{ (item.productPrice * item.quantity).toFixed(2) }}</span>
            </div>
            <div class="col-action">
              <el-button
                type="danger"
                text
                size="small"
                :title="$t('cart.removeItem')"
                @click="handleRemove(item)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </div>

        <!-- Cart Summary -->
        <div class="cart-summary">
          <h3 class="summary-title">{{ $t('checkout.orderSummary') }}</h3>

          <!-- Coupon -->
          <div class="coupon">
            <div class="coupon-label">{{ $t('cart.promoCode') }}</div>
            <div class="coupon-row">
              <el-input v-model="couponCode" :placeholder="$t('cart.promoPlaceholder')" size="small" />
              <el-button type="primary" size="small" @click="handleApplyCoupon">{{ $t('cart.apply') }}</el-button>
            </div>
            <div class="coupon-ok" v-if="couponDiscount > 0">
              <el-icon><CircleCheck /></el-icon>
              {{ $t('cart.discountApplied') }} -${{ couponDiscount.toFixed(2) }}
            </div>
          </div>

          <div class="summary-lines">
            <div class="summary-line">
              <span>{{ $t('cart.subtotal') }} ({{ selectedCount }})</span>
              <span>${{ subtotal.toFixed(2) }}</span>
            </div>
            <div class="summary-line" v-if="couponDiscount > 0">
              <span>{{ $t('cart.discount') }}</span>
              <span class="summary-good">-${{ couponDiscount.toFixed(2) }}</span>
            </div>
            <div class="summary-line">
              <span>{{ $t('cart.shipping') }}</span>
              <span class="summary-good">{{ $t('cart.free') }}</span>
            </div>
            <div class="summary-line summary-total">
              <span>{{ $t('common.total') }}</span>
              <span class="summary-total-value">${{ total.toFixed(2) }}</span>
            </div>
          </div>

          <el-button
            type="primary"
            size="large"
            class="checkout-btn"
            @click="handleCheckout"
            :disabled="selectedCount === 0"
          >
            {{ $t('cart.checkout') }} ({{ selectedCount }})
          </el-button>

          <div class="continue-row">
            <el-button text size="small" @click="$router.push('/products')">
              {{ $t('cart.continueShopping') }}
            </el-button>
          </div>

          <!-- Payment Methods -->
          <div class="pay-methods">
            <div class="pay-title">
              <el-icon><Lock /></el-icon> {{ $t('cart.securePayment') }}
            </div>
            <div class="pay-icons">
              <span class="pay-chip">VISA</span>
              <span class="pay-chip">MC</span>
              <span class="pay-chip">COD</span>
              <span class="pay-chip">ALIPAY</span>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- Empty Cart -->
    <div v-else class="empty-state">
      <div class="empty-card">
        <span class="empty-glyph"><el-icon><ShoppingCart /></el-icon></span>
        <p class="empty-title">{{ $t('cart.emptyTitle') }}</p>
        <p class="empty-sub">{{ $t('cart.emptySub') }}</p>
        <el-button type="primary" size="large" class="btn-rounded" @click="$router.push('/products')">
          {{ $t('cart.startShopping') }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useCartStore } from '@/stores/cart'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, CircleCheck, Lock, ShoppingCart } from '@element-plus/icons-vue'

const { t } = useI18n()
const router = useRouter()
const cart = useCartStore()
const selectAll = ref(false)
const couponCode = ref('')
const couponDiscount = ref(0)

const subtotal = computed(() =>
  cart.items
    .filter(i => i.selected)
    .reduce((s, i) => s + i.productPrice * i.quantity, 0)
)

const selectedCount = computed(() =>
  cart.items.filter(i => i.selected).reduce((s, i) => s + i.quantity, 0)
)

const total = computed(() => Math.max(0, subtotal.value - couponDiscount.value))

onMounted(async () => {
  await cart.fetchCart()
  cart.items.forEach(item => {
    if (!('selected' in item)) item.selected = true
  })
  updateSelectAll()
})

watch(() => cart.items.length, () => {
  cart.items.forEach(item => {
    if (!('selected' in item)) item.selected = true
  })
  updateSelectAll()
})

function handleSelectAll(val) {
  cart.items.forEach(item => { item.selected = val })
}

function updateSelectAll() {
  selectAll.value = cart.items.length > 0 && cart.items.every(i => i.selected)
}

function handleRemove(item) {
  ElMessageBox.confirm(
    t('cart.removeConfirm', { name: item.productName }),
    t('common.confirm'),
    { type: 'warning', confirmButtonText: t('common.remove') }
  ).then(() => {
    cart.removeFromCart(item.id)
    ElMessage.success(t('cart.removed'))
  }).catch(() => {})
}

function handleApplyCoupon() {
  const code = couponCode.value.trim().toUpperCase()
  if (code === 'SAVE10') {
    couponDiscount.value = Math.min(subtotal.value * 0.1, 100)
    ElMessage.success(t('cart.couponApplied'))
  } else if (code === 'WELCOME') {
    couponDiscount.value = 5
    ElMessage.success(t('cart.couponWelcome'))
  } else {
    ElMessage.warning(t('cart.couponInvalid'))
    couponDiscount.value = 0
  }
}

function handleCheckout() {
  if (selectedCount.value === 0) {
    ElMessage.warning(t('cart.selectItems'))
    return
  }
  router.push('/checkout')
}
</script>

<style scoped>
/* ---------- Cart rows ---------- */
.cart-head,
.cart-item {
  display: flex;
  align-items: center;
  gap: 16px;
}
.cart-head {
  padding: 14px 20px;
  background: #FAFAFA;
  font-size: var(--font-sm);
  font-weight: 600;
  color: var(--text-secondary);
}
.cart-item { padding: 20px; border-bottom: 1px solid var(--border-light); }
.cart-item:last-child { border-bottom: none; }

.col-product { flex: 1; min-width: 0; }
.col-price { width: 100px; text-align: center; font-weight: 600; }
.col-qty { width: 140px; text-align: center; }
.col-subtotal {
  width: 100px; text-align: right; font-weight: 700;
  color: var(--primary); font-size: var(--font-lg);
}
.col-action { width: 50px; text-align: center; }
.col-img {
  width: 96px; height: 96px; flex-shrink: 0;
  border-radius: var(--radius-md); object-fit: cover;
  background: var(--surface-muted);
}
.cart-name {
  font-weight: 600;
  font-size: var(--font-sm);
  display: block;
  margin-bottom: 4px;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cart-name:hover { color: var(--primary); }
.cart-id { font-size: var(--font-xs); color: var(--text-muted); }

/* Labels only shown in the mobile card layout */
.m-label { display: none; }

/* ---------- Summary ---------- */
.summary-title {
  font-size: var(--font-xl);
  font-weight: 700;
  margin-bottom: 16px;
  color: var(--ink);
}
.coupon { margin-bottom: 16px; }
.coupon-label {
  font-size: var(--font-sm);
  font-weight: 600;
  margin-bottom: 6px;
}
.coupon-row { display: flex; gap: 8px; }
.coupon-ok {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: var(--font-xs);
  color: var(--success);
  margin-top: 6px;
}
.summary-lines { border-top: 1px solid var(--border-light); padding-top: 16px; }
.summary-line {
  display: flex;
  justify-content: space-between;
  font-size: var(--font-sm);
  color: var(--text-secondary);
  margin-bottom: 8px;
}
.summary-good { color: var(--success); font-weight: 600; }
.summary-total {
  font-size: var(--font-xl);
  font-weight: 700;
  color: var(--ink);
  padding-top: 12px;
  border-top: 1px solid var(--border);
  margin-top: 4px;
}
.summary-total-value { color: var(--primary); }
.checkout-btn {
  width: 100%;
  height: 48px;
  font-size: var(--font-lg);
  font-weight: 700;
  margin-top: 16px;
  border-radius: var(--radius-full) !important;
}
.continue-row { margin-top: 12px; text-align: center; }

.pay-methods {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
  text-align: center;
}
.pay-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: var(--font-xs);
  color: var(--text-muted);
  margin-bottom: 10px;
}
.pay-icons { display: flex; gap: 6px; justify-content: center; flex-wrap: wrap; }
.pay-chip {
  padding: 4px 9px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.04em;
  color: var(--text-secondary);
  background: #fff;
}

/* ---------- Empty state ---------- */
.empty-card {
  background: #fff;
  border-radius: var(--radius-xl);
  padding: 60px 20px;
  max-width: 500px;
  margin: 0 auto;
  box-shadow: var(--shadow-sm);
}
.empty-glyph {
  width: 76px; height: 76px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  background: var(--primary-light);
  color: var(--primary);
  font-size: 32px;
}
.empty-title { font-size: 20px; font-weight: 700; color: var(--ink); }
.empty-sub { margin: 8px 0 20px; color: var(--text-secondary); }
.btn-rounded { border-radius: var(--radius-full) !important; padding: 12px 40px !important; }

/* ---------- Mobile: rows become cards ---------- */
@media (max-width: 768px) {
  .cart-head { display: none; }

  .cart-item {
    position: relative;
    display: grid;
    grid-template-columns: auto 76px 1fr;
    grid-template-areas:
      "check img  product"
      "check img  qty"
      "check img  price"
      "check img  subtotal";
    gap: 8px 12px;
    align-items: center;
    padding: 16px 14px;
  }
  .col-check { grid-area: check; align-self: start; }
  .col-img { grid-area: img; width: 76px; height: 76px; align-self: start; }
  .col-product { grid-area: product; padding-right: 30px; }
  .col-qty { grid-area: qty; width: auto; text-align: left; }
  .col-price { grid-area: price; }
  .col-subtotal { grid-area: subtotal; }

  .col-price,
  .col-subtotal {
    width: auto;
    text-align: left;
    font-size: var(--font-base);
    display: flex;
    align-items: baseline;
    gap: 6px;
  }
  .col-subtotal { font-size: var(--font-lg); }
  .m-label {
    display: inline;
    font-size: 11px;
    font-weight: 600;
    color: var(--text-muted);
  }
  .col-action {
    position: absolute;
    top: 10px;
    right: 10px;
    width: auto;
  }
}
</style>
