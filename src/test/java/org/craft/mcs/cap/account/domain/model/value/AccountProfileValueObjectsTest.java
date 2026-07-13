package org.craft.mcs.cap.account.domain.model.value;

import java.net.URI;
import org.craft.mcs.cap.core.account.domain.model.value.DisplayName;
import org.craft.mcs.cap.core.account.domain.model.value.EmailAddress;
import org.craft.mcs.cap.core.account.domain.model.value.HttpUrl;
import org.craft.mcs.cap.core.account.domain.model.value.PhoneNumber;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class AccountProfileValueObjectsTest {

  @Test
  void normalizesEmailAddressAndExposesLocalPart() {
    assertThat(EmailAddress.of(" technologist@example.com "))
        .returns("technologist@example.com", EmailAddress::value)
        .returns("technologist", EmailAddress::localPart);
  }

  @Test
  void rejectsStructurallyInvalidEmailAddress() {
    assertThatThrownBy(() -> EmailAddress.of("invalid-email"))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("email address has invalid format");
  }

  @Test
  void normalizesDisplayName() {
    assertThat(DisplayName.of(" CAP Technologist "))
        .returns("CAP Technologist", DisplayName::value);
  }

  @Test
  void rejectsBlankDisplayName() {
    assertThatThrownBy(() -> DisplayName.of("  "))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("display name cannot be blank");
  }

  @Test
  void acceptsAbsoluteHttpUrl() {
    assertThat(HttpUrl.of(" https://example.com/avatar.png "))
        .returns(URI.create("https://example.com/avatar.png"), HttpUrl::value);
  }

  @Test
  void rejectsNonHttpUrl() {
    assertThatThrownBy(() -> HttpUrl.of("file:///tmp/avatar.png"))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("HTTP URL must be absolute and use HTTP or HTTPS");
  }

  @Test
  void preservesDisplayableInternationalPhoneNumber() {
    assertThat(PhoneNumber.of(" +1 555 0100 "))
        .returns("+1 555 0100", PhoneNumber::value);
  }

  @Test
  void rejectsPhoneNumberWithoutDigits() {
    assertThatThrownBy(() -> PhoneNumber.of("not-a-phone"))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("phone number must contain at least one digit");
  }
}
