package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileCapabilityAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphAssessmentResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Sugar-acidity assessment for an apple variety, backed by SG and TA evidence.")
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
        description = "Potential ABV predicted from the variety sugar profile, when sugar is available.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable PotentialAbvPredictionResponse potentialAbv,
    @Schema(
        description = "Capability assessment explaining missing measurements.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ChemicalProfileCapabilityAssessmentResponse capabilityAssessment
) {
}
