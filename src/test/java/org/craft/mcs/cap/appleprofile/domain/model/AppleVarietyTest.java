package org.craft.mcs.cap.appleprofile.domain.model;

import java.math.BigDecimal;
import java.util.UUID;
import org.craft.mcs.cap.shared.domain.measurement.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppleVarietyTest {

  @Test
  void createsImmutableAppleVarietyAggregate() {
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
  void replacesChemicalProfileWithoutMutatingCurrentAggregate() {
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

    final var updated = appleVariety.withChemicalProfile(updatedProfile);

    assertThat(updated).isNotSameAs(appleVariety);
    assertThat(updated.chemicalProfile()).isEqualTo(updatedProfile);

    assertThat(appleVariety.chemicalProfile()
        .sugar()
        .flatMap(MeasuredCharacteristic::representativeValue)
        .map(SugarConcentration::specificGravity)
        .map(SpecificGravityValue::asBigDecimal)
        .map(BigDecimal::toPlainString))
        .hasValue("1.045");
  }

  @Test
  void rejectsBlankName() {
    assertThatThrownBy(() -> AppleVarietyName.of(" "))
        .isInstanceOf(IllegalArgumentException.class);
  }
}