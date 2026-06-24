import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuth } from '@/store/auth'

const service = axios.create({ baseURL: '/api', timeout: 10000 })

service.interceptors.request.use((config) => {
  const auth = useAuth()
  if (auth.token.value) config.headers.Authorization = 'Bearer ' + auth.token.value
  return config
})

service.interceptors.response.use(
  (resp) => {
    const body = resp.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) return body.data
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(body)
    }
    return body
  },
  (err) => {
    ElMessage.error(err?.response?.data?.message || err.message || '请求失败')
    return Promise.reject(err)
  }
)

export default service
