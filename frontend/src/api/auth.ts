import request from '@/utils/request'

export const login = (data: { username: string; password: string; role: 'sys_admin' | 'auction_admin' }) =>
  request.post('/auth/login', data)
