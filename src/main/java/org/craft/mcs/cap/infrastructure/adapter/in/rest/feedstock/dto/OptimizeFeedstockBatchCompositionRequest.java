package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.CompositionOptimizationMode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZone;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record OptimizeFeedstockBatchCompositionRequest(
    @NonNull UUID ciderMakerId,
    @Nullable List<@NonNull UUID> batchIds,
    @Nullable FeedstockSeasonRequest season,
    @Nullable SugarAcidityZone zone,
    @Nullable ChemicalProfileCapability capability,
    @Nullable Boolean capable,
    @Nullable ProfileTargetScoringPurpose purpose,
    @NonNull FeedstockBatchBlendProfileTargetRequest target,
    @Nullable TanninTargetRangeRequest tanninTarget,
    @Nullable ProfileTargetObjectiveWeightsRequest weights,
    @NonNull CompositionOptimizationMode mode,
    @Nullable CompositionOptimizationAlgorithm algorithm,
    @Nullable ChemicalProfileCompositionCalculatorCode calculator,
    @Nullable ProfileAxisNormalizerCode normalizer,
    @Nullable ProfileTargetObjectiveFunctionCode objectiveFunction,
    @Nullable Integer stepPercent,
    @Nullable Integer limit,
    @Nullable Integer sourceLimit,
    @Nullable BigDecimal targetTotalVolumeLiters
) {

  public OptimizeFeedstockBatchCompositionRequest {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(target, "feedstock batch blend profile target cannot be null");
    requireNonNull(mode, "composition optimization mode cannot be null");
    batchIds = batchIds == null ? null : List.copyOf(batchIds);
  }
}
