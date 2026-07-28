package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;


@Schema(description = "Chemical profile response in canonical service units.")
public record ChemicalProfileResponse(
    @Schema(
        description = "Specific Gravity (SG) in the canonical service unit.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ChemicalMeasurementResponse specificGravity,
    @Schema(
        description = "pH measurement.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ChemicalMeasurementResponse ph,
    @Schema(
        description = "Titratable acidity measurement in g/L as malic acid.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ChemicalMeasurementResponse titratableAcidity,
    @Schema(
        description = "Tannin measurement in mg/L TAE.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ChemicalMeasurementResponse tannin,
    @Schema(
        description = "Yeast-assimilable nitrogen measurement in mg N/L.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ChemicalMeasurementResponse yeastAssimilableNitrogen
) {
}
