ALTER TABLE refresh_tokens
    ADD COLUMN IF NOT EXISTS token_family_id UUID,
    ADD COLUMN IF NOT EXISTS revoked_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS replaced_by_token_id BIGINT,
    ADD COLUMN IF NOT EXISTS device_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS ip_address VARCHAR(45),
    ADD COLUMN IF NOT EXISTS user_agent VARCHAR(512);

UPDATE refresh_tokens
SET token_family_id = gen_random_uuid()
WHERE token_family_id IS NULL;

ALTER TABLE refresh_tokens
    ALTER COLUMN token_family_id SET NOT NULL;

ALTER TABLE refresh_tokens
    ADD CONSTRAINT fk_refresh_tokens_replaced_by
        FOREIGN KEY (replaced_by_token_id)
        REFERENCES refresh_tokens(id)
        ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_refresh_token_family
    ON refresh_tokens(token_family_id);
