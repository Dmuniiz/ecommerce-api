CREATE TABLE addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,

    street VARCHAR(255) NOT NULL,
    number VARCHAR(20) NOT NULL,
    complement VARCHAR(100),
    neighborhood VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,

    is_default BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_addresses_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE address_type (
    address_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,

    PRIMARY KEY (address_id, type),
    CONSTRAINT fk_address_types_address
    FOREIGN KEY (address_id) REFERENCES addresses(id) ON DELETE CASCADE
);

CREATE INDEX idx_addresses_user ON addresses(user_id);
CREATE INDEX idx_address_types_type ON address_type(type);