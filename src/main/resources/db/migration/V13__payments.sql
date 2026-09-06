CREATE TABLE payments (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          order_id UUID NOT NULL unique,
                          amount NUMERIC(10,2) NOT NULL,
                          status VARCHAR(30) NOT NULL,
                          provider_checkout_session_id VARCHAR(255) UNIQUE,
                          provider VARCHAR(50) not null,
                          provider_customer_id VARCHAR(255),
                          currency VARCHAR(10) not null,
                          failure_reason TEXT,
                          paid_at TIMESTAMP WITH TIME ZONE,
                          version BIGINT NOT NULL DEFAULT 0,

                          transaction_id VARCHAR(255),
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL ,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP not null ,

                          FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_created_at ON payments(created_at);
CREATE INDEX idx_payments_provider ON payments(provider);
CREATE UNIQUE INDEX uk_checkout_session ON payments(provider_checkout_session_id);
