<template>
  <div class="auth-page">
    <!-- Brand panel -->
    <aside class="auth-hero">
      <router-link to="/" class="auth-hero-logo">
        <BrandMark :size="30" />
        VeriCart<span class="brand-ai">AI</span>
      </router-link>

      <div class="auth-hero-body">
        <h2>{{ $t('auth.registerWelcome') }}</h2>
        <p>{{ $t('auth.registerIntro') }}</p>
        <div class="auth-hero-points">
          <div class="auth-hero-point">
            <el-icon><Warning /></el-icon> {{ $t('home.f2Title') }}
          </div>
          <div class="auth-hero-point">
            <el-icon><ChatDotRound /></el-icon> {{ $t('messages.composerHint') }}
          </div>
          <div class="auth-hero-point">
            <el-icon><Lock /></el-icon> {{ $t('home.f4Desc') }}
          </div>
        </div>
      </div>

      <p class="auth-hero-foot">{{ $t('auth.createOne') }}</p>
    </aside>

    <!-- Form panel -->
    <div class="auth-form-wrap">
      <div class="auth-card">
        <h1>{{ $t('auth.signUpTitle') }}</h1>
        <p class="auth-sub">{{ $t('auth.signUpSub') }}</p>

        <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
          <el-form-item :label="$t('auth.username')" prop="username">
            <el-input
              v-model="form.username"
              :placeholder="$t('auth.usernamePlaceholder')"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>

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
            />
          </el-form-item>

          <el-form-item :label="$t('auth.confirmPassword')" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              :placeholder="$t('auth.confirmPlaceholder')"
              size="large"
              show-password
              :prefix-icon="Lock"
              @keyup.enter="handleRegister"
            />
          </el-form-item>

          <el-button type="primary" size="large" :loading="loading" class="btn-block" @click="handleRegister">
            {{ $t('auth.signUpTitle') }}
          </el-button>
        </el-form>

        <p class="auth-alt">
          {{ $t('auth.hasAccount') }} <router-link to="/login">{{ $t('auth.goSignIn') }}</router-link>
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
import { User, Message, Lock } from '@element-plus/icons-vue'
import BrandMark from '@/components/BrandMark.vue'

const { t } = useI18n()
const router = useRouter()
const auth = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', email: '', password: '', confirmPassword: '' })

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) callback(new Error(t('auth.passwordMismatch')))
  else callback()
}

const rules = computed(() => ({
  username: [
    { required: true, message: t('auth.usernameRequired'), trigger: 'blur' },
    { min: 3, max: 50, message: t('auth.usernameLen'), trigger: 'blur' }
  ],
  email: [{ required: true, message: t('auth.emailRequired'), trigger: 'blur' }],
  password: [
    { required: true, message: t('auth.passwordRequired'), trigger: 'blur' },
    { min: 8, message: t('auth.passwordMin'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: t('auth.confirmPassword'), trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}))

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await auth.register({
      username: form.username,
      email: form.email,
      password: form.password
    })
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>
