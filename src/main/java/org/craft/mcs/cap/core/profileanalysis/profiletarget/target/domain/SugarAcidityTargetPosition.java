package org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain;

import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRangePosition;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Per-axis position of an SG/TA point relative to a target area's membership rules. */
@ValueObject
public record SugarAcidityTargetPosition(
    @NonNull MeasuredValueRangePosition specificGravity,
    @NonNull MeasuredValueRangePosition titratableAcidity
) {

  public SugarAcidityTargetPosition {
    requireNonNull(specificGravity, "specific gravity target position cannot be null");
    requireNonNull(titratableAcidity, "titratable acidity target position cannot be null");
  }

  /** Returns whether both SG and TA coordinates belong to the target. */
  public boolean inside() {
    return specificGravity == MeasuredValueRangePosition.WITHIN
        && titratableAcidity == MeasuredValueRangePosition.WITHIN;
  }
}
