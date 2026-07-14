package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.domain.model.Workspace;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity.AccountJdbcMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/** Persists CAP Workspace aggregates. */
@Repository
@RequiredArgsConstructor
class JdbcWorkspaceRepository implements WorkspaceRepository {

  private final @NonNull SpringDataWorkspaceJdbcRepository repository;

  @Override
  public @NonNull Workspace save(@NonNull Workspace workspace) {
    requireNonNull(workspace, "workspace cannot be null");
    final var entity = repository.findById(workspace.workspaceId().value())
        .map(existing -> AccountJdbcMapper.updateFromDomain(workspace, existing))
        .orElseGet(() -> AccountJdbcMapper.fromDomain(workspace));
    final var saved = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "workspace",
        () -> repository.save(entity)
    );
    return AccountJdbcMapper.toDomain(saved);
  }

  @Override
  public @NonNull Optional<@NonNull Workspace> findById(@NonNull WorkspaceId workspaceId) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    return repository.findById(workspaceId.value()).map(AccountJdbcMapper::toDomain);
  }
}
