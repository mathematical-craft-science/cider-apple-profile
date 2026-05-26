CREATE TABLE IF NOT EXISTS production_batches
(
    batch_id               UUID                     NOT NULL PRIMARY KEY,
    cider_maker_id         UUID                     NOT NULL,
    name                   VARCHAR(160)             NOT NULL,
    kind                   VARCHAR(32)              NOT NULL,
    status                 VARCHAR(32)              NOT NULL,
    total_volume_liters    NUMERIC(15, 3)           NOT NULL,

    sugar_specific_gravity NUMERIC(12, 9)           NOT NULL,
    acidity_g_l_as_malic   NUMERIC(12, 6)           NOT NULL,
    tannin_mg_l_tae        NUMERIC(12, 4),
    yan_mg_n_l             NUMERIC(12, 4),
    target_distance        NUMERIC(18, 9),

    note                   VARCHAR(2000),

    version                BIGINT                   NOT NULL DEFAULT 0,
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at             TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT chk_production_batches_total_volume
        CHECK (total_volume_liters > 0)
);

CREATE INDEX IF NOT EXISTS idx_production_batches_cider_maker_id
    ON production_batches (cider_maker_id);
