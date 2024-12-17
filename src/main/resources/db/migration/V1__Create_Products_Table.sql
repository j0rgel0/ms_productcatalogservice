CREATE TABLE products
(
    product_id         UUID PRIMARY KEY,
    name               VARCHAR(255)                NOT NULL,
    description        TEXT,
    price              DECIMAL(19, 2)              NOT NULL,
    category           VARCHAR(100),
    available_quantity INTEGER                     NOT NULL,
    attributes         JSONB,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
