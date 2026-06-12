import { ref } from 'vue'

const token = ref<string>(localStorage.getItem('token') || '')
const role = ref<string>(localStorage.getItem('role') || '')
const username = ref<string>(localStorage.getItem('username') || '')

export function useAuth() {
  function login(payload: { token: string; role: string; username: string }) {
    token.value = payload.token
    role.value = payload.role
    username.value = payload.username
    localStorage.setItem('token', payload.token)
    localStorage.setItem('role', payload.role)
    localStorage.setItem('username', payload.username)
  }
  function logout() {
    token.value = ''
    role.value = ''
    username.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    localStorage.removeItem('username')
  }
  return { token, role, username, login, logout }
}
