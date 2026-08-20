package org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.abv;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.abv.application.contract.AbvPredictionMethod;
import org.craft.mcs.cap.core.cideranalysis.abv.application.contract.FinalGravityPredictionSource;
import org.craft.mcs.cap.core.cideranalysis.abv.application.contract.PotentialAbvPredictionView;
import org.craft.mcs.cap.core.cideranalysis.abv.application.contract.PredictionConfidence;
import org.craft.mcs.cap.core.cideranalysis.abv.application.port.in.PredictPotentialAbvUseCase;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationView;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationPotentialAbvView;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidatePotentialAbvView;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.AbvValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class FormulationAbvAnalysisAdapterTest {

  private static final SpecificGravityValue ORIGINAL_GRAVITY = SpecificGravityValue.of("1.052");
  private static final SpecificGravityValue FINAL_GRAVITY = SpecificGravityValue.of("1.000");

  @ParameterizedTest
  @EnumSource(FinalGravityPredictionSource.class)
  void mapsEveryPublishedFinalGravitySource(@NonNull FinalGravityPredictionSource source) {
    final var provider = mock(PredictPotentialAbvUseCase.class);
    when(provider.predict(ORIGINAL_GRAVITY)).thenReturn(new PotentialAbvPredictionView(
        ORIGINAL_GRAVITY,
        FINAL_GRAVITY,
        source,
        AbvValue.of(new BigDecimal("6.825")),
        AbvPredictionMethod.STANDARD_GRAVITY_DROP,
        PredictionConfidence.MEDIUM
    ));

    final var result = new FormulationAbvAnalysisAdapter(provider).predict(ORIGINAL_GRAVITY);

    assertThat(result.predictedFinalGravitySource())
        .isEqualTo(FormulationPotentialAbvView.FinalGravitySource.valueOf(source.name()));
  }

  @ParameterizedTest
  @EnumSource(FinalGravityPredictionSource.class)
  void keepsFeedstockOptimizationSnapshotCompatible(@NonNull FinalGravityPredictionSource source) {
    assertThat(FeedstockCompositionOptimizationView.FinalGravitySource.valueOf(source.name()))
        .hasToString(source.name());
  }

  @ParameterizedTest
  @EnumSource(FinalGravityPredictionSource.class)
  void keepsProductionCandidateSnapshotCompatible(@NonNull FinalGravityPredictionSource source) {
    assertThat(ProductionCandidatePotentialAbvView.FinalGravitySource.valueOf(source.name()))
        .hasToString(source.name());
  }
}
