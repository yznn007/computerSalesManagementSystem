<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2 style="margin: 0">客户信息</h2>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增客户</el-button>
    </div>

    <el-table :data="customers" border stripe v-loading="loading">
      <el-table-column prop="customer_id" label="编号" width="80" />
      <el-table-column prop="customer_name" label="姓名" width="120" />
      <el-table-column prop="phone" label="手机号" width="150" />
      <el-table-column prop="address" label="收货地址" min-width="200" />
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑客户' : '新增客户'" width="500px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
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
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { getCustomers, addCustomer } from '../api'
import { ElMessage } from 'element-plus'

const customers = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = ref({ customer_name: '', phone: '', address: '' })
const rules = {
  customer_name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^\d{11}$/, message: '手机号为11位数字', trigger: 'blur' }
  ],
  address: [{ required: true, message: '请输入收货地址', trigger: 'blur' }]
}

const fetchCustomers = async () => {
  loading.value = true
  try {
    const res = await getCustomers()
    customers.value = res.data || []
  } finally { loading.value = false }
}

const openDialog = () => {
  isEdit.value = false
  form.value = { customer_name: '', phone: '', address: '' }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await addCustomer(form.value)
    ElMessage.success('添加成功')
    dialogVisible.value = false
    fetchCustomers()
  } finally { submitting.value = false }
}

const resetForm = () => formRef.value?.resetFields()

onMounted(fetchCustomers)
</script>
