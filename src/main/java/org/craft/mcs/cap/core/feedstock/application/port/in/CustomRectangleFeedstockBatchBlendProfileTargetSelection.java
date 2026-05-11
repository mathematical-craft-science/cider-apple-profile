package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.RectangleSugarAcidityTargetGeometry;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record CustomRectangleFeedstockBatchBlendProfileTargetSelection(
    @NonNull RectangleSugarAcidityTargetGeometry geometry
) implements FeedstockBatchBlendProfileTargetSelection {

  public CustomRectangleFeedstockBatchBlendProfileTargetSelection {
    requireNonNull(geometry, "custom sugar-acidity rectangle cannot be null");
  }
}
