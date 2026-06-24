package org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in;

import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.RectangleSugarAcidityTargetGeometry;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record CustomRectangleFeedstockProfileTargetSelection(
    @NonNull RectangleSugarAcidityTargetGeometry geometry
) implements FeedstockProfileTargetSelection {

  public CustomRectangleFeedstockProfileTargetSelection {
    requireNonNull(geometry, "custom sugar-acidity rectangle cannot be null");
  }
}
