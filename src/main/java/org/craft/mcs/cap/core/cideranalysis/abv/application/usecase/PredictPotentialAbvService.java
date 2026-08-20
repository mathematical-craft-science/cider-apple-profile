package org.craft.mcs.cap.core.cideranalysis.abv.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.abv.application.contract.AbvPredictionMethod;
import org.craft.mcs.cap.core.cideranalysis.abv.application.contract.FinalGravityPredictionSource;
import org.craft.mcs.cap.core.cideranalysis.abv.application.contract.PotentialAbvPredictionView;
import org.craft.mcs.cap.core.cideranalysis.abv.application.contract.PredictionConfidence;
import org.craft.mcs.cap.core.cideranalysis.abv.application.port.in.PredictPotentialAbvUseCase;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Application boundary for deterministic potential ABV analysis. */
@Component
@RequiredArgsConstructor
public final class PredictPotentialAbvService implements PredictPotentialAbvUseCase {

  private final @NonNull PotentialAbvPredictor predictor;

  @Override
  public @NonNull PotentialAbvPredictionView predict(@NonNull SpecificGravityValue originalGravity) {
    final var prediction = predictor.predict(
        requireNonNull(originalGravity, "original gravity cannot be null")
    );
    return new PotentialAbvPredictionView(
        prediction.originalGravity(),
        prediction.predictedFinalGravity(),
        FinalGravityPredictionSource.valueOf(prediction.predictedFinalGravitySource().name()),
        prediction.potentialAbv(),
        AbvPredictionMethod.valueOf(prediction.method().name()),
        PredictionConfidence.valueOf(prediction.confidence().name())
    );
  }
}
