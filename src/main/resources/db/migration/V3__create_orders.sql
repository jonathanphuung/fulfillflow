CREATE TABLE orders (
    id UUID PRIMARY KEY,
    order_number VARCHAR(40) NOT NULL UNIQUE,
    customer_name VARCHAR(160) NOT NULL,
    status VARCHAR(24) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT valid_order_status CHECK (
        status IN ('PENDING', 'READY_TO_PICK', 'PICKING', 'COMPLETED', 'CANCELLED')
    )
);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id),
    reservation_id UUID NOT NULL UNIQUE REFERENCES inventory_reservations(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    status VARCHAR(24) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT valid_item_status CHECK (
        status IN ('RESERVED', 'PICKED', 'UNAVAILABLE', 'CANCELLED')
    )
);

CREATE INDEX idx_orders_status_created ON orders (status, created_at);
CREATE INDEX idx_order_items_order ON order_items (order_id);
CREATE INDEX idx_order_items_product ON order_items (product_id);
