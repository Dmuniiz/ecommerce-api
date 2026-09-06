CREATE TABLE categories (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                            name VARCHAR(100) NOT NULL,
                            description TEXT,

                            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);