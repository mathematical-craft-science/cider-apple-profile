package org.craft.mcs.cap.account.contributor.domain.model;

import java.time.Instant;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContactVisibility;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorProfile;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorProfileRuleException;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorType;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContributorProfileTest {

  private static final Instant CONSENTED_AT = Instant.parse("2026-07-14T08:00:00Z");

  @Test
  void recordsExplicitConsentWhenContactsBecomePublic() {
    final var profile = ContributorProfile.create(
        AccountUserId.newId(),
        ContributorType.LABORATORY,
        DisplayName.of("North Orchard Lab"),
        null,
        EmailAddress.of("lab@example.com"),
        null,
        true,
        CONSENTED_AT
    );

    assertThat(profile)
        .returns(ContactVisibility.PUBLIC, ContributorProfile::contactVisibility)
        .returns(CONSENTED_AT, ContributorProfile::contactPublicationConsentedAt);
  }

  @Test
  void rejectsPublicVisibilityWithoutConsentEvidence() {
    assertThatThrownBy(() -> new ContributorProfile(
        AccountUserId.newId(),
        ContributorType.PRODUCER,
        DisplayName.of("North Orchard"),
        null,
        null,
        null,
        ContactVisibility.PUBLIC,
        null
    )).isInstanceOf(ContributorProfileRuleException.class)
        .hasMessage("public contributor contacts require explicit publication consent");
  }
}
