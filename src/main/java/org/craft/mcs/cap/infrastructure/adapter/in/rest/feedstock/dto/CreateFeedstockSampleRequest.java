package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockType;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record CreateFeedstockSampleRequest(
    @NonNull UUID batchId,
    @NonNull String name,
    @NonNull FeedstockType type,
    @NonNull ChemicalProfileRequest chemicalProfile,
    @NonNull Instant measuredAt,
    @Nullable String note
) {
}
