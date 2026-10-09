import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const links = [
  { to: '/documents', label: '我的文档' },
  { to: '/ai', label: 'AI 助手' },
  { to: '/categories', label: '分类管理' },
]

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <header className="navbar">
      <div className="navbar-brand">
        <span className="logo">AI</span>
        <span className="brand-text">智能内容协作平台</span>
      </div>
      <nav className="navbar-links">
        {links.map((l) => (
          <NavLink
            key={l.to}
            to={l.to}
            className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}
          >
            {l.label}
          </NavLink>
        ))}
      </nav>
      <div className="navbar-user">
        {user && <span className="username">{user.username}</span>}
        <button className="btn btn-ghost" onClick={handleLogout}>
          退出
        </button>
      </div>
    </header>
  )
}
