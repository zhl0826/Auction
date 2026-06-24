<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listPending, approveGoods, rejectGoods } from '@/api/auction'
import type { GoodsItem } from '@/types'

const list = ref<GoodsItem[]>([])
const loading = ref(false)
const query = reactive({ keyword: '', type: '' })
const TYPE_OPTIONS = ['数码', '服饰', '古董', '书籍', '其它']

async function fetch() {
  loading.value = true
  try { list.value = await listPending({ keyword: query.keyword, type: query.type }) } finally { loading.value = false }
}

async function approve(row: GoodsItem) {
  await ElMessageBox.confirm('确认通过『' + row.title + '』的审核？', '提示', { type: 'success' })
  await approveGoods(row.id)
  ElMessage.success('已通过')
  fetch()
}

const rejectDialog = ref(false)
const rejectTarget = ref<GoodsItem | null>(null)
const rejectReason = ref('')

function openReject(row: GoodsItem) { rejectTarget.value = row; rejectReason.value = ''; rejectDialog.value = true }
async function confirmReject() {
  if (!rejectReason.value.trim()) { ElMessage.warning('请填写拒绝原因'); return }
  if (!rejectTarget.value) return
  await rejectGoods(rejectTarget.value.id, rejectReason.value)
  rejectDialog.value = false
  ElMessage.success('已拒绝')
  fetch()
}

onMounted(fetch)
</script>

<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索商品名/卖家" clearable style="width: 240px" @keyup.enter="fetch" />
      <el-select v-model="query.type" placeholder="类型" clearable style="width: 140px" @change="fetch">
        <el-option v-for="t in TYPE_OPTIONS" :key="t" :label="t" :value="t" />
      </el-select>
      <el-button type="primary" @click="fetch">查询</el-button>
      <el-button @click="() => { query.keyword=''; query.type=''; fetch() }">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="图片" width="100">
        <template #default="{ row }"><el-image :src="row.cover" :preview-src-list="[row.cover]" fit="cover" style="width: 60px; height: 60px; border-radius: 4px" /></template>
      </el-table-column>
      <el-table-column prop="title" label="商品名" />
      <el-table-column prop="type" label="类型" width="80" />
      <el-table-column prop="sellerName" label="卖家" width="100" />
      <el-table-column prop="startPrice" label="起拍价" width="100" />
      <el-table-column prop="endAt" label="结束时间" width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="approve(row)">通过</el-button>
          <el-button size="small" type="danger" @click="openReject(row)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && list.length === 0" description="暂无待审核商品" />

    <el-dialog v-model="rejectDialog" title="拒绝上架" width="420px">
      <p>商品：<b>{{ rejectTarget?.title }}</b></p>
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请填写拒绝原因" />
      <template #footer>
        <el-button @click="rejectDialog = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>
