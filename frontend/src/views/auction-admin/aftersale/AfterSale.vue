<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAfterSales, refund, rejectAfterSale } from '@/api/auction'
import type { AfterSaleItem } from '@/types'

const list = ref<AfterSaleItem[]>([])
const loading = ref(false)

async function fetch() {
  loading.value = true
  try { list.value = await listAfterSales() } finally { loading.value = false }
}

async function doRefund(row: AfterSaleItem) {
  await ElMessageBox.confirm('确认同意订单 ' + row.orderId + ' 的退款？', '提示', { type: 'success' })
  await refund(row.id)
  row.status = 'refunded'
  ElMessage.success('已退款')
}
async function doReject(row: AfterSaleItem) {
  await ElMessageBox.confirm('确认驳回该售后申请？', '提示', { type: 'warning' })
  await rejectAfterSale(row.id)
  row.status = 'rejected'
  ElMessage.success('已驳回')
}

onMounted(fetch)
</script>

<template>
  <div class="page-card">
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="orderId" label="订单号" width="100" />
      <el-table-column prop="goodsTitle" label="商品" />
      <el-table-column prop="buyer" label="买家" width="100" />
      <el-table-column prop="seller" label="卖家" width="100" />
      <el-table-column prop="reason" label="售后原因" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'pending' ? 'warning' : row.status === 'refunded' ? 'success' : 'info'">
            {{ row.status === 'pending' ? '待处理' : row.status === 'refunded' ? '已退款' : '已驳回' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="申请时间" width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 'pending'">
            <el-button size="small" type="success" @click="doRefund(row)">同意退款</el-button>
            <el-button size="small" type="danger" @click="doReject(row)">驳回</el-button>
          </template>
          <span v-else style="color: #909399">已处理</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
