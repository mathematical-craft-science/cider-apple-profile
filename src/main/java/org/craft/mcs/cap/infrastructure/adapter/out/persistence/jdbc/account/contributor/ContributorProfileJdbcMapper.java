package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.contributor;

import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorProfile;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.craft.mcs.cap.core.account.shared.domain.value.PhoneNumber;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Maps the ContributorProfile aggregate to its Spring Data JDBC representation. */
final class ContributorProfileJdbcMapper {

  private ContributorProfileJdbcMapper() {
  }

  static @NonNull ContributorProfileJdbcEntity fromDomain(@NonNull ContributorProfile profile) {
    return new ContributorProfileJdbcEntity(
        profile.userId().value(),
        profile.contributorType(),
        profile.publicName().value(),
        profile.website() == null ? null : profile.website().value().toString(),
        profile.publicEmail() == null ? null : profile.publicEmail().value(),
        profile.publicPhone() == null ? null : profile.publicPhone().value(),
        profile.contactVisibility(),
        profile.contactPublicationConsentedAt()
    );
  }

  static @NonNull ContributorProfileJdbcEntity updateFromDomain(
      @NonNull ContributorProfile profile,
      @NonNull ContributorProfileJdbcEntity entity
  ) {
    entity.contributorType = profile.contributorType();
    entity.publicName = profile.publicName().value();
    entity.website = profile.website() == null ? null : profile.website().value().toString();
    entity.publicEmail = profile.publicEmail() == null ? null : profile.publicEmail().value();
    entity.publicPhone = profile.publicPhone() == null ? null : profile.publicPhone().value();
    entity.contactVisibility = profile.contactVisibility();
    entity.contactPublicationConsentedAt = profile.contactPublicationConsentedAt();
    return entity;
  }

  static @NonNull ContributorProfile toDomain(@NonNull ContributorProfileJdbcEntity entity) {
    return new ContributorProfile(
        AccountUserId.of(entity.userId),
        entity.contributorType,
        DisplayName.of(entity.publicName),
        HttpUrl.fromNullable(entity.website),
        EmailAddress.fromNullable(entity.publicEmail),
        PhoneNumber.fromNullable(entity.publicPhone),
        entity.contactVisibility,
        entity.contactPublicationConsentedAt
    );
  }

  static @NonNull VersionedAggregate<ContributorProfile> toVersionedAggregate(
      @NonNull ContributorProfileJdbcEntity entity
  ) {
    return new VersionedAggregate<>(toDomain(entity), AggregateRevision.of(entity.version));
  }

  static long revision(@NonNull ContributorProfileJdbcEntity entity) {
    return requireNonNull(entity, "contributor profile row cannot be null").version;
  }
}
