import api from './client'
import type { ValueBet } from '@/types'

export interface ValueBetWithFixture extends ValueBet {
  fixtureId: number
  homeTeamName?: string
  awayTeamName?: string
  matchDate?: string
  leagueName?: string
}

export const valueBetsApi = {
  getDaily: () =>
    api.get<{ success: boolean; data: ValueBetWithFixture[] }>('/value-bets')
       .then(r => r.data.data),

  getByFixture: (fixtureId: number) =>
    api.get<{ success: boolean; data: ValueBet }>(`/value-bets/fixture/${fixtureId}`)
       .then(r => r.data.data),
}
