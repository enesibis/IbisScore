import { Link, NavLink } from 'react-router-dom'
import { TrendingUp, Trophy, Star, BarChart2, LogOut, LogIn } from 'lucide-react'
import { useAuthStore } from '@/store/authStore'
import { clsx } from 'clsx'

const links = [
  { to: '/',           label: 'Maçlar',      icon: BarChart2 },
  { to: '/value-bets', label: 'Value Bets',  icon: TrendingUp },
  { to: '/leaderboard',label: 'Sıralama',    icon: Trophy },
]

export default function Navbar() {
  const { username, logout } = useAuthStore()

  return (
    <nav className="bg-card border-b border-border sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">

        {/* Logo */}
        <Link to="/" className="flex items-center gap-2 font-bold text-xl">
          <Star className="text-primary w-6 h-6" />
          <span>IbisScore</span>
        </Link>

        {/* Nav links */}
        <div className="hidden md:flex items-center gap-1">
          {links.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/'}
              className={({ isActive }) => clsx(
                'flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-colors',
                isActive
                  ? 'bg-primary/20 text-primary'
                  : 'text-muted hover:text-white hover:bg-white/10'
              )}
            >
              <Icon className="w-4 h-4" />
              {label}
            </NavLink>
          ))}
        </div>

        {/* Auth */}
        <div className="flex items-center gap-3">
          {username ? (
            <>
              <span className="text-sm text-muted hidden sm:block">{username}</span>
              <button
                onClick={logout}
                className="btn-ghost flex items-center gap-2 text-sm"
              >
                <LogOut className="w-4 h-4" />
                <span className="hidden sm:block">Çıkış</span>
              </button>
            </>
          ) : (
            <Link to="/login" className="btn-primary flex items-center gap-2 text-sm">
              <LogIn className="w-4 h-4" />
              Giriş
            </Link>
          )}
        </div>
      </div>
    </nav>
  )
}
