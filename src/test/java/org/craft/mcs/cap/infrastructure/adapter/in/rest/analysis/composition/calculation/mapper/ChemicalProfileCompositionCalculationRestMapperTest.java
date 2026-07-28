package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionResult;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionRequest;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.MassBalanceChemicalProfileCompositionStrategy;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.CalculateChemicalProfileCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.ChemicalProfileCompositionSourceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.ChemicalProfileCompositionSourceWeightKind;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto.ChemicalProfileCompositionSourceWeightRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.CompositionSourceCharacteristicsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.ChemicalProfileCompositionSourceReferenceRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static java.util.Objects.requireNonNull;


class ChemicalProfileCompositionCalculationRestMapperTest {

  private static final MassBalanceChemicalProfileCompositionStrategy CALCULATOR =
      new MassBalanceChemicalProfileCompositionStrategy();
  private static final PotentialAbvPredictor POTENTIAL_ABV_PREDICTOR = new PotentialAbvPredictor(
      FixedFinalGravityPredictionStrategy.dryCiderDefault(),
      new StandardGravityDropAbvPredictionStrategy()
  );

  @Test
  void mapsYanAxisForCalculationRequestAndResponse() {
    final var command = ChemicalProfileCompositionCalculationRestMapper.toCommand(
        new CalculateChemicalProfileCompositionRequest(
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
        ),
        _ -> {
          throw new AssertionError("target resolver should not be used without predefined target");
        }
    );

    assertThat(yanPointValue(
        requireNonNull(command.sources().getFirst().characteristics().yeastAssimilableNitrogen())
    ).asBigDecimal()).isEqualByComparingTo("120");

    final var composition = CALCULATOR.calculate(new ChemicalProfileCompositionRequest(
        command.sources(),
        command.tanninPolicy()
    ));
    final var response = ChemicalProfileCompositionCalculationRestMapper.toResponse(
        CalculateChemicalProfileCompositionResult.withoutTargetDistance(
            composition,
            POTENTIAL_ABV_PREDICTOR.predict(composition.characteristics().specificGravity())
        )
    );

    assertThat(response.characteristics())
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
                .isEqualByComparingTo("210.000000000")));
    assertThat(response.totalVolume())
        .isNotNull()
        .returns("L", DecimalQuantityResponse::unit);
    assertThat(response.potentialAbv())
        .satisfies(prediction -> assertThat(prediction.originalGravity().value())
            .isEqualByComparingTo("1.045000000"))
        .satisfies(prediction -> assertThat(prediction.potentialAbv().value())
            .isEqualByComparingTo("5.906"))
        .satisfies(prediction -> assertThat(prediction.potentialAbv().unit()).isEqualTo("% ABV"));
    assertThat(response.sources())
        .extracting(source -> source.requestedVolume().unit())
        .containsOnly("L");
  }

  private static @NonNull ChemicalProfileCompositionSourceRequest source(
      @NonNull String id,
      @NonNull String sugar,
      @NonNull String acidity,
      @NonNull String tannin,
      @NonNull String yeastAssimilableNitrogen,
      @NonNull String volumeLiters
  ) {
    return new ChemicalProfileCompositionSourceRequest(
        new ChemicalProfileCompositionSourceReferenceRequest("FEEDSTOCK_BATCH", id),
        new CompositionSourceCharacteristicsRequest(
            new BigDecimal(sugar),
            new BigDecimal(acidity),
            new BigDecimal(tannin),
            new BigDecimal(yeastAssimilableNitrogen)
        ),
        new ChemicalProfileCompositionSourceWeightRequest(
            ChemicalProfileCompositionSourceWeightKind.ABSOLUTE_VOLUME_LITERS,
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
