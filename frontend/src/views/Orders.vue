<template>
  <div class="page-container">
    <h2 class="section-title">My Orders</h2>

    <!-- Order Tabs -->
    <div class="order-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        :class="['order-tab', { active: activeTab === tab.key }]"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
        <span v-if="tabCounts[tab.key]" style="margin-left:4px;opacity:0.7">({{ tabCounts[tab.key] }})</span>
      </button>
    </div>

    <!-- Order List -->
    <template v-if="filteredOrders.length">
      <div v-for="order in filteredOrders" :key="order.id"
        class="order-card"
        @click="$router.push(`/orders/${order.id}`)"
        style="cursor:pointer"
      >
        <!-- Header -->
        <div class="order-card-header">
          <div>
            <span style="font-weight:700;font-size:var(--font-lg)">Order #{{ order.orderNo }}</span>
            <span style="margin-left:12px;font-size:var(--font-xs);color:var(--text-muted)">
              {{ formatDate(order.createdAt) }}
            </span>
          </div>
          <div style="display:flex;align-items:center;gap:8px">
            <el-tag :type="statusTag(order.status)" size="small" effect="dark">
              {{ statusLabel(order.status) }}
            </el-tag>
            <span style="font-size:20px;font-weight:800;color:var(--primary)">¥{{ order.totalAmount }}</span>
          </div>
        </div>

        <!-- Order Timeline -->
        <div class="order-timeline">
          <div v-for="s in getTimeline(order.status)" :key="s.key"
            :class="['timeline-step', s.state]">
            <div class="step-dot" />
            <div class="step-label">{{ s.label }}</div>
          </div>
        </div>

        <!-- Actions -->
        <div style="display:flex;justify-content:flex-end;gap:8px;margin-top:12px">
          <el-button size="small" plain @click.stop="$router.push(`/orders/${order.id}`)">View Details</el-button>
          <el-button v-if="order.status === 'PENDING'" size="small" type="danger" plain @click.stop="handleCancel(order)">
            Cancel
          </el-button>
        </div>
      </div>
    </template>

    <!-- Empty -->
    <div v-else class="empty-state">
      <div style="background:white;border-radius:var(--radius-xl);padding:60px 20px;max-width:500px;margin:0 auto">
        <p style="font-size:64px;margin-bottom:16px">📦</p>
        <p style="font-size:20px;font-weight:700;color:var(--text)">No orders found</p>
        <p style="margin:8px 0 20px;color:var(--text-secondary)">
          {{ activeTab === 'all' ? 'Start shopping to see your orders here!' : 'No orders with this status' }}
        </p>
        <el-button type="primary" size="large" @click="$router.push('/products')" style="border-radius:24px">
          Browse Products
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { orderApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const orders = ref([])
const activeTab = ref('all')

const tabs = [
  { key: 'all', label: 'All Orders' },
  { key: 'PENDING', label: 'Pending' },
  { key: 'PROCESSING', label: 'Processing' },
  { key: 'SHIPPED', label: 'Shipped' },
  { key: 'DELIVERED', label: 'Delivered' },
  { key: 'CANCELLED', label: 'Cancelled' },
]

const filteredOrders = computed(() => {
  if (activeTab.value === 'all') return orders.value
  return orders.value.filter(o => o.status === activeTab.value)
})

const tabCounts = computed(() => {
  return orders.value.reduce((acc, o) => {
    acc[o.status] = (acc[o.status] || 0) + 1
    acc.all = (acc.all || 0) + 1
    return acc
  }, {})
})

onMounted(async () => {
  try {
    const res = await orderApi.list()
    orders.value = res.data || []
  } catch (e) { /* handled */ }
})

function formatDate(date) {
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric', month: 'short', day: 'numeric'
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

function getTimeline(status) {
  const steps = [
    { key: 'pending', label: 'Ordered', state: 'completed' },
    { key: 'paid', label: 'Paid', state: 'completed' },
    { key: 'processing', label: 'Processing', state: 'active' },
    { key: 'shipped', label: 'Shipped', state: '' },
    { key: 'delivered', label: 'Delivered', state: '' },
  ]

  const statusOrder = ['PENDING', 'PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED']
  const idx = statusOrder.indexOf(status)

  if (status === 'CANCELLED') {
    steps.forEach(s => { s.state = s.key === 'pending' ? 'completed' : '' })
    return []
  }

  steps.forEach((s, i) => {
    if (i <= idx) s.state = 'completed'
    else if (i === idx + 1) s.state = 'active'
    else s.state = ''
  })

  return steps
}

async function handleCancel(order) {
  try {
    await ElMessageBox.confirm(`Cancel order #${order.orderNo}?`, 'Confirm', {
      type: 'warning', confirmButtonText: 'Yes, Cancel'
    })
    await orderApi.cancel(order.id)
    ElMessage.success('Order cancelled')
    order.status = 'CANCELLED'
  } catch (e) { /* cancelled */ }
}
</script>
