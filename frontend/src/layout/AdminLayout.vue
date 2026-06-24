<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuth } from '@/store/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuth()

const menus = computed(() => {
  if (auth.role.value === 'sys_admin') {
    return [
      { path: '/sys/users', title: '用户管理', icon: 'User' },
      { path: '/sys/auction-admins', title: '拍卖管理员', icon: 'Avatar' },
      { path: '/sys/config', title: '系统参数', icon: 'Setting' }
    ]
  }
  return [
    { path: '/auction/review', title: '商品审核', icon: 'Document' },
    { path: '/auction/onsale', title: '已上架商品', icon: 'Goods' },
      // 竞拍历史改为从已上架商品点入，不再显示在侧边栏
    { path: '/auction/aftersale', title: '售后处理', icon: 'Service' },
    { path: '/auction/orders', title: '已成交商品', icon: 'List' }
  ]
})

const roleLabel = computed(() => (auth.role.value === 'sys_admin' ? '系统管理员' : '拍卖管理员'))

function logout() {
  auth.logout()
  router.replace('/login')
}
</script>

<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">拍卖管理后台</div>
      <el-menu :default-active="route.path" router class="menu" background-color="#001529" text-color="#cfd3dc" active-text-color="#409eff">
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="crumb">当前角色：<el-tag size="small" :type="auth.role.value === 'sys_admin' ? 'danger' : 'success'">{{ roleLabel }}</el-tag></div>
        <el-dropdown @command="(c: string) => c === 'logout' && logout()">
          <span class="user">
            <el-avatar :size="28" style="margin-right: 8px">{{ auth.username.value.slice(0, 1) }}</el-avatar>
            {{ auth.username }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout { height: 100vh; }
.aside { background: #001529; color: #fff; }
.logo { height: 56px; line-height: 56px; text-align: center; font-weight: 600; color: #fff; border-bottom: 1px solid #1f2d3d; }
.menu { border-right: none; }
.header { background: #fff; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #ebeef5; }
.crumb { color: #606266; }
.user { display: inline-flex; align-items: center; cursor: pointer; color: #303133; }
.main { background: #f5f7fa; }
</style>
