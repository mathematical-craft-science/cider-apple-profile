package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity.AccountJdbcMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcAggregateRevisionGuard.requireExpectedRevision;

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
        () -> repository.save(AccountJdbcMapper.fromDomain(workspace))
    );
    return AccountJdbcMapper.toDomain(saved);
  }

  @Override
  public @NonNull VersionedAggregate<Workspace> update(
      @NonNull VersionedAggregate<Workspace> workspace
  ) {
    requireNonNull(workspace, "versioned workspace cannot be null");
    final var aggregate = workspace.aggregate();
    final var entity = repository.findById(aggregate.workspaceId().value())
        .orElseThrow(() -> new ConcurrentResourceModificationException("workspace", null));
    requireExpectedRevision("workspace", workspace.revision(), AccountJdbcMapper.revision(entity));
    final var saved = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "workspace",
        () -> repository.save(AccountJdbcMapper.updateFromDomain(aggregate, entity))
    );
    return AccountJdbcMapper.toVersionedAggregate(saved);
  }

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<Workspace>> findVersionedById(
      @NonNull WorkspaceId workspaceId
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    return repository.findById(workspaceId.value()).map(AccountJdbcMapper::toVersionedAggregate);
  }
}
