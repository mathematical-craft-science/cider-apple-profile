package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.workspace;

import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceStatus;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import static java.util.Objects.requireNonNull;

/** Spring Data JDBC aggregate entity for a CAP workspace. */
@Table("workspaces")
public class WorkspaceJdbcEntity {

  @Id @NonNull @Column("workspace_id") UUID workspaceId;
  @NonNull @Column("name") String name;
  @NonNull @Column("type") WorkspaceType type;
  @NonNull @Column("status") WorkspaceStatus status;
  @Nullable @Column("external_organization_provider") String externalOrganizationProvider;
  @Nullable @Column("external_organization_subject") String externalOrganizationSubject;
  @CreatedDate @Nullable @Column("created_at") Instant createdAt;
  @LastModifiedDate @Nullable @Column("updated_at") Instant updatedAt;
  @Version @Column("version") long version;

  @SuppressWarnings("NullAway.Init")
  public WorkspaceJdbcEntity() {
  }

  WorkspaceJdbcEntity(
      @NonNull UUID workspaceId,
      @NonNull String name,
      @NonNull WorkspaceType type,
      @NonNull WorkspaceStatus status,
      @Nullable String externalOrganizationProvider,
      @Nullable String externalOrganizationSubject
  ) {
    this.workspaceId = requireNonNull(workspaceId, "workspace id cannot be null");
    this.name = requireNonNull(name, "workspace name cannot be null");
    this.type = requireNonNull(type, "workspace type cannot be null");
    this.status = requireNonNull(status, "workspace status cannot be null");
    this.externalOrganizationProvider = externalOrganizationProvider;
    this.externalOrganizationSubject = externalOrganizationSubject;
  }
}
