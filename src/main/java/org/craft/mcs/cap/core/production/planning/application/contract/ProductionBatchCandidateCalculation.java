package org.craft.mcs.cap.core.production.planning.application.contract;

import java.util.List;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Result of calculating a production candidate from authoritative source snapshots. */
public record ProductionBatchCandidateCalculation(
    @NonNull ProductionProfileSnapshot profile,
    @NonNull ProductionCandidatePotentialAbvView potentialAbv,
    @Nullable ProductionCandidateNormalizer normalizerCode,
    @Nullable ProductionCandidateTargetDistanceView targetDistance,
    @NonNull List<@NonNull ProductionCandidateWarning> warnings
) {

  public ProductionBatchCandidateCalculation {
    requireNonNull(profile, "production candidate profile cannot be null");
    requireNonNull(potentialAbv, "production candidate potential ABV cannot be null");
    warnings = List.copyOf(requireNonNull(warnings, "production candidate warnings cannot be null"));
    if ((normalizerCode == null) != (targetDistance == null)) {
      throw new ApplicationValidationException(
          "production candidate target normalizer and distance must be present together"
      );
    }
    ProductionCandidateAnalysisConsistency.validate(profile, potentialAbv, targetDistance);
  }
}
