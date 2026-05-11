package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.util.UUID;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.ProfileTargetScoringPurpose;
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
    @NonNull ProfileTargetScoringPurpose purpose,
    @NonNull FeedstockBatchBlendProfileTargetRequest target,
    @Nullable TanninTargetRangeRequest tanninTarget,
    @Nullable ProfileTargetObjectiveWeightsRequest weights,
    @Nullable Integer limit
) {

  public ScoreFeedstockBatchBlendProfileTargetRequest {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(purpose, "profile target scoring purpose cannot be null");
    requireNonNull(target, "feedstock batch blend profile target cannot be null");
  }
}
