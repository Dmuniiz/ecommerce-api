CREATE TABLE refresh_tokens (
                                id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY NOT NULL UNIQUE,

                                user_id UUID NOT NULL,
                                token VARCHAR(512) NOT NULL UNIQUE,

                                expiration_time TIMESTAMP WITH TIME ZONE NOT NULL,

                                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);