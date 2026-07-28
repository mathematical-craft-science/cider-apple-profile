package org.craft.mcs.cap.shared.kernel.material.value;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class MaterialAvailabilityTest {

  @Test
  void representsMaterialSelectedFromOneState() {
    final var materialStateId = MaterialStateId.newId();

    assertThat(new MaterialAvailability(
        VolumeValue.ofLiters("60"),
        VolumeValue.ofLiters("100"),
        materialStateId
    ))
        .returns(VolumeValue.ofLiters("60"), MaterialAvailability::requestedVolume)
        .returns(VolumeValue.ofLiters("100"), MaterialAvailability::availableVolume)
        .returns(materialStateId, MaterialAvailability::materialStateId);
  }

  @Test
  void rejectsZeroRequestedVolume() {
    assertThatThrownBy(() -> availability("0", "100"))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("requested material volume must be positive");
  }

  @Test
  void rejectsZeroAvailableVolume() {
    assertThatThrownBy(() -> availability("1", "0"))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("available material volume must be positive");
  }

  @Test
  void rejectsSelectionBeyondAvailableVolume() {
    assertThatThrownBy(() -> availability("101", "100"))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("requested material volume cannot exceed available volume");
  }

  private static MaterialAvailability availability(
      String requested,
      String available
  ) {
    return new MaterialAvailability(
        VolumeValue.ofLiters(requested),
        VolumeValue.ofLiters(available),
        MaterialStateId.newId()
    );
  }
}
