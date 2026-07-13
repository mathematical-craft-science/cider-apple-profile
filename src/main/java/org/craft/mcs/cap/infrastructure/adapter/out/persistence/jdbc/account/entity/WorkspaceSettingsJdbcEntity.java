package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity;

import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import static java.util.Objects.requireNonNull;


/** Spring Data JDBC aggregate entity for workspace product settings. */
@Table("workspace_settings")
public class WorkspaceSettingsJdbcEntity {

  @Id @NonNull @Column("workspace_id") UUID workspaceId;
  @Column("measurement_stale_after_seconds") long measurementStaleAfterSeconds;
  @NonNull @Column("default_acidity_basis") TitratableAcidityBasis defaultAcidityBasis;
  @CreatedDate @Nullable @Column("created_at") Instant createdAt;
  @LastModifiedDate @Nullable @Column("updated_at") Instant updatedAt;
  @Version @Column("version") long version;

  @SuppressWarnings("NullAway.Init")
  public WorkspaceSettingsJdbcEntity() {
  }

  WorkspaceSettingsJdbcEntity(
      @NonNull UUID workspaceId,
      long measurementStaleAfterSeconds,
      @NonNull TitratableAcidityBasis defaultAcidityBasis
  ) {
    this.workspaceId = requireNonNull(workspaceId, "workspace id cannot be null");
    this.measurementStaleAfterSeconds = measurementStaleAfterSeconds;
    this.defaultAcidityBasis = requireNonNull(defaultAcidityBasis, "default acidity basis cannot be null");
  }
}
