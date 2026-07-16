package org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.usecase;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.AbsoluteCompositionVolume;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionSource;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionSourceReference;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.BuiltInChemicalProfileCompositionCalculatorSelector;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.BuiltInProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.BuiltInProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class CalculateChemicalProfileCompositionServiceTest {

  private final CalculateChemicalProfileCompositionService service = new CalculateChemicalProfileCompositionService(
      new BuiltInChemicalProfileCompositionCalculatorSelector(),
      new BuiltInProfileAxisNormalizerSelector(BuiltInSugarAcidityZoneProfiles.thresholds()),
      new BuiltInProfileTargetObjectiveFunctionFactory()
  );

  @Test
  void calculatesChemicalProfileCompositionForProvidedSourceVolumes() {
    final var result = service.calculate(new CalculateChemicalProfileCompositionCommand(
        List.of(
            source("a", "1.060", "5.0", "10"),
            source("b", "1.040", "9.0", "30")
        ),
        ChemicalProfileCompositionTanninPolicy.CALCULATE_WHEN_COMPLETE
    ));

    final var composition = result.composition();
    assertThat(composition.profile().sugar().value()).isEqualByComparingTo("1.045000000");
    assertThat(composition.profile().acidity().value()).isEqualByComparingTo("8.000000000");
    assertThat(composition.profile().tannin()).isNull();
    assertThat(composition.totalVolume()).isNotNull();
    assertThat(composition.totalVolume().liters()).isEqualByComparingTo("40");
    assertThat(composition.sources()).hasSize(2);
    assertThat(composition.sources().getFirst().weight().value()).isEqualByComparingTo("10");
    assertThat(composition.warnings()).isEmpty();
    assertThat(result.targetDistance()).isNull();
    assertThat(result.normalizerCode()).isNull();
  }

  @Test
  void calculatesChemicalProfileCompositionForSingleSource() {
    final var result = service.calculate(new CalculateChemicalProfileCompositionCommand(
        List.of(source("a", "1.060", "5.0", "10")),
        ChemicalProfileCompositionTanninPolicy.CALCULATE_WHEN_COMPLETE
    ));

    final var composition = result.composition();
    assertThat(composition.profile().sugar().value()).isEqualByComparingTo("1.060000000");
    assertThat(composition.profile().acidity().value()).isEqualByComparingTo("5.000000000");
    assertThat(composition.profile().tannin()).isNull();
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
                new BigDecimal("1.060"),
                new BigDecimal("1.070"),
                new BigDecimal("5.0"),
                new BigDecimal("6.0")
            )
        )),
        ProfileTargetObjectiveWeights.balanced(),
        ProfileTargetScoringPurpose.BLENDING_PLANNING
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
        ChemicalProfileAxes.of(
            axis(ChemicalAxis.SUGAR, sugar),
            axis(ChemicalAxis.ACIDITY, acidity),
            null
        ),
        new AbsoluteCompositionVolume(VolumeValue.ofLiters(liters)),
        null
    );
  }

  private static @NonNull AxisValue axis(
      @NonNull ChemicalAxis axis,
      @NonNull String value
  ) {
    return new AxisValue(
        axis,
        new BigDecimal(value),
        MeasurementValueKind.EXACT,
        null,
        null,
        null,
        null
    );
  }
}
