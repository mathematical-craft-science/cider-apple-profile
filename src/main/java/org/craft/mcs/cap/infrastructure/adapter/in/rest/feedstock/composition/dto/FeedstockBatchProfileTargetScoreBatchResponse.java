package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetAnalysisScenario;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;


@Schema(description = "Batch response containing feedstock target distance scores.")
public record FeedstockBatchProfileTargetScoreBatchResponse(
    @Schema(
        description = "Analysis scenario.",
        implementation = ProfileTargetAnalysisScenario.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileTargetAnalysisScenario analysisScenario,
    @Schema(
        description = "Normalizer resolved for the analysis scenario.",
        implementation = ProfileAxisNormalizerCode.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @ArraySchema(
        arraySchema = @Schema(description = "Feedstock target scores.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = FeedstockBatchProfileTargetScoreResponse.class)
    )
    @NonNull List<@NonNull FeedstockBatchProfileTargetScoreResponse> scores
) {
}
