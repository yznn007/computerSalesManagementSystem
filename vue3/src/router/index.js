import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '主页', icon: 'Odometer' } },
      { path: 'customers', name: 'CustomerList', component: () => import('../views/CustomerList.vue'), meta: { title: '客户管理', icon: 'User' } },
      { path: 'products', name: 'ProductList', component: () => import('../views/ProductList.vue'), meta: { title: '商品管理', icon: 'Goods' } },
      { path: 'order-create', name: 'OrderCreate', component: () => import('../views/OrderCreate.vue'), meta: { title: '下单', icon: 'Sell' } },
      { path: 'orders', name: 'OrderList', component: () => import('../views/OrderList.vue'), meta: { title: '订单管理', icon: 'Document' } },
      { path: 'reports', name: 'SalesReport', component: () => import('../views/SalesReport.vue'), meta: { title: '销售统计', icon: 'DataAnalysis' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
