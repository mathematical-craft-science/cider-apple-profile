package org.craft.mcs.cap.shared.kernel.material.value;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class MaterialConsumptionTest {

  @Test
  void representsAConservedMaterialTransition() {
    final var sourceStateId = MaterialStateId.newId();
    final var remainingStateId = MaterialStateId.newId();

    assertThat(new MaterialConsumption(
        VolumeValue.ofLiters("250"),
        VolumeValue.ofLiters("100"),
        VolumeValue.ofLiters("150"),
        sourceStateId,
        remainingStateId
    ))
        .returns(VolumeValue.ofLiters("250"), MaterialConsumption::availableBefore)
        .returns(VolumeValue.ofLiters("100"), MaterialConsumption::consumed)
        .returns(VolumeValue.ofLiters("150"), MaterialConsumption::remaining)
        .returns(sourceStateId, MaterialConsumption::sourceStateId)
        .returns(remainingStateId, MaterialConsumption::remainingStateId);
  }

  @Test
  void rejectsZeroConsumedVolume() {
    assertThatThrownBy(() -> consumption("100", "0", "100"))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("consumed material volume must be positive");
  }

  @Test
  void rejectsVolumeThatIsNotConserved() {
    assertThatThrownBy(() -> consumption("250", "100", "140"))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage(
            "available material volume before consumption must equal consumed plus remaining volume"
        );
  }

  @Test
  void rejectsConsumptionWithoutAStateTransition() {
    final var stateId = MaterialStateId.newId();

    assertThatThrownBy(() -> new MaterialConsumption(
        VolumeValue.ofLiters("100"),
        VolumeValue.ofLiters("40"),
        VolumeValue.ofLiters("60"),
        stateId,
        stateId
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("material consumption must produce a new source state");
  }

  private static MaterialConsumption consumption(
      String availableBefore,
      String consumed,
      String remaining
  ) {
    return new MaterialConsumption(
        VolumeValue.ofLiters(availableBefore),
        VolumeValue.ofLiters(consumed),
        VolumeValue.ofLiters(remaining),
        MaterialStateId.newId(),
        MaterialStateId.newId()
    );
  }
}
