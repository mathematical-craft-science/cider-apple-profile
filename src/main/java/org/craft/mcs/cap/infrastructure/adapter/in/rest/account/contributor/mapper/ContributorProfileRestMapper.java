package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.contributor.mapper;

import org.craft.mcs.cap.core.account.contributor.application.port.in.ConfigureContributorProfileCommand;
import org.craft.mcs.cap.core.account.contributor.application.port.in.ContributorProfileView;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.craft.mcs.cap.core.account.shared.domain.value.PhoneNumber;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.contributor.dto.ConfigureContributorProfileRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.contributor.dto.ContributorProfileResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Maps contributor-profile transport data without leaking HTTP primitives into core. */
public final class ContributorProfileRestMapper {

  private ContributorProfileRestMapper() {
  }

  public static @NonNull ConfigureContributorProfileCommand toCommand(
      @NonNull ConfigureContributorProfileRequest request
  ) {
    requireNonNull(request, "configure contributor profile request cannot be null");
    return new ConfigureContributorProfileCommand(
        request.contributorType(),
        DisplayName.of(request.publicName()),
        HttpUrl.fromNullable(request.website()),
        EmailAddress.fromNullable(request.publicEmail()),
        PhoneNumber.fromNullable(request.publicPhone()),
        requireNonNull(request.publishContacts(), "contact publication choice cannot be null")
    );
  }

  public static @NonNull ContributorProfileResponse toResponse(@NonNull ContributorProfileView view) {
    return new ContributorProfileResponse(
        view.userId().value(),
        view.contributorType(),
        view.publicName().value(),
        view.website() == null ? null : view.website().value().toString(), // NOSONAR
        view.publicEmail() == null ? null : view.publicEmail().value(), // NOSONAR
        view.publicPhone() == null ? null : view.publicPhone().value(), // NOSONAR
        view.contactVisibility(),
        view.contactPublicationConsentedAt()
    );
  }
}
