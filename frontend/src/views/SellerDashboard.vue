<template>
  <div class="page-container">
    <h2 class="section-title">🏪 Seller Dashboard</h2>
    <p style="color:var(--text-muted);margin-bottom:24px">
      Manage your catalogue, fulfil orders and monitor AI review analysis.
    </p>

    <!-- Stats -->
    <div class="stats-grid" v-if="stats">
      <div class="stat-card">
        <span class="stat-label">Products</span>
        <span class="stat-value">{{ stats.productCount }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">Orders</span>
        <span class="stat-value">{{ stats.orderCount }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">Revenue</span>
        <span class="stat-value">${{ Number(stats.revenue || 0).toFixed(2) }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">Avg rating</span>
        <span class="stat-value">{{ stats.avgRating }}/5</span>
      </div>
      <div class="stat-card warn" v-if="stats.flaggedReviews > 0">
        <span class="stat-label">⚠️ Flagged reviews</span>
        <span class="stat-value">{{ stats.flaggedReviews }}</span>
      </div>
      <div class="stat-card warn" v-if="stats.lowStockCount > 0">
        <span class="stat-label">📦 Low stock</span>
        <span class="stat-value">{{ stats.lowStockCount }}</span>
      </div>
    </div>

    <el-tabs v-model="tab">
      <!-- Products -->
      <el-tab-pane label="📦 Products" name="products">
        <div class="tab-head">
          <el-button type="primary" size="small" @click="openCreate">+ Add Product</el-button>
        </div>

        <div v-if="products.length" class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>Product</th>
                <th>Price</th>
                <th>Stock</th>
                <th>Trust</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in products" :key="p.id">
                <td>
                  <div class="prod-cell">
                    <img :src="firstImage(p)" :alt="p.name" />
                    <span>{{ p.name }}</span>
                  </div>
                </td>
                <td>${{ p.price }}</td>
                <td>
                  <span :class="{ 'low-stock': p.stock < 10 }">{{ p.stock }}</span>
                </td>
                <td>
                  <span :class="trustClass(p.trustScore)">{{ p.trustScore ?? '—' }}</span>
                </td>
                <td>
                  <el-button text size="small" @click="openEdit(p)">Edit</el-button>
                  <el-button text size="small" type="danger" @click="removeProduct(p)">
                    Delete
                  </el-button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-else class="empty-state">
          <p style="font-size:36px;margin-bottom:8px">📦</p>
          <p style="font-weight:600;color:var(--text)">No products yet</p>
          <p style="margin-top:4px">Add your first product to start selling.</p>
        </div>
      </el-tab-pane>

      <!-- Orders -->
      <el-tab-pane label="🧾 Orders" name="orders">
        <div v-if="orders.length" class="order-list">
          <div v-for="o in orders" :key="o.id" class="order-card">
            <div class="order-top">
              <div>
                <span class="order-no">#{{ o.orderNo }}</span>
                <span class="order-date">{{ formatDate(o.createdAt) }}</span>
              </div>
              <span :class="['status-badge', statusClass(o.status)]">{{ o.status }}</span>
            </div>
            <div class="order-mid">
              <span class="order-total">${{ o.totalAmount }}</span>
              <span class="order-ship">{{ o.shippingName }} · {{ o.shippingAddress }}</span>
            </div>
            <div class="order-actions">
              <el-button
                v-if="o.status === 'PENDING'"
                size="small"
                type="primary"
                @click="updateStatus(o, 'PROCESSING')"
              >Mark Processing</el-button>
              <el-button
                v-if="['PENDING', 'PAID', 'PROCESSING'].includes(o.status)"
                size="small"
                type="success"
                @click="updateStatus(o, 'SHIPPED')"
              >Mark Shipped</el-button>
              <el-button
                v-if="o.status === 'SHIPPED'"
                size="small"
                type="success"
                @click="updateStatus(o, 'DELIVERED')"
              >Mark Delivered</el-button>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <p style="font-size:36px;margin-bottom:8px">🧾</p>
          <p style="font-weight:600;color:var(--text)">No orders yet</p>
        </div>
      </el-tab-pane>

      <!-- Reviews (AI monitoring) -->
      <el-tab-pane label="⭐ Reviews" name="reviews">
        <div v-if="reviews.length" class="review-list">
          <div
            v-for="r in reviews"
            :key="r.id"
            :class="['seller-review', { flagged: r.isFlagged }]"
          >
            <div class="sr-head">
              <span class="sr-product">{{ r.productName || 'Product' }}</span>
              <StarRating :rating="r.rating" :size="14" />
              <span v-if="r.verified" class="verified-badge">✓ Verified</span>
              <span :class="['ai-verdict', r.isFlagged ? 'verdict-bad' : 'verdict-good']">
                {{ r.isFlagged ? '⚠️ Suspicious' : '✓ Genuine' }}
              </span>
              <span class="sr-date">{{ formatDate(r.createdAt) }}</span>
            </div>
            <p class="sr-content">{{ r.content }}</p>
            <div v-if="r.fakeReason" class="fake-reason">
              <strong>AI Detection:</strong> {{ r.fakeReason }}
            </div>
            <div v-if="r.sentiment || r.emotion" class="sr-ai">
              <span v-if="r.sentiment">Sentiment: {{ r.sentiment }}</span>
              <span v-if="r.emotion">Emotion: {{ r.emotion }}</span>
              <span v-if="r.fakeProbability != null">
                Fake probability: {{ Math.round(r.fakeProbability) }}%
              </span>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <p style="font-size:36px;margin-bottom:8px">⭐</p>
          <p style="font-weight:600;color:var(--text)">No reviews yet</p>
          <p style="margin-top:4px">Customer reviews with AI analysis will appear here.</p>
        </div>
      </el-tab-pane>

      <!-- Messages (customer inquiries) -->
      <el-tab-pane :label="messagesLabel" name="messages">
        <div v-if="messages.length" class="inquiry-list">
          <div
            v-for="m in messages"
            :key="m.id"
            :class="['inquiry-card', { unread: !m.isRead, replied: hasReply(m) }]"
          >
            <div class="inquiry-head">
              <el-avatar :size="34" style="background:#2563eb;color:#fff">
                {{ m.userName?.[0]?.toUpperCase() || '?' }}
              </el-avatar>
              <div style="flex:1;min-width:0">
                <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap">
                  <span class="inquiry-user">{{ m.userName }}</span>
                  <span v-if="!m.isRead" class="inquiry-new">NEW</span>
                  <span :class="['status-badge', (m.status||'').toLowerCase()]">{{ m.status }}</span>
                </div>
                <div class="inquiry-product">
                  🛍️ {{ m.productName }} · {{ formatDate(m.createdAt) }}
                </div>
              </div>
            </div>

            <!-- Full conversation -->
            <div class="chat-body">
              <div
                v-for="msg in m.messages"
                :key="msg.id"
                :class="['chat-row', { 'from-customer': msg.sender === 'USER' }]"
              >
                <div class="bubble-wrap">
                  <div v-if="msg.sender !== 'USER'" class="bubble-label">
                    <span v-if="msg.sender === 'AI'" class="ai-reply-badge">🤖 AI auto-reply</span>
                    <span v-else class="you-badge">You</span>
                  </div>
                  <div class="chat-bubble">{{ msg.content }}</div>
                  <div class="bubble-time">{{ formatTime(msg.createdAt) }}</div>
                </div>
              </div>
            </div>

            <div class="inquiry-actions">
              <el-button size="small" type="primary" @click="openReply(m)">✍️ Reply</el-button>
              <el-button
                v-if="!m.isRead"
                size="small"
                text
                @click="markRead(m)"
              >Mark read</el-button>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <p style="font-size:36px;margin-bottom:8px">💬</p>
          <p style="font-weight:600;color:var(--text)">No messages yet</p>
          <p style="margin-top:4px">When customers ask about your products, their questions appear here.</p>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Reply dialog -->
    <el-dialog v-model="replyDialogVisible" :title="`Reply to ${replyTarget?.userName || 'customer'}`" width="520px">
      <div v-if="replyTarget" style="margin-bottom:12px;font-size:var(--font-sm)">
        <div style="font-weight:600;margin-bottom:8px">🛍️ {{ replyTarget.productName }}</div>
        <div class="dialog-chat">
          <div
            v-for="msg in replyTarget.messages"
            :key="msg.id"
            :class="['chat-row', { 'from-customer': msg.sender === 'USER' }]"
          >
            <div class="bubble-wrap">
              <div v-if="msg.sender !== 'USER'" class="bubble-label">
                <span v-if="msg.sender === 'AI'" class="ai-reply-badge">🤖 AI auto-reply</span>
                <span v-else class="you-badge">You</span>
              </div>
              <div class="chat-bubble">{{ msg.content }}</div>
              <div class="bubble-time">{{ formatTime(msg.createdAt) }}</div>
            </div>
          </div>
        </div>
        <div
          v-if="replyTarget.messages?.some(msg => msg.sender === 'AI')"
          style="margin-top:8px;font-size:12px;color:#1e40af;background:#eff6ff;border:1px solid #bfdbfe;border-radius:8px;padding:8px 12px"
        >
          🤖 The AI already answered this conversation while you were away. Your reply will be
          added to the thread — the customer can keep chatting after it.
        </div>
      </div>
      <el-input
        v-model="replyText"
        type="textarea"
        :rows="4"
        maxlength="2000"
        show-word-limit
        placeholder="Type your answer…"
      />
      <template #footer>
        <el-button @click="replyDialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="replying" :disabled="!replyText.trim()" @click="sendReply">
          Send Reply
        </el-button>
      </template>
    </el-dialog>

    <!-- Create / Edit product dialog -->
    <el-dialog v-model="dialogVisible" :title="editing ? 'Edit Product' : 'Add Product'" width="520px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="Name" required>
          <el-input v-model="form.name" placeholder="Product name" />
        </el-form-item>
        <el-form-item label="Description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="Price" required>
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="Stock" required>
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="Category">
          <el-select v-model="form.categoryId" placeholder="Select" clearable>
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Brand">
          <el-input v-model="form.brand" />
        </el-form-item>
        <el-form-item label="Image URL">
          <el-input v-model="form.imageUrl" placeholder="https://…" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="saving" @click="saveProduct">
          {{ editing ? 'Update' : 'Create' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { sellerApi, productApi, messageApi } from '@/api'
import StarRating from '@/components/StarRating.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const tab = ref('products')
const stats = ref(null)
const products = ref([])
const orders = ref([])
const reviews = ref([])
const categories = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const editing = ref(null)
const saving = ref(false)

// Messages (customer inquiries)
const messages = ref([])
const replyDialogVisible = ref(false)
const replyTarget = ref(null)
const replyText = ref('')
const replying = ref(false)

const messagesLabel = computed(() => {
  const unread = messages.value.filter(m => !m.isRead).length
  return unread > 0 ? `💬 Messages (${unread})` : '💬 Messages'
})

const form = ref(blankForm())
function blankForm() {
  return { name: '', description: '', price: 0, stock: 0, categoryId: null, brand: '', imageUrl: '' }
}

function firstImage(p) {
  if (!p.images) return 'https://placehold.co/60x60/F8F8F8/CCC?text=N/A'
  try {
    const arr = JSON.parse(p.images)
    return Array.isArray(arr) ? arr[0] : p.images
  } catch {
    return p.images
  }
}

function trustClass(score) {
  if (!score) return ''
  if (score >= 85) return 'trust-excellent'
  if (score >= 70) return 'trust-high'
  if (score >= 50) return 'trust-medium'
  return 'trust-low'
}

function statusClass(status) {
  return (status || '').toLowerCase()
}

function formatDate(d) {
  return d ? new Date(d).toLocaleString() : ''
}

async function loadAll() {
  loading.value = true
  try {
    const [s, p, o, r, msg] = await Promise.all([
      sellerApi.stats(),
      sellerApi.products(),
      sellerApi.orders(),
      sellerApi.reviews(),
      messageApi.sellerMessages()
    ])
    stats.value = s.data
    products.value = p.data || []
    orders.value = o.data || []
    reviews.value = r.data || []
    messages.value = msg.data || []
  } catch (e) {
    ElMessage.error('Could not load seller data')
  } finally {
    loading.value = false
  }
}

function hasReply(m) {
  return m.messages?.some(msg => msg.sender !== 'USER')
}

function formatTime(d) {
  if (!d) return ''
  return new Date(d).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

function openReply(m) {
  replyTarget.value = m
  replyText.value = ''
  replyDialogVisible.value = true
}

async function sendReply() {
  if (!replyTarget.value || !replyText.value.trim()) return
  replying.value = true
  try {
    await messageApi.reply(replyTarget.value.id, replyText.value.trim())
    ElMessage.success('Reply sent — the customer will be notified')
    replyDialogVisible.value = false
    await loadAll()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || 'Could not send reply')
  } finally {
    replying.value = false
  }
}

async function markRead(m) {
  try {
    await messageApi.markRead(m.id)
    m.isRead = 1
  } catch (e) {
    ElMessage.error('Could not mark as read')
  }
}

function openCreate() {
  editing.value = null
  form.value = blankForm()
  dialogVisible.value = true
}

function openEdit(p) {
  editing.value = p
  let img = ''
  try {
    const arr = JSON.parse(p.images || '[]')
    img = Array.isArray(arr) && arr.length ? arr[0] : ''
  } catch { img = '' }
  form.value = {
    name: p.name,
    description: p.description || '',
    price: Number(p.price),
    stock: p.stock || 0,
    categoryId: p.categoryId,
    brand: p.brand || '',
    imageUrl: img
  }
  dialogVisible.value = true
}

async function saveProduct() {
  if (!form.value.name?.trim()) return ElMessage.warning('Name is required')
  if (form.value.price <= 0) return ElMessage.warning('Price must be greater than 0')

  saving.value = true
  try {
    const payload = {
      name: form.value.name,
      description: form.value.description,
      price: form.value.price,
      stock: form.value.stock,
      categoryId: form.value.categoryId,
      brand: form.value.brand,
      images: form.value.imageUrl ? JSON.stringify([form.value.imageUrl]) : null
    }
    if (editing.value) {
      await productApi.update(editing.value.id, payload)
      ElMessage.success('Product updated')
    } else {
      await productApi.create(payload)
      ElMessage.success('Product created')
    }
    dialogVisible.value = false
    await loadAll()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || 'Could not save product')
  } finally {
    saving.value = false
  }
}

async function removeProduct(p) {
  try {
    await ElMessageBox.confirm(`Delete "${p.name}"?`, 'Delete Product', {
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
      type: 'warning'
    })
    await productApi.delete(p.id)
    ElMessage.success('Product deleted')
    await loadAll()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('Could not delete product')
  }
}

async function updateStatus(o, status) {
  try {
    await sellerApi.updateOrderStatus(o.id, status)
    o.status = status
    ElMessage.success(`Order marked ${status}`)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || 'Could not update order')
  }
}

onMounted(async () => {
  try {
    const c = await productApi.categories()
    categories.value = c.data || []
  } catch (e) { /* ok */ }
  await loadAll()
})
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 14px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border: 1px solid var(--border-light, #ebeef5);
  border-radius: var(--radius-lg, 12px);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-card.warn {
  background: #fffbeb;
  border-color: #fde68a;
}

.stat-label {
  font-size: 12px;
  color: var(--text-muted, #9ca3af);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 24px;
  font-weight: 800;
  color: var(--text, #303133);
}

.tab-head {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.table-wrap {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  background: #fff;
  border-radius: var(--radius-lg, 12px);
  overflow: hidden;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid var(--border-light, #ebeef5);
}

.data-table th {
  background: #fafafa;
  font-weight: 700;
}

.prod-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.prod-cell img {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 6px;
}

.low-stock {
  color: var(--danger, #ef4444);
  font-weight: 700;
}

/* Orders */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-card {
  background: #fff;
  border: 1px solid var(--border-light, #ebeef5);
  border-radius: var(--radius-lg, 12px);
  padding: 16px;
}

.order-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  gap: 10px;
  flex-wrap: wrap;
}

.order-no {
  font-weight: 700;
  color: var(--text, #303133);
}

.order-date {
  font-size: 12px;
  color: var(--text-muted, #9ca3af);
  margin-left: 8px;
}

.status-badge {
  font-size: 11px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 10px;
  background: #f5f7fa;
  color: var(--text-secondary, #606266);
}

.status-badge.pending { background: #fffbeb; color: #b45309; }
.status-badge.processing { background: #eff6ff; color: #1d4ed8; }
.status-badge.shipped { background: #f5f3ff; color: #6d28d9; }
.status-badge.delivered { background: #ecfdf5; color: #16a34a; }
.status-badge.cancelled { background: #fef2f2; color: #dc2626; }

.order-mid {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  align-items: baseline;
  margin-bottom: 12px;
}

.order-total {
  font-size: 18px;
  font-weight: 800;
  color: var(--primary, #ff6b35);
}

.order-ship {
  font-size: 12px;
  color: var(--text-secondary, #606266);
}

.order-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* Reviews */
.review-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.seller-review {
  background: #fff;
  border: 1px solid var(--border-light, #ebeef5);
  border-radius: var(--radius-lg, 12px);
  padding: 16px;
}

.seller-review.flagged {
  border-left: 3px solid var(--danger, #ef4444);
  background: #fffafa;
}

.sr-head {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.sr-product {
  font-weight: 700;
  font-size: 14px;
  color: var(--text, #303133);
}

.sr-date {
  font-size: 11px;
  color: var(--text-muted, #9ca3af);
  margin-left: auto;
}

.sr-content {
  font-size: 13px;
  color: var(--text, #303133);
  line-height: 1.6;
  margin: 0;
}

.sr-ai {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  margin-top: 10px;
  font-size: 11px;
  color: var(--text-secondary, #606266);
}

.verified-badge {
  font-size: 11px;
  font-weight: 700;
  color: #16a34a;
  background: #ecfdf5;
  border: 1px solid #a7f3d0;
  padding: 2px 8px;
  border-radius: 10px;
}

.ai-verdict {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 10px;
}

.verdict-good { color: #16a34a; background: #ecfdf5; }
.verdict-bad { color: #dc2626; background: #fef2f2; }

.fake-reason {
  margin-top: 8px;
  font-size: 12px;
  color: var(--danger, #ef4444);
  background: #fff0f0;
  padding: 6px 12px;
  border-radius: 6px;
}

.trust-excellent { color: #16a34a; font-weight: 700; }
.trust-high { color: #65a30d; font-weight: 700; }
.trust-medium { color: #f59e0b; font-weight: 700; }
.trust-low { color: #ef4444; font-weight: 700; }

/* Messages / inquiries */
.inquiry-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.inquiry-card {
  background: #fff;
  border: 1px solid var(--border-light, #ebeef5);
  border-radius: var(--radius-lg, 12px);
  padding: 16px;
}

.inquiry-card.unread {
  border-left: 3px solid #2563eb;
  background: #f8fbff;
}

.inquiry-card.replied {
  border-left: 3px solid #16a34a;
}

.inquiry-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.inquiry-user {
  font-weight: 700;
  color: var(--text, #303133);
}

.inquiry-new {
  font-size: 10px;
  font-weight: 800;
  color: #fff;
  background: #dc2626;
  padding: 2px 8px;
  border-radius: 10px;
  letter-spacing: 0.5px;
}

.inquiry-product {
  font-size: 12px;
  color: var(--text-secondary, #606266);
  margin-top: 2px;
}

.chat-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 10px;
  padding: 12px;
  max-height: 300px;
  overflow-y: auto;
  margin-bottom: 12px;
}

.dialog-chat {
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 10px;
  padding: 12px;
  max-height: 280px;
  overflow-y: auto;
  margin-bottom: 12px;
}

.chat-row {
  display: flex;
  justify-content: flex-end;
}

.chat-row.from-customer {
  justify-content: flex-start;
}

.bubble-wrap {
  max-width: 82%;
  display: flex;
  flex-direction: column;
}

.bubble-label {
  margin-bottom: 4px;
}

.chat-bubble {
  font-size: 13px;
  line-height: 1.6;
  border-radius: 12px;
  padding: 9px 13px;
  white-space: pre-wrap;
  word-break: break-word;
  background: #fff;
  border: 1px solid #e5e7eb;
  color: #111827;
}

.chat-row.from-customer .chat-bubble {
  background: #fff7ed;
  border: 1px solid #fed7aa;
  color: #7c2d12;
}

.bubble-time {
  font-size: 10px;
  color: var(--text-muted, #9ca3af);
  margin-top: 3px;
}

.inquiry-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.ai-reply-badge {
  display: inline-block;
  font-size: 11px;
  font-weight: 800;
  color: #1e40af;
  background: #dbeafe;
  border: 1px solid #bfdbfe;
  border-radius: 10px;
  padding: 2px 10px;
}

.you-badge {
  display: inline-block;
  font-size: 11px;
  font-weight: 800;
  color: #065f46;
  background: #d1fae5;
  border: 1px solid #a7f3d0;
  border-radius: 10px;
  padding: 2px 10px;
}
</style>
