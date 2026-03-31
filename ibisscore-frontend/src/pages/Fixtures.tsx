import { useQuery } from '@tanstack/react-query'
import { fixturesApi } from '@/api/fixtures'
import MatchCard from '@/components/match/MatchCard'
import LoadingSpinner from '@/components/ui/LoadingSpinner'

export default function Fixtures() {
  const { data: predictions = [], isLoading } = useQuery({
    queryKey: ['top-predictions'],
    queryFn: () => fixturesApi.getTopPredictions(0.70, 20),
  })

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">En Güvenilir Tahminler</h1>
        <p className="text-sm text-muted mt-1">Güven skoru %70 ve üzeri maçlar</p>
      </div>

      {isLoading ? (
        <LoadingSpinner />
      ) : predictions.length === 0 ? (
        <div className="card text-center py-16 text-muted">
          Henüz tahmin bulunmuyor.
        </div>
      ) : (
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {predictions.map((p: any) => (
            p.fixture && <MatchCard key={p.fixtureId} fixture={p.fixture} />
          ))}
        </div>
      )}
    </div>
  )
}
