package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target;

import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Explicit rectangular sugar-acidity target selected by a technologist or client. */
public record CustomRectangleSugarAcidityTarget(
    @NonNull RectangleSugarAcidityTargetGeometry geometry
) implements SugarAcidityTarget {

  public CustomRectangleSugarAcidityTarget {
    requireNonNull(geometry, "custom SG/TA rectangle cannot be null");
  }

  @Override
  public @NonNull SugarAcidityTargetKind kind() {
    return SugarAcidityTargetKind.CUSTOM_RECTANGLE;
  }
}
