<template>
  <div class="page-container" style="max-width:800px" v-if="order">
    <div style="display:flex;align-items:center;gap:12px;margin-bottom:24px">
      <el-button text @click="$router.push('/orders')"><el-icon><ArrowLeft /></el-icon> Back</el-button>
      <h2>Order #{{ order.orderNo }}</h2>
      <el-tag :type="statusType(order.status)">{{ order.status }}</el-tag>
    </div>

    <!-- Status -->
    <div style="background:white;border-radius:12px;padding:20px;margin-bottom:16px">
      <p><strong>Placed:</strong> {{ new Date(order.createdAt).toLocaleString() }}</p>
      <p><strong>Shipping to:</strong> {{ order.shippingName }} — {{ order.shippingPhone }}</p>
      <p style="color:#6B7280">{{ order.shippingAddress }}</p>
      <p v-if="order.note" style="color:#6B7280;font-style:italic">Note: {{ order.note }}</p>
    </div>

    <!-- Items -->
    <div style="background:white;border-radius:12px;padding:20px;margin-bottom:16px">
      <h4 style="margin-bottom:12px">Items</h4>
      <div v-for="item in items" :key="item.id"
        style="display:flex;justify-content:space-between;align-items:center;padding:8px 0;border-bottom:1px solid var(--border)">
        <div style="display:flex;align-items:center;gap:12px">
          <img :src="item.productImage || 'https://placehold.co/48x48/F1F5F9/9CA3AF'"
            style="width:48px;height:48px;border-radius:8px;object-fit:cover" />
          <span>{{ item.productName }} × {{ item.quantity }}</span>
        </div>
        <span style="font-weight:600">${{ (item.price * item.quantity).toFixed(2) }}</span>
      </div>
      <div style="text-align:right;margin-top:12px;font-size:20px;font-weight:700">
        Total: ${{ order.totalAmount }}
      </div>
    </div>

    <!-- Actions -->
    <div style="display:flex;gap:12px" v-if="order.status === 'PENDING'">
      <el-button type="danger" @click="handleCancel">Cancel Order</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const items = ref([])

onMounted(async () => {
  const [o, i] = await Promise.all([
    orderApi.detail(route.params.id),
    orderApi.items(route.params.id)
  ])
  order.value = o.data
  items.value = i.data || []
})

async function handleCancel() {
  await ElMessageBox.confirm('Cancel this order?', 'Confirm', { type: 'warning' })
  await orderApi.cancel(order.value.id)
  ElMessage.success('Order cancelled')
  order.value.status = 'CANCELLED'
}

function statusType(status) {
  const map = { PENDING: 'warning', PAID: 'success', PROCESSING: '', SHIPPED: '', DELIVERED: 'success', CANCELLED: 'danger' }
  return map[status] || 'info'
}
</script>
