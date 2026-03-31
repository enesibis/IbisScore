import { useQuery } from '@tanstack/react-query'
import { Trophy } from 'lucide-react'
import { clsx } from 'clsx'
import { usersApi, type LeaderboardEntry } from '@/api/users'
import LoadingSpinner from '@/components/ui/LoadingSpinner'

const RANK_STYLE: Record<number, string> = {
  1: 'text-yellow-400',
  2: 'text-slate-300',
  3: 'text-amber-600',
}

const RANK_ICON: Record<number, string> = {
  1: '🥇',
  2: '🥈',
  3: '🥉',
}

export default function Leaderboard() {
  const { data = [], isLoading } = useQuery({
    queryKey: ['leaderboard'],
    queryFn: () => usersApi.getLeaderboard(50),
    refetchInterval: 5 * 60_000,
  })

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Trophy className="text-warning w-6 h-6" />
        <div>
          <h1 className="text-2xl font-bold">Sıralama</h1>
          <p className="text-sm text-muted">En başarılı tahmin yapanlar</p>
        </div>
      </div>

      {isLoading ? (
        <LoadingSpinner />
      ) : data.length === 0 ? (
        <div className="card text-center py-16 text-muted">
          Henüz tahmin yapılmadı.
        </div>
      ) : (
        <>
          {data.length >= 3 && (
            <div className="grid grid-cols-3 gap-4 mb-2">
              {([data[1], data[0], data[2]] as LeaderboardEntry[]).map((entry, col) => {
                const rank = col === 0 ? 2 : col === 1 ? 1 : 3
                return (
                  <PodiumCard key={entry.userId} entry={entry} rank={rank} center={rank === 1} />
                )
              })}
            </div>
          )}

          <div className="card overflow-hidden p-0">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border text-muted text-xs uppercase tracking-wide">
                  <th className="px-4 py-3 text-left w-10">#</th>
                  <th className="px-4 py-3 text-left">Kullanıcı</th>
                  <th className="px-4 py-3 text-right">Puan</th>
                  <th className="px-4 py-3 text-right hidden sm:table-cell">Doğru</th>
                  <th className="px-4 py-3 text-right hidden md:table-cell">Toplam</th>
                  <th className="px-4 py-3 text-right hidden md:table-cell">İsabet %</th>
                </tr>
              </thead>
              <tbody>
                {data.map((entry, idx) => (
                  <LeaderboardRow key={entry.userId} entry={entry} rank={idx + 1} />
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  )
}

function PodiumCard({ entry, rank, center }: { entry: LeaderboardEntry; rank: number; center: boolean }) {
  return (
    <div className={clsx(
      'card text-center flex flex-col items-center gap-2 py-4',
      center ? 'border-warning/40 bg-warning/5' : 'mt-6',
    )}>
      <span className="text-2xl">{RANK_ICON[rank]}</span>
      <span className={clsx('font-bold text-sm truncate w-full text-center', RANK_STYLE[rank])}>
        {entry.username}
      </span>
      <span className="text-lg font-bold">{entry.totalPoints}</span>
      <span className="text-xs text-muted">puan</span>
    </div>
  )
}

function LeaderboardRow({ entry, rank }: { entry: LeaderboardEntry; rank: number }) {
  const accuracy = entry.totalPredictions > 0
    ? ((entry.correctPredictions / entry.totalPredictions) * 100).toFixed(1)
    : null

  return (
    <tr className="border-b border-border/50 hover:bg-surface/50 transition-colors">
      <td className="px-4 py-3">
        <span className={clsx('font-bold', RANK_STYLE[rank] ?? 'text-muted')}>
          {RANK_ICON[rank] ?? rank}
        </span>
      </td>
      <td className="px-4 py-3 font-medium">{entry.username}</td>
      <td className="px-4 py-3 text-right font-bold text-primary">{entry.totalPoints}</td>
      <td className="px-4 py-3 text-right hidden sm:table-cell text-success">{entry.correctPredictions}</td>
      <td className="px-4 py-3 text-right hidden md:table-cell text-muted">{entry.totalPredictions}</td>
      <td className="px-4 py-3 text-right hidden md:table-cell">
        {accuracy !== null ? (
          <span className={clsx(
            'text-xs font-medium px-2 py-0.5 rounded-full',
            Number(accuracy) >= 60 ? 'bg-success/20 text-success' :
            Number(accuracy) >= 40 ? 'bg-warning/20 text-warning' :
                                     'bg-border/50 text-muted',
          )}>
            {accuracy}%
          </span>
        ) : (
          <span className="text-muted">—</span>
        )}
      </td>
    </tr>
  )
}
