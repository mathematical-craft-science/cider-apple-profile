package org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityZoneCode;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.port.in.TargetFitCommand;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class TargetFitPublishedModelTest {

  @Test
  void preservesOpaqueCandidateReference() {
    assertThat(new TargetFitCandidateReference("feedstock-batch", "batch-1"))
        .returns("feedstock-batch", TargetFitCandidateReference::type)
        .returns("batch-1", TargetFitCandidateReference::id);
  }

  @Test
  void rejectsBlankCandidateReferenceParts() {
    assertThatThrownBy(() -> new TargetFitCandidateReference(" ", "batch-1"))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("target candidate reference cannot be blank");
    assertThatThrownBy(() -> new TargetFitCandidateReference("feedstock-batch", " "))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("target candidate reference cannot be blank");
  }

  @Test
  void acceptsPositiveScoringWeightsAndOptionalTannin() {
    final var weights = new TargetFitScoringConfiguration.ObjectiveWeights(
        BigDecimal.ONE,
        BigDecimal.TWO,
        null
    );

    assertThat(weights)
        .returns(BigDecimal.ONE, TargetFitScoringConfiguration.ObjectiveWeights::specificGravity)
        .returns(BigDecimal.TWO, TargetFitScoringConfiguration.ObjectiveWeights::titratableAcidity)
        .returns(null, TargetFitScoringConfiguration.ObjectiveWeights::tannin);
  }

  @Test
  void preservesExplicitScoringConfiguration() {
    final var weights = new TargetFitScoringConfiguration.ObjectiveWeights(
        BigDecimal.ONE,
        BigDecimal.TWO,
        BigDecimal.valueOf(3)
    );

    assertThat(new TargetFitScoringConfiguration(
        TargetFitScoringConfiguration.Normalizer.SENSITIVITY_UNIT,
        TargetFitScoringConfiguration.ObjectiveFunction.MANHATTAN_DISTANCE_TO_TARGET_AREA,
        weights
    ))
        .returns(
            TargetFitScoringConfiguration.Normalizer.SENSITIVITY_UNIT,
            TargetFitScoringConfiguration::normalizer
        )
        .returns(
            TargetFitScoringConfiguration.ObjectiveFunction.MANHATTAN_DISTANCE_TO_TARGET_AREA,
            TargetFitScoringConfiguration::objectiveFunction
        )
        .returns(weights, TargetFitScoringConfiguration::weights);
  }

  @Test
  void rejectsMissingScoringConfigurationParts() {
    final var weights = new TargetFitScoringConfiguration.ObjectiveWeights(
        BigDecimal.ONE,
        BigDecimal.ONE,
        null
    );

    assertThatNullPointerException()
        .isThrownBy(() -> new TargetFitScoringConfiguration(
            null,
            TargetFitScoringConfiguration.ObjectiveFunction.DISTANCE_TO_TARGET_AREA,
            weights
        ))
        .withMessage("target normalizer cannot be null");
    assertThatNullPointerException()
        .isThrownBy(() -> new TargetFitScoringConfiguration(
            TargetFitScoringConfiguration.Normalizer.MIN_MAX,
            null,
            weights
        ))
        .withMessage("target objective function cannot be null");
    assertThatNullPointerException()
        .isThrownBy(() -> new TargetFitScoringConfiguration(
            TargetFitScoringConfiguration.Normalizer.MIN_MAX,
            TargetFitScoringConfiguration.ObjectiveFunction.DISTANCE_TO_TARGET_AREA,
            null
        ))
        .withMessage("target objective weights cannot be null");
  }

  @Test
  void rejectsNonPositiveScoringWeightOnEveryAxis() {
    assertThatThrownBy(() -> new TargetFitScoringConfiguration.ObjectiveWeights(
        BigDecimal.ZERO,
        BigDecimal.ONE,
        null
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("target weights must be positive");
    assertThatThrownBy(() -> new TargetFitScoringConfiguration.ObjectiveWeights(
        BigDecimal.ONE,
        BigDecimal.valueOf(-1),
        null
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("target weights must be positive");
    assertThatThrownBy(() -> new TargetFitScoringConfiguration.ObjectiveWeights(
        BigDecimal.ONE,
        BigDecimal.ONE,
        BigDecimal.ZERO
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("target weights must be positive");
  }

  @Test
  void rejectsDuplicateCandidateReferences() {
    final var reference = new TargetFitCandidateReference("feedstock-batch", "batch-1");
    final var candidate = new TargetFitCommand.Candidate(
        reference,
        new TargetFitCommand.Characteristics(
            PointCharacteristicValue.of(SpecificGravityValue.of("1.050")),
            PointCharacteristicValue.of(TitratableAcidityValue.of("6.0")),
            null
        )
    );

    assertThatThrownBy(() -> new TargetFitCommand(
        new TargetFitCommand.TargetObjective(
            new TargetFitCommand.PredefinedTarget(JolicoeurSugarAcidityZoneCode.PERFECT_APPLES),
            null
        ),
        new TargetFitScoringConfiguration(
            TargetFitScoringConfiguration.Normalizer.EFFECTIVE_RANGE,
            TargetFitScoringConfiguration.ObjectiveFunction.DISTANCE_TO_TARGET_AREA,
            new TargetFitScoringConfiguration.ObjectiveWeights(BigDecimal.ONE, BigDecimal.ONE, null)
        ),
        TargetFitCommand.IntervalPolicy.MIDPOINT,
        List.of(candidate, candidate)
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("target candidate references must be unique");
  }
}
