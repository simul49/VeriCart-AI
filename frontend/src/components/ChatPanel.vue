<template>
  <div class="chat-panel">
    <div class="chat-header">
      <div style="display:flex;align-items:center;gap:8px">
        <span>🤖</span>
        <div>
          <div style="font-weight:600">AI Shopping Assistant</div>
          <div style="font-size:11px;opacity:0.85">{{ productId ? 'Ask me about this product' : 'Ask me anything about products' }}</div>
        </div>
      </div>
      <el-button text style="color:white" @click="$emit('close')">✕</el-button>
    </div>

    <div class="chat-messages" ref="msgContainer">
      <div v-if="messages.length === 0" class="chat-welcome">
        <div v-if="productId" style="font-weight:600;margin-bottom:8px">
          Hi! Ask me anything about <span style="color:var(--primary)">{{ productName || 'this product' }}</span>.
        </div>
        <div v-else style="font-weight:600;margin-bottom:8px">
          Hi, I'm your AI shopping assistant!
        </div>
        <div style="margin-bottom:12px">
          {{ productId
            ? 'I can answer questions about specs, reviews, value, and how it compares to similar products.'
            : 'I can compare products, check trust scores, summarize reviews, or help you find the best deal. Feel free to ask anything — long or short.' }}
        </div>
        <div class="chat-suggestions">
          <button v-for="s in suggestions" :key="s" @click="sendSuggestion(s)">{{ s }}</button>
        </div>
      </div>

      <div v-for="(msg, i) in messages" :key="i"
        :style="{ textAlign: msg.role === 'user' ? 'right' : 'left', marginBottom: '12px' }">
        <div :class="['chat-bubble', msg.role]">
          <div v-html="renderMarkdown(msg.content)"></div>
        </div>
      </div>

      <div v-if="loading" class="chat-typing">
        <span></span><span></span><span></span>
      </div>
    </div>

    <div class="chat-input">
      <el-input
        v-model="input"
        type="textarea"
        :rows="1"
        autosize
        maxlength="800"
        show-word-limit
        placeholder="Ask anything about products, reviews, or deals..."
        @keydown.enter.exact.prevent="send"
        @keydown.enter.shift.exact.stop
      />
      <el-button type="primary" size="small" @click="send" :disabled="!input.trim()" :loading="loading">
        Send
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch, computed } from 'vue'
import { aiApi } from '@/api'
import { ElMessage, ElInput } from 'element-plus'
import { marked } from 'marked'

defineEmits(['close'])

const props = defineProps({
  productId: { type: Number, default: null },
  productName: { type: String, default: '' }
})

const input = ref('')
const messages = ref([])
const loading = ref(false)
const msgContainer = ref(null)

const genericSuggestions = [
  'Which product has the best trust score?',
  'Compare the top-rated headphones',
  'What are common complaints?',
  'Find me a laptop under $1000'
]

const productSuggestions = [
  'Is this product worth the price?',
  'What do reviews say about quality?',
  'What are common complaints about it?',
  'How does this compare to similar products?'
]

const suggestions = computed(() => props.productId ? productSuggestions : genericSuggestions)

function renderMarkdown(text) {
  try {
    return marked(text)
  } catch {
    return text
  }
}

function sendSuggestion(text) {
  input.value = text
  send()
}

async function send() {
  const text = input.value.trim()
  if (!text) return

  const history = messages.value.map(m => ({ role: m.role, content: m.content }))
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true

  try {
    const payload = { question: text, history }
    if (props.productId) payload.productId = props.productId

    const res = await aiApi.chat(payload)
    const answer = res.data?.answer || res?.answer || 'I\'m not sure about that. Try rephrasing your question.'
    messages.value.push({ role: 'assistant', content: answer })
  } catch (e) {
    const isTimeout = e?.code === 'ECONNABORTED' || /timeout/i.test(e?.message || '')
    const msg = isTimeout
      ? '⏳ The AI is taking longer than usual. Please try again in a moment.'
      : 'Sorry, I encountered an error. Please try again.'
    messages.value.push({ role: 'assistant', content: msg })
  } finally {
    loading.value = false
    await nextTick()
    if (msgContainer.value) msgContainer.value.scrollTop = msgContainer.value.scrollHeight
  }
}
</script>

<style scoped>
.chat-welcome {
  color: #4B5563;
  font-size: 13px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 14px;
  margin-bottom: 12px;
}

.chat-suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.chat-suggestions button {
  background: white;
  border: 1px solid #C7D2FE;
  color: var(--primary);
  border-radius: 16px;
  padding: 6px 12px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.chat-suggestions button:hover {
  background: #EEF2FF;
}

.chat-bubble {
  display: inline-block;
  max-width: 85%;
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 14px;
  text-align: left;
  line-height: 1.5;
}

.chat-bubble.user {
  background: var(--primary);
  color: white;
  border-bottom-right-radius: 4px;
}

.chat-bubble.assistant {
  background: #F1F5F9;
  color: var(--text);
  border-bottom-left-radius: 4px;
}

.chat-bubble :deep(p) { margin: 0 0 6px; }
.chat-bubble :deep(p:last-child) { margin-bottom: 0; }
.chat-bubble :deep(strong) { font-weight: 600; }
.chat-bubble :deep(ul) { padding-left: 18px; margin: 6px 0; }
.chat-bubble :deep(li) { margin-bottom: 4px; }

.chat-typing {
  display: flex; gap: 4px; padding: 8px 0;
}

.chat-typing span {
  width: 7px; height: 7px;
  background: #CBD5E1;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out both;
}

.chat-typing span:nth-child(1) { animation-delay: 0s; }
.chat-typing span:nth-child(2) { animation-delay: 0.2s; }
.chat-typing span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}
</style>
