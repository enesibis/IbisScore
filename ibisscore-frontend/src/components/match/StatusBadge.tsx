import { clsx } from 'clsx'

const STATUS_CONFIG: Record<string, { label: string; className: string }> = {
  NS:   { label: 'Başlamadı', className: 'bg-border text-muted' },
  LIVE: { label: '● CANLI',  className: 'bg-success/20 text-success animate-pulse' },
  HT:   { label: 'Devre A.', className: 'bg-warning/20 text-warning' },
  '1H': { label: '1. Yarı',  className: 'bg-success/20 text-success' },
  '2H': { label: '2. Yarı',  className: 'bg-success/20 text-success' },
  FT:   { label: 'Bitti',    className: 'bg-border text-muted' },
  PST:  { label: 'Ertelendi',className: 'bg-danger/20 text-danger' },
  CANC: { label: 'İptal',    className: 'bg-danger/20 text-danger' },
}

export default function StatusBadge({ status }: { status: string }) {
  const config = STATUS_CONFIG[status] ?? { label: status, className: 'bg-border text-muted' }
  return (
    <span className={clsx('badge text-xs', config.className)}>
      {config.label}
    </span>
  )
}
