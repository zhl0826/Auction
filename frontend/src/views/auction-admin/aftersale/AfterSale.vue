<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAfterSales, refund, rejectAfterSale } from '@/api/auction'
import type { AfterSaleItem } from '@/types'

const list = ref<AfterSaleItem[]>([])
const loading = ref(false)
const processing = ref<Record<number, boolean>>({})

function statusLabel(s: string) {
  if (s === 'pending') return '待处理'
  if (s === 'refunded') return '已退款'
  if (s === 'rejected') return '已驳回'
  return s
}
function statusType(s: string): 'warning' | 'success' | 'info' | 'danger' {
  if (s === 'pending') return 'warning'
  if (s === 'refunded') return 'success'
  if (s === 'rejected') return 'danger'
  return 'info'
}

async function fetch() {
  loading.value = true
  try {
    list.value = await listAfterSales()
  } catch (e: any) {
    ElMessage.error('加载失败: ' + (e?.message || e))
  } finally {
    loading.value = false
  }
}

async function doRefund(row: AfterSaleItem) {
  if (processing.value[row.id]) return
  try {
    await ElMessageBox.confirm(
      '确认同意订单 ' + row.orderId + ' 的退款？',
      '提示',
      { type: 'success' }
    )
  } catch {
    return
  }
  processing.value[row.id] = true
  try {
    await refund(row.id)
    row.status = 'refunded'
    ElMessage.success('已退款')
  } catch (e: any) {
    ElMessage.error('退款失败: ' + (e?.message || e))
  } finally {
    processing.value[row.id] = false
  }
}

async function doReject(row: AfterSaleItem) {
  if (processing.value[row.id]) return
  try {
    await ElMessageBox.confirm(
      '确认驳回该售后申请？',
      '提示',
      { type: 'warning' }
    )
  } catch {
    return
  }
  processing.value[row.id] = true
  try {
    await rejectAfterSale(row.id)
    row.status = 'rejected'
    ElMessage.success('已驳回')
  } catch (e: any) {
    ElMessage.error('驳回失败: ' + (e?.message || e))
  } finally {
    processing.value[row.id] = false
  }
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
          <el-tag :type="statusType(row.status)" disable-transitions>
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="申请时间" width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 'pending'">
            <el-button
              size="small"
              type="success"
              :loading="processing[row.id]"
              @click="doRefund(row)"
            >同意退款</el-button>
            <el-button
              size="small"
              type="danger"
              :loading="processing[row.id]"
              @click="doReject(row)"
            >驳回</el-button>
          </template>
          <span v-else style="color: #909399">已处理</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
