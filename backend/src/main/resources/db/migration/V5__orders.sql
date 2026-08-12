CREATE TABLE orders (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id           BIGINT       NOT NULL REFERENCES users (id),
    status            VARCHAR(20)  NOT NULL CHECK (status IN ('PLACED', 'PREPARING', 'READY', 'COMPLETED', 'CANCELLED')),
    total_cents       INT          NOT NULL CHECK (total_cents >= 0),
    payment_reference VARCHAR(50)  NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_user ON orders (user_id);
CREATE INDEX idx_orders_status ON orders (status);

-- item_name and price_cents are snapshots taken at order time, so history
-- stays truthful when the menu changes; menu_item_id may go NULL on delete.
CREATE TABLE order_items (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id     BIGINT       NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    menu_item_id BIGINT       REFERENCES menu_items (id) ON DELETE SET NULL,
    item_name    VARCHAR(100) NOT NULL,
    price_cents  INT          NOT NULL,
    quantity     INT          NOT NULL CHECK (quantity > 0)
);

CREATE INDEX idx_order_items_order ON order_items (order_id);
