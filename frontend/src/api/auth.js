import client, { tokenStore } from './client'

// 认证相关：注册 / 登录 / 当前用户
export const authApi = {
  // 成功返回 JWT 字符串（已由拦截器拆出 data）
  login: async (username, password) => {
    const token = await client.post('/auth/login', { username, password })
    tokenStore.set(token)
    return token
  },
  register: (payload) => client.post('/auth/register', payload),
  me: () => client.get('/users/me'),
  logout: () => tokenStore.clear(),
}
