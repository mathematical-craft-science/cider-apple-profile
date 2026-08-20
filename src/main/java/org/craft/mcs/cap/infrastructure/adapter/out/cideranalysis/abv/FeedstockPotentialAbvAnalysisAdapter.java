package org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.abv;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.abv.application.port.in.PredictPotentialAbvUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.abv.contract.FeedstockPotentialAbvPredictionView;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.abv.port.out.FeedstockPotentialAbvAnalyzer;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** ACL from Feedstock potential-ABV analysis to CiderAnalysis. */
@Component
@RequiredArgsConstructor
public final class FeedstockPotentialAbvAnalysisAdapter implements FeedstockPotentialAbvAnalyzer {

  private final @NonNull PredictPotentialAbvUseCase useCase;

  @Override
  public @NonNull FeedstockPotentialAbvPredictionView predict(
      @NonNull SpecificGravityValue originalGravity
  ) {
    final var result = useCase.predict(originalGravity);
    return new FeedstockPotentialAbvPredictionView(
        result.originalGravity(),
        result.predictedFinalGravity(),
        FeedstockPotentialAbvPredictionView.FinalGravitySource.valueOf(
            result.predictedFinalGravitySource().name()
        ),
        result.potentialAbv(),
        FeedstockPotentialAbvPredictionView.Method.valueOf(result.method().name()),
        FeedstockPotentialAbvPredictionView.Confidence.valueOf(result.confidence().name())
    );
  }
}
