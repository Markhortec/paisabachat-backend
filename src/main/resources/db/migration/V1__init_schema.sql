-- ─────────────────────────────────────────
-- PaisaBachat Database Schema - V1
-- ─────────────────────────────────────────

-- ── EXTENSIONS ──
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ── USERS ──
CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    firebase_uid        VARCHAR(128) UNIQUE,
    email               VARCHAR(255) UNIQUE,
    name                VARCHAR(100),
    phone               VARCHAR(20),
    avatar_url          TEXT,
    role                VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
    tier                VARCHAR(20)  NOT NULL DEFAULT 'FREE',
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    is_email_verified   BOOLEAN      NOT NULL DEFAULT FALSE,
    streak_current      INTEGER      NOT NULL DEFAULT 0,
    streak_longest      INTEGER      NOT NULL DEFAULT 0,
    streak_last_date    DATE,
    xp_total            BIGINT       NOT NULL DEFAULT 0,
    level               INTEGER      NOT NULL DEFAULT 1,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── GOALS ──
CREATE TABLE goals (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title           VARCHAR(50)  NOT NULL,
    description     VARCHAR(200),
    target_amount   NUMERIC(12,2) NOT NULL CHECK (target_amount > 0),
    saved_amount    NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (saved_amount >= 0),
    deadline        DATE         NOT NULL,
    priority        VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM',
    icon_name       VARCHAR(50)  NOT NULL DEFAULT 'default',
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    is_synced       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_priority CHECK (priority IN ('LOW','MEDIUM','HIGH')),
    CONSTRAINT chk_status   CHECK (status   IN ('ACTIVE','COMPLETED','ARCHIVED'))
);

-- ── CONTRIBUTIONS ──
CREATE TABLE contributions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    goal_id         UUID          NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    user_id         UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount          NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    note            VARCHAR(200),
    contributed_at  DATE          NOT NULL DEFAULT CURRENT_DATE,
    is_synced       BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ── ACHIEVEMENTS ──
CREATE TABLE achievements (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    achievement_key VARCHAR(50)  NOT NULL,
    unlocked        BOOLEAN      NOT NULL DEFAULT FALSE,
    unlocked_at     TIMESTAMP,
    progress        INTEGER      NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, achievement_key)
);

-- ── REFRESH TOKENS ──
CREATE TABLE refresh_tokens (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash      VARCHAR(255) NOT NULL UNIQUE,
    device_info     VARCHAR(255),
    ip_address      VARCHAR(45),
    expires_at      TIMESTAMP    NOT NULL,
    is_revoked      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── AUDIT LOG ──
CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID         REFERENCES users(id) ON DELETE SET NULL,
    action          VARCHAR(100) NOT NULL,
    entity_type     VARCHAR(50),
    entity_id       UUID,
    old_value       TEXT,
    new_value       TEXT,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(255),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── NOTIFICATION SETTINGS ──
CREATE TABLE notification_settings (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id                 UUID    NOT NULL REFERENCES users(id) ON DELETE CASCADE UNIQUE,
    daily_reminder_enabled  BOOLEAN NOT NULL DEFAULT TRUE,
    daily_reminder_time     TIME    NOT NULL DEFAULT '20:00:00',
    streak_alerts_enabled   BOOLEAN NOT NULL DEFAULT TRUE,
    goal_alerts_enabled     BOOLEAN NOT NULL DEFAULT TRUE,
    fcm_token               VARCHAR(255),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ─────────────────────────────────────────
-- INDEXES — Performance
-- ─────────────────────────────────────────
CREATE INDEX idx_goals_user_id         ON goals(user_id);
CREATE INDEX idx_goals_status          ON goals(status);
CREATE INDEX idx_contributions_goal_id ON contributions(goal_id);
CREATE INDEX idx_contributions_user_id ON contributions(user_id);
CREATE INDEX idx_contributions_date    ON contributions(contributed_at);
CREATE INDEX idx_audit_logs_user_id    ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_refresh_tokens_user   ON refresh_tokens(user_id);
CREATE INDEX idx_achievements_user     ON achievements(user_id);

-- ─────────────────────────────────────────
-- AUTO UPDATE updated_at FUNCTION
-- ─────────────────────────────────────────
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- TRIGGERS
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_goals_updated_at
    BEFORE UPDATE ON goals
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_contributions_updated_at
    BEFORE UPDATE ON contributions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_notification_settings_updated_at
    BEFORE UPDATE ON notification_settings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();