CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    telegram_id BIGINT NOT NULL UNIQUE,
    chat_id BIGINT,
    username VARCHAR(255),
    first_name VARCHAR(255),
    video_credits INT DEFAULT 0,
    state VARCHAR(50),
    last_text TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    telegram_id BIGINT NOT NULL,
    yookassa_payment_id VARCHAR(255) NOT NULL UNIQUE,
    amount INT,
    currency VARCHAR(10),
    status VARCHAR(50),
    idempotence_key VARCHAR(100) NOT NULL,
    confirmation_url TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    telegram_id BIGINT NOT NULL,
    fal_job_id VARCHAR(255),
    input_text TEXT,
    status VARCHAR(50),
    video_url TEXT,
    error_message TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_payments_telegram ON payments(telegram_id);
CREATE INDEX idx_jobs_telegram ON jobs(telegram_id);
