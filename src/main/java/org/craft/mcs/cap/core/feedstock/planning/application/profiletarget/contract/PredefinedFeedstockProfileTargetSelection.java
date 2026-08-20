package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract;

import org.craft.mcs.cap.core.feedstock.planning.application.graph.contract.FeedstockPlanningGraphView.Zone;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record PredefinedFeedstockProfileTargetSelection(
    @NonNull Zone zone
) implements FeedstockProfileTargetSelection {

  public PredefinedFeedstockProfileTargetSelection {
    requireNonNull(zone, "sugar-acidity zone cannot be null");
  }
}
