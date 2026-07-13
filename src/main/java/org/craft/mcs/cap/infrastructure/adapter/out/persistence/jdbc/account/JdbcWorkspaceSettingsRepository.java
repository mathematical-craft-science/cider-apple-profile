package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceSettingsRepository;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.core.account.domain.model.value.WorkspaceId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity.AccountJdbcMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/** Persists independently versioned product settings for one CAP workspace. */
@Repository
@RequiredArgsConstructor
class JdbcWorkspaceSettingsRepository implements WorkspaceSettingsRepository {

  private final @NonNull SpringDataWorkspaceSettingsJdbcRepository repository;

  @Override
  public @NonNull WorkspaceSettings save(@NonNull WorkspaceSettings settings) {
    requireNonNull(settings, "workspace settings cannot be null");
    final var entity = repository.findById(settings.workspaceId().value())
        .map(existing -> AccountJdbcMapper.updateFromDomain(settings, existing))
        .orElseGet(() -> AccountJdbcMapper.fromDomain(settings));
    final var saved = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "workspace settings",
        () -> repository.save(entity)
    );
    return AccountJdbcMapper.toDomain(saved);
  }

  @Override
  public @NonNull Optional<@NonNull WorkspaceSettings> findByWorkspaceId(@NonNull WorkspaceId workspaceId) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    return repository.findById(workspaceId.value()).map(AccountJdbcMapper::toDomain);
  }
}
