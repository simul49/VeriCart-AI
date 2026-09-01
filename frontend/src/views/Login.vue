<template>
  <div class="auth-page">
    <!-- Brand panel -->
    <aside class="auth-hero">
      <router-link to="/" class="auth-hero-logo">
        <BrandMark :size="30" />
        VeriCart<span class="brand-ai">AI</span>
      </router-link>

      <div class="auth-hero-body">
        <h2>{{ $t('auth.loginWelcome') }}</h2>
        <p>{{ $t('auth.loginIntro') }}</p>
        <div class="auth-hero-points">
          <div class="auth-hero-point">
            <el-icon><CircleCheck /></el-icon> {{ $t('nav.aiVerified') }}
          </div>
          <div class="auth-hero-point">
            <el-icon><Odometer /></el-icon> {{ $t('home.f3Desc') }}
          </div>
          <div class="auth-hero-point">
            <el-icon><MagicStick /></el-icon> {{ $t('nav.aiPicks') }}
          </div>
        </div>
      </div>

      <p class="auth-hero-foot">{{ $t('footer.tagline') }}</p>
    </aside>

    <!-- Form panel -->
    <div class="auth-form-wrap">
      <div class="auth-card">
        <h1>{{ $t('auth.signInTitle') }}</h1>
        <p class="auth-sub">{{ $t('auth.signInSub') }}</p>

        <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
          <el-form-item :label="$t('auth.email')" prop="email">
            <el-input
              v-model="form.email"
              :placeholder="$t('auth.emailPlaceholder')"
              size="large"
              :prefix-icon="Message"
            />
          </el-form-item>

          <el-form-item :label="$t('auth.password')" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              :placeholder="$t('auth.passwordPlaceholder')"
              size="large"
              show-password
              :prefix-icon="Lock"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-button type="primary" size="large" :loading="loading" class="btn-block" @click="handleLogin">
            {{ $t('auth.signInTitle') }}
          </el-button>
        </el-form>

        <p class="auth-alt">
          {{ $t('auth.noAccount') }} <router-link to="/register">{{ $t('auth.createOne') }}</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { Message, Lock } from '@element-plus/icons-vue'
import BrandMark from '@/components/BrandMark.vue'

const { t } = useI18n()
const router = useRouter()
const auth = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ email: '', password: '' })
const rules = computed(() => ({
  email: [{ required: true, message: t('auth.emailRequired'), trigger: 'blur' }],
  password: [{ required: true, message: t('auth.passwordRequired'), trigger: 'blur' }]
}))

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await auth.login(form)
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>
