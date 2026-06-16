<template>
  <div class="page">
    <div class="page-header">
      <div class="filters">
        <button
          v-for="s in ['', '待付款', '已付款', '已发货', '已取消']"
          :key="s"
          class="filter-btn"
          :class="{ active: statusFilter === s }"
          @click="statusFilter = s; fetchOrders()"
        >
          {{ s || '全部' }}
        </button>
      </div>
    </div>

    <el-table :data="orders" v-loading="loading" size="small">
      <el-table-column prop="order_no" label="订单号" width="200">
        <template #default="{ row }">
          <span class="mono">{{ row.order_no }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="customer_name" label="客户" width="80" />
      <el-table-column label="金额" width="110">
        <template #default="{ row }">
          <span class="mono">¥{{ row.total_amount }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="order_date" label="时间" width="160">
        <template #default="{ row }">
          <span class="mono dim">{{ row.order_date }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <span class="status-dot" :class="row.status">{{ row.status }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <button class="row-btn" @click="showDetail(row)">详情</button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="订单详情" width="500px">
      <template v-if="detailOrder">
        <div class="detail-table">
          <div class="detail-row"><span>订单号</span><span class="mono">{{ detailOrder.order_no }}</span></div>
          <div class="detail-row"><span>客户</span><span>{{ detailOrder.customer_name }}</span></div>
          <div class="detail-row"><span>总金额</span><span class="mono">¥{{ detailOrder.total_amount }}</span></div>
          <div class="detail-row"><span>状态</span><span>{{ detailOrder.status }}</span></div>
          <div class="detail-row"><span>时间</span><span>{{ detailOrder.order_date }}</span></div>
        </div>
        <div v-if="detailItems.length" class="detail-sub" v-for="item in detailItems" :key="item.detail_id">
          <span>{{ item.brand }} {{ item.model }}</span>
          <span class="mono dim">×{{ item.quantity }} ¥{{ item.unit_price }}</span>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOrders, getOrderDetail } from '../api'

const statusFilter = ref('')
const orders = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const detailOrder = ref(null)
const detailItems = ref([])

const fetchOrders = async () => {
  loading.value = true
  try { orders.value = (await getOrders(statusFilter.value ? { status: statusFilter.value } : undefined)).data || [] }
  catch {} finally { loading.value = false }
}

const showDetail = async (row) => {
  detailOrder.value = row
  detailVisible.value = true
  try { detailItems.value = (await getOrderDetail(row.order_id)).data?.items || [] }
  catch { detailItems.value = [] }
}

onMounted(fetchOrders)
</script>

<style scoped>
.page { padding-top: 24px; }
.page-header { margin-bottom: 16px; }

.filters { display: flex; gap: 4px; }
.filter-btn {
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
.filter-btn:hover { color: var(--text-primary); background: var(--bg-hover); }
.filter-btn.active { color: var(--accent); }

.row-btn {
  font-size: 12px;
  padding: 2px 8px;
  border: 1px solid var(--border-dim);
  background: none;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: 3px;
  font-family: var(--font-sans);
}
.row-btn:hover { color: var(--text-primary); border-color: #333; }

.status-dot { font-size: 12px; }
.status-dot.待付款 { color: var(--text-secondary); }
.status-dot.已付款 { color: var(--accent); }
.status-dot.已发货 { color: #67C23A; }
.status-dot.已取消 { color: var(--text-tertiary); }

.mono { font-family: var(--font-mono); font-size: 12px; }
.dim { color: var(--text-tertiary); }

.detail-table { margin-bottom: 20px; }
.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--border-dim);
  font-size: 13px;
}
.detail-row span:first-child { color: var(--text-secondary); }
.detail-row span:last-child { color: var(--text-primary); }

.detail-sub {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 13px;
}
</style>
