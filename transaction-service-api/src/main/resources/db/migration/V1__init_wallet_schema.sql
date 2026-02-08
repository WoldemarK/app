-- =============================
-- Extensions
-- =============================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================
-- wallet_types (broadcast)
-- =============================
CREATE TABLE IF NOT EXISTS wallet_types
(
    uid           UUID PRIMARY KEY     DEFAULT uuid_generate_v4(),
    created_at    TIMESTAMP   NOT NULL DEFAULT now(),
    modified_at   TIMESTAMP,
    name          VARCHAR(32) NOT NULL,
    currency_code VARCHAR(3)  NOT NULL,
    status        VARCHAR(18) NOT NULL,
    archived_at   TIMESTAMP,
    user_type     VARCHAR(15),
    creator       VARCHAR(255),
    modifier      VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_wallet_types_status
    ON wallet_types (status);

-- =============================
-- wallets (sharded by user_uid)
-- =============================
CREATE TABLE IF NOT EXISTS wallets
(
    uid             UUID PRIMARY KEY     DEFAULT uuid_generate_v4(),
    created_at      TIMESTAMP   NOT NULL DEFAULT now(),
    modified_at     TIMESTAMP,
    name            VARCHAR(32) NOT NULL,
    wallet_type_uid UUID        NOT NULL,
    user_uid        UUID        NOT NULL,
    status          VARCHAR(30) NOT NULL,
    balance         DECIMAL     NOT NULL DEFAULT 0.0,
    archived_at     TIMESTAMP,
    CONSTRAINT fk_wallet_wallet_type
        FOREIGN KEY (wallet_type_uid)
            REFERENCES wallet_types (uid)
);

CREATE INDEX IF NOT EXISTS idx_wallets_user_uid
    ON wallets (user_uid);

CREATE INDEX IF NOT EXISTS idx_wallets_status
    ON wallets (status);

-- =============================
-- payment_type enum
-- =============================
DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM pg_type
                       WHERE typname = 'payment_type') THEN
            CREATE TYPE payment_type AS ENUM (
                'DEPOSIT',
                'WITHDRAWAL',
                'TRANSFER'
                );
        END IF;
    END
$$;

-- =============================
-- transactions (sharded by user_uid)
-- =============================
CREATE TABLE IF NOT EXISTS transactions
(
    uid               UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    created_at        TIMESTAMP    NOT NULL DEFAULT now(),
    modified_at       TIMESTAMP,
    user_uid          UUID         NOT NULL,
    wallet_uid        UUID         NOT NULL,
    amount            DECIMAL      NOT NULL DEFAULT 0.0,
    type              payment_type NOT NULL,
    status            VARCHAR(32)  NOT NULL,
    comment           VARCHAR(256),
    fee               DECIMAL,
    target_wallet_uid UUID,
    payment_method_id BIGINT,
    failure_reason    VARCHAR(256)
);

CREATE INDEX IF NOT EXISTS idx_transactions_user_uid
    ON transactions (user_uid);

CREATE INDEX IF NOT EXISTS idx_transactions_wallet_uid
    ON transactions (wallet_uid);

CREATE INDEX IF NOT EXISTS idx_transactions_created_at
    ON transactions (created_at);
