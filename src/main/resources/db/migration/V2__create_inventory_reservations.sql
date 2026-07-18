CREATE TABLE inventory_reservations (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    status VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT valid_reservation_status CHECK (status IN ('ACTIVE', 'RELEASED', 'COMPLETED', 'EXPIRED'))
);

CREATE INDEX idx_reservations_product_status
    ON inventory_reservations (product_id, status);

CREATE INDEX idx_reservations_expiration
    ON inventory_reservations (status, expires_at);
