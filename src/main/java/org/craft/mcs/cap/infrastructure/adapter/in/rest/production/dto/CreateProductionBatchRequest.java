package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

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


public record CreateProductionBatchRequest(
    @NonNull UUID ciderMakerId,
    @NonNull String name,
    @NonNull ProductionBatchKind kind,
    @NonNull List<@NonNull CreateProductionBatchSourceRequest> sources,
    @Nullable ChemicalProfileCompositionTanninPolicy tanninPolicy,
    @Nullable ProfileTargetRequest target,
    @Nullable ProfileTargetTanninRangeRequest tanninTarget,
    @Nullable ProfileTargetObjectiveWeightsRequest weights,
    @Nullable ProfileTargetScoringPurpose scoringPurpose,
    @Nullable ChemicalProfileCompositionCalculatorCode calculator,
    @Nullable ProfileAxisNormalizerCode normalizer,
    @Nullable ProfileTargetObjectiveFunctionCode objectiveFunction,
    @Nullable String note
) {
}
