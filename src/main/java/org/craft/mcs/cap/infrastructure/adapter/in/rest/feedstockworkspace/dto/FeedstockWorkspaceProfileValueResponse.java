package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Compact chemical profile value for feedstock planning board rows.")
public record FeedstockWorkspaceProfileValueResponse(
    @Schema(
        description = "Canonical value used for compact board display.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse value,
    @Schema(
        description = "Source value shape used to compute the compact value.",
        implementation = MeasurementValueKind.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementValueKind valueKind,
    @Schema(
        description = "Minimum value for range-based measurements.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse min,
    @Schema(
        description = "Maximum value for range-based measurements.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse max,
    @Schema(
        description = "Standard deviation for statistical measurements.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse standardDeviation,
    @Schema(
        description = "Sample size for statistical measurements.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable Integer sampleSize
) {
}
