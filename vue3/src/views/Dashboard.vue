<template>
  <div class="dashboard">
    <div class="hero">
      <pre class="ascii">
  ██████╗ ███████╗
 ██╔════╝ ██╔════╝
 ██║      ███████╗
 ██║      ╚════██║
 ╚██████╗ ███████║
  ╚═════╝ ╚══════╝</pre>
      <div class="hero-sub">One click, one PC</div>
      <div class="search-wrapper">
        <div class="search-box">
          <svg class="search-icon" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
          <input
            v-model="keyword"
            class="search-input"
            placeholder="Search products..."
            @keydown.enter="search"
          />
        </div>
      </div>
    </div>

    <div class="product-section">
      <div class="page-header">
        <div class="category-tabs">
          <div v-for="cat in cats" :key="cat" class="tab-group" @mouseenter="hoveredCat = cat" @mouseleave="hoveredCat = ''">
            <button class="tab" :class="{ active: activeCategory === cat }" @click="selectCategory(cat)">
              {{ cat || '全部' }}
              <svg v-if="subCats[cat]?.length" class="tab-arrow" width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m6 9 6 6 6-6"/></svg>
            </button>
            <div v-if="subCats[cat]?.length" class="dropdown" :class="{ show: hoveredCat === cat || activeCategory === cat }">
              <button v-for="sub in subCats[cat]" :key="sub" class="dropdown-item" :class="{ active: activeSub === sub && activeCategory === cat }" @click="selectSub(cat, sub)">{{ sub }}</button>
            </div>
          </div>
        </div>
        <div v-if="keyword" class="search-notice">搜索「{{ keyword }}」</div>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getProducts, getProductDetail } from '../api'

const router = useRouter()
const keyword = ref('')
const cats = ['', '笔记本', '台式机整机', 'DIY配件']
const subCats = {
  '笔记本': ['Lenovo', 'Apple', 'ASUS', 'Dell'],
  '台式机整机': ['Lenovo', 'HP'],
  'DIY配件': ['CPU', '显卡', '主板', '内存', '硬盘']
}
const activeCategory = ref('')
const activeSub = ref('')
const hoveredCat = ref('')
const products = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const detailProduct = ref(null)
const detailDetail = ref(null)

const filteredProducts = computed(() => {
  let result = products.value
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    result = result.filter(p => p.brand.toLowerCase().includes(kw) || p.model.toLowerCase().includes(kw))
  }
  if (activeSub.value && activeCategory.value === 'DIY配件') {
    result = result.filter(p => p.brand.toLowerCase().includes(activeSub.value.toLowerCase()))
  }
  return result
})

const search = () => {}

const selectCategory = (cat) => {
  if (activeCategory.value === cat) {
    activeCategory.value = ''
    activeSub.value = ''
  } else {
    activeCategory.value = cat
    activeSub.value = ''
  }
  fetchProducts()
}

const selectSub = (cat, sub) => {
  activeCategory.value = cat
  activeSub.value = activeSub.value === sub ? '' : sub
  fetchProducts()
}

const fetchProducts = async () => {
  loading.value = true
  try { products.value = (await getProducts(activeCategory.value || undefined)).data || [] } catch {} finally { loading.value = false }
}

const showDetail = async (p) => {
  detailProduct.value = p; detailVisible.value = true
  try { detailDetail.value = (await getProductDetail(p.product_id)).data } catch { detailDetail.value = null }
}

onMounted(() => { fetchProducts() })
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 120px;
}

.hero {
  text-align: center;
}

.ascii {
  font-family: var(--font-mono);
  font-size: 18px;
  line-height: 1.25;
  margin: 0 0 24px;
  letter-spacing: 0;
  background: linear-gradient(180deg, #d0d0d0 0%, #8a8a8a 40%, #5a5a5a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-sub {
  font-size: 20px;
  font-weight: 500;
  color: #6a6a6a;
  letter-spacing: 2px;
}

.search-wrapper {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

.search-box {
  position: relative;
  width: 100%;
  max-width: 390px;
}

.search-icon {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  color: #4a4a4a;
  pointer-events: none;
}

.search-input {
  width: 100%;
  padding: 12px 0 12px 28px;
  background: transparent;
  border: none;
  border-bottom: 1px solid #333;
  color: #d0d0d0;
  font-family: var(--font-mono);
  font-size: 14px;
  outline: none;
  caret-color: #d0d0d0;
  transition: border-color 0.2s;
}

.search-input::placeholder {
  color: #4a4a4a;
}

.search-input::selection {
  background: rgba(255, 255, 255, 0.2);
}

.search-input:focus {
  border-color: #555;
}

.product-section {
  width: 100%;
  max-width: 720px;
  margin-top: 60px;
}

.page-header { display: flex; align-items: center; gap: 16px; margin-bottom: 32px; }

.category-tabs { display: flex; gap: 2px; }
.tab-group { position: relative; }
.tab {
  font-size: 12px; padding: 3px 8px; border: none; background: none;
  color: var(--text-secondary); cursor: pointer; font-family: var(--font-sans);
  transition: color 0.15s; display: flex; align-items: center; gap: 2px;
}
.tab:hover { color: var(--text-primary); }
.tab.active { color: #e0e0e0; }
.tab-arrow { transition: transform 0.2s; }
.tab-group:hover .tab-arrow { transform: rotate(180deg); }

.dropdown {
  position: absolute; top: 100%; left: 0; min-width: 80px;
  background: #1a1a1a; border: 1px solid #333; border-radius: 4px;
  padding: 4px 0; opacity: 0; visibility: hidden;
  transition: opacity 0.15s, visibility 0.15s; z-index: 10;
}
.dropdown.show { opacity: 1; visibility: visible; }
.dropdown-item {
  display: block; width: 100%; padding: 6px 12px; border: none; background: none;
  color: var(--text-secondary); font-size: 12px; text-align: left;
  cursor: pointer; transition: background 0.1s, color 0.1s;
}
.dropdown-item:hover { background: #252525; color: var(--text-primary); }
.dropdown-item.active { color: #e0e0e0; }

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
