package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.preferences;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.user.application.port.out.UserPreferencesRepository;
import org.craft.mcs.cap.core.account.user.domain.model.UserPreferences;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcVersionedUpdate.complete;

/** Persists provider-independent presentation preferences for one CAP user. */
@Repository
@RequiredArgsConstructor
class JdbcUserPreferencesRepository implements UserPreferencesRepository {

  private final @NonNull SpringDataUserPreferencesJdbcRepository repository;

  @Override
  public @NonNull UserPreferences create(@NonNull UserPreferences preferences) {
    requireNonNull(preferences, "user preferences cannot be null");
    final var saved = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "user preferences",
        () -> repository.save(UserPreferencesJdbcMapper.fromDomain(preferences))
    );
    return UserPreferencesJdbcMapper.toDomain(saved);
  }

  @Override
  public @NonNull VersionedAggregate<UserPreferences> update(
      @NonNull VersionedAggregate<UserPreferences> preferences
  ) {
    requireNonNull(preferences, "versioned user preferences cannot be null");
    final var aggregate = preferences.aggregate();
    final var affectedRows = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "user preferences",
        () -> repository.updateByIdAndVersion(
            aggregate.userId().value(),
            aggregate.locale().toLanguageTag(),
            aggregate.timeZone().getId(),
            preferences.revision().value()
        )
    );
    return complete("user preferences", preferences, affectedRows);
  }

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<UserPreferences>> findVersionedById(
      @NonNull AccountUserId userId
  ) {
    requireNonNull(userId, "account user id cannot be null");
    return repository.findById(userId.value()).map(UserPreferencesJdbcMapper::toVersionedAggregate);
  }
}
