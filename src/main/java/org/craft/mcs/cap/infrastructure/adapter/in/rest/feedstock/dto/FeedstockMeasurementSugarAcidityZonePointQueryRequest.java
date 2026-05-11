package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.util.UUID;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record FeedstockMeasurementSugarAcidityZonePointQueryRequest(
    @NonNull UUID ciderMakerId,
    @Nullable UUID batchId,
    @Nullable FeedstockSeasonRequest season,
    @Nullable SugarAcidityZone zone,
    @Nullable ChemicalProfileCapability capability,
    @Nullable Boolean capable,
    @Nullable String after,
    @Nullable Integer limit
) {

  public FeedstockMeasurementSugarAcidityZonePointQueryRequest {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
  }
}
