<template>
  <div class="shell">
    <header class="topbar">
      <router-link to="/dashboard" class="logo-link">
        <pre class="logo">
        ██████╗ ███████╗
       ██╔════╝ ██╔════╝
               ██║      ███████╗
               ██║      ╚════██║
       ╚██████╗ ███████║
        ╚═════╝ ╚══════╝
        </pre>
      </router-link>
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
  border-bottom: 1px solid #262626;
  flex-shrink: 0;
}

.logo-link {
  text-decoration: none;
  cursor: pointer;
}

.logo {
  font-family: var(--font-mono);
  font-size: 18px;
  line-height: 1.25;
  margin: 0;
  margin-left: -16px;
  letter-spacing: 0;
  background: linear-gradient(180deg, #d0d0d0 0%, #8a8a8a 40%, #5a5a5a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  display: inline-block;
  transform: scale(0.22);
  transform-origin: left center;
  flex-shrink: 0;
}

.nav {
  display: flex;
  align-items: center;
  gap: 0;
  flex-shrink: 0;
}

.nav-link {
  font-size: 13px;
  font-weight: 400;
  padding: 4px 8px;
  color: #5a5a5a;
  text-decoration: none;
  transition: color 0.15s;
}
.nav-link:hover {
  color: #e0e0e0;
}
.nav-link.active {
  color: #e0e0e0;
}
.nav-link.dim {
  font-family: var(--font-mono);
  color: #3a3a3a;
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
