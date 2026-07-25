CREATE TABLE IF NOT EXISTS feedstock_batches
(
    batch_id                 UUID                     NOT NULL PRIMARY KEY,
    workspace_id             UUID                     NOT NULL,
    name                     VARCHAR(160)             NOT NULL,
    vessel                   VARCHAR(160),
    type                     VARCHAR(32)              NOT NULL,
    season_starts_on         DATE                     NOT NULL,
    season_ends_on           DATE                     NOT NULL,
    source                   VARCHAR(255),
    available_volume_liters  NUMERIC(15, 3)           NOT NULL,
    note                     VARCHAR(2000),
    deleted_at               TIMESTAMP WITH TIME ZONE,

    version                  BIGINT                   NOT NULL DEFAULT 0,
    created_at               TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at               TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT chk_feedstock_batches_season_dates
        CHECK (season_ends_on >= season_starts_on),
    CONSTRAINT chk_feedstock_batches_available_volume
        CHECK (available_volume_liters > 0),
    CONSTRAINT fk_feedstock_batches_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (workspace_id)
);

CREATE INDEX IF NOT EXISTS idx_feedstock_batches_workspace_id
    ON feedstock_batches (workspace_id);

CREATE INDEX IF NOT EXISTS idx_feedstock_batches_workspace_season
    ON feedstock_batches (workspace_id, season_starts_on, season_ends_on);

CREATE INDEX IF NOT EXISTS idx_feedstock_batches_workspace_season_batch
    ON feedstock_batches (workspace_id, season_starts_on, season_ends_on, batch_id);

CREATE INDEX IF NOT EXISTS idx_feedstock_batches_workspace_lower_name
    ON feedstock_batches (workspace_id, LOWER(name));

CREATE INDEX IF NOT EXISTS idx_feedstock_batches_active_workspace_id
    ON feedstock_batches (workspace_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_feedstock_batches_active_workspace_season
    ON feedstock_batches (workspace_id, season_starts_on, season_ends_on)
    WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS measurement_sets
(
    measurement_set_id UUID                     NOT NULL PRIMARY KEY,
    workspace_id       UUID                     NOT NULL,
    subject_type       VARCHAR(32)              NOT NULL,
    subject_id         UUID                     NOT NULL,
    lifecycle          VARCHAR(16)              NOT NULL,
    current_revision_id UUID                    NOT NULL,
    version            BIGINT                   NOT NULL DEFAULT 0,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT chk_measurement_sets_subject_type
        CHECK (subject_type IN ('FEEDSTOCK_BATCH', 'PRODUCTION_BATCH')),
    CONSTRAINT chk_measurement_sets_lifecycle
        CHECK (lifecycle IN ('ACTIVE', 'RETRACTED')),
    CONSTRAINT fk_measurement_sets_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (workspace_id)
);

CREATE TABLE IF NOT EXISTS measurement_set_revisions
(
    revision_id       UUID                     NOT NULL PRIMARY KEY,
    measurement_set_id UUID                   NOT NULL,
    revision_number   INTEGER                  NOT NULL,
    material_state_id UUID                     NOT NULL,
    name              VARCHAR(160)             NOT NULL,
    note              VARCHAR(2000),
    correction_reason VARCHAR(1000),
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    actor_reference   VARCHAR(255)             NOT NULL,

    CONSTRAINT ux_measurement_revisions_set_number
        UNIQUE (measurement_set_id, revision_number),
    CONSTRAINT chk_measurement_revisions_number
        CHECK (revision_number > 0),
    CONSTRAINT chk_measurement_revisions_correction_reason
        CHECK (
            (revision_number = 1 AND correction_reason IS NULL)
            OR (
                revision_number > 1
                AND correction_reason IS NOT NULL
                AND BTRIM(correction_reason) <> ''
            )
        ),
    CONSTRAINT fk_measurement_revisions_set
        FOREIGN KEY (measurement_set_id) REFERENCES measurement_sets (measurement_set_id)
);

CREATE TABLE IF NOT EXISTS measurement_records
(
    record_id              UUID                     NOT NULL PRIMARY KEY,
    revision_id            UUID                     NOT NULL,
    characteristic         VARCHAR(64)              NOT NULL,
    role                   VARCHAR(16)              NOT NULL,
    value_kind             VARCHAR(16)              NOT NULL,
    measured_at            TIMESTAMP WITH TIME ZONE NOT NULL,
    source_type            VARCHAR(32)              NOT NULL,
    source_name            VARCHAR(255),
    measurement_method     VARCHAR(255),
    source_reference       VARCHAR(1000),
    confidence             VARCHAR(32),
    point_value            NUMERIC(24, 12),
    lower_value            NUMERIC(24, 12),
    upper_value            NUMERIC(24, 12),
    derivation_method      VARCHAR(32),

    CONSTRAINT ux_measurement_records_identity_scope
        UNIQUE (record_id, revision_id, characteristic, role),
    CONSTRAINT chk_measurement_records_role
        CHECK (role IN ('OBSERVATION', 'RESULT')),
    CONSTRAINT chk_measurement_records_value_kind
        CHECK (value_kind IN ('POINT', 'INTERVAL')),
    CONSTRAINT chk_measurement_records_characteristic
        CHECK (characteristic IN (
            'SPECIFIC_GRAVITY',
            'PH',
            'TITRATABLE_ACIDITY',
            'TANNIN',
            'YAN',
            'TEMPERATURE',
            'TURBIDITY'
        )),
    CONSTRAINT chk_measurement_records_source_type
        CHECK (source_type IN (
            'USER',
            'INSTRUMENT',
            'LAB',
            'IMPORT',
            'CURATOR',
            'INSTITUTE',
            'DERIVED'
        )),
    CONSTRAINT chk_measurement_records_confidence
        CHECK (
            confidence IS NULL
            OR confidence IN ('LOW', 'MEDIUM', 'HIGH', 'LAB_VERIFIED')
        ),
    CONSTRAINT chk_measurement_records_derivation
        CHECK (
            (
                derivation_method IS NULL
                AND source_type <> 'DERIVED'
            )
            OR (
                role = 'RESULT'
                AND value_kind = 'POINT'
                AND derivation_method = 'ARITHMETIC_MEAN'
                AND source_type = 'DERIVED'
            )
        ),
    CONSTRAINT chk_measurement_records_value_shape
        CHECK (
            (
                value_kind = 'POINT'
                AND point_value IS NOT NULL
                AND lower_value IS NULL
                AND upper_value IS NULL
            )
            OR
            (
                value_kind = 'INTERVAL'
                AND point_value IS NULL
                AND lower_value IS NOT NULL
                AND upper_value IS NOT NULL
                AND lower_value <= upper_value
            )
        ),
    CONSTRAINT chk_measurement_records_observation
        CHECK (
            role <> 'OBSERVATION'
            OR (value_kind = 'POINT' AND derivation_method IS NULL)
        ),
    CONSTRAINT fk_measurement_records_revision
        FOREIGN KEY (revision_id) REFERENCES measurement_set_revisions (revision_id)
);

CREATE TABLE IF NOT EXISTS measurement_derivation_inputs
(
    revision_id       UUID        NOT NULL,
    characteristic    VARCHAR(64) NOT NULL,
    derived_record_id UUID        NOT NULL,
    derived_role      VARCHAR(16) NOT NULL,
    input_record_id   UUID        NOT NULL,
    input_role        VARCHAR(16) NOT NULL,

    CONSTRAINT pk_measurement_derivation_inputs
        PRIMARY KEY (derived_record_id, input_record_id),
    CONSTRAINT chk_measurement_derivation_roles
        CHECK (derived_role = 'RESULT' AND input_role = 'OBSERVATION'),
    CONSTRAINT fk_measurement_derivation_result
        FOREIGN KEY (derived_record_id, revision_id, characteristic, derived_role)
            REFERENCES measurement_records (record_id, revision_id, characteristic, role),
    CONSTRAINT fk_measurement_derivation_input
        FOREIGN KEY (input_record_id, revision_id, characteristic, input_role)
            REFERENCES measurement_records (record_id, revision_id, characteristic, role)
);

CREATE INDEX IF NOT EXISTS idx_measurement_sets_subject
    ON measurement_sets (workspace_id, subject_type, subject_id, lifecycle);

CREATE INDEX IF NOT EXISTS idx_measurement_sets_current_revision
    ON measurement_sets (current_revision_id);

CREATE INDEX IF NOT EXISTS idx_measurement_set_revisions_set
    ON measurement_set_revisions (measurement_set_id, revision_number DESC);

CREATE INDEX IF NOT EXISTS idx_measurement_set_revisions_material_state
    ON measurement_set_revisions (material_state_id, measurement_set_id, revision_number DESC);

CREATE INDEX IF NOT EXISTS idx_measurement_records_revision_characteristic_role
    ON measurement_records (revision_id, characteristic, role);

CREATE UNIQUE INDEX IF NOT EXISTS ux_measurement_records_revision_result
    ON measurement_records (revision_id, characteristic)
    WHERE role = 'RESULT';

CREATE INDEX IF NOT EXISTS idx_measurement_records_characteristic_measured
    ON measurement_records (characteristic, measured_at DESC, record_id DESC);

CREATE INDEX IF NOT EXISTS idx_measurement_derivation_inputs_revision
    ON measurement_derivation_inputs (revision_id, characteristic);
