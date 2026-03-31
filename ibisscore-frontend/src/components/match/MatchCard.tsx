import { Link } from 'react-router-dom'
import { format } from 'date-fns'
import { tr } from 'date-fns/locale'
import { clsx } from 'clsx'
import type { Fixture } from '@/types'
import ProbabilityBar from './ProbabilityBar'
import StatusBadge from './StatusBadge'

interface Props {
  fixture: Fixture
}

export default function MatchCard({ fixture }: Props) {
  const { homeTeam, awayTeam, matchDate, status, homeGoals, awayGoals, prediction } = fixture

  const isFinished = ['FT', 'AET', 'PEN'].includes(status)
  const isLive     = ['LIVE', 'HT', '1H', '2H'].includes(status)

  return (
    <Link
      to={`/fixtures/${fixture.id}`}
      className={clsx(
        'card hover:border-primary/50 transition-all duration-200 cursor-pointer block',
        isLive && 'border-success/50 shadow-[0_0_12px_rgba(34,197,94,0.15)]'
      )}
    >
      {/* League + time */}
      <div className="flex items-center justify-between mb-3">
        <span className="text-xs text-muted">{fixture.league?.name}</span>
        <div className="flex items-center gap-2">
          <StatusBadge status={status} />
          {!isFinished && !isLive && (
            <span className="text-xs text-muted">
              {format(new Date(matchDate), 'HH:mm', { locale: tr })}
            </span>
          )}
        </div>
      </div>

      {/* Teams + score */}
      <div className="flex items-center justify-between gap-4">
        {/* Home */}
        <div className="flex items-center gap-2 flex-1 min-w-0">
          {homeTeam.logoUrl && (
            <img src={homeTeam.logoUrl} alt="" className="w-8 h-8 object-contain flex-shrink-0" />
          )}
          <span className="font-medium truncate text-sm">{homeTeam.name}</span>
        </div>

        {/* Score */}
        <div className="flex items-center gap-2 flex-shrink-0">
          {isFinished || isLive ? (
            <span className="text-2xl font-bold tabular-nums">
              {homeGoals ?? '—'} : {awayGoals ?? '—'}
            </span>
          ) : (
            <span className="text-sm text-muted px-2">vs</span>
          )}
        </div>

        {/* Away */}
        <div className="flex items-center gap-2 flex-1 min-w-0 justify-end">
          <span className="font-medium truncate text-sm text-right">{awayTeam.name}</span>
          {awayTeam.logoUrl && (
            <img src={awayTeam.logoUrl} alt="" className="w-8 h-8 object-contain flex-shrink-0" />
          )}
        </div>
      </div>

      {/* Probability bar */}
      {prediction && !isFinished && (
        <div className="mt-3 pt-3 border-t border-border">
          <ProbabilityBar prediction={prediction} />
        </div>
      )}
    </Link>
  )
}
