import type {
  UserItem, AuctionAdminItem, GoodsItem, BidItem, AfterSaleItem, SysConfig
} from '@/types'

// 简单内存 mock
export const users: UserItem[] = [
  { id: 1, username: 'buyer01', nickname: '小明', role: 'buyer', balance: 5000, status: 'active', createdAt: '2025-01-10 10:20' },
  { id: 2, username: 'seller01', nickname: '老王', role: 'seller', balance: 1200, status: 'active', createdAt: '2025-02-01 09:00' },
  { id: 3, username: 'buyer02', nickname: '小红', role: 'buyer', balance: 0, status: 'banned', createdAt: '2025-02-12 14:00' },
  { id: 4, username: 'seller02', nickname: '阿强', role: 'seller', balance: 3000, status: 'active', createdAt: '2025-03-05 18:30' }
]

export const auctionAdmins: AuctionAdminItem[] = [
  { id: 1, username: 'auction01', nickname: '审核员-张', status: 'active', createdAt: '2024-12-01 09:00' },
  { id: 2, username: 'auction02', nickname: '审核员-李', status: 'active', createdAt: '2025-01-15 09:00' }
]

export const goods: GoodsItem[] = [
  { id: 101, title: '索尼 A7M4 相机', type: '数码', seller: '老王', startPrice: 8000, currentPrice: 8650, minIncrement: 50, cover: 'https://picsum.photos/seed/g1/200/140', status: 'pending', endAt: '2026-06-20 20:00' },
  { id: 102, title: '宋代茶盏', type: '古董', seller: '阿强', startPrice: 1500, currentPrice: 1500, minIncrement: 100, cover: 'https://picsum.photos/seed/g2/200/140', status: 'pending', endAt: '2026-06-18 20:00' },
  { id: 103, title: 'Nike Air Max', type: '服饰', seller: '老王', startPrice: 300, currentPrice: 480, minIncrement: 20, cover: 'https://picsum.photos/seed/g3/200/140', status: 'on_sale', endAt: '2026-06-15 12:00' },
  { id: 104, title: 'iPhone 15 Pro', type: '数码', seller: '阿强', startPrice: 6000, currentPrice: 7100, minIncrement: 50, cover: 'https://picsum.photos/seed/g4/200/140', status: 'on_sale', endAt: '2026-06-13 21:00' },
  { id: 105, title: '违规商品测试', type: '其它', seller: '老王', startPrice: 50, currentPrice: 50, minIncrement: 10, cover: 'https://picsum.photos/seed/g5/200/140', status: 'rejected', rejectReason: '信息不全', endAt: '-' }
]

export const bids: BidItem[] = [
  { id: 1, goodsId: 103, goodsTitle: 'Nike Air Max', bidder: '小明', price: 380, createdAt: '2026-06-10 10:00' },
  { id: 2, goodsId: 103, goodsTitle: 'Nike Air Max', bidder: '小红', price: 480, createdAt: '2026-06-10 11:30' },
  { id: 3, goodsId: 104, goodsTitle: 'iPhone 15 Pro', bidder: '小明', price: 6800, createdAt: '2026-06-10 12:10' },
  { id: 4, goodsId: 104, goodsTitle: 'iPhone 15 Pro', bidder: '阿强', price: 7100, createdAt: '2026-06-10 13:05' }
]

export const afterSales: AfterSaleItem[] = [
  { id: 1, orderId: 9001, goodsTitle: 'Nike Air Max', buyer: '小红', seller: '老王', reason: '尺码不对', status: 'pending', createdAt: '2026-06-10 15:00' }
]

export const config: SysConfig = { minIncrement: 10, feeRate: 0.02 }
