import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { documentApi } from '../api/documents'
import { categoryApi } from '../api/categories'

export default function Documents() {
  const navigate = useNavigate()
  const [data, setData] = useState({ records: [], total: 0, current: 1, pages: 0 })
  const [categories, setCategories] = useState([])
  const [keyword, setKeyword] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [current, setCurrent] = useState(1)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const size = 10

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const page = await documentApi.page({
        current,
        size,
        keyword: keyword || undefined,
        categoryId: categoryId || undefined,
      })
      setData(page)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [current, keyword, categoryId])

  useEffect(() => {
    load()
  }, [load])

  useEffect(() => {
    categoryApi.list().then(setCategories).catch(() => {})
  }, [])

  async function handleDelete(id) {
    if (!window.confirm('确定删除该文档？')) return
    try {
      await documentApi.remove(id)
      load()
    } catch (err) {
      alert(err.message)
    }
  }

  function onSearch(e) {
    e.preventDefault()
    setCurrent(1)
    load()
  }

  return (
    <div className="page">
      <div className="page-head">
        <h2>我的文档</h2>
        <Link to="/documents/new" className="btn btn-primary">
          + 新建文档
        </Link>
      </div>

      <form className="toolbar" onSubmit={onSearch}>
        <input
          className="input"
          placeholder="搜索标题 / 内容关键词"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
        <select
          className="input"
          value={categoryId}
          onChange={(e) => {
            setCategoryId(e.target.value)
            setCurrent(1)
          }}
        >
          <option value="">全部分类</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
        <button className="btn" type="submit">
          搜索
        </button>
      </form>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="doc-list">
        {loading && <div className="page-loading">加载中…</div>}
        {!loading && data.records.length === 0 && (
          <div className="empty">还没有文档，点击右上角「新建文档」开始吧。</div>
        )}
        {!loading &&
          data.records.map((doc) => (
            <div className="doc-card" key={doc.id}>
              <div className="doc-card-main" onClick={() => navigate(`/documents/${doc.id}`)}>
                <div className="doc-title">{doc.title}</div>
                <div className="doc-meta">
                  <span>
                    分类：
                    {categories.find((c) => c.id === doc.categoryId)?.name || '未分类'}
                  </span>
                  <span>更新：{formatTime(doc.updateTime)}</span>
                </div>
              </div>
              <div className="doc-actions">
                <Link className="btn btn-sm" to={`/documents/${doc.id}`}>
                  查看
                </Link>
                <Link className="btn btn-sm" to={`/documents/${doc.id}/edit`}>
                  编辑
                </Link>
                <button className="btn btn-sm btn-danger" onClick={() => handleDelete(doc.id)}>
                  删除
                </button>
              </div>
            </div>
          ))}
      </div>

      <Pager current={data.current} pages={data.pages} total={data.total} onChange={setCurrent} />
    </div>
  )
}

function Pager({ current, pages, total, onChange }) {
  if (!pages || pages <= 1) return null
  return (
    <div className="pager">
      <button className="btn btn-sm" disabled={current <= 1} onClick={() => onChange(current - 1)}>
        上一页
      </button>
      <span>
        第 {current} / {pages} 页 · 共 {total} 条
      </span>
      <button
        className="btn btn-sm"
        disabled={current >= pages}
        onClick={() => onChange(current + 1)}
      >
        下一页
      </button>
    </div>
  )
}

function formatTime(t) {
  if (!t) return '-'
  return String(t).replace('T', ' ').slice(0, 16)
}
