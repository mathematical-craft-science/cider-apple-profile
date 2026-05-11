package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain.CompositionOptimizationMode;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetTanninRangeRequest;
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
    @NonNull ProfileTargetRequest target,
    @Nullable ProfileTargetTanninRangeRequest tanninTarget,
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
    requireNonNull(target, "feedstock batch profile target cannot be null");
    requireNonNull(mode, "composition optimization mode cannot be null");
    batchIds = batchIds == null ? null : List.copyOf(batchIds);
  }
}
