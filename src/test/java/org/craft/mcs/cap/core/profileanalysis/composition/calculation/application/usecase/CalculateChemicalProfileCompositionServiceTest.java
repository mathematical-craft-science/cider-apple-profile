package org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .compositionSourceCharacteristics;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange.closed;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.AbsoluteCompositionVolume;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.BuiltInChemicalProfileCompositionCalculatorSelector;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionSource;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionSourceReference;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.BuiltInProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.BuiltInProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;


class CalculateChemicalProfileCompositionServiceTest {

  private final CalculateChemicalProfileCompositionService service = new CalculateChemicalProfileCompositionService(
      new BuiltInChemicalProfileCompositionCalculatorSelector(),
      new BuiltInProfileAxisNormalizerSelector(),
      new BuiltInProfileTargetObjectiveFunctionFactory(),
      new PotentialAbvPredictor(
          FixedFinalGravityPredictionStrategy.dryCiderDefault(),
          new StandardGravityDropAbvPredictionStrategy()
      )
  );

  @Test
  void calculatesChemicalProfileCompositionForProvidedSourceVolumes() {
    final var result = service.calculate(new CalculateChemicalProfileCompositionCommand(
        List.of(
            source("a", "1.060", "5.0", "10"),
            source("b", "1.040", "9.0", "30")
        ),
        ChemicalProfileCompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        null,
        scoringConfiguration(),
        ChemicalProfileCompositionCalculatorCode.MASS_BALANCE
    ));

    final var composition = result.composition();
    assertThat(composition.characteristics())
        .satisfies(value -> assertThat(value.specificGravity().asBigDecimal())
            .isEqualByComparingTo("1.045000000"))
        .satisfies(value -> assertThat(value.titratableAcidity().asBigDecimal())
            .isEqualByComparingTo("8.000000000"))
        .satisfies(value -> assertThat(value.tannin()).isNull());
    assertThat(composition.totalVolume()).isNotNull();
    assertThat(composition.totalVolume().liters()).isEqualByComparingTo("40");
    assertThat(composition.sources()).hasSize(2);
    assertThat(composition.sources().getFirst().weight().value()).isEqualByComparingTo("10");
    assertThat(composition.warnings()).isEmpty();
    assertThat(result.targetDistance()).isNull();
    assertThat(result.normalizerCode()).isNull();
    assertThat(result.potentialAbv())
        .satisfies(prediction -> assertThat(prediction.originalGravity().asBigDecimal())
            .isEqualByComparingTo("1.045000000"))
        .satisfies(prediction -> assertThat(prediction.potentialAbv().percent())
            .isEqualByComparingTo("5.906"));
  }

  @Test
  void calculatesChemicalProfileCompositionForSingleSource() {
    final var result = service.calculate(new CalculateChemicalProfileCompositionCommand(
        List.of(source("a", "1.060", "5.0", "10")),
        ChemicalProfileCompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        null,
        scoringConfiguration(),
        ChemicalProfileCompositionCalculatorCode.MASS_BALANCE
    ));

    final var composition = result.composition();
    assertThat(composition.characteristics())
        .satisfies(value -> assertThat(value.specificGravity().asBigDecimal())
            .isEqualByComparingTo("1.060000000"))
        .satisfies(value -> assertThat(value.titratableAcidity().asBigDecimal())
            .isEqualByComparingTo("5.000000000"))
        .satisfies(value -> assertThat(value.tannin()).isNull());
    assertThat(composition.totalVolume()).isNotNull();
    assertThat(composition.totalVolume().liters()).isEqualByComparingTo("10");
    assertThat(composition.sources()).singleElement()
        .extracting(source -> source.reference().id())
        .isEqualTo("a");
    assertThat(composition.warnings()).isEmpty();
    assertThat(result.targetDistance()).isNull();
  }

  @Test
  void calculatesTargetDistanceForChemicalProfileCompositionWhenTargetIsProvided() {
    final var result = service.calculate(new CalculateChemicalProfileCompositionCommand(
        List.of(
            source("a", "1.060", "5.0", "10"),
            source("b", "1.040", "9.0", "30")
        ),
        ChemicalProfileCompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        ProfileTargetObjective.of(new CustomRectangleSugarAcidityTarget(
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
        )),
        scoringConfiguration(),
        ChemicalProfileCompositionCalculatorCode.MASS_BALANCE
    ));

    assertThat(result.normalizerCode()).isNotNull();
    assertThat(result.targetDistance()).isNotNull();
    assertThat(result.targetDistance().scored()).isTrue();
    assertThat(result.targetDistance().insideTarget()).isFalse();
    assertThat(result.targetDistance().value()).isPositive();
  }

  private static @NonNull ChemicalProfileCompositionSource source(
      @NonNull String id,
      @NonNull String sugar,
      @NonNull String acidity,
      @NonNull String liters
  ) {
    return new ChemicalProfileCompositionSource(
        new ChemicalProfileCompositionSourceReference("FEEDSTOCK_BATCH", id),
        compositionSourceCharacteristics(sugar, acidity, null, null),
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

}
