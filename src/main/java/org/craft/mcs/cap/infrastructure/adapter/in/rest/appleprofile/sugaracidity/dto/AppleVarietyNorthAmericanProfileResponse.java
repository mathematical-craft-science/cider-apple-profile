package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanAcidityLevel;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanSugarLevel;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanTanninLevel;
import org.jspecify.annotations.Nullable;


/** Optional per-characteristic North American interpretation for graph exploration. */
@Schema(
    description = "Independently available North American cider-profile terms for one apple variety."
)
public record AppleVarietyNorthAmericanProfileResponse(
    @Schema(
        description = "Sugar level inferred from Specific Gravity (SG).",
        implementation = NorthAmericanSugarLevel.class,
        example = "HIGH",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable NorthAmericanSugarLevel sugar,
    @Schema(
        description = "Acidity level inferred from Titratable Acidity (TA).",
        implementation = NorthAmericanAcidityLevel.class,
        example = "MEDIUM",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable NorthAmericanAcidityLevel acidity,
    @Schema(
        description = "Tannin level term.",
        implementation = NorthAmericanTanninLevel.class,
        example = "MEDIUM",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable NorthAmericanTanninLevel tannin
) {
}
