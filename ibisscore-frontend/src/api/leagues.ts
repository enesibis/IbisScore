import api from './client'
import type { League } from '@/types'

export const leaguesApi = {
  getActive: () =>
    api.get<{ success: boolean; data: League[] }>('/leagues')
       .then(r => r.data.data),

  getById: (id: number) =>
    api.get<{ success: boolean; data: League }>(`/leagues/${id}`)
       .then(r => r.data.data),
}
