<template>
  <div class="order-page">
    <div class="order-grid">
      <!-- 左侧：商品列表 -->
      <div class="product-list">
        <div class="product-header">
          <div class="search-box">
            <el-input
              v-model="keyword"
              placeholder="搜索型号..."
              :prefix-icon="Search"
              clearable
              class="search-input"
            />
          </div>
          <div class="category-tabs">
            <button
              v-for="cat in ['', '笔记本', '台式机整机', 'DIY配件']"
              :key="cat"
              class="cat-tab"
              :class="{ active: selectCategory === cat }"
              @click="selectCategory = cat; fetchProducts()"
            >
              {{ cat || '全部' }}
            </button>
          </div>
        </div>

        <div class="products" v-loading="loading">
          <div
            v-for="p in filteredProducts"
            :key="p.product_id"
            class="product-row"
            @click="addToCart(p)"
          >
            <div class="product-info">
              <span class="product-tag">{{ p.category }}</span>
              <span class="product-name">{{ p.brand }} {{ p.model }}</span>
            </div>
            <div class="product-right">
              <span class="product-stock">{{ p.stock }}</span>
              <span class="product-price">¥{{ p.price }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：购物清单 -->
      <div class="cart-column">
        <div class="cart-section">
          <div class="cart-label">选择客户</div>
          <el-select
            v-model="selectedCustomer"
            placeholder="请选择客户"
            filterable
            style="width: 100%"
            value-key="customer_id"
            class="dark-select"
          >
            <el-option
              v-for="c in customers"
              :key="c.customer_id"
              :label="`${c.customer_name} — ${c.phone}`"
              :value="c"
            />
          </el-select>
          <div v-if="selectedCustomer" class="customer-addr">
            {{ selectedCustomer.address }}
          </div>
        </div>

        <div class="cart-section cart-main">
          <div class="cart-label">购物清单</div>

          <div v-if="cartItems.length === 0" class="cart-empty">
            点击左侧商品添加
          </div>

          <template v-else>
            <div class="cart-items">
              <div v-for="(item, index) in cartItems" :key="item.product_id" class="cart-row">
                <div class="cart-item-main">
                  <span class="cart-item-name">{{ item.brand }} {{ item.model }}</span>
                  <span class="cart-item-price">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
                </div>
                <div class="cart-item-ctl">
                  <el-input-number
                    v-model="item.quantity"
                    :min="1"
                    :max="item.stock"
                    size="small"
                    controls-position="right"
                    class="qty-input"
                  />
                  <button class="remove-btn" @click="removeFromCart(index)" title="移除">×</button>
                </div>
              </div>
            </div>

            <div class="cart-total">
              <div class="total-row">
                <span class="total-label">共 {{ totalCount }} 件</span>
                <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
              </div>
              <button
                class="submit-btn"
                :disabled="!selectedCustomer || cartItems.length === 0 || submitting"
                @click="submitOrder"
              >
                {{ submitting ? '提交中...' : '提交订单' }}
              </button>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
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
    p.brand.toLowerCase().includes(kw) || p.model.toLowerCase().includes(kw)
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
  } catch {}
}

const addToCart = (product) => {
  const existing = cartItems.value.find(i => i.product_id === product.product_id)
  if (existing) {
    if (existing.quantity < product.stock) existing.quantity++
    else ElMessage.warning('已达到最大库存')
  } else {
    cartItems.value.push({ ...product, quantity: 1 })
  }
}

const removeFromCart = (index) => cartItems.value.splice(index, 1)

const submitOrder = async () => {
  if (!selectedCustomer.value) return ElMessage.warning('请先选择客户')
  submitting.value = true
  try {
    const orderData = {
      customer_id: selectedCustomer.value.customer_id,
      items: cartItems.value.map(i => ({ product_id: i.product_id, quantity: i.quantity }))
    }
    const res = await createOrder(orderData)
    if (res.data?.status === 0) {
      ElMessage.success(`下单成功 ${res.data.order_no}`)
      cartItems.value = []
      fetchProducts()
    } else if (res.data?.status === 1) {
      ElMessage.error('库存不足')
    } else {
      ElMessage.error('下单失败')
    }
  } catch {} finally { submitting.value = false }
}

onMounted(() => {
  fetchProducts()
  fetchCustomers()
})
</script>

<style scoped>
.order-page {
  padding-top: 24px;
}

.order-grid {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 32px;
  align-items: start;
}

/* 商品列表 */
.product-header {
  margin-bottom: 16px;
}

.search-input :deep(.el-input__wrapper) {
  background: var(--bg) !important;
  border-color: var(--border-dim) !important;
  box-shadow: none !important;
}
.search-input :deep(.el-input__inner) {
  color: var(--text-primary) !important;
  font-size: 13px;
}

.category-tabs {
  display: flex;
  gap: 4px;
  margin-top: 12px;
}
.cat-tab {
  font-size: 12px;
  padding: 4px 10px;
  border: none;
  background: none;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: 3px;
  font-family: var(--font-sans);
  transition: all 0.15s;
}
.cat-tab:hover {
  color: var(--text-primary);
  background: var(--bg-hover);
}
.cat-tab.active {
  color: var(--accent);
}

.products {
  border: 1px solid var(--border-dim);
}

.product-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.1s;
  border-bottom: 1px solid var(--border-dim);
}
.product-row:last-child {
  border-bottom: none;
}
.product-row:hover {
  background: var(--bg-hover);
}

.product-info {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
.product-tag {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 3px;
  background: var(--border-dim);
  color: var(--text-secondary);
  white-space: nowrap;
}
.product-name {
  font-size: 13px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-right {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-shrink: 0;
}
.product-stock {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-tertiary);
}
.product-price {
  font-family: var(--font-mono);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

/* 购物清单 */
.cart-section {
  margin-bottom: 20px;
}
.cart-label {
  font-size: 12px;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
}
.customer-addr {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 6px;
}

.cart-main {
  border: 1px solid var(--border-dim);
  padding: 16px;
}

.cart-empty {
  font-size: 13px;
  color: var(--text-tertiary);
  padding: 24px 0;
  text-align: center;
}

.cart-items {
  margin-bottom: 16px;
}
.cart-row {
  padding: 8px 0;
  border-bottom: 1px solid var(--border-dim);
}
.cart-row:last-child {
  border-bottom: none;
}
.cart-item-main {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 6px;
}
.cart-item-name {
  font-size: 13px;
  color: var(--text-primary);
}
.cart-item-price {
  font-family: var(--font-mono);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}
.cart-item-ctl {
  display: flex;
  align-items: center;
  gap: 8px;
}
.qty-input {
  width: 100px;
}
.remove-btn {
  background: none;
  border: none;
  font-size: 18px;
  color: var(--text-tertiary);
  cursor: pointer;
  padding: 0 4px;
  line-height: 1;
}
.remove-btn:hover {
  color: #f56c6c;
}

.cart-total {
  border-top: 1px solid var(--border-dim);
  padding-top: 16px;
}
.total-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 16px;
}
.total-label {
  font-size: 13px;
  color: var(--text-secondary);
}
.total-price {
  font-family: var(--font-mono);
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.5px;
}

.submit-btn {
  width: 100%;
  padding: 10px 0;
  background: var(--accent);
  border: none;
  color: #0a0a0a;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border-radius: 4px;
  transition: opacity 0.15s;
  font-family: var(--font-sans);
}
.submit-btn:hover:not(:disabled) {
  opacity: 0.9;
}
.submit-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}
</style>
