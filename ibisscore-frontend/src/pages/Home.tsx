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

  // Öncelikli ligler sırası (API-Football ID → sıra)
  const LEAGUE_PRIORITY: Record<number, number> = {
    2: 1,   // Champions League
    3: 2,   // Europa League
    39: 3,  // Premier League
    140: 4, // La Liga
    78: 5,  // Bundesliga
    135: 6, // Serie A
    61: 7,  // Ligue 1
    203: 8, // Süper Lig
  }

  // Ülke + lig adıyla grupla
  const grouped = fixtures.reduce((acc, f) => {
    const country = f.league?.country ?? 'Diğer'
    const name = f.league?.name ?? 'Diğer'
    const key = `${country}__${name}`
    if (!acc[key]) acc[key] = []
    acc[key].push(f)
    return acc
  }, {} as Record<string, typeof fixtures>)

  // Öncelikli ligler önce, geri kalanlar ülke adına göre alfabetik
  const sortedGroups = Object.entries(grouped).sort(([keyA, matchesA], [keyB, matchesB]) => {
    const prioA = LEAGUE_PRIORITY[matchesA[0]?.league?.apiId ?? 0] ?? 99
    const prioB = LEAGUE_PRIORITY[matchesB[0]?.league?.apiId ?? 0] ?? 99
    if (prioA !== prioB) return prioA - prioB
    return keyA.localeCompare(keyB)
  })

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
          {sortedGroups.map(([key, matches]) => {
            const league = matches[0]?.league
            const country = league?.country ?? 'Diğer'
            const name = league?.name ?? 'Diğer'
            return (
              <section key={key}>
                <h3 className="text-sm font-medium text-muted mb-2 flex items-center gap-2">
                  {league?.logoUrl && (
                    <img src={league.logoUrl} alt={name} className="w-4 h-4 object-contain" />
                  )}
                  <span className="font-semibold text-foreground">{name}</span>
                  <span className="text-xs">— {country}</span>
                  <span className="w-4 h-px bg-border flex-1" />
                  <span>{matches.length} maç</span>
                </h3>
                <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                  {matches.map(f => <MatchCard key={f.id} fixture={f} />)}
                </div>
              </section>
            )
          })}
        </div>
      )}
    </div>
  )
}
