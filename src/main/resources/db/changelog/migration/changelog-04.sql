CREATE TABLE IF NOT EXISTS feedstock_batches
(
    batch_id                 UUID                     NOT NULL PRIMARY KEY,
    workspace_id             UUID                     NOT NULL,
    name                     VARCHAR(160)             NOT NULL,
    vessel_id                UUID                     NOT NULL,
    type                     VARCHAR(32)              NOT NULL,
    season_starts_on         DATE                     NOT NULL,
    season_ends_on           DATE                     NOT NULL,
    source                   VARCHAR(255),
    lifecycle                VARCHAR(16)              NOT NULL,
    available_volume_liters  NUMERIC(15, 3)           NOT NULL,
    current_material_state_id UUID                    NOT NULL,
    note                     VARCHAR(2000),
    deleted_at               TIMESTAMP WITH TIME ZONE,

    version                  BIGINT                   NOT NULL DEFAULT 0,
    created_at               TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at               TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT chk_feedstock_batches_season_dates
        CHECK (season_ends_on >= season_starts_on),
    CONSTRAINT chk_feedstock_batches_available_volume
        CHECK (
            (lifecycle = 'ACTIVE' AND available_volume_liters > 0)
            OR (lifecycle IN ('DEPLETED', 'DISCARDED') AND available_volume_liters = 0)
        ),
    CONSTRAINT chk_feedstock_batches_lifecycle
        CHECK (lifecycle IN ('ACTIVE', 'DEPLETED', 'DISCARDED')),
    CONSTRAINT chk_feedstock_batches_deleted_material
        CHECK (deleted_at IS NULL OR lifecycle <> 'ACTIVE'),
    CONSTRAINT fk_feedstock_batches_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (workspace_id),
    CONSTRAINT fk_feedstock_batches_vessel
        FOREIGN KEY (workspace_id, vessel_id) REFERENCES vessels (workspace_id, vessel_id)
);

CREATE INDEX IF NOT EXISTS idx_feedstock_batches_workspace_id
    ON feedstock_batches (workspace_id);

CREATE INDEX IF NOT EXISTS idx_feedstock_batches_vessel_id
    ON feedstock_batches (vessel_id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_feedstock_batches_active_vessel
    ON feedstock_batches (vessel_id)
    WHERE lifecycle = 'ACTIVE' AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_feedstock_batches_active_workspace_vessel
    ON feedstock_batches (workspace_id, vessel_id)
    WHERE lifecycle = 'ACTIVE' AND deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_feedstock_batches_current_material_state
    ON feedstock_batches (current_material_state_id);

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
    version            BIGINT                   NOT NULL DEFAULT 0,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT chk_measurement_sets_subject_type
        CHECK (subject_type IN ('FEEDSTOCK_BATCH', 'PRODUCTION_BATCH')),
    CONSTRAINT fk_measurement_sets_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (workspace_id)
);

CREATE TABLE IF NOT EXISTS measurement_set_revisions
(
    revision_id       UUID                     NOT NULL PRIMARY KEY,
    measurement_set_id UUID                   NOT NULL,
    revision_number   INTEGER                  NOT NULL,
    material_state_id UUID                     NOT NULL,
    lifecycle         VARCHAR(16)              NOT NULL,
    change_kind       VARCHAR(16)              NOT NULL,
    name              VARCHAR(160)             NOT NULL,
    note              VARCHAR(2000),
    correction_reason VARCHAR(1000),
    retraction_reason VARCHAR(1000),
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    performed_by_type VARCHAR(32)              NOT NULL,
    performed_by_id   UUID                     NOT NULL,

    CONSTRAINT ux_measurement_revisions_set_number
        UNIQUE (measurement_set_id, revision_number),
    CONSTRAINT ux_measurement_revisions_set_identity
        UNIQUE (measurement_set_id, revision_id),
    CONSTRAINT chk_measurement_revisions_number
        CHECK (revision_number > 0),
    CONSTRAINT chk_measurement_revisions_lifecycle
        CHECK (lifecycle IN ('ACTIVE', 'RETRACTED')),
    CONSTRAINT chk_measurement_revisions_change_kind
        CHECK (change_kind IN ('INITIAL', 'CORRECTION', 'RETRACTION')),
    CONSTRAINT chk_measurement_revisions_actor_type
        CHECK (performed_by_type IN ('ACCOUNT_USER', 'MACHINE_IDENTITY')),
    CONSTRAINT chk_measurement_revisions_change_audit
        CHECK (
            (
                change_kind = 'INITIAL'
                AND revision_number = 1
                AND lifecycle = 'ACTIVE'
                AND correction_reason IS NULL
                AND retraction_reason IS NULL
            )
            OR (
                change_kind = 'CORRECTION'
                AND revision_number > 1
                AND lifecycle = 'ACTIVE'
                AND correction_reason IS NOT NULL
                AND BTRIM(correction_reason) <> ''
                AND retraction_reason IS NULL
            )
            OR (
                change_kind = 'RETRACTION'
                AND revision_number > 1
                AND lifecycle = 'RETRACTED'
                AND correction_reason IS NULL
                AND retraction_reason IS NOT NULL
                AND BTRIM(retraction_reason) <> ''
            )
        ),
    CONSTRAINT fk_measurement_revisions_set
        FOREIGN KEY (measurement_set_id) REFERENCES measurement_sets (measurement_set_id)
);

CREATE TABLE IF NOT EXISTS measurement_set_heads
(
    measurement_set_id UUID NOT NULL PRIMARY KEY,
    revision_id        UUID NOT NULL,

    CONSTRAINT fk_measurement_set_heads_set
        FOREIGN KEY (measurement_set_id) REFERENCES measurement_sets (measurement_set_id),
    CONSTRAINT fk_measurement_set_heads_revision
        FOREIGN KEY (measurement_set_id, revision_id)
            REFERENCES measurement_set_revisions (measurement_set_id, revision_id)
);

CREATE TABLE IF NOT EXISTS measurement_records
(
    measurement_set_id     UUID                     NOT NULL,
    record_id              UUID                     NOT NULL PRIMARY KEY,
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
        UNIQUE (measurement_set_id, record_id, characteristic, role),
    CONSTRAINT fk_measurement_records_set
        FOREIGN KEY (measurement_set_id) REFERENCES measurement_sets (measurement_set_id),
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
    CONSTRAINT chk_measurement_records_specific_gravity_range
        CHECK (
            characteristic <> 'SPECIFIC_GRAVITY'
            OR (
                value_kind = 'POINT'
                AND point_value BETWEEN 0.900 AND 1.200
            )
            OR (
                value_kind = 'INTERVAL'
                AND lower_value >= 0.900
                AND upper_value <= 1.200
            )
        ),
    CONSTRAINT chk_measurement_records_ph_range
        CHECK (
            characteristic <> 'PH'
            OR (
                value_kind = 'POINT'
                AND point_value BETWEEN 0 AND 15.0
            )
            OR (
                value_kind = 'INTERVAL'
                AND lower_value >= 0
                AND upper_value <= 15.0
            )
        ),
    CONSTRAINT chk_measurement_records_titratable_acidity_range
        CHECK (
            characteristic <> 'TITRATABLE_ACIDITY'
            OR (
                value_kind = 'POINT'
                AND point_value BETWEEN 0 AND 30.0
            )
            OR (
                value_kind = 'INTERVAL'
                AND lower_value >= 0
                AND upper_value <= 30.0
            )
        ),
    CONSTRAINT chk_measurement_records_tannin_range
        CHECK (
            characteristic <> 'TANNIN'
            OR (
                value_kind = 'POINT'
                AND point_value BETWEEN 0 AND 12000
            )
            OR (
                value_kind = 'INTERVAL'
                AND lower_value >= 0
                AND upper_value <= 12000
            )
        ),
    CONSTRAINT chk_measurement_records_yan_range
        CHECK (
            characteristic <> 'YAN'
            OR (
                value_kind = 'POINT'
                AND point_value BETWEEN 0 AND 2000.0
            )
            OR (
                value_kind = 'INTERVAL'
                AND lower_value >= 0
                AND upper_value <= 2000.0
            )
        ),
    CONSTRAINT chk_measurement_records_temperature_range
        CHECK (
            characteristic <> 'TEMPERATURE'
            OR (
                value_kind = 'POINT'
                AND point_value >= -273.15
            )
            OR (
                value_kind = 'INTERVAL'
                AND lower_value >= -273.15
            )
        ),
    CONSTRAINT chk_measurement_records_turbidity_range
        CHECK (
            characteristic <> 'TURBIDITY'
            OR (
                value_kind = 'POINT'
                AND point_value >= 0
            )
            OR (
                value_kind = 'INTERVAL'
                AND lower_value >= 0
            )
        ),
    CONSTRAINT chk_measurement_records_observation
        CHECK (
            role <> 'OBSERVATION'
            OR (value_kind = 'POINT' AND derivation_method IS NULL)
        )
);

CREATE TABLE IF NOT EXISTS measurement_revision_records
(
    measurement_set_id        UUID                     NOT NULL,
    revision_id               UUID                     NOT NULL,
    record_id                 UUID                     NOT NULL,
    characteristic            VARCHAR(64)              NOT NULL,
    role                      VARCHAR(16)              NOT NULL,
    record_status             VARCHAR(16)              NOT NULL,

    CONSTRAINT pk_measurement_revision_records
        PRIMARY KEY (revision_id, record_id),
    CONSTRAINT fk_measurement_revision_records_revision
        FOREIGN KEY (measurement_set_id, revision_id)
            REFERENCES measurement_set_revisions (measurement_set_id, revision_id),
    CONSTRAINT fk_measurement_revision_records_record
        FOREIGN KEY (measurement_set_id, record_id, characteristic, role)
            REFERENCES measurement_records (measurement_set_id, record_id, characteristic, role),
    CONSTRAINT chk_measurement_revision_records_status
        CHECK (record_status IN ('ACTIVE', 'RETRACTED')),
    CONSTRAINT chk_measurement_revision_records_retraction
        CHECK (
            record_status = 'ACTIVE'
            OR (record_status = 'RETRACTED' AND role = 'RESULT')
        )
);

CREATE TABLE IF NOT EXISTS measurement_derivation_inputs
(
    measurement_set_id UUID        NOT NULL,
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
        FOREIGN KEY (measurement_set_id, derived_record_id, characteristic, derived_role)
            REFERENCES measurement_records (measurement_set_id, record_id, characteristic, role),
    CONSTRAINT fk_measurement_derivation_input
        FOREIGN KEY (measurement_set_id, input_record_id, characteristic, input_role)
            REFERENCES measurement_records (measurement_set_id, record_id, characteristic, role)
);

CREATE INDEX IF NOT EXISTS idx_measurement_sets_subject
    ON measurement_sets (workspace_id, subject_type, subject_id);

CREATE INDEX IF NOT EXISTS idx_measurement_set_revisions_set
    ON measurement_set_revisions (measurement_set_id, revision_number DESC);

CREATE INDEX IF NOT EXISTS idx_measurement_set_heads_revision
    ON measurement_set_heads (revision_id);

CREATE INDEX IF NOT EXISTS idx_measurement_set_revisions_material_state
    ON measurement_set_revisions (material_state_id, measurement_set_id, revision_number DESC);

CREATE INDEX IF NOT EXISTS idx_measurement_revision_records_revision
    ON measurement_revision_records (revision_id, record_status, characteristic, role);

CREATE INDEX IF NOT EXISTS idx_measurement_revision_records_record_history
    ON measurement_revision_records (record_id, record_status, revision_id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_measurement_revision_records_result
    ON measurement_revision_records (revision_id, characteristic)
    WHERE role = 'RESULT';

CREATE INDEX IF NOT EXISTS idx_measurement_records_characteristic_measured
    ON measurement_records (characteristic, measured_at DESC, record_id DESC);

CREATE INDEX IF NOT EXISTS idx_measurement_derivation_inputs_characteristic
    ON measurement_derivation_inputs (characteristic);
