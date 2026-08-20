package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.formulation.calculation.application.usecase.CalculateCompositionService;
import org.craft.mcs.cap.core.formulation.calculation.domain.BuiltInCompositionCalculatorSelector;
import org.craft.mcs.cap.core.formulation.interpretation.application.usecase.CompositionGraphInterpretationService;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CalculateCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CalculatedCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CompositionSourceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CompositionSourceWeightKind;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CompositionSourceWeightRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.CompositionSourceCharacteristicsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.CompositionSourceReferenceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.classification.NorthAmericanTanninLevel;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionTanninPolicy;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.support.CiderAnalysisTestFixtures.formulationAbvAnalyzer;
import static org.craft.mcs.cap.support.CiderAnalysisTestFixtures.formulationTanninLevelAnalyzer;
import static org.craft.mcs.cap.support.CiderAnalysisTestFixtures.formulationTargetFitAnalyzer;


class CompositionCalculationRestMapperTest {

  private static final CalculateCompositionService CALCULATOR = new CalculateCompositionService(
      new BuiltInCompositionCalculatorSelector(),
      formulationAbvAnalyzer(),
      formulationTargetFitAnalyzer()
  );
  private static final CompositionGraphInterpretationService GRAPH_INTERPRETER =
      new CompositionGraphInterpretationService(formulationTanninLevelAnalyzer());

  @Test
  void mapsYanAxisForCalculationRequestAndResponse() {
    final var command = CompositionCalculationRestMapper.toCommand(
        new CalculateCompositionRequest(
            List.of(
                source("a", "1.060", "5.0", "1000", "120", "10"),
                source("b", "1.040", "9.0", "2000", "240", "30")
            ),
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
    );

    assertThat(yanPointValue(
        requireNonNull(command.sources().getFirst().characteristics().yeastAssimilableNitrogen())
    ).asBigDecimal()).isEqualByComparingTo("120");

    final var response = CompositionCalculationRestMapper.toResponse(
        GRAPH_INTERPRETER.interpret(CALCULATOR.calculate(command))
    );

    assertThat(response)
        .returns(
            NorthAmericanTanninLevel.MEDIUM,
            CalculatedCompositionResponse::northAmericanTanninLevel
        )
        .satisfies(value -> assertThat(value.characteristics())
            .satisfies(characteristics -> assertThat(characteristics.specificGravity())
                .returns("SG", DecimalQuantityResponse::unit))
            .satisfies(characteristics -> assertThat(characteristics.titratableAcidity())
                .returns("g/L as malic acid", DecimalQuantityResponse::unit))
            .satisfies(characteristics -> assertThat(characteristics.tannin())
                .isNotNull()
                .returns("mg/L TAE", DecimalQuantityResponse::unit))
            .satisfies(characteristics -> assertThat(characteristics.yeastAssimilableNitrogen())
                .isNotNull()
                .returns("mg N/L", DecimalQuantityResponse::unit)
                .satisfies(quantity -> assertThat(quantity.value())
                    .isEqualByComparingTo("210.000000000"))))
        .satisfies(value -> assertThat(value.totalVolume())
            .isNotNull()
            .returns("L", DecimalQuantityResponse::unit))
        .satisfies(value -> assertThat(value.potentialAbv())
            .satisfies(prediction -> assertThat(prediction.originalGravity().value())
                .isEqualByComparingTo("1.045000000"))
            .satisfies(prediction -> assertThat(prediction.potentialAbv().value())
                .isEqualByComparingTo("5.906"))
            .satisfies(prediction -> assertThat(prediction.potentialAbv().unit()).isEqualTo("% ABV")))
        .satisfies(value -> assertThat(value.sources())
            .extracting(source -> source.requestedVolume().unit())
            .containsOnly("L"));
  }

  @Test
  void rejectsTargetScoringConfigurationWithoutTarget() {
    final var request = new CalculateCompositionRequest(
        List.of(source("a", "1.060", "5.0", "1000", "120", "10")),
        null,
        null,
        null,
        null,
        null,
        ProfileAxisNormalizerCode.EFFECTIVE_RANGE,
        null
    );

    assertThatThrownBy(() -> CompositionCalculationRestMapper.toCommand(request))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("composition target is required when target scoring configuration is provided");
  }

  @ParameterizedTest
  @EnumSource(CompositionTanninPolicy.class)
  void preservesEveryTanninPolicyAcrossTransportAndFormulation(
      @NonNull CompositionTanninPolicy policy
  ) {
    final var command = CompositionCalculationRestMapper.toCommand(new CalculateCompositionRequest(
        List.of(source("a", "1.060", "5.0", "1000", "120", "10")),
        policy,
        null,
        null,
        null,
        null,
        null,
        null
    ));

    assertThat(command.tanninPolicy().name()).isEqualTo(policy.name());
    assertThat(CALCULATOR.calculate(command).composition()).isNotNull();
  }

  private static @NonNull CompositionSourceRequest source(
      @NonNull String id,
      @NonNull String specificGravity,
      @NonNull String titratableAcidity,
      @NonNull String tannin,
      @NonNull String yeastAssimilableNitrogen,
      @NonNull String volumeLiters
  ) {
    return new CompositionSourceRequest(
        new CompositionSourceReferenceRequest("FEEDSTOCK_BATCH", id),
        new CompositionSourceCharacteristicsRequest(
            new BigDecimal(specificGravity),
            new BigDecimal(titratableAcidity),
            new BigDecimal(tannin),
            new BigDecimal(yeastAssimilableNitrogen)
        ),
        new CompositionSourceWeightRequest(
            CompositionSourceWeightKind.ABSOLUTE_VOLUME_LITERS,
            new BigDecimal(volumeLiters)
        ),
        null
    );
  }

  private static @NonNull YeastAssimilableNitrogenValue yanPointValue(
      @NonNull CharacteristicValue<YeastAssimilableNitrogenValue> value
  ) {
    return switch (value) {
      case PointCharacteristicValue<YeastAssimilableNitrogenValue>(var point) -> point;
      case IntervalCharacteristicValue<YeastAssimilableNitrogenValue> _ ->
          throw new AssertionError("expected point YAN characteristic");
    };
  }
}
