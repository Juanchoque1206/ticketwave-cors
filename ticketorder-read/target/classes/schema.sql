-- Read-model schema for the reporting service. Every table is created
-- idempotently (IF NOT EXISTS) on startup against the analytics cluster
-- (a read replica of the monolith ticketwave database plus this dedicated
-- read-model table). Tables without a natural shared key in the replica are
-- still declared here so the application can start against an empty DB.

CREATE TABLE IF NOT EXISTS order_reports (
    order_id                  UUID PRIMARY KEY,
    user_id                   UUID NOT NULL,
    event_id                  UUID NOT NULL,
    event_name                VARCHAR(150),
    quantity                  INTEGER NOT NULL,
    total_amount              NUMERIC(10, 2) NOT NULL,
    discount_amount           NUMERIC(10, 2) NOT NULL,
    status                    VARCHAR(20) NOT NULL,
    reserved_at               TIMESTAMPTZ NOT NULL,
    updated_at                TIMESTAMPTZ NOT NULL,
    paid_at                   TIMESTAMPTZ,
    payment_status            VARCHAR(20),
    provider_transaction_id   VARCHAR(100),
    ticket_count              INTEGER NOT NULL DEFAULT 0,
    refunded_amount           NUMERIC(10, 2) NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_order_report_user ON order_reports (user_id);
CREATE INDEX IF NOT EXISTS idx_order_report_event ON order_reports (event_id);
CREATE INDEX IF NOT EXISTS idx_order_report_status ON order_reports (status);
CREATE INDEX IF NOT EXISTS idx_order_report_reserved ON order_reports (reserved_at);

-- Monolith read-replica projections (read-only in this service).
CREATE TABLE IF NOT EXISTS app_users (
    id          UUID PRIMARY KEY,
    username    VARCHAR(50) NOT NULL,
    email       VARCHAR(120),
    full_name   VARCHAR(255),
    city        VARCHAR(100),
    role        VARCHAR(20) NOT NULL,
    enabled     BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS events (
    id              UUID PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    artist          VARCHAR(255),
    city            VARCHAR(100),
    event_date      TIMESTAMPTZ NOT NULL,
    base_price      NUMERIC(10, 2) NOT NULL,
    total_capacity  INTEGER NOT NULL,
    reserved_count  INTEGER NOT NULL,
    status          VARCHAR(20) NOT NULL,
    created_at      TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS payments (
    id                       UUID PRIMARY KEY,
    order_id                 UUID NOT NULL,
    provider                 VARCHAR(20) NOT NULL,
    status                   VARCHAR(20) NOT NULL,
    amount                   NUMERIC(10, 2) NOT NULL,
    provider_transaction_id  VARCHAR(100),
    paid_at                  TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS tickets (
    id            UUID PRIMARY KEY,
    qr_code       VARCHAR(64) NOT NULL UNIQUE,
    order_id      UUID NOT NULL,
    user_id       UUID NOT NULL,
    event_id      UUID NOT NULL,
    price         NUMERIC(10, 2) NOT NULL,
    seat          VARCHAR(50),
    status        VARCHAR(20) NOT NULL,
    issued_at     TIMESTAMPTZ,
    validated_at  TIMESTAMPTZ,
    refunded_at   TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS notifications (
    id           UUID PRIMARY KEY,
    user_id      UUID NOT NULL,
    type         VARCHAR(30) NOT NULL,
    channel      VARCHAR(20) NOT NULL,
    subject      VARCHAR(200) NOT NULL,
    body         VARCHAR(2000),
    read         BOOLEAN NOT NULL,
    created_at   TIMESTAMPTZ
);