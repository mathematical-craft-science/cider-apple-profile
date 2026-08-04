package org.craft.mcs.cap.core.production.candidate.application.model;

import java.util.List;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidate;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionWarning;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Persisted production candidate together with immutable analysis metadata and its concurrency revision.
 */
public record ProductionBatchCandidateSnapshot(
    @NonNull ProductionBatchCandidate candidate,
    @NonNull PotentialAbvPrediction potentialAbv,
    @Nullable ProfileAxisNormalizerCode normalizerCode,
    @Nullable ProfileTargetDistance targetDistance,
    @NonNull List<@NonNull CompositionWarning> warnings,
    long revision
) {

  public ProductionBatchCandidateSnapshot(
      @NonNull ProductionBatchCandidate candidate,
      @NonNull PotentialAbvPrediction potentialAbv,
      @Nullable ProfileAxisNormalizerCode normalizerCode,
      @Nullable ProfileTargetDistance targetDistance,
      @NonNull List<@NonNull CompositionWarning> warnings
  ) {
    this(candidate, potentialAbv, normalizerCode, targetDistance, warnings, 0L);
  }

  public ProductionBatchCandidateSnapshot {
    requireNonNull(candidate, "production batch candidate cannot be null");
    requireNonNull(potentialAbv, "production batch candidate potential ABV cannot be null");
    warnings = List.copyOf(requireNonNull(warnings, "production candidate warnings cannot be null"));
    if (revision < 0L) {
      throw new ApplicationValidationException("production candidate revision cannot be negative");
    }
  }

  public @NonNull ProductionBatchCandidateSnapshot withCandidate(
      @NonNull ProductionBatchCandidate updatedCandidate
  ) {
    return new ProductionBatchCandidateSnapshot(
        requireNonNull(updatedCandidate, "updated production batch candidate cannot be null"),
        potentialAbv,
        normalizerCode,
        targetDistance,
        warnings,
        revision
    );
  }
}
