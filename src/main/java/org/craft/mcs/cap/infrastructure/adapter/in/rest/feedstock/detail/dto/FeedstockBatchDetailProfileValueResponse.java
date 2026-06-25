package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Compact current-profile value for the feedstock detail read model.
 */
@Schema(
    description = """
        Compact current-profile value derived from the latest measurement. This is not the original
        measurement model; it flattens exact, range and statistical evidence for read-only detail
        cards while preserving valueKind and optional source shape values.
        """
)
public record FeedstockBatchDetailProfileValueResponse(
    @Schema(
        description = "Canonical compact value for detail display.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse value,
    @Schema(
        description = "Source measurement shape used to derive the compact value.",
        implementation = MeasurementValueKind.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementValueKind valueKind,
    @Schema(
        description = "Original minimum value for range measurements.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse min,
    @Schema(
        description = "Original maximum value for range measurements.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse max,
    @Schema(
        description = "Original standard deviation for statistical measurements.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse standardDeviation,
    @Schema(
        description = "Original sample size for statistical measurements.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable Integer sampleSize
) {
}
