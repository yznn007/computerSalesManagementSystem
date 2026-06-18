<template>
  <div class="page">
    <div class="page-header">
      <div class="filters">
        <button
          v-for="s in ['', '待付款', '已付款', '已发货', '已取消', '已退货']"
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
        <template #default="{ row }"><span class="mono">{{ row.order_no }}</span></template>
      </el-table-column>
      <el-table-column prop="customer_name" label="客户" width="80" />
      <el-table-column label="金额" width="110">
        <template #default="{ row }"><span class="mono">¥{{ row.total_amount }}</span></template>
      </el-table-column>
      <el-table-column prop="order_date" label="时间" width="160">
        <template #default="{ row }"><span class="mono dim">{{ row.order_date }}</span></template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }"><span class="status-dot" :class="row.status">{{ row.status }}</span></template>
      </el-table-column>
      <el-table-column v-if="isStaff" label="操作" width="220">
        <template #default="{ row }">
          <button class="row-btn" @click="showDetail(row)">详情</button>
          <button v-if="row.status === '待付款'" class="row-btn" @click="openPay(row)">付款</button>
          <button v-if="row.status === '待付款'" class="row-btn warn" @click="openCancel(row)">取消</button>
          <button v-if="row.status === '已付款'" class="row-btn ok" @click="doShip(row)">发货</button>
          <button v-if="row.status === '已发货'" class="row-btn warn" @click="doReturn(row)">退货</button>
        </template>
      </el-table-column>
      <el-table-column v-else label="操作" width="100">
        <template #default="{ row }"><button class="row-btn" @click="showDetail(row)">详情</button></template>
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

    <el-dialog v-model="payVisible" title="标记付款" width="380px">
      <el-form label-width="72px">
        <el-form-item label="支付方式">
          <el-select v-model="payForm.payment_method" placeholder="请选择">
            <el-option v-for="m in ['微信','支付宝','银行卡','货到付款']" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payVisible = false" size="small">取消</el-button>
        <el-button type="primary" :loading="acting" @click="doPay" size="small">确认付款</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="cancelVisible" title="取消订单" width="380px">
      <el-form label-width="72px">
        <el-form-item label="取消原因">
          <el-input v-model="cancelForm.cancel_reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelVisible = false" size="small">取消</el-button>
        <el-button type="danger" :loading="acting" @click="doCancel" size="small">确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrders, getOrderDetail, updateOrderStatus } from '../api'

const isStaff = computed(() => localStorage.getItem('role') === 'staff')
const statusFilter = ref('')
const orders = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const detailOrder = ref(null)
const detailItems = ref([])
const acting = ref(false)

const payVisible = ref(false)
const payForm = ref({ id: null, payment_method: '微信' })
const cancelVisible = ref(false)
const cancelForm = ref({ id: null, cancel_reason: '' })

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

const openPay = (row) => { payForm.value = { id: row.order_id, payment_method: '微信' }; payVisible.value = true }
const openCancel = (row) => { cancelForm.value = { id: row.order_id, cancel_reason: '' }; cancelVisible.value = true }

const doPay = async () => {
  acting.value = true
  try { await updateOrderStatus(payForm.value.id, { action: 'pay', payment_method: payForm.value.payment_method }); ElMessage.success('已付款'); payVisible.value = false; fetchOrders() }
  catch {} finally { acting.value = false }
}
const doCancel = async () => {
  if (!cancelForm.value.cancel_reason) return ElMessage.warning('请填写取消原因')
  acting.value = true
  try { await updateOrderStatus(cancelForm.value.id, { action: 'cancel', cancel_reason: cancelForm.value.cancel_reason }); ElMessage.success('已取消'); cancelVisible.value = false; fetchOrders() }
  catch {} finally { acting.value = false }
}
const doShip = async (row) => {
  await ElMessageBox.confirm('确认发货？', '提示').catch(() => { throw 'cancel' })
  try { await updateOrderStatus(row.order_id, { action: 'ship' }); ElMessage.success('已发货'); fetchOrders() } catch {}
}
const doReturn = async (row) => {
  await ElMessageBox.confirm('确认退货？库存将回补。', '提示').catch(() => { throw 'cancel' })
  try { await updateOrderStatus(row.order_id, { action: 'return' }); ElMessage.success('已退货'); fetchOrders() } catch {}
}

onMounted(fetchOrders)
</script>

<style scoped>
.page { padding-top: 24px; }
.page-header { margin-bottom: 16px; }
.filters { display: flex; gap: 4px; }
.filter-btn { font-size: 12px; padding: 4px 10px; border: none; background: none; color: var(--text-secondary); cursor: pointer; border-radius: 3px; font-family: var(--font-sans); transition: all 0.15s; }
.filter-btn:hover { color: var(--text-primary); background: var(--bg-hover); }
.filter-btn.active { color: #e0e0e0; }
.row-btn { font-size: 12px; padding: 2px 8px; border: 1px solid var(--border-dim); background: none; color: var(--text-secondary); cursor: pointer; border-radius: 3px; font-family: var(--font-sans); margin-right: 4px; }
.row-btn:hover { color: var(--text-primary); border-color: #333; }
.row-btn.ok { color: #67C23A; border-color: #67C23A66; }
.row-btn.warn { color: #f56c6c; border-color: #f56c6c66; }
.status-dot { font-size: 12px; }
.status-dot.待付款 { color: var(--text-secondary); }
.status-dot.已付款 { color: #e0e0e0; }
.status-dot.已发货 { color: #67C23A; }
.status-dot.已取消 { color: var(--text-tertiary); }
.status-dot.已退货 { color: #e6a23c; }
.mono { font-family: var(--font-mono); font-size: 12px; }
.dim { color: var(--text-tertiary); }
.detail-table { margin-bottom: 20px; }
.detail-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid var(--border-dim); font-size: 13px; }
.detail-row span:first-child { color: var(--text-secondary); }
.detail-row span:last-child { color: var(--text-primary); }
.detail-sub { display: flex; justify-content: space-between; padding: 6px 0; font-size: 13px; }
</style>
