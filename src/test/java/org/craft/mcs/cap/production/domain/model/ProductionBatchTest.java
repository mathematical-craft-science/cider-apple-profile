package org.craft.mcs.cap.production.domain.model;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.value.AlcoholicFermentationState;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


class ProductionBatchTest {

  @Test
  void createsActiveBatchBeforeAlcoholicFermentation() {
    final var batchId = ProductionBatchId.of(newUuid());
    final var workspaceId = WorkspaceId.newId();
    final var name = BatchName.of("Classic cider");
    final var batch = ProductionBatch.create(
        batchId,
        workspaceId,
        name,
        VesselTestFixtures.vesselId("Tank F-03"),
        VolumeValue.ofLiters("100"),
        profile(),
        null
    );

    assertThat(batch)
        .returns(batchId, ProductionBatch::batchId)
        .returns(workspaceId, ProductionBatch::workspaceId)
        .returns(name, ProductionBatch::name)
        .returns(VesselTestFixtures.PRODUCTION_VESSEL_ID, ProductionBatch::vesselId)
        .returns(BatchLifecycle.ACTIVE, ProductionBatch::lifecycle)
        .returns(
            AlcoholicFermentationState.NOT_STARTED,
            ProductionBatch::alcoholicFermentationState
        )
        .returns(VolumeValue.ofLiters("100"), ProductionBatch::currentVolume)
        .returns(profile(), ProductionBatch::initialProfileSnapshot)
        .returns(null, ProductionBatch::note);
  }

  @Test
  void normalizesOptionalNote() {
    final var notedBatch = ProductionBatch.create(
        ProductionBatchId.newId(),
        WorkspaceId.newId(),
        BatchName.of("Noted cider"),
        VesselTestFixtures.vesselId("Tank F-03"),
        VolumeValue.ofLiters("100"),
        profile(),
        "  Retain for tasting  "
    );
    final var blankNoteBatch = ProductionBatch.create(
        ProductionBatchId.newId(),
        WorkspaceId.newId(),
        BatchName.of("Unnoted cider"),
        VesselTestFixtures.vesselId("Tank F-04"),
        VolumeValue.ofLiters("100"),
        profile(),
        "   "
    );

    assertThat(notedBatch.note()).isEqualTo("Retain for tasting");
    assertThat(blankNoteBatch.note()).isNull();
  }

  @Test
  void rejectsZeroInitialVolume() {
    final var zeroVolume = VolumeValue.ofLiters("0");

    assertThatThrownBy(() -> productionBatch(zeroVolume))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("active batch material volume must be greater than zero");
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

  @ParameterizedTest(name = "{0} is allowed from {1}")
  @MethodSource("allowedFermentationTransitions")
  void allowsOnlyDeclaredAlcoholicFermentationTransitions(
      FermentationAction action,
      AlcoholicFermentationState initialState,
      AlcoholicFermentationState expectedState
  ) {
    final var batch = restoredBatch(
        BatchLifecycle.ACTIVE,
        initialState,
        VolumeValue.ofLiters("100")
    );

    final var stateBeforeTransition = batch.currentMaterialStateId();
    final var result = action.apply(batch);

    assertThat(result)
        .isSameAs(batch)
        .returns(expectedState, ProductionBatch::alcoholicFermentationState)
        .satisfies(updated -> assertThat(updated.currentMaterialStateId())
            .isNotEqualTo(stateBeforeTransition));
  }

  @ParameterizedTest(name = "{0} is forbidden from {1}")
  @MethodSource("forbiddenFermentationTransitions")
  void rejectsEveryUndeclaredAlcoholicFermentationTransition(
      FermentationAction action,
      AlcoholicFermentationState initialState
  ) {
    final var batch = restoredBatch(
        BatchLifecycle.ACTIVE,
        initialState,
        VolumeValue.ofLiters("100")
    );

    assertThatThrownBy(() -> action.apply(batch))
        .isInstanceOf(DomainConflictException.class)
        .hasMessageContaining("state is " + initialState);
  }

  @Test
  void depletedBatchHasNoMaterialAndCannotAcceptOperations() {
    final var batch = productionBatch();
    final var stateBeforeDepletion = batch.currentMaterialStateId();
    final var result = batch.deplete();
    final var targetVessel = VesselTestFixtures.vesselId("Tank F-04");

    assertThat(result)
        .isSameAs(batch)
        .returns(BatchLifecycle.DEPLETED, ProductionBatch::lifecycle)
        .returns(VolumeValue.ofLiters("0"), ProductionBatch::currentVolume)
        .satisfies(updated -> assertThat(updated.currentMaterialStateId())
            .isNotEqualTo(stateBeforeDepletion));
    assertThatThrownBy(() -> batch.transferTo(targetVessel))
        .isInstanceOf(DomainConflictException.class)
        .hasMessageContaining("lifecycle is DEPLETED");
  }

  @Test
  void discardedBatchKeepsFermentationStateIndependentFromMaterialLifecycle() {
    final var batch = productionBatch().startAlcoholicFermentation();
    final var stateBeforeDiscard = batch.currentMaterialStateId();
    final var result = batch.discard();

    assertThat(result)
        .isSameAs(batch)
        .returns(BatchLifecycle.DISCARDED, ProductionBatch::lifecycle)
        .returns(VolumeValue.ofLiters("0"), ProductionBatch::currentVolume)
        .returns(
            AlcoholicFermentationState.ACTIVE,
            ProductionBatch::alcoholicFermentationState
        )
        .satisfies(updated -> assertThat(updated.currentMaterialStateId())
            .isNotEqualTo(stateBeforeDiscard));
  }

  @Test
  void reducesOnlyPositiveCurrentVolume() {
    final var batch = productionBatch();
    final var stateBeforeReduction = batch.currentMaterialStateId();
    final var excessiveVolume = VolumeValue.ofLiters("101");
    final var unchangedVolume = VolumeValue.ofLiters("100");
    final var zeroVolume = VolumeValue.ofLiters("0");

    assertThatThrownBy(() -> batch.reduceCurrentVolumeTo(excessiveVolume))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("remaining material volume cannot exceed current volume");
    assertThatThrownBy(() -> batch.reduceCurrentVolumeTo(zeroVolume))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("remaining material volume must be greater than zero");
    assertThatThrownBy(() -> batch.reduceCurrentVolumeTo(unchangedVolume))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("remaining material volume must be less than current volume");

    assertThat(batch.reduceCurrentVolumeTo(VolumeValue.ofLiters("92")))
        .returns(VolumeValue.ofLiters("92"), ProductionBatch::currentVolume)
        .satisfies(updated -> assertThat(updated.currentMaterialStateId())
            .isNotEqualTo(stateBeforeReduction));
  }

  @Test
  void rejectsTransferToCurrentVesselWithoutCreatingMaterialState() {
    final var batch = productionBatch();
    final var currentState = batch.currentMaterialStateId();
    final var currentVessel = batch.vesselId();

    assertThatThrownBy(() -> batch.transferTo(currentVessel))
        .isInstanceOf(DomainConflictException.class)
        .hasMessage("target production vessel must differ from current vessel");
    assertThat(batch)
        .returns(currentVessel, ProductionBatch::vesselId)
        .returns(currentState, ProductionBatch::currentMaterialStateId);
  }

  @Test
  void advancesMaterialStateForVesselAndFermentationChanges() {
    final var batch = productionBatch();
    final var initialState = batch.currentMaterialStateId();

    final var transferResult = batch.transferTo(VesselTestFixtures.vesselId("Tank F-04"));
    final var transferredState = batch.currentMaterialStateId();
    batch.startAlcoholicFermentation();

    assertThat(transferResult)
        .isSameAs(batch)
        .satisfies(updated -> assertThat(transferredState).isNotEqualTo(initialState))
        .satisfies(updated -> assertThat(updated.currentMaterialStateId())
            .isNotEqualTo(transferredState));
  }

  @ParameterizedTest(name = "{0} is forbidden for an inactive batch")
  @MethodSource("inactiveBatchOperations")
  void rejectsEveryMaterialMutationForInactiveBatch(
      String operation,
      ProductionOperation action,
      AlcoholicFermentationState fermentationState
  ) {
    final var batch = restoredBatch(
        BatchLifecycle.DEPLETED,
        fermentationState,
        VolumeValue.ofLiters("0")
    );

    assertThatThrownBy(() -> action.apply(batch))
        .isInstanceOf(DomainConflictException.class)
        .hasMessageContaining("cannot " + operation)
        .hasMessageContaining("lifecycle is DEPLETED");
  }

  @Test
  void rejectsRestoredLifecycleAndVolumeMismatch() {
    final var zeroVolume = VolumeValue.ofLiters("0");

    assertThatThrownBy(() -> restoredBatch(
        BatchLifecycle.ACTIVE,
        AlcoholicFermentationState.NOT_STARTED,
        zeroVolume
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("active batch material volume must be greater than zero");
  }

  @Test
  void rejectsRestoredInactiveBatchWithMaterial() {
    final var remainingVolume = VolumeValue.ofLiters("5");

    assertThatThrownBy(() -> restoredBatch(
        BatchLifecycle.DISCARDED,
        AlcoholicFermentationState.NOT_STARTED,
        remainingVolume
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("inactive batch material volume must be zero");
  }

  private static ProductionBatch productionBatch() {
    return productionBatch(VolumeValue.ofLiters("100"));
  }

  private static ProductionBatch productionBatch(VolumeValue volume) {
    return ProductionBatch.create(
        ProductionBatchId.newId(),
        WorkspaceId.newId(),
        BatchName.of("Classic cider"),
        VesselTestFixtures.vesselId("Tank F-03"),
        volume,
        profile(),
        null
    );
  }

  private static ProductionBatch restoredBatch(
      BatchLifecycle lifecycle,
      AlcoholicFermentationState alcoholicFermentationState,
      VolumeValue volume
  ) {
    return ProductionBatch.restore(
        ProductionBatchId.newId(),
        WorkspaceId.newId(),
        BatchName.of("Classic cider"),
        VesselTestFixtures.vesselId("Tank F-03"),
        lifecycle,
        alcoholicFermentationState,
        volume,
        MaterialStateId.newId(),
        profile(),
        null
    );
  }

  private static Stream<Arguments> allowedFermentationTransitions() {
    return Arrays.stream(FermentationAction.values())
        .flatMap(action -> action.allowedInitialStates().stream()
            .map(initialState -> Arguments.of(action, initialState, action.resultingState())));
  }

  private static Stream<Arguments> forbiddenFermentationTransitions() {
    return Arrays.stream(FermentationAction.values())
        .flatMap(action -> Arrays.stream(AlcoholicFermentationState.values())
            .filter(initialState -> !action.allowedInitialStates().contains(initialState))
            .map(initialState -> Arguments.of(action, initialState)));
  }

  private static Stream<Arguments> inactiveBatchOperations() {
    return Stream.of(
        Arguments.of(
            "transfer",
            (ProductionOperation) batch -> batch.transferTo(VesselTestFixtures.vesselId("Tank F-04")),
            AlcoholicFermentationState.NOT_STARTED
        ),
        Arguments.of(
            "reduce volume",
            (ProductionOperation) batch -> batch.reduceCurrentVolumeTo(VolumeValue.ofLiters("1")),
            AlcoholicFermentationState.NOT_STARTED
        ),
        Arguments.of(
            "deplete",
            (ProductionOperation) ProductionBatch::deplete,
            AlcoholicFermentationState.NOT_STARTED
        ),
        Arguments.of(
            "discard",
            (ProductionOperation) ProductionBatch::discard,
            AlcoholicFermentationState.NOT_STARTED
        ),
        Arguments.of(
            "start alcoholic fermentation",
            (ProductionOperation) ProductionBatch::startAlcoholicFermentation,
            AlcoholicFermentationState.NOT_STARTED
        ),
        Arguments.of(
            "arrest alcoholic fermentation",
            (ProductionOperation) ProductionBatch::arrestAlcoholicFermentation,
            AlcoholicFermentationState.ACTIVE
        ),
        Arguments.of(
            "resume alcoholic fermentation",
            (ProductionOperation) ProductionBatch::resumeAlcoholicFermentation,
            AlcoholicFermentationState.ARRESTED
        ),
        Arguments.of(
            "complete alcoholic fermentation",
            (ProductionOperation) ProductionBatch::completeAlcoholicFermentation,
            AlcoholicFermentationState.ACTIVE
        )
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

  private enum FermentationAction {
    START(
        Set.of(AlcoholicFermentationState.NOT_STARTED),
        AlcoholicFermentationState.ACTIVE
    ) {
      @Override
      ProductionBatch apply(ProductionBatch batch) {
        return batch.startAlcoholicFermentation();
      }
    },
    ARREST(
        Set.of(AlcoholicFermentationState.ACTIVE),
        AlcoholicFermentationState.ARRESTED
    ) {
      @Override
      ProductionBatch apply(ProductionBatch batch) {
        return batch.arrestAlcoholicFermentation();
      }
    },
    RESUME(
        Set.of(AlcoholicFermentationState.ARRESTED),
        AlcoholicFermentationState.ACTIVE
    ) {
      @Override
      ProductionBatch apply(ProductionBatch batch) {
        return batch.resumeAlcoholicFermentation();
      }
    },
    COMPLETE(
        Set.of(
            AlcoholicFermentationState.ACTIVE,
            AlcoholicFermentationState.ARRESTED
        ),
        AlcoholicFermentationState.COMPLETED
    ) {
      @Override
      ProductionBatch apply(ProductionBatch batch) {
        return batch.completeAlcoholicFermentation();
      }
    };

    private final Set<AlcoholicFermentationState> allowedInitialStates;
    private final AlcoholicFermentationState resultingState;

    FermentationAction(
        Set<AlcoholicFermentationState> allowedInitialStates,
        AlcoholicFermentationState resultingState
    ) {
      this.allowedInitialStates = Set.copyOf(allowedInitialStates);
      this.resultingState = resultingState;
    }

    abstract ProductionBatch apply(ProductionBatch batch);

    Set<AlcoholicFermentationState> allowedInitialStates() {
      return allowedInitialStates;
    }

    AlcoholicFermentationState resultingState() {
      return resultingState;
    }
  }

  @FunctionalInterface
  private interface ProductionOperation {

    ProductionBatch apply(ProductionBatch batch);
  }
}
