import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
import ProtectedRoute from './components/ProtectedRoute'
import Login from './pages/Login'
import Register from './pages/Register'
import Documents from './pages/Documents'
import DocumentEditor from './pages/DocumentEditor'
import DocumentDetail from './pages/DocumentDetail'
import AIAssistant from './pages/AIAssistant'
import Categories from './pages/Categories'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route path="/documents" element={<Documents />} />
        <Route path="/documents/new" element={<DocumentEditor />} />
        <Route path="/documents/:id/edit" element={<DocumentEditor />} />
        <Route path="/documents/:id" element={<DocumentDetail />} />
        <Route path="/ai" element={<AIAssistant />} />
        <Route path="/categories" element={<Categories />} />
      </Route>

      <Route path="*" element={<Navigate to="/documents" replace />} />
    </Routes>
  )
}
