import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { fixturesApi } from '@/api/fixtures'
import ProbabilityBar from '@/components/match/ProbabilityBar'
import StatusBadge from '@/components/match/StatusBadge'
import LoadingSpinner from '@/components/ui/LoadingSpinner'
import { useLiveScore } from '@/hooks/useLiveScore'
import { format } from 'date-fns'
import { tr } from 'date-fns/locale'
import { clsx } from 'clsx'

export default function FixtureDetail() {
  const { id } = useParams<{ id: string }>()

  const { data: fixture, isLoading } = useQuery({
    queryKey: ['fixture', id],
    queryFn: () => fixturesApi.getById(Number(id)),
    enabled: !!id,
  })

  const { data: predictions = [] } = useQuery({
    queryKey: ['predictions', id],
    queryFn: () => fixturesApi.getPredictions(Number(id)),
    enabled: !!id,
  })

  const liveScore = useLiveScore(
    fixture && ['1H', '2H', 'HT', 'ET', 'BT', 'P'].includes(fixture.status)
      ? fixture.id
      : null
  )

  if (isLoading) return <LoadingSpinner />
  if (!fixture)  return <div className="text-center text-muted py-16">Maç bulunamadı.</div>

  const pred = predictions[0]
  const isLive     = ['1H', '2H', 'HT', 'ET', 'BT', 'P'].includes(fixture.status)
  const isFinished = ['FT', 'AET', 'PEN'].includes(fixture.status)
  const homeGoals  = liveScore?.homeGoals ?? fixture.homeGoals
  const awayGoals  = liveScore?.awayGoals ?? fixture.awayGoals

  return (
    <div className="max-w-3xl mx-auto space-y-6">

      {/* Header */}
      <div className="card text-center space-y-4">
        <div className="flex items-center justify-center gap-2">
          <span className="text-sm text-muted">{fixture.league.name}</span>
          <StatusBadge status={fixture.status} />
        </div>
        <p className="text-xs text-muted">
          {format(new Date(fixture.matchDate), 'dd MMMM yyyy HH:mm', { locale: tr })}
        </p>

        {/* Teams + Score */}
        <div className="flex items-center justify-around">
          <div className="text-center space-y-2">
            {fixture.homeTeam.logoUrl && (
              <img src={fixture.homeTeam.logoUrl} alt="" className="w-16 h-16 mx-auto object-contain" />
            )}
            <p className="font-bold">{fixture.homeTeam.name}</p>
          </div>

          <div className="text-center">
            {(isLive || isFinished) ? (
              <div className="space-y-1">
                <div className="flex items-center gap-2 justify-center">
                  {isLive && (
                    <span className="flex h-2 w-2">
                      <span className="animate-ping absolute inline-flex h-2 w-2 rounded-full bg-red-400 opacity-75" />
                      <span className="relative inline-flex rounded-full h-2 w-2 bg-red-500" />
                    </span>
                  )}
                  <p className={clsx('text-5xl font-bold tabular-nums', isLive && 'text-red-400')}>
                    {homeGoals ?? 0} – {awayGoals ?? 0}
                  </p>
                </div>
                {(liveScore?.homeGoalsHt ?? fixture.homeGoalsHt) !== undefined && (
                  <p className="text-xs text-muted">
                    İY: {liveScore?.homeGoalsHt ?? fixture.homeGoalsHt} – {liveScore?.awayGoalsHt ?? fixture.awayGoalsHt}
                  </p>
                )}
              </div>
            ) : (
              <p className="text-2xl text-muted">vs</p>
            )}
          </div>

          <div className="text-center space-y-2">
            {fixture.awayTeam.logoUrl && (
              <img src={fixture.awayTeam.logoUrl} alt="" className="w-16 h-16 mx-auto object-contain" />
            )}
            <p className="font-bold">{fixture.awayTeam.name}</p>
          </div>
        </div>
      </div>

      {/* Tahmin */}
      {pred && (
        <div className="card space-y-4">
          <h2 className="font-semibold">AI Tahmini</h2>
          <ProbabilityBar prediction={pred} />

          <div className="grid grid-cols-3 gap-3 text-center text-sm mt-2">
            <div className="bg-blue-500/10 rounded-lg p-3">
              <p className="text-muted text-xs mb-1">Ev Gol</p>
              <p className="font-bold text-lg">{pred.predictedHomeGoals?.toFixed(1)}</p>
            </div>
            <div className="bg-yellow-500/10 rounded-lg p-3">
              <p className="text-muted text-xs mb-1">O 2.5</p>
              <p className="font-bold text-lg">{pred.over25Prob ? `${Math.round(pred.over25Prob * 100)}%` : '—'}</p>
            </div>
            <div className="bg-red-500/10 rounded-lg p-3">
              <p className="text-muted text-xs mb-1">Dep Gol</p>
              <p className="font-bold text-lg">{pred.predictedAwayGoals?.toFixed(1)}</p>
            </div>
          </div>

          {pred.recommendation !== 'NO_BET' && (
            <div className="bg-primary/10 border border-primary/30 rounded-lg p-3 text-center">
              <p className="text-xs text-muted mb-1">Model Önerisi</p>
              <p className="font-bold text-primary">
                {pred.recommendation === 'HOME_WIN' ? `${fixture.homeTeam.name} Kazanır` :
                 pred.recommendation === 'DRAW' ? 'Beraberlik' :
                 `${fixture.awayTeam.name} Kazanır`}
              </p>
            </div>
          )}
        </div>
      )}

      {/* Oranlar */}
      {fixture.bestOdds && (
        <div className="card">
          <h2 className="font-semibold mb-3">En İyi Oranlar ({fixture.bestOdds.bookmaker})</h2>
          <div className="grid grid-cols-3 gap-3 text-center">
            {[
              { label: '1 (Ev)', value: fixture.bestOdds.homeWinOdd },
              { label: 'X (Beraberlik)', value: fixture.bestOdds.drawOdd },
              { label: '2 (Dep)', value: fixture.bestOdds.awayWinOdd },
            ].map(({ label, value }) => (
              <div key={label} className="bg-surface rounded-lg p-3">
                <p className="text-xs text-muted mb-1">{label}</p>
                <p className="font-bold text-lg">{value?.toFixed(2) ?? '—'}</p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
