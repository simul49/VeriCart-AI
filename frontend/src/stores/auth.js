import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, userApi } from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('username') || '')
  const email = ref(localStorage.getItem('email') || '')
  const role = ref(localStorage.getItem('role') || 'CUSTOMER')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'ADMIN')

  async function login(credentials) {
    const res = await authApi.login(credentials)
    saveAuth(res.data)
    return res
  }

  async function register(data) {
    const res = await authApi.register(data)
    saveAuth(res.data)
    return res
  }

  function saveAuth(data) {
    token.value = data.token
    userId.value = data.userId
    username.value = data.username
    email.value = data.email
    role.value = data.role

    localStorage.setItem('token', data.token)
    localStorage.setItem('userId', data.userId)
    localStorage.setItem('username', data.username)
    localStorage.setItem('email', data.email)
    localStorage.setItem('role', data.role)
  }

  function logout() {
    token.value = ''
    userId.value = ''
    username.value = ''
    email.value = ''
    role.value = 'CUSTOMER'

    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('email')
    localStorage.removeItem('role')
  }

  return { token, userId, username, email, role, isLoggedIn, isAdmin, login, register, logout }
})
