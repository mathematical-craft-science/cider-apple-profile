package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.RectangleSugarAcidityTargetGeometry;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record CustomRectangleFeedstockProfileTargetSelection(
    @NonNull RectangleSugarAcidityTargetGeometry geometry
) implements FeedstockProfileTargetSelection {

  public CustomRectangleFeedstockProfileTargetSelection {
    requireNonNull(geometry, "custom SG/TA rectangle cannot be null");
  }
}
