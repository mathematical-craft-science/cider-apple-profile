package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain.CompositionOptimizationMode;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionCode;
import org.jspecify.annotations.NonNull;


@Schema(description = "Feedstock batch composition optimization response.")
public record OptimizeFeedstockBatchCompositionResponse(
    @Schema(description = "Number of source batches considered.", example = "5")
    int sourceCount,
    @Schema(description = "Optimization mode.", implementation = CompositionOptimizationMode.class)
    @NonNull CompositionOptimizationMode mode,
    @Schema(description = "Optimization algorithm.", implementation = CompositionOptimizationAlgorithm.class)
    @NonNull CompositionOptimizationAlgorithm algorithm,
    @Schema(description = "Composition calculator used.", implementation = ChemicalProfileCompositionCalculatorCode.class)
    @NonNull ChemicalProfileCompositionCalculatorCode calculator,
    @Schema(description = "Axis normalizer used.", implementation = ProfileAxisNormalizerCode.class)
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @Schema(description = "Objective function used.", implementation = ProfileTargetObjectiveFunctionCode.class)
    @NonNull ProfileTargetObjectiveFunctionCode objectiveFunction,
    @Schema(description = "Discrete search step in percent.", example = "5")
    int stepPercent,
    @Schema(description = "Number of evaluated candidates.", example = "210")
    int evaluatedCandidates,
    @ArraySchema(schema = @Schema(description = "Ranked composition candidate.", implementation = FeedstockBatchCompositionOptimizationCandidateResponse.class))
    @NonNull List<@NonNull FeedstockBatchCompositionOptimizationCandidateResponse> candidates
) {
}
