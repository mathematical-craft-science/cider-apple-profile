package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.contributor;

import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContactVisibility;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;


/** Spring Data command repository for ContributorProfile JDBC entities. */
interface SpringDataContributorProfileJdbcRepository
    extends ListCrudRepository<ContributorProfileJdbcEntity, @NonNull UUID> {

  @Modifying
  @Query("""
      UPDATE contributor_profiles
         SET contributor_type = :contributorType,
             public_name = :publicName,
             website = :website,
             public_email = :publicEmail,
             public_phone = :publicPhone,
             contact_visibility = :contactVisibility,
             contact_publication_consented_at = :contactPublicationConsentedAt,
             version = version + 1,
             updated_at = GREATEST(clock_timestamp(), created_at, updated_at)
       WHERE user_id = :userId
         AND version = :expectedVersion
      """)
  int updateByIdAndVersion(
      @Param("userId") @NonNull UUID userId,
      @Param("contributorType") @NonNull ContributorType contributorType,
      @Param("publicName") @NonNull String publicName,
      @Param("website") @Nullable String website,
      @Param("publicEmail") @Nullable String publicEmail,
      @Param("publicPhone") @Nullable String publicPhone,
      @Param("contactVisibility") @NonNull ContactVisibility contactVisibility,
      @Param("contactPublicationConsentedAt") @Nullable Instant contactPublicationConsentedAt,
      @Param("expectedVersion") long expectedVersion
  );

  @Modifying
  @Query("DELETE FROM contributor_profiles WHERE user_id = :userId AND version = :version")
  int deleteByIdAndVersion(
      @Param("userId") @NonNull UUID userId,
      @Param("version") long version
  );
}
