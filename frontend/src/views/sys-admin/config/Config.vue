<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getConfig, saveConfig } from '@/api/sys'
import type { SysConfig } from '@/types'

const form = reactive<SysConfig>({ minIncrement: 10, feeRate: 0.02 })
const saving = ref(false)

async function load() {
  const data: any = await getConfig()
  if (data) Object.assign(form, data)
}

async function save() {
  if (form.minIncrement <= 0) { ElMessage.warning('最小加价幅度必须大于 0'); return }
  if (form.feeRate < 0 || form.feeRate > 1) { ElMessage.warning('手续费比例需在 0~1 之间'); return }
  saving.value = true
  try { await saveConfig(form); ElMessage.success('系统参数已保存') } finally { saving.value = false }
}

onMounted(load)
</script>

<template>
  <div class="page-card" style="max-width: 720px">
    <h3 style="margin-top: 0">系统参数配置</h3>
    <el-form label-width="120px">
      <el-form-item label="最小加价幅度">
        <el-input-number v-model="form.minIncrement" :min="1" :step="5" />
        <span style="margin-left: 12px; color: #909399">每次竞拍加价的最小单位</span>
      </el-form-item>
      <el-form-item label="手续费比例">
        <el-input-number v-model="form.feeRate" :min="0" :max="1" :step="0.005" :precision="3" />
        <span style="margin-left: 12px; color: #909399">成交时按比例收取，0~1（如 0.02 表示 2%）</span>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保存设置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
