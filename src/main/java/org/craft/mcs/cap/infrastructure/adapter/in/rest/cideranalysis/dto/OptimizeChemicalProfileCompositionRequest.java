package org.craft.mcs.cap.infrastructure.adapter.in.rest.cideranalysis.dto;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.CompositionOptimizationMode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjectiveFunctionCode;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record OptimizeChemicalProfileCompositionRequest(
    @NonNull List<@NonNull ChemicalProfileCompositionOptimizationSourceRequest> sources,
    @Nullable ChemicalProfileCompositionTanninPolicy tanninPolicy,
    @NonNull ProfileTargetRequest target,
    @Nullable ProfileTargetTanninRangeRequest tanninTarget,
    @Nullable ProfileTargetObjectiveWeightsRequest weights,
    @Nullable ProfileTargetScoringPurpose scoringPurpose,
    @NonNull CompositionOptimizationMode mode,
    @Nullable CompositionOptimizationAlgorithm algorithm,
    @Nullable ChemicalProfileCompositionCalculatorCode calculator,
    @Nullable ProfileAxisNormalizerCode normalizer,
    @Nullable ProfileTargetObjectiveFunctionCode objectiveFunction,
    @Nullable Integer stepPercent,
    @Nullable Integer limit,
    @Nullable BigDecimal targetTotalVolumeLiters
) {
}
