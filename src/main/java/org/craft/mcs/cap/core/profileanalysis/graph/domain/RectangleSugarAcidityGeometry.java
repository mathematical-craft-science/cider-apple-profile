package org.craft.mcs.cap.core.profileanalysis.graph.domain;

import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Reusable axis-aligned rectangle in the SG/TA coordinate space. */
@ValueObject
public record RectangleSugarAcidityGeometry(
    @NonNull MeasuredValueRange<SpecificGravityValue> specificGravityRange,
    @NonNull MeasuredValueRange<TitratableAcidityValue> titratableAcidityRange
) implements SugarAcidityRegion {

  public RectangleSugarAcidityGeometry {
    requireNonNull(specificGravityRange, "rectangle specific gravity range cannot be null");
    requireNonNull(titratableAcidityRange, "rectangle titratable acidity range cannot be null");
  }

  @Override
  public @NonNull SugarAcidityGeometryShape shape() {
    return SugarAcidityGeometryShape.RECTANGLE;
  }

  @Override
  public boolean contains(@NonNull SugarAcidityGraphPoint point) {
    requireNonNull(point, "SG/TA point cannot be null");
    return specificGravityRange.contains(point.specificGravity())
        && titratableAcidityRange.contains(point.titratableAcidity());
  }

  public @NonNull SugarAcidityGraphPoint nearestPointTo(
      @NonNull SugarAcidityGraphPoint point
  ) {
    requireNonNull(point, "SG/TA point cannot be null");
    return new SugarAcidityGraphPoint(
        specificGravityRange.projectOntoClosure(point.specificGravity()),
        titratableAcidityRange.projectOntoClosure(point.titratableAcidity())
    );
  }
}
