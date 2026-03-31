import { useQuery } from '@tanstack/react-query'
import { fixturesApi } from '@/api/fixtures'
import LoadingSpinner from '@/components/ui/LoadingSpinner'
import { TrendingUp, Star } from 'lucide-react'
import { clsx } from 'clsx'
import type { ValueBet } from '@/types'

const CONFIDENCE_COLOR: Record<string, string> = {
  HIGH:   'text-success border-success/30 bg-success/10',
  MEDIUM: 'text-warning border-warning/30 bg-warning/10',
  LOW:    'text-muted  border-border       bg-border/30',
}

export default function ValueBets() {
  const { data = [], isLoading } = useQuery({
    queryKey: ['value-bets'],
    queryFn: fixturesApi.getValueBets,
    refetchInterval: 30 * 60_000,  // 30 dakika
  })

  const bets = data as (ValueBet & { fixture?: { homeTeam?: { name: string }; awayTeam?: { name: string } } })[]

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <TrendingUp className="text-primary w-6 h-6" />
        <div>
          <h1 className="text-2xl font-bold">Value Bets</h1>
          <p className="text-sm text-muted">Beklenen değeri pozitif olan bahis fırsatları</p>
        </div>
      </div>

      {isLoading ? (
        <LoadingSpinner />
      ) : bets.length === 0 ? (
        <div className="card text-center py-16 text-muted">
          Bugün için value bet bulunamadı.
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {bets.map((bet, i) => (
            <ValueBetCard key={bet.fixtureId ?? i} bet={bet} />
          ))}
        </div>
      )}

      <div className="card bg-primary/5 border-primary/20">
        <p className="text-xs text-muted leading-relaxed">
          <strong className="text-white">Sorumluluk Reddi:</strong> Value bet hesaplamaları
          istatistiksel modellere dayanır ve %100 kesinlik garantisi vermez. Bahis yaparken
          sorumlu davranın. Bu platform yatırım tavsiyesi niteliği taşımaz.
        </p>
      </div>
    </div>
  )
}

function ValueBetCard({ bet }: { bet: any }) {
  const bestBetLabel =
    bet.bestBet === 'HOME_WIN' ? '1 (Ev Kazanır)' :
    bet.bestBet === 'DRAW'     ? 'X (Beraberlik)' :
    bet.bestBet === 'AWAY_WIN' ? '2 (Dep Kazanır)' : '—'

  const evPercent = bet.bestBetEv != null ? `+${(bet.bestBetEv * 100).toFixed(1)}%` : null
  const kellyPct  = bet.bestBetKelly != null ? `${(bet.bestBetKelly * 100).toFixed(1)}%` : null
  const level     = bet.confidenceLevel ?? 'LOW'

  return (
    <div className={clsx('card border', CONFIDENCE_COLOR[level])}>
      <div className="flex items-start justify-between mb-3">
        <span className={clsx('badge border', CONFIDENCE_COLOR[level])}>
          {level === 'HIGH' ? '★ Yüksek' : level === 'MEDIUM' ? '◈ Orta' : '◇ Düşük'}
        </span>
        {evPercent && (
          <span className="text-success font-bold text-sm">{evPercent} EV</span>
        )}
      </div>

      <p className="font-semibold text-sm mb-1">
        Maç #{bet.fixtureId}
      </p>

      <div className="space-y-2 text-sm mt-3">
        <div className="flex justify-between">
          <span className="text-muted">Öneri</span>
          <span className="font-medium">{bestBetLabel}</span>
        </div>
        {bet.bestBetOdd && (
          <div className="flex justify-between">
            <span className="text-muted">Oran</span>
            <span className="font-medium">{bet.bestBetOdd.toFixed(2)}</span>
          </div>
        )}
        {kellyPct && (
          <div className="flex justify-between">
            <span className="text-muted">Kelly Stake</span>
            <span className="font-medium">{kellyPct}</span>
          </div>
        )}
      </div>
    </div>
  )
}
