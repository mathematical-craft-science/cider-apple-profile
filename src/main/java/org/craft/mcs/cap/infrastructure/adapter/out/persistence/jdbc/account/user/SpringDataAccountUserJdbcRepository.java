package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.user;

import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUserStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;


/** Spring Data command repository for AccountUser JDBC entities. */
interface SpringDataAccountUserJdbcRepository
    extends ListCrudRepository<AccountUserJdbcEntity, @NonNull UUID> {

  @Modifying
  @Query("""
      UPDATE account_users
         SET identity_provider = :identityProvider,
             external_subject = :externalSubject,
             email = :email,
             display_name = :displayName,
             avatar_url = :avatarUrl,
             status = :status,
             version = version + 1,
             updated_at = GREATEST(clock_timestamp(), created_at, updated_at)
       WHERE user_id = :userId
         AND version = :expectedVersion
      """)
  int updateByIdAndVersion(
      @Param("userId") @NonNull UUID userId,
      @Param("identityProvider") @NonNull String identityProvider,
      @Param("externalSubject") @NonNull String externalSubject,
      @Param("email") @NonNull String email,
      @Param("displayName") @NonNull String displayName,
      @Param("avatarUrl") @Nullable String avatarUrl,
      @Param("status") @NonNull AccountUserStatus status,
      @Param("expectedVersion") long expectedVersion
  );

  @NonNull Optional<@NonNull AccountUserJdbcEntity> findByIdentityProviderAndExternalSubject(
      @NonNull String identityProvider,
      @NonNull String externalSubject
  );

  @NonNull Optional<@NonNull AccountUserJdbcEntity> findByEmailIgnoreCase(@NonNull String email);
}
