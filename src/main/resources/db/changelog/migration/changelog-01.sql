CREATE TABLE  IF NOT EXISTS apple_varieties
(
    variety_id                   UUID                     NOT NULL PRIMARY KEY,
    name                         VARCHAR(120)             NOT NULL,

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

    created_at                   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at                   TIMESTAMP WITH TIME ZONE NOT NULL,
    version                      BIGINT                   NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_apple_varieties_lower_name
    ON apple_varieties (lower(name));