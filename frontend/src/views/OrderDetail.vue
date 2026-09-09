<template>
  <div class="page-container" style="max-width:1000px" v-if="order">
    <!-- Breadcrumb -->
    <div style="margin-bottom:20px;font-size:var(--font-sm);color:var(--text-muted)">
      <router-link to="/orders" style="color:var(--primary)">My Orders</router-link>
      <span style="margin:0 8px">/</span>
      <span>Order #{{ order.orderNo }}</span>
    </div>

    <!-- Status Header -->
    <div style="background:white;border-radius:var(--radius-xl);padding:28px;margin-bottom:16px;border:1px solid var(--border-light);display:flex;justify-content:space-between;align-items:flex-start;flex-wrap:wrap;gap:16px">
      <div>
        <div style="display:flex;align-items:center;gap:12px;margin-bottom:8px">
          <h2 style="font-size:var(--font-2xl);font-weight:700">Order #{{ order.orderNo }}</h2>
          <el-tag :type="statusTag(order.status)" effect="dark">{{ statusLabel(order.status) }}</el-tag>
        </div>
        <div style="font-size:var(--font-sm);color:var(--text-secondary)">
          Placed on {{ formatDateTime(order.createdAt) }}
        </div>
      </div>
      <div style="text-align:right">
        <div style="font-size:var(--font-xs);color:var(--text-muted)">Total Amount</div>
        <div style="font-size:var(--font-3xl);font-weight:800;color:var(--primary)">¥{{ order.totalAmount }}</div>
      </div>
    </div>

    <!-- Timeline -->
    <div style="background:white;border-radius:var(--radius-xl);padding:28px;margin-bottom:16px;border:1px solid var(--border-light)">
      <h3 style="font-weight:700;margin-bottom:20px">Order Progress</h3>
      <div class="order-timeline" style="margin:0">
        <div v-for="s in timeline" :key="s.key"
          :class="['timeline-step', s.state]">
          <div class="step-dot" />
          <div class="step-label" style="font-size:var(--font-sm)">{{ s.label }}</div>
          <div style="font-size:var(--font-xs);color:var(--text-muted);margin-top:4px">{{ s.date }}</div>
        </div>
      </div>
    </div>

    <!-- Details Grid -->
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;margin-bottom:16px">
      <!-- Shipping -->
      <div style="background:white;border-radius:var(--radius-xl);padding:24px;border:1px solid var(--border-light)">
        <h4 style="font-weight:700;margin-bottom:12px">📍 Shipping Address</h4>
        <div style="font-size:var(--font-sm);color:var(--text-secondary);line-height:1.8">
          <div style="font-weight:600;color:var(--text)">{{ order.shippingName }}</div>
          <div>{{ order.shippingPhone }}</div>
          <div>{{ order.shippingAddress }}</div>
          <div v-if="order.note" style="margin-top:8px;font-style:italic">Note: {{ order.note }}</div>
        </div>
      </div>

      <!-- Summary -->
      <div style="background:white;border-radius:var(--radius-xl);padding:24px;border:1px solid var(--border-light)">
        <h4 style="font-weight:700;margin-bottom:12px">💰 Order Summary</h4>
        <div style="font-size:var(--font-sm);color:var(--text-secondary)">
          <div style="display:flex;justify-content:space-between;padding:6px 0">
            <span>Payment Method</span>
            <span style="color:var(--text);font-weight:500">{{ order.paymentMethod || 'Credit Card' }}</span>
          </div>
          <div style="display:flex;justify-content:space-between;padding:6px 0">
            <span>Items</span>
            <span style="color:var(--text)">{{ items.length }} item(s)</span>
          </div>
          <div style="display:flex;justify-content:space-between;padding:6px 0">
            <span>Shipping</span>
            <span style="color:var(--success);font-weight:600">Free</span>
          </div>
          <div style="display:flex;justify-content:space-between;padding:8px 0;border-top:1px solid var(--border-light);margin-top:4px;font-weight:700;font-size:var(--font-base);color:var(--text)">
            <span>Total</span>
            <span style="color:var(--primary)">¥{{ order.totalAmount }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Order Items -->
    <div style="background:white;border-radius:var(--radius-xl);padding:24px;margin-bottom:16px;border:1px solid var(--border-light)">
      <h4 style="font-weight:700;margin-bottom:16px">📦 Order Items</h4>
      <div v-for="item in items" :key="item.id"
        style="display:flex;align-items:center;gap:16px;padding:16px 0;border-bottom:1px solid var(--border-light)">
        <img :src="item.productImage || 'https://placehold.co/80x80/F8F8F8/CCC'"
          style="width:80px;height:80px;border-radius:var(--radius-md);object-fit:cover;flex-shrink:0" />
        <div style="flex:1;min-width:0">
          <router-link :to="`/products/${item.productId}`" style="font-weight:600;font-size:var(--font-base)">
            {{ item.productName }}
          </router-link>
          <div style="color:var(--text-muted);font-size:var(--font-xs);margin-top:4px">
            Quantity: {{ item.quantity }}
          </div>
        </div>
        <div style="text-align:right">
          <div style="font-size:var(--font-lg);font-weight:700;color:var(--primary)">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
          <div style="font-size:var(--font-xs);color:var(--text-muted)">¥{{ item.price }} each</div>
        </div>
      </div>
    </div>

    <!-- Actions -->
    <div style="display:flex;gap:12px;flex-wrap:wrap" v-if="order.status === 'PENDING'">
      <el-button type="danger" @click="handleCancel">Cancel Order</el-button>
    </div>
    <div v-if="order.status === 'DELIVERED'" style="display:flex;gap:12px">
      <el-button type="primary" @click="$router.push(`/products/${items[0]?.productId}`)">Buy Again</el-button>
      <el-button plain>Write Review</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { orderApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const order = ref(null)
const items = ref([])

const timeline = computed(() => {
  const steps = [
    { key: 'pending', label: 'Order Placed', state: 'completed', date: formatDate(order.value?.createdAt) },
    { key: 'paid', label: 'Payment Confirmed', state: '', date: '' },
    { key: 'processing', label: 'Processing', state: '', date: '' },
    { key: 'shipped', label: 'Shipped', state: '', date: '' },
    { key: 'delivered', label: 'Delivered', state: '', date: '' },
  ]

  const statusOrder = ['PENDING', 'PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED']
  const idx = statusOrder.indexOf(order.value?.status)

  if (order.value?.status === 'CANCELLED') return []

  steps.forEach((s, i) => {
    if (i <= idx) s.state = 'completed'
    else if (i === idx + 1) s.state = 'active'
  })

  return steps
})

onMounted(async () => {
  try {
    const [o, i] = await Promise.all([
      orderApi.detail(route.params.id),
      orderApi.items(route.params.id)
    ])
    order.value = o.data
    items.value = i.data || []
  } catch (e) { /* handled */ }
})

async function handleCancel() {
  try {
    await ElMessageBox.confirm('Cancel this order? This cannot be undone.', 'Confirm Cancel', {
      type: 'warning', confirmButtonText: 'Yes, Cancel'
    })
    await orderApi.cancel(order.value.id)
    ElMessage.success('Order cancelled')
    order.value.status = 'CANCELLED'
  } catch (e) { /* cancelled */ }
}

function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
}

function formatDateTime(date) {
  if (!date) return ''
  return new Date(date).toLocaleString('en-US', {
    year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit'
  })
}

function statusTag(status) {
  const map = { PENDING: 'warning', PAID: '', PROCESSING: '', SHIPPED: '', DELIVERED: 'success', CANCELLED: 'danger' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { PENDING: 'Pending', PAID: 'Paid', PROCESSING: 'Processing', SHIPPED: 'Shipped', DELIVERED: 'Delivered', CANCELLED: 'Cancelled' }
  return map[status] || status
}
</script>
