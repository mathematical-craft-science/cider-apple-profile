package org.craft.mcs.cap.core.production.application.model;

import java.util.List;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Production-owned current measurement projection supplied through an outbound boundary. */
public record ProductionMeasurementProfileView(
    @NonNull ChemicalProfile chemicalProfile,
    @NonNull List<@NonNull ProductionMeasurementEvidenceView> evidence
) {

  public ProductionMeasurementProfileView {
    requireNonNull(chemicalProfile, "production measured chemical profile cannot be null");
    evidence = List.copyOf(requireNonNull(evidence, "production measurement evidence cannot be null"));
  }
}
