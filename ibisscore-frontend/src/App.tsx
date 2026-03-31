import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from '@/components/layout/Layout'
import Home from '@/pages/Home'
import Fixtures from '@/pages/Fixtures'
import FixtureDetail from '@/pages/FixtureDetail'
import ValueBets from '@/pages/ValueBets'
import Leaderboard from '@/pages/Leaderboard'
import Login from '@/pages/Login'
import Register from '@/pages/Register'
import { useAuthStore } from '@/store/authStore'

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const token = useAuthStore(s => s.token)
  return token ? <>{children}</> : <Navigate to="/login" replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/login"    element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/" element={<Layout />}>
        <Route index           element={<Home />} />
        <Route path="fixtures" element={<Fixtures />} />
        <Route path="fixtures/:id" element={<FixtureDetail />} />
        <Route path="value-bets"   element={<ValueBets />} />
        <Route path="leaderboard"  element={
          <ProtectedRoute><Leaderboard /></ProtectedRoute>
        } />
      </Route>
    </Routes>
  )
}
