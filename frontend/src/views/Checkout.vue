<template>
  <div class="page-container">
    <h2 class="section-title">{{ $t('checkout.title') }}</h2>

    <el-steps :active="step" finish-status="success" class="checkout-steps" align-center>
      <el-step :title="$t('checkout.shippingAddress')" :description="$t('checkout.deliveryDetails')" />
      <el-step :title="$t('checkout.paymentMethod')" :description="$t('checkout.choosePayment')" />
      <el-step :title="$t('checkout.review')" :description="$t('checkout.confirmOrder')" />
      <el-step :title="$t('checkout.done')" :description="$t('checkout.orderPlacedStep')" />
    </el-steps>

    <div class="checkout-layout" v-if="step < 3">
      <!-- Main Section -->
      <div>
        <!-- Step 0: Shipping -->
        <div class="checkout-section" v-if="step === 0">
          <h3><el-icon><Location /></el-icon> {{ $t('checkout.shippingAddress') }}</h3>
          <el-form :model="form" label-position="top" size="large">
            <div class="form-grid form-grid-2">
              <el-form-item :label="$t('checkout.fullName')" required>
                <el-input v-model="form.shippingName" placeholder="John Doe" />
              </el-form-item>
              <el-form-item :label="$t('checkout.phone')" required>
                <el-input v-model="form.shippingPhone" placeholder="+1 (234) 567-8900" />
              </el-form-item>
            </div>
            <el-form-item :label="$t('checkout.address')" required>
              <el-input v-model="form.shippingAddress" placeholder="123 Main Street, Apt 4B" />
            </el-form-item>
            <div class="form-grid form-grid-3">
              <el-form-item :label="$t('checkout.city')" required>
                <el-input v-model="form.city" placeholder="City" />
              </el-form-item>
              <el-form-item :label="$t('checkout.state')" required>
                <el-input v-model="form.state" placeholder="State" />
              </el-form-item>
              <el-form-item :label="$t('checkout.zip')" required>
                <el-input v-model="form.zip" placeholder="ZIP" />
              </el-form-item>
            </div>
            <el-form-item :label="$t('checkout.note')">
              <el-input
                v-model="form.note"
                type="textarea"
                :rows="2"
                :placeholder="$t('checkout.notePlaceholder')"
              />
            </el-form-item>
          </el-form>
          <div class="checkout-nav">
            <el-button @click="$router.push('/cart')">{{ $t('checkout.backToCart') }}</el-button>
            <el-button type="primary" size="large" @click="step = 1"
              :disabled="!form.shippingName || !form.shippingPhone || !form.shippingAddress || !form.city || !form.state || !form.zip">
              {{ $t('checkout.continuePayment') }}
            </el-button>
          </div>
        </div>

        <!-- Step 1: Payment -->
        <div class="checkout-section" v-if="step === 1">
          <h3><el-icon><CreditCard /></el-icon> {{ $t('checkout.paymentMethod') }}</h3>
          <div class="form-grid form-grid-2 pay-grid">
            <div
              v-for="method in paymentMethods"
              :key="method.id"
              :class="['payment-method', { active: selectedPayment === method.id }]"
              @click="selectedPayment = method.id"
            >
              <span class="pay-glyph">{{ method.icon }}</span>
              <div class="pay-body">
                <div class="pay-name">{{ method.name }}</div>
                <div class="pay-desc">{{ method.desc }}</div>
              </div>
            </div>
          </div>
          <div class="checkout-nav">
            <el-button @click="step = 0">{{ $t('common.back') }}</el-button>
            <el-button type="primary" size="large" @click="step = 2">
              {{ $t('checkout.reviewOrder') }}
            </el-button>
          </div>
        </div>

        <!-- Step 2: Review -->
        <div v-if="step === 2">
          <div class="checkout-section">
            <h3><el-icon><Location /></el-icon> {{ $t('checkout.shippingDetails') }}</h3>
            <div class="review-grid">
              <div><strong>{{ $t('checkout.fullName') }}:</strong> {{ form.shippingName }}</div>
              <div><strong>{{ $t('checkout.phone') }}:</strong> {{ form.shippingPhone }}</div>
              <div class="span-all">
                <strong>{{ $t('checkout.address') }}:</strong>
                {{ form.shippingAddress }}, {{ form.city }}, {{ form.state }} {{ form.zip }}
              </div>
              <div v-if="form.note" class="span-all review-note">
                {{ $t('checkout.note') }}: {{ form.note }}
              </div>
            </div>
            <el-button text type="primary" size="small" @click="step = 0" class="edit-btn">
              {{ $t('common.edit') }}
            </el-button>
          </div>

          <div class="checkout-section">
            <h3><el-icon><CreditCard /></el-icon> {{ $t('checkout.paymentMethod') }}</h3>
            <div class="review-payment">
              {{ paymentMethods.find(m => m.id === selectedPayment)?.name || $t('checkout.card') }}
            </div>
            <el-button text type="primary" size="small" @click="step = 1" class="edit-btn">
              {{ $t('common.edit') }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- Order Summary Sidebar -->
      <div>
        <div class="checkout-summary">
          <h3 class="summary-title">{{ $t('checkout.orderSummary') }}</h3>

          <div v-for="item in cart.items.filter(i => i.selected)" :key="item.id" class="summary-item">
            <img
              :src="item.productImage || 'https://placehold.co/64x64/F8F8F8/CCC'"
              :alt="item.productName"
              class="summary-thumb"
            />
            <div class="summary-info">
              <div class="summary-name">{{ item.productName }}</div>
              <div class="summary-qty">{{ $t('common.quantity') }}: {{ item.quantity }}</div>
              <div class="summary-amount">${{ (item.productPrice * item.quantity).toFixed(2) }}</div>
            </div>
          </div>

          <div class="summary-totals">
            <div class="summary-line">
              <span>{{ $t('cart.subtotal') }} ({{ selectedCount }})</span>
              <span>${{ subtotal.toFixed(2) }}</span>
            </div>
            <div class="summary-line">
              <span>{{ $t('cart.shipping') }}</span>
              <span class="summary-good">{{ $t('cart.free') }}</span>
            </div>
            <div class="summary-line summary-total">
              <span>{{ $t('common.total') }}</span>
              <span class="summary-total-value">${{ subtotal.toFixed(2) }}</span>
            </div>
          </div>

          <el-button
            v-if="step === 2"
            type="primary"
            size="large"
            class="place-order-btn"
            @click="placeOrder"
            :loading="placing"
          >
            {{ placing ? $t('checkout.placing') : $t('checkout.placeOrder') }}
          </el-button>

          <div v-if="step < 2" class="summary-hint">
            {{ $t('checkout.completeHint') }}
          </div>
        </div>
      </div>
    </div>

    <!-- Step 3: Success -->
    <div v-if="step === 3" class="success-wrap">
      <div class="success-card">
        <span class="success-glyph"><el-icon><CircleCheckFilled /></el-icon></span>
        <h2 class="success-title">{{ $t('checkout.orderPlaced') }}</h2>
        <p class="success-order">{{ $t('orders.orderNo', { id: orderNo }) }}</p>
        <p class="success-sub">{{ $t('checkout.thankYou') }}</p>
        <div class="success-actions">
          <el-button type="primary" size="large" class="btn-rounded" @click="$router.push(`/orders/${orderId}`)">
            {{ $t('orders.viewDetails') }}
          </el-button>
          <el-button size="large" class="btn-rounded" @click="$router.push('/products')">
            {{ $t('cart.continueShopping') }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCartStore } from '@/stores/cart'
import { orderApi } from '@/api'
import { ElMessage } from 'element-plus'
import { Location, CreditCard, CircleCheckFilled } from '@element-plus/icons-vue'

const { t } = useI18n()
const cart = useCartStore()

const step = ref(0)
const placing = ref(false)
const orderNo = ref('')
const orderId = ref(null)
const selectedPayment = ref('card')

const form = ref({
  shippingName: '',
  shippingPhone: '',
  shippingAddress: '',
  city: '',
  state: '',
  zip: '',
  note: ''
})

const paymentMethods = [
  { id: 'card', name: 'Credit/Debit Card', desc: 'Visa, Mastercard, Amex', icon: '💳' },
  { id: 'bank', name: 'Bank Transfer', desc: 'Direct bank transfer', icon: '🏦' },
  { id: 'wallet', name: 'Digital Wallet', desc: 'PayPal, Apple Pay, etc.', icon: '📱' },
  { id: 'cod', name: 'Cash on Delivery', desc: 'Pay when you receive', icon: '💵' },
]

const subtotal = computed(() =>
  cart.items
    .filter(i => i.selected)
    .reduce((s, i) => s + i.productPrice * i.quantity, 0)
)

const selectedCount = computed(() =>
  cart.items.filter(i => i.selected).reduce((s, i) => s + i.quantity, 0)
)

async function placeOrder() {
  placing.value = true
  try {
    const items = cart.items
      .filter(i => i.selected)
      .map(i => ({ productId: i.productId, quantity: i.quantity }))
    const fullAddress = `${form.value.shippingAddress}, ${form.value.city}, ${form.value.state} ${form.value.zip}`
    const res = await orderApi.create({
      shippingName: form.value.shippingName,
      shippingPhone: form.value.shippingPhone,
      shippingAddress: fullAddress,
      note: form.value.note,
      items
    })
    orderNo.value = res.data?.orderNo || res.data?.id
    orderId.value = res.data?.id
    step.value = 3
    await cart.fetchCart()
  } catch (e) {
    ElMessage.error(t('common.networkError'))
  } finally {
    placing.value = false
  }
}
</script>

<style scoped>
/* ---------- Steps ---------- */
.checkout-steps { margin-bottom: 32px; }

/* ---------- Forms ---------- */
.form-grid { display: grid; gap: 16px; }
.form-grid-2 { grid-template-columns: 1fr 1fr; }
.form-grid-3 { grid-template-columns: 1fr 1fr 1fr; }
.checkout-section h3 {
  display: flex;
  align-items: center;
  gap: 8px;
}
.checkout-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

/* ---------- Payment ---------- */
.pay-grid { margin-bottom: 24px; gap: 12px; }
.payment-method {
  padding: 20px;
  border: 2px solid var(--border-light);
  border-radius: var(--radius-lg);
  text-align: center;
  cursor: pointer;
  transition: all var(--transition);
  background: white;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.payment-method:hover { border-color: var(--primary-light); background: var(--primary-light); }
.payment-method.active { border-color: var(--primary); background: var(--primary-light); }
.pay-glyph { font-size: 28px; }
.pay-body { margin-top: 8px; }
.pay-name { font-weight: 600; font-size: var(--font-sm); }
.pay-desc { font-size: var(--font-xs); color: var(--text-muted); }

/* ---------- Review ---------- */
.review-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  font-size: var(--font-sm);
  color: var(--text-secondary);
}
.span-all { grid-column: 1 / -1; }
.review-note { font-style: italic; }
.review-payment { font-size: var(--font-sm); color: var(--text-secondary); }
.edit-btn { margin-top: 8px; }

/* ---------- Summary sidebar ---------- */
.checkout-summary {
  background: #fff;
  border-radius: var(--radius-xl);
  padding: 24px;
  position: sticky;
  top: 92px;
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
}
.summary-title { font-size: var(--font-xl); font-weight: 700; margin-bottom: 16px; color: var(--ink); }
.summary-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-light);
}
.summary-thumb {
  width: 64px; height: 64px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
  background: var(--surface-muted);
}
.summary-info { flex: 1; min-width: 0; }
.summary-name {
  font-size: var(--font-sm);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.summary-qty { font-size: var(--font-xs); color: var(--text-muted); }
.summary-amount { font-weight: 700; color: var(--primary); margin-top: 2px; }

.summary-totals { border-top: 1px solid var(--border); padding-top: 16px; margin-top: 12px; }
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
.place-order-btn {
  width: 100%;
  height: 48px;
  font-size: var(--font-lg);
  font-weight: 700;
  margin-top: 16px;
  border-radius: var(--radius-full) !important;
}
.summary-hint {
  margin-top: 16px;
  text-align: center;
  font-size: var(--font-xs);
  color: var(--text-muted);
}
.btn-rounded { border-radius: var(--radius-full) !important; }

/* ---------- Success ---------- */
.success-wrap { text-align: center; padding: 60px 20px; }
.success-card {
  background: #fff;
  border-radius: var(--radius-xl);
  padding: 60px 40px;
  max-width: 500px;
  margin: 0 auto;
  box-shadow: var(--shadow-md);
}
.success-glyph {
  width: 88px; height: 88px;
  margin: 0 auto 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  background: var(--success-light);
  color: var(--success);
  font-size: 46px;
}
.success-title { color: var(--success); margin-bottom: 8px; font-weight: 800; }
.success-order { color: var(--text-secondary); margin-bottom: 4px; }
.success-sub { font-size: var(--font-sm); color: var(--text-muted); margin-bottom: 24px; }
.success-actions { display: flex; gap: 12px; justify-content: center; flex-wrap: wrap; }

/* ---------- Responsive ---------- */
@media (max-width: 768px) {
  /* Multi-column forms collapse to a single column */
  .form-grid-2,
  .form-grid-3,
  .review-grid { grid-template-columns: 1fr; }

  .checkout-summary { position: static; padding: 18px; }
  .checkout-nav { flex-direction: column-reverse; align-items: stretch; }
  .checkout-nav .el-button { width: 100%; margin-left: 0 !important; }
  .checkout-nav .el-button + .el-button { margin-bottom: 0; }

  /* Compact stepper: keep titles, drop descriptions */
  .checkout-steps { margin-bottom: 20px; }
  .checkout-steps :deep(.el-step__description) { display: none !important; }
  .checkout-steps :deep(.el-step__title) { font-size: 12px; }
  .checkout-steps :deep(.el-step__icon) { width: 22px; height: 22px; font-size: 12px; }

  .success-card { padding: 40px 20px; }
  .success-actions .el-button { width: 100%; }
}
</style>
