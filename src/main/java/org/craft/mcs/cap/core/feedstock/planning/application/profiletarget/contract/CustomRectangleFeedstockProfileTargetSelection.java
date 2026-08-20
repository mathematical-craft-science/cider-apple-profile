package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract;

import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record CustomRectangleFeedstockProfileTargetSelection(
    @NonNull MeasuredValueRange<SpecificGravityValue> specificGravity,
    @NonNull MeasuredValueRange<TitratableAcidityValue> titratableAcidity
) implements FeedstockProfileTargetSelection {

  public CustomRectangleFeedstockProfileTargetSelection {
    requireNonNull(specificGravity, "custom target specific gravity range cannot be null");
    requireNonNull(titratableAcidity, "custom target titratable acidity range cannot be null");
  }
}
