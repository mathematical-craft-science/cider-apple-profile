package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account;

import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity.AccountUserJdbcEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.ListCrudRepository;


/** Spring Data command repository for AccountUser JDBC entities. */
interface SpringDataAccountUserJdbcRepository
    extends ListCrudRepository<AccountUserJdbcEntity, @NonNull UUID> {

  @NonNull Optional<@NonNull AccountUserJdbcEntity> findByIdentityProviderAndExternalSubject(
      @NonNull String identityProvider,
      @NonNull String externalSubject
  );
}
