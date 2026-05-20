package org.craft.mcs.cap.appleprofile.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.measurement.RangeMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.StatisticalMeasurement;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChemicalProfileTest {

  @Test
  void createsProfileWithExactRangeAndStatisticalCharacteristics() {
    final var sugar = ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
        SpecificGravityValue.of("1.052")
    ));
    final var profile = ChemicalProfile.builder()
        .sugar(sugar)
        .ph(RangeMeasurement.of(PhValue.of("3.20"), PhValue.of("3.60")))
        .titratableAcidity(StatisticalMeasurement.mean(TitratableAcidityValue.of("5.8")))
        .tanninity(RangeMeasurement.of(TanninityValue.of("650"), TanninityValue.of("1100")))
        .build();

    assertThat(profile.sugar()).hasValue(sugar);
    assertThat(profile.ph()).isPresent();
    assertThat(profile.titratableAcidity()).isPresent();
    assertThat(profile.tanninity()).isPresent();
  }

  @Test
  void rejectsEmptyProfile() {
    final var builder = ChemicalProfile.builder();

    assertThatThrownBy(builder::build)
        .isInstanceOf(DomainRuleViolationException.class);
  }
}