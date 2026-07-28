package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationMode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.jspecify.annotations.NonNull;


@Schema(description = "Feedstock batch composition optimization response.")
public record OptimizeFeedstockBatchCompositionResponse(
    @Schema(
        description = "Number of source batches considered.",
        example = "5",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    int sourceCount,
    @Schema(
        description = "Optimization mode.",
        implementation = CompositionOptimizationMode.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull CompositionOptimizationMode mode,
    @Schema(
        description = "Optimization algorithm.",
        implementation = CompositionOptimizationAlgorithm.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull CompositionOptimizationAlgorithm algorithm,
    @Schema(
        description = "Composition calculator used.",
        implementation = ChemicalProfileCompositionCalculatorCode.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ChemicalProfileCompositionCalculatorCode calculator,
    @Schema(
        description = "Axis normalizer used.",
        implementation = ProfileAxisNormalizerCode.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @Schema(
        description = "Objective function used.",
        implementation = ProfileTargetObjectiveFunctionCode.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileTargetObjectiveFunctionCode objectiveFunction,
    @Schema(
        description = "Discrete search step in percent.",
        example = "5",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    int stepPercent,
    @Schema(
        description = "Number of evaluated candidates.",
        example = "210",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    int evaluatedCandidates,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Ranked composition candidates.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = FeedstockBatchCompositionOptimizationCandidateResponse.class)
    )
    @NonNull List<@NonNull FeedstockBatchCompositionOptimizationCandidateResponse> candidates
) {
}
