import axios from 'axios'

// 统一的后端请求实例：走相对路径 /api（开发由 Vite 代理到 8080，生产由 Nginx 反代）
const client = axios.create({
  baseURL: '/api',
  timeout: 60000,
  headers: { 'Content-Type': 'application/json' },
})

const TOKEN_KEY = 'aidoc_token'

export const tokenStore = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (t) => localStorage.setItem(TOKEN_KEY, t),
  clear: () => localStorage.removeItem(TOKEN_KEY),
}

// 请求拦截：自动带上 JWT
client.interceptors.request.use((config) => {
  const token = tokenStore.get()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 响应拦截：拆后端统一 Result{code,message,data}；401 清登录态并跳登录
client.interceptors.response.use(
  (resp) => {
    const body = resp.data
    // 后端业务码在 HTTP 200 里也可能非 200（如 403/400）
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) return body.data
      const err = new Error(body.message || '请求失败')
      err.code = body.code
      return Promise.reject(err)
    }
    return body
  },
  (error) => {
    const status = error.response?.status
    const body = error.response?.data
    if (status === 401) {
      tokenStore.clear()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
      return Promise.reject(new Error(body?.message || '未登录或Token已过期'))
    }
    if (status === 429) {
      return Promise.reject(new Error(body?.message || '操作过于频繁，请稍后再试'))
    }
    const msg = body?.message || error.message || '网络异常'
    const err = new Error(msg)
    err.code = status
    return Promise.reject(err)
  },
)

export default client
