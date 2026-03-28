-- TABLES
CREATE TABLE IF NOT EXISTS users (
    id                      UUID        PRIMARY KEY DEFAULT uuidv7(),
    auth_id                 TEXT        NOT NULL UNIQUE,
    email                   TEXT        NOT NULL UNIQUE,
    time_zone               TEXT        NOT NULL,
    daily_mail_enabled      BOOLEAN     NOT NULL DEFAULT false,
    daily_mail_time         TIME        NOT NULL DEFAULT '06:00:00',
    created_at              timestamptz NOT NULL DEFAULT now(),
    updated_at              timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS documents (
    id                      UUID        PRIMARY KEY DEFAULT uuidv7(),
    owner_id                UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_name               TEXT        NOT NULL,
    tags                    TEXT[]      NOT NULL,
    content                 TEXT        NOT NULL,
    questions               TEXT[]      NOT NULL DEFAULT '{}',
    content_search_vector   tsvector    GENERATED ALWAYS AS (to_tsvector('simple', content)) STORED,
    created_at              timestamptz NOT NULL DEFAULT now(),
    updated_at              timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_documents_user_file_name UNIQUE (owner_id, file_name)
);

-- INDICES
CREATE INDEX IF NOT EXISTS idx_users_daily_mail_enabled
    ON users (id)
    WHERE daily_mail_enabled = true;

CREATE INDEX IF NOT EXISTS idx_documents_tags
    ON documents USING GIN (tags);

CREATE INDEX IF NOT EXISTS idx_documents_content_search_vector
    ON documents USING GIN (content_search_vector);

CREATE INDEX IF NOT EXISTS idx_documents_has_questions
    ON documents (owner_id)
    WHERE questions <> '{}';

-- FUNCTIONS
CREATE OR REPLACE FUNCTION fun_set_updated_at()
    RETURNS trigger AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- TRIGGERS
DROP TRIGGER IF EXISTS trg_users_set_updated_at ON users;
CREATE TRIGGER trg_users_set_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
EXECUTE FUNCTION fun_set_updated_at();

DROP TRIGGER IF EXISTS trg_documents_set_updated_at ON documents;
CREATE TRIGGER trg_documents_set_updated_at
    BEFORE UPDATE ON documents
    FOR EACH ROW
EXECUTE FUNCTION fun_set_updated_at();
