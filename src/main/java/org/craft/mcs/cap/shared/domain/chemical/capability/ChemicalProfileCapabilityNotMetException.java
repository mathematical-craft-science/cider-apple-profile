package org.craft.mcs.cap.shared.domain.chemical.capability;

import java.io.Serial;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class ChemicalProfileCapabilityNotMetException
    extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull ChemicalProfileCapabilityAssessment assessment;

  public ChemicalProfileCapabilityNotMetException(
      @NonNull ChemicalProfileCapabilityAssessment assessment
  ) {
    super("chemical profile does not meet capability requirements: " + requireNonNull(assessment).capability());
    this.assessment = assessment;
  }

  public @NonNull ChemicalProfileCapabilityAssessment assessment() {
    return assessment;
  }
}
