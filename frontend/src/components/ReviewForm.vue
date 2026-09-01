<template>
  <div class="review-form">
    <h4 class="review-form-title">
      {{ isEditing ? '✏️ Edit your review' : '✍️ Write a review' }}
    </h4>

    <p v-if="!isEditing" class="review-form-hint">
      Your review is analysed by AI for authenticity, sentiment and emotion before publishing.
    </p>

    <!-- AI pipeline animation shown while submitting -->
    <AiAnalysisAnimation v-if="submitting" :active="true" :current-step="pipelineStep" />

    <div v-show="!submitting">
      <div class="form-row">
        <label class="form-label">Your rating <span class="req">*</span></label>
        <StarRating v-model="rating" :editable="true" :size="26" :show-label="true" />
      </div>

      <div class="form-row">
        <label class="form-label">
          Your review <span class="req">*</span>
        </label>
        <el-input
          v-model="content"
          type="textarea"
          :rows="4"
          maxlength="2000"
          show-word-limit
          placeholder="What did you like or dislike? Mention specifics like battery life, build quality, delivery…"
        />
      </div>

      <div v-if="errorMsg" class="form-error">{{ errorMsg }}</div>

      <div class="form-actions">
        <button class="btn-primary" :disabled="!valid || submitting" @click="submit">
          {{ isEditing ? 'Update Review' : 'Submit Review' }}
        </button>
        <button v-if="isEditing || showCancel" class="btn-ghost" @click="$emit('cancel')">
          Cancel
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import StarRating from '@/components/StarRating.vue'
import AiAnalysisAnimation from '@/components/AiAnalysisAnimation.vue'
import { reviewApi } from '@/api'
import { ElMessage } from 'element-plus'

const props = defineProps({
  productId: { type: [Number, String], required: true },
  orderId: { type: [Number, String], default: null },
  existing: { type: Object, default: null },
  showCancel: { type: Boolean, default: false }
})

const emit = defineEmits(['submitted', 'cancel'])

const rating = ref(0)
const content = ref('')
const errorMsg = ref('')
const submitting = ref(false)
const pipelineStep = ref(0)

const isEditing = computed(() => !!props.existing)

const valid = computed(() => rating.value >= 1 && content.value.trim().length >= 10)

// Pre-fill when editing
watch(
  () => props.existing,
  (r) => {
    if (r) {
      rating.value = r.rating || 0
      content.value = r.content || ''
    }
  },
  { immediate: true }
)

async function submit() {
  if (!valid.value) {
    errorMsg.value = 'Please give a star rating and write at least 10 characters.'
    return
  }
  errorMsg.value = ''
  submitting.value = true
  pipelineStep.value = 0

  // Visually walk the multi-LLM pipeline while the backend analyses
  const tick = setInterval(() => {
    if (pipelineStep.value < 3) pipelineStep.value++
  }, 700)

  try {
    const payload = {
      productId: Number(props.productId),
      rating: rating.value,
      content: content.value.trim()
    }
    if (props.orderId) payload.orderId = Number(props.orderId)

    if (isEditing.value) {
      await reviewApi.update(props.existing.id, payload)
      ElMessage.success('Review updated')
    } else {
      await reviewApi.create(payload)
      ElMessage.success('Review submitted — AI analysed it')
    }

    // Kick off AI re-analysis so the trust score reflects the new review
    if (!isEditing.value) {
      try { await reviewApi.trustMetrics(props.productId) } catch (e) { /* ok */ }
    }

    if (!isEditing.value) {
      rating.value = 0
      content.value = ''
    }
    emit('submitted')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || 'Could not submit review')
  } finally {
    clearInterval(tick)
    submitting.value = false
  }
}
</script>

<style scoped>
.review-form {
  background: #fff;
  border: 1px solid var(--border-light, #ebeef5);
  border-radius: var(--radius-lg, 12px);
  padding: 20px;
}

.review-form-title {
  font-weight: 700;
  font-size: 16px;
  color: var(--text, #303133);
  margin: 0 0 6px;
}

.review-form-hint {
  font-size: var(--font-xs, 12px);
  color: var(--text-muted, #9ca3af);
  margin-bottom: 16px;
}

.form-row {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  font-weight: 600;
  font-size: var(--font-sm, 14px);
  color: var(--text, #303133);
  margin-bottom: 8px;
}

.req {
  color: var(--danger, #ef4444);
}

.form-error {
  background: #fff0f0;
  color: var(--danger, #ef4444);
  font-size: 13px;
  padding: 8px 12px;
  border-radius: 6px;
  margin-bottom: 12px;
}

.form-actions {
  display: flex;
  gap: 10px;
}

.btn-primary {
  background: var(--primary, #ff6b35);
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 10px 22px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-ghost {
  background: #fff;
  color: var(--text-secondary, #606266);
  border: 1px solid var(--border, #dcdfe6);
  border-radius: 8px;
  padding: 10px 22px;
  font-weight: 600;
  cursor: pointer;
}
</style>
