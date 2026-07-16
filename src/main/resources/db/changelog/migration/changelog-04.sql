CREATE TABLE IF NOT EXISTS production_batches
(
    batch_id               UUID                     NOT NULL PRIMARY KEY,
    workspace_id           UUID                     NOT NULL,
    name                   VARCHAR(160)             NOT NULL,
    kind                   VARCHAR(32)              NOT NULL,
    status                 VARCHAR(32)              NOT NULL,
    total_volume_liters    NUMERIC(15, 3)           NOT NULL,

    sugar_specific_gravity NUMERIC(12, 9)           NOT NULL,
    acidity_g_l_as_malic   NUMERIC(12, 6)           NOT NULL,
    tannin_mg_l_tae        NUMERIC(12, 4),
    yan_mg_n_l             NUMERIC(12, 4),

    note                   VARCHAR(2000),

    version                BIGINT                   NOT NULL DEFAULT 0,
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at             TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT chk_production_batches_total_volume
        CHECK (total_volume_liters > 0),
    CONSTRAINT fk_production_batches_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (workspace_id)
);

CREATE INDEX IF NOT EXISTS idx_production_batches_workspace_id
    ON production_batches (workspace_id);

CREATE TABLE IF NOT EXISTS production_batch_candidates
(
    candidate_id                  UUID                     NOT NULL PRIMARY KEY,
    workspace_id                  UUID                     NOT NULL,
    status                        VARCHAR(32)              NOT NULL,
    sources_json                  TEXT                     NOT NULL,
    total_volume_liters           NUMERIC(15, 3)           NOT NULL,

    sugar_specific_gravity        NUMERIC(12, 9)           NOT NULL,
    acidity_g_l_as_malic          NUMERIC(12, 6)           NOT NULL,
    tannin_mg_l_tae               NUMERIC(12, 4),
    yan_mg_n_l                    NUMERIC(12, 4),

    normalizer_code               VARCHAR(64),
    target_distance_json          TEXT,
    warnings_json                 TEXT                     NOT NULL,
    confirmed_production_batch_id UUID,

    version                       BIGINT                   NOT NULL DEFAULT 0,
    created_at                    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at                    TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT chk_production_batch_candidates_total_volume
        CHECK (total_volume_liters > 0),
    CONSTRAINT fk_production_batch_candidates_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (workspace_id),
    CONSTRAINT fk_production_batch_candidates_confirmed_batch
        FOREIGN KEY (confirmed_production_batch_id) REFERENCES production_batches (batch_id),
    CONSTRAINT uq_production_batch_candidates_confirmed_batch
        UNIQUE (confirmed_production_batch_id),
    CONSTRAINT chk_production_batch_candidates_confirmation
        CHECK (
            (status = 'AVAILABLE' AND confirmed_production_batch_id IS NULL)
                OR (status = 'CONFIRMED' AND confirmed_production_batch_id IS NOT NULL)
        )
);

CREATE INDEX IF NOT EXISTS idx_production_batch_candidates_workspace_id
    ON production_batch_candidates (workspace_id);
