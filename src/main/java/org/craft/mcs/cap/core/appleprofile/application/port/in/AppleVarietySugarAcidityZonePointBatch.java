package org.craft.mcs.cap.core.appleprofile.application.port.in;

import java.util.List;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record AppleVarietySugarAcidityZonePointBatch(
    @NonNull List<@NonNull AppleVarietySugarAcidityZoneAssessment> points,
    @NonNull List<@NonNull UnplottableAppleVarietySugarAcidityZonePoint> unplottable
) {

  public AppleVarietySugarAcidityZonePointBatch {
    points = List.copyOf(requireNonNull(points, "apple variety sugar-acidity points cannot be null"));
    unplottable = List.copyOf(requireNonNull(unplottable, "unplottable apple variety points cannot be null"));
  }
}
