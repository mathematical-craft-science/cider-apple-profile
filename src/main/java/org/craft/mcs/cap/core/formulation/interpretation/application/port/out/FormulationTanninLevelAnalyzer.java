package org.craft.mcs.cap.core.formulation.interpretation.application.port.out;

import org.craft.mcs.cap.core.formulation.interpretation.application.contract.FormulationTanninLevel;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.jspecify.annotations.NonNull;


/** Formulation-owned port for North American tannin interpretation. */
public interface FormulationTanninLevelAnalyzer {

  @NonNull
  FormulationTanninLevel assess(@NonNull TanninValue tannin);
}
