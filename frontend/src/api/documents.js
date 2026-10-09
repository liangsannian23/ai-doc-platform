import client from './client'

// 文档 CRUD + 分页 + 详情
export const documentApi = {
  // 返回 MyBatis-Plus 分页对象：{ records, total, size, current, pages }
  page: (params) => client.get('/documents/page', { params }),
  detail: (id) => client.get(`/documents/${id}`),
  // create 返回新文档 id
  create: (payload) => client.post('/documents', payload),
  update: (id, payload) => client.put(`/documents/${id}`, payload),
  remove: (id) => client.delete(`/documents/${id}`),
}
