<template>
  <div class="login-page">
    <div class="login-box">
      <pre class="logo">
  ██████╗ ███████╗
 ██╔════╝ ██╔════╝
 ██║      ███████╗
 ██║      ╚════██║
 ╚██████╗ ███████║
  ╚═════╝ ╚══════╝</pre>

      <div class="tabs">
        <button class="tab" :class="{ active: mode === 'customer' }" @click="mode = 'customer'">客户登录</button>
        <button class="tab" :class="{ active: mode === 'staff' }" @click="mode = 'staff'">销售员登录</button>
      </div>

      <form @submit.prevent="handleLogin">
        <input v-model="form.account" class="field" :placeholder="mode === 'customer' ? '手机号' : '用户名'" />
        <input v-model="form.password" type="password" class="field" placeholder="密码" />
        <button class="submit" :disabled="loading" type="submit">{{ loading ? '...' : '登录' }}</button>
      </form>

      <div class="foot">
        <a v-if="mode === 'customer'" href="#" @click.prevent="showRegister = true">没有账号？去注册</a>
        <a v-else href="#" @click.prevent="mode = 'customer'">← 返回客户登录</a>
      </div>
    </div>

    <el-dialog v-model="showRegister" title="客户注册" width="420px">
      <el-form :model="reg" label-width="72px">
        <el-form-item label="姓名"><el-input v-model="reg.customer_name" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="reg.phone" maxlength="11" /></el-form-item>
        <el-form-item label="收货地址"><el-input v-model="reg.address" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="reg.password" type="password" show-password placeholder="默认 123456" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegister = false" size="small">取消</el-button>
        <el-button type="primary" :loading="regLoading" @click="handleRegister" size="small">注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { loginCustomer, loginStaff, register } from '../api'

const router = useRouter()
const mode = ref('customer')
const form = ref({ account: '', password: '' })
const loading = ref(false)
const showRegister = ref(false)
const reg = ref({ customer_name: '', phone: '', address: '', password: '' })
const regLoading = ref(false)

const handleLogin = async () => {
  if (!form.value.account || !form.value.password) return ElMessage.warning('请填写完整')
  loading.value = true
  try {
    const fn = mode.value === 'customer' ? loginCustomer : loginStaff
    const { data } = await fn(form.value)
    localStorage.setItem('token', data.token)
    localStorage.setItem('role', data.role)
    localStorage.setItem('name', data.name)
    localStorage.setItem('uid', data.id)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch {} finally { loading.value = false }
}

const handleRegister = async () => {
  const r = reg.value
  if (!r.customer_name || !r.phone || !r.address) return ElMessage.warning('请填写完整')
  regLoading.value = true
  try { await register(r); ElMessage.success('注册成功，请登录'); showRegister.value = false; reg.value = { customer_name: '', phone: '', address: '', password: '' } }
  catch {} finally { regLoading.value = false }
}
</script>

<style scoped>
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; flex-direction: column; }
.login-box { width: 360px; }
.logo { font-family: var(--font-mono); font-size: 18px; line-height: 1.25; margin: 0 0 32px; text-align: center; background: linear-gradient(180deg, #d0d0d0 0%, #8a8a8a 40%, #5a5a5a 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
.tabs { display: flex; gap: 2px; margin-bottom: 24px; }
.tab { flex: 1; font-size: 13px; padding: 8px 0; border: 1px solid #333; background: none; color: #5a5a5a; cursor: pointer; font-family: var(--font-sans); transition: all 0.15s; }
.tab.active { color: #e0e0e0; border-color: #555; }
form { display: flex; flex-direction: column; gap: 16px; }
.field { width: 100%; height: 38px; padding: 0 12px; background: none; border: 1px solid #333; color: #d0d0d0; font-size: 14px; font-family: var(--font-sans); outline: none; transition: border-color 0.15s; }
.field:focus { border-color: #666; }
.submit { width: 100%; height: 38px; background: #e0e0e0; border: none; color: #0a0a0a; font-size: 14px; font-weight: 600; cursor: pointer; font-family: var(--font-sans); transition: opacity 0.15s; }
.submit:hover:not(:disabled) { opacity: 0.9; }
.submit:disabled { opacity: 0.4; cursor: default; }
.foot { margin-top: 20px; text-align: center; }
.foot a { font-size: 12px; color: #6a6a6a; text-decoration: none; }
.foot a:hover { color: #e0e0e0; }
</style>
