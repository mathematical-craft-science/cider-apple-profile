package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionCalculatorCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionTanninPolicy;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationMode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationYanReadinessRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Request to optimize source composition against a profile target.")
public record OptimizeCompositionRequest(
    @ArraySchema(
        arraySchema = @Schema(description = "Optimization sources.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = CompositionOptimizationSourceRequest.class)
    )
    @NonNull List<@NonNull CompositionOptimizationSourceRequest> sources,
    @Schema(
        description = "Policy for optional tannin calculation.",
        implementation = CompositionTanninPolicy.class,
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable CompositionTanninPolicy tanninPolicy,
    @Schema(description = "Required profile target.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetRequest target,
    @Schema(
        description = "Optional tannin target range.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ProfileTargetTanninRangeRequest tanninTarget,
    @Schema(description = "Optional axis weights.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ProfileTargetObjectiveWeightsRequest weights,
    @Schema(
        description = "Optimization mode.",
        implementation = CompositionOptimizationMode.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull CompositionOptimizationMode mode,
    @Schema(
        description = "Optimization algorithm.",
        implementation = CompositionOptimizationAlgorithm.class,
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable CompositionOptimizationAlgorithm algorithm,
    @Schema(
        description = "Composition calculator code.",
        implementation = CompositionCalculatorCode.class,
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable CompositionCalculatorCode calculator,
    @Schema(
        description = "Axis normalizer code.",
        implementation = ProfileAxisNormalizerCode.class,
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ProfileAxisNormalizerCode normalizer,
    @Schema(
        description = "Objective function code.",
        implementation = ProfileTargetObjectiveFunctionCode.class,
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ProfileTargetObjectiveFunctionCode objectiveFunction,
    @Schema(
        description = "Optional YAN readiness criterion. YAN affects objective score as a readiness penalty, "
            + "not profile-target distance.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable CompositionOptimizationYanReadinessRequest yanReadiness,
    @Schema(
        description = "Discrete search step in percent.",
        example = "5",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable Integer stepPercent,
    @Schema(
        description = "Maximum candidates returned.",
        example = "10",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable Integer limit,
    @Schema(
        description = "Target total volume in liters for absolute-volume optimization.",
        example = "100.0",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable BigDecimal targetTotalVolumeLiters
) {
}
