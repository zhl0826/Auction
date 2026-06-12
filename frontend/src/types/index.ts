export type Role = 'sys_admin' | 'auction_admin'

export interface UserItem {
  id: number
  username: string
  nickname: string
  role: 'buyer' | 'seller'
  balance: number
  status: 'active' | 'banned'
  createdAt: string
}

export interface AuctionAdminItem {
  id: number
  username: string
  nickname: string
  status: 'active' | 'disabled'
  createdAt: string
}

export interface GoodsItem {
  id: number
  title: string
  type: string
  seller: string
  startPrice: number
  currentPrice: number
  minIncrement: number
  cover: string
  status: 'pending' | 'on_sale' | 'sold' | 'off_shelf' | 'rejected'
  rejectReason?: string
  endAt: string
}

export interface BidItem {
  id: number
  goodsId: number
  goodsTitle: string
  bidder: string
  price: number
  createdAt: string
}

export interface AfterSaleItem {
  id: number
  orderId: number
  goodsTitle: string
  buyer: string
  seller: string
  reason: string
  status: 'pending' | 'refunded' | 'rejected'
  createdAt: string
}

export interface SysConfig {
  minIncrement: number
  feeRate: number // 0~1
}
