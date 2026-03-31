import { useEffect, useRef, useState } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

export interface LiveScore {
  fixtureId:   number
  homeGoals:   number
  awayGoals:   number
  homeGoalsHt: number
  awayGoalsHt: number
  status:      string
}

const WS_URL = `${import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'}/match-service/ws`

/**
 * Tek bir maçın canlı skoruna abone olur.
 * Dönen score değeri null ise henüz güncelleme gelmemiş demektir.
 */
export function useLiveScore(fixtureId: number | null): LiveScore | null {
  const [score, setScore] = useState<LiveScore | null>(null)
  const clientRef = useRef<Client | null>(null)

  useEffect(() => {
    if (!fixtureId) return

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/scores/${fixtureId}`, (msg) => {
          try {
            setScore(JSON.parse(msg.body) as LiveScore)
          } catch {
            // malformed frame — ignore
          }
        })
      },
    })

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
    }
  }, [fixtureId])

  return score
}
