<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAuctionAdmins, addAuctionAdmin, updateAdminStatus, deleteAuctionAdmin } from '@/api/sys'
import type { AuctionAdminItem } from '@/types'

const list = ref<AuctionAdminItem[]>([])
const loading = ref(false)
const query = reactive({ keyword: '' })

async function fetch() {
  loading.value = true
  try { list.value = await listAuctionAdmins(query.keyword) } finally { loading.value = false }
}

const dialogVisible = ref(false)
const form = reactive({ username: '', nickname: '', password: '' })

function openAdd() { form.username = ''; form.nickname = ''; form.password = ''; dialogVisible.value = true }

async function save() {
  if (!form.username || !form.password) { ElMessage.warning('请填写账号和初始密码'); return }
  await addAuctionAdmin({ username: form.username, nickname: form.nickname, password: form.password })
  ElMessage.success('新增成功')
  dialogVisible.value = false
  fetch()
}

async function remove(row: AuctionAdminItem) {
  await ElMessageBox.confirm('确认删除拍卖管理员 ' + row.username + ' ？', '提示', { type: 'warning' })
  await deleteAuctionAdmin(row.id)
  ElMessage.success('删除成功')
  fetch()
}

async function toggle(row: AuctionAdminItem) {
  const next = row.status === 'active' ? 'disabled' : 'active'
  await updateAdminStatus(row.id, next)
  row.status = next
  ElMessage.success('状态已更新')
}

onMounted(fetch)
</script>

<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索账号/昵称" clearable style="width: 220px" @keyup.enter="fetch" />
      <el-button type="primary" @click="fetch">查询</el-button>
      <div class="grow"></div>
      <el-button type="success" @click="openAdd">新增拍卖管理员</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="账号" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : 'info'">{{ row.status === 'active' ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="toggle(row)">{{ row.status === 'active' ? '停用' : '启用' }}</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增拍卖管理员" width="420px">
      <el-form label-width="90px">
        <el-form-item label="账号" required><el-input v-model="form.username" placeholder="登录账号" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" placeholder="可选" /></el-form-item>
        <el-form-item label="初始密码" required><el-input v-model="form.password" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
