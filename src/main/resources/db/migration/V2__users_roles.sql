CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(150) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       cpf VARCHAR(14) UNIQUE,
                       phone_number VARCHAR(20),
                       birth_date DATE,

                       created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);


CREATE TABLE roles (
                       id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                       role_name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO roles (role_name)
VALUES ('ADMIN'), ('USER')
ON CONFLICT (role_name) DO NOTHING;


CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role_id BIGINT NOT NULL,

                            PRIMARY KEY (user_id, role_id),

                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);