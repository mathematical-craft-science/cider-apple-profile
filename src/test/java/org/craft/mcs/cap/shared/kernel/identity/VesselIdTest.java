package org.craft.mcs.cap.shared.kernel.identity;

import java.util.UUID;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class VesselIdTest {

  @Test
  void acceptsRfc9562UuidVersionSeven() {
    final var value = UUID.fromString("019fd42c-3ac8-7000-91fe-48173dfc0c7e");

    assertThat(VesselId.of(value))
        .returns(value, VesselId::value);
  }

  @Test
  void rejectsUuidFromAnotherVersion() {
    final var versionFour = UUID.fromString("4d96759c-86ff-4d39-8b19-241bc60f82ff");

    assertThatThrownBy(() -> VesselId.of(versionFour))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("vessel id must be an RFC 9562 UUID version 7");
  }

  @Test
  void rejectsUuidVersionSevenWithNonRfcVariant() {
    final var nonRfcVariant = UUID.fromString("019fd42c-3ac8-7000-11fe-48173dfc0c7e");

    assertThatThrownBy(() -> VesselId.of(nonRfcVariant))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("vessel id must be an RFC 9562 UUID version 7");
  }
}
