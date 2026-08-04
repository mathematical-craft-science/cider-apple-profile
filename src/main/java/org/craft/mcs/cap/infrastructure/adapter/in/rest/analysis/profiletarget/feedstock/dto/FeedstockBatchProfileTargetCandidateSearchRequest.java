package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.feedstock.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisCapability;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.model.ProfileTargetAnalysisScenario;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockSeasonRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@Schema(description = "Search request for target-aware feedstock batch candidates.")
public record FeedstockBatchProfileTargetCandidateSearchRequest(
    @Schema(description = "Workspace identifier.", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID workspaceId,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Optional feedstock batch identifier filter.",
            nullable = true,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
        ),
        schema = @Schema(format = "uuid")
    )
    @Nullable List<@NonNull UUID> batchIds,
    @Schema(
        description = "Optional season overlap filter.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockSeasonRequest season,
    @Schema(
        description = "Optional Jolicoeur sugar-acidity zone filter.",
        implementation = JolicoeurSugarAcidityZone.class,
        example = "PERFECT_APPLES",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable JolicoeurSugarAcidityZone zone,
    @Schema(
        description = "Optional profile-analysis capability filter.",
        implementation = ProfileAnalysisCapability.class,
        example = "SUGAR_ACIDITY_GRAPH",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ProfileAnalysisCapability capability,
    @Schema(
        description = "Whether the optional capability must be met.",
        example = "true",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable Boolean capable,
    @Schema(
        description = "Analysis scenario used to select scoring defaults.",
        implementation = ProfileTargetAnalysisScenario.class,
        example = "BLENDING_PLANNING",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonAlias("purpose")
    @NonNull ProfileTargetAnalysisScenario analysisScenario,
    @Schema(description = "Target to score candidates against.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetRequest target,
    @Schema(
        description = "Optional tannin target range.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ProfileTargetTanninRangeRequest tanninTarget,
    @Schema(description = "Optional axis weights.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ProfileTargetObjectiveWeightsRequest weights,
    @Schema(
        description = "Maximum results returned.",
        minimum = "1",
        maximum = "200",
        defaultValue = "50",
        example = "50",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable Integer limit
) {

  public FeedstockBatchProfileTargetCandidateSearchRequest {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(analysisScenario, "profile target analysis scenario cannot be null");
    requireNonNull(target, "feedstock batch profile target cannot be null");
  }
}
