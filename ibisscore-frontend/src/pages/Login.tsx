import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/store/authStore'
import toast from 'react-hot-toast'
import { Star } from 'lucide-react'

export default function Login() {
  const [form, setForm]       = useState({ usernameOrEmail: '', password: '' })
  const [loading, setLoading] = useState(false)
  const navigate  = useNavigate()
  const setAuth   = useAuthStore(s => s.setAuth)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    try {
      const data = await authApi.login(form.usernameOrEmail, form.password)
      setAuth(data.token, data.userId, data.username, data.role)
      toast.success('Giriş başarılı!')
      navigate('/')
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Giriş başarısız')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-surface flex items-center justify-center p-4">
      <div className="w-full max-w-sm space-y-6">
        <div className="text-center">
          <Star className="text-primary w-10 h-10 mx-auto mb-2" />
          <h1 className="text-2xl font-bold">IbisScore</h1>
          <p className="text-muted text-sm mt-1">Hesabına giriş yap</p>
        </div>

        <form onSubmit={handleSubmit} className="card space-y-4">
          <div>
            <label className="text-sm text-muted block mb-1">Kullanıcı adı veya e-posta</label>
            <input
              type="text"
              value={form.usernameOrEmail}
              onChange={e => setForm(f => ({ ...f, usernameOrEmail: e.target.value }))}
              className="w-full bg-surface border border-border rounded-lg px-3 py-2 text-sm
                         focus:outline-none focus:border-primary transition-colors"
              placeholder="kullanici@email.com"
              required
            />
          </div>
          <div>
            <label className="text-sm text-muted block mb-1">Şifre</label>
            <input
              type="password"
              value={form.password}
              onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
              className="w-full bg-surface border border-border rounded-lg px-3 py-2 text-sm
                         focus:outline-none focus:border-primary transition-colors"
              placeholder="••••••••"
              required
            />
          </div>
          <button type="submit" disabled={loading} className="btn-primary w-full">
            {loading ? 'Giriş yapılıyor...' : 'Giriş Yap'}
          </button>
        </form>

        <p className="text-center text-sm text-muted">
          Hesabın yok mu?{' '}
          <Link to="/register" className="text-primary hover:underline">Kayıt ol</Link>
        </p>
      </div>
    </div>
  )
}
