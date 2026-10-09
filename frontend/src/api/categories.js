import client from './client'

// 文档分类 CRUD
export const categoryApi = {
  list: () => client.get('/categories'),
  create: (name) => client.post('/categories', { name }),
  update: (id, name) => client.put(`/categories/${id}`, { name }),
  remove: (id) => client.delete(`/categories/${id}`),
}
