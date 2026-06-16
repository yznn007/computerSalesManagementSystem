<template>
  <div style="display: flex; gap: 20px; height: calc(100vh - 140px)">
    <!-- 左侧：商品列表 -->
    <div style="flex: 6; overflow-y: auto">
      <div class="panel-dark">
        <div style="display: flex; gap: 12px; margin-bottom: 12px">
          <el-input v-model="keyword" placeholder="搜索商品名称..." :prefix-icon="Search" clearable style="flex: 1" />
          <el-select v-model="selectCategory" placeholder="分类筛选" clearable style="width: 140px" @change="fetchProducts">
            <el-option label="全部" value="" />
            <el-option label="笔记本" value="笔记本" />
            <el-option label="台式机整机" value="台式机整机" />
            <el-option label="DIY配件" value="DIY配件" />
          </el-select>
        </div>

        <el-row :gutter="12" v-loading="loading">
          <el-col v-for="p in filteredProducts" :key="p.product_id" :span="8" style="margin-bottom: 12px">
            <el-card shadow="hover" :body-style="{ padding: '12px' }" style="cursor: pointer" @click="addToCart(p)">
              <div>
                <el-tag size="small" :type="tagMap[p.category]" style="margin-bottom: 4px">{{ p.category }}</el-tag>
                <div style="font-weight: 600; font-size: 14px; margin-bottom: 4px; line-height: 1.4">{{ p.brand }} {{ p.model }}</div>
                <div style="display: flex; justify-content: space-between; align-items: baseline">
                  <span style="color: #f56c6c; font-size: 16px; font-weight: bold">￥{{ p.price }}</span>
                  <span style="color: #999; font-size: 12px">库存 {{ p.stock }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 右侧：购物清单 + 客户选择 -->
    <div style="flex: 4; display: flex; flex-direction: column; gap: 16px; min-width: 360px">
      <!-- 客户选择 -->
      <div class="panel-dark">
        <div style="font-weight: 600; margin-bottom: 8px; color: #e8e8e8">选择客户</div>
        <el-select v-model="selectedCustomer" placeholder="请选择客户" filterable style="width: 100%" value-key="customer_id">
          <el-option v-for="c in customers" :key="c.customer_id" :label="`${c.customer_name} - ${c.phone}`" :value="c" />
        </el-select>
        <div v-if="selectedCustomer" style="margin-top: 6px; color: #999; font-size: 12px">
          收货地址：{{ selectedCustomer.address }}
        </div>
      </div>

      <!-- 购物清单 -->
      <div style="flex: 1">
        <div class="cart-panel">
          <h3 style="margin: 0 0 12px">购物清单</h3>

          <el-empty v-if="cartItems.length === 0" description="点击左侧商品添加" :image-size="80" />

          <div v-else style="display: flex; flex-direction: column; height: calc(100% - 40px)">
            <div style="flex: 1; overflow-y: auto">
              <div v-for="(item, index) in cartItems" :key="item.product_id" class="cart-item">
                <div class="cart-item-info">
                  <div class="cart-item-name">{{ item.brand }} {{ item.model }}</div>
                  <div class="cart-item-price">￥{{ item.price.toFixed(2) }}</div>
                </div>
                <div class="cart-item-actions">
                  <el-input-number v-model="item.quantity" :min="1" :max="item.stock" size="small" style="width: 110px" controls-position="right" />
                  <el-button type="danger" :icon="Delete" size="small" circle @click="removeFromCart(index)" />
                </div>
              </div>
            </div>

            <div style="border-top: 2px solid #f0f0f0; padding-top: 12px; margin-top: auto">
              <div style="font-size: 14px; color: #666">共 <b>{{ totalCount }}</b> 件商品</div>
              <div style="font-size: 22px; color: #f56c6c; font-weight: bold; margin: 8px 0 16px">￥{{ totalPrice.toFixed(2) }}</div>
              <el-button type="primary" size="large" style="width: 100%" :icon="ShoppingCartFull" :loading="submitting" :disabled="cartItems.length === 0 || !selectedCustomer" @click="submitOrder">
                提交订单
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search, Delete, ShoppingCartFull } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getCustomers, getProducts, createOrder } from '../api'

const tagMap = { '笔记本': 'primary', '台式机整机': 'success', 'DIY配件': 'warning' }

const keyword = ref('')
const selectCategory = ref('')
const products = ref([])
const customers = ref([])
const loading = ref(false)
const selectedCustomer = ref(null)
const cartItems = ref([])
const submitting = ref(false)

const filteredProducts = computed(() => {
  if (!keyword.value) return products.value
  const kw = keyword.value.toLowerCase()
  return products.value.filter(p =>
    p.brand.toLowerCase().includes(kw) ||
    p.model.toLowerCase().includes(kw)
  )
})

const totalCount = computed(() => cartItems.value.reduce((s, i) => s + i.quantity, 0))
const totalPrice = computed(() => cartItems.value.reduce((s, i) => s + i.price * i.quantity, 0))

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await getProducts(selectCategory.value || undefined)
    products.value = res.data || []
  } finally { loading.value = false }
}

const fetchCustomers = async () => {
  try {
    const res = await getCustomers()
    customers.value = res.data || []
  } finally {}
}

const addToCart = (product) => {
  const existing = cartItems.value.find(i => i.product_id === product.product_id)
  if (existing) {
    if (existing.quantity < product.stock) existing.quantity++
    else ElMessage.warning('已达到最大库存')
  } else {
    cartItems.value.push({ ...product, quantity: 1 })
  }
  ElMessage.success(`已添加 ${product.model}`)
}

const removeFromCart = (index) => cartItems.value.splice(index, 1)

const submitOrder = async () => {
  if (!selectedCustomer.value) {
    ElMessage.warning('请先选择客户')
    return
  }
  if (cartItems.value.length === 0) {
    ElMessage.warning('购物清单为空')
    return
  }

  submitting.value = true
  try {
    const orderData = {
      customer_id: selectedCustomer.value.customer_id,
      items: cartItems.value.map(i => ({
        product_id: i.product_id,
        quantity: i.quantity
      }))
    }
    const res = await createOrder(orderData)
    if (res.data?.status === 0) {
      ElMessage.success(`下单成功！订单号：${res.data.order_no}`)
      cartItems.value = []
      fetchProducts()
    } else if (res.data?.status === 1) {
      ElMessage.error('下单失败：库存不足')
    } else if (res.data?.status === 2) {
      ElMessage.error('下单失败：商品不存在')
    } else {
      ElMessage.error('下单失败：系统异常')
    }
  } catch {
    // 拦截器已处理
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchProducts()
  fetchCustomers()
})
</script>

<style scoped>
.panel-dark {
  background: #1e1e1e;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #2a2a2a;
}

.cart-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #1e1e1e;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #2a2a2a;
}
.cart-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #2a2a2a;
}
.cart-item-name {
  font-size: 13px;
  font-weight: 500;
  color: #e8e8e8;
}
.cart-item-price {
  font-size: 14px;
  color: #E6A23C;
  font-weight: bold;
  margin-top: 4px;
}
.cart-item-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
