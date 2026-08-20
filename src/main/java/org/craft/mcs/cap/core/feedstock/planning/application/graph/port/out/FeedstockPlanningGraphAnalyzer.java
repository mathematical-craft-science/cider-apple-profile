package org.craft.mcs.cap.core.feedstock.planning.application.graph.port.out;

import org.craft.mcs.cap.core.feedstock.planning.application.graph.contract.FeedstockPlanningGraphView;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Feedstock Planning outbound port for SG/TA graph facts required by planning scenarios. */
public interface FeedstockPlanningGraphAnalyzer {

  @NonNull FeedstockPlanningGraphView evaluate(
      @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
      @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity
  );
}
