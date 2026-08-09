package org.craft.mcs.cap.vessel.domain.model;

import org.craft.mcs.cap.core.vessel.domain.model.Vessel;
import org.craft.mcs.cap.core.vessel.domain.model.VesselMaterial;
import org.craft.mcs.cap.core.vessel.domain.model.VesselOperationalStatus;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class VesselTest {

  @Test
  void createsOperationalVesselWithNormalizedCharacteristics() {
    final var vesselId = VesselId.newId();
    final var workspaceId = WorkspaceId.newId();

    final var vessel = Vessel.create(
        vesselId,
        workspaceId,
        "  Tank F-03  ",
        VolumeValue.ofLiters("500"),
        VesselMaterial.STAINLESS_STEEL
    );

    assertThat(vessel)
        .returns(vesselId, Vessel::vesselId)
        .returns(workspaceId, Vessel::workspaceId)
        .returns("Tank F-03", Vessel::name)
        .returns(VolumeValue.ofLiters("500"), Vessel::nominalCapacity)
        .returns(VesselMaterial.STAINLESS_STEEL, Vessel::material)
        .returns(VesselOperationalStatus.OPERATIONAL, Vessel::operationalStatus);
  }

  @Test
  void rejectsInvalidCatalogCharacteristics() {
    final var validCapacity = VolumeValue.ofLiters("500");

    assertThatThrownBy(() -> Vessel.create(
        VesselId.newId(),
        WorkspaceId.newId(),
        " ",
        validCapacity,
        VesselMaterial.GLASS
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("vessel name cannot be blank");

    assertThatThrownBy(() -> Vessel.create(
        VesselId.newId(),
        WorkspaceId.newId(),
        "x".repeat(161),
        validCapacity,
        VesselMaterial.GLASS
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("vessel name length must not exceed 160 characters");

    assertThatThrownBy(() -> Vessel.create(
        VesselId.newId(),
        WorkspaceId.newId(),
        "Tank F-03",
        VolumeValue.ofLiters("0"),
        VesselMaterial.GLASS
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("vessel nominal capacity must be greater than zero");
  }

  @Test
  void validatesOperationalAvailabilityAndNominalCapacity() {
    final var vessel = vessel();

    assertThatCode(() -> vessel.requireCanReceive(VolumeValue.ofLiters("500")))
        .doesNotThrowAnyException();
    assertThatThrownBy(() -> vessel.requireCanReceive(VolumeValue.ofLiters("500.001")))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("material volume exceeds vessel nominal capacity");

    vessel.changeOperationalStatus(VesselOperationalStatus.OUT_OF_SERVICE);

    assertThatThrownBy(() -> vessel.requireCanReceive(VolumeValue.ofLiters("1")))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("vessel is not operational");
  }

  @Test
  void rejectsZeroMaterialVolumeWithMaterialSpecificRule() {
    final var vessel = vessel();

    assertThatThrownBy(() -> vessel.requireCanReceive(VolumeValue.ofLiters("0")))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("material volume must be greater than zero");
  }

  @Test
  void validatesExistingContentCapacityRegardlessOfOperationalStatus() {
    final var vessel = vessel().changeOperationalStatus(VesselOperationalStatus.OUT_OF_SERVICE);

    assertThatCode(() -> vessel.requireCapacityFor(VolumeValue.ofLiters("500")))
        .doesNotThrowAnyException();
    assertThatThrownBy(() -> vessel.requireCapacityFor(VolumeValue.ofLiters("500.001")))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("material volume exceeds vessel nominal capacity");
  }

  @ParameterizedTest
  @CsvSource({
      "OPERATIONAL, OPERATIONAL",
      "OPERATIONAL, OUT_OF_SERVICE",
      "OPERATIONAL, RETIRED",
      "OUT_OF_SERVICE, OPERATIONAL",
      "OUT_OF_SERVICE, OUT_OF_SERVICE",
      "OUT_OF_SERVICE, RETIRED"
  })
  void supportsAllowedOperationalStatusTransitions(
      VesselOperationalStatus source,
      VesselOperationalStatus target
  ) {
    final var vessel = Vessel.restore(
        VesselId.newId(),
        WorkspaceId.newId(),
        "Tank F-03",
        VolumeValue.ofLiters("500"),
        VesselMaterial.STAINLESS_STEEL,
        source
    );

    vessel.changeOperationalStatus(target);

    assertThat(vessel).returns(target, Vessel::operationalStatus);
  }

  @Test
  void allowsTemporaryShutdownButMakesRetirementTerminal() {
    final var vessel = vessel();

    vessel.changeOperationalStatus(VesselOperationalStatus.OUT_OF_SERVICE)
        .changeOperationalStatus(VesselOperationalStatus.OPERATIONAL)
        .changeOperationalStatus(VesselOperationalStatus.RETIRED);

    assertThat(vessel).returns(VesselOperationalStatus.RETIRED, Vessel::operationalStatus);
    assertThatThrownBy(() -> vessel.changeOperationalStatus(VesselOperationalStatus.OPERATIONAL))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("cannot change vessel operational status after vessel retirement");
    assertThatThrownBy(() -> vessel.updateCharacteristics(
        "Renamed tank",
        VolumeValue.ofLiters("600"),
        VesselMaterial.STAINLESS_STEEL
    ))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("cannot update vessel characteristics after vessel retirement");
  }

  @Test
  void updatesCatalogCharacteristicsWithoutChangingIdentityOrWorkspace() {
    final var vessel = vessel();
    final var vesselId = vessel.vesselId();
    final var workspaceId = vessel.workspaceId();

    vessel.updateCharacteristics(
        "  Barrel B-12  ",
        VolumeValue.ofLiters("225"),
        VesselMaterial.WOOD
    );

    assertThat(vessel)
        .returns(vesselId, Vessel::vesselId)
        .returns(workspaceId, Vessel::workspaceId)
        .returns("Barrel B-12", Vessel::name)
        .returns(VolumeValue.ofLiters("225"), Vessel::nominalCapacity)
        .returns(VesselMaterial.WOOD, Vessel::material);
  }

  private static Vessel vessel() {
    return Vessel.create(
        VesselId.newId(),
        WorkspaceId.newId(),
        "Tank F-03",
        VolumeValue.ofLiters("500"),
        VesselMaterial.STAINLESS_STEEL
    );
  }
}
