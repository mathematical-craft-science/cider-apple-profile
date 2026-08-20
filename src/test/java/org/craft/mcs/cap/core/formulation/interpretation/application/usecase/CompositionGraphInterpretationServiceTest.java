package org.craft.mcs.cap.core.formulation.interpretation.application.usecase;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CalculatedCompositionView;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationPotentialAbvView;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CalculateCompositionResult;
import org.craft.mcs.cap.core.formulation.interpretation.application.contract.CalculatedCompositionGraphView;
import org.craft.mcs.cap.core.formulation.interpretation.application.contract.FormulationTanninLevel;
import org.craft.mcs.cap.core.formulation.interpretation.application.port.out.FormulationTanninLevelAnalyzer;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.AbvValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


class CompositionGraphInterpretationServiceTest {

  @ParameterizedTest
  @CsvSource({
      "1499.999, LOW",
      "1500, MEDIUM",
      "2499.999, MEDIUM",
      "2500, HIGH"
  })
  void interpretsCalculatedTanninForGraphPointColor(
      @NonNull String tannin,
      @NonNull FormulationTanninLevel expectedLevel
  ) {
    final var analyzer = mock(FormulationTanninLevelAnalyzer.class);
    final var service = new CompositionGraphInterpretationService(analyzer);
    when(analyzer.assess(TanninValue.of(tannin))).thenReturn(expectedLevel);

    assertThat(service.interpret(calculation(tannin)))
        .returns(expectedLevel, CalculatedCompositionGraphView::northAmericanTanninLevel);
  }

  @Test
  void omitsGraphTanninLevelWhenCompositionTanninWasNotCalculated() {
    final var analyzer = mock(FormulationTanninLevelAnalyzer.class);
    final var service = new CompositionGraphInterpretationService(analyzer);

    assertThat(service.interpret(calculation(null)).northAmericanTanninLevel())
        .isNull();
    verifyNoInteractions(analyzer);
  }

  private static @NonNull CalculateCompositionResult calculation(
      @Nullable String tannin
  ) {
    final var specificGravity = SpecificGravityValue.of("1.060");
    final var composition = new CalculatedCompositionView(
        List.of(),
        new CalculatedCompositionView.Characteristics(
            specificGravity,
            TitratableAcidityValue.of("5.0"),
            tannin == null ? null : TanninValue.of(tannin),
            null
        ),
        null,
        List.of()
    );
    return new CalculateCompositionResult(
        composition,
        new FormulationPotentialAbvView(
            specificGravity,
            SpecificGravityValue.of("1.000"),
            FormulationPotentialAbvView.FinalGravitySource.FIXED_DRY_CIDER_DEFAULT,
            AbvValue.of(new BigDecimal("7.875")),
            FormulationPotentialAbvView.Method.STANDARD_GRAVITY_DROP,
            FormulationPotentialAbvView.Confidence.MEDIUM
        ),
        null,
        null
    );
  }
}
