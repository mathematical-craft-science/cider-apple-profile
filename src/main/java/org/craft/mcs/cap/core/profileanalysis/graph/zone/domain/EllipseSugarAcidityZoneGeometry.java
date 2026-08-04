package org.craft.mcs.cap.core.profileanalysis.graph.zone.domain;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.EllipseSugarAcidityGeometry;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGeometryShape;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Elliptical domain boundary used to classify representative SG/TA points. */
public record EllipseSugarAcidityZoneGeometry(
    @NonNull JolicoeurSugarAcidityZone zone,
    @NonNull EllipseSugarAcidityGeometry geometry
) implements JolicoeurSugarAcidityZoneGeometry {

  public EllipseSugarAcidityZoneGeometry {
    requireNonNull(zone, "sugar-acidity zone cannot be null");
    requireNonNull(geometry, "zone ellipse geometry cannot be null");
  }

  public EllipseSugarAcidityZoneGeometry(
      @NonNull JolicoeurSugarAcidityZone zone,
      @NonNull SpecificGravityValue specificGravityCenter,
      @NonNull TitratableAcidityValue titratableAcidityCenter,
      @NonNull BigDecimal specificGravityRadius,
      @NonNull BigDecimal titratableAcidityRadius
  ) {
    this(
        zone,
        new EllipseSugarAcidityGeometry(
            specificGravityCenter,
            titratableAcidityCenter,
            specificGravityRadius,
            titratableAcidityRadius
        )
    );
  }

  @Override
  public @NonNull SugarAcidityGeometryShape shape() {
    return geometry.shape();
  }

  @Override
  public boolean contains(@NonNull SugarAcidityGraphPoint point) {
    return geometry.contains(point);
  }

  public @NonNull SpecificGravityValue specificGravityCenter() {
    return geometry.specificGravityCenter();
  }

  public @NonNull TitratableAcidityValue titratableAcidityCenter() {
    return geometry.titratableAcidityCenter();
  }

  public @NonNull BigDecimal specificGravityRadius() {
    return geometry.specificGravityRadius();
  }

  public @NonNull BigDecimal titratableAcidityRadius() {
    return geometry.titratableAcidityRadius();
  }
}
