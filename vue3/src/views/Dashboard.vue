<template>
  <div class="dashboard">
    <div class="stats-row">
      <div class="stat-block">
        <div class="stat-label">今日订单</div>
        <div class="stat-value">{{ stats.todayOrders }}</div>
        <div class="stat-unit">笔</div>
      </div>
      <div class="stat-block">
        <div class="stat-label">今日销售额</div>
        <div class="stat-value">¥{{ stats.todaySales }}</div>
      </div>
      <div class="stat-block">
        <div class="stat-label">客户总数</div>
        <div class="stat-value">{{ stats.totalCustomers }}</div>
      </div>
      <div class="stat-block">
        <div class="stat-label">商品种类</div>
        <div class="stat-value">{{ stats.totalProducts }}</div>
      </div>
    </div>

    <div class="quick-actions">
      <router-link to="/order-create" class="action-btn primary">新建订单</router-link>
      <router-link to="/products" class="action-btn">浏览商品</router-link>
      <router-link to="/customers" class="action-btn">客户信息</router-link>
      <router-link to="/orders" class="action-btn">订单记录</router-link>
    </div>

    <div class="meta">
      <span class="meta-item">MySQL 8.0 / computer_sales_db</span>
      <span class="meta-sep">·</span>
      <span class="meta-item">Spring Boot 4.0.7 + MyBatis 4.0.1</span>
      <span class="meta-sep">·</span>
      <span class="meta-item">Vue 3.5 + Element Plus</span>
      <span class="meta-sep">·</span>
      <span class="meta-item">FOR UPDATE 悲观锁</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCustomers, getProducts, getOrders } from '../api'

const stats = ref({ todayOrders: 0, todaySales: 0, totalCustomers: 0, totalProducts: 0 })

onMounted(async () => {
  try {
    const [custRes, prodRes] = await Promise.all([getCustomers(), getProducts()])
    stats.value.totalCustomers = custRes.data?.length || 0
    stats.value.totalProducts = prodRes.data?.length || 0
  } catch {}
})
</script>

<style scoped>
.dashboard {
  padding-top: 48px;
}

.stats-row {
  display: flex;
  gap: 48px;
  margin-bottom: 48px;
}

.stat-block {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  font-family: var(--font-mono);
  font-size: 32px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -1px;
}

.stat-unit {
  font-size: 12px;
  color: var(--text-tertiary);
}

.quick-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 48px;
}

.action-btn {
  font-size: 13px;
  padding: 7px 16px;
  border-radius: 4px;
  color: var(--text-secondary);
  text-decoration: none;
  border: 1px solid var(--border-dim);
  transition: all 0.15s;
}
.action-btn:hover {
  color: var(--text-primary);
  background: var(--bg-hover);
}
.action-btn.primary {
  color: var(--accent);
  border-color: var(--accent);
}
.action-btn.primary:hover {
  background: var(--accent-dim);
}

.meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.meta-item {
  font-size: 12px;
  color: var(--text-tertiary);
  font-family: var(--font-mono);
}
.meta-sep {
  font-size: 12px;
  color: #333;
}
</style>
