CREATE TABLE IF NOT EXISTS batch_trace_steps
(
    step_id        UUID                     NOT NULL PRIMARY KEY,
    trace_id       UUID                     NOT NULL,
    parent_step_id UUID,
    operation_type VARCHAR(64)              NOT NULL,
    occurred_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    performed_by   VARCHAR(160),
    note           VARCHAR(2000),
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS batch_trace_step_subjects
(
    step_id                      UUID        NOT NULL,
    subject_role                 VARCHAR(16) NOT NULL,
    subject_order                INTEGER     NOT NULL,
    subject_type                 VARCHAR(64) NOT NULL,
    subject_id                   VARCHAR(120) NOT NULL,
    display_name_snapshot        VARCHAR(255),

    snapshot_volume_liters       NUMERIC(15, 3),
    snapshot_confidence          VARCHAR(64),

    sugar_value                  NUMERIC(12, 9),
    sugar_value_kind             VARCHAR(32),
    sugar_min                    NUMERIC(12, 9),
    sugar_max                    NUMERIC(12, 9),
    sugar_standard_deviation     NUMERIC(12, 9),
    sugar_sample_size            INTEGER,

    acidity_value                NUMERIC(12, 6),
    acidity_value_kind           VARCHAR(32),
    acidity_min                  NUMERIC(12, 6),
    acidity_max                  NUMERIC(12, 6),
    acidity_standard_deviation   NUMERIC(12, 6),
    acidity_sample_size          INTEGER,

    tanninity_value              NUMERIC(12, 4),
    tanninity_value_kind         VARCHAR(32),
    tanninity_min                NUMERIC(12, 4),
    tanninity_max                NUMERIC(12, 4),
    tanninity_standard_deviation NUMERIC(12, 4),
    tanninity_sample_size        INTEGER,

    CONSTRAINT fk_batch_trace_step_subjects_step
        FOREIGN KEY (step_id)
            REFERENCES batch_trace_steps (step_id)
            ON DELETE CASCADE,
    CONSTRAINT pk_batch_trace_step_subjects
        PRIMARY KEY (step_id, subject_role, subject_order),
    CONSTRAINT chk_batch_trace_step_subjects_role
        CHECK (subject_role IN ('INPUT', 'OUTPUT')),
    CONSTRAINT chk_batch_trace_step_subjects_order
        CHECK (subject_order >= 0)
);

CREATE INDEX IF NOT EXISTS idx_batch_trace_steps_trace_id
    ON batch_trace_steps (trace_id);

CREATE INDEX IF NOT EXISTS idx_batch_trace_steps_parent_step_id
    ON batch_trace_steps (parent_step_id);

CREATE INDEX IF NOT EXISTS idx_batch_trace_steps_operation_occurred
    ON batch_trace_steps (operation_type, occurred_at, step_id);

CREATE INDEX IF NOT EXISTS idx_batch_trace_step_subjects_step_id
    ON batch_trace_step_subjects (step_id);

CREATE INDEX IF NOT EXISTS idx_batch_trace_step_subjects_lookup
    ON batch_trace_step_subjects (subject_role, subject_type, subject_id);
