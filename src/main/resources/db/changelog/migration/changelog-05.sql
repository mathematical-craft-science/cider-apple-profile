CREATE TABLE IF NOT EXISTS batch_trace_steps
(
    step_id        UUID                     NOT NULL PRIMARY KEY,
    workspace_id   UUID                     NOT NULL,
    parent_step_id UUID,
    step_type      VARCHAR(64)              NOT NULL,
    occurred_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    performed_by_type VARCHAR(32)            NOT NULL,
    performed_by_id   VARCHAR(160)           NOT NULL,
    note           VARCHAR(2000),
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_batch_trace_steps_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (workspace_id)
);

CREATE TABLE IF NOT EXISTS batch_trace_step_subjects
(
    step_id                      UUID        NOT NULL,
    subject_role                 VARCHAR(16) NOT NULL,
    subject_order                INTEGER     NOT NULL,
    trace_state_id               UUID        NOT NULL,
    subject_type                 VARCHAR(64) NOT NULL,
    subject_id                   UUID        NOT NULL,
    display_name_snapshot        VARCHAR(255),

    snapshot_volume_liters       NUMERIC(15, 3),
    snapshot_confidence          VARCHAR(64),

    sugar_value                  NUMERIC(12, 9),
    sugar_value_kind             VARCHAR(32),
    sugar_min                    NUMERIC(12, 9),
    sugar_max                    NUMERIC(12, 9),

    acidity_value                NUMERIC(12, 6),
    acidity_value_kind           VARCHAR(32),
    acidity_min                  NUMERIC(12, 6),
    acidity_max                  NUMERIC(12, 6),

    tannin_value              NUMERIC(12, 4),
    tannin_value_kind         VARCHAR(32),
    tannin_min                NUMERIC(12, 4),
    tannin_max                NUMERIC(12, 4),

    yan_value                    NUMERIC(12, 4),
    yan_value_kind               VARCHAR(32),
    yan_min                      NUMERIC(12, 4),
    yan_max                      NUMERIC(12, 4),

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

CREATE INDEX IF NOT EXISTS idx_batch_trace_steps_workspace_id
    ON batch_trace_steps (workspace_id);

CREATE INDEX IF NOT EXISTS idx_batch_trace_steps_parent_step_id
    ON batch_trace_steps (parent_step_id);

CREATE INDEX IF NOT EXISTS idx_batch_trace_steps_type_occurred
    ON batch_trace_steps (step_type, occurred_at, step_id);

CREATE INDEX IF NOT EXISTS idx_batch_trace_step_subjects_step_id
    ON batch_trace_step_subjects (step_id);

CREATE INDEX IF NOT EXISTS idx_batch_trace_step_subjects_lookup
    ON batch_trace_step_subjects (subject_role, subject_type, subject_id);

CREATE INDEX IF NOT EXISTS idx_batch_trace_step_subjects_state_id
    ON batch_trace_step_subjects (trace_state_id);

CREATE TABLE IF NOT EXISTS batch_trace_current_states
(
    workspace_id     UUID                     NOT NULL,
    subject_type     VARCHAR(64)              NOT NULL,
    subject_id       UUID                     NOT NULL,
    current_state_id UUID                     NOT NULL,
    version          BIGINT                   NOT NULL DEFAULT 0,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_batch_trace_current_states
        PRIMARY KEY (workspace_id, subject_type, subject_id),
    CONSTRAINT fk_batch_trace_current_states_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (workspace_id),
    CONSTRAINT chk_batch_trace_current_states_version
        CHECK (version >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_batch_trace_current_states_subject
    ON batch_trace_current_states (subject_type, subject_id);

CREATE INDEX IF NOT EXISTS idx_batch_trace_current_states_state_id
    ON batch_trace_current_states (current_state_id);
