package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Request to calculate a composition from explicit sources and weights.")
public record CalculateCompositionRequest(
    @ArraySchema(
        arraySchema = @Schema(description = "Composition sources.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = CompositionSourceRequest.class)
    )
    @NonNull List<@NonNull CompositionSourceRequest> sources,
    @Schema(
        description = "Policy for optional tannin calculation.",
        implementation = CompositionTanninPolicy.class,
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable CompositionTanninPolicy tanninPolicy,
    @Schema(
        description = "Optional target used to calculate distance for the resulting composition.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ProfileTargetRequest target,
    @Schema(
        description = "Optional tannin target range.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ProfileTargetTanninRangeRequest tanninTarget,
    @Schema(description = "Optional axis weights.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ProfileTargetObjectiveWeightsRequest weights,
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
    @Nullable ProfileTargetObjectiveFunctionCode objectiveFunction
) {
}
