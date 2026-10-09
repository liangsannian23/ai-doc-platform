import { Outlet } from 'react-router-dom'
import Navbar from './Navbar'

// 受保护区域的整体骨架：顶部导航 + 内容出口
export default function Layout() {
  return (
    <div className="app-shell">
      <Navbar />
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  )
}
