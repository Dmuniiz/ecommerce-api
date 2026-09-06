CREATE TABLE payment_transactions (

        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        payment_id UUID not null,
        type VARCHAR(100) NOT NULL ,
        status VARCHAR(50) not null,
        provider_event_id VARCHAR(255),
        provider_transaction_id VARCHAR(255),
        raw_payload jsonb,
        error_message TEXT,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP not null,

    CONSTRAINT fk_transaction_payment FOREIGN KEY (payment_id) REFERENCES payments(id)
);

CREATE INDEX idx_payment_transactions_payment_id
    on payment_transactions(payment_id);

CREATE INDEX idx_payment_transactions_type
    ON payment_transactions(type);

CREATE INDEX idx_payment_transactions_created_at
    ON payment_transactions(created_at);

CREATE INDEX idx_payment_transactions_provider_event_id
    ON payment_transactions(provider_event_id);