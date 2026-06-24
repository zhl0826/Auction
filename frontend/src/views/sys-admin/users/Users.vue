<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUsers, updateUserStatus } from '@/api/sys'
import type { UserItem } from '@/types'

const list = ref<UserItem[]>([])
const loading = ref(false)
const total = ref(0)
const query = reactive({ keyword: '', status: '' as '' | 'active' | 'banned' })
const page = reactive({ current: 1, size: 10 })

async function fetch() {
  loading.value = true
  try {
    const data: any = await listUsers({ ...query, page: page.current, size: page.size })
    list.value = data.list
    total.value = data.total
  } finally { loading.value = false }
}

async function toggleBan(row: UserItem) {
  const next = row.status === 'banned' ? 'active' : 'banned'
  const action = next === 'banned' ? '封禁' : '解封'
  await ElMessageBox.confirm('确认 ' + action + ' 用户 ' + row.username + ' ？', '提示', { type: 'warning' })
  await updateUserStatus(row.id, next)
  row.status = next
  ElMessage.success('操作成功')
}

onMounted(fetch)
</script>

<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索账号/昵称" clearable style="width: 220px" @keyup.enter="fetch" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px" @change="fetch">
        <el-option label="正常" value="active" />
        <el-option label="封禁" value="banned" />
      </el-select>
      <el-button type="primary" @click="fetch">查询</el-button>
      <el-button @click="() => { query.keyword=''; query.status=''; page.current=1; fetch() }">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="账号" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="balance" label="余额" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : 'danger'">{{ row.status === 'active' ? '正常' : '封禁' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="160" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" :type="row.status === 'banned' ? 'success' : 'danger'" @click="toggleBan(row)">
            {{ row.status === 'banned' ? '解封' : '封禁' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page.current"
      v-model:page-size="page.size"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top: 16px; justify-content: flex-end"
      @current-change="fetch"
      @size-change="fetch"
    />
  </div>
</template>
