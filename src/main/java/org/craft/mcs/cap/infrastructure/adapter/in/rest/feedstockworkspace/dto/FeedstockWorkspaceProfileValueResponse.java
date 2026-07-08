package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Compact board-row projection of one current chemical profile value.
 *
 * <p>This transport shape is intentionally not the original measurement model. It flattens exact,
 * range, and statistical measurements into one display-oriented value while preserving enough
 * context to explain how that compact value was derived. The full source measurement remains
 * available through {@link FeedstockWorkspaceCurrentMeasurementResponse#chemicalProfile()}.
 */
@Schema(
    description = """
        Compact board-row projection of one current chemical profile value. This is not the original
        measurement model: exact, range, and statistical measurements are flattened into one display
        value with valueKind explaining how that value was derived. The full source measurement is
        available in currentMeasurement.chemicalProfile.
        """
)
public record FeedstockWorkspaceProfileValueResponse(
    @Schema(
        description = """
            Canonical compact value for board display. For exact measurements this is the measured
            value, for ranges this is the midpoint, and for statistical measurements this is median
            when present or mean otherwise.
            """,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse value,
    @Schema(
        description = "Source measurement shape used to derive the compact board value.",
        implementation = MeasurementValueKind.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementValueKind valueKind,
    @Schema(
        description = "Original minimum value for range-based measurements.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse min,
    @Schema(
        description = "Original maximum value for range-based measurements.",
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
