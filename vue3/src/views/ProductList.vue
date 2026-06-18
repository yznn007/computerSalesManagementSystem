<template>
  <div class="page">
    <div class="page-header">
      <div class="category-tabs">
        <button v-for="cat in cats" :key="cat" class="tab" :class="{ active: activeCategory === cat }" @click="activeCategory = cat; fetchProducts()">{{ cat || '全部' }}</button>
      </div>
      <button v-if="isStaff" class="add-btn" @click="openDialog()">+ 新增商品</button>
      <div v-if="searchKeyword" class="search-notice">搜索「{{ searchKeyword }}」</div>
    </div>

    <div v-loading="loading">
      <div v-for="(p, i) in filteredProducts" :key="p.product_id" class="row" @click="showDetail(p)">
        <span class="row-num">{{ i + 1 }}</span>
        <span class="row-name">{{ p.brand }} {{ p.model }}</span>
        <span class="row-tag">{{ p.category }}</span>
        <span class="row-stock mono-dim">{{ p.stock }}</span>
        <span class="row-price mono">¥{{ p.price }}</span>
        <span v-if="isStaff" class="row-ops" @click.stop>
          <button class="row-btn" @click="openDialog(p)">编辑</button>
          <button class="row-btn warn" @click="remove(p)">删除</button>
        </span>
      </div>
      <div v-if="!loading && filteredProducts.length === 0" class="empty">暂无商品</div>
    </div>

    <el-dialog v-model="detailVisible" :title="detailProduct?.model" width="420px">
      <template v-if="detailProduct">
        <div class="detail-row"><span>品牌</span><span>{{ detailProduct.brand }}</span></div>
        <div class="detail-row"><span>售价</span><span class="mono">¥{{ detailProduct.price }}</span></div>
        <div class="detail-row"><span>库存</span><span class="mono">{{ detailProduct.stock }}</span></div>
        <div v-if="detailDetail">
          <div v-if="detailDetail.screen_size" class="detail-row"><span>屏幕</span><span>{{ detailDetail.screen_size }}</span></div>
          <div v-if="detailDetail.cpu_model" class="detail-row"><span>处理器</span><span>{{ detailDetail.cpu_model }}</span></div>
          <div v-if="detailDetail.gpu_model" class="detail-row"><span>显卡</span><span>{{ detailDetail.gpu_model }}</span></div>
          <div v-if="detailDetail.weight" class="detail-row"><span>重量</span><span>{{ detailDetail.weight }}</span></div>
          <div v-if="detailDetail.form_factor" class="detail-row"><span>机箱</span><span>{{ detailDetail.form_factor }}</span></div>
          <div v-if="detailDetail.cpu_desc" class="detail-row"><span>处理器</span><span>{{ detailDetail.cpu_desc }}</span></div>
          <div v-if="detailDetail.gpu_desc" class="detail-row"><span>显卡</span><span>{{ detailDetail.gpu_desc }}</span></div>
          <div v-if="detailDetail.ram_desc" class="detail-row"><span>内存</span><span>{{ detailDetail.ram_desc }}</span></div>
          <div v-if="detailDetail.storage_desc" class="detail-row"><span>存储</span><span>{{ detailDetail.storage_desc }}</span></div>
          <div v-if="detailDetail.part_type" class="detail-row"><span>配件</span><span>{{ detailDetail.part_type }} / {{ detailDetail.specification }}</span></div>
        </div>
        <div v-if="detailDetail?.composition?.length" style="margin-top:12px;border-top:1px solid #333;padding-top:12px">
          <div style="font-size:12px;color:#999;margin-bottom:8px">组装配置</div>
          <div v-for="c in detailDetail.composition" :key="c.product_id" class="detail-row">
            <span>{{ c.part_type }} · {{ c.brand }} {{ c.model }}</span>
            <span class="mono">×{{ c.quantity }}</span>
          </div>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑商品' : '新增商品'" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="品牌"><el-input v-model="form.brand" /></el-form-item>
        <el-form-item label="型号"><el-input v-model="form.model" /></el-form-item>
        <el-form-item label="售价"><el-input-number v-model="form.price" :min="0" :precision="2" controls-position="right" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" controls-position="right" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" :disabled="!!editing" placeholder="请选择">
            <el-option v-for="c in ['笔记本','台式机整机','DIY配件']" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>

        <template v-if="form.category === '笔记本'">
          <el-form-item label="屏幕"><el-input v-model="form.screen_size" /></el-form-item>
          <el-form-item label="处理器"><el-input v-model="form.cpu_model" /></el-form-item>
          <el-form-item label="显卡"><el-input v-model="form.gpu_model" /></el-form-item>
          <el-form-item label="重量"><el-input v-model="form.weight" /></el-form-item>
        </template>
        <template v-else-if="form.category === '台式机整机'">
          <el-form-item label="机箱类型"><el-input v-model="form.form_factor" /></el-form-item>
          <el-form-item label="处理器"><el-input v-model="form.cpu_desc" /></el-form-item>
          <el-form-item label="显卡"><el-input v-model="form.gpu_desc" /></el-form-item>
          <el-form-item label="内存"><el-input v-model="form.ram_desc" /></el-form-item>
          <el-form-item label="存储"><el-input v-model="form.storage_desc" /></el-form-item>
        </template>
        <template v-else-if="form.category === 'DIY配件'">
          <el-form-item label="配件类型">
            <el-select v-model="form.part_type" placeholder="请选择">
              <el-option v-for="t in ['CPU','显卡','主板','内存','硬盘','电源','机箱','散热器']" :key="t" :label="t" :value="t" />
            </el-select>
          </el-form-item>
          <el-form-item label="规格参数"><el-input v-model="form.specification" type="textarea" :rows="2" /></el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" size="small">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit" size="small">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProducts, getProductDetail, createProduct, updateProduct, deleteProduct } from '../api'

const route = useRoute()
const isStaff = computed(() => localStorage.getItem('role') === 'staff')
const cats = ['', '笔记本', '台式机整机', 'DIY配件']
const activeCategory = ref('')
const searchKeyword = ref('')
const products = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const detailProduct = ref(null)
const detailDetail = ref(null)

const dialogVisible = ref(false)
const submitting = ref(false)
const editing = ref(null)
const blankForm = () => ({ brand: '', model: '', price: 0, stock: 0, category: '笔记本', screen_size: '', cpu_model: '', gpu_model: '', weight: '', form_factor: '', cpu_desc: '', gpu_desc: '', ram_desc: '', storage_desc: '', part_type: '', specification: '' })
const form = ref(blankForm())

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

const openDialog = async (p) => {
  if (p) {
    editing.value = p.product_id
    try {
      const d = (await getProductDetail(p.product_id)).data
      form.value = { brand: d.brand, model: d.model, price: Number(d.price), stock: d.stock, category: d.category,
        screen_size: d.screen_size || '', cpu_model: d.cpu_model || '', gpu_model: d.gpu_model || '', weight: d.weight || '',
        form_factor: d.form_factor || '', cpu_desc: d.cpu_desc || '', gpu_desc: d.gpu_desc || '', ram_desc: d.ram_desc || '', storage_desc: d.storage_desc || '',
        part_type: d.part_type || '', specification: d.specification || '' }
    } catch { editing.value = null; return }
  } else {
    editing.value = null
    form.value = blankForm()
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.brand || !form.value.model || !form.value.category) return ElMessage.warning('请填写完整')
  submitting.value = true
  try {
    if (editing.value) { await updateProduct(editing.value, form.value); ElMessage.success('已更新') }
    else { await createProduct(form.value); ElMessage.success('已添加') }
    dialogVisible.value = false; fetchProducts()
  } catch {} finally { submitting.value = false }
}

const remove = async (p) => {
  await ElMessageBox.confirm(`确认删除商品 ${p.brand} ${p.model}？`, '提示', { type: 'warning' }).catch(() => { throw 'cancel' })
  try { await deleteProduct(p.product_id); ElMessage.success('已删除'); fetchProducts() } catch {}
}

watch(() => route.query.search, v => searchKeyword.value = v || '')
onMounted(() => { searchKeyword.value = route.query.search || ''; fetchProducts() })
</script>

<style scoped>
.page { padding-top: 24px; }
.page-header { display: flex; align-items: center; gap: 16px; margin-bottom: 32px; }
.category-tabs { display: flex; gap: 2px; }
.tab { font-size: 12px; padding: 3px 8px; border: none; background: none; color: var(--text-secondary); cursor: pointer; font-family: var(--font-sans); transition: color 0.15s; }
.tab:hover { color: var(--text-primary); }
.tab.active { color: #e0e0e0; }
.add-btn { font-size: 13px; padding: 6px 14px; border: 1px solid #333; background: none; color: #5a5a5a; cursor: pointer; border-radius: 4px; font-family: var(--font-sans); }
.add-btn:hover { background: #161616; color: #e0e0e0; }
.search-notice { font-size: 12px; color: var(--text-secondary); }
.row { display: grid; grid-template-columns: 32px 1fr 90px 60px 100px 120px; align-items: center; padding: 10px 8px; cursor: pointer; transition: background 0.1s; }
.row:hover { background: var(--bg-hover); }
.row-num { font-family: var(--font-mono); font-size: 12px; color: var(--text-tertiary); text-align: right; padding-right: 8px; }
.row-name { font-size: 13px; color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.row-tag { font-size: 11px; color: var(--text-tertiary); text-align: center; }
.row-stock { font-size: 12px; text-align: right; padding-right: 16px; }
.row-price { font-size: 14px; font-weight: 500; color: var(--text-primary); text-align: right; }
.row-ops { display: flex; gap: 4px; justify-content: flex-end; }
.mono { font-family: var(--font-mono); }
.mono-dim { font-family: var(--font-mono); color: var(--text-tertiary); }
.empty { text-align: center; color: var(--text-tertiary); padding: 80px 0; font-size: 13px; }
.detail-row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 13px; }
.detail-row span:first-child { color: var(--text-secondary); }
.row-btn { font-size: 12px; padding: 2px 8px; border: 1px solid var(--border-dim); background: none; color: var(--text-secondary); cursor: pointer; border-radius: 3px; font-family: var(--font-sans); }
.row-btn:hover { color: var(--text-primary); border-color: #333; }
.row-btn.warn { color: #f56c6c; border-color: #f56c6c66; }
</style>
