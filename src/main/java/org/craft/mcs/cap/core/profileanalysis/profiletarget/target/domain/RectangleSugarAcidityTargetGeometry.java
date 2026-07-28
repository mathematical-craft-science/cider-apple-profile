package org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain;

import org.craft.mcs.cap.core.profileanalysis.graph.domain.RectangleSugarAcidityGeometry;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGeometryShape;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityPointDistance;
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

  public @NonNull MeasuredValueRange<SpecificGravityValue> specificGravityRange() {
    return geometry.specificGravityRange();
  }

  public @NonNull MeasuredValueRange<TitratableAcidityValue> titratableAcidityRange() {
    return geometry.titratableAcidityRange();
  }
}
