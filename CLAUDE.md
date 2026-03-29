# IbisScore — Claude Code Kılavuzu

Yapay zeka destekli futbol skor tahmini ve bahis analizi platformu.

---

## Proje Yapısı

```
IbisScore/                          ← Maven multi-module root
├── CLAUDE.md                       ← Bu dosya
├── pom.xml                         ← Parent POM (Java 21, Spring Boot 3.3.4)
├── docker-compose.yml              ← Tüm servisleri ayağa kaldırır
├── .env.example                    ← .env şablonu (kopyala → .env)
├── scripts/init-db.sql             ← PostgreSQL şema + başlangıç verileri
│
├── ibisscore-common/               ← Paylaşılan DTOs, enum, event, exception
├── ibisscore-gateway/              ← Spring Cloud Gateway (port 8080)
├── ibisscore-user-service/         ← Auth + JWT (port 8081)
├── ibisscore-match-service/        ← Maçlar, tahminler (port 8082)
├── ibisscore-data-ingestion/       ← API-Football cron jobs (port 8083)
├── ibisscore-betting-service/      ← Value bet hesaplama (port 8084)
│
├── ibisscore-ai-service/           ← Python FastAPI + ML modeller (port 8000)
└── ibisscore-frontend/             ← React + TypeScript + Vite (port 3000)
```

---

## Servis Portları

| Servis | Port | Teknoloji |
|---|---|---|
| API Gateway | 8080 | Spring Cloud Gateway |
| User Service | 8081 | Spring Boot + PostgreSQL |
| Match Service | 8082 | Spring Boot + Redis + RabbitMQ |
| Data Ingestion | 8083 | Spring Boot + Scheduled jobs |
| Betting Service | 8084 | Spring Boot + JdbcTemplate |
| AI Service | 8000 | Python FastAPI |
| Frontend | 3000 | React + Vite |
| PostgreSQL | 5432 | — |
| Redis | 6379 | — |
| RabbitMQ | 5672 / 15672 | — |

---

## Hızlı Başlangıç

```bash
# 1. Ortam değişkenlerini ayarla
cp .env.example .env
# .env dosyasını düzenle: API_FOOTBALL_KEY, JWT_SECRET, vs.

# 2. Altyapıyı başlat (DB + Redis + RabbitMQ)
docker-compose up -d postgres redis rabbitmq

# 3. Java servislerini derle
mvn clean install -DskipTests

# 4. Tüm servisleri başlat
docker-compose up -d

# 5. AI servisini başlat (Python)
cd ibisscore-ai-service
pip install -r requirements.txt
uvicorn main:app --reload --port 8000

# 6. Frontend'i başlat
cd ibisscore-frontend
npm install && npm run dev
```

---

## Java Modül Kuralları

### Ortak Prensipler
- **Java 21** — record, sealed class, pattern matching kullanılabilir
- **Spring Boot 3.3.4** — Jakarta EE namespace (javax → jakarta)
- **Lombok** tüm modüllerde aktif (`@Data`, `@Builder`, `@Slf4j` vb.)
- **MapStruct** entity↔DTO dönüşümü için (manuel mapping yasak)
- **GlobalExceptionHandler** → `ibisscore-common`'dan extend edilir

### Paket Yapısı (her servis için)
```
com.ibisscore.<servis>/
├── config/          ← Spring config sınıfları
├── controller/      ← REST controller'lar
├── dto/             ← Servis-spesifik request/response nesneleri
├── entity/          ← JPA entity'leri
├── mapper/          ← MapStruct interface'leri
├── repository/      ← JPA/JDBC repository'leri
├── service/         ← İş mantığı
└── <Servis>Application.java
```

### API Response Formatı
Her endpoint `ApiResponse<T>` döndürür (`ibisscore-common`'da tanımlı):
```json
{
  "success": true,
  "message": "...",
  "data": { ... },
  "timestamp": "2024-01-01T12:00:00"
}
```

### Güvenlik
- **Gateway'de JWT doğrulanır** — downstream servisler `X-User-Id` ve `X-User-Role` header'larını okur
- Public endpoint'ler: `/api/auth/**`, `/api/fixtures`, `/api/leagues`, `/api/predictions`, `/actuator/**`
- Korumalı endpoint'ler: `/api/users/**`, `/api/bets/**`, `/api/value-bets/**`

### Cache Stratejisi (Redis TTL)
| Cache Key | TTL | Açıklama |
|---|---|---|
| `fixtures-today` | 15 dk | Günlük maç listesi |
| `fixture-detail` | 5 dk | Maç detayı (canlıysa 2 dk) |
| `live-fixtures` | 2 dk | Canlı maçlar |
| `top-predictions` | 60 dk | En güvenilir tahminler |
| `daily-value-bets` | 30 dk | Günün value bet listesi |

---

## Veri Tabanı

- **PostgreSQL 16** — tek veritabanı, tüm servisler paylaşır
- **Flyway migration** — sadece `user-service`'te aktif (`V1__`, `V2__` formatı)
- Diğer tablolar `init-db.sql` ile oluşturulur
- `ddl-auto: validate` — Hibernate tablo oluşturmaz, sadece doğrular

### Kritik Tablolar
| Tablo | Açıklama |
|---|---|
| `fixtures` | Maç verileri (API-Football'dan) |
| `teams` | Takım bilgileri |
| `leagues` | Lig bilgileri |
| `odds` | Bookmaker oranları |
| `team_season_stats` | Takım sezon istatistikleri (Poisson lambda'ları) |
| `predictions` | AI model tahminleri |
| `users` | Kullanıcı hesapları |
| `user_predictions` | Kullanıcı tahminleri (leaderboard) |

---

## RabbitMQ Event Akışı

```
data-ingestion  ──(fixture.event)──►  ibisscore.fixtures exchange
                                           │
                                           ▼
                                      fixtures.queue
                                           │
                                           ▼
                                    match-service (consumer)
                                           │
                                    prediction.request
                                           │
                                           ▼
                                  ibisscore.predictions exchange
                                           │
                                           ▼
                                    ai-service (consumer)
```

### Exchange / Queue İsimleri
| Exchange | Routing Key | Queue |
|---|---|---|
| `ibisscore.fixtures` | `fixture.event` | `fixtures.queue` |
| `ibisscore.predictions` | `prediction.request` | `predictions.queue` |

DLQ (Dead Letter Queue): `predictions.dlq` — başarısız tahmin istekleri

---

## AI Servis (Python)

### Endpoint'ler
```
POST /predict/match/{fixture_id}   ← Tek maç tahmini
POST /predict/batch                ← Toplu tahmin
GET  /model/metrics                ← Model performans metrikleri
GET  /predict/value-bets           ← Günün value bet önerileri
```

### Model Pipeline
1. **Poisson Modeli** — Dixon-Coles düzeltmeli beklenen gol hesabı
2. **XGBoost Modeli** — Feature engineering + 1X2 olasılık tahmini
3. **Ensemble** — %40 Poisson + %60 XGBoost

### Feature'lar (XGBoost için)
- `home/away_avg_goals_for/against_5` — Son 5 maç ortalamaları
- `home/away_form_points` — Form puanı (W=3, D=1, L=0)
- `home_home_win_rate`, `away_away_win_rate` — Saha-spesifik kazanma oranı
- `h2h_home_wins`, `h2h_draws`, `h2h_avg_goals` — Head-to-head
- `implied_prob_*` — Bookmaker implied olasılıkları
- `days_since_last_match_*` — Dinlenme süresi

---

## Bahis Analizi

### Value Bet Formülleri
```
Expected Value (EV) = (model_probability × odd) - 1
Edge = model_probability - (1 / odd)
Kelly = (b×p - q) / b   [b = odd-1, p = model_prob, q = 1-p]
Stake = Kelly × 0.25    (Quarter Kelly — güvenli)
```

### Value Bet Eşikleri
- `EV > 0.05` (min %5 EV) → value bet adayı
- `Edge > 0.03` (min %3 edge) → onaylanır
- Güven: `EV > 0.15 && confidence > 0.75` → HIGH

---

## API-Football Rate Limit Yönetimi

- **Free tier**: 100 request/gün, 10 request/dakika
- `Bucket4j` ile throttle: `data-ingestion/ApiFootballClient.java`
- `@Retryable(maxAttempts=3, backoff=Exponential)` ile retry
- Başarısız istekler `Dead Letter Queue`'ya düşer

### Cron Zamanları
| Job | Zaman | Açıklama |
|---|---|---|
| `fetchDailyFixtures` | Her gün 06:00 | Bugün + yarının maçları |
| `fetchUpcomingOdds` | Her 2 saatte | Yaklaşan maç oranları |
| `fetchLiveScores` | Her 5 dakika | Canlı skor güncellemesi |
| `fetchTeamStats` | Pazartesi 03:00 | Haftalık istatistik |
| `fetchYesterdayResults` | Her gün 02:00 | Dünkü sonuçları kaydet |

---

## Geliştirme Notları

### Yeni Endpoint Eklerken
1. Controller'da `ApiResponse<T>` kullan
2. Service'te `@Transactional(readOnly = true)` tercih et
3. Cache için `@Cacheable` ekle ve TTL'i `RedisConfig`'e yaz
4. Validation için `@Valid` + `jakarta.validation` constraint kullan

### Yeni DB Migrasyonu Eklerken
```
ibisscore-user-service/src/main/resources/db/migration/
V{n}__description_in_snake_case.sql
```

### Test Stratejisi
- Unit test: Service katmanı (`@ExtendWith(MockitoExtension.class)`)
- Integration test: `@SpringBootTest` + Testcontainers (PostgreSQL, Redis)
- API testi: `MockMvc` veya `WebTestClient` (gateway için)

### Docker Build
```bash
# Tek servis build
cd ibisscore-match-service
mvn clean package -DskipTests
docker build -t ibisscore-match-service .

# Tüm servisler
mvn clean package -DskipTests
docker-compose build
```

---

## Kalan Geliştirme Adımları (MVP Sonrası)

- [ ] `ibisscore-betting-service` → application.yml + Dockerfile tamamla
- [ ] `ibisscore-ai-service` Python kodu yaz (Poisson + XGBoost)
- [ ] `ibisscore-frontend` React uygulamasını oluştur
- [ ] Model yeniden eğitim pipeline'ı (haftalık otomatik)
- [ ] WebSocket canlı skor güncellemesi
- [ ] Kullanıcı leaderboard sistemi
- [ ] Prometheus + Grafana monitoring kurulumu
- [ ] GitHub Actions CI/CD pipeline

---

## Ortam Değişkenleri

Tüm değişkenler `.env.example`'da belgelenmiştir.
Zorunlu olanlar: `API_FOOTBALL_KEY`, `JWT_SECRET`, `POSTGRES_PASSWORD`

**Not:** `.env` dosyasını asla commit etme. `.gitignore`'da zaten var.
