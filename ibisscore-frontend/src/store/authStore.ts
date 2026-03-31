import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface AuthState {
  token:    string | null
  userId:   number | null
  username: string | null
  role:     string | null
  setAuth:  (token: string, userId: number, username: string, role: string) => void
  logout:   () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token:    null,
      userId:   null,
      username: null,
      role:     null,
      setAuth: (token, userId, username, role) =>
        set({ token, userId, username, role }),
      logout: () => set({ token: null, userId: null, username: null, role: null }),
    }),
    { name: 'ibisscore-auth' }
  )
)
