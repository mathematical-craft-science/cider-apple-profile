package org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in;

import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record PredefinedFeedstockProfileTargetSelection(
    @NonNull JolicoeurSugarAcidityZone zone
) implements FeedstockProfileTargetSelection {

  public PredefinedFeedstockProfileTargetSelection {
    requireNonNull(zone, "sugar-acidity zone cannot be null");
  }
}
