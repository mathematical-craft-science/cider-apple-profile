package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.jspecify.annotations.Nullable;


@Schema(description = "Chemical profile request with optional measurements. At least one measurement must be present.")
public record ChemicalProfileRequest(
    @Schema(description = "Sugar measurement. Supported units: SG, °Bx, Brix, °P, Plato.") @Nullable ChemicalMeasurementRequest sugar,
    @Schema(description = "pH measurement.") @Nullable ChemicalMeasurementRequest ph,
    @Schema(description = "Titratable acidity measurement. Supported units include g/L as malic acid, lactic acid, citric acid.") @Nullable ChemicalMeasurementRequest titratableAcidity,
    @Schema(description = "Tannin measurement. Supported units include mg/L TAE and g/L TAE.") @Nullable ChemicalMeasurementRequest tanninity,
    @Schema(description = "Yeast-assimilable nitrogen measurement. Supported unit: mg N/L.") @Nullable ChemicalMeasurementRequest yeastAssimilableNitrogen,
    @Schema(description = "Data source description.", example = "lab") @Nullable String source,
    @Schema(description = "Confidence level for the profile data.", implementation = ConfidenceLevel.class) @Nullable ConfidenceLevel confidence
) {
}
