import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { format } from 'date-fns'
import { tr } from 'date-fns/locale'
import { ChevronLeft, ChevronRight, Wifi } from 'lucide-react'
import { fixturesApi } from '@/api/fixtures'
import MatchCard from '@/components/match/MatchCard'
import LoadingSpinner from '@/components/ui/LoadingSpinner'

export default function Home() {
  const [date, setDate] = useState(new Date())

  const dateStr = format(date, 'yyyy-MM-dd')

  const { data: fixtures = [], isLoading } = useQuery({
    queryKey: ['fixtures', dateStr],
    queryFn: () => fixturesApi.getByDate(dateStr),
    refetchInterval: 60_000,
  })

  const { data: live = [] } = useQuery({
    queryKey: ['live-fixtures'],
    queryFn: fixturesApi.getLive,
    refetchInterval: 30_000,
  })

  const prevDay = () => setDate(d => { const n = new Date(d); n.setDate(d.getDate() - 1); return n })
  const nextDay = () => setDate(d => { const n = new Date(d); n.setDate(d.getDate() + 1); return n })

  // Liglere göre grupla
  const grouped = fixtures.reduce((acc, f) => {
    const key = f.league?.name ?? 'Diğer'
    if (!acc[key]) acc[key] = []
    acc[key].push(f)
    return acc
  }, {} as Record<string, typeof fixtures>)

  return (
    <div className="space-y-6">

      {/* Canlı maçlar */}
      {live.length > 0 && (
        <section>
          <h2 className="flex items-center gap-2 text-success font-semibold mb-3">
            <Wifi className="w-4 h-4 animate-pulse" />
            Canlı Maçlar ({live.length})
          </h2>
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {live.map(f => <MatchCard key={f.id} fixture={f} />)}
          </div>
        </section>
      )}

      {/* Tarih navigasyonu */}
      <div className="flex items-center justify-between">
        <button onClick={prevDay} className="btn-ghost"><ChevronLeft className="w-5 h-5" /></button>
        <h1 className="font-semibold text-lg capitalize">
          {format(date, 'dd MMMM yyyy, EEEE', { locale: tr })}
        </h1>
        <button onClick={nextDay} className="btn-ghost"><ChevronRight className="w-5 h-5" /></button>
      </div>

      {/* Maçlar */}
      {isLoading ? (
        <LoadingSpinner />
      ) : fixtures.length === 0 ? (
        <div className="text-center text-muted py-16">Bu tarihte maç bulunamadı.</div>
      ) : (
        <div className="space-y-6">
          {Object.entries(grouped).map(([league, matches]) => (
            <section key={league}>
              <h3 className="text-sm font-medium text-muted mb-2 flex items-center gap-2">
                <span className="w-4 h-px bg-border" />
                {league}
                <span className="w-4 h-px bg-border flex-1" />
                <span>{matches.length} maç</span>
              </h3>
              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                {matches.map(f => <MatchCard key={f.id} fixture={f} />)}
              </div>
            </section>
          ))}
        </div>
      )}
    </div>
  )
}
