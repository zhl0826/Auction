<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { listOrders } from '@/api/auction'
import type { OrderItem } from '@/types'

const list = ref<OrderItem[]>([])
const loading = ref(false)
const query = reactive({ keyword: '', status: 'paid' })
const STATUS_OPTIONS = [
  { value: 'paid', label: '已成交' },
  { value: 'refunded', label: '已退款' }
]

async function fetch() {
  loading.value = true
  try {
    list.value = await listOrders(query.status)
  } catch (e: any) {
    ElMessage.error('加载失败: ' + (e?.message || e))
  } finally {
    loading.value = false
  }
}

const filtered = computed(() => {
  const k = query.keyword.trim().toLowerCase()
  if (!k) return list.value
  return list.value.filter((o) =>
    o.goodsTitle.toLowerCase().includes(k) ||
    o.buyerName.toLowerCase().includes(k) ||
    o.sellerName.toLowerCase().includes(k)
  )
})

const totalAmount = computed(() =>
  filtered.value.reduce((s, o) => s + Number(o.amount || 0), 0)
)
const totalFee = computed(() =>
  filtered.value.reduce((s, o) => s + Number(o.fee || 0), 0)
)

function afterSaleTag(s: string) {
  if (s === 'pending') return { type: 'warning', text: '售后审核中' }
  if (s === 'refunded') return { type: 'success', text: '已退款' }
  if (s === 'rejected') return { type: 'info', text: '已被驳回' }
  return null
}

onMounted(fetch)
</script>

<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索商品/买家/卖家" clearable style="width: 240px" @keyup.enter="fetch" />
      <el-select v-model="query.status" style="width: 140px" @change="fetch">
        <el-option v-for="t in STATUS_OPTIONS" :key="t.value" :label="t.label" :value="t.value" />
      </el-select>
      <el-button type="primary" @click="fetch">查询</el-button>
      <div class="summary">
        共 <b>{{ filtered.length }}</b> 单 ·
        总成交 <b>¥{{ totalAmount.toFixed(2) }}</b> ·
        平台手续费 <b>¥{{ totalFee.toFixed(2) }}</b>
      </div>
    </div>

    <el-table v-loading="loading" :data="filtered" border stripe>
      <el-table-column prop="id" label="订单ID" width="80" />
      <el-table-column label="商品" min-width="220">
        <template #default="{ row }">
          <div class="goods-cell">
            <el-image :src="row.goodsCover" fit="cover" class="cover" />
            <span class="title">{{ row.goodsTitle }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="amount" label="成交价" width="110">
        <template #default="{ row }">¥{{ Number(row.amount).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="fee" label="手续费" width="100">
        <template #default="{ row }">¥{{ Number(row.fee).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="buyerName" label="买家" width="100" />
      <el-table-column prop="sellerName" label="卖家" width="100" />
      <el-table-column label="状态" width="180">
        <template #default="{ row }">
          <el-tag :type="row.status === 'paid' ? 'success' : 'info'" disable-transitions>
            {{ row.status === 'paid' ? '已成交' : row.status === 'refunded' ? '已退款' : row.status }}
          </el-tag>
          <el-tag v-if="afterSaleTag(row.afterSaleStatus)" :type="afterSaleTag(row.afterSaleStatus)!.type" disable-transitions style="margin-left: 6px">
            {{ afterSaleTag(row.afterSaleStatus)!.text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="成交时间" width="160" />
    </el-table>
    <el-empty v-if="!loading && filtered.length === 0" description="暂无订单" />
  </div>
</template>

<style scoped>
.toolbar { display: flex; gap: 12px; align-items: center; margin-bottom: 12px; flex-wrap: wrap; }
.summary { margin-left: auto; color: #606266; }
.goods-cell { display: flex; align-items: center; gap: 8px; }
.cover { width: 48px; height: 48px; border-radius: 4px; background: #f0f0f0; flex-shrink: 0; }
.title { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
