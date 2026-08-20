package org.craft.mcs.cap.infrastructure.adapter.out.cideranalysis.classification;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.classification.application.contract.CiderAppleCharacteristics;
import org.craft.mcs.cap.core.cideranalysis.classification.application.port.in.AssessNorthAmericanCiderProfileUseCase;
import org.craft.mcs.cap.core.formulation.interpretation.application.contract.FormulationTanninLevel;
import org.craft.mcs.cap.core.formulation.interpretation.application.port.out.FormulationTanninLevelAnalyzer;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** ACL from Formulation graph interpretation to CiderAnalysis classification. */
@Component
@RequiredArgsConstructor
public final class FormulationTanninLevelAnalysisAdapter implements FormulationTanninLevelAnalyzer {

  private final @NonNull AssessNorthAmericanCiderProfileUseCase assessmentUseCase;

  @Override
  public @NonNull FormulationTanninLevel assess(@NonNull TanninValue tannin) {
    final var assessment = assessmentUseCase.assess(new CiderAppleCharacteristics(
        null,
        null,
        PointCharacteristicValue.of(requireNonNull(tannin, "formulation tannin cannot be null"))
    ));
    return FormulationTanninLevel.valueOf(requireNonNull(assessment.tannin()).name());
  }
}
