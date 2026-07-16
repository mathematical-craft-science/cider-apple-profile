package org.craft.mcs.cap.core.production.candidate.application.model;

import java.util.List;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidate;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionWarning;
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
    @Nullable ProfileAxisNormalizerCode normalizerCode,
    @Nullable ProfileTargetDistance targetDistance,
    @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings,
    long revision
) {

  public ProductionBatchCandidateSnapshot(
      @NonNull ProductionBatchCandidate candidate,
      @Nullable ProfileAxisNormalizerCode normalizerCode,
      @Nullable ProfileTargetDistance targetDistance,
      @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings
  ) {
    this(candidate, normalizerCode, targetDistance, warnings, 0L);
  }

  public ProductionBatchCandidateSnapshot {
    requireNonNull(candidate, "production batch candidate cannot be null");
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
        normalizerCode,
        targetDistance,
        warnings,
        revision
    );
  }
}
