package org.craft.mcs.cap.production.planning.application.contract;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateCalculation;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateCalculationRequest;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidateNormalizer;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidatePotentialAbvView;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidate;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.craft.mcs.cap.shared.kernel.measurement.value.AbvValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.productionCandidatePrediction;
import static org.mockito.Mockito.mock;


class ProductionCandidateAnalysisMetadataTest {

  private static final ProductionProfileSnapshot PROFILE = new ProductionProfileSnapshot(
      SpecificGravityValue.of("1.052"),
      TitratableAcidityValue.of("6.2"),
      null,
      null
  );

  @Test
  void calculationRequiresTargetNormalizerAndDistanceTogether() {
    assertThatThrownBy(() -> new ProductionBatchCandidateCalculation(
        PROFILE,
        productionCandidatePrediction(PROFILE.specificGravity()),
        ProductionCandidateNormalizer.SENSITIVITY_UNIT,
        null,
        List.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("production candidate target normalizer and distance must be present together");
  }

  @Test
  void persistedSnapshotRequiresTargetNormalizerAndDistanceTogether() {
    assertThatThrownBy(() -> new ProductionBatchCandidateSnapshot(
        mock(ProductionBatchCandidate.class),
        productionCandidatePrediction(PROFILE.specificGravity()),
        ProductionCandidateNormalizer.SENSITIVITY_UNIT,
        null,
        List.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("production candidate target normalizer and distance must be present together");
  }

  @Test
  void calculationRequiresAbvPredictionForCalculatedProfile() {
    assertThatThrownBy(() -> new ProductionBatchCandidateCalculation(
        PROFILE,
        productionCandidatePrediction(SpecificGravityValue.of("1.060")),
        null,
        null,
        List.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("production candidate ABV original gravity must match the calculated profile");
  }

  @Test
  void potentialAbvRejectsFinalGravityAboveOriginalGravity() {
    assertThatThrownBy(() -> new ProductionCandidatePotentialAbvView(
        SpecificGravityValue.of("1.010"),
        SpecificGravityValue.of("1.020"),
        ProductionCandidatePotentialAbvView.FinalGravitySource.FIXED_DRY_CIDER_DEFAULT,
        AbvValue.of(BigDecimal.ZERO),
        ProductionCandidatePotentialAbvView.Method.STANDARD_GRAVITY_DROP,
        ProductionCandidatePotentialAbvView.Confidence.MEDIUM
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("production candidate predicted final gravity must not exceed original gravity");
  }

  @Test
  void targetDefinitionRequiresClosedTanninRange() {
    assertThatThrownBy(() -> new ProductionBatchCandidateCalculationRequest.TargetDefinition(
        new ProductionBatchCandidateCalculationRequest.PredefinedTarget(
            ProductionBatchCandidateCalculationRequest.Zone.PERFECT_APPLES
        ),
        MeasuredValueRange.closedOpen(TanninValue.of("800"), TanninValue.of("1800")),
        new ProductionBatchCandidateCalculationRequest.Scoring(
            ProductionCandidateNormalizer.SENSITIVITY_UNIT,
            ProductionBatchCandidateCalculationRequest.ObjectiveFunction.DISTANCE_TO_TARGET_AREA,
            new ProductionBatchCandidateCalculationRequest.Weights(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE)
        )
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("production candidate tannin target must include its maximum");
  }
}
