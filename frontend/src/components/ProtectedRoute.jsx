import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { tokenStore } from '../api/client'

// 未登录访问受保护路由 → 跳登录页，并记录来源以便登录后回跳
export default function ProtectedRoute({ children }) {
  const { user, loading } = useAuth()
  const location = useLocation()

  if (loading) return <div className="page-loading">加载中…</div>

  if (!user || !tokenStore.get()) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }
  return children
}
