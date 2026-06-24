package org.craft.mcs.cap.core.profileanalysis.classification.result;

import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record BorderlineEvidence(
    @NonNull ChemicalAxis axis,
    @NonNull BorderlineReason reason
) {

  public BorderlineEvidence {
    requireNonNull(axis, "chemical axis cannot be null");
    requireNonNull(reason, "borderline reason cannot be null");
  }
}
