<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="今日订单" :value="stats.todayOrders">
            <template #suffix>笔</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="今日销售额" :value="stats.todaySales">
            <template #prefix>￥</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="客户总数" :value="stats.totalCustomers" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="商品种类" :value="stats.totalProducts" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card header="快捷操作">
          <el-space wrap>
            <el-button type="primary" :icon="Sell" @click="$router.push('/order-create')">新建订单</el-button>
            <el-button type="success" :icon="Goods" @click="$router.push('/products')">查看商品</el-button>
            <el-button :icon="User" @click="$router.push('/customers')">客户管理</el-button>
            <el-button :icon="Document" @click="$router.push('/orders')">查看订单</el-button>
          </el-space>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="系统信息">
            <el-descriptions :column="1" border size="small" class="dark-descriptions">
              <el-descriptions-item label="数据库">MySQL 8.0 / computer_sales_db</el-descriptions-item>
              <el-descriptions-item label="后端">Spring Boot 4.0.7 + MyBatis 4.0.1</el-descriptions-item>
              <el-descriptions-item label="前端">Vue 3.5 + Element Plus</el-descriptions-item>
              <el-descriptions-item label="并发控制">悲观锁 FOR UPDATE</el-descriptions-item>
            </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Sell, Goods, User, Document } from '@element-plus/icons-vue'
import { getCustomers, getProducts, getOrders } from '../api'

const stats = ref({ todayOrders: 0, todaySales: 0, totalCustomers: 0, totalProducts: 0 })

onMounted(async () => {
  try {
    const [custRes, prodRes, orderRes] = await Promise.all([
      getCustomers(),
      getProducts(),
      getOrders({ page: 1, pageSize: 1 })
    ])
    stats.value.totalCustomers = custRes.data?.length || 0
    stats.value.totalProducts = prodRes.data?.length || 0
    stats.value.todayOrders = orderRes.data?.total || 0
  } catch { /* 后端未启动时静默 */ }
})
</script>
