package org.craft.mcs.cap.core.production.candidate.application.model;

import java.util.List;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionWarning;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Result of calculating a production candidate from authoritative source snapshots. */
public record ProductionBatchCandidateCalculation(
    @NonNull ProductionProfileSnapshot profile,
    @NonNull PotentialAbvPrediction potentialAbv,
    @Nullable ProfileAxisNormalizerCode normalizerCode,
    @Nullable ProfileTargetDistance targetDistance,
    @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings
) {

  public ProductionBatchCandidateCalculation {
    requireNonNull(profile, "production candidate profile cannot be null");
    requireNonNull(potentialAbv, "production candidate potential ABV cannot be null");
    warnings = List.copyOf(requireNonNull(warnings, "production candidate warnings cannot be null"));
  }
}
