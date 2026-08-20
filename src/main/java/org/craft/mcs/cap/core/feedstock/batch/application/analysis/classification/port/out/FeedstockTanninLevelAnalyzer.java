package org.craft.mcs.cap.core.feedstock.batch.application.analysis.classification.port.out;

import org.craft.mcs.cap.core.feedstock.batch.application.analysis.classification.contract.FeedstockNorthAmericanTanninLevel;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.jspecify.annotations.NonNull;


/** Feedstock-owned outbound port for North American tannin interpretation. */
public interface FeedstockTanninLevelAnalyzer {

  @NonNull
  FeedstockNorthAmericanTanninLevel assess(@NonNull CharacteristicValue<TanninValue> tannin);
}
