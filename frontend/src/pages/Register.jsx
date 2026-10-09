import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from '../api/auth'

export default function Register() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ username: '', password: '', email: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function update(key) {
    return (e) => setForm((f) => ({ ...f, [key]: e.target.value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await authApi.register({
        username: form.username,
        password: form.password,
        email: form.email || undefined,
      })
      navigate('/login', { replace: true })
    } catch (err) {
      setError(err.message || '注册失败')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h1 className="auth-title">注册</h1>
        <p className="auth-sub">创建你的账号</p>

        {error && <div className="alert alert-error">{error}</div>}

        <label className="field">
          <span>用户名</span>
          <input value={form.username} onChange={update('username')} placeholder="3-20 个字符" required />
        </label>

        <label className="field">
          <span>密码</span>
          <input
            type="password"
            value={form.password}
            onChange={update('password')}
            placeholder="6-30 个字符"
            required
          />
        </label>

        <label className="field">
          <span>邮箱（选填）</span>
          <input type="email" value={form.email} onChange={update('email')} placeholder="you@example.com" />
        </label>

        <button className="btn btn-primary btn-block" disabled={loading}>
          {loading ? '注册中…' : '注册'}
        </button>

        <p className="auth-foot">
          已有账号？<Link to="/login">去登录</Link>
        </p>
      </form>
    </div>
  )
}
