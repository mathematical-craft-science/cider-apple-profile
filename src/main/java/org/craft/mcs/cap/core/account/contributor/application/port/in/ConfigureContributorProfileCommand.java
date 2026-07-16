package org.craft.mcs.cap.core.account.contributor.application.port.in;

import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorType;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.craft.mcs.cap.core.account.shared.domain.value.PhoneNumber;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Defines the current account's public contributor identity and contact-publication choice. */
public record ConfigureContributorProfileCommand(
    @NonNull ContributorType contributorType,
    @NonNull DisplayName publicName,
    @Nullable HttpUrl website,
    @Nullable EmailAddress publicEmail,
    @Nullable PhoneNumber publicPhone,
    boolean publishContacts
) {

  public ConfigureContributorProfileCommand {
    requireNonNull(contributorType, "contributor type cannot be null");
    requireNonNull(publicName, "contributor public name cannot be null");
  }
}
