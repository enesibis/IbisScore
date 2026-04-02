#!/bin/bash
# IbisScore — Lokal başlatma scripti
# Kullanım: bash scripts/start-local.sh
#
# Önkoşullar:
#   docker compose up -d postgres redis rabbitmq
#   mvn package -DskipTests -B
#
# NOT: Windows'ta başka bir PostgreSQL varsa POSTGRES_PORT=5433 kullan

set -a
source .env 2>/dev/null || true
set +a

# Zorunlu değişkenler
: "${POSTGRES_DB:=ibisscore}"
: "${POSTGRES_USER:=ibisscore_user}"
: "${POSTGRES_PASSWORD:=ibisscore_pg_pass_2024}"
: "${POSTGRES_PORT:=5433}"
: "${REDIS_PASSWORD:=ibisscore_redis_2024}"
: "${RABBITMQ_USER:=ibis_rabbit}"
: "${RABBITMQ_PASSWORD:=ibisscore_rabbit_2024}"
: "${JWT_SECRET:=ibisscore_jwt_secret_key_for_local_dev_only_32chars}"
: "${JWT_EXPIRATION_MS:=86400000}"
: "${API_FOOTBALL_KEY:=dummy_for_local_dev}"

ARGS="-Dspring.datasource.url=jdbc:postgresql://localhost:${POSTGRES_PORT}/${POSTGRES_DB} \
  -Dspring.datasource.username=${POSTGRES_USER} \
  -Dspring.datasource.password=${POSTGRES_PASSWORD} \
  -DSPRING_REDIS_HOST=localhost \
  -Dspring.redis.host=localhost \
  -DSPRING_REDIS_PASSWORD=${REDIS_PASSWORD} \
  -Dspring.redis.password=${REDIS_PASSWORD} \
  -Dspring.rabbitmq.host=localhost \
  -Dspring.rabbitmq.username=${RABBITMQ_USER} \
  -Dspring.rabbitmq.password=${RABBITMQ_PASSWORD} \
  -Djwt.secret=${JWT_SECRET} \
  -Djwt.expiration-ms=${JWT_EXPIRATION_MS} \
  -DAPI_FOOTBALL_KEY=${API_FOOTBALL_KEY}"

mkdir -p logs

echo "Starting User Service (8081)..."
java $ARGS -jar ibisscore-user-service/target/ibisscore-user-service-*.jar \
  > logs/user-service.log 2>&1 &
echo "  PID: $!"

echo "Starting Match Service (8082)..."
java $ARGS -jar ibisscore-match-service/target/ibisscore-match-service-*.jar \
  > logs/match-service.log 2>&1 &
echo "  PID: $!"

echo "Starting Data Ingestion (8083)..."
java $ARGS -jar ibisscore-data-ingestion/target/ibisscore-data-ingestion-*.jar \
  > logs/data-ingestion.log 2>&1 &
echo "  PID: $!"

echo "Starting Betting Service (8084)..."
java $ARGS -jar ibisscore-betting-service/target/ibisscore-betting-service-*.jar \
  > logs/betting-service.log 2>&1 &
echo "  PID: $!"

echo "Starting Gateway (8080)..."
java $ARGS -jar ibisscore-gateway/target/ibisscore-gateway-*.jar \
  > logs/gateway.log 2>&1 &
echo "  PID: $!"

echo ""
echo "All services starting... 45s bekle sonra health check yap:"
echo "  Gateway:   http://localhost:8080/actuator/health"
echo "  User:      http://localhost:8081/actuator/health"
echo "  Match:     http://localhost:8082/actuator/health"
echo "  Ingestion: http://localhost:8083/actuator/health"
echo "  Betting:   http://localhost:8084/actuator/health"
echo ""
echo "Test:"
echo "  curl -s http://localhost:8080/api/leagues"
echo "  curl -X POST http://localhost:8080/api/auth/login -H 'Content-Type: application/json' -d '{\"usernameOrEmail\":\"admin\",\"password\":\"Admin1234!\"}'"
echo ""
echo "Logs: tail -f logs/*.log"
