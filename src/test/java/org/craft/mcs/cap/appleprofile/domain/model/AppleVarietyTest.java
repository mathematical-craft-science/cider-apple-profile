package org.craft.mcs.cap.appleprofile.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import java.util.UUID;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyReferenceProfile;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppleVarietyTest {

  @Test
  void createsAppleVarietyAggregate() {
    final var id = AppleVarietyId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    final var profile = profile("1.048");

    final var appleVariety = AppleVariety.create(
        id,
        AppleVarietyName.of(" Kingston Black "),
        profile
    );

    assertThat(appleVariety.varietyId()).isEqualTo(id);
    assertThat(appleVariety.name().value()).isEqualTo("Kingston Black");
    assertThat(appleVariety.referenceProfile()).isEqualTo(profile);
  }

  @Test
  void changesReferenceProfile() {
    final var appleVariety = AppleVariety.create(
        AppleVarietyId.of(UUID.fromString("00000000-0000-0000-0000-000000000001")),
        AppleVarietyName.of("Dabinett"),
        profile("1.045")
    );
    final var updatedProfile = profile("1.055");

    final var updated = appleVariety.changeReferenceProfile(updatedProfile);

    assertThat(updated).isSameAs(appleVariety);
    assertThat(appleVariety.referenceProfile()).isEqualTo(updatedProfile);
  }

  @Test
  void rejectsEmptyReferenceProfile() {
    assertThatThrownBy(() -> new AppleVarietyReferenceProfile(null, null, null, null))
        .isInstanceOf(DomainRuleViolationException.class);
  }

  @Test
  void rejectsBlankName() {
    assertThatThrownBy(() -> AppleVarietyName.of(" "))
        .isInstanceOf(DomainRuleViolationException.class);
  }

  private static AppleVarietyReferenceProfile profile(String specificGravity) {
    return new AppleVarietyReferenceProfile(
        PointCharacteristicValue.of(SpecificGravityValue.of(specificGravity)),
        null,
        null,
        null
    );
  }
}
