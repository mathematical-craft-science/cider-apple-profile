package org.craft.mcs.cap.core.account.contributor.application.port.in;

import java.time.Instant;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContactVisibility;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorType;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.craft.mcs.cap.core.account.shared.domain.value.PhoneNumber;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Current account's complete contributor-profile management view. */
public record ContributorProfileView(
    @NonNull AccountUserId userId,
    @NonNull ContributorType contributorType,
    @NonNull DisplayName publicName,
    @Nullable HttpUrl website,
    @Nullable EmailAddress publicEmail,
    @Nullable PhoneNumber publicPhone,
    @NonNull ContactVisibility contactVisibility,
    @Nullable Instant contactPublicationConsentedAt
) {

  public ContributorProfileView {
    requireNonNull(userId, "account user id cannot be null");
    requireNonNull(contributorType, "contributor type cannot be null");
    requireNonNull(publicName, "contributor public name cannot be null");
    requireNonNull(contactVisibility, "contact visibility cannot be null");
  }
}
