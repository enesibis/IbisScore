import { Trophy } from 'lucide-react'

// Placeholder — backend user_predictions tablosu ile doldurulacak
export default function Leaderboard() {
  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Trophy className="text-warning w-6 h-6" />
        <h1 className="text-2xl font-bold">Sıralama</h1>
      </div>
      <div className="card text-center py-16 text-muted">
        Leaderboard yakında aktif olacak.
      </div>
    </div>
  )
}
