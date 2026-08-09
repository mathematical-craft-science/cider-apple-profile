package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.GraphMissingCharacteristic;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto.ProfileCharacteristicValueResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(
    description = "Graph-oriented apple variety analysis backed by canonical SG, TA and optional tannin evidence."
)
public record AppleVarietySugarAcidityZoneResponse(
    @Schema(
        description = "Apple variety identifier.",
        format = "uuid",
        example = "6d62d888-2a2b-4f47-8f21-2ab2d54d8c2f",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID varietyId,
    @Schema(
        description = "Apple variety name.",
        example = "Kingston Black",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String varietyName,
    @Schema(
        description = "Whether the variety can be plotted.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    boolean plottable,
    @Schema(
        description = "Zone assessment when plottable.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable SugarAcidityGraphAssessmentResponse assessment,
    @Schema(
        description = "Canonical tannin characteristic in mg/L TAE, when available.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ProfileCharacteristicValueResponse tannin,
    @Schema(
        description = "Independently available North American sugar, acidity and tannin interpretations.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable AppleVarietyNorthAmericanProfileResponse northAmericanProfile,
    @Schema(
        description = "Potential ABV predicted from the variety sugar profile, when sugar is available.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable PotentialAbvPredictionResponse potentialAbv,
    @ArraySchema(
        arraySchema = @Schema(
            description = "SG or TA inputs missing from an unplottable graph projection.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = GraphMissingCharacteristic.class)
    )
    @NonNull List<@NonNull GraphMissingCharacteristic> missingCharacteristics
) {
}
