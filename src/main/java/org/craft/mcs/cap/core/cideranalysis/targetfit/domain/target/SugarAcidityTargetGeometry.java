package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target;

import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityPointDistance;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityRegion;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRangePosition;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Geometry operations required by sugar-acidity target scoring. */
public interface SugarAcidityTargetGeometry extends SugarAcidityRegion {

  @NonNull
  SugarAcidityGraphPoint nearestPointTo(
      @NonNull SugarAcidityGraphPoint point,
      @NonNull SugarAcidityPointDistance distance
  );

  /**
   * Determines per-axis target position from the source and its nearest target point.
   *
   * <p>Range-backed geometries override this method to preserve exclusive-bound membership.</p>
   */
  default @NonNull SugarAcidityTargetPosition positionOf(
      @NonNull SugarAcidityGraphPoint point,
      @NonNull SugarAcidityGraphPoint nearestTargetPoint
  ) {
    requireNonNull(point, "SG/TA point cannot be null");
    requireNonNull(nearestTargetPoint, "nearest SG/TA target point cannot be null");
    return new SugarAcidityTargetPosition(
        positionOf(point.specificGravity(), nearestTargetPoint.specificGravity()),
        positionOf(point.titratableAcidity(), nearestTargetPoint.titratableAcidity())
    );
  }

  private static @NonNull MeasuredValueRangePosition positionOf(
      @NonNull MeasuredValue value,
      @NonNull MeasuredValue target
  ) {
    return MeasuredValueRangePosition.fromComparison(value.compareTo(target));
  }
}
