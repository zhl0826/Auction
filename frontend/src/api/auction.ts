import request from '@/utils/request'
import type { GoodsItem, BidItem, AfterSaleItem, OrderItem } from '@/types'

export const listPending = (params: { keyword?: string; type?: string }) =>
  request.get<GoodsItem[], any>('/auction/goods/pending', { params })

export const approveGoods = (id: number) => request.put(`/auction/goods/${id}/approve`)

export const rejectGoods = (id: number, reason: string) =>
  request.put(`/auction/goods/${id}/reject`, { reason })

export const listOnSale = (params: { keyword?: string; type?: string }) =>
  request.get<GoodsItem[], any>('/auction/goods/onsale', { params })

export const offShelf = (id: number) => request.put(`/auction/goods/${id}/off-shelf`)

export const listBids = (goodsId: number) =>
  request.get<BidItem[], any>(`/auction/goods/${goodsId}/bids`)

export const listAfterSales = () => request.get<AfterSaleItem[], any>('/auction/aftersales')
export const refund = (id: number) => request.put(`/auction/aftersales/${id}/refund`)
export const rejectAfterSale = (id: number) => request.put(`/auction/aftersales/${id}/reject`)


export const listOrders = (status = 'paid') =>
  request.get<OrderItem[], any>('/auction/orders', { params: { status } })
