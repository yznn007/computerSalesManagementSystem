<template>
  <div class="page">
    <div class="page-header">
      <button class="add-btn" @click="openDialog()">+ 新增客户</button>
    </div>


    <div class="table-head">
      <span class="th">姓名</span>
      <span class="th">手机号</span>
      <span class="th">收货地址</span>
      <span class="th th-ops">操作</span>
    </div>

    <div v-loading="loading">
      <div v-for="row in customers" :key="row.customer_id" class="row">
        <span class="cell">{{ row.customer_name }}</span>
        <span class="mono">{{ row.phone }}</span>
        <span class="cell addr">{{ row.address }}</span>
        <span class="row-ops">
          <button class="row-btn" @click="openDialog(row)">编辑</button>
          <button class="row-btn warn" @click="remove(row)">删除</button>
        </span>
      </div>
      <div v-if="!loading && customers.length === 0" class="empty">暂无客户</div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑客户' : '新增客户'" width="420px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="84px">
        <el-form-item label="姓名" prop="customer_name"><el-input v-model="form.customer_name" placeholder="请输入姓名" /></el-form-item>
        <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" placeholder="11位手机号" maxlength="11" /></el-form-item>
        <el-form-item label="收货地址" prop="address"><el-input v-model="form.address" placeholder="请输入收货地址" /></el-form-item>
        <el-form-item v-if="!editing" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="默认 123456" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" size="small">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit" size="small">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCustomers, addCustomer, updateCustomer, deleteCustomer } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const customers = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editing = ref(null)

const form = ref({ customer_name: '', phone: '', address: '', password: '' })
const rules = {
  customer_name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, pattern: /^\d{11}$/, message: '11位手机号', trigger: 'blur' }],
  address: [{ required: true, message: '请输入收货地址', trigger: 'blur' }]
}

const fetchCustomers = async () => {
  loading.value = true
  try { customers.value = (await getCustomers()).data || [] } catch {} finally { loading.value = false }
}

const openDialog = (row) => {
  editing.value = row ? row.customer_id : null
  form.value = row ? { customer_name: row.customer_name, phone: row.phone, address: row.address }     : { customer_name: '', phone: '', address: '', password: '' }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (editing.value) { await updateCustomer(editing.value, form.value); ElMessage.success('已更新') }
    else { await addCustomer(form.value); ElMessage.success('已添加') }
    dialogVisible.value = false; fetchCustomers()
  } catch {} finally { submitting.value = false }
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除客户 ${row.customer_name}？`, '提示', { type: 'warning' }).catch(() => { throw 'cancel' })
  try { await deleteCustomer(row.customer_id); ElMessage.success('已删除'); fetchCustomers() } catch {}
}

const resetForm = () => formRef.value?.resetFields()

onMounted(fetchCustomers)
</script>

<style scoped>
.page { padding-top: 24px; }
.page-header { display: flex; justify-content: flex-end; margin-bottom: 16px; }
.add-btn { font-size: 13px; padding: 6px 14px; border: none; background: none; color: var(--text-tertiary); cursor: pointer; border-radius: 4px; font-family: var(--font-sans); transition: color 0.15s; }
.add-btn:hover { color: #e0e0e0; }

.table-head { display: grid; grid-template-columns: 100px 140px 1fr 140px; align-items: center; padding: 8px 8px; border-bottom: 1px solid #1a1a1a; }
.th { font-size: 12px; font-weight: 500; text-transform: uppercase; letter-spacing: 0.5px; color: var(--text-tertiary); }
.th-ops { text-align: right; }

.row { display: grid; grid-template-columns: 100px 140px 1fr 140px; align-items: center; padding: 12px 8px; transition: background 0.1s; border-bottom: 1px solid #1a1a1a; }
.row:hover { background: var(--bg-hover); }
.cell { font-size: 13px; color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.addr { color: var(--text-secondary); }
.row-ops { display: flex; gap: 4px; justify-content: flex-end; }

.row-btn { font-size: 12px; padding: 2px 8px; border: none; background: none; color: var(--text-secondary); cursor: pointer; border-radius: 3px; font-family: var(--font-sans); transition: color 0.15s; }
.row-btn:hover { color: var(--text-primary); }
.row-btn.warn { color: #f56c6c; }

.mono { font-family: var(--font-mono); font-size: 13px; color: var(--text-primary); }
.empty { text-align: center; color: var(--text-tertiary); padding: 80px 0; font-size: 13px; }
</style>
