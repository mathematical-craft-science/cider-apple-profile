package org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.abv;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.analysis.application.abv.contract.ApplePotentialAbvPredictionView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.abv.port.out.ApplePotentialAbvAnalyzer;
import org.craft.mcs.cap.core.cideranalysis.abv.application.port.in.PredictPotentialAbvUseCase;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** ACL from AppleProfile potential-ABV analysis to CiderAnalysis. */
@Component
@RequiredArgsConstructor
public final class ApplePotentialAbvAnalysisAdapter implements ApplePotentialAbvAnalyzer {

  private final @NonNull PredictPotentialAbvUseCase useCase;

  @Override
  public @NonNull ApplePotentialAbvPredictionView predict(
      @NonNull SpecificGravityValue originalGravity
  ) {
    final var result = useCase.predict(originalGravity);
    return new ApplePotentialAbvPredictionView(
        result.originalGravity(),
        result.predictedFinalGravity(),
        ApplePotentialAbvPredictionView.FinalGravitySource.valueOf(
            result.predictedFinalGravitySource().name()
        ),
        result.potentialAbv(),
        ApplePotentialAbvPredictionView.Method.valueOf(result.method().name()),
        ApplePotentialAbvPredictionView.Confidence.valueOf(result.confidence().name())
    );
  }
}
