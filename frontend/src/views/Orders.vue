<template>
  <div class="page-container">
    <h2 class="section-title">My Orders</h2>

    <template v-if="orders.length">
      <div v-for="order in orders" :key="order.id"
        style="background:white;border-radius:12px;padding:20px;margin-bottom:12px;cursor:pointer"
        @click="$router.push(`/orders/${order.id}`)">
        <div style="display:flex;justify-content:space-between;align-items:center">
          <div>
            <div style="font-weight:600">Order #{{ order.orderNo }}</div>
            <div style="color:#6B7280;font-size:13px;margin-top:4px">
              {{ new Date(order.createdAt).toLocaleDateString() }}
            </div>
          </div>
          <div style="text-align:right">
            <div style="font-size:20px;font-weight:700">${{ order.totalAmount }}</div>
            <el-tag :type="statusType(order.status)" size="small" style="margin-top:4px">
              {{ order.status }}
            </el-tag>
          </div>
        </div>
      </div>
    </template>

    <div v-else style="text-align:center;padding:60px">
      <el-empty description="No orders yet" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { orderApi } from '@/api'

const orders = ref([])

onMounted(async () => {
  const res = await orderApi.list()
  orders.value = res.data || []
})

function statusType(status) {
  const map = { PENDING: 'warning', PAID: 'success', PROCESSING: '', SHIPPED: '', DELIVERED: 'success', CANCELLED: 'danger' }
  return map[status] || 'info'
}
</script>
