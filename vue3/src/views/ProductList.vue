<template>
  <div>
    <h2 style="margin: 0 0 16px">商品列表</h2>

    <el-tabs v-model="activeCategory" @tab-change="fetchProducts">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="笔记本" name="笔记本" />
      <el-tab-pane label="台式机整机" name="台式机整机" />
      <el-tab-pane label="DIY配件" name="DIY配件" />
    </el-tabs>

    <el-row :gutter="16" v-loading="loading">
      <el-col v-for="p in products" :key="p.product_id" :span="6" style="margin-bottom: 16px">
        <ProductCard :product="p" @click="showDetail(p)" />
      </el-col>
    </el-row>

    <el-empty v-if="!loading && products.length === 0" description="暂无商品" />

    <el-dialog v-model="detailVisible" :title="detailProduct?.model" width="550px">
      <template v-if="detailProduct">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="品牌">{{ detailProduct.brand }}</el-descriptions-item>
          <el-descriptions-item label="型号">{{ detailProduct.model }}</el-descriptions-item>
          <el-descriptions-item label="售价">￥{{ detailProduct.price }}</el-descriptions-item>
          <el-descriptions-item label="库存">{{ detailProduct.stock }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ detailProduct.category }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <template v-if="detailDetail">
          <!-- 笔记本详情 -->
          <template v-if="detailDetail.screen_size !== undefined">
            <h4>笔记本参数</h4>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="屏幕">{{ detailDetail.screen_size }}</el-descriptions-item>
              <el-descriptions-item label="处理器">{{ detailDetail.cpu_model }}</el-descriptions-item>
              <el-descriptions-item label="显卡">{{ detailDetail.gpu_model }}</el-descriptions-item>
              <el-descriptions-item label="重量">{{ detailDetail.weight }}</el-descriptions-item>
            </el-descriptions>
          </template>
          <!-- 配件详情 -->
          <template v-else-if="detailDetail.part_type !== undefined">
            <h4>配件规格</h4>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="配件类型">{{ detailDetail.part_type }}</el-descriptions-item>
              <el-descriptions-item label="规格">{{ detailDetail.specification }}</el-descriptions-item>
            </el-descriptions>
          </template>
        </template>
        <p v-else style="color: #707070">该商品暂无详细参数</p>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProducts, getProductDetail } from '../api'
import ProductCard from '../components/ProductCard.vue'

const activeCategory = ref('')
const products = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const detailProduct = ref(null)
const detailDetail = ref(null)

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
  } catch {
    detailDetail.value = null
  }
}

onMounted(fetchProducts)
</script>
