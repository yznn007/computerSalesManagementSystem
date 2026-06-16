<template>
  <div class="shell">
    <header class="topbar">
      <a href="/" class="logo">
        <span class="logo-mono">>_</span>
        <span class="logo-text">电脑销售系统</span>
      </a>
      <nav class="nav">
        <router-link to="/order-create" class="nav-link" :class="{ active: $route.path === '/order-create' }">
          下单
        </router-link>
        <router-link to="/products" class="nav-link" :class="{ active: $route.path === '/products' }">
          商品
        </router-link>
        <router-link to="/orders" class="nav-link" :class="{ active: $route.path === '/orders' }">
          订单
        </router-link>
        <router-link to="/customers" class="nav-link" :class="{ active: $route.path === '/customers' }">
          客户
        </router-link>
        <router-link to="/dashboard" class="nav-link" :class="{ active: $route.path === '/dashboard' }">
          仪表盘
        </router-link>
        <a href="#" class="nav-link dim">
          {{ time }}
        </a>
      </nav>
    </header>

    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const time = ref('')

let timer = null
onMounted(() => {
  const update = () => {
    time.value = new Date().toLocaleString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
  }
  update()
  timer = setInterval(update, 1000)
})
onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 24px;
  border-bottom: 1px solid var(--border-dim);
  flex-shrink: 0;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-primary);
}

.logo-mono {
  font-family: var(--font-mono);
  font-size: 15px;
  font-weight: 600;
  color: var(--accent);
  letter-spacing: 1px;
}

.logo-text {
  font-size: 14px;
  font-weight: 500;
  letter-spacing: -0.3px;
}

.nav {
  display: flex;
  align-items: center;
  gap: 2px;
}

.nav-link {
  font-size: 13px;
  padding: 4px 12px;
  border-radius: 4px;
  color: var(--text-secondary);
  text-decoration: none;
  transition: all 0.15s;
}
.nav-link:hover {
  color: var(--text-primary);
  background: var(--bg-hover);
}
.nav-link.active {
  color: var(--accent);
}
.nav-link.dim {
  font-family: var(--font-mono);
  color: var(--text-tertiary);
  font-size: 12px;
  padding: 4px 8px;
  pointer-events: none;
}

.main {
  flex: 1;
  max-width: 1280px;
  width: 100%;
  margin: 0 auto;
  padding: 32px 24px 64px;
}
</style>
