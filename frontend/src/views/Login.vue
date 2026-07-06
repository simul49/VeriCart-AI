<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2>Welcome Back</h2>
      <p style="color:#6B7280;margin-bottom:24px">Sign in to your VeriCart AI account</p>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" placeholder="you@example.com" size="large" />
        </el-form-item>
        <el-form-item label="Password" prop="password">
          <el-input v-model="form.password" type="password" placeholder="Enter password" size="large"
            show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" style="width:100%"
          @click="handleLogin">Sign In</el-button>
      </el-form>

      <p style="text-align:center;margin-top:16px;color:#6B7280">
        Don't have an account? <router-link to="/register">Create one</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ email: '', password: '' })
const rules = {
  email: [{ required: true, message: 'Email is required', trigger: 'blur' }],
  password: [{ required: true, message: 'Password is required', trigger: 'blur' }]
}

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

<style scoped>
.auth-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #EEF2FF 0%, #E0E7FF 100%);
}
.auth-card {
  background: white; padding: 40px; border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08); width: 100%; max-width: 420px;
}
.auth-card h2 { font-size: 28px; font-weight: 700; margin-bottom: 4px; }
</style>
