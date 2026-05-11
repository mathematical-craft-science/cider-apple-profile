package org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record CalculateChemicalProfileCompositionRequest(
    @NonNull List<@NonNull ChemicalProfileCompositionSourceRequest> sources,
    @Nullable ChemicalProfileCompositionTanninPolicy tanninPolicy,
    @Nullable ProfileTargetRequest target,
    @Nullable ProfileTargetTanninRangeRequest tanninTarget,
    @Nullable ProfileTargetObjectiveWeightsRequest weights,
    @Nullable ProfileTargetScoringPurpose scoringPurpose,
    @Nullable ChemicalProfileCompositionCalculatorCode calculator,
    @Nullable ProfileAxisNormalizerCode normalizer,
    @Nullable ProfileTargetObjectiveFunctionCode objectiveFunction
) {
}
