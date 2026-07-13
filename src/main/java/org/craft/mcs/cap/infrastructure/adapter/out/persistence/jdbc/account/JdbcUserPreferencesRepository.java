package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.port.out.UserPreferencesRepository;
import org.craft.mcs.cap.core.account.domain.model.UserPreferences;
import org.craft.mcs.cap.core.account.domain.model.value.AccountUserId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity.AccountJdbcMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/** Persists provider-independent presentation preferences for one CAP user. */
@Repository
@RequiredArgsConstructor
class JdbcUserPreferencesRepository implements UserPreferencesRepository {

  private final @NonNull SpringDataUserPreferencesJdbcRepository repository;

  @Override
  public @NonNull UserPreferences save(@NonNull UserPreferences preferences) {
    requireNonNull(preferences, "user preferences cannot be null");
    final var entity = repository.findById(preferences.userId().value())
        .map(existing -> AccountJdbcMapper.updateFromDomain(preferences, existing))
        .orElseGet(() -> AccountJdbcMapper.fromDomain(preferences));
    final var saved = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "user preferences",
        () -> repository.save(entity)
    );
    return AccountJdbcMapper.toDomain(saved);
  }

  @Override
  public @NonNull Optional<@NonNull UserPreferences> findByUserId(@NonNull AccountUserId userId) {
    requireNonNull(userId, "account user id cannot be null");
    return repository.findById(userId.value()).map(AccountJdbcMapper::toDomain);
  }
}
