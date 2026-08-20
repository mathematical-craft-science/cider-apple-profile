package org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.port.out;

import org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.contract.FeedstockFermentationStartReadinessView;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Feedstock-owned outbound port for fermentation-start readiness analysis. */
public interface FeedstockFermentationStartReadinessAnalyzer {

  @NonNull
  FeedstockFermentationStartReadinessView assess(
      @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
      @Nullable CharacteristicValue<PhValue> ph,
      @Nullable CharacteristicValue<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen
  );
}
