package org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.abv;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.abv.application.port.in.PredictPotentialAbvUseCase;
import org.craft.mcs.cap.core.production.batch.application.contract.ProductionPotentialAbvPredictionView;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionPotentialAbvAnalyzer;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** ACL from Production potential-ABV analysis to CiderAnalysis. */
@Component
@RequiredArgsConstructor
public final class ProductionPotentialAbvAnalysisAdapter implements ProductionPotentialAbvAnalyzer {

  private final @NonNull PredictPotentialAbvUseCase useCase;

  @Override
  public @NonNull ProductionPotentialAbvPredictionView predict(
      @NonNull SpecificGravityValue originalGravity
  ) {
    final var result = useCase.predict(originalGravity);
    return new ProductionPotentialAbvPredictionView(
        result.originalGravity(),
        result.predictedFinalGravity(),
        ProductionPotentialAbvPredictionView.FinalGravitySource.valueOf(
            result.predictedFinalGravitySource().name()
        ),
        result.potentialAbv(),
        ProductionPotentialAbvPredictionView.Method.valueOf(result.method().name()),
        ProductionPotentialAbvPredictionView.Confidence.valueOf(result.confidence().name())
    );
  }
}
