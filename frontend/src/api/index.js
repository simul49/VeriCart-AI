import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// Request interceptor — attach JWT
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Response interceptor — handle errors
api.interceptors.response.use(
  res => res.data,
  err => {
    const status = err.response?.status
    const msg = err.response?.data?.message || err.message || 'Network error'

    if (status === 401) {
      // Not logged in or token expired — redirect to login
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('email')
      localStorage.removeItem('role')
      window.location.href = '/login'
      return Promise.reject(err)
    }

    if (status === 403) {
      // Logged in but wrong role — show permission error
      ElMessage.error(msg || 'You do not have permission')
      return Promise.reject(err)
    }

    ElMessage.error(msg)
    return Promise.reject(err)
  }
)

// ---- Auth ----
export const authApi = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data)
}

// ---- User ----
export const userApi = {
  profile: () => api.get('/user/profile'),
  updateProfile: (data) => api.put('/user/profile', data),
  changePassword: (data) => api.put('/user/password', data)
}

// ---- Products ----
export const productApi = {
  list: (params) => api.get('/products', { params }),
  detail: (id) => api.get(`/products/${id}`),
  create: (data) => api.post('/seller/products', data),
  update: (id, data) => api.put(`/seller/products/${id}`, data),
  delete: (id) => api.delete(`/seller/products/${id}`),
  categories: () => api.get('/categories')
}

// ---- Cart ----
export const cartApi = {
  get: () => api.get('/cart'),
  add: (data) => api.post('/cart/add', data),
  update: (id, data) => api.put(`/cart/${id}`, data),
  remove: (id) => api.delete(`/cart/${id}`),
  clear: () => api.delete('/cart/clear'),
  count: () => api.get('/cart/count')
}

// ---- Orders ----
export const orderApi = {
  create: (data) => api.post('/orders', data),
  list: () => api.get('/orders'),
  detail: (id) => api.get(`/orders/${id}`),
  items: (id) => api.get(`/orders/${id}/items`),
  cancel: (id) => api.put(`/orders/${id}/cancel`)
}

// ---- Reviews ----
export const reviewApi = {
  create: (data) => api.post('/reviews', data),
  byProduct: (productId) => api.get(`/reviews/product/${productId}`),
  delete: (id) => api.delete(`/reviews/${id}`)
}

// ---- Wishlist ----
export const wishlistApi = {
  list: () => api.get('/wishlist'),
  add: (productId) => api.post('/wishlist/add', { productId }),
  remove: (productId) => api.delete(`/wishlist/${productId}`),
  check: (productId) => api.get(`/wishlist/check/${productId}`)
}

// ---- AI ----
export const aiApi = {
  analyze: (productId) => api.post(`/ai/analyze/${productId}`),
  chat: (data) => api.post('/ai/chat', data),
  recommend: (data) => api.post('/ai/recommend', data),
  batch: () => api.post('/ai/batch')
}

// ---- Admin ----
export const adminApi = {
  dashboard: () => api.get('/admin/dashboard'),
  orders: (status) => api.get('/admin/orders', { params: { status } }),
  updateOrderStatus: (id, status) => api.put(`/admin/orders/${id}/status`, { status }),
  flaggedReviews: () => api.get('/admin/reviews/flagged'),
  products: () => api.get('/admin/products'),
  users: () => api.get('/admin/users')
}
