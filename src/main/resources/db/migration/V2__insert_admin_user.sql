CREATE EXTENSION IF NOT EXISTS "pgcrypto";

INSERT INTO tb_user (user_id, email, password, role)
VALUES (
        gen_random_uuid(),
        'admin@acaminho.com',
        '$2a$12$fRnYsKuA4lVYDdpQZtCyBOBYR/2OTgUuZylLMH2hPtuBJQdEpJQf2',
        'ADMIN'
);