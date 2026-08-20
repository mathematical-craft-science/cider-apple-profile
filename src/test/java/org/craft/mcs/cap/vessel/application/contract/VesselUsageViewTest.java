package org.craft.mcs.cap.vessel.application.contract;

import org.craft.mcs.cap.core.vessel.application.VesselUsageProjectionException;
import org.craft.mcs.cap.core.vessel.application.contract.VesselCurrentContentType;
import org.craft.mcs.cap.core.vessel.application.contract.VesselCurrentContentView;
import org.craft.mcs.cap.core.vessel.application.contract.VesselUsageClassification;
import org.craft.mcs.cap.core.vessel.application.contract.VesselUsageView;
import org.craft.mcs.cap.core.vessel.application.contract.VesselView;
import org.craft.mcs.cap.core.vessel.domain.model.VesselMaterial;
import org.craft.mcs.cap.core.vessel.domain.model.VesselOperationalStatus;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


class VesselUsageViewTest {

  @Test
  void derivesEmptyUsageWithoutDuplicatedState() {
    final var usage = VesselUsageView.empty(vessel(VesselId.newId()));

    assertThat(usage)
        .returns(null, VesselUsageView::currentContent)
        .returns(VolumeValue.ofLiters("0"), VesselUsageView::usedVolume)
        .returns(VolumeValue.ofLiters("500"), VesselUsageView::remainingCapacity)
        .returns(VesselUsageClassification.EMPTY, VesselUsageView::usage);
  }

  @Test
  void derivesPartialAndFullUsageFromCurrentContent() {
    final var vesselId = VesselId.newId();
    final var vessel = vessel(vesselId);

    assertThat(new VesselUsageView(vessel, content(vesselId, "125")))
        .returns(VolumeValue.ofLiters("125"), VesselUsageView::usedVolume)
        .returns(VolumeValue.ofLiters("375"), VesselUsageView::remainingCapacity)
        .returns(VesselUsageClassification.PARTIALLY_FILLED, VesselUsageView::usage);
    assertThat(new VesselUsageView(vessel, content(vesselId, "500")))
        .returns(VolumeValue.ofLiters("500"), VesselUsageView::usedVolume)
        .returns(VolumeValue.ofLiters("0"), VesselUsageView::remainingCapacity)
        .returns(VesselUsageClassification.FULL, VesselUsageView::usage);
  }

  @Test
  void rejectsContentFromAnotherVesselOrAboveCapacity() {
    final var vessel = vessel(VesselId.newId());

    assertThatThrownBy(() -> new VesselUsageView(vessel, content(VesselId.newId(), "100")))
        .isInstanceOf(VesselUsageProjectionException.class)
        .hasMessage("current content belongs to another vessel");
    assertThatThrownBy(() -> new VesselUsageView(vessel, content(vessel.vesselId(), "500.001")))
        .isInstanceOf(VesselUsageProjectionException.class)
        .hasMessage("current content exceeds vessel nominal capacity");
  }

  @Test
  void rejectsZeroVolumeActiveContent() {
    assertThatThrownBy(() -> content(VesselId.newId(), "0"))
        .isInstanceOf(VesselUsageProjectionException.class)
        .hasMessage("active vessel content volume must be greater than zero");
  }

  private static VesselView vessel(VesselId vesselId) {
    return new VesselView(
        vesselId,
        WorkspaceId.newId(),
        "Tank F-03",
        VolumeValue.ofLiters("500"),
        VesselMaterial.STAINLESS_STEEL,
        VesselOperationalStatus.OPERATIONAL,
        AggregateRevision.of(1)
    );
  }

  private static VesselCurrentContentView content(
      VesselId vesselId,
      String volume
  ) {
    return new VesselCurrentContentView(
        vesselId,
        VesselCurrentContentType.FEEDSTOCK_BATCH,
        newUuid(),
        MaterialStateId.newId(),
        BatchName.of("October juice"),
        VolumeValue.ofLiters(volume)
    );
  }
}
