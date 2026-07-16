package org.craft.mcs.cap.core.account.contributor.domain.model;

import java.time.Instant;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.craft.mcs.cap.core.account.shared.domain.value.PhoneNumber;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Optional public contributor identity used by catalog, laboratory, and regional-profile flows. */
@AggregateRoot
public record ContributorProfile(
    @NonNull AccountUserId userId,
    @NonNull ContributorType contributorType,
    @NonNull DisplayName publicName,
    @Nullable HttpUrl website,
    @Nullable EmailAddress publicEmail,
    @Nullable PhoneNumber publicPhone,
    @NonNull ContactVisibility contactVisibility,
    @Nullable Instant contactPublicationConsentedAt
) {

  public ContributorProfile {
    requireNonNull(userId, "account user id cannot be null");
    requireNonNull(contributorType, "contributor type cannot be null");
    requireNonNull(publicName, "contributor public name cannot be null");
    requireNonNull(contactVisibility, "contact visibility cannot be null");
    if (contactVisibility == ContactVisibility.PUBLIC && contactPublicationConsentedAt == null) {
      throw new ContributorProfileRuleException(
          "public contributor contacts require explicit publication consent"
      );
    }
    if (contactVisibility == ContactVisibility.PRIVATE && contactPublicationConsentedAt != null) {
      throw new ContributorProfileRuleException(
          "private contributor contacts cannot retain publication consent"
      );
    }
  }

  /** Creates a CAP-owned contributor profile with explicit contact-publication consent state. */
  public static @NonNull ContributorProfile create(
      @NonNull AccountUserId userId,
      @NonNull ContributorType contributorType,
      @NonNull DisplayName publicName,
      @Nullable HttpUrl website,
      @Nullable EmailAddress publicEmail,
      @Nullable PhoneNumber publicPhone,
      boolean publishContacts,
      @NonNull Instant consentedAt
  ) {
    requireNonNull(consentedAt, "contact publication consent time cannot be null");
    return new ContributorProfile(
        userId,
        contributorType,
        publicName,
        website,
        publicEmail,
        publicPhone,
        publishContacts ? ContactVisibility.PUBLIC : ContactVisibility.PRIVATE,
        publishContacts ? consentedAt : null
    );
  }

  /** Replaces editable contributor data and records or withdraws contact-publication consent. */
  public @NonNull ContributorProfile update(
      @NonNull ContributorType updatedType,
      @NonNull DisplayName updatedPublicName,
      @Nullable HttpUrl updatedWebsite,
      @Nullable EmailAddress updatedPublicEmail,
      @Nullable PhoneNumber updatedPublicPhone,
      boolean publishContacts,
      @NonNull Instant consentedAt
  ) {
    return create(
        userId,
        updatedType,
        updatedPublicName,
        updatedWebsite,
        updatedPublicEmail,
        updatedPublicPhone,
        publishContacts,
        consentedAt
    );
  }
}
