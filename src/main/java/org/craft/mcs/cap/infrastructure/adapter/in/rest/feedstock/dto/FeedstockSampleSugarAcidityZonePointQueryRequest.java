package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.util.UUID;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.SugarAcidityZone;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record FeedstockSampleSugarAcidityZonePointQueryRequest(
    @NonNull UUID ciderMakerId,
    @Nullable UUID batchId,
    @Nullable FeedstockSeasonRequest season,
    @Nullable SugarAcidityZone zone,
    @Nullable ChemicalProfileCapability capability,
    @Nullable Boolean capable,
    @Nullable String after,
    @Nullable Integer limit
) {

  public FeedstockSampleSugarAcidityZonePointQueryRequest {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
  }
}
