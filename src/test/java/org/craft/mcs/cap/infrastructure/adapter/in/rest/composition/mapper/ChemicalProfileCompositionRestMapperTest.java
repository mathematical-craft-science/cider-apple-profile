package org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionResult;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionRequest;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.MassBalanceChemicalProfileCompositionStrategy;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto.CalculateChemicalProfileCompositionRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto.ChemicalProfileCompositionAxesRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto.ChemicalProfileCompositionSourceReferenceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto.ChemicalProfileCompositionSourceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto.ChemicalProfileCompositionSourceWeightKind;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto.ChemicalProfileCompositionSourceWeightRequest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ChemicalProfileCompositionRestMapperTest {

  private static final MassBalanceChemicalProfileCompositionStrategy CALCULATOR =
      new MassBalanceChemicalProfileCompositionStrategy();

  @Test
  void mapsYanAxisForCalculationRequestAndResponse() {
    final var command = ChemicalProfileCompositionRestMapper.toCommand(
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
            null,
            null
        ),
        zone -> {
          throw new AssertionError("target resolver should not be used without predefined target");
        }
    );

    assertThat(command.sources().getFirst().axes().valueOf(ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN))
        .isPresent()
        .get()
        .extracting(AxisValue::value)
        .isEqualTo(new BigDecimal("120"));

    final var composition = CALCULATOR.calculate(new ChemicalProfileCompositionRequest(
        command.sources(),
        command.tanninPolicy()
    ));
    final var response = ChemicalProfileCompositionRestMapper.toResponse(
        CalculateChemicalProfileCompositionResult.withoutTargetDistance(composition)
    );

    assertThat(response.yeastAssimilableNitrogen()).isNotNull();
    assertThat(response.yeastAssimilableNitrogen().value()).isEqualByComparingTo("210.000000000");
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
        new ChemicalProfileCompositionAxesRequest(
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
}
