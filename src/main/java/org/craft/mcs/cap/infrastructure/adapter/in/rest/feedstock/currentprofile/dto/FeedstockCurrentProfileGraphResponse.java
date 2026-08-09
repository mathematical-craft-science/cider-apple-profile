package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.GraphMissingCharacteristic;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphAssessmentResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Current batch sugar-acidity graph projection.")
public record FeedstockCurrentProfileGraphResponse(
    @Schema(
        description = "Whether current SG and TA evidence can be plotted on the sugar-acidity graph.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    boolean plottable,
    @Schema(
        description = "Jolicoeur zone assessment and SG/TA graph point when plottable.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable SugarAcidityGraphAssessmentResponse assessment,
    @ArraySchema(
        arraySchema = @Schema(
            description = "SG or TA inputs missing from an unplottable current profile.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = GraphMissingCharacteristic.class)
    )
    @NonNull List<@NonNull GraphMissingCharacteristic> missingCharacteristics
) {
}
