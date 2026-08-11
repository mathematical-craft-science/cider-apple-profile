CREATE TABLE IF NOT EXISTS vessels
(
    vessel_id                UUID                     NOT NULL PRIMARY KEY,
    workspace_id             UUID                     NOT NULL,
    name                     VARCHAR(160)             NOT NULL,
    nominal_capacity_liters  NUMERIC(15, 3)           NOT NULL,
    material                 VARCHAR(32)              NOT NULL,
    operational_status       VARCHAR(32)              NOT NULL,

    version                  BIGINT                   NOT NULL DEFAULT 0,
    created_at               TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at               TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_vessels_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (workspace_id),
    CONSTRAINT uq_vessels_workspace_identity
        UNIQUE (workspace_id, vessel_id),
    CONSTRAINT chk_vessels_nominal_capacity
        CHECK (nominal_capacity_liters > 0),
    CONSTRAINT chk_vessels_material
        CHECK (material IN (
            'STAINLESS_STEEL',
            'FOOD_GRADE_PLASTIC',
            'GLASS',
            'FIBERGLASS',
            'WOOD',
            'CONCRETE',
            'OTHER'
        )),
    CONSTRAINT chk_vessels_operational_status
        CHECK (operational_status IN ('OPERATIONAL', 'OUT_OF_SERVICE', 'RETIRED'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_vessels_workspace_lower_name
    ON vessels (workspace_id, LOWER(name));

CREATE INDEX IF NOT EXISTS idx_vessels_workspace_status
    ON vessels (workspace_id, operational_status);
