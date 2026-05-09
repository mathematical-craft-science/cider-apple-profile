package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.fermentation.dto.PotentialAbvPredictionResponse;
import org.jspecify.annotations.NonNull;


public record FeedstockSamplePotentialAbvResponse(
    @NonNull String sampleId,
    @NonNull String ciderMakerId,
    @NonNull String sampleName,
    @NonNull String originalGravityValueKind,
    @NonNull PotentialAbvPredictionResponse prediction
) {
}
