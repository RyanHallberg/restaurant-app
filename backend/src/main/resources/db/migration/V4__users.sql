CREATE TABLE users (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(72)  NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'CUSTOMER')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

ALTER TABLE reservations
    ADD CONSTRAINT fk_reservations_user FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX idx_reservations_user ON reservations (user_id);

-- No admin is seeded here on purpose: this migration runs in prod, and a
-- committed credential in a prod-facing migration is a security hole. The dev
-- admin lives in db/seed (local/test Flyway locations only); prod gets an
-- operator-created admin.
