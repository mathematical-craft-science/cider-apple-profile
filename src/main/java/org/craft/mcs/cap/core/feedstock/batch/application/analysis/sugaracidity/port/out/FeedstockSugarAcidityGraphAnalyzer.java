package org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.port.out;

import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.contract.FeedstockSugarAcidityGraphView;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Feedstock-owned outbound port for SG/TA graph evaluation. */
public interface FeedstockSugarAcidityGraphAnalyzer {

  @NonNull
  FeedstockSugarAcidityGraphView evaluate(
      @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
      @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity
  );
}
