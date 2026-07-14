package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockSeasonRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@Schema(description = "Request to score current feedstock batches against a profile target.")
public record ScoreFeedstockBatchProfileTargetRequest(
    @Schema(
        description = "Workspace identifier.",
        format = "uuid",
        example = "4d96759c-86ff-4d39-8b19-241bc60f82ff",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID workspaceId,
    @Schema(description = "Optional single feedstock batch filter.", format = "uuid", example = "b2a49b8a-3a19-4f8f-989d-9278f4e00d2f", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable UUID batchId,
    @Schema(description = "Optional season overlap filter.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable FeedstockSeasonRequest season,
    @Schema(description = "Optional sugar-acidity zone filter.", implementation = SugarAcidityZone.class, example = "PERFECT_APPLES", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable SugarAcidityZone zone,
    @Schema(description = "Optional chemical profile capability filter.", implementation = ChemicalProfileCapability.class, example = "SUGAR_ACIDITY_PLOT", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ChemicalProfileCapability capability,
    @Schema(description = "Whether the optional capability must be met.", example = "true", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable Boolean capable,
    @Schema(description = "Scoring purpose.", implementation = ProfileTargetScoringPurpose.class, example = "BLENDING_PLANNING", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetScoringPurpose purpose,
    @Schema(description = "Target to score against.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetRequest target,
    @Schema(description = "Optional tannin target range.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ProfileTargetTanninRangeRequest tanninTarget,
    @Schema(description = "Optional axis weights.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ProfileTargetObjectiveWeightsRequest weights,
    @Schema(description = "Maximum results returned.", example = "20", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable Integer limit
) {

  public ScoreFeedstockBatchProfileTargetRequest {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(purpose, "profile target scoring purpose cannot be null");
    requireNonNull(target, "feedstock batch profile target cannot be null");
  }
}
