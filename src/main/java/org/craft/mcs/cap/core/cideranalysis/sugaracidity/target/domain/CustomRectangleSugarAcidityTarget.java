package org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain;

import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record CustomRectangleSugarAcidityTarget(
    @NonNull RectangleSugarAcidityTargetGeometry geometry
) implements SugarAcidityTarget {

  public CustomRectangleSugarAcidityTarget {
    requireNonNull(geometry, "custom sugar-acidity rectangle cannot be null");
  }

  @Override
  public @NonNull SugarAcidityTargetKind kind() {
    return SugarAcidityTargetKind.CUSTOM_RECTANGLE;
  }
}
