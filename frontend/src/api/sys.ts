import request from '@/utils/request'
import type { UserItem, AuctionAdminItem, SysConfig } from '@/types'

export const listUsers = (params: { keyword?: string; role?: string; status?: string; page?: number; size?: number }) =>
  request.get<{ list: UserItem[]; total: number }, any>('/sys/users', { params })

export const updateUserStatus = (id: number, status: 'active' | 'banned') =>
  request.put(`/sys/users/${id}/status`, { status })

export const listAuctionAdmins = (keyword?: string) =>
  request.get<AuctionAdminItem[], any>('/sys/auction-admins', { params: { keyword } })

export const addAuctionAdmin = (data: { username: string; password: string; nickname?: string }) =>
  request.post('/sys/auction-admins', data)

export const updateAdminStatus = (id: number, status: 'active' | 'disabled') =>
  request.put(`/sys/auction-admins/${id}/status`, { status })

export const deleteAuctionAdmin = (id: number) =>
  request.delete(`/sys/auction-admins/${id}`)

export const getConfig = () => request.get<SysConfig, any>('/sys/config')
export const saveConfig = (data: SysConfig) => request.put('/sys/config', data)
