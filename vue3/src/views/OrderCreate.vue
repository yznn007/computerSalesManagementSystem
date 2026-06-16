<template>
  <div class="order-page">
    <div class="order-grid">
      <div class="product-panel">
        <div class="panel-top">
          <div class="search-box">
            <input v-model="keyword" class="search-input" placeholder="搜索型号..." @keydown.enter />
            <svg class="search-icon" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
          </div>
          <div class="tabs">
            <button v-for="cat in ['', '笔记本', '台式机整机', 'DIY配件']" :key="cat" class="tab" :class="{ active: selectCategory === cat }" @click="selectCategory = cat; fetchProducts()">{{ cat || '全部' }}</button>
          </div>
        </div>

        <div v-loading="loading">
          <div v-for="p in filteredProducts" :key="p.product_id" class="row" @click="addToCart(p)">
            <span class="row-name">{{ p.brand }} {{ p.model }}</span>
            <span class="row-cat">{{ p.category }}</span>
            <span class="row-stock mono-dim">{{ p.stock }}</span>
            <span class="row-price mono">¥{{ p.price }}</span>
          </div>
        </div>
      </div>

      <div class="cart-panel">
        <div class="cart-block">
          <div class="cart-label">客户</div>
          <el-select v-model="selectedCustomer" placeholder="请选择客户" filterable value-key="customer_id" class="dark-select">
            <el-option v-for="c in customers" :key="c.customer_id" :label="`${c.customer_name} — ${c.phone}`" :value="c" />
          </el-select>
          <div v-if="selectedCustomer" class="addr">{{ selectedCustomer.address }}</div>
        </div>

        <div class="cart-block cart-main">
          <div class="cart-label">清单</div>
          <div v-if="cartItems.length === 0" class="cart-empty">点击左侧商品添加</div>
          <template v-else>
            <div v-for="(item, index) in cartItems" :key="item.product_id" class="cart-row">
              <div class="cart-row-top">
                <span class="cart-row-name">{{ item.brand }} {{ item.model }}</span>
                <span class="cart-row-price mono">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
              </div>
              <div class="cart-row-ctl">
                <el-input-number v-model="item.quantity" :min="1" :max="item.stock" size="small" controls-position="right" class="qty" />
                <button class="rm" @click="removeFromCart(index)">×</button>
              </div>
            </div>
            <div class="cart-total">
              <span class="total-label">共 {{ totalCount }} 件</span>
              <span class="total-price mono">¥{{ totalPrice.toFixed(2) }}</span>
            </div>
            <button class="submit" :disabled="!selectedCustomer || cartItems.length === 0 || submitting" @click="submitOrder">{{ submitting ? '...' : '提交订单' }}</button>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCustomers, getProducts, createOrder } from '../api'

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
  return products.value.filter(p => p.brand.toLowerCase().includes(kw) || p.model.toLowerCase().includes(kw))
})
const totalCount = computed(() => cartItems.value.reduce((s, i) => s + i.quantity, 0))
const totalPrice = computed(() => cartItems.value.reduce((s, i) => s + i.price * i.quantity, 0))

const fetchProducts = async () => {
  loading.value = true
  try { products.value = (await getProducts(selectCategory.value || undefined)).data || [] } catch {} finally { loading.value = false }
}
const fetchCustomers = async () => {
  try { customers.value = (await getCustomers()).data || [] } catch {}
}

const addToCart = (p) => {
  const ex = cartItems.value.find(i => i.product_id === p.product_id)
  if (ex) ex.quantity < p.stock ? ex.quantity++ : ElMessage.warning('库存不足')
  else cartItems.value.push({ ...p, quantity: 1 })
}
const removeFromCart = (i) => cartItems.value.splice(i, 1)

const submitOrder = async () => {
  if (!selectedCustomer.value) return ElMessage.warning('请选择客户')
  submitting.value = true
  try {
    const res = await createOrder({ customer_id: selectedCustomer.value.customer_id, items: cartItems.value.map(i => ({ product_id: i.product_id, quantity: i.quantity })) })
    if (res.data?.status === 0) { ElMessage.success(`下单成功 ${res.data.order_no}`); cartItems.value = []; fetchProducts() }
    else ElMessage.error('下单失败')
  } catch {} finally { submitting.value = false }
}

onMounted(() => { fetchProducts(); fetchCustomers() })
</script>

<style scoped>
.order-page { padding-top: 24px; }
.order-grid { display: grid; grid-template-columns: 1fr 320px; gap: 40px; align-items: start; }

.panel-top { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }

.search-box { position: relative; flex: 1; }
.search-input {
  width: 100%; height: 28px; padding: 0 24px 0 0;
  background: none; border: none; color: var(--text-secondary);
  font-size: 13px; font-family: var(--font-sans); outline: none;
  transition: color 0.15s;
}
.search-input::placeholder { color: var(--text-tertiary); }
.search-input:focus { color: var(--text-primary); }
.search-icon { position: absolute; right: 4px; top: 50%; transform: translateY(-50%); color: var(--text-tertiary); pointer-events: none; }

.tabs { display: flex; gap: 2px; flex-shrink: 0; }
.tab { font-size: 12px; padding: 3px 8px; border: none; background: none; color: var(--text-secondary); cursor: pointer; font-family: var(--font-sans); transition: color 0.15s; }
.tab:hover { color: var(--text-primary); }
.tab.active { color: #e0e0e0; }

.row {
  display: grid; grid-template-columns: 1fr 70px 60px 100px;
  align-items: center; padding: 10px 8px; cursor: pointer; transition: background 0.1s;
}
.row:hover { background: var(--bg-hover); }
.row-name { font-size: 13px; color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.row-cat { font-size: 11px; color: var(--text-tertiary); text-align: center; }
.row-stock { font-size: 12px; text-align: right; padding-right: 16px; }
.row-price { font-size: 14px; font-weight: 500; color: var(--text-primary); text-align: right; }
.mono { font-family: var(--font-mono); }
.mono-dim { font-family: var(--font-mono); color: var(--text-tertiary); }

.cart-block { margin-bottom: 24px; }
.cart-label { font-size: 12px; color: var(--text-tertiary); margin-bottom: 8px; }
.addr { font-size: 12px; color: var(--text-tertiary); margin-top: 6px; }

.cart-main { border: 1px solid var(--border-dim); padding: 16px; }
.cart-empty { font-size: 13px; color: var(--text-tertiary); text-align: center; padding: 24px 0; }

.cart-row { padding: 8px 0; border-bottom: 1px solid var(--border-dim); }
.cart-row:last-child { border-bottom: none; }
.cart-row-top { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 4px; }
.cart-row-name { font-size: 13px; color: var(--text-primary); }
.cart-row-price { font-size: 14px; font-weight: 500; color: var(--text-primary); }
.cart-row-ctl { display: flex; align-items: center; gap: 8px; }
.rm { background: none; border: none; font-size: 16px; color: var(--text-tertiary); cursor: pointer; }
.rm:hover { color: #f56c6c; }

.cart-total { display: flex; justify-content: space-between; align-items: baseline; padding-top: 12px; }
.total-label { font-size: 13px; color: var(--text-secondary); }
.total-price { font-size: 20px; font-weight: 600; color: var(--text-primary); letter-spacing: -0.5px; }

.submit {
  width: 100%; margin-top: 16px; padding: 10px 0;
  background: var(--accent); border: none; color: #0a0a0a;
  font-size: 14px; font-weight: 600; cursor: pointer; font-family: var(--font-sans);
  transition: opacity 0.15s;
}
.submit:hover:not(:disabled) { opacity: 0.9; }
.submit:disabled { opacity: 0.3; cursor: default; }
</style>
