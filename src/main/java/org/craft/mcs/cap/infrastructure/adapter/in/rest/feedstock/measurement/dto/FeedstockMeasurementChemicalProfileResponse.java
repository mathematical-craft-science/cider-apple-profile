package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalMeasurementResponse;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.jspecify.annotations.Nullable;


/**
 * Deprecated Feedstock response shape that reconstructs profile-wide provenance when all axes agree.
 */
@Schema(description = "Deprecated Feedstock chemical profile response with profile-wide provenance.")
public record FeedstockMeasurementChemicalProfileResponse(
    @Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ChemicalMeasurementResponse sugar,
    @Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ChemicalMeasurementResponse ph,
    @Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ChemicalMeasurementResponse titratableAcidity,
    @Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ChemicalMeasurementResponse tannin,
    @Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ChemicalMeasurementResponse yeastAssimilableNitrogen,
    @Schema(
        description = "Common source when all result records agree.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String source,
    @Schema(
        description = "Common confidence when all result records agree.",
        implementation = ConfidenceLevel.class,
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ConfidenceLevel confidence
) {
}
