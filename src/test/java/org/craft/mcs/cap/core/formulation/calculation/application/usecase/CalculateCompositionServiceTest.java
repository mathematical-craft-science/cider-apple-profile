package org.craft.mcs.cap.core.formulation.calculation.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange.closed;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CompositionSourceInput;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationPotentialAbvView;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationCalculator;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTanninPolicy;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDistanceView;
import org.craft.mcs.cap.core.formulation.calculation.application.port.in.CalculateCompositionCommand;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CalculateCompositionResult;
import org.craft.mcs.cap.core.formulation.calculation.application.port.out.FormulationTargetFitAnalyzer;
import org.craft.mcs.cap.core.formulation.calculation.domain.BuiltInCompositionCalculatorSelector;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.AbvValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;


class CalculateCompositionServiceTest {

  private final FormulationTargetFitAnalyzer targetFitAnalyzer = mock(FormulationTargetFitAnalyzer.class);
  private final CalculateCompositionService service = new CalculateCompositionService(
      new BuiltInCompositionCalculatorSelector(),
      originalGravity -> potentialAbv(originalGravity, "5.906"),
      targetFitAnalyzer
  );

  @Test
  void calculatesCompositionForProvidedSourceVolumes() {
    final var result = service.calculate(new CalculateCompositionCommand(
        List.of(
            source("a", "1.060", "5.0", "10"),
            source("b", "1.040", "9.0", "30")
        ),
        FormulationTanninPolicy.CALCULATE_WHEN_COMPLETE,
        null,
        FormulationCalculator.MASS_BALANCE
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
            .satisfies(source -> assertThat(((CompositionSourceInput.AbsoluteVolume) source.weight())
                .volume().liters()).isEqualByComparingTo("10")))
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
        FormulationTanninPolicy.CALCULATE_WHEN_COMPLETE,
        null,
        FormulationCalculator.MASS_BALANCE
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
    final var distance = mock(FormulationTargetDistanceView.class);
    when(targetFitAnalyzer.evaluate(any(), any())).thenReturn(distance);
    final var target = targetDefinition();
    final var result = service.calculate(new CalculateCompositionCommand(
        List.of(
            source("a", "1.060", "5.0", "10"),
            source("b", "1.040", "9.0", "30")
        ),
        FormulationTanninPolicy.CALCULATE_WHEN_COMPLETE,
        target,
        FormulationCalculator.MASS_BALANCE
    ));

    assertThat(result)
        .satisfies(value -> assertThat(value.normalizerCode()).isNotNull())
        .returns(distance, CalculateCompositionResult::targetDistance);
    verify(targetFitAnalyzer).evaluate(result.composition().characteristics(), target);
  }

  @Test
  void rejectsPartialTargetAssessmentMetadataInResult() {
    when(targetFitAnalyzer.evaluate(any(), any())).thenReturn(mock(FormulationTargetDistanceView.class));
    final var withoutTarget = service.calculate(new CalculateCompositionCommand(
        List.of(source("a", "1.060", "5.0", "10")),
        FormulationTanninPolicy.CALCULATE_WHEN_COMPLETE,
        null,
        FormulationCalculator.MASS_BALANCE
    ));
    final var withTarget = service.calculate(new CalculateCompositionCommand(
        List.of(source("a", "1.060", "5.0", "10")),
        FormulationTanninPolicy.CALCULATE_WHEN_COMPLETE,
        targetDefinition(),
        FormulationCalculator.MASS_BALANCE
    ));
    final var withoutTargetComposition = withoutTarget.composition();
    final var withoutTargetPotentialAbv = withoutTarget.potentialAbv();
    final var withTargetComposition = withTarget.composition();
    final var withTargetPotentialAbv = withTarget.potentialAbv();
    final var targetDistance = withTarget.targetDistance();

    assertThatThrownBy(() -> new CalculateCompositionResult(
        withoutTargetComposition,
        withoutTargetPotentialAbv,
        FormulationTargetDefinition.Normalizer.EFFECTIVE_RANGE,
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

  private static @NonNull CompositionSourceInput source(
      @NonNull String id,
      @NonNull String specificGravity,
      @NonNull String titratableAcidity,
      @NonNull String liters
  ) {
    return new CompositionSourceInput(
        new CompositionSourceInput.Reference("FEEDSTOCK_BATCH", id),
        new CompositionSourceInput.Characteristics(
            PointCharacteristicValue.of(SpecificGravityValue.of(specificGravity)),
            PointCharacteristicValue.of(TitratableAcidityValue.of(titratableAcidity)),
            null,
            null
        ),
        new CompositionSourceInput.AbsoluteVolume(VolumeValue.ofLiters(liters)),
        null
    );
  }

  private static @NonNull FormulationTargetDefinition targetDefinition() {
    return new FormulationTargetDefinition(
        new FormulationTargetDefinition.CustomRectangleTarget(
            closed(
                SpecificGravityValue.of("1.060"),
                SpecificGravityValue.of("1.070")
            ),
            closed(
                TitratableAcidityValue.of("5.0"),
                TitratableAcidityValue.of("6.0")
            )
        ),
        null,
        new FormulationTargetDefinition.ScoringConfiguration(
            FormulationTargetDefinition.Normalizer.EFFECTIVE_RANGE,
            FormulationTargetDefinition.ObjectiveFunction.DISTANCE_TO_TARGET_AREA,
            new FormulationTargetDefinition.Weights(BigDecimal.ONE, BigDecimal.ONE, null)
        )
    );
  }

  private static @NonNull FormulationPotentialAbvView potentialAbv(
      @NonNull SpecificGravityValue originalGravity,
      @NonNull String percent
  ) {
    return new FormulationPotentialAbvView(
        originalGravity,
        SpecificGravityValue.of("1.000"),
        FormulationPotentialAbvView.FinalGravitySource.FIXED_DRY_CIDER_DEFAULT,
        AbvValue.of(new BigDecimal(percent)),
        FormulationPotentialAbvView.Method.STANDARD_GRAVITY_DROP,
        FormulationPotentialAbvView.Confidence.MEDIUM
    );
  }

}
