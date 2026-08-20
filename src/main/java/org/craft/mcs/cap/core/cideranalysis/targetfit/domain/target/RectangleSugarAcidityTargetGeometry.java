package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target;

import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.RectangleSugarAcidityGeometry;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityGeometryShape;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityPointDistance;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Rectangular SG/TA area used for containment and nearest-target distance calculations. */
public record RectangleSugarAcidityTargetGeometry(
    @NonNull RectangleSugarAcidityGeometry geometry
) implements SugarAcidityTargetGeometry {

  public RectangleSugarAcidityTargetGeometry {
    requireNonNull(geometry, "target rectangle geometry cannot be null");
  }

  public RectangleSugarAcidityTargetGeometry(
      @NonNull MeasuredValueRange<SpecificGravityValue> specificGravityRange,
      @NonNull MeasuredValueRange<TitratableAcidityValue> titratableAcidityRange
  ) {
    this(new RectangleSugarAcidityGeometry(specificGravityRange, titratableAcidityRange));
  }

  @Override
  public @NonNull SugarAcidityGeometryShape shape() {
    return geometry.shape();
  }

  @Override
  public boolean contains(@NonNull SugarAcidityGraphPoint point) {
    return geometry.contains(point);
  }

  @Override
  public @NonNull SugarAcidityGraphPoint nearestPointTo(
      @NonNull SugarAcidityGraphPoint point,
      @NonNull SugarAcidityPointDistance distance
  ) {
    requireNonNull(distance, "SG/TA point distance cannot be null");
    return geometry.nearestPointTo(point);
  }

  @Override
  public @NonNull SugarAcidityTargetPosition positionOf(
      @NonNull SugarAcidityGraphPoint point,
      @NonNull SugarAcidityGraphPoint nearestTargetPoint
  ) {
    requireNonNull(point, "SG/TA point cannot be null");
    requireNonNull(nearestTargetPoint, "nearest SG/TA target point cannot be null");
    return new SugarAcidityTargetPosition(
        specificGravityRange().positionOf(point.specificGravity()),
        titratableAcidityRange().positionOf(point.titratableAcidity())
    );
  }

  public @NonNull MeasuredValueRange<SpecificGravityValue> specificGravityRange() {
    return geometry.specificGravityRange();
  }

  public @NonNull MeasuredValueRange<TitratableAcidityValue> titratableAcidityRange() {
    return geometry.titratableAcidityRange();
  }
}
