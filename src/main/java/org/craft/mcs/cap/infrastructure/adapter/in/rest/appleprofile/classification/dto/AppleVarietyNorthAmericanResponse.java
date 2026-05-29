package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.classification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.core.cideranalysis.classification.profile.northamerican.NorthAmericanLevel;
import org.jspecify.annotations.NonNull;

@Schema(description = "North American cider profile terms for an apple variety.")
public record AppleVarietyNorthAmericanResponse(
    @Schema(description = "Apple variety identifier.", format = "uuid", example = "6d62d888-2a2b-4f47-8f21-2ab2d54d8c2f", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull UUID varietyId,
    @Schema(description = "Apple variety name.", example = "Kingston Black", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull String varietyName,
    @Schema(description = "Sugar level term.", implementation = NorthAmericanLevel.class, example = "HIGH", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull NorthAmericanLevel sugar,
    @Schema(description = "Acidity level term.", implementation = NorthAmericanLevel.class, example = "MEDIUM", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull NorthAmericanLevel acidity,
    @Schema(description = "Tannin level term.", implementation = NorthAmericanLevel.class, example = "MEDIUM", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull NorthAmericanLevel tannin
) {
}
