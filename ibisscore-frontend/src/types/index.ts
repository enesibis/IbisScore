export interface Team {
  id:        number
  name:      string
  shortName?: string
  logoUrl?:  string
  form?:     string
  formPoints?: number
}

export interface League {
  id:      number
  name:    string
  country?: string
  logoUrl?: string
  season?: number
}

export interface Fixture {
  id:         number
  apiId?:     number
  league:     League
  homeTeam:   Team
  awayTeam:   Team
  matchDate:  string
  status:     string
  homeGoals?: number
  awayGoals?: number
  homeGoalsHt?: number
  awayGoalsHt?: number
  prediction?: Prediction
  bestOdds?:   Odds
}

export interface Prediction {
  id?:             number
  fixtureId:       number
  modelVersion?:   string
  homeWinProb:     number
  drawProb:        number
  awayWinProb:     number
  predictedHomeGoals?: number
  predictedAwayGoals?: number
  over25Prob?:     number
  bttsProbability?: number
  confidenceScore: number
  recommendation:  string
  valueBet?:       ValueBet
  createdAt?:      string
}

export interface Odds {
  bookmaker?:  string
  homeWinOdd?: number
  drawOdd?:    number
  awayWinOdd?: number
  over25Odd?:  number
  bttsYesOdd?: number
}

export interface ValueBet {
  fixtureId?:      number
  evHome?:         number
  evDraw?:         number
  evAway?:         number
  isValueBetHome?: boolean
  isValueBetDraw?: boolean
  isValueBetAway?: boolean
  bestBet?:        string
  bestBetEv?:      number
  bestBetOdd?:     number
  bestBetKelly?:   number
  confidenceLevel?: 'LOW' | 'MEDIUM' | 'HIGH'
}
