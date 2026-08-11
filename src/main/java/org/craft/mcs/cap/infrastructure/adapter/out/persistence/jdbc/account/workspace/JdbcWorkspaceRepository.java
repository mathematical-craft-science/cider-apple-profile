package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.workspace;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcVersionedUpdate.complete;

/** Persists CAP Workspace aggregates. */
@Repository
@RequiredArgsConstructor
class JdbcWorkspaceRepository implements WorkspaceRepository {

  private final @NonNull SpringDataWorkspaceJdbcRepository repository;

  @Override
  public @NonNull Workspace create(@NonNull Workspace workspace) {
    requireNonNull(workspace, "workspace cannot be null");
    final var saved = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "workspace",
        () -> repository.save(WorkspaceJdbcMapper.fromDomain(workspace))
    );
    return WorkspaceJdbcMapper.toDomain(saved);
  }

  @Override
  public @NonNull VersionedAggregate<Workspace> update(
      @NonNull VersionedAggregate<Workspace> workspace
  ) {
    requireNonNull(workspace, "versioned workspace cannot be null");
    final var aggregate = workspace.aggregate();
    final var affectedRows = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "workspace",
        () -> repository.updateByIdAndVersion(
            aggregate.workspaceId().value(),
            aggregate.name().value(),
            aggregate.type(),
            aggregate.status(),
            aggregate.externalOrganizationProvider() == null
                ? null
                : aggregate.externalOrganizationProvider().value(),
            aggregate.externalOrganizationSubject() == null
                ? null
                : aggregate.externalOrganizationSubject().value(),
            workspace.revision().value()
        )
    );
    return complete("workspace", workspace, affectedRows);
  }

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<Workspace>> findVersionedById(
      @NonNull WorkspaceId workspaceId
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    return repository.findById(workspaceId.value()).map(WorkspaceJdbcMapper::toVersionedAggregate);
  }
}
