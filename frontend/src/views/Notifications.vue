<template>
  <div class="page-container">
    <div class="notif-head">
      <h2 class="section-title">🔔 Notifications</h2>
      <el-button v-if="unread > 0" type="primary" size="small" @click="markAllRead">
        Mark all as read ({{ unread }})
      </el-button>
    </div>

    <!-- Filter tabs -->
    <el-tabs v-model="activeTab" class="notif-tabs">
      <el-tab-pane label="All" name="all" />
      <el-tab-pane label="Unread" name="unread" />
      <el-tab-pane label="Orders" name="ORDER" />
      <el-tab-pane label="Reviews" name="REVIEW" />
      <el-tab-pane label="AI" name="AI" />
    </el-tabs>

    <div v-if="loading" class="empty-state">Loading…</div>

    <div v-else-if="filtered.length" class="notif-list">
      <div
        v-for="n in filtered"
        :key="n.id"
        :class="['notif-item', { unread: !n.isRead }]"
        @click="markRead(n)"
      >
        <span class="notif-icon">{{ iconFor(n.type) }}</span>

        <div class="notif-body">
          <div class="notif-title-row">
            <span class="notif-title">{{ n.title }}</span>
            <span class="notif-type">{{ n.type }}</span>
          </div>
          <p class="notif-content">{{ n.content }}</p>
          <span class="notif-date">{{ formatDate(n.createdAt) }}</span>
        </div>

        <div class="notif-actions">
          <span v-if="!n.isRead" class="unread-dot" title="Unread"></span>
          <el-button text size="small" @click.stop="remove(n.id)">🗑️</el-button>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <p style="font-size:40px;margin-bottom:8px">🔔</p>
      <p style="font-weight:600;color:var(--text)">No notifications</p>
      <p style="margin-top:4px">Order updates, review alerts and AI results will appear here.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { notificationApi } from '@/api'
import { ElMessage } from 'element-plus'

const items = ref([])
const unread = ref(0)
const loading = ref(true)
const activeTab = ref('all')

const filtered = computed(() => {
  if (activeTab.value === 'all') return items.value
  if (activeTab.value === 'unread') return items.value.filter(n => !n.isRead)
  return items.value.filter(n => n.type === activeTab.value)
})

const ICONS = { ORDER: '📦', REVIEW: '⭐', SYSTEM: '⚙️', AI: '🤖' }
function iconFor(type) {
  return ICONS[type] || '🔔'
}

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleString()
}

async function load() {
  loading.value = true
  try {
    const res = await notificationApi.list(50)
    items.value = res.data?.items || []
    unread.value = res.data?.unread || 0
  } catch (e) {
    ElMessage.error('Could not load notifications')
  } finally {
    loading.value = false
  }
}

async function markRead(n) {
  if (n.isRead) return
  try {
    await notificationApi.markRead(n.id)
    n.isRead = 1
    unread.value = Math.max(0, unread.value - 1)
  } catch (e) { /* silent */ }
}

async function markAllRead() {
  try {
    await notificationApi.markAllRead()
    items.value.forEach(n => (n.isRead = 1))
    unread.value = 0
    ElMessage.success('All notifications marked as read')
  } catch (e) {
    ElMessage.error('Could not update notifications')
  }
}

async function remove(id) {
  try {
    await notificationApi.delete(id)
    items.value = items.value.filter(n => n.id !== id)
    ElMessage.success('Notification deleted')
  } catch (e) {
    ElMessage.error('Could not delete notification')
  }
}

onMounted(load)
</script>

<style scoped>
.notif-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.notif-tabs {
  margin-bottom: 8px;
}

.notif-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.notif-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  background: #fff;
  border: 1px solid var(--border-light, #ebeef5);
  border-radius: var(--radius-lg, 12px);
  padding: 14px 18px;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.notif-item:hover {
  border-color: var(--primary, #ff6b35);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
}

.notif-item.unread {
  background: #fffaf5;
  border-left: 3px solid var(--primary, #ff6b35);
}

.notif-icon {
  font-size: 22px;
  line-height: 1.2;
}

.notif-body {
  flex: 1;
  min-width: 0;
}

.notif-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.notif-title {
  font-weight: 700;
  font-size: 14px;
  color: var(--text, #303133);
}

.notif-type {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: var(--text-secondary, #606266);
  background: #f5f7fa;
  padding: 2px 7px;
  border-radius: 8px;
}

.notif-content {
  margin: 4px 0;
  font-size: 13px;
  color: var(--text-secondary, #606266);
  line-height: 1.6;
}

.notif-date {
  font-size: 11px;
  color: var(--text-muted, #9ca3af);
}

.notif-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary, #ff6b35);
}
</style>
