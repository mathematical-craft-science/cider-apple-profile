package org.craft.mcs.cap.appleprofile.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import java.util.UUID;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppleVarietyTest {

  @Test
  void createsAppleVarietyAggregate() {
    final var id = AppleVarietyId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    final var profile = ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.048")
        )))
        .build();

    final var appleVariety = AppleVariety.create(
        id,
        AppleVarietyName.of(" Kingston Black "),
        profile
    );

    assertThat(appleVariety.varietyId()).isEqualTo(id);
    assertThat(appleVariety.name().value()).isEqualTo("Kingston Black");
    assertThat(appleVariety.chemicalProfile()).isEqualTo(profile);
  }

  @Test
  void changesChemicalProfile() {
    final var appleVariety = AppleVariety.create(
        AppleVarietyId.of(UUID.fromString("00000000-0000-0000-0000-000000000001")),
        AppleVarietyName.of("Dabinett"),
        ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
                SpecificGravityValue.of("1.045")
            )))
            .build()
    );
    final var updatedProfile = ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.055")
        )))
        .build();

    final var updated = appleVariety.changeChemicalProfile(updatedProfile);

    assertThat(updated).isSameAs(appleVariety);
    assertThat(appleVariety.chemicalProfile()).isEqualTo(updatedProfile);
  }

  @Test
  void rejectsYeastAssimilableNitrogenInCatalogProfile() {
    final var profile = ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.048")
        )))
        .yeastAssimilableNitrogen(ExactMeasurement.of(YeastAssimilableNitrogenValue.of("140")))
        .build();

    assertThatThrownBy(() -> AppleVariety.create(
        AppleVarietyId.of(UUID.fromString("00000000-0000-0000-0000-000000000001")),
        AppleVarietyName.of("Kingston Black"),
        profile
    ))
        .isInstanceOf(DomainRuleViolationException.class);
  }

  @Test
  void rejectsBlankName() {
    assertThatThrownBy(() -> AppleVarietyName.of(" "))
        .isInstanceOf(DomainRuleViolationException.class);
  }
}
