package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;


@Schema(description = "Chemical profile request with optional measurements. At least one measurement must be present.")
public record ChemicalProfileRequest(
    @Schema(
        description = "Sugar measurement. Supported units: SG, °Bx, Brix, °P, Plato.",
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
        description = "Titratable acidity measurement. Supported units include g/L as malic, lactic, or citric acid.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ChemicalMeasurementRequest titratableAcidity,
    @Schema(
        description = "Tannin measurement. Supported units include mg/L TAE and g/L TAE.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ChemicalMeasurementRequest tannin,
    @Schema(
        description = "Yeast-assimilable nitrogen measurement. Supported unit: mg N/L.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ChemicalMeasurementRequest yeastAssimilableNitrogen
) {
}
