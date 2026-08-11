package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.settings;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.workspace.application.model.WorkspaceSettingsRevision;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceSettingsRepository;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceSettingsState;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcVersionedUpdate.requireSingleRow;

/** Persists independently versioned product settings for one CAP workspace. */
@Repository
@RequiredArgsConstructor
class JdbcWorkspaceSettingsRepository implements WorkspaceSettingsRepository {

  private final @NonNull SpringDataWorkspaceSettingsJdbcRepository repository;

  @Override
  public @NonNull WorkspaceSettingsState create(@NonNull WorkspaceSettings settings) {
    requireNonNull(settings, "workspace settings cannot be null");
    final var saved = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "workspace settings",
        () -> repository.save(WorkspaceSettingsJdbcMapper.fromDomain(settings))
    );
    return WorkspaceSettingsJdbcMapper.toState(saved);
  }

  @Override
  public @NonNull Optional<@NonNull WorkspaceSettingsState> update(
      @NonNull WorkspaceSettings settings,
      @NonNull WorkspaceSettingsRevision expectedRevision
  ) {
    requireNonNull(settings, "workspace settings cannot be null");
    requireNonNull(expectedRevision, "expected workspace settings revision cannot be null");
    final var affectedRows = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "workspace settings",
        () -> repository.updateByIdAndVersion(
            settings.workspaceId().value(),
            settings.measurementStaleAfter().toSeconds(),
            settings.defaultAcidityBasis(),
            expectedRevision.value()
        )
    );
    requireSingleRow("workspace settings", affectedRows);

    // The read returns database-managed audit timestamps; it is not part of the concurrency check.
    return repository.findById(settings.workspaceId().value()).map(WorkspaceSettingsJdbcMapper::toState);
  }

  @Override
  public @NonNull Optional<@NonNull WorkspaceSettingsState> findByWorkspaceId(
      @NonNull WorkspaceId workspaceId
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    return repository.findById(workspaceId.value()).map(WorkspaceSettingsJdbcMapper::toState);
  }
}
