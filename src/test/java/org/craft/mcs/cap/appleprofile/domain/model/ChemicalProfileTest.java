package org.craft.mcs.cap.appleprofile.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChemicalProfileTest {

  @Test
  void createsProfileWithPointAndIntervalCharacteristics() {
    final var sugar = PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
        SpecificGravityValue.of("1.052")
    ));
    final var profile = ChemicalProfile.builder()
        .sugar(sugar)
        .ph(IntervalCharacteristicValue.of(PhValue.of("3.20"), PhValue.of("3.60")))
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("5.8")))
        .tannin(IntervalCharacteristicValue.of(TanninValue.of("650"), TanninValue.of("1100")))
        .build();

    assertThat(profile.sugar()).hasValue(sugar);
    assertThat(profile.ph()).isPresent();
    assertThat(profile.titratableAcidity()).isPresent();
    assertThat(profile.tannin()).isPresent();
  }

  @Test
  void rejectsEmptyProfile() {
    final var builder = ChemicalProfile.builder();

    assertThatThrownBy(builder::build)
        .isInstanceOf(DomainRuleViolationException.class);
  }
}
