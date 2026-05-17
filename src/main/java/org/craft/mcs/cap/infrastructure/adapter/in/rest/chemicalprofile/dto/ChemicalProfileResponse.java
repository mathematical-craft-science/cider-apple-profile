package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.jspecify.annotations.Nullable;


@Schema(description = "Chemical profile response in canonical service units.")
public record ChemicalProfileResponse(
    @Schema(description = "Sugar measurement in SG.") 
    @Nullable ChemicalMeasurementResponse sugar,
    @Schema(description = "pH measurement.")
    @Nullable ChemicalMeasurementResponse ph,
    @Schema(description = "Titratable acidity measurement in g/L as malic acid.")
    @Nullable ChemicalMeasurementResponse titratableAcidity,
    @Schema(description = "Tannin measurement in mg/L TAE.")
    @Nullable ChemicalMeasurementResponse tanninity,
    @Schema(description = "Data source description.", example = "lab")
    @Nullable String source,
    @Schema(description = "Confidence level for profile data.", implementation = ConfidenceLevel.class)
    @Nullable ConfidenceLevel confidence
) {
}
