package org.craft.mcs.cap.shared.kernel.material.value;

import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class BatchMaterialStateTest {

  @Test
  void createsActiveMaterialWithNewIdentity() {
    final var state = BatchMaterialState.active(VolumeValue.ofLiters("100"));

    assertThat(state)
        .returns(BatchLifecycle.ACTIVE, BatchMaterialState::lifecycle)
        .returns(VolumeValue.ofLiters("100"), BatchMaterialState::volume)
        .extracting(BatchMaterialState::stateId)
        .isNotNull();
  }

  @Test
  void consumingRemainingVolumeDepletesMaterial() {
    final var initial = BatchMaterialState.active(VolumeValue.ofLiters("100"));

    final var consumed = initial.consume(VolumeValue.ofLiters("100"));

    assertThat(consumed)
        .returns(BatchLifecycle.DEPLETED, BatchMaterialState::lifecycle)
        .returns(VolumeValue.ofLiters("0"), BatchMaterialState::volume)
        .satisfies(state -> assertThat(state.stateId()).isNotEqualTo(initial.stateId()));
  }

  @Test
  void reducingVolumeKeepsMaterialActiveAndAdvancesIdentity() {
    final var initial = BatchMaterialState.active(VolumeValue.ofLiters("100"));

    final var reduced = initial.reduceTo(VolumeValue.ofLiters("92"));

    assertThat(reduced)
        .returns(BatchLifecycle.ACTIVE, BatchMaterialState::lifecycle)
        .returns(VolumeValue.ofLiters("92"), BatchMaterialState::volume)
        .satisfies(state -> assertThat(state.stateId()).isNotEqualTo(initial.stateId()));
  }

  @Test
  void rejectsActiveStateWithoutMaterial() {
    assertThatThrownBy(() -> BatchMaterialState.restore(
        BatchLifecycle.ACTIVE,
        VolumeValue.ofLiters("0"),
        MaterialStateId.newId()
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("active batch material volume must be greater than zero");
  }

  @ParameterizedTest
  @EnumSource(value = BatchLifecycle.class, names = {"DEPLETED", "DISCARDED"})
  void rejectsTerminalStateWithRemainingMaterial(BatchLifecycle lifecycle) {
    assertThatThrownBy(() -> BatchMaterialState.restore(
        lifecycle,
        VolumeValue.ofLiters("1"),
        MaterialStateId.newId()
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("inactive batch material volume must be zero");
  }

  @Test
  void rejectsConsumptionBeyondAvailableMaterial() {
    final var state = BatchMaterialState.active(VolumeValue.ofLiters("100"));

    assertThatThrownBy(() -> state.consume(VolumeValue.ofLiters("101")))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("consumed material volume exceeds available volume");
  }

  @Test
  void rejectsTransitionsFromTerminalState() {
    final var depleted = BatchMaterialState.restore(
        BatchLifecycle.DEPLETED,
        VolumeValue.ofLiters("0"),
        MaterialStateId.newId()
    );

    assertThatThrownBy(depleted::advance)
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("cannot advance material state when batch lifecycle is DEPLETED");
  }
}
