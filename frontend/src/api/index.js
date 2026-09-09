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
  categories: () => api.get('/categories'),
  // Browse "All Products" grouped by main → sub category, with product cards per sub-category
  grouped: (params) => api.get('/products/grouped', { params }),
  // Paginated listing → {items,total,page,size,totalPages}
  page: (params) => api.get('/products/page', { params }),
  // FR-057 comparison
  compare: (ids) => api.get('/products/compare', { params: { ids } })
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
  delete: (id) => api.delete(`/reviews/${id}`),
  // FR-034 edit · FR-037 report · explainable trust metrics
  update: (id, data) => api.put(`/reviews/${id}`, data),
  report: (id) => api.post(`/reviews/${id}/report`),
  myReviews: () => api.get('/reviews/my'),
  trustMetrics: (productId) => api.get(`/reviews/product/${productId}/trust`),
  hasReviewed: (productId) => api.get(`/reviews/product/${productId}/mine`)
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

// ---- Notifications (FR-065–068) ----
export const notificationApi = {
  list: (limit = 30) => api.get('/notifications', { params: { limit } }),
  unreadCount: () => api.get('/notifications/unread-count'),
  markRead: (id) => api.put(`/notifications/${id}/read`),
  markAllRead: () => api.put('/notifications/read-all'),
  delete: (id) => api.delete(`/notifications/${id}`)
}

// ---- Seller Dashboard ----
export const sellerApi = {
  products: () => api.get('/seller/products'),
  orders: (status) => api.get('/seller/orders', { params: status ? { status } : {} }),
  orderItems: (id) => api.get(`/seller/orders/${id}/items`),
  updateOrderStatus: (id, status) => api.put(`/seller/orders/${id}/status`, { status }),
  reviews: () => api.get('/seller/reviews'),
  stats: () => api.get('/seller/stats')
}

// ---- Product inquiries (message the store owner) ----
export const messageApi = {
  send: (data) => api.post('/messages', data),
  sendFollowUp: (id, message) => api.post(`/messages/${id}/send`, { message }),
  myMessages: () => api.get('/messages/my'),
  sellerMessages: () => api.get('/seller/messages'),
  sellerUnreadCount: () => api.get('/seller/messages/unread-count'),
  reply: (id, reply) => api.put(`/seller/messages/${id}/reply`, { reply }),
  markRead: (id) => api.put(`/seller/messages/${id}/read`)
}
