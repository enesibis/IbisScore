import api from './client'

export interface UserProfile {
  id: number
  username: string
  email: string
  role: string
  createdAt: string
}

export interface UserPredictionRequest {
  fixtureId: number
  predictedResult: '1' | 'X' | '2'
  predictedHome?: number
  predictedAway?: number
}

export interface UserPrediction {
  id: number
  fixtureId: number
  predictedResult: string
  predictedHome?: number
  predictedAway?: number
  pointsEarned: number
  isCorrect?: boolean
  createdAt: string
}

export interface LeaderboardEntry {
  userId: number
  username: string
  totalPoints: number
  correctPredictions: number
  totalPredictions: number
}

export const usersApi = {
  getProfile: () =>
    api.get<{ success: boolean; data: UserProfile }>('/users/me')
       .then(r => r.data.data),

  submitPrediction: (req: UserPredictionRequest) =>
    api.post<{ success: boolean; data: UserPrediction }>('/users/predictions', req)
       .then(r => r.data.data),

  getMyPredictions: () =>
    api.get<{ success: boolean; data: UserPrediction[] }>('/users/predictions')
       .then(r => r.data.data),

  getLeaderboard: (limit = 20) =>
    api.get<{ success: boolean; data: LeaderboardEntry[] }>('/users/leaderboard', {
      params: { limit },
    }).then(r => r.data.data),
}
