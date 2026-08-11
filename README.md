# TicketWave Events

Plataforma monolítica modular (Spring Boot 4, Java 21) para gestión de eventos y venta de tickets, con un flujo unificado de **reserva + compra** basado en `TicketOrder`.

## Tecnologías

- **Java 21**
- **Spring Boot 4**
- **Spring Data JPA** + PostgreSQL (H2 para desarrollo local)
- **Spring Security + JWT** (jjwt 0.12)
- **Redis** (bloqueo de tickets, detección de fraude y snapshots del saga)
- **Saga orquestado** (EventBus / CommandBus, en memoria o RabbitMQ)
- **OpenAPI / Swagger UI**
- **Lombok**

## Requerimientos

- JDK 21
- Maven 3.9+
- PostgreSQL 15+ (base de datos `ticketwave_cors`)
- Redis 7+ (el saga y el caché persisten ahí)
- RabbitMQ (opcional, solo con el perfil `rabbitmq`)

## Estructura

```
ticketwave-events/
 ├── src/main/java/com/ticketwave/
 │   ├── TicketwaveApplication.java
 │   ├── config/            # Security, JWT, OpenAPI, Cache, DataSeeder, EventBus
 │   ├── controller/        # Event, Payment, Ticket, User, Notification, Promotion, Fraud
 │   ├── application/       # Use cases y servicios + SagaRecoveryJob
 │   ├── domain/            # Entidades, enums, events, commands, bus y saga
 │   ├── repository/        # Contratos de acceso a datos
 │   ├── infrastructure/    # Repositorios JPA, bus (RabbitMQ/in-memory), clientes HTTP, security
 │   ├── exception/         # Excepciones + GlobalExceptionHandler
 │   └── modules/           # Fronteras modulares (preparación para microservicios)
 ├── src/main/resources/    # application.yml, application-local.yml
 ├── diagrams/c4model/      # Diagramas arquitectónicos (modelo C4)
 └── src/test/
```

## Diagramas C4

Los diagramas de arquitectura (modelo C4) están en `diagrams/c4model`:

| Diagrama | Archivo |
|----------|---------|
| C1 - Contexto del sistema | `diagrams/c4model/ticketwave-c1-context.drawio.svg` |
| C2 - Contenedores | `diagrams/c4model/ticketwave-c2-container.drawio.svg` |
| C3 - Saga cross-service | `diagrams/c4model/ticketwave-c3-cross-service-saga.drawio.svg` |
| C3 - Digital ticket service | `diagrams/c4model/ticketwave-c3-digital-ticket-service.drawio.svg` |
| C3 - Búsqueda de eventos | `diagrams/c4model/ticketwave-c3-event-search.drawio.svg` |
| C3 - Notifications service | `diagrams/c4model/ticketwave-c3-notifications-service.drawio.svg` |
| C3 - Payment service | `diagrams/c4model/ticketwave-c3-payment-service.drawio.svg` |
| C3 - Promotions service | `diagrams/c4model/ticketwave-c3-promotions-service.drawio.svg` |
| C3 - Flujo de compra | `diagrams/c4model/ticketwave-c3-purchase-flow.drawio.svg` |
| C3 - Reembolsos y cancelaciones | `diagrams/c4model/ticketwave-c3-refunds-cancellations.drawio.svg` |
| C3 - Saga orchestrator | `diagrams/c4model/ticketwave-c3-saga-orchestrator.drawio.svg` |

## Ejecución

```bash
# Desarrollo local (H2 en memoria, bus en memoria, sin Redis externo)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# PostgreSQL + Redis, configuración por variables de entorno
$env:DB_URL="jdbc:postgresql://localhost:5432/ticketwave_cors"
$env:DB_USERNAME="postgres"; $env:DB_PASSWORD="postgres"
$env:REDIS_HOST="localhost"; $env:REDIS_PORT="6379"
$env:JWT_SECRET="<secreto-de-32-bytes>"
mvn spring-boot:run
```

Por defecto (sin perfil activo) la app usa PostgreSQL + Redis y el bus **en memoria**.
Para activar el transporte con RabbitMQ:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=rabbitmq
```

## Docker (dependencias externas)

Contenedores independientes (puedes iniciarlos por separado o todos juntos):

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

Enlaces externos:

- RabbitMQ Management: http://localhost:15672/
- Prometheus Targets: http://localhost:9090/targets
- Grafana: http://localhost:3000/

## Configuración por variables de entorno (application.yml)

| Variable             | Default                              | Uso                              |
|----------------------|--------------------------------------|----------------------------------|
| `DB_URL`             | `jdbc:postgresql://localhost:5432/ticketwave_cors` | Datasource JDBC |
| `DB_USERNAME`        | `postgres`                           | Usuario de la base de datos      |
| `DB_PASSWORD`        | `postgres`                           | Contraseña de la base de datos   |
| `REDIS_HOST`         | `localhost`                          | Host de Redis                    |
| `REDIS_PORT`         | `6379`                               | Puerto de Redis                  |
| `JWT_SECRET`         | valor por defecto de desarrollo      | Secreto JWT (mínimo 32 bytes)    |
| `JWT_EXPIRATION`     | `86400000`                           | Expiración del token (ms)        |
| `ORDER_TTL_MINUTES`  | `15`                                 | TTL de la orden (reserva)        |
| `INTERNAL_TOKEN`     | `change-me-internal-token`           | Token para llamadas servicio-a-servicio |
| `ORDER_SERVICE_URL`  | `http://localhost:8090`              | URL del servicio ticketorder-write |

## Monolito legacy

Swagger UI: http://localhost:8081/swagger-ui/index.html#/Events/search

## Arquitectura CQRS (read / write)

Documentación Swagger de los servicios CQRS de la plataforma:

| Servicio | Documentación Swagger |
|----------|----------------------|
| **Read (reportes)** | http://localhost:8091/swagger-ui/index.html#/report-controller/allReports |
| **Write (ticket orders - reserve)** | http://localhost:8093/swagger-ui/index.html#/Ticket%20Orders/reserve |

## Credenciales de demostración (seed automático, perfil `local`)

| Usuario | Contraseña | Rol   |
|---------|------------|-------|
| admin   | admin1234  | ADMIN |
| user    | user1234   | USER  |

## Endpoints principales

| Método | Ruta                                   | Descripción                                  | Acceso  |
|--------|----------------------------------------|----------------------------------------------|---------|
| GET    | `/api/events`                          | Búsqueda paginada (ciudad, artista, venue, fecha) | Público |
| GET    | `/api/events/all`                      | Lista completa de eventos                    | Público |
| GET    | `/api/events/{id}`                     | Detalle de un evento                         | Público |
| POST   | `/api/events`                          | Crear evento                                 | ADMIN   |
| PUT    | `/api/events/{id}`                     | Actualizar evento                            | ADMIN   |
| DELETE | `/api/events/{id}`                     | Cancelar evento                              | ADMIN   |
| POST   | `/api/events/{id}/reserve`             | Reservar capacidad                           | ADMIN   |
| POST   | `/api/events/{id}/release`             | Liberar capacidad                            | ADMIN   |
| POST   | `/api/payments`                        | Confirmar reserva y crear pago               | Autenticado |
| GET    | `/api/payments/order/{orderId}`        | Consultar pago por orden                     | Autenticado |
| GET    | `/api/tickets/{id}`                    | Detalle de ticket                            | Autenticado |
| GET    | `/api/tickets/order/{orderId}`         | Tickets de una orden                         | Autenticado |
| POST   | `/api/tickets/validate`                | Validar ticket en venue                      | ADMIN   |
| POST   | `/api/tickets/{id}/refund`             | Reembolsar ticket                            | Autenticado |
| POST   | `/api/users/register`                  | Registro                                     | Público |
| POST   | `/api/users/login`                     | Login → JWT                                  | Público |
| GET    | `/api/users/me`                        | Perfil del usuario autenticado               | Autenticado |
| GET    | `/api/promotions`                      | Promociones activas                          | Público |
| POST   | `/api/promotions`                      | Crear promoción                              | ADMIN   |
| POST   | `/api/promotions/{code}/quote`         | Calcular descuento                           | ADMIN   |
| POST   | `/api/promotions/{code}/increment-usage` | Incrementar uso                             | ADMIN   |
| GET    | `/api/notifications`                   | Notificaciones del usuario                   | Autenticado |
| PATCH  | `/api/notifications/{id}/read`         | Marcar notificación como leída               | Autenticado |
| GET    | `/api/fraud/check`                     | Evaluación de riesgo de fraude               | Autenticado |
| POST   | `/api/fraud/guard`                     | Bloquear intentos por usuario/IP             | ADMIN   |
| POST   | `/api/fraud/orders`                    | Marcar orden como fraudulenta                | ADMIN   |

## Flujo de adquisición (saga TicketOrder)

1. `ticketorder-write` publica `TicketOrderCreated` → el orquestador arranca el saga.
2. `ProcessPaymentCommand` → al confirmarse se emite el pago y se autoriza.
3. `IssueTicketCommand` → se emiten los tickets digitales con código QR (`TicketIssued`).
4. `NotifyOrderCommand` → se envían las notificaciones al usuario.
5. Compensaciones: pago fallido cancela la orden; entrega de ticket fallida reembolsa el pago; ambos convergen en `COMPENSATED`.
6. Los snapshots del saga se persisten en Redis (`SagaStateRepository`) y `SagaRecoveryJob` reanuda los sagas interrumpidos (habilitable con `ticketwave.saga.recovery-enabled`).

## Bus de eventos / comandos

- Perfil `rabbitmq`: transporte real sobre RabbitMQ (`RabbitMQEventBusAdapter` / `RabbitMQCommandBusAdapter`).
- Cualquier otro perfil (o ninguno): dobles en memoria (`InMemoryEventBus` / `InMemoryCommandBus`), sin broker.

## Pruebas

```bash
mvn test
```

Los tests usan el perfil `test` (H2 en memoria + bus en memoria).

## Seguridad

- JWT bearer token emitido en `/api/users/login` y `/api/users/register`.
- Endpoints administrativos protegidos con `@PreAuthorize("hasRole('ADMIN')")`.
- Llamadas servicio-a-servicio autenticadas con header `X-Internal-Token`.
- Detección de fraude: límite de intentos por usuario/IP en Redis, prevención de órdenes duplicadas.