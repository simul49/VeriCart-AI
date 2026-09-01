<template>
  <div class="chat-app">
    <!-- ============ Conversation sidebar ============ -->
    <aside class="conv-sidebar" :class="{ 'is-open': sidebarOpen }">
      <div class="sidebar-head">
        <div>
          <h2>{{ $t('messages.title') }}</h2>
          <p class="sidebar-sub">{{ $t('messages.threadCountPlural', { count: threads.length }) }}</p>
        </div>
        <button class="icon-btn" :title="$t('common.close')" @click="sidebarOpen = false">
          <el-icon><Close /></el-icon>
        </button>
      </div>

      <div class="sidebar-search">
        <el-icon><Search /></el-icon>
        <input v-model="convQuery" :placeholder="$t('messages.searchConversations')" />
      </div>

      <router-link to="/products" class="new-chat">
        <el-icon><Promotion /></el-icon> {{ $t('messages.newConversation') }}
      </router-link>

      <div class="conv-list">
        <button
          v-for="t in filteredThreads"
          :key="t.id"
          :class="['conv-item', { active: t.id === activeId }]"
          @click="selectThread(t)"
        >
          <span class="conv-avatar"><el-icon><Goods /></el-icon></span>
          <span class="conv-body">
            <span class="conv-title">{{ t.productName || 'Product' }}</span>
            <span class="conv-preview">{{ preview(t) }}</span>
          </span>
          <span class="conv-meta">
            <span class="conv-time">{{ shortTime(lastAt(t)) }}</span>
            <span v-if="isPending(t)" class="conv-dot" title="AI is replying" />
          </span>
        </button>

        <p v-if="!filteredThreads.length && threads.length" class="conv-empty">
          {{ $t('messages.noMatch', { q: convQuery }) }}
        </p>
      </div>
    </aside>

    <!-- ============ Chat pane ============ -->
    <section v-if="activeThread" class="chat-pane">
      <header class="chat-top">
        <button class="menu-btn" @click="sidebarOpen = true" aria-label="Show conversations">
          <el-icon><ChatLineRound /></el-icon>
        </button>
        <div class="chat-top-info">
          <h3>{{ activeThread.productName || $t('common.product') }}</h3>
          <p class="chat-top-sub">
            <span class="live-dot" />
            {{ $t('messages.aiOnline') }}
          </p>
        </div>
        <router-link :to="`/products/${activeThread.productId}`" class="btn btn-sm btn-outline view-product">
          {{ $t('messages.viewProduct') }}
        </router-link>
        <span :class="['status-chip', (activeThread.status || '').toLowerCase()]">
          {{ activeThread.status }}
        </span>
      </header>

      <!-- Message stream -->
      <div class="chat-stream" ref="streamEl">
        <div class="stream-inner">
          <div v-for="msg in activeThread.messages" :key="msg.id" :class="['msg-row', roleOf(msg)]">
            <!-- Avatar (AI / seller only) -->
            <span v-if="roleOf(msg) !== 'user'" :class="['msg-avatar', roleOf(msg)]">
              <el-icon v-if="roleOf(msg) === 'ai'"><MagicStick /></el-icon>
              <el-icon v-else><Shop /></el-icon>
            </span>

            <div class="msg-col">
              <div class="msg-meta">
                <span class="msg-author">
                  {{ roleOf(msg) === 'ai' ? $t('messages.aiAssistant')
                    : roleOf(msg) === 'seller' ? $t('messages.storeOwner') : $t('messages.you') }}
                </span>
                <span class="msg-time">{{ timeAt(msg.createdAt) }}</span>
              </div>

              <div :class="['msg-bubble', roleOf(msg)]">
                <div v-if="roleOf(msg) === 'user'" class="msg-text">{{ msg.content }}</div>
                <div v-else class="msg-md" v-html="renderMarkdown(msg.content)" />
              </div>

              <button
                v-if="roleOf(msg) !== 'user'"
                class="msg-copy"
                :title="$t('messages.copy')"
                @click="copyText(msg.content)"
              >
                <el-icon><DocumentCopy /></el-icon>
              </button>
            </div>
          </div>

          <!-- AI thinking indicator (every question gets an answer) -->
          <div v-if="isPending(activeThread)" class="msg-row ai">
            <span class="msg-avatar ai"><el-icon><MagicStick /></el-icon></span>
            <div class="msg-col">
              <div class="msg-meta"><span class="msg-author">{{ $t('messages.aiAssistant') }}</span></div>
              <div class="msg-bubble ai is-thinking">
                <span class="dot" /><span class="dot" /><span class="dot" />
                <span class="thinking-label">{{ thinkingLabel }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Composer -->
      <div class="composer-wrap">
        <div v-if="suggestions.length && !isPending(activeThread)" class="suggestions">
          <button v-for="s in suggestions" :key="s" class="suggest-chip" @click="useSuggestion(s)">
            {{ s }}
          </button>
        </div>

        <div class="composer" :class="{ 'is-busy': sending }">
          <textarea
            ref="inputEl"
            v-model="draft"
            rows="1"
            :placeholder="$t('messages.placeholder')"
            @keydown.enter.exact.prevent="send"
            @input="autoGrow"
          />
          <button
            class="send-btn"
            :disabled="!draft.trim() || sending"
            :title="sending ? $t('common.sending') : $t('common.send')"
            @click="send"
          >
            <el-icon><Promotion /></el-icon>
          </button>
        </div>

        <p class="composer-hint">
          <el-icon><InfoFilled /></el-icon>
          {{ $t('messages.composerHint') }}
        </p>
      </div>
    </section>

    <!-- Empty state -->
    <section v-else class="chat-pane is-empty">
      <div class="empty-chat">
        <span class="empty-glyph"><el-icon><ChatDotRound /></el-icon></span>
        <h2>{{ $t('messages.emptyTitle') }}</h2>
        <p>{{ $t('messages.emptySub') }}</p>
        <router-link to="/products" class="btn btn-primary">
          <el-icon><Goods /></el-icon> {{ $t('messages.browseProducts') }}
        </router-link>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { messageApi } from '@/api'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import {
  Close, Search, Promotion, Goods, MagicStick, Shop, ChatLineRound,
  ChatDotRound, DocumentCopy, InfoFilled
} from '@element-plus/icons-vue'

const { t } = useI18n()

/* ---------------- State ---------------- */
const route = useRoute()
const threads = ref([])
const loading = ref(true)
const sending = ref(false)
const activeId = ref(null)
const draft = ref('')
const convQuery = ref('')
const sidebarOpen = ref(false)
const streamEl = ref(null)
const inputEl = ref(null)

let pollTimer = null
let pollCount = 0
const POLL_MS = 2500
const MAX_POLLS = 40 // ~100s — plenty for the AI to answer

const activeThread = computed(() => threads.value.find(t => t.id === activeId.value) || null)

const filteredThreads = computed(() => {
  const q = convQuery.value.trim().toLowerCase()
  if (!q) return threads.value
  return threads.value.filter(t =>
    (t.productName || '').toLowerCase().includes(q) ||
    (t.messages || []).some(m => (m.content || '').toLowerCase().includes(q))
  )
})

/* Suggested follow-ups, contextual to the last AI answer */
const suggestions = computed(() => {
  const th = activeThread.value
  if (!th) return []
  const msgs = th.messages || []
  if (!msgs.length) {
    return [
      t('messages.suggestDefault1'),
      t('messages.suggestDefault2'),
      t('messages.suggestDefault3')
    ]
  }
  const last = msgs[msgs.length - 1]
  if (last.sender === 'USER') return []
  return [t('messages.suggestThanks'), t('messages.suggestMore'), t('messages.suggestReturns')]
})

const thinkingLabel = computed(() => {
  const th = activeThread.value
  const turns = (th?.messages || []).filter(m => m.sender === 'USER').length
  return turns > 1 ? t('messages.thinkingFollowUp') : t('messages.thinking')
})

/* ---------------- Helpers ---------------- */
const roleOf = (msg) => {
  if (msg.sender === 'USER') return 'user'
  if (msg.sender === 'SELLER') return 'seller'
  return 'ai'
}

const lastAt = (t) => {
  const msgs = t.messages || []
  return msgs.length ? msgs[msgs.length - 1].createdAt : (t.updatedAt || t.createdAt)
}

const preview = (thread) => {
  const msgs = thread.messages || []
  if (!msgs.length) return thread.message || t('messages.noMessages')
  const last = msgs[msgs.length - 1]
  const role = roleOf(last)
  const who = role === 'user'
    ? t('messages.you') + ': '
    : role === 'ai' ? t('messages.aiAssistant') + ': ' : t('messages.storeOwner') + ': '
  return who + String(last.content).replace(/\s+/g, ' ').slice(0, 46)
}

const isPending = (t) => {
  const msgs = t?.messages || []
  return msgs.length > 0 && msgs[msgs.length - 1].sender === 'USER'
}

function timeAt(d) {
  if (!d) return ''
  return new Date(d).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

function shortTime(d) {
  if (!d) return ''
  const date = new Date(d)
  const today = new Date()
  const sameDay = date.toDateString() === today.toDateString()
  return sameDay
    ? date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    : date.toLocaleDateString([], { day: '2-digit', month: 'short' })
}

/** Escape angle brackets so AI output can never inject markup, then render markdown. */
function renderMarkdown(text) {
  try {
    const safe = String(text)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
    return marked.parse(safe, { breaks: true })
  } catch {
    return String(text).replace(/</g, '&lt;')
  }
}

async function copyText(text) {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(t('common.copied'))
  } catch {
    ElMessage.warning(t('common.copyFail'))
  }
}

function autoGrow() {
  const el = inputEl.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 160) + 'px'
}

function useSuggestion(s) {
  draft.value = s
  nextTick(() => inputEl.value?.focus())
}

/* ---------------- Data ---------------- */
async function load() {
  const res = await messageApi.myMessages()
  threads.value = (res.data || []).map(t => ({ ...t, messages: t.messages || [] }))
  if (activeId.value == null && threads.value.length) activeId.value = threads.value[0].id
  return threads.value
}

function scrollToBottom() {
  nextTick(() => {
    const el = streamEl.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

function startPolling() {
  stopPolling()
  pollCount = 0
  pollTimer = setInterval(async () => {
    pollCount++
    try {
      await load()
      scrollToBottom()
    } catch { /* keep last known state */ }
    if (!threads.value.some(isPending) || pollCount >= MAX_POLLS) stopPolling()
  }, POLL_MS)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

function selectThread(t) {
  activeId.value = t.id
  sidebarOpen.value = false
  scrollToBottom()
}

async function send() {
  const text = draft.value.trim()
  if (!text || sending.value || !activeThread.value) return

  sending.value = true
  const threadId = activeThread.value.id
  try {
    await messageApi.sendFollowUp(threadId, text)
    draft.value = ''
    if (inputEl.value) inputEl.value.style.height = 'auto'
    await load()
    scrollToBottom()
    startPolling() // wait for the AI to answer this question too
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || 'Could not send your message')
  } finally {
    sending.value = false
  }
}

/* ---------------- Lifecycle ---------------- */
onMounted(async () => {
  try {
    await load()
    // Deep link from the product page (?thread=<id>) opens that conversation
    const requested = Number(route.query.thread)
    if (requested && threads.value.some(t => t.id === requested)) {
      activeId.value = requested
    }
    scrollToBottom()
    if (threads.value.some(isPending)) startPolling()
  } catch {
    ElMessage.error(t('errors.loadFailed'))
  } finally {
    loading.value = false
  }
})

onBeforeUnmount(stopPolling)

// Keep the newest message in view as the thread grows
watch(
  () => activeThread.value?.messages?.length,
  () => scrollToBottom()
)
</script>

<style scoped>
.chat-app {
  position: relative;
  display: grid;
  grid-template-columns: 300px 1fr;
  height: calc(100vh - 148px);
  min-height: 520px;
  max-width: var(--maxw);
  margin: 0 auto;
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

/* ================= Sidebar ================= */
.conv-sidebar {
  border-right: 1px solid var(--border-light);
  background: #FCFCFD;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.sidebar-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 18px 16px 12px;
}
.sidebar-head h2 {
  font-size: var(--font-lg);
  font-weight: 800;
  color: var(--ink);
  letter-spacing: -0.02em;
}
.sidebar-sub { font-size: var(--font-xs); color: var(--text-muted); margin-top: 1px; }
.icon-btn {
  display: none;
  width: 32px; height: 32px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: #fff;
  cursor: pointer;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
}

.sidebar-search {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 16px 12px;
  padding: 0 12px;
  height: 38px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  transition: border-color var(--transition);
}
.sidebar-search:focus-within { border-color: var(--primary); }
.sidebar-search .el-icon { color: var(--text-muted); }
.sidebar-search input {
  flex: 1; min-width: 0;
  border: none; outline: none; background: transparent;
  font-size: var(--font-sm); font-family: inherit;
}

.new-chat {
  margin: 0 16px 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  height: 38px;
  border-radius: var(--radius-md);
  background: var(--primary);
  color: #fff;
  font-size: var(--font-sm);
  font-weight: 700;
  transition: background var(--transition);
}
.new-chat:hover { background: var(--primary-hover); }

.conv-list { flex: 1; overflow-y: auto; padding: 0 8px 12px; min-height: 0; }
.conv-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  cursor: pointer;
  text-align: left;
  font-family: inherit;
  transition: background var(--transition);
}
.conv-item:hover { background: #F3F4F7; }
.conv-item.active { background: var(--primary-light); }

.conv-avatar {
  width: 34px; height: 34px;
  flex-shrink: 0;
  border-radius: var(--radius-md);
  background: #fff;
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  font-size: 16px;
}
.conv-item.active .conv-avatar { border-color: var(--primary-soft); color: var(--primary); }

.conv-body { flex: 1; min-width: 0; }
.conv-title {
  display: block;
  font-size: var(--font-sm);
  font-weight: 700;
  color: var(--ink);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.conv-preview {
  display: block;
  font-size: var(--font-xs);
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 1px;
}
.conv-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  flex-shrink: 0;
}
.conv-time { font-size: 10px; color: var(--text-muted); }
.conv-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: var(--primary);
  animation: pulse 1.3s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 0.35; transform: scale(0.85); }
  50% { opacity: 1; transform: scale(1.1); }
}
.conv-empty { padding: 16px 12px; font-size: var(--font-xs); color: var(--text-muted); text-align: center; }

/* ================= Chat pane ================= */
.chat-pane { display: flex; flex-direction: column; min-height: 0; min-width: 0; }

.chat-top {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  border-bottom: 1px solid var(--border-light);
  background: #fff;
  flex-shrink: 0;
}
.menu-btn {
  display: none;
  width: 34px; height: 34px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: #fff;
  cursor: pointer;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  color: var(--text-secondary);
}
.chat-top-info { flex: 1; min-width: 0; }
.chat-top-info h3 {
  font-size: var(--font-md);
  font-weight: 700;
  color: var(--ink);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.chat-top-sub {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--font-xs);
  color: var(--text-muted);
  margin-top: 1px;
}
.live-dot {
  width: 7px; height: 7px;
  border-radius: 50%;
  background: var(--success);
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.18);
}
.view-product { flex-shrink: 0; }
.status-chip {
  flex-shrink: 0;
  padding: 3px 10px;
  border-radius: var(--radius-full);
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.04em;
  background: var(--surface-muted);
  color: var(--text-secondary);
}
.status-chip.open { background: #FFF1EE; color: var(--primary-dark); }
.status-chip.replied { background: var(--success-light); color: #067A56; }

/* ---- Stream ---- */
.chat-stream {
  flex: 1;
  overflow-y: auto;
  padding: 24px 20px 8px;
  background: #FBFBFC;
  min-height: 0;
}
.stream-inner {
  max-width: 760px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.msg-row { display: flex; gap: 10px; align-items: flex-start; }
.msg-row.user { justify-content: flex-end; }

.msg-avatar {
  width: 30px; height: 30px;
  flex-shrink: 0;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  color: #fff;
}
.msg-avatar.ai { background: linear-gradient(135deg, #FF7A5A, #F03524); }
.msg-avatar.seller { background: var(--ink); }

.msg-col { max-width: 78%; min-width: 0; display: flex; flex-direction: column; }
.msg-row.user .msg-col { align-items: flex-end; }

.msg-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 5px;
}
.msg-author { font-size: 11px; font-weight: 700; color: var(--text-secondary); }
.msg-time { font-size: 10px; color: var(--text-muted); }

.msg-bubble {
  padding: 11px 15px;
  border-radius: 14px;
  font-size: var(--font-md);
  line-height: 1.68;
  word-break: break-word;
  overflow-wrap: anywhere;
}
.msg-bubble.user {
  background: linear-gradient(135deg, #FF6A5A, #FF4D3D);
  color: #fff;
  border-top-right-radius: 5px;
  box-shadow: 0 2px 10px rgba(255, 77, 61, 0.22);
}
.msg-bubble.ai {
  background: #fff;
  border: 1px solid var(--border-light);
  color: var(--text);
  border-top-left-radius: 5px;
  box-shadow: var(--shadow-xs);
}
.msg-bubble.seller {
  background: var(--success-light);
  border: 1px solid #C6EFDF;
  color: #0B3B2E;
  border-top-left-radius: 5px;
}
.msg-text { white-space: pre-wrap; }

/* Markdown inside AI / seller replies */
.msg-md :deep(p) { margin: 0 0 8px; }
.msg-md :deep(p:last-child) { margin-bottom: 0; }
.msg-md :deep(strong) { font-weight: 700; }
.msg-md :deep(ul),
.msg-md :deep(ol) { padding-left: 20px; margin: 6px 0; }
.msg-md :deep(li) { margin-bottom: 3px; }
.msg-md :deep(code) {
  background: var(--surface-muted);
  padding: 1px 5px;
  border-radius: 4px;
  font-size: 0.9em;
}
.msg-md :deep(a) { color: var(--primary); text-decoration: underline; }

.msg-copy {
  align-self: flex-start;
  margin-top: 5px;
  width: 26px; height: 26px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  background: #fff;
  color: var(--text-muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  opacity: 0;
  transition: all var(--transition);
}
.msg-row:hover .msg-copy { opacity: 1; }
.msg-copy:hover { color: var(--primary); border-color: var(--primary); }

/* Thinking indicator */
.msg-bubble.is-thinking {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 13px 16px;
}
.dot {
  width: 7px; height: 7px;
  border-radius: 50%;
  background: var(--primary);
  animation: bounce 1.3s infinite ease-in-out both;
}
.dot:nth-child(2) { animation-delay: 0.18s; }
.dot:nth-child(3) { animation-delay: 0.36s; }
@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}
.thinking-label { font-size: var(--font-sm); color: var(--text-muted); margin-left: 6px; }

/* ---- Composer ---- */
.composer-wrap {
  flex-shrink: 0;
  padding: 12px 20px 16px;
  background: #fff;
  border-top: 1px solid var(--border-light);
}
.suggestions {
  max-width: 760px;
  margin: 0 auto 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.suggest-chip {
  padding: 6px 13px;
  border: 1px solid var(--border);
  border-radius: var(--radius-full);
  background: #fff;
  color: var(--text-secondary);
  font-size: var(--font-xs);
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition);
}
.suggest-chip:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: var(--primary-light);
}

.composer {
  max-width: 760px;
  margin: 0 auto;
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 8px 8px 8px 16px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  transition: all var(--transition);
}
.composer:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(255, 77, 61, 0.1);
}
.composer textarea {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  resize: none;
  background: transparent;
  font-family: inherit;
  font-size: var(--font-md);
  line-height: 1.6;
  color: var(--text);
  max-height: 160px;
  padding: 6px 0;
}
.composer textarea::placeholder { color: var(--text-muted); }

.send-btn {
  width: 38px; height: 38px;
  flex-shrink: 0;
  border: none;
  border-radius: var(--radius-md);
  background: var(--primary);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all var(--transition);
}
.send-btn:hover:not(:disabled) { background: var(--primary-hover); transform: translateY(-1px); }
.send-btn:disabled { background: #DDE1E9; color: #9AA4B5; cursor: not-allowed; }

.composer-hint {
  max-width: 760px;
  margin: 9px auto 0;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: var(--text-muted);
}

/* ---- Empty chat ---- */
.chat-pane.is-empty { align-items: center; justify-content: center; }
.empty-chat { text-align: center; padding: 40px 24px; max-width: 420px; }
.empty-glyph {
  width: 68px; height: 68px;
  margin: 0 auto 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  background: var(--primary-light);
  color: var(--primary);
  font-size: 30px;
}
.empty-chat h2 {
  font-size: var(--font-2xl);
  font-weight: 800;
  color: var(--ink);
  letter-spacing: -0.02em;
  margin-bottom: 8px;
}
.empty-chat p { color: var(--text-secondary); font-size: var(--font-md); margin-bottom: 22px; line-height: 1.7; }

/* ================= Responsive ================= */
@media (max-width: 900px) {
  .chat-app { grid-template-columns: 1fr; height: calc(100vh - 120px); }
  .conv-sidebar {
    position: absolute;
    inset: 0 auto 0 0;
    width: 84%;
    max-width: 320px;
    z-index: 20;
    transform: translateX(-100%);
    transition: transform var(--transition-slow);
    box-shadow: var(--shadow-xl);
  }
  .conv-sidebar.is-open { transform: translateX(0); }
  .icon-btn { display: flex; }
  .menu-btn { display: flex; }
  .view-product { display: none; }
  .msg-col { max-width: 88%; }
}
</style>
