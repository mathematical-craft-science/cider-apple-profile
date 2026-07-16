package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.classification.dto;


import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.result.ClassificationConfidence;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Classification result for an apple variety in a selected cider classification system.")
public record AppleVarietyClassificationResponse(
    @Schema(description = "Apple variety identifier.", format = "uuid", example = "6d62d888-2a2b-4f47-8f21-2ab2d54d8c2f", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull UUID varietyId,
    @Schema(description = "Apple variety name.", example = "Kingston Black", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull String varietyName,
    @Schema(description = "Classification profile code.", example = "LONG_ASHTON", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull String profileCode,
    @Schema(description = "Whether the variety matched a class.", requiredMode = Schema.RequiredMode.REQUIRED) boolean classified,
    @Schema(description = "Matched apple class when classified.", example = "BITTERSHARP", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Nullable String appleClass,
    @Schema(description = "Classification confidence.", implementation = ClassificationConfidence.class, requiredMode = Schema.RequiredMode.REQUIRED) @NonNull ClassificationConfidence confidence,
    @Schema(description = "Whether the value is close to a classification boundary.", requiredMode = Schema.RequiredMode.REQUIRED) boolean borderline,
    @ArraySchema(
        arraySchema = @Schema(description = "Evidence explaining borderline classification.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = BorderlineEvidenceResponse.class)
    )
    @NonNull List<@NonNull BorderlineEvidenceResponse> borderlineReasons
) {
}
