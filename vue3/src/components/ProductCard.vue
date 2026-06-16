<template>
  <el-card shadow="hover" :body-style="{ padding: '12px' }" style="cursor: pointer">
    <div style="display: flex; justify-content: space-between; align-items: flex-start">
      <div>
        <el-tag size="small" :type="tagType" style="margin-bottom: 6px">{{ product.category }}</el-tag>
        <div style="font-weight: 600; margin-bottom: 4px">{{ product.brand }} {{ product.model }}</div>
        <div style="color: #f56c6c; font-size: 18px; font-weight: bold">￥{{ product.price }}</div>
        <div style="color: #999; font-size: 12px; margin-top: 4px">
          库存：<span :style="{ color: product.stock < 5 ? '#f56c6c' : '#67c23a' }">{{ product.stock }}</span>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ product: Object })

const tagType = computed(() => {
  const map = { '笔记本': 'primary', '台式机整机': 'success', 'DIY配件': 'warning' }
  return map[props.product.category] || 'info'
})
</script>
