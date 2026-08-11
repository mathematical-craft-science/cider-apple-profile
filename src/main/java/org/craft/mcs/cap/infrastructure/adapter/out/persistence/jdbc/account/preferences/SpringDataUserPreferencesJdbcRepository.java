package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.preferences;

import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;


/** Spring Data command repository for UserPreferences JDBC entities. */
interface SpringDataUserPreferencesJdbcRepository
    extends ListCrudRepository<UserPreferencesJdbcEntity, @NonNull UUID> {

  @Modifying
  @Query("""
      UPDATE user_preferences
         SET locale = :locale,
             time_zone = :timeZone,
             version = version + 1,
             updated_at = GREATEST(clock_timestamp(), created_at, updated_at)
       WHERE user_id = :userId
         AND version = :expectedVersion
      """)
  int updateByIdAndVersion(
      @Param("userId") @NonNull UUID userId,
      @Param("locale") @NonNull String locale,
      @Param("timeZone") @NonNull String timeZone,
      @Param("expectedVersion") long expectedVersion
  );
}
