package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZone;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record PredefinedZoneFeedstockBatchBlendProfileTargetRequest(
    @NonNull String kind,
    @NonNull SugarAcidityZone zone
) implements FeedstockBatchBlendProfileTargetRequest {

  public PredefinedZoneFeedstockBatchBlendProfileTargetRequest {
    requireNonNull(kind, "target kind cannot be null");
    requireNonNull(zone, "sugar-acidity zone cannot be null");
  }
}
