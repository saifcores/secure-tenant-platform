# SecureTenant Platform

Enterprise-style **B2B multi-tenant SaaS** demo: several organisations share one Spring Boot API while data and permissions stay isolated per tenant.

The invariant the project is built to prove:

> A user from tenant `acme` can never read or mutate resources that belong to tenant `globex`, even by forging HTTP headers, query parameters, or path identifiers.

## Stack

| Layer         | Choice                                                      |
| ------------- | ----------------------------------------------------------- |
| Runtime       | Java 25, Spring Boot 4.1                                    |
| Security      | Spring Security OAuth2 Resource Server, JWT, Keycloak       |
| Multi-tenancy | Arconia (`resolution-mode: oauth2`) + Hibernate `@TenantId` |
| Persistence   | PostgreSQL 16, Flyway, shared schema                        |
| Messaging     | Kafka (outbox → `payments.events`), optional locally        |
| Observability | Arconia OpenTelemetry, Actuator, Prometheus, Grafana        |
| Tests         | JUnit, MockMvc JWT, Testcontainers (PostgreSQL + Keycloak)  |

## Quick start

Java 25 is required (Arconia 0.30 bytecode). With SDKMAN: `sdk use java 25-tem`.

```bash
docker compose up --build
```

| Service                    | URL                                   |
| -------------------------- | ------------------------------------- |
| API                        | http://localhost:8080                 |
| Keycloak                   | http://localhost:8081 (admin / admin) |
| Kafka (host)               | localhost:19092                       |
| Prometheus                 | http://localhost:9090                 |
| Grafana                    | http://localhost:3000 (admin / admin) |
| Grafana LGTM (traces/logs) | http://localhost:3001                 |

Run only the infrastructure and start the API from Maven:

```bash
docker compose up postgres keycloak kafka prometheus grafana otel-lgtm
export JAVA_HOME="$HOME/.sdkman/candidates/java/25-tem"
./mvnw spring-boot:run
```

Prometheus scrapes `api:8080` (Compose API container) and `host.docker.internal:8080` (local `spring-boot:run`). One of the two targets will be down depending on how you start the API.

## Demo users

All passwords: `password`

| User             | Tenant | Role           |
| ---------------- | ------ | -------------- |
| `alice`          | acme   | TENANT_ADMIN   |
| `bob`            | acme   | USER           |
| `carol`          | acme   | MANAGER        |
| `dave`           | acme   | AUDITOR        |
| `john`           | globex | TENANT_ADMIN   |
| `mike`           | globex | USER           |
| `nina`           | fincorp | TENANT_ADMIN   |
| `paul`           | fincorp | USER           |
| `platform-admin` | —      | PLATFORM_ADMIN |

Obtain a token (public client, Resource Owner Password Grant for the demo only):

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/realms/securetenant/protocol/openid-connect/token \
  -d grant_type=password \
  -d client_id=securetenant-public \
  -d username=alice \
  -d password=password | jq -r .access_token)

curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/orders
```

The script `scripts/demo.sh` walks through the mandatory security scenario (own orders, cross-tenant deny, forged `X-Tenant-ID`, audit, confirm + pay, wallets, reconciliation).

## Tests

```bash
export JAVA_HOME="$HOME/.sdkman/candidates/java/25-tem"
./mvnw test
```

Docker is required for Testcontainers (PostgreSQL and Keycloak).

## Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md)
- [SECURITY.md](SECURITY.md)
- [MULTI-TENANCY.md](MULTI-TENANCY.md)
- [PAYMENTS.md](PAYMENTS.md)
- [API.md](API.md)
- [ADR/](ADR/)
