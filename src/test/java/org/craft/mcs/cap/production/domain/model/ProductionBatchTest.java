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
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ProductionBatchTest {

  @Test
  void createsActiveBatchBeforeAlcoholicFermentation() {
    final var batch = productionBatch();

    assertThat(batch)
        .returns(Vessel.named("Tank F-03"), ProductionBatch::vessel)
        .returns(BatchLifecycle.ACTIVE, ProductionBatch::lifecycle)
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

    action.apply(batch);

    assertThat(batch.alcoholicFermentationState()).isEqualTo(expectedState);
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
    final var batch = productionBatch().deplete();
    final var targetVessel = Vessel.named("Tank F-04");

    assertThat(batch)
        .returns(BatchLifecycle.DEPLETED, ProductionBatch::lifecycle)
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
        .returns(BatchLifecycle.DISCARDED, ProductionBatch::lifecycle)
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
    final var zeroVolume = VolumeValue.ofLiters("0");

    assertThatThrownBy(() -> restoredBatch(
        BatchLifecycle.ACTIVE,
        AlcoholicFermentationState.NOT_STARTED,
        zeroVolume
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("active production batch volume must be greater than zero");
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

  private static ProductionBatch restoredBatch(
      BatchLifecycle lifecycle,
      AlcoholicFermentationState alcoholicFermentationState,
      VolumeValue volume
  ) {
    return ProductionBatch.restore(
        ProductionBatchId.newId(),
        WorkspaceId.newId(),
        BatchName.of("Classic cider"),
        Vessel.named("Tank F-03"),
        lifecycle,
        alcoholicFermentationState,
        volume,
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
      void apply(ProductionBatch batch) {
        batch.startAlcoholicFermentation();
      }
    },
    ARREST(
        Set.of(AlcoholicFermentationState.ACTIVE),
        AlcoholicFermentationState.ARRESTED
    ) {
      @Override
      void apply(ProductionBatch batch) {
        batch.arrestAlcoholicFermentation();
      }
    },
    RESUME(
        Set.of(AlcoholicFermentationState.ARRESTED),
        AlcoholicFermentationState.ACTIVE
    ) {
      @Override
      void apply(ProductionBatch batch) {
        batch.resumeAlcoholicFermentation();
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
      void apply(ProductionBatch batch) {
        batch.completeAlcoholicFermentation();
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

    abstract void apply(ProductionBatch batch);

    Set<AlcoholicFermentationState> allowedInitialStates() {
      return allowedInitialStates;
    }

    AlcoholicFermentationState resultingState() {
      return resultingState;
    }
  }
}
