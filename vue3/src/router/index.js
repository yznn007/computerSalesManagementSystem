import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '主页' } },
      { path: 'products', name: 'ProductList', component: () => import('../views/ProductList.vue'), meta: { title: '商品', staff: true } },
      { path: 'orders', name: 'OrderList', component: () => import('../views/OrderList.vue'), meta: { title: '订单' } },
      { path: 'customers', name: 'CustomerList', component: () => import('../views/CustomerList.vue'), meta: { title: '客户', staff: true } },
      { path: 'stats', name: 'SalesReport', component: () => import('../views/SalesReport.vue'), meta: { title: '统计', staff: true } },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (!token && to.path !== '/login') return '/login'
  if (token && to.path === '/login') return '/dashboard'
  if (to.meta.staff && localStorage.getItem('role') !== 'staff') return '/dashboard'
})

export default router
