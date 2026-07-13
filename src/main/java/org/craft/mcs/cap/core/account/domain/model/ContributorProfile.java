package org.craft.mcs.cap.core.account.domain.model;

import org.craft.mcs.cap.core.account.domain.model.value.AccountUserId;
import org.craft.mcs.cap.core.account.domain.model.value.DisplayName;
import org.craft.mcs.cap.core.account.domain.model.value.EmailAddress;
import org.craft.mcs.cap.core.account.domain.model.value.HttpUrl;
import org.craft.mcs.cap.core.account.domain.model.value.PhoneNumber;
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
    @NonNull ContactVisibility contactVisibility
) {

  public ContributorProfile {
    requireNonNull(userId, "account user id cannot be null");
    requireNonNull(contributorType, "contributor type cannot be null");
    requireNonNull(publicName, "contributor public name cannot be null");
    requireNonNull(contactVisibility, "contact visibility cannot be null");
  }
}
