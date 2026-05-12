CREATE TABLE orders (
                        id UUID PRIMARY KEY,
                        user_id UUID NOT NULL,
                        status VARCHAR(50) NOT NULL DEFAULT 'PENDING',


                        shipping_street VARCHAR(255),
                        shipping_number VARCHAR(20),
                        shipping_complement VARCHAR(255),
                        shipping_neighborhood VARCHAR(100),
                        shipping_city VARCHAR(100),
                        shipping_state VARCHAR(50),
                        shipping_zip_code VARCHAR(20),
                        shipping_country VARCHAR(100),


                        billing_street VARCHAR(255),
                        billing_number VARCHAR(20),
                        billing_complement VARCHAR(255),
                        billing_neighborhood VARCHAR(100),
                        billing_city VARCHAR(100),
                        billing_state VARCHAR(50),
                        billing_zip_code VARCHAR(20),
                        billing_country VARCHAR(100),
                        currency VARCHAR(10) not null,

                        total_amount NUMERIC(10, 2) NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Index opcional para busca rápida por usuário
CREATE INDEX idx_orders_user_id ON orders(user_id);