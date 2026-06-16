<template>
  <div class="page">
    <div class="page-header">
      <button class="add-btn" @click="openDialog">+ 新增客户</button>
    </div>

    <el-table :data="customers" v-loading="loading" size="small">
      <el-table-column prop="customer_id" label="编号" width="60">
        <template #default="{ row }">
          <span class="mono">{{ row.customer_id }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="customer_name" label="姓名" width="100" />
      <el-table-column prop="phone" label="手机号" width="140">
        <template #default="{ row }">
          <span class="mono">{{ row.phone }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="address" label="收货地址" min-width="200" />
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增客户" width="420px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="72px">
        <el-form-item label="姓名" prop="customer_name">
          <el-input v-model="form.customer_name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="11位手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="收货地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入收货地址" />
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
import { getCustomers, addCustomer } from '../api'
import { ElMessage } from 'element-plus'

const customers = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = ref({ customer_name: '', phone: '', address: '' })
const rules = {
  customer_name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, pattern: /^\d{11}$/, message: '11位手机号', trigger: 'blur' }],
  address: [{ required: true, message: '请输入收货地址', trigger: 'blur' }]
}

const fetchCustomers = async () => {
  loading.value = true
  try { customers.value = (await getCustomers()).data || [] } catch {} finally { loading.value = false }
}

const openDialog = () => { form.value = { customer_name: '', phone: '', address: '' }; dialogVisible.value = true }

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try { await addCustomer(form.value); ElMessage.success('已添加'); dialogVisible.value = false; fetchCustomers() }
  catch {} finally { submitting.value = false }
}

const resetForm = () => formRef.value?.resetFields()

onMounted(fetchCustomers)
</script>

<style scoped>
.page { padding-top: 24px; }
.page-header { margin-bottom: 16px; }

.add-btn {
  font-size: 13px;
  padding: 6px 14px;
  border: 1px solid var(--accent);
  background: none;
  color: var(--accent);
  cursor: pointer;
  border-radius: 4px;
  font-family: var(--font-sans);
  transition: all 0.15s;
}
.add-btn:hover { background: var(--accent-dim); }

.mono { font-family: var(--font-mono); font-size: 12px; }
</style>
