package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionCalculatorCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationMode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveFunctionCode;
import org.jspecify.annotations.NonNull;


@Schema(description = "Composition optimization response.")
public record OptimizeCompositionResponse(
    @Schema(
        description = "Optimization mode.",
        implementation = CompositionOptimizationMode.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull CompositionOptimizationMode mode,
    @Schema(
        description = "Optimization algorithm used.",
        implementation = CompositionOptimizationAlgorithm.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull CompositionOptimizationAlgorithm algorithm,
    @Schema(
        description = "Composition calculator used.",
        implementation = CompositionCalculatorCode.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull CompositionCalculatorCode calculator,
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
        description = "Number of candidate compositions evaluated.",
        example = "210",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    int evaluatedCandidates,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Ranked optimization candidates.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = CompositionOptimizationCandidateResponse.class)
    )
    @NonNull List<@NonNull CompositionOptimizationCandidateResponse> candidates
) {
}
