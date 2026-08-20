package org.craft.mcs.cap.core.production.planning.application.contract;

import java.util.List;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidateId;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidateStatus;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionProfileSnapshot;
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
    @NonNull ProductionCandidatePotentialAbvView potentialAbv,
    @Nullable ProductionCandidateNormalizer normalizerCode,
    @Nullable ProductionCandidateTargetDistanceView targetDistance,
    @NonNull List<@NonNull ProductionCandidateWarning> warnings,
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
