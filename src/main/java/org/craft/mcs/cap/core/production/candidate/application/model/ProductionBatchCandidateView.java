package org.craft.mcs.cap.core.production.candidate.application.model;

import java.util.List;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateId;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateStatus;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionWarning;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Application read model of a saved production candidate. */
public record ProductionBatchCandidateView(
    @NonNull ProductionBatchCandidateId candidateId,
    @NonNull WorkspaceId workspaceId,
    @NonNull ProductionBatchCandidateStatus status,
    @NonNull ProductionSourceComposition sourceComposition,
    @NonNull ProductionProfileSnapshot profile,
    @NonNull PotentialAbvPrediction potentialAbv,
    @Nullable ProfileAxisNormalizerCode normalizerCode,
    @Nullable ProfileTargetDistance targetDistance,
    @NonNull List<@NonNull CompositionWarning> warnings,
    @Nullable ProductionBatchId confirmedProductionBatchId
) {

  public ProductionBatchCandidateView {
    requireNonNull(candidateId, "production batch candidate id cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(status, "production batch candidate status cannot be null");
    requireNonNull(sourceComposition, "production candidate source composition cannot be null");
    requireNonNull(profile, "production candidate profile cannot be null");
    requireNonNull(potentialAbv, "production candidate potential ABV cannot be null");
    warnings = List.copyOf(requireNonNull(warnings, "production candidate warnings cannot be null"));
  }
}
