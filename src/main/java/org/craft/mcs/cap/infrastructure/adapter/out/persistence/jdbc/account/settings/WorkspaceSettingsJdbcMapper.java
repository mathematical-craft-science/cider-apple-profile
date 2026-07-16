package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.settings;

import java.time.Duration;
import org.craft.mcs.cap.core.account.workspace.application.model.WorkspaceSettingsRevision;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceSettingsState;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Maps independently versioned WorkspaceSettings state to Spring Data JDBC. */
final class WorkspaceSettingsJdbcMapper {

  private WorkspaceSettingsJdbcMapper() {
  }

  static @NonNull WorkspaceSettingsJdbcEntity fromDomain(@NonNull WorkspaceSettings settings) {
    return new WorkspaceSettingsJdbcEntity(
        settings.workspaceId().value(),
        settings.measurementStaleAfter().toSeconds(),
        settings.defaultAcidityBasis()
    );
  }

  static @NonNull WorkspaceSettingsJdbcEntity updateFromDomain(
      @NonNull WorkspaceSettings settings,
      @NonNull WorkspaceSettingsJdbcEntity entity
  ) {
    entity.measurementStaleAfterSeconds = settings.measurementStaleAfter().toSeconds();
    entity.defaultAcidityBasis = settings.defaultAcidityBasis();
    return entity;
  }

  static @NonNull WorkspaceSettings toDomain(@NonNull WorkspaceSettingsJdbcEntity entity) {
    return new WorkspaceSettings(
        WorkspaceId.of(entity.workspaceId),
        Duration.ofSeconds(entity.measurementStaleAfterSeconds),
        entity.defaultAcidityBasis
    );
  }

  static @NonNull WorkspaceSettingsState toState(@NonNull WorkspaceSettingsJdbcEntity entity) {
    return new WorkspaceSettingsState(
        toDomain(entity),
        WorkspaceSettingsRevision.of(entity.version),
        requireNonNull(entity.createdAt, "workspace settings creation time cannot be null"),
        requireNonNull(entity.updatedAt, "workspace settings update time cannot be null")
    );
  }
}
