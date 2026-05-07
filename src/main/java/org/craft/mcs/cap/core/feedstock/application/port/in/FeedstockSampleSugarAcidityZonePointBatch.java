package org.craft.mcs.cap.core.feedstock.application.port.in;

import java.util.List;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockSampleSugarAcidityZonePointBatch(
    @NonNull List<@NonNull FeedstockSampleSugarAcidityZoneAssessment> points,
    @NonNull List<@NonNull UnplottableFeedstockSampleSugarAcidityZonePoint> unplottable
) {

  public FeedstockSampleSugarAcidityZonePointBatch {
    points = List.copyOf(requireNonNull(points, "feedstock sample sugar-acidity points cannot be null"));
    unplottable = List.copyOf(requireNonNull(unplottable, "unplottable feedstock sample points cannot be null"));
  }
}
