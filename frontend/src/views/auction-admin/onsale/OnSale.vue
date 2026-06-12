<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listOnSale, offShelf } from '@/api/auction'
import type { GoodsItem } from '@/types'

const router = useRouter()
const list = ref<GoodsItem[]>([])
const loading = ref(false)
const query = reactive({ keyword: '', type: '' })
const TYPE_OPTIONS = ['数码', '服饰', '古董', '书籍', '其它']

async function fetch() {
  loading.value = true
  try { list.value = await listOnSale({ keyword: query.keyword, type: query.type }) } finally { loading.value = false }
}

async function off(row: GoodsItem) {
  await ElMessageBox.confirm('确认下架『' + row.title + '』？', '提示', { type: 'warning' })
  await offShelf(row.id)
  ElMessage.success('已下架')
  fetch()
}

function viewBids(row: GoodsItem) {
  router.push({ path: '/auction/history', query: { goodsId: row.id } })
}

onMounted(fetch)
</script>

<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索商品名" clearable style="width: 240px" @keyup.enter="fetch" />
      <el-select v-model="query.type" placeholder="类型" clearable style="width: 140px" @change="fetch">
        <el-option v-for="t in TYPE_OPTIONS" :key="t" :label="t" :value="t" />
      </el-select>
      <el-button type="primary" @click="fetch">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="图片" width="100">
        <template #default="{ row }"><el-image :src="row.cover" fit="cover" style="width: 60px; height: 60px; border-radius: 4px" /></template>
      </el-table-column>
      <el-table-column prop="title" label="商品名" />
      <el-table-column prop="type" label="类型" width="80" />
      <el-table-column prop="sellerName" label="卖家" width="100" />
      <el-table-column prop="currentPrice" label="当前价" width="100" />
      <el-table-column prop="endAt" label="结束时间" width="160" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewBids(row)">查看出价</el-button>
          <el-button size="small" type="warning" @click="off(row)">下架</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && list.length === 0" description="暂无已上架商品" />
  </div>
</template>
