package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZone;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record PredefinedFeedstockSampleBlendProfileTargetSelection(
    @NonNull SugarAcidityZone zone
) implements FeedstockSampleBlendProfileTargetSelection {

  public PredefinedFeedstockSampleBlendProfileTargetSelection {
    requireNonNull(zone, "sugar-acidity zone cannot be null");
  }
}
