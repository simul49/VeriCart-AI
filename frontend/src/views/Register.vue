<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2>Create Account</h2>
      <p style="color:#6B7280;margin-bottom:24px">Join VeriCart AI for a smarter shopping experience</p>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="Username" prop="username">
          <el-input v-model="form.username" placeholder="Choose a username" size="large" />
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" placeholder="you@example.com" size="large" />
        </el-form-item>
        <el-form-item label="Password" prop="password">
          <el-input v-model="form.password" type="password" placeholder="At least 8 characters"
            size="large" show-password />
        </el-form-item>
        <el-form-item label="Confirm Password" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="Re-enter password"
            size="large" show-password @keyup.enter="handleRegister" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" style="width:100%"
          @click="handleRegister">Create Account</el-button>
      </el-form>

      <p style="text-align:center;margin-top:16px;color:#6B7280">
        Already have an account? <router-link to="/login">Sign in</router-link>
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

const form = reactive({
  username: '', email: '', password: '', confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) callback(new Error('Passwords do not match'))
  else callback()
}

const rules = {
  username: [
    { required: true, message: 'Username is required', trigger: 'blur' },
    { min: 3, max: 50, message: 'Username must be 3-50 characters', trigger: 'blur' }
  ],
  email: [{ required: true, message: 'Email is required', trigger: 'blur' }],
  password: [
    { required: true, message: 'Password is required', trigger: 'blur' },
    { min: 8, message: 'Password must be at least 8 characters', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: 'Please confirm password', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

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
