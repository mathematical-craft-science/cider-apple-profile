CREATE TABLE IF NOT EXISTS feedstock_samples
(
    sample_id                    UUID                     NOT NULL PRIMARY KEY,
    cider_maker_id               UUID                     NOT NULL,
    name                         VARCHAR(160)             NOT NULL,
    type                         VARCHAR(32)              NOT NULL,
    collected_at                 TIMESTAMP WITH TIME ZONE NOT NULL,
    note                         VARCHAR(2000),

    sugar_kind                   VARCHAR(32),
    sugar_exact_sg               NUMERIC(12, 4),
    sugar_min_sg                 NUMERIC(12, 4),
    sugar_max_sg                 NUMERIC(12, 4),
    sugar_mean_sg                NUMERIC(12, 4),
    sugar_median_sg              NUMERIC(12, 4),
    sugar_standard_deviation     NUMERIC(12, 4),
    sugar_sample_size            INTEGER,

    ph_kind                      VARCHAR(32),
    ph_exact                     NUMERIC(12, 4),
    ph_min                       NUMERIC(12, 4),
    ph_max                       NUMERIC(12, 4),
    ph_mean                      NUMERIC(12, 4),
    ph_median                    NUMERIC(12, 4),
    ph_standard_deviation        NUMERIC(12, 4),
    ph_sample_size               INTEGER,

    ta_kind                      VARCHAR(32),
    ta_exact_g_l_as_malic        NUMERIC(12, 4),
    ta_min_g_l_as_malic          NUMERIC(12, 4),
    ta_max_g_l_as_malic          NUMERIC(12, 4),
    ta_mean_g_l_as_malic         NUMERIC(12, 4),
    ta_median_g_l_as_malic       NUMERIC(12, 4),
    ta_standard_deviation        NUMERIC(12, 4),
    ta_sample_size               INTEGER,

    tanninity_kind               VARCHAR(32),
    tanninity_exact_mg_l_tae     NUMERIC(12, 4),
    tanninity_min_mg_l_tae       NUMERIC(12, 4),
    tanninity_max_mg_l_tae       NUMERIC(12, 4),
    tanninity_mean_mg_l_tae      NUMERIC(12, 4),
    tanninity_median_mg_l_tae    NUMERIC(12, 4),
    tanninity_standard_deviation NUMERIC(12, 4),
    tanninity_sample_size        INTEGER,

    version                      BIGINT                   NOT NULL DEFAULT 0,
    created_at                   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at                   TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_feedstock_samples_cider_maker_id
    ON feedstock_samples (cider_maker_id);

CREATE INDEX IF NOT EXISTS idx_feedstock_samples_cider_maker_collected_at
    ON feedstock_samples (cider_maker_id, collected_at DESC);

CREATE INDEX IF NOT EXISTS idx_feedstock_samples_cider_maker_type
    ON feedstock_samples (cider_maker_id, type);

CREATE INDEX IF NOT EXISTS idx_feedstock_samples_cider_maker_name
    ON feedstock_samples (cider_maker_id, name);

CREATE INDEX IF NOT EXISTS idx_feedstock_samples_cider_maker_lower_name
    ON feedstock_samples (cider_maker_id, LOWER(name));