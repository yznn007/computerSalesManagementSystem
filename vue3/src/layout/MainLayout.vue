<template>
  <div class="app-shell">
    <!-- 顶栏 -->
    <header class="top-bar">
      <div class="top-bar-left">
        <el-icon :size="22" color="#C9A96E"><Monitor /></el-icon>
        <span class="top-bar-title">电脑销售管理系统</span>
      </div>
      <nav class="top-bar-nav">
        <router-link
          v-for="tab in tabs"
          :key="tab.path"
          :to="tab.path"
          class="nav-tab"
          :class="{ active: isActive(tab.path) }"
        >
          <el-icon><component :is="tab.icon" /></el-icon>
          <span>{{ tab.label }}</span>
        </router-link>
      </nav>
      <div class="top-bar-right">
        <span class="top-bar-time">{{ currentTime }}</span>
      </div>
    </header>

    <!-- 主内容 -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { Monitor } from '@element-plus/icons-vue'

const route = useRoute()
const currentTime = ref('')

const tabs = [
  { path: '/dashboard', label: '仪表盘', icon: 'Odometer' },
  { path: '/customers', label: '客户管理', icon: 'User' },
  { path: '/products', label: '商品管理', icon: 'Goods' },
  { path: '/order-create', label: '下单', icon: 'Sell' },
  { path: '/orders', label: '订单管理', icon: 'Document' },
  { path: '/reports', label: '销售统计', icon: 'DataAnalysis' }
]

const isActive = (path) => route.path === path

let timer = null
onMounted(() => {
  timer = setInterval(() => {
    currentTime.value = new Date().toLocaleString('zh-CN')
  }, 1000)
})
onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.app-shell {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #141414;
}

.top-bar {
  height: 52px;
  display: flex;
  align-items: center;
  background: #1a1a1a;
  border-bottom: 1px solid #2a2a2a;
  padding: 0 20px;
  gap: 24px;
  flex-shrink: 0;
}

.top-bar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  white-space: nowrap;
}

.top-bar-title {
  font-size: 16px;
  font-weight: 700;
  color: #e8e8e8;
  letter-spacing: 0.5px;
}

.top-bar-nav {
  display: flex;
  gap: 4px;
  flex: 1;
  justify-content: center;
}

.nav-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border-radius: 6px;
  font-size: 13px;
  color: #a0a0a0;
  text-decoration: none;
  transition: all 0.2s;
  position: relative;
}

.nav-tab:hover {
  color: #e8e8e8;
  background: rgba(201, 169, 110, 0.08);
}

.nav-tab.active {
  color: #C9A96E;
  background: rgba(201, 169, 110, 0.12);
}

.nav-tab.active::after {
  content: '';
  position: absolute;
  bottom: -14px;
  left: 50%;
  transform: translateX(-50%);
  width: 60%;
  height: 2px;
  background: #C9A96E;
  border-radius: 1px;
}

.nav-tab .el-icon {
  font-size: 16px;
}

.top-bar-right {
  white-space: nowrap;
}

.top-bar-time {
  font-size: 12px;
  color: #707070;
  font-variant-numeric: tabular-nums;
}

.main-content {
  flex: 1;
  overflow: auto;
  padding: 20px;
}
</style>
