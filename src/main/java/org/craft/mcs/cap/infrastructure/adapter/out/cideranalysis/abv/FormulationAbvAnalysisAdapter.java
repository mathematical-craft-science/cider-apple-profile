package org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.abv;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.abv.application.port.in.PredictPotentialAbvUseCase;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationPotentialAbvView;
import org.craft.mcs.cap.core.formulation.calculation.application.port.out.FormulationAbvAnalyzer;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** ACL from Formulation ABV language to CiderAnalysis ABV prediction. */
@Component
@RequiredArgsConstructor
public final class FormulationAbvAnalysisAdapter implements FormulationAbvAnalyzer {

  private final @NonNull PredictPotentialAbvUseCase predictPotentialAbvUseCase;

  @Override
  public @NonNull FormulationPotentialAbvView predict(
      @NonNull SpecificGravityValue originalGravity
  ) {
    final var prediction = predictPotentialAbvUseCase.predict(
        requireNonNull(originalGravity, "formulation original gravity cannot be null")
    );
    return new FormulationPotentialAbvView(
        prediction.originalGravity(),
        prediction.predictedFinalGravity(),
        FormulationPotentialAbvView.FinalGravitySource.valueOf(
            prediction.predictedFinalGravitySource().name()
        ),
        prediction.potentialAbv(),
        FormulationPotentialAbvView.Method.valueOf(prediction.method().name()),
        FormulationPotentialAbvView.Confidence.valueOf(prediction.confidence().name())
    );
  }
}
