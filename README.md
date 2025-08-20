# Banking Wallet Platform

Monorepo scaffold for a microservices-based banking wallet platform. This project is structured for a Spring Cloud + Docker Compose setup, with each service in its own directory and shared infrastructure managed via `docker-compose.yml`.

## Services
- `api-gateway/`: API Gateway (e.g., Spring Cloud Gateway)
- `config-server/`: Centralized config server
- `discovery-service/`: Service discovery/registry (e.g., Eureka)
- `auth-service/`: Authentication/authorization service
- `account-service/`: Account domain service
- `transaction-service/`: Transactions domain service
- `payment-service/`: Payments/settlement service
- `notification-service/`: Notifications (email/SMS/push)
- `config-repo/`: Externalized configuration repository
- `scripts/`: Helper scripts

## Prerequisites
- Docker Desktop (WSL2 engine on Windows recommended)
- Docker Compose (v2 via `docker compose`)
- Java 17 and Maven 3.9+ (only needed for local builds outside of Docker)

## Quick start (Docker)
1) Build and start everything
   - Makefile: `make up`
   - Or directly: `docker compose up -d --build`

2) Check status and logs
   - `docker compose ps`
   - `docker compose logs -f SERVICE_NAME`

3) Stop and clean up
   - Makefile: `make down`
   - Or directly: `docker compose down -v`

## Infrastructure (via docker-compose)
- Postgres: `localhost:5432` (DB: `wallet`, user: `wallet`, pass: `wallet`)
- MongoDB: `localhost:27017`
- Redis: `localhost:6379`
- Zookeeper: `localhost:2181`
- Kafka: `localhost:9092`

## Service ports / URLs
- Config Server: `http://localhost:8888` (health: `/actuator/health`)
- Service Discovery (Eureka): `http://localhost:8761`
- Auth Service: `http://localhost:9000` (Swagger: `/swagger-ui/index.html`)
- API Gateway: `http://localhost:8080` (Swagger: `/swagger-ui/index.html`)
- Domain services register with discovery and import config from Config Server.

## Smoke test
Option A (PowerShell script on Windows)
- Run: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke.ps1`
- The script:
  - Fetches an access token from `auth-service`
  - Creates an account through the gateway
  - Reads the created account back

Option B (manual via curl)
1) Get token (password grant)
```
curl -s -u demo-client:demo-secret \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&username=alice@example.com&password=password" \
  http://localhost:9000/oauth2/token
```

2) Create an account (replace TOKEN)
```
curl -s -X POST http://localhost:8080/api/accounts \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"ownerId":"alice@example.com"}'
```

3) Fetch the account (replace TOKEN and ACCOUNT_ID)
```
curl -s http://localhost:8080/api/accounts/ACCOUNT_ID \
  -H "Authorization: Bearer TOKEN"
```

## Development
- Build a single service image: `docker compose build SERVICE_NAME`
- Start a single service: `docker compose up -d SERVICE_NAME`
- Run tests locally: `mvn test`

## Notes
- Configuration is centralized in `config-server` with files under `config-repo/`.
- Services register with `discovery-service` and are routed via `api-gateway`.
- Default demo OAuth2 client: `demo-client` / `demo-secret`. Demo user: `alice@example.com` / `password`.

# banking-wallet-platform