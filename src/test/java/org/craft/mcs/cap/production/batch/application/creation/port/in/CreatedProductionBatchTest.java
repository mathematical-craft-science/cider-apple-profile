package org.craft.mcs.cap.production.batch.application.creation.port.in;

import org.craft.mcs.cap.core.production.batch.application.creation.port.in.CreatedProductionBatch;
import org.craft.mcs.cap.core.production.batch.application.model.ProductionBatchView;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreatedProductionBatchTest {

  @Test
  void acceptsTheCreatedBatchVesselReference() {
    final var batch = batchView();
    final var vessel = VesselTestFixtures.reference(batch.vesselId(), "Tank A");

    assertThat(new CreatedProductionBatch(batch, vessel))
        .returns(batch, CreatedProductionBatch::batch)
        .returns(vessel, CreatedProductionBatch::vessel);
  }

  @Test
  void rejectsAVesselReferenceForAnotherVessel() {
    final var batch = batchView();

    assertThatThrownBy(() -> new CreatedProductionBatch(
        batch,
        VesselTestFixtures.reference(VesselTestFixtures.PRODUCTION_VESSEL_ID, "Tank B")
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("production vessel reference must match created batch vessel id");
  }

  private static ProductionBatchView batchView() {
    final var batch = ProductionBatch.create(
        ProductionBatchId.newId(),
        WorkspaceId.newId(),
        BatchName.of("August cider"),
        VesselTestFixtures.vesselId("Tank A"),
        VolumeValue.ofLiters("100"),
        new ProductionProfileSnapshot(
            SpecificGravityValue.of("1.052"),
            TitratableAcidityValue.of("6.2"),
            null
        ),
        null
    );
    return new ProductionBatchView(
        batch.batchId(),
        batch.workspaceId(),
        batch.name(),
        batch.vesselId(),
        batch.lifecycle(),
        batch.alcoholicFermentationState(),
        batch.currentVolume(),
        batch.currentMaterialStateId(),
        batch.initialProfileSnapshot(),
        batch.note()
    );
  }
}
