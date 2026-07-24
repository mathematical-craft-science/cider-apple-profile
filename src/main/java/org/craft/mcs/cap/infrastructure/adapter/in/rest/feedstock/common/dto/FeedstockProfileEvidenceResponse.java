package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentMeasurementFreshnessStatus;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockProfileCharacteristic;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Evidence and freshness selected for one current Feedstock profile characteristic.")
public record FeedstockProfileEvidenceResponse(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FeedstockProfileCharacteristic characteristic,
    @Schema(format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID measurementSetId,
    @Schema(format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID recordId,
    @Schema(format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull Instant measuredAt,
    @Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable String source,
    @Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ConfidenceLevel confidence,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FeedstockCurrentMeasurementFreshnessStatus freshnessStatus,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean stale
) {
}
