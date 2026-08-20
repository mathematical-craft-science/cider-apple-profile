package org.craft.mcs.cap.core.cideranalysis.classification.domain.result;

import java.util.List;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

public record ClassificationAssessment(
    @NonNull ClassificationConfidence confidence,
    @NonNull List<@NonNull BorderlineEvidence> evidence
) {

  public ClassificationAssessment {
    requireNonNull(confidence, "classification confidence cannot be null");
    requireNonNull(evidence, "borderline evidence cannot be null");
    evidence = List.copyOf(evidence);
  }

  public boolean borderline() {
    return !evidence.isEmpty();
  }
}
