CREATE TABLE carts (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                       user_id UUID NOT NULL UNIQUE,

                       total_amount NUMERIC(10,2) NOT NULL,

                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);