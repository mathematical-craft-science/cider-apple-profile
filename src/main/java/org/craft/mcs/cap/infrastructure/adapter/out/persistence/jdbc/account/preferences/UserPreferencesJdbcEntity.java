package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.preferences;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import static java.util.Objects.requireNonNull;


/** Spring Data JDBC aggregate entity for user presentation preferences. */
@Table("user_preferences")
public class UserPreferencesJdbcEntity {

  @Id @NonNull @Column("user_id") UUID userId;
  @NonNull @Column("locale") String locale;
  @NonNull @Column("time_zone") String timeZone;
  @CreatedDate @Nullable @Column("created_at") Instant createdAt;
  @LastModifiedDate @Nullable @Column("updated_at") Instant updatedAt;
  @Version @Column("version") long version;

  @SuppressWarnings("NullAway.Init")
  public UserPreferencesJdbcEntity() {
  }

  UserPreferencesJdbcEntity(
      @NonNull UUID userId,
      @NonNull String locale,
      @NonNull String timeZone
  ) {
    this.userId = requireNonNull(userId, "account user id cannot be null");
    this.locale = requireNonNull(locale, "user locale cannot be null");
    this.timeZone = requireNonNull(timeZone, "user time zone cannot be null");
  }
}
