<template>
  <div class="page">
    <div class="page-header">
      <div class="category-tabs">
        <button v-for="cat in cats" :key="cat" class="tab" :class="{ active: activeCategory === cat }" @click="activeCategory = cat; fetchProducts()">{{ cat || '全部' }}</button>
      </div>
      <div v-if="searchKeyword" class="search-notice">搜索「{{ searchKeyword }}」</div>
    </div>

    <div v-loading="loading">
      <div v-for="(p, i) in filteredProducts" :key="p.product_id" class="row" @click="showDetail(p)">
        <span class="row-num">{{ i + 1 }}</span>
        <span class="row-name">{{ p.brand }} {{ p.model }}</span>
        <span class="row-tag">{{ p.category }}</span>
        <span class="row-stock mono-dim">{{ p.stock }}</span>
        <span class="row-price mono">¥{{ p.price }}</span>
      </div>
      <div v-if="!loading && filteredProducts.length === 0" class="empty">暂无商品</div>
    </div>

    <el-dialog v-model="detailVisible" :title="detailProduct?.model" width="420px">
      <template v-if="detailProduct">
        <div class="detail-row"><span>品牌</span><span>{{ detailProduct.brand }}</span></div>
        <div class="detail-row"><span>售价</span><span class="mono">¥{{ detailProduct.price }}</span></div>
        <div class="detail-row"><span>库存</span><span class="mono">{{ detailProduct.stock }}</span></div>
        <div v-if="detailDetail">
          <div v-if="detailDetail.screen_size !== undefined" class="detail-row"><span>屏幕</span><span>{{ detailDetail.screen_size }}</span></div>
          <div v-if="detailDetail.cpu_model" class="detail-row"><span>处理器</span><span>{{ detailDetail.cpu_model }}</span></div>
          <div v-if="detailDetail.gpu_model" class="detail-row"><span>显卡</span><span>{{ detailDetail.gpu_model }}</span></div>
          <div v-if="detailDetail.weight" class="detail-row"><span>重量</span><span>{{ detailDetail.weight }}</span></div>
          <div v-if="detailDetail.part_type" class="detail-row"><span>配件</span><span>{{ detailDetail.part_type }} / {{ detailDetail.specification }}</span></div>
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
const cats = ['', '笔记本', '台式机整机', 'DIY配件']
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
  return products.value.filter(p => p.brand.toLowerCase().includes(kw) || p.model.toLowerCase().includes(kw))
})

const fetchProducts = async () => {
  loading.value = true
  try { products.value = (await getProducts(activeCategory.value || undefined)).data || [] } catch {} finally { loading.value = false }
}

const showDetail = async (p) => {
  detailProduct.value = p; detailVisible.value = true
  try { detailDetail.value = (await getProductDetail(p.product_id)).data } catch { detailDetail.value = null }
}

watch(() => route.query.search, v => searchKeyword.value = v || '')
onMounted(() => { searchKeyword.value = route.query.search || ''; fetchProducts() })
</script>

<style scoped>
.page { padding-top: 24px; }

.page-header { display: flex; align-items: center; gap: 16px; margin-bottom: 32px; }

.category-tabs { display: flex; gap: 2px; }
.tab {
  font-size: 12px; padding: 3px 8px; border: none; background: none;
  color: var(--text-secondary); cursor: pointer; font-family: var(--font-sans);
  transition: color 0.15s;
}
.tab:hover { color: var(--text-primary); }
.tab.active { color: #e0e0e0; }

.search-notice { font-size: 12px; color: var(--text-secondary); }

.row {
  display: grid;
  grid-template-columns: 32px 1fr 80px 60px 100px;
  align-items: center;
  padding: 10px 8px;
  cursor: pointer;
  transition: background 0.1s;
}
.row:hover { background: var(--bg-hover); }

.row-num { font-family: var(--font-mono); font-size: 12px; color: var(--text-tertiary); text-align: right; padding-right: 8px; }
.row-name { font-size: 13px; color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.row-tag { font-size: 11px; color: var(--text-tertiary); text-align: center; }
.row-stock { font-size: 12px; text-align: right; padding-right: 16px; }
.row-price { font-size: 14px; font-weight: 500; color: var(--text-primary); text-align: right; }

.mono { font-family: var(--font-mono); }
.mono-dim { font-family: var(--font-mono); color: var(--text-tertiary); }

.empty { text-align: center; color: var(--text-tertiary); padding: 80px 0; font-size: 13px; }

.detail-row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 13px; }
.detail-row span:first-child { color: var(--text-secondary); }
</style>
