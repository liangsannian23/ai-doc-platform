import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { documentApi } from '../api/documents'
import { aiApi } from '../api/ai'

export default function DocumentDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [doc, setDoc] = useState(null)
  const [error, setError] = useState('')

  const [summary, setSummary] = useState('')
  const [aiLoading, setAiLoading] = useState(false)
  const [question, setQuestion] = useState('')
  const [answer, setAnswer] = useState('')
  const [qaLoading, setQaLoading] = useState(false)

  useEffect(() => {
    documentApi
      .detail(id)
      .then(setDoc)
      .catch((err) => setError(err.message))
  }, [id])

  async function runSummary() {
    setAiLoading(true)
    setError('')
    try {
      const res = await aiApi.summary(doc.content || '')
      setSummary(res)
    } catch (err) {
      setError(err.message)
    } finally {
      setAiLoading(false)
    }
  }

  async function runQa(e) {
    e.preventDefault()
    if (!question.trim()) return
    setQaLoading(true)
    setError('')
    try {
      const res = await aiApi.qa(Number(id), question)
      setAnswer(res)
    } catch (err) {
      setError(err.message)
    } finally {
      setQaLoading(false)
    }
  }

  if (error && !doc) return <div className="page"><div className="alert alert-error">{error}</div></div>

  return (
    <div className="page">
      <div className="page-head">
        <h2>{doc?.title || '加载中…'}</h2>
        <div className="head-actions">
          <Link className="btn" to={`/documents/${id}/edit`}>
            编辑
          </Link>
          <button className="btn" onClick={() => navigate('/documents')}>
            返回列表
          </button>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {doc && (
        <>
          <div className="doc-detail-meta">
            <span>创建：{formatTime(doc.createTime)}</span>
            <span>更新：{formatTime(doc.updateTime)}</span>
            <span>可见性：{doc.isPublic ? '公开' : '私有'}</span>
          </div>

          <article className="doc-body">{doc.content || '（暂无内容）'}</article>

          <section className="ai-panel">
            <h3>AI 能力</h3>
            <div className="ai-row">
              <button className="btn btn-primary" onClick={runSummary} disabled={aiLoading}>
                {aiLoading ? '生成中…' : '生成摘要'}
              </button>
              {summary && <div className="ai-result">{summary}</div>}
            </div>

            <form className="ai-row" onSubmit={runQa}>
              <input
                className="input"
                placeholder="就本文档提问，例如：这篇文档讲了什么重点？"
                value={question}
                onChange={(e) => setQuestion(e.target.value)}
              />
              <button className="btn btn-primary" disabled={qaLoading}>
                {qaLoading ? '思考中…' : '提问'}
              </button>
              {answer && <div className="ai-result ai-answer">{answer}</div>}
            </form>
          </section>
        </>
      )}
    </div>
  )
}

function formatTime(t) {
  if (!t) return '-'
  return String(t).replace('T', ' ').slice(0, 16)
}
