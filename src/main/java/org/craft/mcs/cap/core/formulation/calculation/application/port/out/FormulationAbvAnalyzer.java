package org.craft.mcs.cap.core.formulation.calculation.application.port.out;

import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationPotentialAbvView;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;


/** Formulation-owned port for deterministic potential ABV analysis. */
public interface FormulationAbvAnalyzer {

  @NonNull
  FormulationPotentialAbvView predict(@NonNull SpecificGravityValue originalGravity);
}
