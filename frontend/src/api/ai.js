import client from './client'

// AI 能力：摘要 / 纠错 / 润色 / 问答
export const aiApi = {
  // 返回摘要字符串
  summary: (content) => client.post('/ai/summary', { content }),
  // 返回 { errors: [{wrong,right,reason}], correctedText }
  proofread: (content) => client.post('/ai/proofread', { content }),
  // style：专业 / 幽默 / 简洁
  polish: (content, style) => client.post('/ai/polish', { content, style }),
  // 基于指定文档问答
  qa: (documentId, question) => client.post('/ai/qa', { documentId, question }),
}
