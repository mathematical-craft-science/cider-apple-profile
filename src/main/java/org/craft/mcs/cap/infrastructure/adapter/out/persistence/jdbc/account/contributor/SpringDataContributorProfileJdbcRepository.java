package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.contributor;

import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;


/** Spring Data command repository for ContributorProfile JDBC entities. */
interface SpringDataContributorProfileJdbcRepository
    extends ListCrudRepository<ContributorProfileJdbcEntity, @NonNull UUID> {

  @Modifying
  @Query("DELETE FROM contributor_profiles WHERE user_id = :userId AND version = :version")
  int deleteByIdAndVersion(
      @Param("userId") @NonNull UUID userId,
      @Param("version") long version
  );
}
