CREATE TABLE menu_categories (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name          VARCHAR(50) NOT NULL UNIQUE,
    display_order INT         NOT NULL
);

CREATE TABLE menu_items (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_id BIGINT       NOT NULL REFERENCES menu_categories (id) ON DELETE RESTRICT,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    price_cents INT          NOT NULL CHECK (price_cents >= 0),
    image_url   TEXT,
    available   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_menu_items_category ON menu_items (category_id);
