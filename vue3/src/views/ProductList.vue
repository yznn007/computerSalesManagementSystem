<template>
  <div class="page">
    <div class="page-header">
      <div class="category-tabs">
        <button
          v-for="cat in ['', '笔记本', '台式机整机', 'DIY配件']"
          :key="cat"
          class="tab"
          :class="{ active: activeCategory === cat }"
          @click="activeCategory = cat; fetchProducts()"
        >
          {{ cat || '全部' }}
        </button>
      </div>
      <div v-if="searchKeyword" class="search-notice">
        搜索 <span class="mono">{{ searchKeyword }}</span>
      </div>
    </div>

    <div class="product-grid" v-loading="loading">
      <div
        v-for="p in filteredProducts"
        :key="p.product_id"
        class="product-item"
        @click="showDetail(p)"
      >
        <div class="product-item-top">
          <span class="product-item-tag">{{ p.category }}</span>
          <span class="product-item-stock">{{ p.stock }}</span>
        </div>
        <div class="product-item-name">{{ p.brand }} {{ p.model }}</div>
        <div class="product-item-price">¥{{ p.price }}</div>
      </div>
    </div>

    <div v-if="!loading && products.length === 0" class="empty">暂无商品</div>

    <el-dialog v-model="detailVisible" :title="detailProduct?.model" width="480px">
      <template v-if="detailProduct">
        <div class="detail-table">
          <div class="detail-row"><span>品牌</span><span>{{ detailProduct.brand }}</span></div>
          <div class="detail-row"><span>型号</span><span>{{ detailProduct.model }}</span></div>
          <div class="detail-row"><span>售价</span><span class="mono">¥{{ detailProduct.price }}</span></div>
          <div class="detail-row"><span>库存</span><span class="mono">{{ detailProduct.stock }}</span></div>
          <div class="detail-row"><span>分类</span><span>{{ detailProduct.category }}</span></div>
        </div>

        <div v-if="detailDetail" class="detail-extra">
          <div v-if="detailDetail.screen_size !== undefined" class="detail-row"><span>屏幕</span><span>{{ detailDetail.screen_size }}</span></div>
          <div v-if="detailDetail.cpu_model" class="detail-row"><span>处理器</span><span>{{ detailDetail.cpu_model }}</span></div>
          <div v-if="detailDetail.gpu_model" class="detail-row"><span>显卡</span><span>{{ detailDetail.gpu_model }}</span></div>
          <div v-if="detailDetail.weight" class="detail-row"><span>重量</span><span>{{ detailDetail.weight }}</span></div>
          <div v-if="detailDetail.part_type" class="detail-row"><span>配件类型</span><span>{{ detailDetail.part_type }}</span></div>
          <div v-if="detailDetail.specification" class="detail-row"><span>规格</span><span>{{ detailDetail.specification }}</span></div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getProducts, getProductDetail } from '../api'

const route = useRoute()

const activeCategory = ref('')
const searchKeyword = ref('')
const products = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const detailProduct = ref(null)
const detailDetail = ref(null)

const filteredProducts = computed(() => {
  if (!searchKeyword.value) return products.value
  const kw = searchKeyword.value.toLowerCase()
  return products.value.filter(p =>
    p.brand.toLowerCase().includes(kw) || p.model.toLowerCase().includes(kw)
  )
})

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await getProducts(activeCategory.value || undefined)
    products.value = res.data || []
  } finally { loading.value = false }
}

const showDetail = async (product) => {
  detailProduct.value = product
  detailVisible.value = true
  try {
    const res = await getProductDetail(product.product_id)
    detailDetail.value = res.data
  } catch { detailDetail.value = null }
}

watch(() => route.query.search, (val) => {
  searchKeyword.value = val || ''
})

onMounted(() => {
  searchKeyword.value = route.query.search || ''
  fetchProducts()
})
</script>

<style scoped>
.page { padding-top: 24px; }

.page-header { margin-bottom: 24px; }

.category-tabs { display: flex; gap: 4px; }
.tab {
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
.tab:hover { color: var(--text-primary); background: var(--bg-hover); }
.tab.active { color: var(--accent); }

.search-notice {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-secondary);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.product-item {
  padding: 16px;
  border: 1px solid var(--border-dim);
  cursor: pointer;
  transition: background 0.1s;
}
.product-item:hover { background: var(--bg-hover); }
.product-item-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.product-item-tag {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 3px;
  background: var(--border-dim);
  color: var(--text-secondary);
}
.product-item-stock {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-tertiary);
}
.product-item-name {
  font-size: 13px;
  color: var(--text-primary);
  margin-bottom: 6px;
  line-height: 1.4;
}
.product-item-price {
  font-family: var(--font-mono);
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
}

.empty {
  text-align: center;
  color: var(--text-tertiary);
  font-size: 13px;
  padding: 64px 0;
}

.detail-extra { margin-top: 24px; }
.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--border-dim);
  font-size: 13px;
}
.detail-row span:first-child { color: var(--text-secondary); }
.detail-row span:last-child { color: var(--text-primary); }
.mono { font-family: var(--font-mono); }
</style>
