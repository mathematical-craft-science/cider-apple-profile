package org.craft.mcs.cap.core.appleprofile.analysis.application.abv.port.out;

import org.craft.mcs.cap.core.appleprofile.analysis.application.abv.contract.ApplePotentialAbvPredictionView;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;


/** Apple-analysis-owned outbound port for potential ABV analysis. */
public interface ApplePotentialAbvAnalyzer {

  @NonNull
  ApplePotentialAbvPredictionView predict(@NonNull SpecificGravityValue originalGravity);
}
