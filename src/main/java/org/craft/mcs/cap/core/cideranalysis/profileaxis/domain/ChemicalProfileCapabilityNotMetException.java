package org.craft.mcs.cap.core.cideranalysis.profileaxis.domain;

import java.io.Serial;
import org.craft.mcs.cap.shared.kernel.DomainException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class ChemicalProfileCapabilityNotMetException
    extends DomainException {

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
