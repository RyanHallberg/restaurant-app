CREATE TABLE reservations (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id           BIGINT,
    customer_name     VARCHAR(100) NOT NULL,
    customer_email    VARCHAR(255) NOT NULL,
    customer_phone    VARCHAR(30)  NOT NULL,
    party_size        INT          NOT NULL CHECK (party_size BETWEEN 1 AND 12),
    reservation_date  DATE         NOT NULL,
    reservation_time  TIME         NOT NULL,
    status            VARCHAR(20)  NOT NULL CHECK (status IN ('CONFIRMED', 'CANCELLED', 'COMPLETED')),
    confirmation_code VARCHAR(12)  NOT NULL UNIQUE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- user_id gains its FK when the users table arrives with auth (M6).
CREATE INDEX idx_reservations_slot ON reservations (reservation_date, reservation_time);
