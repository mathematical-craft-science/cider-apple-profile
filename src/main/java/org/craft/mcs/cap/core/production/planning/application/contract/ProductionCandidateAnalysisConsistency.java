package org.craft.mcs.cap.core.production.planning.application.contract;

import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Protects the semantic consistency of analysis facts calculated for one production profile. */
final class ProductionCandidateAnalysisConsistency {

  private ProductionCandidateAnalysisConsistency() {
  }

  static void validate(
      @NonNull ProductionProfileSnapshot profile,
      @NonNull ProductionCandidatePotentialAbvView potentialAbv,
      @Nullable ProductionCandidateTargetDistanceView targetDistance
  ) {
    requireNonNull(profile, "production candidate profile cannot be null");
    requireNonNull(potentialAbv, "production candidate potential ABV cannot be null");
    if (!profile.specificGravity().equals(potentialAbv.originalGravity())) {
      throw new ApplicationValidationException(
          "production candidate ABV original gravity must match the calculated profile"
      );
    }
    if (targetDistance == null) {
      return;
    }
    final var point = targetDistance.point();
    if (!profile.specificGravity().equals(point.specificGravity())
        || !profile.titratableAcidity().equals(point.titratableAcidity())) {
      throw new ApplicationValidationException(
          "production candidate target point must match the calculated profile"
      );
    }
    final var scoredTannin = targetDistance.tannin();
    if (scoredTannin != null && !scoredTannin.equals(profile.tannin())) {
      throw new ApplicationValidationException(
          "production candidate target tannin must match the calculated profile"
      );
    }
  }
}
