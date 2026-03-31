import { clsx } from 'clsx'
import type { Prediction } from '@/types'

interface Props {
  prediction: Prediction
  showLabels?: boolean
}

export default function ProbabilityBar({ prediction, showLabels = true }: Props) {
  const { homeWinProb, drawProb, awayWinProb, recommendation, confidenceScore } = prediction

  const homePct = Math.round(homeWinProb * 100)
  const drawPct = Math.round(drawProb * 100)
  const awayPct = Math.round(awayWinProb * 100)

  const isValueHome = prediction.valueBet?.isValueBetHome
  const isValueDraw = prediction.valueBet?.isValueBetDraw
  const isValueAway = prediction.valueBet?.isValueBetAway

  return (
    <div className="space-y-1.5">
      {/* Bar */}
      <div className="flex rounded-full overflow-hidden h-2 gap-px">
        <div
          className="bg-blue-500 transition-all duration-500"
          style={{ width: `${homePct}%` }}
        />
        <div
          className="bg-yellow-500 transition-all duration-500"
          style={{ width: `${drawPct}%` }}
        />
        <div
          className="bg-red-500 transition-all duration-500"
          style={{ width: `${awayPct}%` }}
        />
      </div>

      {/* Labels */}
      {showLabels && (
        <div className="flex justify-between text-xs">
          <span className={clsx(
            'flex items-center gap-1',
            recommendation === 'HOME_WIN' ? 'text-blue-400 font-semibold' : 'text-muted'
          )}>
            {homePct}% {isValueHome && <span className="text-success">★</span>}
          </span>
          <span className={clsx(
            recommendation === 'DRAW' ? 'text-yellow-400 font-semibold' : 'text-muted'
          )}>
            {drawPct}% {isValueDraw && <span className="text-success">★</span>}
          </span>
          <span className={clsx(
            'flex items-center gap-1',
            recommendation === 'AWAY_WIN' ? 'text-red-400 font-semibold' : 'text-muted'
          )}>
            {isValueAway && <span className="text-success">★</span>} {awayPct}%
          </span>
        </div>
      )}

      {/* Confidence */}
      <div className="flex items-center gap-2">
        <div className="flex-1 h-1 bg-border rounded-full overflow-hidden">
          <div
            className={clsx(
              'h-full rounded-full transition-all',
              confidenceScore >= 0.75 ? 'bg-success' :
              confidenceScore >= 0.60 ? 'bg-warning' : 'bg-muted'
            )}
            style={{ width: `${confidenceScore * 100}%` }}
          />
        </div>
        <span className="text-xs text-muted">{Math.round(confidenceScore * 100)}%</span>
      </div>
    </div>
  )
}
