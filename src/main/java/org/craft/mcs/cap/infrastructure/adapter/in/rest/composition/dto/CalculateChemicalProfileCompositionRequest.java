package org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Request to calculate a chemical profile composition from explicit sources and weights.")
public record CalculateChemicalProfileCompositionRequest(
    @ArraySchema(schema = @Schema(description = "Composition source.", implementation = ChemicalProfileCompositionSourceRequest.class))
    @NonNull List<@NonNull ChemicalProfileCompositionSourceRequest> sources,
    @Schema(description = "Policy for optional tannin calculation.", implementation = ChemicalProfileCompositionTanninPolicy.class)
    @Nullable ChemicalProfileCompositionTanninPolicy tanninPolicy,
    @Schema(description = "Optional target used to calculate distance for the resulting composition.")
    @Nullable ProfileTargetRequest target,
    @Schema(description = "Optional tannin target range.")
    @Nullable ProfileTargetTanninRangeRequest tanninTarget,
    @Schema(description = "Optional axis weights.")
    @Nullable ProfileTargetObjectiveWeightsRequest weights,
    @Schema(description = "Scoring purpose used to select defaults.", implementation = ProfileTargetScoringPurpose.class)
    @Nullable ProfileTargetScoringPurpose scoringPurpose,
    @Schema(description = "Composition calculator code.", implementation = ChemicalProfileCompositionCalculatorCode.class)
    @Nullable ChemicalProfileCompositionCalculatorCode calculator,
    @Schema(description = "Axis normalizer code.", implementation = ProfileAxisNormalizerCode.class)
    @Nullable ProfileAxisNormalizerCode normalizer,
    @Schema(description = "Objective function code.", implementation = ProfileTargetObjectiveFunctionCode.class)
    @Nullable ProfileTargetObjectiveFunctionCode objectiveFunction
) {
}
