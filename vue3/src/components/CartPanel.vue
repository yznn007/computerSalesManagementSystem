<template>
  <div class="cart-panel">
    <h3 style="margin: 0 0 12px">购物清单</h3>

    <el-empty v-if="items.length === 0" description="暂无商品，请从左侧添加" :image-size="80" />

    <div v-else>
      <div v-for="(item, index) in items" :key="item.product_id" class="cart-item">
        <div class="cart-item-info">
          <div class="cart-item-name">{{ item.brand }} {{ item.model }}</div>
          <div class="cart-item-price">￥{{ item.price.toFixed(2) }}</div>
        </div>
        <div class="cart-item-actions">
          <el-input-number v-model="item.quantity" :min="1" :max="item.stock" size="small" style="width: 110px" controls-position="right" />
          <el-button type="danger" :icon="Delete" size="small" circle @click="removeItem(index)" />
        </div>
      </div>

      <el-divider />

      <div style="font-size: 14px; color: #666">
        共 <b>{{ totalCount }}</b> 件商品
      </div>
      <div style="font-size: 22px; color: #f56c6c; font-weight: bold; margin: 8px 0 16px">
        ￥{{ totalPrice.toFixed(2) }}
      </div>
      <el-button type="primary" size="large" style="width: 100%" :icon="ShoppingCartFull" :loading="submitting" :disabled="items.length === 0" @click="$emit('submit')">
        提交订单
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Delete, ShoppingCartFull } from '@element-plus/icons-vue'

const props = defineProps({
  items: { type: Array, default: () => [] },
  submitting: { type: Boolean, default: false }
})

const emit = defineEmits(['remove', 'submit'])

const totalCount = computed(() => props.items.reduce((s, i) => s + i.quantity, 0))
const totalPrice = computed(() => props.items.reduce((s, i) => s + i.price * i.quantity, 0))

const removeItem = (index) => emit('remove', index)
</script>

<style scoped>
.cart-panel {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.cart-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.cart-item-name {
  font-size: 13px;
  font-weight: 500;
}
.cart-item-price {
  font-size: 14px;
  color: #f56c6c;
  font-weight: bold;
  margin-top: 4px;
}
.cart-item-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
