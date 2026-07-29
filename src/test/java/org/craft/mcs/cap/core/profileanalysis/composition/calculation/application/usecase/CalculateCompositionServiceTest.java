package org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .compositionSourceCharacteristics;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange.closed;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateCompositionResult;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.AbsoluteCompositionVolume;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.BuiltInCompositionCalculatorSelector;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionSource;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionSourceReference;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.BuiltInProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.BuiltInProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;


class CalculateCompositionServiceTest {

  private final CalculateCompositionService service = new CalculateCompositionService(
      new BuiltInCompositionCalculatorSelector(),
      new BuiltInProfileAxisNormalizerSelector(),
      new BuiltInProfileTargetObjectiveFunctionFactory(),
      new PotentialAbvPredictor(
          FixedFinalGravityPredictionStrategy.dryCiderDefault(),
          new StandardGravityDropAbvPredictionStrategy()
      )
  );

  @Test
  void calculatesCompositionForProvidedSourceVolumes() {
    final var result = service.calculate(new CalculateCompositionCommand(
        List.of(
            source("a", "1.060", "5.0", "10"),
            source("b", "1.040", "9.0", "30")
        ),
        CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        null,
        scoringConfiguration(),
        CompositionCalculatorCode.MASS_BALANCE
    ));

    final var composition = result.composition();
    assertThat(composition)
        .satisfies(value -> assertThat(value.characteristics())
            .satisfies(characteristics -> assertThat(characteristics.specificGravity().asBigDecimal())
                .isEqualByComparingTo("1.045000000"))
            .satisfies(characteristics -> assertThat(characteristics.titratableAcidity().asBigDecimal())
                .isEqualByComparingTo("8.000000000"))
            .satisfies(characteristics -> assertThat(characteristics.tannin()).isNull()))
        .satisfies(value -> assertThat(value.totalVolume())
            .isNotNull()
            .satisfies(volume -> assertThat(volume.liters()).isEqualByComparingTo("40")))
        .satisfies(value -> assertThat(value.sources())
            .hasSize(2)
            .first()
            .satisfies(source -> assertThat(source.weight().value()).isEqualByComparingTo("10")))
        .satisfies(value -> assertThat(value.warnings()).isEmpty());
    assertThat(result)
        .satisfies(value -> assertThat(value.targetDistance()).isNull())
        .satisfies(value -> assertThat(value.normalizerCode()).isNull())
        .satisfies(value -> assertThat(value.potentialAbv())
            .satisfies(prediction -> assertThat(prediction.originalGravity().asBigDecimal())
                .isEqualByComparingTo("1.045000000"))
            .satisfies(prediction -> assertThat(prediction.potentialAbv().percent())
                .isEqualByComparingTo("5.906")));
  }

  @Test
  void calculatesCompositionForSingleSource() {
    final var result = service.calculate(new CalculateCompositionCommand(
        List.of(source("a", "1.060", "5.0", "10")),
        CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        null,
        scoringConfiguration(),
        CompositionCalculatorCode.MASS_BALANCE
    ));

    final var composition = result.composition();
    assertThat(composition)
        .satisfies(value -> assertThat(value.characteristics())
            .satisfies(characteristics -> assertThat(characteristics.specificGravity().asBigDecimal())
                .isEqualByComparingTo("1.060000000"))
            .satisfies(characteristics -> assertThat(characteristics.titratableAcidity().asBigDecimal())
                .isEqualByComparingTo("5.000000000"))
            .satisfies(characteristics -> assertThat(characteristics.tannin()).isNull()))
        .satisfies(value -> assertThat(value.totalVolume())
            .isNotNull()
            .satisfies(volume -> assertThat(volume.liters()).isEqualByComparingTo("10")))
        .satisfies(value -> assertThat(value.sources())
            .singleElement()
            .extracting(source -> source.reference().id())
            .isEqualTo("a"))
        .satisfies(value -> assertThat(value.warnings()).isEmpty());
    assertThat(result)
        .satisfies(value -> assertThat(value.targetDistance()).isNull());
  }

  @Test
  void calculatesTargetDistanceForCompositionWhenTargetIsProvided() {
    final var result = service.calculate(new CalculateCompositionCommand(
        List.of(
            source("a", "1.060", "5.0", "10"),
            source("b", "1.040", "9.0", "30")
        ),
        CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        targetObjective(),
        scoringConfiguration(),
        CompositionCalculatorCode.MASS_BALANCE
    ));

    assertThat(result)
        .satisfies(value -> assertThat(value.normalizerCode()).isNotNull())
        .satisfies(value -> assertThat(value.targetDistance())
            .isNotNull()
            .satisfies(distance -> assertThat(distance)
                .returns(true, ProfileTargetDistance::scored)
                .returns(false, ProfileTargetDistance::insideTarget)
                .satisfies(candidate -> assertThat(candidate.value()).isPositive())));
  }

  @Test
  void rejectsPartialTargetAssessmentMetadataInResult() {
    final var withoutTarget = service.calculate(new CalculateCompositionCommand(
        List.of(source("a", "1.060", "5.0", "10")),
        CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        null,
        scoringConfiguration(),
        CompositionCalculatorCode.MASS_BALANCE
    ));
    final var withTarget = service.calculate(new CalculateCompositionCommand(
        List.of(source("a", "1.060", "5.0", "10")),
        CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        targetObjective(),
        scoringConfiguration(),
        CompositionCalculatorCode.MASS_BALANCE
    ));
    final var withoutTargetComposition = withoutTarget.composition();
    final var withoutTargetPotentialAbv = withoutTarget.potentialAbv();
    final var withTargetComposition = withTarget.composition();
    final var withTargetPotentialAbv = withTarget.potentialAbv();
    final var targetDistance = withTarget.targetDistance();

    assertThatThrownBy(() -> new CalculateCompositionResult(
        withoutTargetComposition,
        withoutTargetPotentialAbv,
        ProfileAxisNormalizerCode.EFFECTIVE_RANGE,
        null
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("normalizer and target distance");
    assertThatThrownBy(() -> new CalculateCompositionResult(
        withTargetComposition,
        withTargetPotentialAbv,
        null,
        targetDistance
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("normalizer and target distance");
  }

  private static @NonNull CompositionSource source(
      @NonNull String id,
      @NonNull String specificGravity,
      @NonNull String titratableAcidity,
      @NonNull String liters
  ) {
    return new CompositionSource(
        new CompositionSourceReference("FEEDSTOCK_BATCH", id),
        compositionSourceCharacteristics(specificGravity, titratableAcidity, null, null),
        new AbsoluteCompositionVolume(VolumeValue.ofLiters(liters)),
        null
    );
  }

  private static @NonNull ProfileTargetScoringConfiguration scoringConfiguration() {
    return new ProfileTargetScoringConfiguration(
        ProfileAxisNormalizerCode.EFFECTIVE_RANGE,
        ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA,
        ProfileTargetObjectiveWeights.balanced()
    );
  }

  private static @NonNull ProfileTargetObjective targetObjective() {
    return ProfileTargetObjective.of(new CustomRectangleSugarAcidityTarget(
        new RectangleSugarAcidityTargetGeometry(
            closed(
                SpecificGravityValue.of("1.060"),
                SpecificGravityValue.of("1.070")
            ),
            closed(
                TitratableAcidityValue.of("5.0"),
                TitratableAcidityValue.of("6.0")
            )
        )
    ));
  }

}
