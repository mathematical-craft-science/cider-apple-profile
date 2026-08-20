package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target;

import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.EllipseSugarAcidityGeometry;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityGeometryShape;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityPointDistance;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Elliptical SG/TA area used for containment and nearest-target distance calculations. */
public record EllipseSugarAcidityTargetGeometry(
    @NonNull EllipseSugarAcidityGeometry geometry
) implements SugarAcidityTargetGeometry {

  public EllipseSugarAcidityTargetGeometry {
    requireNonNull(geometry, "target ellipse geometry cannot be null");
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
    return geometry.nearestPointTo(point, distance);
  }
}
