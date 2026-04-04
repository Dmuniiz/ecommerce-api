INSERT INTO roles (role_name)
VALUES ('ADMIN'), ('USER')
ON CONFLICT (role_name) DO NOTHING;