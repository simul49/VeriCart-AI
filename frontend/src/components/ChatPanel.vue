<template>
  <div class="chat-panel">
    <div class="chat-header">
      <span>🤖 AI Shopping Assistant</span>
      <el-button text style="color:white" @click="$emit('close')">✕</el-button>
    </div>

    <div class="chat-messages" ref="msgContainer">
      <div v-if="messages.length === 0" style="color:#9CA3AF;font-size:13px">
        👋 Hi! I'm your AI shopping assistant. Ask me about any product:
        <ul style="margin-top:8px;padding-left:16px">
          <li>"Should I buy this?"</li>
          <li>"Which product is better?"</li>
          <li>"Is this trustworthy?"</li>
          <li>"What problems do customers mention?"</li>
        </ul>
      </div>

      <div v-for="(msg, i) in messages" :key="i"
        :style="{ textAlign: msg.role === 'user' ? 'right' : 'left', marginBottom: '12px' }">
        <div :style="{
          display: 'inline-block', maxWidth: '80%', padding: '8px 12px', borderRadius: '12px',
          background: msg.role === 'user' ? 'var(--primary)' : '#F1F5F9',
          color: msg.role === 'user' ? 'white' : 'var(--text)',
          fontSize: '14px', textAlign: 'left'
        }">{{ msg.content }}</div>
      </div>

      <div v-if="loading" style="color:#9CA3AF;font-size:13px">Thinking...</div>
    </div>

    <div class="chat-input">
      <input v-model="input" placeholder="Ask about a product..." @keyup.enter="send" />
      <el-button type="primary" size="small" @click="send" :disabled="!input.trim()">Send</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { aiApi } from '@/api'
import { ElMessage } from 'element-plus'

defineEmits(['close'])

const props = defineProps({ productId: { type: Number, default: null } })

const input = ref('')
const messages = ref([])
const loading = ref(false)
const msgContainer = ref(null)

async function send() {
  const text = input.value.trim()
  if (!text) return

  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true

  try {
    const payload = { question: text }
    if (props.productId) payload.productId = props.productId

    const res = await aiApi.chat(payload)
    const answer = res.data?.answer || res?.answer || 'I couldn\'t analyze that. Try asking differently.'
    messages.value.push({ role: 'assistant', content: answer })
  } catch {
    messages.value.push({ role: 'assistant', content: 'Sorry, I encountered an error. Please try again.' })
  } finally {
    loading.value = false
    await nextTick()
    if (msgContainer.value) msgContainer.value.scrollTop = msgContainer.value.scrollHeight
  }
}
</script>
