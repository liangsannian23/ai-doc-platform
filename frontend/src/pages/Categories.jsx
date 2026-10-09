import { useEffect, useState } from 'react'
import { categoryApi } from '../api/categories'

export default function Categories() {
  const [list, setList] = useState([])
  const [name, setName] = useState('')
  const [editingId, setEditingId] = useState(null)
  const [editingName, setEditingName] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function load() {
    setLoading(true)
    try {
      setList(await categoryApi.list())
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  async function handleAdd(e) {
    e.preventDefault()
    if (!name.trim()) return
    setError('')
    try {
      await categoryApi.create(name.trim())
      setName('')
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleRename(id) {
    if (!editingName.trim()) return
    setError('')
    try {
      await categoryApi.update(id, editingName.trim())
      setEditingId(null)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleDelete(id) {
    if (!window.confirm('确定删除该分类？')) return
    setError('')
    try {
      await categoryApi.remove(id)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="page">
      <div className="page-head">
        <h2>分类管理</h2>
      </div>

      <form className="toolbar" onSubmit={handleAdd}>
        <input
          className="input"
          placeholder="新分类名称"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <button className="btn btn-primary" type="submit">
          添加
        </button>
      </form>

      {error && <div className="alert alert-error">{error}</div>}
      {loading && <div className="page-loading">加载中…</div>}

      <ul className="cat-list">
        {list.map((c) => (
          <li className="cat-item" key={c.id}>
            {editingId === c.id ? (
              <>
                <input
                  className="input"
                  value={editingName}
                  onChange={(e) => setEditingName(e.target.value)}
                  autoFocus
                />
                <button className="btn btn-sm btn-primary" onClick={() => handleRename(c.id)}>
                  保存
                </button>
                <button className="btn btn-sm" onClick={() => setEditingId(null)}>
                  取消
                </button>
              </>
            ) : (
              <>
                <span className="cat-name">{c.name}</span>
                <button
                  className="btn btn-sm"
                  onClick={() => {
                    setEditingId(c.id)
                    setEditingName(c.name)
                  }}
                >
                  重命名
                </button>
                <button className="btn btn-sm btn-danger" onClick={() => handleDelete(c.id)}>
                  删除
                </button>
              </>
            )}
          </li>
        ))}
        {!loading && list.length === 0 && <div className="empty">还没有分类，添加一个吧。</div>}
      </ul>
    </div>
  )
}
