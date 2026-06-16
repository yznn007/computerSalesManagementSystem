import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.response.use(
  res => res,
  err => {
    ElMessage.error(err.response?.data?.message || '请求失败')
    return Promise.reject(err)
  }
)

// 客户
export const getCustomers = () => api.get('/customers')
export const addCustomer = (data) => api.post('/customers', data)

// 商品
export const getProducts = (category) => api.get('/products', { params: { category } })
export const getProductDetail = (id) => api.get(`/products/${id}`)

// 订单
export const createOrder = (data) => api.post('/orders', data)
export const getOrders = (params) => api.get('/orders', { params })
export const getOrderDetail = (id) => api.get(`/orders/${id}`)
export const updateOrderStatus = (id, status) => api.put(`/orders/${id}/status`, { status })

export default api
