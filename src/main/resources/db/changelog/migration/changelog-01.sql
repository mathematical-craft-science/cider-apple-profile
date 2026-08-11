CREATE TABLE IF NOT EXISTS apple_varieties
(
    variety_id                   UUID                     NOT NULL PRIMARY KEY,
    name                         VARCHAR(120)             NOT NULL,

    sugar_kind                   VARCHAR(32),
    sugar_exact_sg               NUMERIC(5, 4),
    sugar_min_sg                 NUMERIC(5, 4),
    sugar_max_sg                 NUMERIC(5, 4),

    ph_kind                      VARCHAR(32),
    ph_exact                     NUMERIC(5, 3),
    ph_min                       NUMERIC(5, 3),
    ph_max                       NUMERIC(5, 3),

    ta_kind                      VARCHAR(32),
    ta_exact_g_l_as_malic        NUMERIC(5, 3),
    ta_min_g_l_as_malic          NUMERIC(5, 3),
    ta_max_g_l_as_malic          NUMERIC(5, 3),

    tannin_kind               VARCHAR(32),
    tannin_exact_mg_l_tae     NUMERIC(6, 1),
    tannin_min_mg_l_tae       NUMERIC(6, 1),
    tannin_max_mg_l_tae       NUMERIC(6, 1),

    chemical_profile_source     VARCHAR(255),
    chemical_profile_confidence VARCHAR(32),

    created_at                   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at                   TIMESTAMP WITH TIME ZONE NOT NULL,
    version                      BIGINT                   NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_apple_varieties_lower_name
    ON apple_varieties (lower(name));

CREATE INDEX IF NOT EXISTS idx_apple_varieties_name_variety_id
    ON apple_varieties (name, variety_id);
