package org.craft.mcs.cap.account.contributor.application.usecase;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.craft.mcs.cap.core.account.contributor.application.port.in.ConfigureContributorProfileCommand;
import org.craft.mcs.cap.core.account.contributor.application.port.in.ContributorProfileView;
import org.craft.mcs.cap.core.account.contributor.application.port.out.ContributorProfileRepository;
import org.craft.mcs.cap.core.account.contributor.application.usecase.ContributorProfileService;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContactVisibility;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorProfile;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorType;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.user.application.usecase.CurrentAccountUserResolver;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.account.support.DirectTransactionHelper.INSTANCE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContributorProfileServiceTest {

  private static final Instant NOW = Instant.parse("2026-07-14T09:00:00Z");

  @Test
  void createsPublicContributorProfileWithExplicitConsentTime() {
    final var userResolver = mock(CurrentAccountUserResolver.class);
    final var repository = mock(ContributorProfileRepository.class);
    final var user = accountUser();
    final var command = new ConfigureContributorProfileCommand(
        ContributorType.LABORATORY,
        DisplayName.of("North Orchard Lab"),
        null,
        EmailAddress.of("lab@example.com"),
        null,
        true
    );
    when(userResolver.resolve()).thenReturn(user);
    when(repository.findByUserId(user.userId())).thenReturn(Optional.empty());
    when(repository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0, ContributorProfile.class));
    final var service = new ContributorProfileService(
        userResolver,
        repository,
        INSTANCE,
        Clock.fixed(NOW, ZoneOffset.UTC)
    );

    final var result = service.configure(command);

    assertThat(result)
        .returns(user.userId(), ContributorProfileView::userId)
        .returns(ContactVisibility.PUBLIC, ContributorProfileView::contactVisibility)
        .returns(NOW, ContributorProfileView::contactPublicationConsentedAt)
        .returns(command.publicEmail(), ContributorProfileView::publicEmail);
    verify(repository).save(any(ContributorProfile.class));
  }

  private static @NonNull AccountUser accountUser() {
    return AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("contributor"),
        EmailAddress.of("contributor@example.com"),
        DisplayName.of("Contributor"),
        null
    );
  }
}
