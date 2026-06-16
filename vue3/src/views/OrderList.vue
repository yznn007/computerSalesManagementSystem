<template>
  <div>
    <h2 style="margin: 0 0 16px">订单管理</h2>

    <div style="display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap">
      <el-select v-model="statusFilter" placeholder="订单状态" clearable style="width: 140px" @change="fetchOrders">
        <el-option label="全部" value="" />
        <el-option label="待付款" value="待付款" />
        <el-option label="已付款" value="已付款" />
        <el-option label="已发货" value="已发货" />
        <el-option label="已取消" value="已取消" />
      </el-select>
    </div>

    <el-table :data="orders" border stripe v-loading="loading">
      <el-table-column prop="order_no" label="订单号" width="200" />
      <el-table-column prop="customer_name" label="客户" width="120" />
      <el-table-column prop="total_amount" label="总金额" width="120">
        <template #default="{ row }">￥{{ row.total_amount }}</template>
      </el-table-column>
      <el-table-column prop="order_date" label="下单时间" width="180" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="showDetail(row)">详情</el-button>
          <el-dropdown v-if="row.status !== '已取消' && row.status !== '已发货'" @command="(cmd) => changeStatus(row, cmd)" style="margin-left: 8px">
            <el-button size="small" type="primary">
              操作 <el-icon><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="row.status === '待付款'" command="已付款">标记已付款</el-dropdown-item>
                <el-dropdown-item v-if="row.status === '已付款'" command="已发货">标记已发货</el-dropdown-item>
                <el-dropdown-item command="已取消">取消订单</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="700px">
      <template v-if="detailOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ detailOrder.order_no }}</el-descriptions-item>
          <el-descriptions-item label="客户">{{ detailOrder.customer_name }}</el-descriptions-item>
          <el-descriptions-item label="总金额">￥{{ detailOrder.total_amount }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detailOrder.status)">{{ detailOrder.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ detailOrder.order_date }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />
        <h4>商品明细</h4>
        <el-table :data="detailItems" border size="small">
          <el-table-column prop="brand" label="品牌" width="100" />
          <el-table-column prop="model" label="型号" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="unit_price" label="单价" width="120">
            <template #default="{ row }">￥{{ row.unit_price }}</template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrders, getOrderDetail, updateOrderStatus } from '../api'

const statusFilter = ref('')
const orders = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const detailOrder = ref(null)
const detailItems = ref([])

const statusType = (s) => ({ '待付款': 'warning', '已付款': 'primary', '已发货': 'success', '已取消': 'info' }[s] || '')

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await getOrders(statusFilter.value ? { status: statusFilter.value } : undefined)
    orders.value = res.data || []
  } finally { loading.value = false }
}

const showDetail = async (row) => {
  detailOrder.value = row
  detailVisible.value = true
  try {
    const res = await getOrderDetail(row.order_id)
    detailItems.value = res.data?.items || []
  } catch { detailItems.value = [] }
}

const changeStatus = async (row, status) => {
  try {
    await ElMessageBox.confirm(`确认将订单标记为「${status}」？`, '提示', { type: 'warning' })
    await updateOrderStatus(row.order_id, status)
    ElMessage.success('状态已更新')
    fetchOrders()
  } catch {}
}

onMounted(fetchOrders)
</script>
