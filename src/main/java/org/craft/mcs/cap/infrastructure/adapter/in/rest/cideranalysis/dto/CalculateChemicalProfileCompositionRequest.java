package org.craft.mcs.cap.infrastructure.adapter.in.rest.cideranalysis.dto;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionTanninPolicy;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record CalculateChemicalProfileCompositionRequest(
    @NonNull List<@NonNull ChemicalProfileCompositionSourceRequest> sources,
    @Nullable ChemicalProfileCompositionTanninPolicy tanninPolicy,
    @Nullable ProfileTargetRequest target,
    @Nullable ProfileTargetTanninRangeRequest tanninTarget,
    @Nullable ProfileTargetObjectiveWeightsRequest weights,
    @Nullable ProfileTargetScoringPurpose scoringPurpose
) {
}
