package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementunits.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;


/**
 * Transport representation of one canonical measurement unit and its accepted numeric range.
 *
 * <p>The response keeps physical values as {@link DecimalQuantityResponse} so API clients receive
 * both the numeric bound and the unit label instead of relying on hard-coded frontend assumptions.
 * Short and full labels are explicit because compact controls and detailed value displays have
 * different text needs.
 */
@Schema(description = "Canonical unit metadata for configuring numeric inputs and displays.")
public record MeasurementUnitMetadataResponse(
    @Schema(
        description = "Canonical unit code.",
        implementation = MeasurementUnitCode.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementUnitCode code,
    @Schema(
        description = "Canonical unit label used by CAP API responses.",
        example = "SG",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String canonicalUnit,
    @Schema(
        description = "Compact unit label for dense controls.",
        example = "g/L",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String shortLabel,
    @Schema(
        description = "Full unit label for detailed displays and tooltips.",
        example = "g/L as malic acid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String fullLabel,
    @Schema(description = "Minimum accepted value in the canonical unit.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse min,
    @Schema(description = "Maximum accepted value in the canonical unit.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse max,
    @Schema(description = "Suggested UI input step in the canonical unit.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse step
) {
}
