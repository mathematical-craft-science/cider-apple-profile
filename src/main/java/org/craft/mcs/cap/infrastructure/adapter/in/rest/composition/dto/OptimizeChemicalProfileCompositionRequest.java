package org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain.CompositionOptimizationMode;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Request to optimize source composition against a profile target.")
public record OptimizeChemicalProfileCompositionRequest(
    @ArraySchema(schema = @Schema(description = "Optimization source.", implementation = ChemicalProfileCompositionOptimizationSourceRequest.class))
    @NonNull List<@NonNull ChemicalProfileCompositionOptimizationSourceRequest> sources,
    @Schema(description = "Policy for optional tannin calculation.", implementation = ChemicalProfileCompositionTanninPolicy.class)
    @Nullable ChemicalProfileCompositionTanninPolicy tanninPolicy,
    @Schema(description = "Required profile target.")
    @NonNull ProfileTargetRequest target,
    @Schema(description = "Optional tannin target range.")
    @Nullable ProfileTargetTanninRangeRequest tanninTarget,
    @Schema(description = "Optional axis weights.")
    @Nullable ProfileTargetObjectiveWeightsRequest weights,
    @Schema(description = "Scoring purpose used to select defaults.", implementation = ProfileTargetScoringPurpose.class)
    @Nullable ProfileTargetScoringPurpose scoringPurpose,
    @Schema(description = "Optimization mode.", implementation = CompositionOptimizationMode.class)
    @NonNull CompositionOptimizationMode mode,
    @Schema(description = "Optimization algorithm.", implementation = CompositionOptimizationAlgorithm.class)
    @Nullable CompositionOptimizationAlgorithm algorithm,
    @Schema(description = "Composition calculator code.", implementation = ChemicalProfileCompositionCalculatorCode.class)
    @Nullable ChemicalProfileCompositionCalculatorCode calculator,
    @Schema(description = "Axis normalizer code.", implementation = ProfileAxisNormalizerCode.class)
    @Nullable ProfileAxisNormalizerCode normalizer,
    @Schema(description = "Objective function code.", implementation = ProfileTargetObjectiveFunctionCode.class)
    @Nullable ProfileTargetObjectiveFunctionCode objectiveFunction,
    @Schema(description = "Discrete search step in percent.", example = "5")
    @Nullable Integer stepPercent,
    @Schema(description = "Maximum candidates returned.", example = "10")
    @Nullable Integer limit,
    @Schema(description = "Target total volume in liters for absolute-volume optimization.", example = "100.0")
    @Nullable BigDecimal targetTotalVolumeLiters
) {
}
