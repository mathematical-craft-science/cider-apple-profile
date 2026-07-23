package org.craft.mcs.cap.production.domain.model;

import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.value.AlcoholicFermentationState;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchLifecycle;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ProductionBatchTest {

  @Test
  void createsActiveBatchBeforeAlcoholicFermentation() {
    final var batch = productionBatch();

    assertThat(batch)
        .returns(Vessel.named("Tank F-03"), ProductionBatch::vessel)
        .returns(ProductionBatchLifecycle.ACTIVE, ProductionBatch::lifecycle)
        .returns(
            AlcoholicFermentationState.NOT_STARTED,
            ProductionBatch::alcoholicFermentationState
        )
        .returns(VolumeValue.ofLiters("100"), ProductionBatch::currentVolume)
        .returns(profile(), ProductionBatch::initialProfileSnapshot);
  }

  @Test
  void rejectsZeroInitialVolume() {
    final var zeroVolume = VolumeValue.ofLiters("0");

    assertThatThrownBy(() -> productionBatch(zeroVolume))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("production batch volume must be greater than zero");
  }

  @Test
  void followsPrimaryAlcoholicFermentationTransitions() {
    final var batch = productionBatch();

    batch.startAlcoholicFermentation()
        .arrestAlcoholicFermentation()
        .resumeAlcoholicFermentation()
        .completeAlcoholicFermentation();

    assertThat(batch.alcoholicFermentationState())
        .isEqualTo(AlcoholicFermentationState.COMPLETED);
  }

  @Test
  void rejectsStartingCompletedAlcoholicFermentationAgain() {
    final var batch = productionBatch()
        .startAlcoholicFermentation()
        .completeAlcoholicFermentation();

    assertThatThrownBy(batch::startAlcoholicFermentation)
        .isInstanceOf(DomainConflictException.class)
        .hasMessageContaining("state is COMPLETED");
  }

  @Test
  void depletedBatchHasNoMaterialAndCannotAcceptOperations() {
    final var batch = productionBatch().deplete();
    final var targetVessel = Vessel.named("Tank F-04");

    assertThat(batch)
        .returns(ProductionBatchLifecycle.DEPLETED, ProductionBatch::lifecycle)
        .returns(VolumeValue.ofLiters("0"), ProductionBatch::currentVolume);
    assertThatThrownBy(() -> batch.transferTo(targetVessel))
        .isInstanceOf(DomainConflictException.class)
        .hasMessageContaining("lifecycle is DEPLETED");
  }

  @Test
  void discardedBatchKeepsFermentationStateIndependentFromMaterialLifecycle() {
    final var batch = productionBatch()
        .startAlcoholicFermentation()
        .discard();

    assertThat(batch)
        .returns(ProductionBatchLifecycle.DISCARDED, ProductionBatch::lifecycle)
        .returns(VolumeValue.ofLiters("0"), ProductionBatch::currentVolume)
        .returns(
            AlcoholicFermentationState.ACTIVE,
            ProductionBatch::alcoholicFermentationState
        );
  }

  @Test
  void reducesOnlyPositiveCurrentVolume() {
    final var batch = productionBatch();
    final var excessiveVolume = VolumeValue.ofLiters("101");
    final var zeroVolume = VolumeValue.ofLiters("0");

    assertThatThrownBy(() -> batch.reduceCurrentVolumeTo(excessiveVolume))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("remaining production volume cannot exceed current volume");
    assertThatThrownBy(() -> batch.reduceCurrentVolumeTo(zeroVolume))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("production batch volume must be greater than zero");

    assertThat(batch.reduceCurrentVolumeTo(VolumeValue.ofLiters("92")).currentVolume())
        .isEqualTo(VolumeValue.ofLiters("92"));
  }

  @Test
  void rejectsRestoredLifecycleAndVolumeMismatch() {
    final var batchId = ProductionBatchId.newId();
    final var workspaceId = WorkspaceId.newId();
    final var name = BatchName.of("Invalid batch");
    final var vessel = Vessel.named("Tank F-03");
    final var zeroVolume = VolumeValue.ofLiters("0");
    final var initialProfileSnapshot = profile();

    assertThatThrownBy(() -> ProductionBatch.restore(
        batchId,
        workspaceId,
        name,
        vessel,
        ProductionBatchLifecycle.ACTIVE,
        AlcoholicFermentationState.NOT_STARTED,
        zeroVolume,
        initialProfileSnapshot,
        null
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("active production batch volume must be greater than zero");
  }

  @Test
  void rejectsRestoredInactiveBatchWithMaterial() {
    final var batchId = ProductionBatchId.newId();
    final var workspaceId = WorkspaceId.newId();
    final var name = BatchName.of("Invalid discarded batch");
    final var vessel = Vessel.named("Tank F-03");
    final var remainingVolume = VolumeValue.ofLiters("5");
    final var initialProfileSnapshot = profile();

    assertThatThrownBy(() -> ProductionBatch.restore(
        batchId,
        workspaceId,
        name,
        vessel,
        ProductionBatchLifecycle.DISCARDED,
        AlcoholicFermentationState.NOT_STARTED,
        remainingVolume,
        initialProfileSnapshot,
        null
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("inactive production batch volume must be zero");
  }

  private static ProductionBatch productionBatch() {
    return productionBatch(VolumeValue.ofLiters("100"));
  }

  private static ProductionBatch productionBatch(VolumeValue volume) {
    return ProductionBatch.create(
        ProductionBatchId.newId(),
        WorkspaceId.newId(),
        BatchName.of("Classic cider"),
        Vessel.named("Tank F-03"),
        volume,
        profile(),
        null
    );
  }

  private static ProductionProfileSnapshot profile() {
    return new ProductionProfileSnapshot(
        SpecificGravityValue.of("1.052"),
        TitratableAcidityValue.of("6.2"),
        null,
        null
    );
  }
}
