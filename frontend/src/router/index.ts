import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from '@/store/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: () => import('@/views/login/Login.vue'), meta: { public: true } },
    {
      path: '/sys',
      component: () => import('@/layout/AdminLayout.vue'),
      meta: { role: 'sys_admin' },
      children: [
        { path: '', redirect: '/sys/users' },
        { path: 'users', name: 'sys-users', component: () => import('@/views/sys-admin/users/Users.vue'), meta: { title: '用户管理', icon: 'User' } },
        { path: 'auction-admins', name: 'sys-auction-admins', component: () => import('@/views/sys-admin/auction-admins/AuctionAdmins.vue'), meta: { title: '拍卖管理员', icon: 'Avatar' } },
        { path: 'config', name: 'sys-config', component: () => import('@/views/sys-admin/config/Config.vue'), meta: { title: '系统参数', icon: 'Setting' } }
      ]
    },
    {
      path: '/auction',
      component: () => import('@/layout/AdminLayout.vue'),
      meta: { role: 'auction_admin' },
      children: [
        { path: '', redirect: '/auction/review' },
        { path: 'review', name: 'auction-review', component: () => import('@/views/auction-admin/review/Review.vue'), meta: { title: '商品审核', icon: 'Document' } },
        { path: 'onsale', name: 'auction-onsale', component: () => import('@/views/auction-admin/onsale/OnSale.vue'), meta: { title: '已上架商品', icon: 'Goods' } },
        { path: 'history', name: 'auction-history', component: () => import('@/views/auction-admin/history/History.vue'), meta: { title: '竞拍历史', icon: 'Histogram', hidden: true } },
        { path: 'aftersale', name: 'auction-aftersale', component: () => import('@/views/auction-admin/aftersale/AfterSale.vue'), meta: { title: '售后处理', icon: 'Service' } },
    { path: 'orders', name: 'auction-orders', component: () => import('@/views/auction-admin/orders/Orders.vue'), meta: { title: '已成交商品', icon: 'List' } }
      ]
    },
    { path: '/', redirect: '/login' },
    { path: '/:pathMatch(.*)*', redirect: '/login' }
  ]
})

router.beforeEach((to) => {
  const auth = useAuth()
  if (to.meta.public) return true
  if (!auth.token.value) return { path: '/login', query: { redirect: to.fullPath } }
  if (to.meta.role && auth.role.value !== to.meta.role) {
    return auth.role.value === 'sys_admin' ? '/sys/users' : '/auction/review'
  }
  return true
})

export default router
