# TicketWave Events

Modular monolithic platform (Spring Boot 4, Java 21) for event management and ticket sales, with a unified **reserve + purchase** flow based on `TicketOrder`.

## Architecture Diagrams

Documentation diagrams (SVG, editable in draw.io):

### C4 model — `diagrams/c4model/`

![C2 c4model diagram](./diagrams/c4model/ticketwave-c2-container.drawio.svg)

| Level | File | Description |
|-------|------|-------------|
| C1 | `ticketwave-c1-context.drawio.svg` | System context: users and external systems around TicketWave |
| C2 | `ticketwave-c2-container.drawio.svg` | Containers: web/API, monolith, database, Redis |
| C3 | `ticketwave-c3-event-search.drawio.svg` | Component: event search |
| C3 | `ticketwave-c3-digital-ticket-service.drawio.svg` | Component: digital ticket service |
| C3 | `ticketwave-c3-ticket-purchase.drawio.svg` | Component: ticket purchase flow |
| C3 | `ticketwave-c3-payment-service.drawio.svg` | Component: payment service |
| C3 | `ticketwave-c3-promotions-service.drawio.svg` | Component: promotions service |
| C3 | `ticketwave-c3-notifications-service.drawio.svg` | Component: notifications service |
| C3 | `ticketwave-c3-refunds-cancellations.drawio.svg` | Component: refunds & cancellations |


## Technologies

- **Java 21**
- **Spring Boot 4**
- **Spring Data JPA** + PostgreSQL (H2 for local development)
- **Spring Security + JWT** (jjwt 0.12)
- **Redis** (ticket locking, fraud detection and saga snapshots)
- **Orchestrated saga** (EventBus / CommandBus, in-memory or RabbitMQ)
- **OpenAPI / Swagger UI**
- **Lombok**

## Requirements

- JDK 21
- Maven 3.9+
- PostgreSQL 15+ (database `ticketwave_cors`)
- Redis 7+ (saga and cache persist there)
- RabbitMQ (optional, only with the `rabbitmq` profile)

## Structure

```
ticketwave-events/
 ├── src/main/java/com/ticketwave/
 │   ├── TicketwaveApplication.java
 │   ├── config/            # Security, JWT, OpenAPI, Cache, DataSeeder, EventBus
 │   ├── controller/        # Event, Payment, Ticket, User, Notification, Promotion, Fraud
 │   ├── application/       # Use cases and services + SagaRecoveryJob
 │   ├── domain/            # Entities, enums, events, commands, bus and saga
 │   ├── repository/        # Data access contracts
 │   ├── infrastructure/    # JPA repositories, bus (RabbitMQ/in-memory), HTTP clients, security
 │   ├── exception/         # Exceptions + GlobalExceptionHandler
 │   └── modules/           # Modular boundaries (preparation for microservices)
 ├── src/main/resources/    # application.yml, application-local.yml
 ├── diagrams/c4model/      # Architecture diagrams (C4 model)
 └── src/test/
```

## C4 diagrams

Architecture diagrams (C4 model) are in `diagrams/c4model`:

| Diagram | File |
|----------|---------|
| C1 - System context | `diagrams/c4model/ticketwave-c1-context.drawio.svg` |
| C2 - Containers | `diagrams/c4model/ticketwave-c2-container.drawio.svg` |
| C3 - Cross-service saga | `diagrams/c4model/ticketwave-c3-cross-service-saga.drawio.svg` |
| C3 - Digital ticket service | `diagrams/c4model/ticketwave-c3-digital-ticket-service.drawio.svg` |
| C3 - Event search | `diagrams/c4model/ticketwave-c3-event-search.drawio.svg` |
| C3 - Notifications service | `diagrams/c4model/ticketwave-c3-notifications-service.drawio.svg` |
| C3 - Payment service | `diagrams/c4model/ticketwave-c3-payment-service.drawio.svg` |
| C3 - Promotions service | `diagrams/c4model/ticketwave-c3-promotions-service.drawio.svg` |
| C3 - Purchase flow | `diagrams/c4model/ticketwave-c3-purchase-flow.drawio.svg` |
| C3 - Refunds and cancellations | `diagrams/c4model/ticketwave-c3-refunds-cancellations.drawio.svg` |
| C3 - Saga orchestrator | `diagrams/c4model/ticketwave-c3-saga-orchestrator.drawio.svg` |

## Running

```bash
# Local development (in-memory H2, in-memory bus, no external Redis)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# PostgreSQL + Redis, configured through environment variables
$env:DB_URL="jdbc:postgresql://localhost:5432/ticketwave_cors"
$env:DB_USERNAME="postgres"; $env:DB_PASSWORD="postgres"
$env:REDIS_HOST="localhost"; $env:REDIS_PORT="6379"
$env:JWT_SECRET="<32-byte-secret>"
mvn spring-boot:run
```

By default (no active profile) the app uses PostgreSQL + Redis and the **in-memory** bus.
To enable the RabbitMQ transport:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=rabbitmq
```

## Docker (external dependencies)

Standalone containers (you can start them separately or all together):

```bash
# rabbitmq
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:4-management

# redis
docker run -d --name redis -p 6379:6379 redis:7-alpine

# kong api gateway
docker run -d --name kong \
  -e "KONG_DATABASE=off" \
  -e "KONG_DECLARATIVE_CONFIG=/kong/kong.yml" \
  -e "KONG_ADMIN_LISTEN=0.0.0.0:8001" \
  -v ticketwave-api-gateway\ticketwave-api-gateway\kong:/kong \
  -p 9000:8000 \
  -p 9001:8001 \
  kong:latest

# prometheus
docker run -d --name prometheus \
  -v ticketwave-api-gateway\ticketwave-api-gateway\prometheus\prometheus.yml:/etc/prometheus/prometheus.yml \
  -p 9090:9090 prom/prometheus

# grafana
docker run -d --name grafana -p 3000:3000 grafana/grafana
```

External links:

- RabbitMQ Management: http://localhost:15672/
- Prometheus Targets: http://localhost:9090/targets
- Grafana: http://localhost:3000/

## Requirements

- JDK 21
- Maven 3.9+
- PostgreSQL 15+ (or use the `local` profile with embedded H2)
- Redis 7+ (optional in the `local` profile)
- Docker (for external dependencies)

## Docker (external dependencies)

Independent containers (you can start them separately or together):

```bash
# rabbitmq
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:4-management

# redis
docker run -d --name redis -p 6379:6379 redis:7-alpine

# kong api gateway
docker run -d --name kong \
  -e "KONG_DATABASE=off" \
  -e "KONG_DECLARATIVE_CONFIG=/kong/kong.yml" \
  -e "KONG_ADMIN_LISTEN=0.0.0.0:8001" \
  -v ticketwave-api-gateway\ticketwave-api-gateway\kong:/kong \
  -p 9000:8000 \
  -p 9001:8001 \
  kong:latest

# Prometheus
docker run -d --name prometheus \
  -v ticketwave-api-gateway\ticketwave-api-gateway\prometheus\prometheus.yml:/etc/prometheus/prometheus.yml \
  -p 9090:9090 prom/prometheus

# grafana
docker run -d --name grafana -p 3000:3000 grafana/grafana
```

External links:

- RabbitMQ Management: <http://localhost:15672/>
- Prometheus Targets: <http://localhost:9090/targets>
- Grafana: <http://localhost:3000/>

## Kong (API Gateway) configuration

Kong runs in DB-less mode reading the declarative config from `kong/kong.yml` (mounted as `/kong/kong.yml` in the container). Services, routes, and plugins:

```yaml
_format_version: "3.0"
_transform: true

services:
  - name: service-8091
    url: http://host.docker.internal:8091
    routes:
      - name: route-8091
        paths:
          - /api8091
        strip_path: true
      - name: route-8091-docs
        paths:
          - /v3/api-docs/reports
        strip_path: false
      - name: route-8091-swagger-ui
        paths:
          - /reports
        strip_path: false

  - name: service-8090
    url: http://host.docker.internal:8090
    routes:
      - name: route-8090
        paths:
          - /api8090
        strip_path: true
      - name: route-8090-docs
        paths:
          - /v3/api-docs/ticketorder
        strip_path: false
      - name: route-8090-swagger-ui
        paths:
          - /ticketorder
        strip_path: false

  - name: service-8081
    url: http://host.docker.internal:8081
    routes:
      - name: route-8081
        paths:
          - /api8081
        strip_path: true
      - name: route-8081-docs
        paths:
          - /v3/api-docs/legacy
        strip_path: false
      - name: route-8081-swagger-ui
        paths:
          - /legacy
        strip_path: false

plugins:
  - name: prometheus
  
  - name: http-log
    config:
      http_endpoint: http://host.docker.internal:8085/logs
      method: POST
      timeout: 1000
      keepalive: 30
      flush_timeout: 2


  # - name: jwt
  #   config:
  #     key_claim_name: kid
  #     secret_is_base64: false

# ---------------------------------------------------------------------------
# TLS termination
# ---------------------------------------------------------------------------
# Self-signed placeholder cert for local testing. Generate a real one with:
#   openssl req -x509 -newkey rsa:2048 -keyout key.pem -out cert.pem \
#       -days 365 -nodes -subj "/CN=api.ticketwave.local"
# and paste the PEM blocks below (replace the placeholders).
# certificates:
#   - id: ticketwave-tls
#     cert: |
#       -----BEGIN CERTIFICATE-----
#       (replace with your certificate PEM - see README)
#       -----END CERTIFICATE-----
#     key: |
#       -----BEGIN PRIVATE KEY-----
#       (replace with your private key PEM - see README)
#       -----END PRIVATE KEY-----
#     snis:
#       - name: api.ticketwave.local
```

## Prometheus configuration

Prometheus reads its config from `prometheus/prometheus.yml` (mounted as `/etc/prometheus/prometheus.yml` in the container):

```yaml
scrape_configs:
  - job_name: 'kong'
    metrics_path: /metrics
    static_configs:
      - targets: ['host.docker.internal:9001']
```


## Environment variable configuration (application.yml)

| Variable            | Default                              | Purpose                           |
|---------------------|--------------------------------------|-----------------------------------|
| `DB_URL`            | `jdbc:postgresql://localhost:5432/ticketwave_cors` | JDBC datasource |
| `DB_USERNAME`       | `postgres`                           | Database username                 |
| `DB_PASSWORD`       | `postgres`                           | Database password                 |
| `REDIS_HOST`        | `localhost`                          | Redis host                        |
| `REDIS_PORT`        | `6379`                               | Redis port                        |
| `JWT_SECRET`        | default development value            | JWT secret (minimum 32 bytes)     |
| `JWT_EXPIRATION`    | `86400000`                           | Token expiration (ms)             |
| `ORDER_TTL_MINUTES` | `15`                                 | Order TTL (reservation)           |
| `INTERNAL_TOKEN`    | `change-me-internal-token`           | Token for service-to-service calls |
| `ORDER_SERVICE_URL` | `http://localhost:8090`              | ticketorder-write service URL      |

## Legacy monolith

Swagger UI: http://localhost:8081/swagger-ui/index.html#/Events/search

## CQRS architecture (read / write)

Swagger documentation for the platform's CQRS services:

| Service | Swagger documentation |
|----------|----------------------|
| **Read (reports)** | http://localhost:8091/swagger-ui/index.html#/report-controller/allReports |
| **Write (ticket orders - reserve)** | http://localhost:8093/swagger-ui/index.html#/Ticket%20Orders/reserve |

## Demo credentials (automatic seed, `local` profile)

| User  | Password | Role   |
|-------|----------|--------|
| admin | admin1234 | ADMIN |
| user  | user1234  | USER   |

## Main endpoints

| Method | Route                                | Description                              | Access  |
|--------|--------------------------------------|------------------------------------------|---------|
| GET    | `/api/events`                        | Paginated search (city, artist, venue, date) | Public |
| GET    | `/api/events/all`                    | Full list of events                      | Public |
| GET    | `/api/events/{id}`                   | Event details                            | Public |
| POST   | `/api/events`                        | Create event                             | ADMIN   |
| PUT    | `/api/events/{id}`                   | Update event                             | ADMIN   |
| DELETE | `/api/events/{id}`                   | Cancel event                             | ADMIN   |
| POST   | `/api/events/{id}/reserve`           | Reserve capacity                         | ADMIN   |
| POST   | `/api/events/{id}/release`           | Release capacity                         | ADMIN   |
| POST   | `/api/payments`                      | Confirm reservation and create payment   | Authenticated |
| GET    | `/api/payments/order/{orderId}`      | Get payment by order                     | Authenticated |
| GET    | `/api/tickets/{id}`                  | Ticket details                           | Authenticated |
| GET    | `/api/tickets/order/{orderId}`       | Tickets of an order                      | Authenticated |
| POST   | `/api/tickets/validate`              | Validate ticket at venue                 | ADMIN   |
| POST   | `/api/tickets/{id}/refund`           | Refund ticket                            | Authenticated |
| POST   | `/api/users/register`                | Registration                             | Public |
| POST   | `/api/users/login`                   | Login → JWT                              | Public |
| GET    | `/api/users/me`                      | Profile of the authenticated user        | Authenticated |
| GET    | `/api/promotions`                    | Active promotions                        | Public |
| POST   | `/api/promotions`                    | Create promotion                         | ADMIN   |
| POST   | `/api/promotions/{code}/quote`       | Calculate discount                       | ADMIN   |
| POST   | `/api/promotions/{code}/increment-usage` | Increment usage                       | ADMIN   |
| GET    | `/api/notifications`                 | User notifications                       | Authenticated |
| PATCH  | `/api/notifications/{id}/read`       | Mark notification as read                | Authenticated |
| GET    | `/api/fraud/check`                   | Fraud risk assessment                    | Authenticated |
| POST   | `/api/fraud/guard`                   | Block attempts by user/IP                | ADMIN   |
| POST   | `/api/fraud/orders`                  | Mark order as fraudulent                 | ADMIN   |

## Purchase flow (TicketOrder saga)

1. `ticketorder-write` publishes `TicketOrderCreated` → the orchestrator starts the saga.
2. `ProcessPaymentCommand` → on confirmation the payment is settled and authorized.
3. `IssueTicketCommand` → digital tickets with QR code are issued (`TicketIssued`).
4. `NotifyOrderCommand` → notifications are sent to the user.
5. Compensations: a failed payment cancels the order; a failed ticket delivery refunds the payment; both converge on `COMPENSATED`.
6. Saga snapshots are persisted in Redis (`SagaStateRepository`) and `SagaRecoveryJob` resumes interrupted sagas (enable with `ticketwave.saga.recovery-enabled`).

## Event / command bus

- `rabbitmq` profile: real transport over RabbitMQ (`RabbitMQEventBusAdapter` / `RabbitMQCommandBusAdapter`).
- Any other profile (or none): in-memory doubles (`InMemoryEventBus` / `InMemoryCommandBus`), no broker.

## Testing

```bash
mvn test
```

Tests use the `test` profile (in-memory H2 + in-memory bus).

## Security

- JWT bearer token issued at `/api/users/login` and `/api/users/register`.
- Admin endpoints protected with `@PreAuthorize("hasRole('ADMIN')")`.
- Service-to-service calls authenticated with the `X-Internal-Token` header.
- Fraud detection: attempt limit per user/IP in Redis, duplicate order prevention.