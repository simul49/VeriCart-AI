<template>
  <div class="page-container" style="max-width:800px">
    <h2 class="section-title">Checkout</h2>

    <el-steps :active="step" finish-status="success" style="margin-bottom:32px">
      <el-step title="Address" />
      <el-step title="Review" />
      <el-step title="Confirm" />
    </el-steps>

    <!-- Step 1: Shipping -->
    <template v-if="step === 0">
      <el-form :model="form" label-position="top">
        <el-form-item label="Full Name" required>
          <el-input v-model="form.shippingName" placeholder="John Doe" size="large" />
        </el-form-item>
        <el-form-item label="Phone" required>
          <el-input v-model="form.shippingPhone" placeholder="+1 234 567 8900" size="large" />
        </el-form-item>
        <el-form-item label="Address" required>
          <el-input v-model="form.shippingAddress" type="textarea" :rows="3"
            placeholder="Street, City, State, ZIP" size="large" />
        </el-form-item>
        <el-form-item label="Note (optional)">
          <el-input v-model="form.note" placeholder="Delivery instructions..." size="large" />
        </el-form-item>
      </el-form>
      <el-button type="primary" size="large" @click="step = 1"
        :disabled="!form.shippingName || !form.shippingPhone || !form.shippingAddress">
        Continue
      </el-button>
    </template>

    <!-- Step 2: Review -->
    <template v-if="step === 1">
      <div style="background:white;border-radius:12px;padding:24px;margin-bottom:16px">
        <h4 style="margin-bottom:12px">Shipping To:</h4>
        <p>{{ form.shippingName }} — {{ form.shippingPhone }}</p>
        <p style="color:#6B7280">{{ form.shippingAddress }}</p>
        <p v-if="form.note" style="color:#6B7280;font-style:italic">Note: {{ form.note }}</p>
      </div>

      <div style="background:white;border-radius:12px;padding:24px;margin-bottom:16px">
        <h4 style="margin-bottom:12px">Order Items:</h4>
        <div v-for="item in cart.items" :key="item.id" style="display:flex;justify-content:space-between;padding:8px 0">
          <span>{{ item.productName }} × {{ item.quantity }}</span>
          <span>${{ (item.productPrice * item.quantity).toFixed(2) }}</span>
        </div>
        <div style="border-top:1px solid var(--border);margin-top:8px;padding-top:8px;font-weight:700;font-size:18px">
          Total: ${{ total.toFixed(2) }}
        </div>
      </div>

      <div style="display:flex;gap:12px">
        <el-button size="large" @click="step = 0">Back</el-button>
        <el-button type="primary" size="large" @click="step = 2">Place Order</el-button>
      </div>
    </template>

    <!-- Step 3: Placing -->
    <template v-if="step === 2">
      <div style="text-align:center;padding:40px">
        <el-icon style="font-size:48px;color:var(--primary)" v-if="placing"><Loading /></el-icon>
        <div v-if="placed" style="color:#059669">
          <el-icon style="font-size:64px"><CircleCheckFilled /></el-icon>
          <h2 style="margin:16px 0">Order Placed Successfully!</h2>
          <p style="color:#6B7280">Order #{{ orderNo }}</p>
          <div style="display:flex;gap:12px;justify-content:center;margin-top:20px">
            <el-button type="primary" @click="$router.push('/orders')">View Orders</el-button>
            <el-button @click="$router.push('/products')">Continue Shopping</el-button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { orderApi } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const cart = useCartStore()

const step = ref(0)
const placing = ref(false)
const placed = ref(false)
const orderNo = ref('')

const form = ref({
  shippingName: '',
  shippingPhone: '',
  shippingAddress: '',
  note: ''
})

const total = computed(() => cart.items.reduce((s, i) => s + i.productPrice * i.quantity, 0))

// Watch for step 2 to trigger place order
import { watch } from 'vue'
watch(step, async (val) => {
  if (val === 2) {
    placing.value = true
    try {
      const items = cart.items.map(i => ({ productId: i.productId, quantity: i.quantity }))
      const res = await orderApi.create({ ...form.value, items })
      orderNo.value = res.data?.orderNo
      placed.value = true
      await cart.fetchCart()
    } catch (e) {
      step.value = 1
    } finally {
      placing.value = false
    }
  }
})
</script>
