package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationMode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockSeasonRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationYanReadinessRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@Schema(description = "Request to optimize feedstock batch source composition against a profile target.")
public record OptimizeFeedstockBatchCompositionRequest(
    @Schema(
        description = "Workspace identifier.",
        format = "uuid",
        example = "4d96759c-86ff-4d39-8b19-241bc60f82ff",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID workspaceId,
    @ArraySchema(
        arraySchema = @Schema(description = "Optional feedstock batch id filter.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED),
        schema = @Schema(format = "uuid")
    )
    @Nullable List<@NonNull UUID> batchIds,
    @Schema(description = "Optional season overlap filter.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable FeedstockSeasonRequest season,
    @Schema(description = "Optional sugar-acidity zone filter.", implementation = SugarAcidityZone.class, example = "PERFECT_APPLES", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable SugarAcidityZone zone,
    @Schema(description = "Optional chemical profile capability filter.", implementation = ChemicalProfileCapability.class, example = "SUGAR_ACIDITY_PLOT", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ChemicalProfileCapability capability,
    @Schema(description = "Whether the optional capability must be met.", example = "true", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable Boolean capable,
    @Schema(description = "Scoring purpose used to select default normalizer.", implementation = ProfileTargetScoringPurpose.class, example = "BLENDING_PLANNING", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ProfileTargetScoringPurpose purpose,
    @Schema(description = "Required target.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileTargetRequest target,
    @Schema(description = "Optional tannin target range.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ProfileTargetTanninRangeRequest tanninTarget,
    @Schema(description = "Optional axis weights.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ProfileTargetObjectiveWeightsRequest weights,
    @Schema(description = "Optimization mode.", implementation = CompositionOptimizationMode.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull CompositionOptimizationMode mode,
    @Schema(description = "Optimization algorithm.", implementation = CompositionOptimizationAlgorithm.class, nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable CompositionOptimizationAlgorithm algorithm,
    @Schema(description = "Composition calculator code.", implementation = ChemicalProfileCompositionCalculatorCode.class, nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ChemicalProfileCompositionCalculatorCode calculator,
    @Schema(description = "Axis normalizer code.", implementation = ProfileAxisNormalizerCode.class, nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ProfileAxisNormalizerCode normalizer,
    @Schema(description = "Objective function code.", implementation = ProfileTargetObjectiveFunctionCode.class, nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ProfileTargetObjectiveFunctionCode objectiveFunction,
    @Schema(description = "Optional YAN readiness criterion. YAN affects objective score as a readiness penalty, not profile-target distance.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable CompositionOptimizationYanReadinessRequest yanReadiness,
    @Schema(description = "Discrete search step in percent.", example = "5", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable Integer stepPercent,
    @Schema(description = "Maximum optimization candidates returned.", example = "10", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable Integer limit,
    @Schema(description = "Maximum source batches considered before optimization.", example = "8", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable Integer sourceLimit,
    @Schema(description = "Target total volume in liters for absolute-volume optimization.", example = "100.0", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable BigDecimal targetTotalVolumeLiters
) {

  public OptimizeFeedstockBatchCompositionRequest {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(target, "feedstock batch profile target cannot be null");
    requireNonNull(mode, "composition optimization mode cannot be null");
    batchIds = batchIds == null ? null : List.copyOf(batchIds);
  }
}
