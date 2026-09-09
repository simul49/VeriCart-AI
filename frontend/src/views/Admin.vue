<template>
  <div class="page-container">
    <h2 class="section-title">Admin Dashboard</h2>

    <!-- Stats Cards -->
    <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:16px;margin-bottom:32px">
      <el-statistic title="Total Products" :value="stats.totalProducts" />
      <el-statistic title="Total Orders" :value="stats.totalOrders" />
      <el-statistic title="Revenue" :value="`¥${(stats.totalRevenue || 0).toFixed(2)}`" />
      <el-statistic title="Flagged Reviews" :value="stats.flaggedReviews" />
    </div>

    <!-- Tabs -->
    <el-tabs v-model="activeTab" type="border-card">
      <!-- Flagged Reviews Tab -->
      <el-tab-pane label="Flagged Reviews" name="reviews">
        <div v-if="flagged.length">
          <div v-for="r in flagged" :key="r.id" class="review-card review-flagged">
            <div class="review-header">
              <span class="name">{{ r.username }}</span>
              <span style="color:#DC2626;font-weight:600">⚠️ Suspicious ({{ r.fakeProbability }}%)</span>
            </div>
            <p style="color:#4B5563;margin-top:4px">{{ r.content }}</p>
            <p style="color:#DC2626;font-size:12px;margin-top:4px">Reason: {{ r.fakeReason }}</p>
          </div>
        </div>
        <div v-else style="color:#9CA3AF;text-align:center;padding:40px">
          ✅ No flagged reviews
        </div>
      </el-tab-pane>

      <!-- Orders Tab -->
      <el-tab-pane label="Orders" name="orders">
        <div style="margin-bottom:16px">
          <el-select v-model="orderStatusFilter" placeholder="Filter by status" clearable @change="fetchOrders" style="width:200px">
            <el-option label="All" value="" />
            <el-option label="Pending" value="PENDING" />
            <el-option label="Paid" value="PAID" />
            <el-option label="Shipped" value="SHIPPED" />
            <el-option label="Delivered" value="DELIVERED" />
            <el-option label="Cancelled" value="CANCELLED" />
          </el-select>
        </div>

        <el-table :data="orders" style="width:100%" v-loading="ordersLoading" stripe>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="username" label="Customer" width="120" />
          <el-table-column prop="totalAmount" label="Amount" width="100">
            <template #default="{ row }">¥{{ (row.totalAmount || 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="Status" width="120">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="Date" width="180">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="Actions" min-width="200">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 'PAID'"
                type="primary" size="small"
                @click="updateStatus(row.id, 'SHIPPED')"
              >
                Mark Shipped
              </el-button>
              <el-button
                v-if="row.status === 'SHIPPED'"
                type="success" size="small"
                @click="updateStatus(row.id, 'DELIVERED')"
              >
                Mark Delivered
              </el-button>
              <el-button
                v-if="['PENDING', 'PAID'].includes(row.status)"
                type="danger" size="small" plain
                @click="updateStatus(row.id, 'CANCELLED')"
              >
                Cancel
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Products Tab -->
      <el-tab-pane label="Products" name="products">
        <el-table :data="adminProducts" style="width:100%" v-loading="productsLoading" stripe>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="name" label="Name" min-width="180" />
          <el-table-column prop="price" label="Price" width="100">
            <template #default="{ row }">¥{{ (row.price || 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="stock" label="Stock" width="80" />
          <el-table-column label="Trust Score" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.trustScore" :type="trustTagType(row.trustScore)">
                {{ row.trustScore }}/100
              </el-tag>
              <span v-else style="color:#9CA3AF">N/A</span>
            </template>
          </el-table-column>
          <el-table-column prop="reviewCount" label="Reviews" width="80" />
          <el-table-column prop="fakeReviewCount" label="Fake" width="60">
            <template #default="{ row }">
              <span :style="{ color: row.fakeReviewCount > 0 ? '#DC2626' : '#059669' }">
                {{ row.fakeReviewCount || 0 }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Users Tab -->
      <el-tab-pane label="Users" name="users">
        <el-table :data="adminUsers" style="width:100%" v-loading="usersLoading" stripe>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="username" label="Username" width="150" />
          <el-table-column prop="email" label="Email" min-width="200" />
          <el-table-column prop="role" label="Role" width="100">
            <template #default="{ row }">
              <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'">{{ row.role }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="Joined" width="180">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api'
import { ElMessage } from 'element-plus'

const activeTab = ref('reviews')
const stats = ref({ totalProducts: 0, totalOrders: 0, totalRevenue: 0, flaggedReviews: 0 })
const flagged = ref([])

// Orders
const orders = ref([])
const ordersLoading = ref(false)
const orderStatusFilter = ref('')

// Products
const adminProducts = ref([])
const productsLoading = ref(false)

// Users
const adminUsers = ref([])
const usersLoading = ref(false)

onMounted(async () => {
  const [dash, rev] = await Promise.all([
    adminApi.dashboard(),
    adminApi.flaggedReviews()
  ])
  stats.value = dash.data || dash
  flagged.value = rev.data || rev || []
})

async function fetchOrders() {
  ordersLoading.value = true
  try {
    const res = await adminApi.orders(orderStatusFilter.value || undefined)
    orders.value = res.data || res || []
  } catch (e) { /* handled */ }
  finally { ordersLoading.value = false }
}

async function fetchProducts() {
  productsLoading.value = true
  try {
    const res = await adminApi.products()
    adminProducts.value = res.data || res || []
  } catch (e) { /* handled */ }
  finally { productsLoading.value = false }
}

async function fetchUsers() {
  usersLoading.value = true
  try {
    const res = await adminApi.users()
    adminUsers.value = res.data || res || []
  } catch (e) { /* handled */ }
  finally { usersLoading.value = false }
}

async function updateStatus(orderId, status) {
  try {
    await adminApi.updateOrderStatus(orderId, status)
    ElMessage.success(`Order #${orderId} marked as ${status}`)
    await fetchOrders()
    // Refresh stats
    const dash = await adminApi.dashboard()
    stats.value = dash.data || dash
  } catch (e) {
    ElMessage.error('Failed to update order status')
  }
}

function statusType(status) {
  const map = { PENDING: 'warning', PAID: 'primary', SHIPPED: 'success', DELIVERED: '', CANCELLED: 'danger' }
  return map[status] || 'info'
}

function trustTagType(score) {
  if (score >= 85) return 'success'
  if (score >= 70) return ''
  if (score >= 50) return 'warning'
  return 'danger'
}

function formatDate(date) {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

// Lazy load tab data
import { watch } from 'vue'
watch(activeTab, (tab) => {
  if (tab === 'orders') fetchOrders()
  if (tab === 'products') fetchProducts()
  if (tab === 'users') fetchUsers()
})
</script>
