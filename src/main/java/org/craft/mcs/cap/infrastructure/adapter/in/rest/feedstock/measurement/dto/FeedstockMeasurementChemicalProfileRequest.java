package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalMeasurementRequest;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.jspecify.annotations.Nullable;


/**
 * Deprecated Feedstock transport shape that preserves profile-wide provenance for compatibility.
 */
@Schema(description = "Deprecated Feedstock chemical profile request with profile-wide provenance.")
public record FeedstockMeasurementChemicalProfileRequest(
    @Schema(
        description = "Deprecated sugar field converted to canonical Specific Gravity.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ChemicalMeasurementRequest sugar,
    @Schema(
        description = "pH measurement.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ChemicalMeasurementRequest ph,
    @Schema(
        description = "Titratable acidity measurement.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ChemicalMeasurementRequest titratableAcidity,
    @Schema(
        description = "Tannin measurement.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ChemicalMeasurementRequest tannin,
    @Schema(
        description = "Yeast-assimilable nitrogen measurement.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ChemicalMeasurementRequest yeastAssimilableNitrogen,
    @Schema(
        description = "Legacy source shared by all supplied characteristics.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String source,
    @Schema(
        description = "Legacy confidence shared by all supplied characteristics.",
        implementation = ConfidenceLevel.class,
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ConfidenceLevel confidence
) {
}
