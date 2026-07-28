package org.craft.mcs.cap.core.measurement.application.compatibility;

import java.util.List;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Transitional chemical profile assembled from state-aware current measurement evidence. */
public record CurrentMeasurementProfileView(
    @NonNull MaterialStateId currentMaterialStateId,
    @NonNull ChemicalProfile chemicalProfile,
    @NonNull List<@NonNull MeasurementEvidenceView> evidence
) {

  public CurrentMeasurementProfileView {
    requireNonNull(currentMaterialStateId, "current material state id cannot be null");
    requireNonNull(chemicalProfile, "current measurement chemical profile cannot be null");
    evidence = List.copyOf(requireNonNull(evidence, "current measurement evidence cannot be null"));
  }
}
