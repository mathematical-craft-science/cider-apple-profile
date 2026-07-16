package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.preferences;

import java.time.ZoneId;
import java.util.Locale;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.user.domain.model.UserPreferences;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Maps the UserPreferences aggregate to its Spring Data JDBC representation. */
final class UserPreferencesJdbcMapper {

  private UserPreferencesJdbcMapper() {
  }

  static @NonNull UserPreferencesJdbcEntity fromDomain(@NonNull UserPreferences preferences) {
    return new UserPreferencesJdbcEntity(
        preferences.userId().value(),
        preferences.locale().toLanguageTag(),
        preferences.timeZone().getId()
    );
  }

  static @NonNull UserPreferencesJdbcEntity updateFromDomain(
      @NonNull UserPreferences preferences,
      @NonNull UserPreferencesJdbcEntity entity
  ) {
    entity.locale = preferences.locale().toLanguageTag();
    entity.timeZone = preferences.timeZone().getId();
    return entity;
  }

  static @NonNull UserPreferences toDomain(@NonNull UserPreferencesJdbcEntity entity) {
    return new UserPreferences(
        AccountUserId.of(entity.userId),
        Locale.forLanguageTag(entity.locale),
        ZoneId.of(entity.timeZone)
    );
  }

  static @NonNull VersionedAggregate<UserPreferences> toVersionedAggregate(
      @NonNull UserPreferencesJdbcEntity entity
  ) {
    return new VersionedAggregate<>(toDomain(entity), AggregateRevision.of(entity.version));
  }

  static long revision(@NonNull UserPreferencesJdbcEntity entity) {
    return requireNonNull(entity, "user preferences row cannot be null").version;
  }
}
