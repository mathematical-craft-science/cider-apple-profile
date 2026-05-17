package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Request to create a production batch from feedstock sources.")
public record CreateProductionBatchRequest(
    @Schema(description = "Cider maker identifier.", format = "uuid", example = "4d96759c-86ff-4d39-8b19-241bc60f82ff")
    @NonNull UUID ciderMakerId,
    @Schema(description = "Production batch name.", example = "October cider base")
    @NonNull String name,
    @Schema(description = "Production batch kind.", implementation = ProductionBatchKind.class, example = "CIDER")
    @NonNull ProductionBatchKind kind,
    @ArraySchema(schema = @Schema(description = "Feedstock source.", implementation = CreateProductionBatchSourceRequest.class))
    @NonNull List<@NonNull CreateProductionBatchSourceRequest> sources,
    @Schema(description = "Policy for optional tannin calculation.", implementation = ChemicalProfileCompositionTanninPolicy.class)
    @Nullable ChemicalProfileCompositionTanninPolicy tanninPolicy,
    @Schema(description = "Optional target used to calculate distance for the resulting production profile.")
    @Nullable ProfileTargetRequest target,
    @Schema(description = "Optional tannin target range.")
    @Nullable ProfileTargetTanninRangeRequest tanninTarget,
    @Schema(description = "Optional objective weights.")
    @Nullable ProfileTargetObjectiveWeightsRequest weights,
    @Schema(description = "Scoring purpose used to select defaults.", implementation = ProfileTargetScoringPurpose.class)
    @Nullable ProfileTargetScoringPurpose scoringPurpose,
    @Schema(description = "Composition calculator code.", implementation = ChemicalProfileCompositionCalculatorCode.class)
    @Nullable ChemicalProfileCompositionCalculatorCode calculator,
    @Schema(description = "Axis normalizer code.", implementation = ProfileAxisNormalizerCode.class)
    @Nullable ProfileAxisNormalizerCode normalizer,
    @Schema(description = "Objective function code.", implementation = ProfileTargetObjectiveFunctionCode.class)
    @Nullable ProfileTargetObjectiveFunctionCode objectiveFunction,
    @Schema(description = "Optional production note.", example = "Planned first fermentation in tank A.")
    @Nullable String note
) {
}
