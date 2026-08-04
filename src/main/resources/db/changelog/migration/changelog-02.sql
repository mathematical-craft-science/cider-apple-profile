CREATE TABLE IF NOT EXISTS account_users
(
    user_id              UUID                     NOT NULL PRIMARY KEY,
    identity_provider    VARCHAR(64)              NOT NULL,
    external_subject     VARCHAR(255)             NOT NULL,
    email                VARCHAR(320)             NOT NULL,
    display_name         VARCHAR(255)             NOT NULL,
    avatar_url           VARCHAR(1000),
    status               VARCHAR(32)              NOT NULL,

    version              BIGINT                   NOT NULL DEFAULT 0,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uk_account_users_external_identity
        UNIQUE (identity_provider, external_subject)
);

CREATE TABLE IF NOT EXISTS workspaces
(
    workspace_id                  UUID                     NOT NULL PRIMARY KEY,
    name                          VARCHAR(255)             NOT NULL,
    type                          VARCHAR(64)              NOT NULL,
    status                        VARCHAR(32)              NOT NULL,
    external_organization_provider VARCHAR(64),
    external_organization_subject VARCHAR(255),

    version                       BIGINT                   NOT NULL DEFAULT 0,
    created_at                    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at                    TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT chk_workspaces_external_organization
        CHECK ((external_organization_provider IS NULL) = (external_organization_subject IS NULL))
);

CREATE TABLE IF NOT EXISTS workspace_memberships
(
    membership_id UUID                     NOT NULL PRIMARY KEY,
    workspace_id  UUID                     NOT NULL,
    user_id       UUID                     NOT NULL,
    role          VARCHAR(64)              NOT NULL,
    status        VARCHAR(32)              NOT NULL,

    version       BIGINT                   NOT NULL DEFAULT 0,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_workspace_memberships_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (workspace_id),
    CONSTRAINT fk_workspace_memberships_user
        FOREIGN KEY (user_id) REFERENCES account_users (user_id),
    CONSTRAINT chk_workspace_memberships_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'REMOVED')),
    CONSTRAINT uk_workspace_memberships_workspace_user UNIQUE (workspace_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_workspace_memberships_user_id
    ON workspace_memberships (user_id);

CREATE TABLE IF NOT EXISTS workspace_settings
(
    workspace_id                 UUID                     NOT NULL PRIMARY KEY,
    measurement_stale_after_seconds BIGINT                NOT NULL,
    default_acidity_basis        VARCHAR(64)              NOT NULL,

    version                      BIGINT                   NOT NULL DEFAULT 0,
    created_at                   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at                   TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_workspace_settings_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (workspace_id),
    CONSTRAINT chk_workspace_settings_stale_after
        CHECK (measurement_stale_after_seconds > 0)
);

CREATE TABLE IF NOT EXISTS user_preferences
(
    user_id     UUID                     NOT NULL PRIMARY KEY,
    locale      VARCHAR(35)              NOT NULL,
    time_zone   VARCHAR(64)              NOT NULL,

    version     BIGINT                   NOT NULL DEFAULT 0,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_user_preferences_user
        FOREIGN KEY (user_id) REFERENCES account_users (user_id)
);

CREATE TABLE IF NOT EXISTS contributor_profiles
(
    user_id             UUID                     NOT NULL PRIMARY KEY,
    contributor_type    VARCHAR(64)              NOT NULL,
    public_name         VARCHAR(255)             NOT NULL,
    website             VARCHAR(1000),
    public_email        VARCHAR(320),
    public_phone        VARCHAR(64),
    contact_visibility  VARCHAR(32)              NOT NULL,
    contact_publication_consented_at TIMESTAMP WITH TIME ZONE,

    version             BIGINT                   NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_contributor_profiles_user
        FOREIGN KEY (user_id) REFERENCES account_users (user_id)
);

CREATE TABLE IF NOT EXISTS workspace_invitations
(
    invitation_id       UUID                     NOT NULL PRIMARY KEY,
    workspace_id        UUID                     NOT NULL,
    invited_email       VARCHAR(320)             NOT NULL,
    role                VARCHAR(64)              NOT NULL,
    status              VARCHAR(32)              NOT NULL,
    invited_by_user_id  UUID                     NOT NULL,
    accepted_by_user_id UUID,
    expires_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    resend_count        INTEGER                  NOT NULL DEFAULT 0,

    version             BIGINT                   NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_workspace_invitations_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (workspace_id),
    CONSTRAINT fk_workspace_invitations_inviter
        FOREIGN KEY (invited_by_user_id) REFERENCES account_users (user_id),
    CONSTRAINT fk_workspace_invitations_acceptor
        FOREIGN KEY (accepted_by_user_id) REFERENCES account_users (user_id),
    CONSTRAINT chk_workspace_invitations_owner_role
        CHECK (role <> 'OWNER'),
    CONSTRAINT chk_workspace_invitations_resend_count
        CHECK (resend_count >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_workspace_invitations_pending_email
    ON workspace_invitations (workspace_id, lower(invited_email))
    WHERE status = 'PENDING';

CREATE INDEX IF NOT EXISTS idx_workspace_invitations_workspace_created
    ON workspace_invitations (workspace_id, created_at DESC, invitation_id DESC);

CREATE TABLE IF NOT EXISTS workspace_administrative_audit
(
    audit_entry_id UUID                     NOT NULL PRIMARY KEY,
    workspace_id   UUID                     NOT NULL,
    actor_user_id  UUID                     NOT NULL,
    action         VARCHAR(64)              NOT NULL,
    subject_type   VARCHAR(64)              NOT NULL,
    subject_id     UUID                     NOT NULL,
    previous_value VARCHAR(2000),
    new_value      VARCHAR(2000),
    occurred_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    version        BIGINT                   NOT NULL DEFAULT 0,

    CONSTRAINT fk_workspace_administrative_audit_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (workspace_id),
    CONSTRAINT fk_workspace_administrative_audit_actor
        FOREIGN KEY (actor_user_id) REFERENCES account_users (user_id)
);

CREATE INDEX IF NOT EXISTS idx_workspace_administrative_audit_workspace_occurred
    ON workspace_administrative_audit (workspace_id, occurred_at DESC, audit_entry_id DESC);
