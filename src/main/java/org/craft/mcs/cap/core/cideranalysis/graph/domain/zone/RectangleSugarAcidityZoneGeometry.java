package org.craft.mcs.cap.core.cideranalysis.graph.domain.zone;

import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.RectangleSugarAcidityGeometry;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityGeometryShape;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityGraphPoint;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Rectangular domain boundary used to classify representative SG/TA points. */
public record RectangleSugarAcidityZoneGeometry(
    @NonNull JolicoeurSugarAcidityZoneId zone,
    @NonNull RectangleSugarAcidityGeometry geometry
) implements JolicoeurSugarAcidityZoneGeometry {

  public RectangleSugarAcidityZoneGeometry {
    requireNonNull(zone, "sugar-acidity zone cannot be null");
    requireNonNull(geometry, "zone rectangle geometry cannot be null");
  }

  public RectangleSugarAcidityZoneGeometry(
      @NonNull JolicoeurSugarAcidityZoneId zone,
      @NonNull MeasuredValueRange<SpecificGravityValue> specificGravityRange,
      @NonNull MeasuredValueRange<TitratableAcidityValue> titratableAcidityRange
  ) {
    this(zone, new RectangleSugarAcidityGeometry(specificGravityRange, titratableAcidityRange));
  }

  @Override
  public @NonNull SugarAcidityGeometryShape shape() {
    return geometry.shape();
  }

  @Override
  public boolean contains(@NonNull SugarAcidityGraphPoint point) {
    return geometry.contains(point);
  }

  public @NonNull MeasuredValueRange<SpecificGravityValue> specificGravityRange() {
    return geometry.specificGravityRange();
  }

  public @NonNull MeasuredValueRange<TitratableAcidityValue> titratableAcidityRange() {
    return geometry.titratableAcidityRange();
  }
}
