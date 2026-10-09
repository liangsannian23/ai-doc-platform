import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { documentApi } from '../api/documents'
import { categoryApi } from '../api/categories'

export default function DocumentEditor() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [form, setForm] = useState({ title: '', content: '', categoryId: '', isPublic: 0 })
  const [categories, setCategories] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    categoryApi.list().then(setCategories).catch(() => {})
  }, [])

  useEffect(() => {
    if (!isEdit) return
    setLoading(true)
    documentApi
      .detail(id)
      .then((doc) =>
        setForm({
          title: doc.title || '',
          content: doc.content || '',
          categoryId: doc.categoryId || '',
          isPublic: doc.isPublic ?? 0,
        }),
      )
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [id, isEdit])

  function update(key) {
    return (e) => setForm((f) => ({ ...f, [key]: e.target.value }))
  }

  async function handleSave(e) {
    e.preventDefault()
    setError('')
    setSaving(true)
    try {
      const payload = {
        title: form.title,
        content: form.content,
        categoryId: form.categoryId ? Number(form.categoryId) : null,
      }
      if (isEdit) {
        await documentApi.update(id, payload)
        navigate(`/documents/${id}`)
      } else {
        payload.isPublic = Number(form.isPublic)
        const newId = await documentApi.create(payload)
        navigate(`/documents/${newId}`)
      }
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="page">
      <div className="page-head">
        <h2>{isEdit ? '编辑文档' : '新建文档'}</h2>
        <button className="btn" onClick={() => navigate(-1)}>
          返回
        </button>
      </div>

      {loading && <div className="page-loading">加载中…</div>}
      {error && <div className="alert alert-error">{error}</div>}

      <form className="editor" onSubmit={handleSave}>
        <input
          className="input input-title"
          placeholder="文档标题"
          value={form.title}
          onChange={update('title')}
          required
        />

        <div className="editor-side">
          <label className="field">
            <span>分类</span>
            <select className="input" value={form.categoryId} onChange={update('categoryId')}>
              <option value="">未分类</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </label>

          {!isEdit && (
            <label className="field">
              <span>是否公开</span>
              <select className="input" value={form.isPublic} onChange={update('isPublic')}>
                <option value={0}>私有</option>
                <option value={1}>公开</option>
              </select>
            </label>
          )}
        </div>

        <textarea
          className="input input-content"
          placeholder="在这里撰写文档内容…"
          value={form.content}
          onChange={update('content')}
          rows={18}
        />

        <div className="editor-foot">
          <button className="btn btn-primary" disabled={saving}>
            {saving ? '保存中…' : '保存'}
          </button>
        </div>
      </form>
    </div>
  )
}
