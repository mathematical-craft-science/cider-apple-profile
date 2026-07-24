package org.craft.mcs.cap.core.measurement.application.model;

import java.util.List;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Composite current profile assembled independently from the latest result for each characteristic. */
public record CurrentMeasurementProfileView(
    @NonNull ChemicalProfile chemicalProfile,
    @NonNull List<@NonNull MeasurementEvidenceView> evidence
) {

  public CurrentMeasurementProfileView {
    requireNonNull(chemicalProfile, "current measurement chemical profile cannot be null");
    evidence = List.copyOf(requireNonNull(evidence, "current measurement evidence cannot be null"));
  }
}
