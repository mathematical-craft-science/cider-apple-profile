package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.CompositionOptimizationMode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjectiveFunctionCode;
import org.jspecify.annotations.NonNull;


public record OptimizeFeedstockBatchCompositionResponse(
    int sourceCount,
    @NonNull CompositionOptimizationMode mode,
    @NonNull CompositionOptimizationAlgorithm algorithm,
    @NonNull ChemicalProfileCompositionCalculatorCode calculator,
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @NonNull ProfileTargetObjectiveFunctionCode objectiveFunction,
    int stepPercent,
    int evaluatedCandidates,
    @NonNull List<@NonNull FeedstockBatchCompositionOptimizationCandidateResponse> candidates
) {
}
