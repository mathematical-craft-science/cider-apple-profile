package org.craft.mcs.cap.core.feedstock.batch.application.analysis.abv.port.out;

import org.craft.mcs.cap.core.feedstock.batch.application.analysis.abv.contract.FeedstockPotentialAbvPredictionView;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;


/** Feedstock-owned outbound port for potential ABV analysis. */
public interface FeedstockPotentialAbvAnalyzer {

  @NonNull
  FeedstockPotentialAbvPredictionView predict(@NonNull SpecificGravityValue originalGravity);
}
