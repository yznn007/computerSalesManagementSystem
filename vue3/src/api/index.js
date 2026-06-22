import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  res => res,
  err => {
    const msg = err.response?.data?.message || '请求失败'
    ElMessage.error(msg)
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('name')
      if (location.pathname !== '/login') location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// 鉴权
export const register = (data) => api.post('/auth/register', data)
export const loginCustomer = (data) => api.post('/auth/login/customer', data)
export const loginStaff = (data) => api.post('/auth/login/staff', data)
export const me = () => api.get('/auth/me')
export const updateProfile = (data) => api.put('/auth/me', data)
export const changePassword = (data) => api.put('/auth/me/password', data)

// 客户
export const getCustomers = () => api.get('/customers')
export const addCustomer = (data) => api.post('/customers', data)
export const updateCustomer = (id, data) => api.put(`/customers/${id}`, data)
export const deleteCustomer = (id) => api.delete(`/customers/${id}`)

// 商品
export const getProducts = (category) => api.get('/products', { params: { category } })
export const getProductDetail = (id) => api.get(`/products/${id}`)
export const createProduct = (data) => api.post('/products', data)
export const updateProduct = (id, data) => api.put(`/products/${id}`, data)
export const deleteProduct = (id) => api.delete(`/products/${id}`)

// 订单
export const createOrder = (data) => api.post('/orders', data)
export const getOrders = (params) => api.get('/orders', { params })
export const getOrderDetail = (id) => api.get(`/orders/${id}`)
export const updateOrderStatus = (id, payload) => api.put(`/orders/${id}/status`, payload)

// 统计
export const getStatsOverview = () => api.get('/stats/overview')

export default api
