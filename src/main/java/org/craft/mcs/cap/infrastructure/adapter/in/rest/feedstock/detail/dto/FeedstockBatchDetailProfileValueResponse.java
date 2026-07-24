package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Compact current-profile value for the feedstock detail read model.
 */
@Schema(
    description = """
        Compact current-profile value derived from measurement evidence. This is not the original
        measurement model; it flattens point and interval evidence for read-only detail cards while
        preserving valueKind and optional interval bounds.
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
        implementation = AxisValueKind.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull AxisValueKind valueKind,
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
    @Nullable DecimalQuantityResponse max
) {
}
