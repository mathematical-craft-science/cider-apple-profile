package org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in;

import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record PredefinedFeedstockProfileTargetSelection(
    @NonNull SugarAcidityZone zone
) implements FeedstockProfileTargetSelection {

  public PredefinedFeedstockProfileTargetSelection {
    requireNonNull(zone, "sugar-acidity zone cannot be null");
  }
}
