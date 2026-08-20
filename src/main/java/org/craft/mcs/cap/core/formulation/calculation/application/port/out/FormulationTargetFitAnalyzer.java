package org.craft.mcs.cap.core.formulation.calculation.application.port.out;

import org.craft.mcs.cap.core.formulation.calculation.application.contract.CalculatedCompositionView.Characteristics;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDistanceView;
import org.jspecify.annotations.NonNull;


/**
 * Formulation-owned port for stateless CiderAnalysis target-fit evaluation.
 */
public interface FormulationTargetFitAnalyzer {

  @NonNull
  FormulationTargetDistanceView evaluate(
      @NonNull Characteristics characteristics,
      @NonNull FormulationTargetDefinition target
  );
}
