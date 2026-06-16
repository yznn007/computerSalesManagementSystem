<template>
  <div class="dashboard">
    <div class="hero">
      <pre class="ascii">  ██████╗ ███████╗
 ██╔════╝ ██╔════╝
 ██║      ███████╗
 ██║      ╚════██║
 ╚██████╗ ███████║
  ╚═════╝ ╚══════╝</pre>
      <div class="hero-sub">电脑销售系统</div>
      <div class="hero-desc">MySQL InnoDB · 悲观锁 · Spring Boot 4.0 · Vue 3</div>
      <div class="hero-cmd">$ npm run dev</div>
    </div>

    <div class="stats">
      <div class="stat">
        <div class="stat-num mono">{{ stats.todayOrders }}</div>
        <div class="stat-label">今日订单</div>
      </div>
      <div class="stat">
        <div class="stat-num mono">¥{{ stats.todaySales }}</div>
        <div class="stat-label">销售额</div>
      </div>
      <div class="stat">
        <div class="stat-num mono">{{ stats.totalCustomers }}</div>
        <div class="stat-label">客户</div>
      </div>
      <div class="stat">
        <div class="stat-num mono">{{ stats.totalProducts }}</div>
        <div class="stat-label">商品</div>
      </div>
    </div>

    <div class="links">
      <router-link to="/order-create" class="link accent">新 建 订 单</router-link>
      <router-link to="/products" class="link">商品列表</router-link>
      <router-link to="/customers" class="link">客户信息</router-link>
      <router-link to="/orders" class="link">订单记录</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCustomers, getProducts } from '../api'

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
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 80px;
}

.hero {
  text-align: center;
  margin-bottom: 64px;
}

.ascii {
  font-family: var(--font-mono);
  font-size: 18px;
  line-height: 1.25;
  color: var(--accent);
  margin: 0 0 24px;
  letter-spacing: 0;
  background: linear-gradient(180deg, #C9A96E 0%, #8a7040 40%, #4a3a20 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-sub {
  font-size: 20px;
  font-weight: 500;
  color: #e0e0e0;
  letter-spacing: 2px;
  margin-bottom: 8px;
}

.hero-desc {
  font-family: var(--font-mono);
  font-size: 12px;
  color: #5a5a5a;
  margin-bottom: 16px;
}

.hero-cmd {
  font-family: var(--font-mono);
  font-size: 13px;
  color: #3a3a3a;
}

.hero-cmd::before {
  content: '';
}

.stats {
  display: flex;
  gap: 48px;
  margin-bottom: 48px;
}

.stat {
  text-align: center;
}

.stat-num {
  font-size: 36px;
  font-weight: 600;
  color: #e0e0e0;
  letter-spacing: -1px;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #5a5a5a;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.links {
  display: flex;
  gap: 8px;
}

.link {
  font-size: 13px;
  padding: 8px 20px;
  color: #5a5a5a;
  text-decoration: none;
  border: 1px solid #262626;
  transition: all 0.15s;
}
.link:hover {
  color: #e0e0e0;
  border-color: #444;
}
.link.accent {
  color: var(--accent);
  border-color: var(--accent);
}
.link.accent:hover {
  background: rgba(201, 169, 110, 0.1);
}

.mono { font-family: var(--font-mono); }
</style>
