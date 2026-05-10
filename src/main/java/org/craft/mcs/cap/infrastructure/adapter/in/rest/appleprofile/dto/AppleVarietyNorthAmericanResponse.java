package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.shared.domain.classification.profile.northamerican.NorthAmericanLevel;
import org.jspecify.annotations.NonNull;

@Schema(description = "North American cider profile terms for an apple variety.")
public record AppleVarietyNorthAmericanResponse(
    @Schema(description = "Apple variety identifier.", format = "uuid", example = "6d62d888-2a2b-4f47-8f21-2ab2d54d8c2f") @NonNull UUID varietyId,
    @Schema(description = "Apple variety name.", example = "Kingston Black") @NonNull String varietyName,
    @Schema(description = "Sugar level term.", implementation = NorthAmericanLevel.class, example = "HIGH") @NonNull NorthAmericanLevel sugar,
    @Schema(description = "Acidity level term.", implementation = NorthAmericanLevel.class, example = "MEDIUM") @NonNull NorthAmericanLevel acidity,
    @Schema(description = "Tannin level term.", implementation = NorthAmericanLevel.class, example = "MEDIUM") @NonNull NorthAmericanLevel tannin
) {
}
