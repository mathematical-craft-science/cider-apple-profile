package org.craft.mcs.cap.core.account.contributor.application.usecase;

import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.contributor.application.ContributorProfileNotFoundException;
import org.craft.mcs.cap.core.account.contributor.application.port.in.ConfigureContributorProfileCommand;
import org.craft.mcs.cap.core.account.contributor.application.port.in.ContributorProfileUseCase;
import org.craft.mcs.cap.core.account.contributor.application.port.in.ContributorProfileView;
import org.craft.mcs.cap.core.account.contributor.application.port.out.ContributorProfileRepository;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContactVisibility;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorProfile;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.user.application.usecase.CurrentAccountUserResolver;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/** Manages CAP-owned contributor identity and explicit public-contact consent. */
@Component
@RequiredArgsConstructor
public final class ContributorProfileService implements ContributorProfileUseCase {

  private final @NonNull CurrentAccountUserResolver currentAccountUserResolver;
  private final @NonNull ContributorProfileRepository contributorProfileRepository;
  private final @NonNull TransactionHelper transactionHelper;
  private final @NonNull Clock clock;

  @Override
  public @NonNull ContributorProfileView get() {
    final var user = currentAccountUserResolver.resolve();
    return toView(findProfile(user.userId()));
  }

  @Override
  public @NonNull ContributorProfileView configure(
      @NonNull ConfigureContributorProfileCommand command
  ) {
    requireNonNull(command, "configure contributor profile command cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> {
      final var user = currentAccountUserResolver.resolve();
      final var existing = contributorProfileRepository.findVersionedById(user.userId());
      final var consentedAt = consentTime(
          existing.map(VersionedAggregate::aggregate).orElse(null),
          command.publishContacts()
      );
      final var profile = existing
          .map(current -> current.aggregate().update(
              command.contributorType(),
              command.publicName(),
              command.website(),
              command.publicEmail(),
              command.publicPhone(),
              command.publishContacts(),
              consentedAt
          ))
          .orElseGet(() -> ContributorProfile.create(
              user.userId(),
              command.contributorType(),
              command.publicName(),
              command.website(),
              command.publicEmail(),
              command.publicPhone(),
              command.publishContacts(),
              consentedAt
          ));
      final var saved = existing
          .map(state -> contributorProfileRepository.update(state.withAggregate(profile)).aggregate())
          .orElseGet(() -> contributorProfileRepository.create(profile));
      return toView(saved);
    });
  }

  @Override
  public void delete() {
    transactionHelper.executeInTransaction(() -> {
      final var user = currentAccountUserResolver.resolve();
      final var profile = contributorProfileRepository.findVersionedById(user.userId())
          .orElseThrow(ContributorProfileNotFoundException::new);
      contributorProfileRepository.delete(profile);
    });
  }

  private @NonNull ContributorProfile findProfile(@NonNull AccountUserId userId) {
    return contributorProfileRepository.findByUserId(userId)
        .orElseThrow(ContributorProfileNotFoundException::new);
  }

  private @NonNull Instant consentTime(
      @Nullable ContributorProfile existing,
      boolean publishContacts
  ) {
    if (publishContacts
        && existing != null
        && existing.contactVisibility() == ContactVisibility.PUBLIC
        && existing.contactPublicationConsentedAt() != null) {
      return requireNonNull(
          existing.contactPublicationConsentedAt(),
          "public contributor profile consent time cannot be null"
      );
    }
    return clock.instant();
  }

  private static @NonNull ContributorProfileView toView(@NonNull ContributorProfile profile) {
    return new ContributorProfileView(
        profile.userId(),
        profile.contributorType(),
        profile.publicName(),
        profile.website(),
        profile.publicEmail(),
        profile.publicPhone(),
        profile.contactVisibility(),
        profile.contactPublicationConsentedAt()
    );
  }
}
