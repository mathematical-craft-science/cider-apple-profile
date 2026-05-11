package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.util.UUID;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.BlendProfileScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZone;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record ScoreFeedstockBatchBlendProfileTargetRequest(
    @NonNull UUID ciderMakerId,
    @Nullable UUID batchId,
    @Nullable FeedstockSeasonRequest season,
    @Nullable SugarAcidityZone zone,
    @Nullable ChemicalProfileCapability capability,
    @Nullable Boolean capable,
    @NonNull BlendProfileScoringPurpose purpose,
    @NonNull FeedstockBatchBlendProfileTargetRequest target,
    @Nullable TanninTargetRangeRequest tanninTarget,
    @Nullable BlendProfileObjectiveWeightsRequest weights,
    @Nullable Integer limit
) {

  public ScoreFeedstockBatchBlendProfileTargetRequest {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(purpose, "blend profile scoring purpose cannot be null");
    requireNonNull(target, "feedstock batch blend profile target cannot be null");
  }
}
