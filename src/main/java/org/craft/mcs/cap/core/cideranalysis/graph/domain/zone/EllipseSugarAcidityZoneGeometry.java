package org.craft.mcs.cap.core.cideranalysis.graph.domain.zone;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.EllipseSugarAcidityGeometry;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityGeometryShape;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityGraphPoint;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Elliptical domain boundary used to classify representative SG/TA points. */
public record EllipseSugarAcidityZoneGeometry(
    @NonNull JolicoeurSugarAcidityZoneId zone,
    @NonNull EllipseSugarAcidityGeometry geometry
) implements JolicoeurSugarAcidityZoneGeometry {

  public EllipseSugarAcidityZoneGeometry {
    requireNonNull(zone, "sugar-acidity zone cannot be null");
    requireNonNull(geometry, "zone ellipse geometry cannot be null");
  }

  public EllipseSugarAcidityZoneGeometry(
      @NonNull JolicoeurSugarAcidityZoneId zone,
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
