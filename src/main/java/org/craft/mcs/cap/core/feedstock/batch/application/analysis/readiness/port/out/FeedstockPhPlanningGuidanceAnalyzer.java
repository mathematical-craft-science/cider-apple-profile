package org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.port.out;

import org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.contract.FeedstockPhPlanningGuidanceView;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Feedstock-owned outbound port for non-blocking pH planning guidance. */
public interface FeedstockPhPlanningGuidanceAnalyzer {

  @NonNull
  FeedstockPhPlanningGuidanceView assess(@Nullable CharacteristicValue<PhValue> ph);
}
