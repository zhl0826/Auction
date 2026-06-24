<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuth } from '@/store/auth'
import { login as apiLogin } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuth()

const form = reactive({ username: 'sysadmin', password: '123456', role: 'sys_admin' as 'sys_admin' | 'auction_admin' })
const loading = ref(false)

const accounts = [
  { label: '系统管理员', username: 'sysadmin', role: 'sys_admin' as const },
  { label: '拍卖管理员', username: 'auction01', role: 'auction_admin' as const }
]

function pickAccount(a: typeof accounts[number]) {
  form.username = a.username
  form.role = a.role
  form.password = '123456'
}

async function submit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const data: any = await apiLogin({ username: form.username, password: form.password, role: form.role })
    auth.login({ token: data.token, role: data.role, username: data.username })
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || (data.role === 'sys_admin' ? '/sys/users' : '/auction/review')
    router.replace(redirect)
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-wrap">
    <div class="login-box">
      <h2 class="title">拍卖管理后台</h2>
      <p class="sub">Auction Admin Console</p>
      <el-form :model="form" label-width="0" size="large">
        <el-form-item>
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="系统管理员" value="sys_admin" />
            <el-option label="拍卖管理员" value="auction_admin" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.username" placeholder="账号" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" show-password placeholder="密码" />
        </el-form-item>
        <el-button type="primary" :loading="loading" style="width: 100%" @click="submit">登录</el-button>
      </el-form>
      <div class="quick">
        <span>快捷登录：</span>
        <el-link v-for="a in accounts" :key="a.username" type="primary" @click="pickAccount(a)">{{ a.label }}</el-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-wrap { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea, #764ba2); }
.login-box { width: 380px; background: #fff; border-radius: 12px; padding: 32px; box-shadow: 0 8px 24px rgba(0,0,0,.12); }
.title { margin: 0; text-align: center; }
.sub { text-align: center; color: #909399; margin: 4px 0 24px; }
.quick { margin-top: 16px; font-size: 13px; color: #606266; display: flex; gap: 12px; align-items: center; }
</style>
