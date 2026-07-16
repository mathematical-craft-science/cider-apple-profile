package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.contributor;

import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContactVisibility;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import static java.util.Objects.requireNonNull;

/** Spring Data JDBC aggregate entity for an optional public contributor profile. */
@Table("contributor_profiles")
public class ContributorProfileJdbcEntity {

  @Id @NonNull @Column("user_id") UUID userId;
  @NonNull @Column("contributor_type") ContributorType contributorType;
  @NonNull @Column("public_name") String publicName;
  @Nullable @Column("website") String website;
  @Nullable @Column("public_email") String publicEmail;
  @Nullable @Column("public_phone") String publicPhone;
  @NonNull @Column("contact_visibility") ContactVisibility contactVisibility;
  @Nullable @Column("contact_publication_consented_at") Instant contactPublicationConsentedAt;
  @CreatedDate @Nullable @Column("created_at") Instant createdAt;
  @LastModifiedDate @Nullable @Column("updated_at") Instant updatedAt;
  @Version @Column("version") long version;

  @SuppressWarnings("NullAway.Init")
  public ContributorProfileJdbcEntity() {
  }

  ContributorProfileJdbcEntity(
      @NonNull UUID userId,
      @NonNull ContributorType contributorType,
      @NonNull String publicName,
      @Nullable String website,
      @Nullable String publicEmail,
      @Nullable String publicPhone,
      @NonNull ContactVisibility contactVisibility,
      @Nullable Instant contactPublicationConsentedAt
  ) {
    this.userId = requireNonNull(userId, "account user id cannot be null");
    this.contributorType = requireNonNull(contributorType, "contributor type cannot be null");
    this.publicName = requireNonNull(publicName, "contributor public name cannot be null");
    this.website = website;
    this.publicEmail = publicEmail;
    this.publicPhone = publicPhone;
    this.contactVisibility = requireNonNull(contactVisibility, "contact visibility cannot be null");
    this.contactPublicationConsentedAt = contactPublicationConsentedAt;
  }
}
