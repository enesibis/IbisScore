import api from './client'
import type { Fixture, Prediction } from '@/types'

export const fixturesApi = {
  getByDate: (date?: string) =>
    api.get<{ success: boolean; data: Fixture[] }>('/fixtures', {
      params: { date },
    }).then(r => r.data.data),

  getById: (id: number) =>
    api.get<{ success: boolean; data: Fixture }>(`/fixtures/${id}`)
       .then(r => r.data.data),

  getLive: () =>
    api.get<{ success: boolean; data: Fixture[] }>('/fixtures/live')
       .then(r => r.data.data),

  getPredictions: (fixtureId: number) =>
    api.get<{ success: boolean; data: Prediction[] }>(`/predictions/fixture/${fixtureId}`)
       .then(r => r.data.data),

  getTopPredictions: (minConfidence = 0.7, limit = 10) =>
    api.get<{ success: boolean; data: Prediction[] }>('/predictions/top', {
      params: { minConfidence, limit },
    }).then(r => r.data.data),

  getValueBets: () =>
    api.get<{ success: boolean; data: unknown[] }>('/value-bets')
       .then(r => r.data.data),
}
