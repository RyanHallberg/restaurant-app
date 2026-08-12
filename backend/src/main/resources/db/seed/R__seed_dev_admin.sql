-- DEV/TEST ONLY. This location (classpath:db/seed) is added to
-- spring.flyway.locations only in the local profile, so it never runs in prod.
-- Repeatable + ON CONFLICT so it's idempotent across restarts.
-- Credentials: admin@sageandember.example / admin123
INSERT INTO users (email, password_hash, full_name, role)
VALUES ('admin@sageandember.example',
        '$2y$10$e8kyNsjgtu2NX4A76Ui9ceKtzaNS.11VQHLxK6gS6JOCnxjZJ6KgO',
        'Site Admin',
        'ADMIN')
ON CONFLICT (email) DO NOTHING;
