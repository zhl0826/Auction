<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listBids, listOnSale } from '@/api/auction'
import { getConfig as _gc } from '@/api/sys'  // 占位，真实里若需要商品基础信息可另写一个 detail 接口
import type { BidItem, GoodsItem } from '@/types'

const route = useRoute()
const router = useRouter()

const goodsId = computed(() => Number(route.query.goodsId) || 0)
const goodsInfo = ref<GoodsItem | null>(null)
const list = ref<BidItem[]>([])
const loading = ref(false)

async function loadGoods() {
  if (!goodsId.value) return
  // 没有专门的 detail 接口，从已上架列表里找（演示用）
  const all = await listOnSale({})
  goodsInfo.value = (all as GoodsItem[]).find(g => g.id === goodsId.value) || null
}

async function fetch() {
  if (!goodsId.value) return
  loading.value = true
  try { list.value = await listBids(goodsId.value) } finally { loading.value = false }
}

onMounted(async () => { await loadGoods(); await fetch() })
</script>

<template>
  <div class="page-card">
    <div class="toolbar">
      <el-button @click="router.replace('/auction/onsale')">返回已上架商品</el-button>
      <div v-if="goodsInfo" class="title">
        <span>商品：<b>{{ goodsInfo.title }}</b></span>
        <span style="margin-left: 16px; color: #909399">当前价：¥{{ goodsInfo.currentPrice }}</span>
      </div>
    </div>

    <el-empty v-if="!goodsId" description="缺少商品ID，无法查看" />
    <template v-else>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="id" label="出价ID" width="80" />
        <el-table-column prop="bidder" label="出价人" width="120" />
        <el-table-column prop="price" label="出价金额" width="120" />
        <el-table-column prop="createdAt" label="出价时间" width="180" />
        <el-table-column label="名次" width="100">
          <template #default="{ $index }">
            <el-tag :type="$index === 0 ? 'danger' : 'info'">{{ $index === 0 ? '最高' : '第' + ($index + 1) + '名' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && list.length === 0" description="该商品暂无出价记录" />
    </template>
  </div>
</template>

<style scoped>
.title { color: #303133; }
</style>
