package org.craft.mcs.cap.core.production.application.model;

import java.util.List;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Current production profile obtained by overlaying measured axes on the accepted initial snapshot.
 */
public record ProductionCurrentProfileView(
    @NonNull ChemicalProfile chemicalProfile,
    @NonNull List<@NonNull ProductionMeasurementEvidenceView> evidence
) {

  public ProductionCurrentProfileView {
    requireNonNull(chemicalProfile, "production current chemical profile cannot be null");
    evidence = List.copyOf(requireNonNull(evidence, "production measurement evidence cannot be null"));
  }
}
