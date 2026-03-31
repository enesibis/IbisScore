import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/store/authStore'
import toast from 'react-hot-toast'
import { Star } from 'lucide-react'

export default function Register() {
  const [form, setForm]       = useState({ username: '', email: '', password: '' })
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const setAuth  = useAuthStore(s => s.setAuth)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (form.password.length < 8) {
      toast.error('Şifre en az 8 karakter olmalı')
      return
    }
    setLoading(true)
    try {
      const data = await authApi.register(form.username, form.email, form.password)
      setAuth(data.token, data.userId, data.username, data.role)
      toast.success('Kayıt başarılı!')
      navigate('/')
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Kayıt başarısız')
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
          <p className="text-muted text-sm mt-1">Yeni hesap oluştur</p>
        </div>

        <form onSubmit={handleSubmit} className="card space-y-4">
          {[
            { key: 'username', label: 'Kullanıcı adı',  type: 'text',     placeholder: 'enes_ibis' },
            { key: 'email',    label: 'E-posta',         type: 'email',    placeholder: 'enes@email.com' },
            { key: 'password', label: 'Şifre',           type: 'password', placeholder: '••••••••' },
          ].map(({ key, label, type, placeholder }) => (
            <div key={key}>
              <label className="text-sm text-muted block mb-1">{label}</label>
              <input
                type={type}
                value={(form as any)[key]}
                onChange={e => setForm(f => ({ ...f, [key]: e.target.value }))}
                className="w-full bg-surface border border-border rounded-lg px-3 py-2 text-sm
                           focus:outline-none focus:border-primary transition-colors"
                placeholder={placeholder}
                required
              />
            </div>
          ))}
          <button type="submit" disabled={loading} className="btn-primary w-full">
            {loading ? 'Kayıt yapılıyor...' : 'Kayıt Ol'}
          </button>
        </form>

        <p className="text-center text-sm text-muted">
          Zaten hesabın var mı?{' '}
          <Link to="/login" className="text-primary hover:underline">Giriş yap</Link>
        </p>
      </div>
    </div>
  )
}
