package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@Schema(description = "Request to score current feedstock batches against a profile target.")
public record ScoreFeedstockBatchProfileTargetRequest(
    @Schema(description = "Cider maker identifier.", format = "uuid", example = "4d96759c-86ff-4d39-8b19-241bc60f82ff")
    @NonNull UUID ciderMakerId,
    @Schema(description = "Optional single feedstock batch filter.", format = "uuid", example = "b2a49b8a-3a19-4f8f-989d-9278f4e00d2f")
    @Nullable UUID batchId,
    @Schema(description = "Optional season overlap filter.")
    @Nullable FeedstockSeasonRequest season,
    @Schema(description = "Optional sugar-acidity zone filter.", implementation = SugarAcidityZone.class, example = "PERFECT_APPLES")
    @Nullable SugarAcidityZone zone,
    @Schema(description = "Optional chemical profile capability filter.", implementation = ChemicalProfileCapability.class, example = "SUGAR_ACIDITY_PLOT")
    @Nullable ChemicalProfileCapability capability,
    @Schema(description = "Whether the optional capability must be met.", example = "true")
    @Nullable Boolean capable,
    @Schema(description = "Scoring purpose.", implementation = ProfileTargetScoringPurpose.class, example = "BLENDING_PLANNING")
    @NonNull ProfileTargetScoringPurpose purpose,
    @Schema(description = "Target to score against.")
    @NonNull ProfileTargetRequest target,
    @Schema(description = "Optional tannin target range.")
    @Nullable ProfileTargetTanninRangeRequest tanninTarget,
    @Schema(description = "Optional axis weights.")
    @Nullable ProfileTargetObjectiveWeightsRequest weights,
    @Schema(description = "Maximum results returned.", example = "20")
    @Nullable Integer limit
) {

  public ScoreFeedstockBatchProfileTargetRequest {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(purpose, "profile target scoring purpose cannot be null");
    requireNonNull(target, "feedstock batch profile target cannot be null");
  }
}
