package org.craft.mcs.cap.core.production.batch.application.port.out;

import org.craft.mcs.cap.core.production.batch.application.contract.ProductionPotentialAbvPredictionView;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;


/** Production-owned outbound port for potential ABV analysis. */
public interface ProductionPotentialAbvAnalyzer {

  @NonNull
  ProductionPotentialAbvPredictionView predict(@NonNull SpecificGravityValue originalGravity);
}
